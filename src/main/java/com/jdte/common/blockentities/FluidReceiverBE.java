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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public abstract class FluidReceiverBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, FluidMachineBE, AreaAffectingBE, BaseFilterMachine {
    public static final int BASE_FLUID_CAPACITY = 8000; // 8 buckets

    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    public final JDTEFluidTank fluidTank;

    public final FluidContainerData fluidContainerData;
    private int transferRetryTicks;
    private int transferFailureBackoff;
    private boolean transferMoved;

    protected FluidReceiverBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
            transferMoved = false;
            receiveFluid();
            updateTransferBackoff(transferMoved);
        }
    }

    protected void receiveFluid() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int fluidToReceive = getFluidToReceive();

        // 在范围内寻找源容器
        AABB area = getAABB(getBlockPos());
        for (BlockPos sourcePos : BlockPos.betweenClosed(
                (int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
            // 跳过自身位置
            if (sourcePos.equals(getBlockPos())) continue;

            Direction preferredSide = getSideFacingMachine(sourcePos);
            if (preferredSide != null && receiveFluidFromSide(serverLevel, sourcePos, preferredSide, fluidToReceive)) {
                return;
            }
            for (Direction side : Direction.values()) {
                if (side != preferredSide && receiveFluidFromSide(serverLevel, sourcePos, side, fluidToReceive)) {
                    return;
                }
            }
            if (receiveFluidFromSide(serverLevel, sourcePos, null, fluidToReceive)) {
                return;
            }
        }
    }

    private boolean receiveFluidFromSide(ServerLevel serverLevel, BlockPos sourcePos, Direction side, int fluidToReceive) {
        IFluidHandler sourceHandler = serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, sourcePos, side);
        if (sourceHandler == null) return false;

        FluidStack extracted = sourceHandler.drain(fluidToReceive, IFluidHandler.FluidAction.SIMULATE);
        if (extracted.isEmpty()) return false;

        int fillable = fluidTank.fill(extracted, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return false;

        FluidStack toDrain = extracted.copy();
        toDrain.setAmount(fillable);
        FluidStack drained = sourceHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return false;

        int filled = fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            sourceHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            return false;
        }
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            sourceHandler.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        setChanged();
        transferMoved = true;
        return true;
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

    protected int getFluidToReceive() {
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
        OverclockDirectTransferHelper.invalidate(this);
    }
}
