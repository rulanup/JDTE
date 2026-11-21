package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.autoioconfig.OverclockDirectTransferHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public abstract class ItemSenderBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE, BaseFilterMachine {
    public static final int STORAGE_SLOTS = 9;

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStacksResourceHandler itemHandler;
    private int transferRetryTicks;
    private int transferFailureBackoff;
    private boolean transferMoved;

    protected ItemSenderBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = STORAGE_SLOTS;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        // 设置默认半径为1，这样可以搜索相邻方块
        areaAffectingData.xRadius = 1;
        areaAffectingData.yRadius = 1;
        areaAffectingData.zRadius = 1;
        itemHandler = new MachineInventoryHandler(STORAGE_SLOTS) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                resetTransferBackoff();
                setChanged();
            }
        };
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    @Override
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
    }

    @Override
    public void setAreaSettings(double xRadius, double yRadius, double zRadius,
                                int xOffset, int yOffset, int zOffset, boolean renderArea) {
        AreaAffectingBE.super.setAreaSettings(
                xRadius, yRadius, zRadius, xOffset, yOffset, zOffset, renderArea);
        OverclockDirectTransferHelper.invalidate(this);
        resetTransferBackoff();
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (transferRetryTicks > 0) {
            transferRetryTicks--;
            return;
        }
        if (isActiveRedstone() && canRun()) {
            if (OverclockDirectTransferHelper.isEnabled(this)) {
                int moved = canRunDirectTransfer() ? OverclockDirectTransferHelper.transfer(this) : 0;
                if (moved > 0) onDirectTransferSuccess();
                updateTransferBackoff(moved > 0);
                return;
            }
            transferMoved = false;
            sendItems();
            updateTransferBackoff(transferMoved);
        }
    }

    protected void sendItems() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int itemsToSend = getItemsToSend();
        int sent = 0;

        for (BlockPos targetPos : OverclockDirectTransferHelper.getCachedItemPositions(
                serverLevel, this, this)) {
            if (sent >= itemsToSend) break;

            Direction preferredSide = getSideFacingMachine(targetPos);
            if (preferredSide != null) {
                sent += sendItemsToSide(serverLevel, targetPos, preferredSide, itemsToSend - sent);
            }
            for (Direction side : Direction.values()) {
                if (sent >= itemsToSend) break;
                if (side != preferredSide) {
                    sent += sendItemsToSide(serverLevel, targetPos, side, itemsToSend - sent);
                }
            }
            if (sent < itemsToSend) {
                sent += sendItemsToSide(serverLevel, targetPos, null, itemsToSend - sent);
            }
        }

        if (sent > 0) {
            transferMoved = true;
            setChanged();
        }
    }

    private int sendItemsToSide(ServerLevel serverLevel, BlockPos targetPos, Direction side, int limit) {
        var adjacent = serverLevel.getCapability(Capabilities.Item.BLOCK, targetPos, side);
        if (adjacent == null || adjacent == itemHandler || limit <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int moved = ResourceHandlerUtil.moveStacking(itemHandler, adjacent, resource -> true, limit, transaction);
            transaction.commit();
            return moved;
        }
    }

    private Direction getSideFacingMachine(BlockPos targetPos) {
        int dx = getBlockPos().getX() - targetPos.getX();
        int dy = getBlockPos().getY() - targetPos.getY();
        int dz = getBlockPos().getZ() - targetPos.getZ();
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);
        int absZ = Math.abs(dz);

        if (absX == 0 && absY == 0 && absZ == 0) return null;
        if (absY >= absX && absY >= absZ) return dy > 0 ? Direction.UP : Direction.DOWN;
        if (absX >= absZ) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    protected int getItemsToSend() {
        if (UpgradeHelper.hasCreativeUpgrade(this)
                || UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) {
            return JDTEConfig.COMMON.senderReceiverOverclockItemTransferRate.get();
        }
        return JDTEConfig.COMMON.senderReceiverItemTransferRate.get();
    }

    private void updateTransferBackoff(boolean moved) {
        if (moved) {
            transferFailureBackoff = 0;
            return;
        }
        int maximum = JDTEConfig.COMMON.transferFailureBackoffMax.get();
        transferFailureBackoff = transferFailureBackoff <= 0
                ? Math.min(JDTEConfig.COMMON.transferFailureBackoffStart.get(), maximum)
                : Math.min(maximum, transferFailureBackoff * 2);
        transferRetryTicks = transferFailureBackoff;
    }

    private void resetTransferBackoff() {
        transferRetryTicks = 0;
        transferFailureBackoff = 0;
    }

    protected boolean canRunDirectTransfer() {
        return true;
    }

    protected void onDirectTransferSuccess() {
    }

    public ItemStacksResourceHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        return getData(JDTRegistration.HANDLER_BASIC_FILTER);
    }

    @Override
    public FilterData getFilterData() {
        return filterData;
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", itemHandler);
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(itemHandler::deserialize);
        loadAreaSettings(tag);
        areaAffectingData.area = null;
        OverclockDirectTransferHelper.invalidate(this);
    }
}
