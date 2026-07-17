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
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class LifeExtractorBE extends BaseMachineBE implements FilterableBE, RedstoneControlledBE, AreaAffectingBE, FluidMachineBE {
    public static final int MODE_HOSTILE = 0;
    public static final int MODE_FRIENDLY = 1;
    public static final int MODE_ALL = 2;
    public static final int BASE_FLUID_CAPACITY = 16000;
    public static final int BASE_ENERGY_COST = 300;
    public static final float BASE_RADIUS = 5.0f;

    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public final ContainerData lifeExtractorData;
    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected int mode = MODE_HOSTILE;
    protected int tickCounter = 0;
    protected double pendingLifeFluid = 0.0D;
    private final ItemStackHandler emptyHandler = new ItemStackHandler(0);

    protected LifeExtractorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = 0;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        areaAffectingData.xRadius = BASE_RADIUS;
        areaAffectingData.yRadius = BASE_RADIUS;
        areaAffectingData.zRadius = BASE_RADIUS;
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> f.is(JDTEFluids.LIFE_FLUID_SOURCE.get()));
        fluidContainerData = new FluidContainerData(this);
        lifeExtractorData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> mode;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    mode = value;
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    @Override
    public ItemStackHandler getMachineHandler() {
        return emptyHandler;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        syncAreaRadius();
        if (isActiveRedstone() && canRun()) {
            extractLife();
        }
    }

    @Override
    public boolean canRun() {
        return true;
    }

    protected abstract int getExtractInterval();

    protected abstract int getMaxEntitiesPerTick();

    protected abstract double getFluidLossMultiplier();

    protected abstract double getFluidBonusMultiplier();

    public abstract int getEffectiveEnergyCost();

    public abstract boolean hasEnoughPower(int energyCost);

    public abstract int extractEnergy(int energy, boolean simulate);

    protected void syncAreaRadius() {
        double maxRadius = UpgradeHelper.getMaxAreaRadius(this);
        areaAffectingData.xRadius = Math.min(areaAffectingData.xRadius, maxRadius);
        areaAffectingData.yRadius = Math.min(areaAffectingData.yRadius, maxRadius);
        areaAffectingData.zRadius = Math.min(areaAffectingData.zRadius, maxRadius);
    }

    protected void extractLife() {
        tickCounter++;
        if (tickCounter < getExtractInterval()) {
            return;
        }
        tickCounter = 0;

        if (!(level instanceof ServerLevel serverLevel)) return;

        int energyCost = getEffectiveEnergyCost();
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            return;
        }

        flushPendingLifeFluid();
        if (pendingLifeFluid >= 1.0D) {
            return;
        }

        AABB area = getAABB(getBlockPos());
        boolean hasFilterUpgrade = UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.FILTER) > 0;

        for (Entity crystal : serverLevel.getEntitiesOfClass(Entity.class, area, e -> e.getClass().getName().contains("GuardianCrystal"))) {
            crystal.discard();
        }

        int processed = 0;
        int maxEntities = getMaxEntitiesPerTick();
        for (Entity entity : serverLevel.getEntitiesOfClass(Entity.class, area, e -> isValidTarget(e, hasFilterUpgrade))) {
            if (processed >= maxEntities) break;
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            float currentHealth = livingEntity.getHealth();
            if (currentHealth <= 0) continue;

            double fluidProduced = calculateLifeFluid(currentHealth) * getFluidBonusMultiplier();
            double loss = getFluidLossMultiplier();
            if (loss > 0) {
                fluidProduced *= 1.0D - loss;
            }

            if (fluidProduced > 0) {
                pendingLifeFluid += fluidProduced;
                flushPendingLifeFluid();
            }

            entity.discard();
            processed++;

            if (pendingLifeFluid >= 1.0D) {
                break;
            }
        }

        if (processed > 0 && !UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(energyCost, false);
        }

        if (processed > 0) {
            setChanged();
        }
    }

    public static double calculateLifeFluid(double health) {
        if (!(health > 0.0D) || !Double.isFinite(health)) return 0.0D;
        double fluidPerHealth = JDTEConfig.COMMON.lifeExtractorFluidPerHealth.get();
        if (health <= 100.0D) return health * fluidPerHealth;

        double decay = 1.0D - JDTEConfig.COMMON.lifeExtractorHighHealthLossPercent.get() / 100.0D;
        if (decay >= 1.0D) return health * fluidPerHealth;
        if (decay <= 0.0D) return 100.0D * fluidPerHealth;

        double extraHealth = health - 100.0D;
        long fullBands = (long) Math.floor(extraHealth / 100.0D);
        double remainder = extraHealth - fullBands * 100.0D;
        double fullBandHealth = 100.0D * decay * (1.0D - Math.pow(decay, fullBands)) / (1.0D - decay);
        double remainderHealth = remainder * Math.pow(decay, fullBands + 1.0D);
        return (100.0D + fullBandHealth + remainderHealth) * fluidPerHealth;
    }

    private void flushPendingLifeFluid() {
        long wholeAmount = (long) Math.floor(pendingLifeFluid + 1.0E-9D);
        if (wholeAmount <= 0) {
            return;
        }

        int requested = (int) Math.min(wholeAmount, Integer.MAX_VALUE);
        int accepted = fluidTank.fill(
                new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), requested),
                IFluidHandler.FluidAction.EXECUTE);
        if (accepted > 0) {
            pendingLifeFluid = Math.max(0.0D, pendingLifeFluid - accepted);
        }
    }

    protected boolean isValidTarget(Entity entity, boolean hasFilterUpgrade) {
        if (entity instanceof Player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity.isSpectator()) return false;

        if (hasFilterUpgrade) {
            return isEntityValidFilter(entity, level);
        }

        return switch (mode) {
            case MODE_HOSTILE -> isHostileTarget(entity);
            case MODE_FRIENDLY -> !isHostileTarget(entity);
            case MODE_ALL -> true;
            default -> false;
        };
    }

    private boolean isHostileTarget(Entity entity) {
        EntityType<?> type = entity.getType();
        return entity instanceof Enemy || type.getCategory() == MobCategory.MONSTER;
    }

    protected int getAreaEnergyScale() {
        return Math.max(1, 1
                + (int) Math.ceil(areaAffectingData.xRadius)
                + (int) Math.ceil(areaAffectingData.yRadius)
                + (int) Math.ceil(areaAffectingData.zRadius));
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = Math.clamp(mode, MODE_HOSTILE, MODE_ALL);
        setChanged();
    }

    public ContainerData getLifeExtractorData() {
        return lifeExtractorData;
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
        tag.putInt("mode", mode);
        tag.putInt("tickCounter", tickCounter);
        tag.putDouble("pendingLifeFluid", pendingLifeFluid);
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        if (tag.contains("mode")) {
            mode = tag.getInt("mode");
        }
        if (tag.contains("tickCounter")) {
            tickCounter = tag.getInt("tickCounter");
        }
        if (tag.contains("pendingLifeFluid")) {
            pendingLifeFluid = Math.max(0.0D, tag.getDouble("pendingLifeFluid"));
        }
        loadAreaSettings(tag);
    }
}
