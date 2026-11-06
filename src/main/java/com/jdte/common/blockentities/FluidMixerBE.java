package com.jdte.common.blockentities;

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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

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
    public final FluidContainerData fluidData;
    /** 额外 ContainerData：输入罐 B 和输出罐同步到客户端。 */
    public final ContainerData fluidMixerData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();

    protected final ItemStackHandler itemHandler;
    protected int progress = 0;
    private final int baseEnergyCapacity;

    /** 当前匹配的配方缓存。 */
    private FluidMixerRecipe cachedRecipe = null;
    private long lastRecipeCacheGen = -1L;

    protected FluidMixerBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
                           int baseEnergyCapacity) {
        super(type, pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        this.baseEnergyCapacity = baseEnergyCapacity;
        energyStorage = new MachineEnergyStorage(getMaxEnergy()) {
            @Override public int receiveEnergy(int amount,boolean simulate) { int accepted=super.receiveEnergy(amount,simulate);if(!simulate && accepted>0)setChanged();return accepted; }
        };
        poweredData = new PoweredMachineContainerData(this);
        inputTankA = persistentTank();
        inputTankB = persistentTank();
        // Internal production must be able to fill this tank. The combined capability
        // restricts external fills to inputTankA/inputTankB.
        outputTank = persistentTank();
        fluidData = new FluidContainerData(this);
        fluidMixerData = new ContainerData() {
            private final int[] client = new int[15];
            @Override public int get(int index) {
                if (level != null && level.isClientSide) return client[index];
                int max = cachedRecipe == null ? 0 : getEffectiveProcessTicks(cachedRecipe);
                return switch(index) {
                    case 0 -> progress & 0xFFFF;
                    case 1 -> max & 0xFFFF;
                    case 2 -> BuiltInRegistries.FLUID.getId(inputTankB.getFluid().getFluid());
                    case 3 -> inputTankB.getFluidAmount() & 0xFFFF;
                    case 4 -> inputTankB.getFluidAmount() >>> 16;
                    case 5 -> BuiltInRegistries.FLUID.getId(outputTank.getFluid().getFluid());
                    case 6 -> outputTank.getFluidAmount() & 0xFFFF;
                    case 7 -> outputTank.getFluidAmount() >>> 16;
                    case 8 -> BuiltInRegistries.FLUID.getId(inputTankA.getFluid().getFluid());
                    case 9 -> inputTankA.getFluidAmount() & 0xFFFF;
                    case 10 -> inputTankA.getFluidAmount() >>> 16;
                    case 11 -> getMaxMB() & 0xFFFF;
                    case 12 -> getMaxMB() >>> 16;
                    case 13 -> progress >>> 16;
                    case 14 -> max >>> 16;
                    default -> 0;
                };
            }
            @Override public void set(int index,int value) { if(index>=0 && index<client.length)client[index]=value & 0xFFFF; }
            @Override public int getCount() { return client.length; }
        };
        itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    private JDTEFluidTank persistentTank() {
        return new JDTEFluidTank(getMaxMB()) {
            @Override protected void onContentsChanged() { setChanged(); }
        };
    }

    public void syncTankCapacities() {
        int capacity = getMaxMB();
        for (JDTEFluidTank tank : new JDTEFluidTank[]{inputTankA,inputTankB,outputTank}) {
            if (tank == null) continue;
            tank.setCapacity(capacity);
            if (tank.getFluidAmount() > capacity) tank.setFluid(com.jdte.common.integrations.ae2.FluidCopies.withAmount(tank.getFluid(),capacity));
        }
    }

    // Forge JDT exposes its input-only fluid capability before attached capabilities.
    private net.minecraftforge.common.util.LazyOptional<IFluidHandler> combinedCapability = net.minecraftforge.common.util.LazyOptional.of(this::getCombinedFluidHandler);
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
        if(capability==net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER) return combinedCapability==null ? net.minecraftforge.common.util.LazyOptional.empty() : combinedCapability.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps();if(combinedCapability!=null)combinedCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps();combinedCapability=net.minecraftforge.common.util.LazyOptional.of(this::getCombinedFluidHandler); }
    private boolean liveEndpoint() { return level!=null && !isRemoved() && level.isLoaded(worldPosition) && level.getBlockEntity(worldPosition)==this; }
    // This machine owns its recipe clock; JDT's generic delay must not divide it again.
    @Override public boolean canRun() { return true; }
    @Override public boolean isDefaultSettings() { return false; }

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
    public ItemStackHandler getMachineHandler() {
        return itemHandler;
    }

    public ContainerData getFluidMixerData() {
        return fluidMixerData;
    }

    /**
     * 供自动化使用的组合流体处理器：将三个罐组合为一个视图。
     * 罐 0/1 为输入（可灌入）、罐 2 为输出（只抽出）。
     */
    public IFluidHandler getCombinedFluidHandler() {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return 3;
            }

            @Override
            public FluidStack getFluidInTank(int tank) {
                if(!liveEndpoint())return FluidStack.EMPTY;
                return switch (tank) {
                    case 0 -> inputTankA.getFluid();
                    case 1 -> inputTankB.getFluid();
                    case 2 -> outputTank.getFluid();
                    default -> FluidStack.EMPTY;
                };
            }

            @Override
            public int getTankCapacity(int tank) {
                if(!liveEndpoint())return 0;
                return switch (tank) {
                    case 0 -> inputTankA.getCapacity();
                    case 1 -> inputTankB.getCapacity();
                    case 2 -> outputTank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public boolean isFluidValid(int tank, FluidStack stack) {
                return liveEndpoint() && (tank == 0 || tank == 1); // 输出罐不接受灌入
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (!liveEndpoint() || resource.isEmpty()) return 0;
                // 优先填入匹配的罐；若都空则 A 先填
                if (!inputTankA.getFluid().isEmpty() && inputTankA.getFluid().getFluid() == resource.getFluid()) {
                    return inputTankA.fill(resource, action);
                }
                if (!inputTankB.getFluid().isEmpty() && inputTankB.getFluid().getFluid() == resource.getFluid()) {
                    return inputTankB.fill(resource, action);
                }
                if (inputTankA.getFluid().isEmpty()) {
                    return inputTankA.fill(resource, action);
                }
                if (inputTankB.getFluid().isEmpty()) {
                    return inputTankB.fill(resource, action);
                }
                return 0;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if(!liveEndpoint())return FluidStack.EMPTY;
                // 只从输出罐抽取
                if (!resource.isEmpty() && outputTank.getFluid().getFluid() == resource.getFluid()) {
                    return outputTank.drain(resource, action);
                }
                return FluidStack.EMPTY;
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                if(!liveEndpoint())return FluidStack.EMPTY;
                return outputTank.drain(maxDrain, action);
            }
        };
    }

    // ------ 工作逻辑 ------

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun() && UpgradeHelper.mayRunWithUpgrades(this)) {
            processMixing();
        }
    }

    protected int getEffectiveEnergyCost(int baseCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        return UpgradeHelper.adjustEnergyCost(this, baseCost);
    }

    private void processMixing() {
        if (!(level instanceof ServerLevel) || !UpgradeHelper.mayRunWithUpgrades(this)) return;

        FluidStack tankAFluid = inputTankA.getFluid();
        FluidStack tankBFluid = inputTankB.getFluid();
        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);

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

        if (!storeOutput(com.jdte.common.integrations.ae2.FluidCopies.withAmount(outputResult,totalOutputAmount))) {
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            inputTankA.drain(totalDrainA, IFluidHandler.FluidAction.EXECUTE);
            inputTankB.drain(totalDrainB, IFluidHandler.FluidAction.EXECUTE);
            if (recipe.itemInput().isPresent()) {
                int itemDrain = recipe.itemInput().get().count() * parallel;
                catalyst.shrink(itemDrain);
                itemHandler.setStackInSlot(CATALYST_SLOT, catalyst);
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
        if (!UpgradeHelper.mayRunWithUpgrades(this) && isActiveRedstone()) return;
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        if (ticks <= 0 || !isActiveRedstone() || !canRun()) {
            return;
        }
        processAcceleratedMixing(ticks);
    }

    protected void processAcceleratedMixing(int ticks) {
        if (!(level instanceof ServerLevel) || !UpgradeHelper.mayRunWithUpgrades(this)) return;
        FluidStack tankAFluid = inputTankA.getFluid();
        FluidStack tankBFluid = inputTankB.getFluid();
        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
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

        if (!storeOutput(com.jdte.common.integrations.ae2.FluidCopies.withAmount(outputResult,totalOutputAmount))) {
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            inputTankA.drain(totalDrainA, IFluidHandler.FluidAction.EXECUTE);
            inputTankB.drain(totalDrainB, IFluidHandler.FluidAction.EXECUTE);
            if (recipe.itemInput().isPresent()) {
                int itemDrain = recipe.itemInput().get().count() * parallel;
                catalyst.shrink(itemDrain);
                itemHandler.setStackInSlot(CATALYST_SLOT, catalyst);
            }
            if (totalEnergy > 0) {
                extractEnergy(totalEnergy, false);
            }
        }

        setChanged();
    }

    public int getEffectiveProcessTicks(FluidMixerRecipe recipe) {
        if (recipe == null) return 0;
        if (UpgradeHelper.hasCreativeUpgrade(this) || UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasUltimateOverclock(this)) {
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
            if (!existing.isEmpty() && existing.getFluid() != outputResult.getFluid()) {
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
        if (lastRecipeCacheGen != RecipeCacheSignal.generation()) {
            lastRecipeCacheGen = RecipeCacheSignal.generation();
            cachedRecipe = null;
        }
        if (tankA.isEmpty() && tankB.isEmpty()) return null;

        // 优先检查缓存的配方
        if (cachedRecipe != null && cachedRecipe.matchesFluids(tankA, tankB, catalyst)) {
            return cachedRecipe;
        }

        // 从 JDTE 配方查找
        if (level != null) {
            for (FluidMixerRecipe recipe : level.getRecipeManager()
                    .getAllRecipesFor(JDTERecipes.FLUID_MIXER_RECIPE_TYPE.get())) {
                if (recipe.matchesFluids(tankA, tankB, catalyst)) {
                    return recipe;
                }
            }
        }

        // 从 Mekanism 配方缓存查找（如果已加载）
        if (net.minecraftforge.fml.ModList.get().isLoaded("mekanism")) {
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
                && outputTank.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
    }

    private boolean storeOutput(FluidStack result) {
        if (!canOutputFluid(result)) return false;
        FluidStack previous = outputTank.getFluid().copy();
        if (outputTank.fill(result, IFluidHandler.FluidAction.EXECUTE) != result.getAmount()) {
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("inputTankA", inputTankA.serializeNBT());
        tag.put("inputTankB", inputTankB.serializeNBT());
        tag.put("outputTank", outputTank.serializeNBT());
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) {
            OversizedItemStackHandlerSerialization.deserialize(itemHandler, tag.getCompound("inventory"));
        }
        if (tag.contains("inputTankA")) {
            inputTankA.deserializeNBT(tag.getCompound("inputTankA"));
        }
        if (tag.contains("inputTankB")) {
            inputTankB.deserializeNBT(tag.getCompound("inputTankB"));
        }
        if (tag.contains("outputTank")) {
            outputTank.deserializeNBT(tag.getCompound("outputTank"));
        }
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
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
