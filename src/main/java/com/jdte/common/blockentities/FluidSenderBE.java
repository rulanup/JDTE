package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.autoioconfig.OverclockDirectTransferHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FluidSenderBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, FluidMachineBE, AreaAffectingBE, BaseFilterMachine {
    public static final int BASE_FLUID_CAPACITY = 8000; // 8 buckets
    private static final int TARGET_REFRESH_INTERVAL_TICKS = 40;

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    public final JDTEFluidTank fluidTank;

    public final FluidContainerData fluidContainerData;
    private int transferRetryTicks;
    private int transferFailureBackoff;
    private List<BlockPos> cachedTargets = List.of();
    private final Map<BlockPos, Direction> successfulTargetSides = new HashMap<>();
    private AABB cachedTargetArea;
    private long nextTargetRefreshTick;
    private int targetCursor;

    protected FluidSenderBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        // 设置默认半径为1，这样可以搜索相邻方块
        areaAffectingData.xRadius = 1;
        areaAffectingData.yRadius = 1;
        areaAffectingData.zRadius = 1;
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> true) {
            @Override
            protected void onContentsChanged() {
                resetTransferBackoff();
                setChanged();
            }
        };
        fluidContainerData = new FluidContainerData(this);
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
        cachedTargetArea = null;
        nextTargetRefreshTick = 0;
        OverclockDirectTransferHelper.invalidate(this);
        resetTransferBackoff();
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
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
            updateTransferBackoff(sendFluid() > 0);
        }
    }

    protected int sendFluid() {
        if (!(level instanceof ServerLevel serverLevel)) return 0;

        refreshTargetsIfNeeded(serverLevel);
        if (cachedTargets.isEmpty()) return 0;

        int transferLimit = getFluidToSend();
        int transferred = 0;
        int targetCount = cachedTargets.size();
        int startIndex = Math.floorMod(targetCursor, targetCount);
        int attempted = 0;
        while (attempted < targetCount && transferred < transferLimit && !fluidTank.isEmpty()) {
            int targetIndex = (startIndex + attempted) % targetCount;
            transferred += sendFluidToTarget(serverLevel, cachedTargets.get(targetIndex), transferLimit - transferred);
            attempted++;
        }
        targetCursor = (startIndex + Math.max(1, attempted)) % targetCount;
        return transferred;
    }

    private int sendFluidToTarget(ServerLevel serverLevel, BlockPos targetPos, int limit) {
        Direction successfulSide = successfulTargetSides.get(targetPos);
        if (successfulSide != null) {
            int transferred = sendFluidToSide(serverLevel, targetPos, successfulSide, limit);
            if (transferred > 0) return transferred;
            successfulTargetSides.remove(targetPos);
        }

        Direction preferredSide = getSideFacingMachine(targetPos);
        if (preferredSide != null && preferredSide != successfulSide) {
            int transferred = sendFluidToSide(serverLevel, targetPos, preferredSide, limit);
            if (transferred > 0) {
                successfulTargetSides.put(targetPos, preferredSide);
                return transferred;
            }
        }
        for (Direction side : Direction.values()) {
            if (side == preferredSide || side == successfulSide) continue;
            int transferred = sendFluidToSide(serverLevel, targetPos, side, limit);
            if (transferred > 0) {
                successfulTargetSides.put(targetPos, side);
                return transferred;
            }
        }
        int transferred = sendFluidToSide(serverLevel, targetPos, null, limit);
        if (transferred > 0) successfulTargetSides.remove(targetPos);
        return transferred;
    }

    private int sendFluidToSide(ServerLevel serverLevel, BlockPos targetPos, Direction side, int limit) {
        IFluidHandler targetHandler = serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side);
        if (targetHandler == null) return 0;

        FluidStack toSend = fluidTank.drain(limit, IFluidHandler.FluidAction.SIMULATE);
        if (toSend.isEmpty()) return 0;

        int fillable = targetHandler.fill(toSend, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return 0;

        FluidStack drained = fluidTank.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return 0;

        int filled = targetHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            return 0;
        }
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            fluidTank.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        setChanged();
        return filled;
    }

    private void refreshTargetsIfNeeded(ServerLevel serverLevel) {
        AABB area = getAABB(getBlockPos());
        long gameTime = serverLevel.getGameTime();
        if (sameArea(cachedTargetArea, area) && gameTime < nextTargetRefreshTick) return;

        List<BlockPos> targets = new ArrayList<>();
        int minX = Mth.floor(area.minX);
        int minY = Mth.floor(area.minY);
        int minZ = Mth.floor(area.minZ);
        int maxX = Mth.ceil(area.maxX) - 1;
        int maxY = Mth.ceil(area.maxY) - 1;
        int maxZ = Mth.ceil(area.maxZ) - 1;
        for (BlockPos targetPos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (targetPos.equals(getBlockPos()) || !serverLevel.hasChunkAt(targetPos)) continue;
            if (hasFluidTarget(serverLevel, targetPos)) targets.add(targetPos.immutable());
        }

        cachedTargets = List.copyOf(targets);
        successfulTargetSides.keySet().retainAll(cachedTargets);
        cachedTargetArea = area;
        nextTargetRefreshTick = gameTime + TARGET_REFRESH_INTERVAL_TICKS;
        targetCursor = cachedTargets.isEmpty() ? 0 : Math.floorMod(targetCursor, cachedTargets.size());
    }

    private boolean hasFluidTarget(ServerLevel serverLevel, BlockPos targetPos) {
        Direction preferredSide = getSideFacingMachine(targetPos);
        if (preferredSide != null
                && serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, preferredSide) != null) {
            return true;
        }
        for (Direction side : Direction.values()) {
            if (side != preferredSide
                    && serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side) != null) {
                return true;
            }
        }
        return serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, null) != null;
    }

    private boolean sameArea(AABB first, AABB second) {
        return first != null
                && Double.compare(first.minX, second.minX) == 0
                && Double.compare(first.minY, second.minY) == 0
                && Double.compare(first.minZ, second.minZ) == 0
                && Double.compare(first.maxX, second.maxX) == 0
                && Double.compare(first.maxY, second.maxY) == 0
                && Double.compare(first.maxZ, second.maxZ) == 0;
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

    protected int getFluidToSend() {
        if (JDTEConfig.COMMON.fluidSenderUnlimitedTransfer.get()) {
            return fluidTank.getFluidAmount();
        }
        if (UpgradeHelper.hasCreativeUpgrade(this)
                || UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) {
            return JDTEConfig.COMMON.senderReceiverOverclockFluidTransferRate.get();
        }
        return JDTEConfig.COMMON.senderReceiverFluidTransferRate.get();
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

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY);
    }

    @Override
    public JDTEFluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public FluidContainerData getFluidContainerData() {
        return fluidContainerData;
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
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        loadAreaSettings(tag);
        areaAffectingData.area = null;
        cachedTargetArea = null;
        nextTargetRefreshTick = 0;
        OverclockDirectTransferHelper.invalidate(this);
    }
}
