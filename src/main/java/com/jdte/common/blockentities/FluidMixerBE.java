package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.recipes.FluidMixerRecipe;
import com.jdte.common.recipes.FluidMixerParallelLimit;
import com.jdte.common.recipes.RecipeCacheSignal;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

/**
 * 流体混合器基类。
 *
 * <p>两种流体输入罐（A/B）+ 可选物品催化剂槽 + 流体输出罐。
 * 消耗 FE 驱动混合过程，支持 JDTE 自有 datapack 配方和可选 Mekanism Chemical Infuser 配方。</p>
 */
public abstract class FluidMixerBE extends BaseMachineBE implements PoweredMachineBE, FluidMachineBE,
        RedstoneControlledBE, CoalescedAcceleratedMachine {
    public static final int CATALYST_SLOT = 0;
    public static final int TOTAL_SLOTS = 1;
    public static final int BASE_FLUID_CAPACITY = 8000;

    protected final MachineEnergyStorage energyStorage;
    protected final PoweredMachineContainerData poweredData;
    /** 流体输入罐 A（左侧）。 */
    public final JDTEFluidTank inputTankA;
    /** 流体输入罐 B（右侧）。 */
    public final JDTEFluidTank inputTankB;
    /** 流体输出罐。 */
    public final JDTEFluidTank outputTank;
    private final ResourceHandler<FluidResource> combinedFluidHandler;
    public final FluidContainerData fluidData;
    /** 额外 ContainerData：输入罐 B 和输出罐同步到客户端。 */
    public final ContainerData fluidMixerData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();

    protected final ItemStacksResourceHandler itemHandler;
    protected int progress = 0;
    private final int baseEnergyCapacity;

    /** 当前匹配的配方缓存。 */
    private FluidMixerRecipe cachedRecipe = null;
    private long lastRecipeCacheGen = -1L;

    protected FluidMixerBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
                           int baseEnergyCapacity) {
        super(type, pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        this.baseEnergyCapacity = baseEnergyCapacity;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredData = new PoweredMachineContainerData(this);
        inputTankA = new JDTEFluidTank(getMaxMB(), f -> true);
        inputTankB = new JDTEFluidTank(getMaxMB(), f -> true);
        // Internal production must be able to fill this tank. The combined capability
        // restricts external fills to inputTankA/inputTankB.
        outputTank = new JDTEFluidTank(getMaxMB());
        combinedFluidHandler = new MachineAutomationHandler<>(
                new CombinedResourceHandler<>(inputTankA, inputTankB, outputTank), slot -> slot < 2, slot -> slot == 2) {
            @Override public int insert(FluidResource resource, int amount, TransactionContext transaction) {
                for (int slot = 0; slot < 2; slot++) {
                    if (getResource(slot).equals(resource)) return insert(slot, resource, amount, transaction);
                }
                for (int slot = 0; slot < 2; slot++) {
                    if (getResource(slot).isEmpty()) return insert(slot, resource, amount, transaction);
                }
                return 0;
            }
        };
        fluidData = new FluidContainerData(this);
        fluidMixerData = new ContainerData() {
            private int clientProgress;
            private int clientMaxProgress;
            private int clientInputBFluidId;
            private int clientInputBAmountLow;
            private int clientInputBAmountHigh;
            private int clientOutputFluidId;
            private int clientOutputAmountLow;
            private int clientOutputAmountHigh;

            @Override
            public int get(int index) {
                if (level != null && !level.isClientSide()) {
                    return switch (index) {
                        case 0 -> progress;
                        case 1 -> cachedRecipe != null ? getEffectiveProcessTicks(cachedRecipe) : 0;
                        case 2 -> BuiltInRegistries.FLUID.getId(inputTankB.getFluid().getFluid());
                        case 3 -> inputTankB.getFluidAmount() & 0xFFFF;
                        case 4 -> inputTankB.getFluidAmount() >>> 16;
                        case 5 -> BuiltInRegistries.FLUID.getId(outputTank.getFluid().getFluid());
                        case 6 -> outputTank.getFluidAmount() & 0xFFFF;
                        case 7 -> outputTank.getFluidAmount() >>> 16;
                        default -> 0;
                    };
                }
                return switch (index) {
                    case 0 -> clientProgress;
                    case 1 -> clientMaxProgress;
                    case 2 -> clientInputBFluidId;
                    case 3 -> clientInputBAmountLow;
                    case 4 -> clientInputBAmountHigh;
                    case 5 -> clientOutputFluidId;
                    case 6 -> clientOutputAmountLow;
                    case 7 -> clientOutputAmountHigh;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> { progress = value; clientProgress = value; }
                    case 1 -> clientMaxProgress = value;
                    case 2 -> clientInputBFluidId = value;
                    case 3 -> clientInputBAmountLow = value;
                    case 4 -> clientInputBAmountHigh = value;
                    case 5 -> clientOutputFluidId = value;
                    case 6 -> clientOutputAmountLow = value;
                    case 7 -> clientOutputAmountHigh = value;
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
        itemHandler = new MachineInventoryHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                setChanged();
            }
        };
    }

    // ------ 接口实现 ------

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, baseEnergyCapacity);
    }

    @Override
    public ContainerData getContainerData() {
        return poweredData;
    }

    @Override
    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public int getStandardEnergyCost() {
        return cachedRecipe != null ? getEffectiveEnergyCost(cachedRecipe.energy()) : 0;
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY);
    }

    @Override
    public JDTEFluidTank getFluidTank() {
        // BaseMachineScreen 用于渲染默认流体条 — 映射到输入罐 A
        return inputTankA;
    }

    @Override
    public FluidContainerData getFluidContainerData() {
        return fluidData;
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
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    public ContainerData getFluidMixerData() {
        return fluidMixerData;
    }

    /**
     * 供自动化使用的组合流体处理器：将三个罐组合为一个视图。
     * 罐 0/1 为输入（可灌入）、罐 2 为输出（只抽出）。
     */
    public ResourceHandler<FluidResource> getCombinedFluidHandler() {
        return combinedFluidHandler;
    }

    // ------ 工作逻辑 ------

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun()) {
            processMixing();
        }
    }

    protected int getEffectiveEnergyCost(int baseCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        return UpgradeHelper.adjustEnergyCost(this, baseCost);
    }

    private void processMixing() {
        if (!(level instanceof ServerLevel)) return;

        FluidStack tankAFluid = inputTankA.getFluid();
        FluidStack tankBFluid = inputTankB.getFluid();
        ItemStack catalyst = itemHandler.getResource(CATALYST_SLOT).toStack(itemHandler.getAmountAsInt(CATALYST_SLOT));

        FluidMixerRecipe recipe = findRecipe(tankAFluid, tankBFluid, catalyst);
        if (recipe == null) {
            resetProgress();
            return;
        }

        // 检查流体数量
        if (!recipe.hasEnoughFluids(tankAFluid, tankBFluid)) {
            resetProgress();
            return;
        }

        // 检查输出空间
        FluidStack outputResult = recipe.output();
        if (!canOutputFluid(outputResult)) {
            resetProgress();
            return;
        }

        // 检查能量
        int energyCost = getEffectiveEnergyCost(recipe.energy());
        if (energyCost > 0 && !hasEnoughPower(energyCost)) {
            resetProgress();
            return;
        }

        this.cachedRecipe = recipe;
        int maxTicks = getEffectiveProcessTicks(recipe);
        progress++;
        if (progress < maxTicks) {
            setChanged();
            return;
        }

        // 完成阶段：计算并行混合批次数并一次性消耗产出
        int parallel = calculateMaxParallel(recipe, tankAFluid, tankBFluid, catalyst, energyCost);
        if (parallel <= 0) {
            return;
        }

        int totalDrainA = recipe.getDrainA(tankAFluid) * parallel;
        int totalDrainB = recipe.getDrainB(tankBFluid) * parallel;
        int totalEnergy = energyCost * parallel;
        int totalOutputAmount = outputResult.getAmount() * parallel;

        if (!storeOutput(outputResult.copyWithAmount(totalOutputAmount))) {
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            inputTankA.extractFluid(totalDrainA);
            inputTankB.extractFluid(totalDrainB);
            if (recipe.itemInput().isPresent()) {
                int itemDrain = recipe.itemInput().get().count() * parallel;
                catalyst.shrink(itemDrain);
                itemHandler.set(CATALYST_SLOT, ItemResource.of(catalyst), catalyst.getCount());
            }
            if (totalEnergy > 0) {
                extractEnergy(totalEnergy, false);
            }
        }

        progress = 0;
        setChanged();
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
        processAcceleratedMixing(ticks);
    }

    protected void processAcceleratedMixing(int ticks) {
        if (!(level instanceof ServerLevel)) return;
        FluidStack tankAFluid = inputTankA.getFluid();
        FluidStack tankBFluid = inputTankB.getFluid();
        ItemStack catalyst = itemHandler.getResource(CATALYST_SLOT).toStack(itemHandler.getAmountAsInt(CATALYST_SLOT));
        FluidMixerRecipe recipe = findRecipe(tankAFluid, tankBFluid, catalyst);
        if (recipe == null || !recipe.hasEnoughFluids(tankAFluid, tankBFluid)) {
            resetProgress();
            return;
        }
        FluidStack outputResult = recipe.output();
        if (!canOutputFluid(outputResult)) {
            return;
        }
        int energyCost = getEffectiveEnergyCost(recipe.energy());
        if (energyCost > 0 && !hasEnoughPower(energyCost)) {
            return;
        }
        this.cachedRecipe = recipe;
        int maxTicks = getEffectiveProcessTicks(recipe);
        long totalProgress = (long) progress + ticks;
        long batches = totalProgress / maxTicks;
        progress = (int) (totalProgress % maxTicks);
        if (batches <= 0) {
            setChanged();
            return;
        }

        int maxParallel = calculateMaxParallel(recipe, tankAFluid, tankBFluid, catalyst, energyCost);
        int parallel = (int) Math.min(batches, maxParallel);
        if (parallel <= 0) {
            return;
        }

        int totalDrainA = recipe.getDrainA(tankAFluid) * parallel;
        int totalDrainB = recipe.getDrainB(tankBFluid) * parallel;
        int totalEnergy = energyCost * parallel;
        int totalOutputAmount = outputResult.getAmount() * parallel;

        if (!storeOutput(outputResult.copyWithAmount(totalOutputAmount))) {
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            inputTankA.extractFluid(totalDrainA);
            inputTankB.extractFluid(totalDrainB);
            if (recipe.itemInput().isPresent()) {
                int itemDrain = recipe.itemInput().get().count() * parallel;
                catalyst.shrink(itemDrain);
                itemHandler.set(CATALYST_SLOT, ItemResource.of(catalyst), catalyst.getCount());
            }
            if (totalEnergy > 0) {
                extractEnergy(totalEnergy, false);
            }
        }

        setChanged();
    }

    public int getEffectiveProcessTicks(FluidMixerRecipe recipe) {
        if (recipe == null) return 0;
        if (UpgradeHelper.hasCreativeUpgrade(this) || UpgradeHelper.hasOverclock(this)) {
            return 1;
        }
        if (UpgradeHelper.countUpgrades(this, UpgradeType.UNDERCLOCK) > 0) {
            return Math.max(1, recipe.processTicks() * 2);
        }
        return Math.max(1, recipe.processTicks());
    }

    /**
     * 计算当前输入原料、输出空间与能量所支持的最大并行混合倍数。
     * 常规升级最多并行 8 批，顶级升级上限为 Integer.MAX_VALUE。
     */
    protected int calculateMaxParallel(FluidMixerRecipe recipe, FluidStack tankA, FluidStack tankB,
                                       ItemStack catalyst, int singleEnergyCost) {
        int drainA = recipe.getDrainA(tankA);
        int drainB = recipe.getDrainB(tankB);
        if (drainA < 0 || drainB < 0 || (drainA == 0 && drainB == 0)) {
            return 0;
        }

        int maxA = drainA == 0 ? Integer.MAX_VALUE : tankA.getAmount() / drainA;
        int maxB = drainB == 0 ? Integer.MAX_VALUE : tankB.getAmount() / drainB;
        int maxParallel = Math.min(maxA, maxB);

        if (recipe.itemInput().isPresent()) {
            int itemReq = recipe.itemInput().get().count();
            if (itemReq > 0) {
                int maxItem = catalyst.getCount() / itemReq;
                maxParallel = Math.min(maxParallel, maxItem);
            }
        }

        FluidStack outputResult = recipe.output();
        if (outputResult.getAmount() > 0) {
            FluidStack existing = outputTank.getFluid();
            if (!existing.isEmpty() && !existing.is(outputResult.getFluid())) {
                return 0;
            }
            int availableOutputSpace = outputTank.getCapacity() - outputTank.getFluidAmount();
            int maxOutput = availableOutputSpace / outputResult.getAmount();
            maxParallel = Math.min(maxParallel, maxOutput);
        }

        if (singleEnergyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this)) {
            int maxEnergy = getEnergyStored() / singleEnergyCost;
            maxParallel = Math.min(maxParallel, maxEnergy);
        }

        return FluidMixerParallelLimit.apply(
                Math.max(0, maxParallel),
                UpgradeHelper.hasMixingUpgrade(this),
                UpgradeHelper.hasUltimateMixingParallelUpgrade(this));
    }

    private FluidMixerRecipe findRecipe(FluidStack tankA, FluidStack tankB, ItemStack catalyst) {
        if (tankA.isEmpty() && tankB.isEmpty()) return null;

        // 优先检查缓存的配方
        if (cachedRecipe != null && cachedRecipe.matchesFluids(tankA, tankB, catalyst)) {
            return cachedRecipe;
        }

        // 从 JDTE 配方查找
        if (level instanceof ServerLevel serverLevel) {
            for (RecipeHolder<FluidMixerRecipe> holder : serverLevel.getServer().getRecipeManager()
                    .recipeMap().byType(JDTERecipes.FLUID_MIXER_RECIPE_TYPE.get())) {
                if (holder.value().matchesFluids(tankA, tankB, catalyst)) {
                    return holder.value();
                }
            }
        }

        // 从 Mekanism 配方缓存查找（如果已加载）
        if (net.neoforged.fml.ModList.get().isLoaded("mekanism")) {
            List<FluidMixerRecipe> mekRecipes = MekanismFluidMixerBridge.getCachedRecipes(level);
            for (FluidMixerRecipe recipe : mekRecipes) {
                if (recipe.matchesFluids(tankA, tankB, catalyst)) {
                    return recipe;
                }
            }
        }

        return null;
    }

    private boolean canOutputFluid(FluidStack result) {
        return !result.isEmpty()
                && outputTank.canStore(result);
    }

    private boolean storeOutput(FluidStack result) {
        if (!canOutputFluid(result)) return false;
        FluidStack previous = outputTank.getFluid().copy();
        if (outputTank.insertFluid(result) != result.getAmount()) {
            outputTank.setFluid(previous);
            return false;
        }
        return true;
    }

    private void resetProgress() {
        if (progress != 0 || cachedRecipe != null) {
            progress = 0;
            cachedRecipe = null;
            setChanged();
        }
    }

    @Override
    public boolean hasEnoughPower(int energyCost) {
        return PoweredMachineBE.super.hasEnoughPower(energyCost);
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        return PoweredMachineBE.super.extractEnergy(energy, simulate);
    }

    // ------ 序列化 ------

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", itemHandler);
        tag.putChild("inputTankA", inputTankA);
        tag.putChild("inputTankB", inputTankB);
        tag.putChild("outputTank", outputTank);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(itemHandler::deserialize);
        tag.child("inputTankA").ifPresent(inputTankA::deserialize);
        tag.child("inputTankB").ifPresent(inputTankB::deserialize);
        tag.child("outputTank").ifPresent(outputTank::deserialize);
        tag.getInt("energy").ifPresent(energyStorage::setEnergy);
        progress = tag.getIntOr("progress", progress);
    }

    // ------ Mek 桥接（延迟加载）------

    /**
     * 内部桥接类，仅在 Mekanism 可用时调用。
     * 实际逻辑委托给 {@code MekanismFluidMixerIntegration}。
     */
    static final class MekanismFluidMixerBridge {
        private MekanismFluidMixerBridge() {}

        static List<FluidMixerRecipe> getCachedRecipes(net.minecraft.world.level.Level level) {
            return com.jdte.common.integrations.MekanismFluidMixerIntegration.getCachedRecipes(level);
        }
    }
}
