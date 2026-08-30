package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public abstract class TimeAcceleratorBE extends BaseMachineBE implements RedstoneControlledBE, AreaAffectingBE, FluidMachineBE, FilterableBE, TimeAcceleratorMachine, BaseFilterMachine {
    public final FluidContainerData fluidContainerData;
    public final JDTEFluidTank fluidTank;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
    public FilterData filterData = new FilterData();
    private double pendingFluidCost;

    protected TimeAcceleratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tickSpeed = 1;
        fluidTank = new JDTEFluidTank(getMaxMB(), fluidStack -> fluidStack.getFluid() instanceof TimeFluid);
        fluidContainerData = new FluidContainerData(this);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun() && UpgradeHelper.mayRunWithUpgrades(this)) {
            handleAccelerationTick();
        } else {
            ExtendedTimeAccelerationManager.deactivate(this);
        }
    }

    protected void handleAccelerationTick() {
        ExtendedTimeAccelerationManager.submit(this);
    }

    protected void accelerateArea() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int effectiveMultiplier = getEffectiveMultiplier();
        int workTicks = getAccelerationWorkTicks(effectiveMultiplier);
        int fluidCost = getFluidDrainAmount(workTicks);
        int energyCost = getEnergyCost(workTicks);
        if (!hasResources(fluidCost, energyCost)) {
            return;
        }

        boolean accelerated = false;
        AABB area = getAABB(getBlockPos());
        for (BlockPos blockPos : BlockPos.betweenClosed(
                (int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
            BlockPos immutable = blockPos.immutable();
            BlockState blockState = serverLevel.getBlockState(immutable);

            // Fast air block check - skip most blocks
            if (blockState.isAir()) {
                continue;
            }

            if (!isBlockValidFilter(serverLevel, immutable, blockState)) {
                continue;
            }

            if (accelerateTarget(serverLevel, immutable, workTicks, effectiveMultiplier)) {
                accelerated = true;
            }
        }

        if (accelerated) {
            consumeResources(workTicks, energyCost);
        }
    }

    protected boolean isBlockValidFilter(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        if (blockState.getBlock() instanceof LiquidBlock liquidBlock) {
            return isStackValidFilter(liquidBlock);
        }
        ItemStack blockItemStack = blockState.getCloneItemStack(new BlockHitResult(Vec3.ZERO, getDirectionValue(), blockPos, false), serverLevel, blockPos, getFakePlayer(serverLevel));
        return isStackValidFilter(blockItemStack);
    }

    protected boolean accelerateTarget(ServerLevel serverLevel, BlockPos blockPos, int workTicks, int displayMultiplier) {
        if (workTicks <= 0) {
            return false;
        }

        CoalescedAcceleratedMachine coalescedTarget = null;
        int remainingWork = workTicks;
        try {
            while (remainingWork > 0) {
                UltimateTimeWandTargetRuntime.Result result =
                        UltimateTimeWandTargetRuntime.executeOrdinary(
                                serverLevel, blockPos, remainingWork, remainingWork);
                if (result.coalescedTarget() != null) {
                    coalescedTarget = result.coalescedTarget();
                }
                if (!result.valid() || result.executed() <= 0) {
                    return false;
                }
                remainingWork -= result.executed();
            }
        } finally {
            if (coalescedTarget != null) {
                coalescedTarget.flushAcceleratedTicks();
            }
        }

        spawnAccelerationEffect(serverLevel, blockPos, displayMultiplier);
        return true;
    }

    private void spawnAccelerationEffect(ServerLevel serverLevel, BlockPos blockPos, int multiplier) {
        boolean hasEffect = serverLevel.getEntitiesOfClass(
                com.jdte.common.entities.TimeAcceleratorEffectEntity.class,
                new net.minecraft.world.phys.AABB(blockPos),
                e -> true
        ).isEmpty() == false;

        if (!hasEffect) {
            com.jdte.common.entities.TimeAcceleratorEffectEntity effect = new com.jdte.common.entities.TimeAcceleratorEffectEntity(serverLevel, blockPos, multiplier);
            serverLevel.addFreshEntity(effect);
        }
    }

    protected boolean hasResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return true;
        }
        if (fluidCost <= 0 && getFluidCostPerTick(getAccelerationWorkTicks(getEffectiveMultiplier())) > 0.0D) {
            return !fluidTank.getFluid().isEmpty();
        }
        return fluidTank.drain(fluidCost, IFluidHandler.FluidAction.SIMULATE).getAmount() == fluidCost;
    }

    protected void consumeResources(int workTicks, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return;
        }
        TimeAcceleratorCostMath.Settlement settlement =
                TimeAcceleratorCostMath.settleFluid(pendingFluidCost, getFluidCostPerTick(workTicks));
        if (settlement.drainMb() > 0) {
            fluidTank.drain(settlement.drainMb(), IFluidHandler.FluidAction.EXECUTE);
        }
        pendingFluidCost = settlement.remainingCost();
        setChanged();
    }

    protected int getAccelerationWorkTicks(int effectiveMultiplier) {
        return TimeAcceleratorTiming.workTicks(
                effectiveMultiplier,
                JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
    }

    protected int getFluidDrainAmount(int workTicks) {
        return TimeAcceleratorCostMath.settleFluid(pendingFluidCost, getFluidCostPerTick(workTicks)).drainMb();
    }

    protected double getFluidCostPerTick(int workTicks) {
        return TimeAcceleratorCostMath.fluidCost(
                workTicks,
                Config.TIMEWAND_FLUID_COST.get(),
                JDTEConfig.COMMON.timeAcceleratorFluidCostMultiplier.get(),
                getTierFluidCostMultiplier());
    }

    protected double getTierFluidCostMultiplier() {
        return 1.0D;
    }

    protected int getEnergyCost(int workTicks) {
        return 0;
    }

    public abstract int getEffectiveMultiplier();

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.timeAcceleratorBaseFluidCapacity.get());
    }

    @Override
    public ContainerData getFluidContainerData() {
        return fluidContainerData;
    }

    @Override
    public JDTEFluidTank getFluidTank() {
        return fluidTank;
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
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
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
    public boolean isDefaultSettings() {
        return super.isDefaultSettings() && fluidTank.getFluid().isEmpty();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        tag.putDouble("pendingFluidCost", pendingFluidCost);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        if (tag.contains("pendingFluidCost")) {
            pendingFluidCost = tag.getDouble("pendingFluidCost");
        }
    }
}
