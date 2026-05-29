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
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public abstract class TimeAcceleratorBE extends BaseMachineBE implements RedstoneControlledBE, AreaAffectingBE, FluidMachineBE, FilterableBE, TimeAcceleratorMachine {
    public static final int BASE_FLUID_CAPACITY = 1000;

    public final FluidContainerData fluidContainerData;
    public final JDTEFluidTank fluidTank;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
    public FilterData filterData = new FilterData();

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
        if (isActiveRedstone() && canRun()) {
            accelerateArea();
        }
    }

    protected void accelerateArea() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int multiplier = getEffectiveMultiplier();
        int fluidCost = getFluidCost(multiplier);
        int energyCost = getEnergyCost(multiplier);
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
            BlockEntity blockEntity = serverLevel.getBlockEntity(immutable);
            if (blockEntity instanceof TimeAcceleratorMachine || !MiscTools.isValidTickAccelBlock(serverLevel, blockState, blockEntity)) {
                continue;
            }
            if (!isBlockValidFilter(serverLevel, immutable, blockState)) {
                continue;
            }
            if (accelerateTarget(serverLevel, immutable, blockState, blockEntity, multiplier)) {
                accelerated = true;
            }
        }

        if (accelerated) {
            consumeResources(fluidCost, energyCost);
        }
    }

    protected boolean isBlockValidFilter(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        if (blockState.getBlock() instanceof LiquidBlock liquidBlock) {
            return isStackValidFilter(liquidBlock);
        }
        ItemStack blockItemStack = blockState.getCloneItemStack(new BlockHitResult(Vec3.ZERO, getDirectionValue(), blockPos, false), serverLevel, blockPos, getFakePlayer(serverLevel));
        return isStackValidFilter(blockItemStack);
    }

    @SuppressWarnings("unchecked")
    protected boolean accelerateTarget(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, int multiplier) {
        if (blockEntity != null) {
            BlockEntityTicker<BlockEntity> ticker = blockEntity.getBlockState().getTicker(serverLevel, (BlockEntityType<BlockEntity>) blockEntity.getType());
            if (ticker == null) {
                return false;
            }
            for (int i = 0; i < multiplier; i++) {
                ticker.tick(serverLevel, blockPos, blockEntity.getBlockState(), blockEntity);
            }
            spawnAccelerationEffect(serverLevel, blockPos, multiplier);
            return true;
        }

        if (!blockState.isRandomlyTicking()) {
            return false;
        }
        for (int i = 0; i < multiplier; i++) {
            blockState.randomTick(serverLevel, blockPos, serverLevel.random);
        }
        spawnAccelerationEffect(serverLevel, blockPos, multiplier);
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
        return fluidTank.drain(fluidCost, IFluidHandler.FluidAction.SIMULATE).getAmount() == fluidCost;
    }

    protected void consumeResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return;
        }
        fluidTank.drain(fluidCost, IFluidHandler.FluidAction.EXECUTE);
        setChanged();
    }

    protected int getFluidCost(int multiplier) {
        int cost = Math.max(1, (int) Math.ceil(multiplier * Config.TIMEWAND_FLUID_COST.get()));
        return UpgradeHelper.hasOverclock(this) ? cost * 2 : cost;
    }

    protected int getEnergyCost(int multiplier) {
        return 0;
    }

    public abstract int getEffectiveMultiplier();

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY);
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
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
    }
}
