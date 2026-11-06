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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public abstract class ItemSenderBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE, BaseFilterMachine {
    public static final int STORAGE_SLOTS = 9;

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStackHandler itemHandler;
    /**
     * The filter handler, cached at construction from the capability provider.
     * It is the same object the provider hands out, so capability reads and
     * NBT (de)serialization stay consistent. Caching matters because vanilla
     * survival destroyBlock removes the block entity (invalidating its
     * capabilities) BEFORE calling dropResources, and JDT's getDrops then
     * calls saveAdditional, which reads getFilterHandler(). The original
     * 0.6.5.2 stores the filter as a data attachment, which survives removal;
     * the Forge port's capability lookup throws instead, crashing the server
     * when a non-default sender is broken in survival. The cache restores
     * the original survival behaviour for senders without touching other
     * machines' capability handling.
     */
    private final FilterBasicHandler cachedFilterHandler;
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
        itemHandler = new ItemStackHandler(STORAGE_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                resetTransferBackoff();
                setChanged();
            }
        };
        // Mirror BioExtractorBE: establish the attached filter storage in the
        // constructor, before the first capability/NBT snapshot, and cache
        // the handler so saveAdditional still works after the block entity
        // is removed (see the cachedFilterHandler comment above).
        cachedFilterHandler = com.jdte.setup.JDTEAttachments.filter(this);
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
        IItemHandler targetHandler = com.jdte.common.capabilities.ForgeCapabilityHelper.get(serverLevel, targetPos, ForgeCapabilities.ITEM_HANDLER, side);
        if (targetHandler == null) return 0;

        int sent = 0;
        for (int i = 0; i < STORAGE_SLOTS && sent < limit; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            int canSend = Math.min(stack.getCount(), limit - sent);
            ItemStack toSend = stack.copyWithCount(canSend);
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(targetHandler, toSend, false);
            int actuallySent = canSend - remaining.getCount();
            if (actuallySent > 0) {
                itemHandler.extractItem(i, actuallySent, false);
                sent += actuallySent;
            }
        }
        return sent;
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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        // After construction the cached handler is the provider's handler.
        // Fall back to the capability only if called before the constructor
        // assignment (defensive; no such call exists in the current code).
        return cachedFilterHandler != null ? cachedFilterHandler : com.jdte.setup.JDTEAttachments.filter(this);
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        saveAreaSettings(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
        }
        loadAreaSettings(tag);
        areaAffectingData.area = null;
        OverclockDirectTransferHelper.invalidate(this);
    }
}
