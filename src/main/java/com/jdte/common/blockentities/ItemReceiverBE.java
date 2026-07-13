package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.autoioconfig.OverclockDirectTransferHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class ItemReceiverBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE, BaseFilterMachine {
    public static final int STORAGE_SLOTS = 9;

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStackHandler itemHandler;
    private int transferRetryTicks;
    private int transferFailureBackoff;
    private boolean transferMoved;

    protected ItemReceiverBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = STORAGE_SLOTS;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        // 设置默认半径为1，这样可以搜索相邻方块
        areaAffectingData.xRadius = 1;
        areaAffectingData.yRadius = 1;
        areaAffectingData.zRadius = 1;
        itemHandler = new ItemStackHandler(STORAGE_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                resetTransferBackoff();
                setChanged();
            }
        };
    }

    @Override
    public ItemStackHandler getMachineHandler() {
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
            receiveItems();
            updateTransferBackoff(transferMoved);
        }
    }

    protected void receiveItems() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int itemsToReceive = getItemsToReceive();
        int received = 0;

        for (BlockPos sourcePos : OverclockDirectTransferHelper.getCachedItemPositions(
                serverLevel, this, this)) {
            if (received >= itemsToReceive) break;

            Direction preferredSide = getSideFacingMachine(sourcePos);
            if (preferredSide != null) {
                received += receiveItemsFromSide(serverLevel, sourcePos, preferredSide, itemsToReceive - received);
            }
            for (Direction side : Direction.values()) {
                if (received >= itemsToReceive) break;
                if (side != preferredSide) {
                    received += receiveItemsFromSide(serverLevel, sourcePos, side, itemsToReceive - received);
                }
            }
            if (received < itemsToReceive) {
                received += receiveItemsFromSide(serverLevel, sourcePos, null, itemsToReceive - received);
            }
        }

        if (received > 0) {
            transferMoved = true;
            setChanged();
        }
    }

    private int receiveItemsFromSide(ServerLevel serverLevel, BlockPos sourcePos, Direction side, int limit) {
        IItemHandler sourceHandler = serverLevel.getCapability(Capabilities.ItemHandler.BLOCK, sourcePos, side);
        if (sourceHandler == null) return 0;

        int received = 0;
        for (int i = 0; i < sourceHandler.getSlots() && received < limit; i++) {
            ItemStack simulated = sourceHandler.extractItem(i, limit - received, true);
            if (simulated.isEmpty()) continue;

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(itemHandler, simulated, true);
            int accepted = simulated.getCount() - remainder.getCount();
            if (accepted <= 0) continue;

            ItemStack extracted = sourceHandler.extractItem(i, accepted, false);
            if (extracted.isEmpty()) continue;

            ItemStack notInserted = ItemHandlerHelper.insertItemStacked(itemHandler, extracted, false);
            int actuallyReceived = extracted.getCount() - notInserted.getCount();
            received += actuallyReceived;
            if (!notInserted.isEmpty()) {
                sourceHandler.insertItem(i, notInserted, false);
            }
        }
        return received;
    }

    private Direction getSideFacingMachine(BlockPos sourcePos) {
        int dx = getBlockPos().getX() - sourcePos.getX();
        int dy = getBlockPos().getY() - sourcePos.getY();
        int dz = getBlockPos().getZ() - sourcePos.getZ();
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);
        int absZ = Math.abs(dz);

        if (absX == 0 && absY == 0 && absZ == 0) return null;
        if (absY >= absX && absY >= absZ) return dy > 0 ? Direction.UP : Direction.DOWN;
        if (absX >= absZ) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    protected int getItemsToReceive() {
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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        return getData(Registration.HANDLER_BASIC_FILTER);
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        loadAreaSettings(tag);
        areaAffectingData.area = null;
        OverclockDirectTransferHelper.invalidate(this);
    }
}
