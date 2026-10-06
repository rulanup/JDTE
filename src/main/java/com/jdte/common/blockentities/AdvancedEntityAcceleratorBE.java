package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.manager.EntityAccelerationManager;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.SelectiveUpgradeMachine;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Advanced Entity Accelerator: accelerates living entities inside its configured area
 * using Time Fluid and FE energy.
 * Suppresses entity AI, navigation, and excessive travel physics during accelerated sub-ticks.
 */
public class AdvancedEntityAcceleratorBE extends BaseMachineBE implements AreaAffectingBE,
        PoweredMachineBE, FluidMachineBE, FilterableBE, RedstoneControlledBE,
        SelectiveUpgradeMachine, BaseFilterMachine, TimeAcceleratorMachine {

    public static final Set<UpgradeType> ALLOWED_UPGRADES = Set.of(
            UpgradeType.CAPACITY,
            UpgradeType.ULTIMATE_CAPACITY,
            UpgradeType.FLUID,
            UpgradeType.RANGE,
            UpgradeType.FILTER,
            UpgradeType.OVERCLOCK,
            UpgradeType.ULTIMATE_OVERCLOCK,
            UpgradeType.UNDERCLOCK,
            UpgradeType.CREATIVE,
            UpgradeType.AE_CRAFTING_READ
    );

    public final FluidContainerData fluidContainerData;
    public final JDTEFluidTank fluidTank;
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    public FilterData filterData = new FilterData();

    private int multiplier;
    private double pendingFluidCost;

    private final Set<EntityType<?>> cachedFilterTypes = new HashSet<>();
    private boolean cachedFilterAllowlist = false;
    private int filterFingerprint = -1;

    public AdvancedEntityAcceleratorBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.ADVANCED_ENTITY_ACCELERATOR.get(), pos, state);
    }

    protected AdvancedEntityAcceleratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tickSpeed = 1;
        this.areaAffectingData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING));
        this.fluidTank = new JDTEFluidTank(getMaxMB(), fluidStack -> fluidStack.getFluid() instanceof TimeFluid);
        this.fluidContainerData = new FluidContainerData(this);
        this.energyStorage = new MachineEnergyStorage(getMaxEnergy());
        this.poweredMachineData = new PoweredMachineContainerData(this);
        this.multiplier = JDTEConfig.COMMON.entityAccelerator.advancedDefaultMultiplier.get();
    }

    @Override
    public boolean canRun() {
        return super.canRun()
                && (UpgradeHelper.hasCreativeUpgrade(this)
                || (!fluidTank.getFluid().isEmpty() && energyStorage.getEnergyStored() > 0));
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun() && UpgradeHelper.mayRunWithUpgrades(this)) {
            accelerateEntities();
        }
    }

    protected void accelerateEntities() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int effectiveMultiplier = getEffectiveMultiplier();
        int extraTicks = effectiveMultiplier - 1;
        if (extraTicks <= 0) {
            return;
        }

        AABB area = getAABB(getBlockPos());
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::isValidTargetEntity);
        if (targets.isEmpty()) {
            return;
        }

        int maxEntities = getMaxEntitiesPerTick();
        if (targets.size() > maxEntities) {
            targets = targets.subList(0, maxEntities);
        }

        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        int totalWorkTicks = extraTicks * targets.size();

        if (!creative) {
            double timeWandFluidCost = Config.TIMEWAND_FLUID_COST.get();
            double configMultiplier = JDTEConfig.COMMON.entityAccelerator.fluidCostMultiplier.get();
            double tierMultiplier = getTierFluidCostMultiplier();
            double neededFluidDouble = TimeAcceleratorCostMath.fluidCost(totalWorkTicks, timeWandFluidCost, configMultiplier, tierMultiplier);
            TimeAcceleratorCostMath.Settlement settlement = TimeAcceleratorCostMath.settleFluid(pendingFluidCost, neededFluidDouble);
            int fluidDrain = settlement.drainMb();
            int energyCost = TimeAcceleratorCostMath.energyCost(totalWorkTicks, Config.TIMEWAND_RF_COST.get());

            if (fluidTank.getFluidAmount() < fluidDrain || energyStorage.getEnergyStored() < energyCost) {
                return;
            }

            if (fluidDrain > 0) {
                fluidTank.drain(fluidDrain, IFluidHandler.FluidAction.EXECUTE);
            }
            pendingFluidCost = settlement.remainingCost();

            if (energyCost > 0) {
                energyStorage.extractEnergy(energyCost, false);
            }
        }

        for (LivingEntity entity : targets) {
            EntityAccelerationManager.runAcceleratedTicks(entity, extraTicks);

            if (serverLevel.random.nextFloat() < 0.25F) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                        2, entity.getBbWidth() * 0.3D, entity.getBbHeight() * 0.25D, entity.getBbWidth() * 0.3D, 0.05D);
            }
        }
    }

    public boolean isValidTargetEntity(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            return false;
        }
        if (entity instanceof Player) {
            return false;
        }
        refreshFilterCache();
        if (cachedFilterTypes.isEmpty()) {
            return true;
        }
        boolean matched = cachedFilterTypes.contains(entity.getType());
        return cachedFilterAllowlist == matched;
    }

    private void refreshFilterCache() {
        FilterBasicHandler handler = getFilterHandler();
        if (handler == null) return;
        int fingerprint = filterData.allowlist ? 1 : 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            fingerprint = 31 * fingerprint + ItemStack.hashItemAndComponents(stack);
        }
        if (fingerprint == filterFingerprint && cachedFilterAllowlist == filterData.allowlist) {
            return;
        }
        filterFingerprint = fingerprint;
        cachedFilterAllowlist = filterData.allowlist;
        cachedFilterTypes.clear();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.getItem() instanceof SpawnEggItem egg) {
                cachedFilterTypes.add(egg.getType(stack));
            }
        }
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = Math.max(1, Math.min(multiplier, getMaxAdjustableMultiplier()));
        markDirtyClient();
    }

    public int getEffectiveMultiplier() {
        return (UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this))
                ? getOverclockMultiplier()
                : multiplier;
    }

    protected int getMaxAdjustableMultiplier() {
        return JDTEConfig.COMMON.entityAccelerator.advancedMaxMultiplier.get();
    }

    protected int getOverclockMultiplier() {
        return JDTEConfig.COMMON.entityAccelerator.advancedOverclockMultiplier.get();
    }

    protected double getTierFluidCostMultiplier() {
        return JDTEConfig.COMMON.entityAccelerator.advancedTierFluidCostMultiplier.get();
    }

    protected int getMaxEntitiesPerTick() {
        return JDTEConfig.COMMON.entityAccelerator.advancedMaxEntitiesPerTick.get();
    }

    @Override
    public boolean isUpgradeAllowed(UpgradeType type) {
        return ALLOWED_UPGRADES.contains(type);
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.entityAccelerator.advancedEnergyCapacity.get());
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.entityAccelerator.baseFluidCapacity.get());
    }

    @Override
    public int getStandardEnergyCost() {
        return TimeAcceleratorCostMath.energyCost(getEffectiveMultiplier() - 1, Config.TIMEWAND_RF_COST.get());
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
    public void setFilterSettings(FilterData settings) {
        FilterableBE.super.setFilterSettings(settings);
        filterFingerprint = -1;
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public ContainerData getContainerData() {
        return poweredMachineData;
    }

    @Override
    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
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
    public boolean isDefaultSettings() {
        return super.isDefaultSettings()
                && energyStorage.getEnergyStored() == 0
                && fluidTank.getFluid().isEmpty()
                && multiplier == JDTEConfig.COMMON.entityAccelerator.advancedDefaultMultiplier.get();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        tag.putDouble("pendingFluidCost", pendingFluidCost);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("multiplier", multiplier);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        filterFingerprint = -1;
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        if (tag.contains("pendingFluidCost")) {
            pendingFluidCost = tag.getDouble("pendingFluidCost");
        }
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        if (tag.contains("multiplier")) {
            setMultiplier(tag.getInt("multiplier"));
        }
    }
}
