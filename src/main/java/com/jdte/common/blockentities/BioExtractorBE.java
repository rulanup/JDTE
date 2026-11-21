package com.jdte.common.blockentities;

import com.jdte.common.recipes.MachineRecipeHelper;
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
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.recipes.BioExtractorRecipe;
import com.jdte.common.recipes.BioFactoryOutput;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public abstract class BioExtractorBE extends BaseMachineBE implements AreaAffectingBE, PoweredMachineBE,
        FluidMachineBE, FilterableBE, RedstoneControlledBE, CoalescedAcceleratedMachine {

    public static final String COOLDOWN_KEY = "jdte:bio_cooldown";
    public static final int OVERCLOCK_SCAN_INTERVAL = 5;

    protected final int outputSlotCount;
    protected final int baseEnergyCapacity;
    protected final int baseFluidCapacity;
    protected final float baseRadius;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public final FilterData filterData = new FilterData();
    public final RedstoneControlData redstoneControlData = new RedstoneControlData();
    public final AreaAffectingData areaAffectingData;

    private final MachineInventoryHandler itemHandler;
    private final ResourceHandler<ItemResource> automationItemHandler;
    protected int tickCounter = 0;

    protected BioExtractorBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
                             int outputSlotCount, int baseEnergyCapacity, int baseFluidCapacity, float baseRadius) {
        super(type, pos, state);
        this.outputSlotCount = outputSlotCount;
        this.baseEnergyCapacity = baseEnergyCapacity;
        this.baseFluidCapacity = baseFluidCapacity;
        this.baseRadius = baseRadius;
        MACHINE_SLOTS = outputSlotCount;

        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        areaAffectingData.xRadius = baseRadius;
        areaAffectingData.yRadius = baseRadius;
        areaAffectingData.zRadius = baseRadius;

        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);

        fluidTank = new JDTEFluidTank(getMaxMB(), f -> true);
        fluidContainerData = new FluidContainerData(this);

        itemHandler = new MachineInventoryHandler(outputSlotCount) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                setChanged();
            }
        };

        automationItemHandler = new MachineAutomationHandler<>(itemHandler, slot -> false, slot -> true);
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    public ResourceHandler<ItemResource> getAutomationItemHandler() {
        return automationItemHandler;
    }

    public int getOutputSlotCount() {
        return outputSlotCount;
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, baseEnergyCapacity);
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, baseFluidCapacity);
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
    public AreaAffectingData getAreaAffectingData() {
        return areaAffectingData;
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
    public FilterBasicHandler getFilterHandler() {
        return getData(JDTRegistration.HANDLER_BASIC_FILTER);
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
    public boolean canRun() {
        return true;
    }

    protected abstract int getMaxEntitiesPerTick();

    protected int getExtractInterval() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return OVERCLOCK_SCAN_INTERVAL;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) return OVERCLOCK_SCAN_INTERVAL;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.UNDERCLOCK) > 0) return 40;
        return 20;
    }

    public int getEffectiveEnergyCost(int baseCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        return UpgradeHelper.adjustEnergyCost(this, baseCost);
    }

    @Override
    public int getStandardEnergyCost() {
        return getEffectiveEnergyCost(1000);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        syncAreaRadius();
        if (isActiveRedstone() && canRun()) {
            extractBio();
        }
    }

    protected void syncAreaRadius() {
        double maxRadius = UpgradeHelper.getMaxAreaRadius(this);
        areaAffectingData.xRadius = Math.min(areaAffectingData.xRadius, maxRadius);
        areaAffectingData.yRadius = Math.min(areaAffectingData.yRadius, maxRadius);
        areaAffectingData.zRadius = Math.min(areaAffectingData.zRadius, maxRadius);
    }

    protected int accumulatedAcceleratedTicks = 0;

    @Override
    public void accumulateAcceleratedTicks(int ticks) {
        if (ticks > 0) {
            accumulatedAcceleratedTicks = (int) Math.min(Integer.MAX_VALUE, (long) accumulatedAcceleratedTicks + ticks);
        }
    }

    @Override
    public void flushAcceleratedTicks() {
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        if (ticks <= 0 || !isActiveRedstone() || !canRun()) {
            return;
        }
        processAcceleratedBio(ticks);
    }

    protected void processAcceleratedBio(int ticks) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int interval = Math.max(1, getExtractInterval());
        long totalProgress = (long) tickCounter + ticks;
        long cycles = totalProgress / interval;
        tickCounter = (int) (totalProgress % interval);
        if (cycles <= 0) {
            setChanged();
            return;
        }

        int maxCycles = (int) Math.min(cycles, 64);
        for (int i = 0; i < maxCycles; i++) {
            int processed = executeOneExtractCycle(serverLevel);
            if (processed == 0) {
                break;
            }
        }
        setChanged();
    }

    protected void extractBio() {
        tickCounter++;
        if (tickCounter < getExtractInterval()) {
            return;
        }
        tickCounter = 0;

        if (!(level instanceof ServerLevel serverLevel)) return;
        executeOneExtractCycle(serverLevel);
    }

    protected int executeOneExtractCycle(ServerLevel serverLevel) {
        AABB area = getAABB(getBlockPos());
        boolean hasFilterUpgrade = UpgradeHelper.countUpgrades(this, UpgradeType.FILTER) > 0;
        int maxEntities = getMaxEntitiesPerTick();
        int processed = 0;

        for (Entity entity : serverLevel.getEntitiesOfClass(Entity.class, area, e -> isValidTarget(e, hasFilterUpgrade))) {
            if (processed >= maxEntities) break;
            if (!(entity instanceof LivingEntity living)) continue;

            long cooldownEnd = living.getPersistentData().getLongOr(COOLDOWN_KEY, 0L);
            if (serverLevel.getGameTime() < cooldownEnd) continue;

            if (living instanceof Sheep sheep && sheep.isSheared()) {
                continue;
            }

            BioExtractorRecipe recipe = findRecipe(living);
            if (recipe == null) continue;

            int energyCost = getEffectiveEnergyCost(recipe.energy());
            if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
                break;
            }

            try (Transaction transaction = Transaction.openRoot()) {
                if (recipe.outputFluid().isPresent()) {
                    FluidStack fluid = recipe.outputFluid().get();
                    if (!fluid.isEmpty() && fluidTank.insert(FluidResource.of(fluid), fluid.getAmount(),
                            transaction) != fluid.getAmount()) continue;
                }
                if (!itemHandler.insertBatch(0, outputSlotCount, rollOutputs(recipe, living, serverLevel),
                        transaction)) continue;
                transaction.commit();
            }

            if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this)) {
                extractEnergy(energyCost, false);
            }

            if (recipe.damage() > 0.0f) {
                living.hurt(serverLevel.damageSources().generic(), recipe.damage());
            }

            if (living instanceof Sheep sheep) {
                sheep.setSheared(true);
            }

            living.getPersistentData().putLong(COOLDOWN_KEY, serverLevel.getGameTime() + recipe.cooldown());

            playExtractionSound(serverLevel, living, recipe);
            processed++;
        }

        if (processed > 0) {
            setChanged();
        }
        return processed;
    }

    protected boolean isValidTarget(Entity entity, boolean hasFilterUpgrade) {
        if (entity instanceof Player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity.isSpectator()) return false;
        if (!entity.isAlive()) return false;

        if (hasFilterUpgrade) {
            return isEntityValidFilter(entity, level);
        }
        return true;
    }

    public BioExtractorRecipe findRecipe(LivingEntity entity) {
        if (level == null) return null;
        for (RecipeHolder<BioExtractorRecipe> holder : MachineRecipeHelper.recipes(level).byType(JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get())) {
            if (holder.value().matches(entity.getType())) {
                return holder.value();
            }
        }
        return null;
    }

    private List<ItemStack> rollOutputs(BioExtractorRecipe recipe, LivingEntity living, ServerLevel serverLevel) {
        List<ItemStack> results = new ArrayList<>();
        for (BioFactoryOutput output : recipe.outputItems()) {
            ItemStack stack = output.roll(serverLevel.getRandom(), 1.0D);
            if (!stack.isEmpty()) {
                if (living instanceof Sheep sheep && stack.is(net.minecraft.world.item.Items.WHITE_WOOL)) {
                    net.minecraft.world.item.Item woolItem = switch (sheep.getColor()) {
                        case WHITE -> net.minecraft.world.item.Items.WHITE_WOOL;
                        case ORANGE -> net.minecraft.world.item.Items.ORANGE_WOOL;
                        case MAGENTA -> net.minecraft.world.item.Items.MAGENTA_WOOL;
                        case LIGHT_BLUE -> net.minecraft.world.item.Items.LIGHT_BLUE_WOOL;
                        case YELLOW -> net.minecraft.world.item.Items.YELLOW_WOOL;
                        case LIME -> net.minecraft.world.item.Items.LIME_WOOL;
                        case PINK -> net.minecraft.world.item.Items.PINK_WOOL;
                        case GRAY -> net.minecraft.world.item.Items.GRAY_WOOL;
                        case LIGHT_GRAY -> net.minecraft.world.item.Items.LIGHT_GRAY_WOOL;
                        case CYAN -> net.minecraft.world.item.Items.CYAN_WOOL;
                        case PURPLE -> net.minecraft.world.item.Items.PURPLE_WOOL;
                        case BLUE -> net.minecraft.world.item.Items.BLUE_WOOL;
                        case BROWN -> net.minecraft.world.item.Items.BROWN_WOOL;
                        case GREEN -> net.minecraft.world.item.Items.GREEN_WOOL;
                        case RED -> net.minecraft.world.item.Items.RED_WOOL;
                        case BLACK -> net.minecraft.world.item.Items.BLACK_WOOL;
                    };
                    stack = new ItemStack(woolItem, stack.getCount());
                }
                results.add(stack);
            }
        }
        return results;
    }

    private void playExtractionSound(ServerLevel level, LivingEntity entity, BioExtractorRecipe recipe) {
        if (recipe.outputFluid().isPresent() && recipe.outputFluid().get().getFluid().toString().contains("milk")) {
            level.playSound(null, entity.blockPosition(), SoundEvents.COW_MILK, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (entity instanceof Sheep) {
            level.playSound(null, entity.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, entity.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("itemHandler", itemHandler);
        tag.putChild("fluidTank", fluidTank);
        tag.putInt("energy", energyStorage.getEnergyStored());
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("itemHandler")) itemHandler.deserialize(tag.childOrEmpty("itemHandler"));
        if (tag.keySet().contains("fluidTank")) fluidTank.deserialize(tag.childOrEmpty("fluidTank"));
        if (tag.keySet().contains("energy")) energyStorage.setEnergy(tag.getIntOr("energy", 0));
        loadAreaSettings(tag);
    }
}
