package com.jdte.common.blockentities;

import com.jdte.common.network.JDTEPacketHandler;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.blocks.LifeSynthesisStructure;
import com.jdte.common.network.data.LifeSynthesisRunningPayload;
import com.jdte.common.recipes.LifeSynthesisRecipe;
import com.jdte.common.recipes.RecipeCacheSignal;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEFluids;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 生命合成舱控制器。
 *
 * <p>两阶段状态机：培养阶段按 (elapsedTicks × 基础速率 × 倍率 × 加速) 积累
 * cultureWork，达标一批即消耗培养基/养分/FE（Creative 免 FE 与时间流体）并进入
 * 待蒸馏队列；蒸馏阶段把队列灌入相邻流体处理器（直连优先，最近成功者优先），
 * 剩余进入内部生命流体罐。资源不足只暂停，不丢进度。</p>
 */
public class LifeSynthesisVatBE extends BaseMachineBE implements PoweredMachineBE, FluidMachineBE,
        RedstoneControlledBE, ExtendedUpgradeMachine, CoalescedAcceleratedMachine {
    public static final int INPUT_SLOTS = 12;
    public static final int TOTAL_SLOTS = INPUT_SLOTS;
    public static final int UPGRADE_SLOTS = 8;
    /** 培养基中的禁止项（生命流体正反馈红线：生命产物不可回流作原料）。 */
    private static final List<ItemStack> BANNED_INPUTS = List.of(
            new ItemStack(JDTEItems.LIFE_APPLE.get()),
            new ItemStack(JDTEFluids.LIFE_FLUID_BUCKET.get()));
    /** 配方支持材料集合缓存：按 RecipeCacheSignal 代数重建，避免每次插入遍历全部配方。 */
    private static long supportedMaterialsGeneration = -1L;
    private static Set<Item> supportedMaterials = Set.of();

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    // 罐内容变化即时标脏（setChanged + 客户端同步），避免依赖周期性心跳兜底
    private final JDTEFluidTank nutrientTank = vatTank(stack ->
            !stack.getFluid().isSame(JDTEFluids.LIFE_FLUID_SOURCE.get())
                    && !stack.getFluid().isSame(JDTEFluids.LIFE_FLUID_FLOWING.get())
                    && !(stack.getFluid() instanceof TimeFluid));
    private final JDTEFluidTank timeFluidTank = vatTank(
            stack -> stack.getFluid() instanceof TimeFluid);
    private final JDTEFluidTank lifeFluidTank = vatTank(stack ->
            stack.getFluid().isSame(JDTEFluids.LIFE_FLUID_SOURCE.get()) || stack.getFluid().isSame(JDTEFluids.LIFE_FLUID_FLOWING.get()));
    /** 组合流体处理器视图（养分/时间/生命），避免每次查询分配数组。 */
    private final JDTEFluidTank[] combinedTanks = {nutrientTank, timeFluidTank, lifeFluidTank};
    private final FluidContainerData fluidData = new FluidContainerData(this);
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot < 0 || slot >= INPUT_SLOTS || stack.isEmpty()) return false;
            for (ItemStack banned : BANNED_INPUTS) {
                if (ItemStack.isSameItemSameTags(banned, stack)) return false;
            }
            // 只允许配方支持的材料（客户端与服务端同规则；配方数据包未同步时宽松放行，服务端权威兜底）
            return isSupportedMaterial(level, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            renderInputRevision++;
            setChanged();
            markDirtyClient();
        }
    };
    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override public int getSlots() { return INPUT_SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) { return valid(slot) ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return valid(slot) ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? itemHandler.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return valid(slot) && itemHandler.isItemValid(slot, stack);
        }
        private boolean valid(int slot) { return slot >= 0 && slot < INPUT_SLOTS; }
    };
    private final IFluidHandler combinedFluidHandler = new CombinedFluidHandler();
    private final ContainerData vatData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cultureWork;
                case 1 -> cachedRecipe == null ? 1 : cachedRecipe.processTicks();
                case 2 -> nutrientTank.getFluidAmount();
                case 3 -> timeFluidTank.getFluidAmount();
                case 4 -> lifeFluidTank.getFluidAmount();
                case 5 -> getMaxMB();
                case 6 -> pendingLifeFluid;
                case 7 -> cachedRecipe == null ? 0 : tierCode(cachedRecipe.tier());
                case 8 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 9 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                case 10 -> cachedRecipe == null ? -1 : recipeListIndex;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> syncedCultureWork = value;
                case 1 -> syncedProcessTicks = value;
                case 2 -> syncedNutrientFluid = value;
                case 3 -> syncedTimeFluid = value;
                case 4 -> syncedLifeFluid = value;
                case 5 -> syncedFluidCapacity = value;
                case 6 -> syncedPendingLifeFluid = value;
                case 7 -> syncedTierCode = value;
                case 8 -> syncedMultiplier = value;
                case 9 -> syncedMaxMultiplier = value;
                case 10 -> syncedRecipeIndex = value;
                default -> { }
            }
        }

        @Override public int getCount() { return 11; }
    };

    private int settlementTicker;
    private boolean legacyRoofChecked;
    private int cultureWork;
    private int pendingLifeFluid;
    private int multiplier;
    private long lastSettlementGameTime = Long.MIN_VALUE;
    private int accumulatedAcceleratedTicks;
    // 配方缓存
    private long cachedRecipeGeneration = -1L;
    private LifeSynthesisRecipe cachedRecipe;
    private int recipeListIndex = -1;
    private final ItemStack[] cachedInputs = new ItemStack[INPUT_SLOTS];
    private Fluid cachedNutrientFluid;
    private boolean cacheValid;
    // 直连输出缓存
    private Direction cachedBoundaryFacing;
    private List<LifeSynthesisStructure.BoundaryNeighbor> cachedBoundaryNeighbors = List.of();
    private final List<LifeSynthesisStructure.BoundaryNeighbor> outputPreference = new ArrayList<>();
    /** 直连输出邻居失败退避截止 gameTime，防全满/不支持时每结算全量能力探测。 */
    private static final int NEIGHBOR_RETRY_TICKS = 100;
    private final Map<LifeSynthesisStructure.BoundaryNeighbor, Long> neighborCooldown = new HashMap<>();
    /** 客户端舱内展示物，只在输入库存变化后重新选择。 */
    private int renderInputRevision;
    private int cachedRenderInputRevision = -1;
    private ItemStack cachedRenderInput = ItemStack.EMPTY;
    // 客户端同步字段
    private int syncedCultureWork;
    private int syncedProcessTicks = 1;
    private int syncedNutrientFluid;
    private int syncedTimeFluid;
    private int syncedLifeFluid;
    private int syncedFluidCapacity = 1;
    private int syncedPendingLifeFluid;
    private int syncedTierCode;
    private int syncedMultiplier = 1;
    private int syncedMaxMultiplier = 32;
    private int syncedRecipeIndex = -1;
    private boolean running;
    /** 客户端合成运行状态（由 LifeSynthesisRunningPayload 轻量同步，驱动光束渲染）。 */
    private boolean syncedRunning;
    /** 客户端渲染平滑进度（不参与逻辑与存档；-1 表示未初始化）。 */
    private float renderSmoothProgress = -1F;

    public LifeSynthesisVatBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LIFE_SYNTHESIS_VAT.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.lifeSynthesisVat.defaultSpeedMultiplier.get();
        Arrays.fill(cachedInputs, ItemStack.EMPTY);
    }

    /** 罐内容变化时即时标脏，保证客户端渲染与存档及时。 */
    private JDTEFluidTank vatTank(Predicate<FluidStack> validator) {
        return new JDTEFluidTank(getMaxMB(), validator) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                markDirtyClient();
            }
        };
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (!legacyRoofChecked && level != null) {
            legacyRoofChecked = LifeSynthesisStructure.removeLegacyRoof(
                    level, worldPosition, LifeSynthesisStructure.horizontalFacing(getBlockState()));
        }
        // 配方解析由服务端权威执行；客户端只消费 ContainerData，避免两端配方缓存不一致。
        resolveRecipe();
        syncRunningState();
        if (level != null && level.getGameTime() % 20L == 0L) {
            UpgradeHelper.syncCapacities(this);
            // 容量变化（升级）才需要同步客户端；流体/进度由罐回调与结算路径即时覆盖
            if (syncExtraTankCapacities()) markDirtyClient();
            // 周期性持久化兜底（能量等杂项状态），本地标记开销可忽略
            setChanged();
        }
        advanceProductionTicks(1);
    }

    @Override
    public void accumulateAcceleratedTicks(int ticks) {
        accumulatedAcceleratedTicks = saturatingAdd(accumulatedAcceleratedTicks, ticks);
    }

    @Override
    public void flushAcceleratedTicks() {
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        advanceProductionTicks(ticks);
    }

    private void advanceProductionTicks(int ticks) {
        if (ticks <= 0) return;
        if (!isActiveRedstone() || !canRun()) {
            settlementTicker = 0;
            return;
        }
        int interval = JDTEConfig.COMMON.lifeSynthesisVat.settlementInterval.get();
        settlementTicker = saturatingAdd(settlementTicker, ticks);
        if (settlementTicker < interval || level == null || lastSettlementGameTime == level.getGameTime()) return;
        int completedSettlements = Math.max(1, settlementTicker / interval);
        int elapsed = completedSettlements * interval;
        settlementTicker -= elapsed;
        lastSettlementGameTime = level.getGameTime();
        settleProduction(elapsed);
    }

    private void settleProduction(int elapsedTicks) {
        if (!resolveRecipe()) {
            // 配方消失（原料被换走/营养液类型不匹配）：清进度，与新温室行为一致
            if (cultureWork != 0) {
                cultureWork = 0;
                setChanged();
            }
            return;
        }
        LifeSynthesisRecipe recipe = cachedRecipe;
        int config = JDTEConfig.COMMON.lifeSynthesisVat.baseWorkRate.get();
        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        int speedMultiplier = getEffectiveSpeedMultiplier();
        int timeFluidCost = JDTEConfig.COMMON.lifeSynthesisVat.timeFluidPerBatch.get();
        boolean timeBoost = creative || (timeFluidCost > 0 && timeFluidTank.getFluidAmount() >= timeFluidCost);
        long workAdded = (long) elapsedTicks * config * speedMultiplier * (timeBoost ? 2L : 1L);
        cultureWork = saturatingAdd(cultureWork, (int) Math.min(Integer.MAX_VALUE, workAdded));

        int processTicks = recipe.processTicks();
        int maxBatches = JDTEConfig.COMMON.lifeSynthesisVat.maxBatchesPerSettlement.get();
        int pendingCap = JDTEConfig.COMMON.lifeSynthesisVat.pendingLifeFluidCap.get();
        int nutrientAmount = recipe.nutrient().getAmount();
        int energyPerBatch = creative ? 0 : recipe.energy();
        int timeCost = creative || !timeBoost ? 0 : timeFluidCost;
        int outputPerBatch = recipe.output().getAmount();

        int batches = 0;
        // 输入列表在循环外取一次：consumeStrict 直接修改槽内 ItemStack 引用，避免逐批重建
        List<ItemStack> inputs = getInputStacks();
        while (cultureWork >= processTicks && batches < maxBatches && pendingLifeFluid < pendingCap) {
            if (!creative && (energyStorage.getEnergyStored() < energyPerBatch
                    || nutrientTank.getFluidAmount() < nutrientAmount)) break;
            if (!creative && timeBoost && timeFluidTank.getFluidAmount() < timeCost) break;
            // 严格扣减：任一输入行数量不足时整批放弃，防止部分扣减造成超产
            if (!recipe.consumeStrict(inputs)) break;
            cultureWork -= processTicks;
            // 养分与培养基即使创造升级也照常消耗，防止生命流体正反馈循环
            nutrientTank.drain(nutrientAmount, IFluidHandler.FluidAction.EXECUTE);
            if (!creative) {
                energyStorage.extractEnergy(energyPerBatch, false);
                if (timeBoost) timeFluidTank.drain(timeCost, IFluidHandler.FluidAction.EXECUTE);
            }
            pendingLifeFluid = saturatingAdd(pendingLifeFluid, outputPerBatch);
            batches++;
        }
        distill();
        // 结算必然改变 settlementTicker/进度/待蒸馏量等存档状态；客户端显示由罐回调与 ContainerData 覆盖
        setChanged();
    }

    /** 直连相邻流体处理器优先（最近成功者优先），剩余进内部罐；队列上限由配置约束。 */
    private void distill() {
        if (pendingLifeFluid <= 0) return;
        int remaining = pendingLifeFluid;
        long now = level != null ? level.getGameTime() : 0L;
        Fluid lifeFluid = JDTEFluids.LIFE_FLUID_SOURCE.get();
        // 迭代副本：循环内 moveNeighborToFront 会重排 outputPreference（remove+add），
        // 直接迭代原列表会在第二个成功邻居处抛 ConcurrentModificationException
        for (LifeSynthesisStructure.BoundaryNeighbor neighbor : List.copyOf(getOutputNeighbors())) {
            if (remaining <= 0) break;
            // 失败邻居短期退避，避免全满/不支持时每结算做全量能力探测
            Long retryUntil = neighborCooldown.get(neighbor);
            if (retryUntil != null && now < retryUntil) continue;
            BlockEntity neighborBlockEntity = neighbor.pos() != null && level != null
                    ? level.getBlockEntity(neighbor.pos()) : null;
            IFluidHandler handler = neighborBlockEntity != null
                    ? neighborBlockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, neighbor.exposedSide()).orElse(null)
                    : null;
            if (handler == null) {
                neighborCooldown.put(neighbor, now + NEIGHBOR_RETRY_TICKS);
                continue;
            }
            int accepted = handler.fill(new FluidStack(lifeFluid, remaining),
                    IFluidHandler.FluidAction.EXECUTE);
            if (accepted > 0) {
                remaining -= accepted;
                neighborCooldown.remove(neighbor);
                moveNeighborToFront(neighbor);
            } else {
                neighborCooldown.put(neighbor, now + NEIGHBOR_RETRY_TICKS);
            }
        }
        if (remaining > 0) {
            remaining -= lifeFluidTank.fill(new FluidStack(lifeFluid, remaining),
                    IFluidHandler.FluidAction.EXECUTE);
        }
        if (remaining != pendingLifeFluid) {
            pendingLifeFluid = remaining;
        }
    }

    // ============================================================
    // 配方缓存
    // ============================================================

    private boolean resolveRecipe() {
        long generation = RecipeCacheSignal.generation();
        if (generation != cachedRecipeGeneration) {
            cachedRecipeGeneration = generation;
            invalidateRecipeCache();
        }
        List<ItemStack> inputs = getInputStacks();
        Fluid nutrientFluid = nutrientTank.getFluid().getFluid();
        if (cacheValid && inputsMatchCache(inputs) && nutrientFluid == cachedNutrientFluid) {
            return cachedRecipe != null;
        }
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            cachedInputs[slot] = inputs.get(slot).isEmpty() ? ItemStack.EMPTY : inputs.get(slot).copy();
        }
        cachedNutrientFluid = nutrientFluid;
        cacheValid = true;
        cachedRecipe = findRecipe(inputs);
        return cachedRecipe != null;
    }

    private LifeSynthesisRecipe findRecipe(List<ItemStack> inputs) {
        if (level == null) return null;
        int index = 0;
        for (LifeSynthesisRecipe recipe : level.getRecipeManager().getAllRecipesFor(JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get())) {
            if (!recipe.matchesSlots(inputs)) {
                index++;
                continue;
            }
            FluidStack nutrient = recipe.nutrient();
            if (!nutrient.isEmpty() && !nutrientTank.isEmpty() && !nutrient.getFluid().isSame(nutrientTank.getFluid().getFluid())) {
                index++;
                continue;
            }
            recipeListIndex = index;
            return recipe;
        }
        recipeListIndex = -1;
        return null;
    }

    private boolean inputsMatchCache(List<ItemStack> inputs) {
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack stack = inputs.get(slot);
            // 数量也必须一致：缓存快照保留 count，防止数量不足时仍命中配方造成超产
            if (!ItemStack.isSameItemSameTags(stack, cachedInputs[slot])
                    || stack.getCount() != cachedInputs[slot].getCount()) return false;
        }
        return true;
    }

    private void invalidateRecipeCache() {
        cachedRecipe = null;
        recipeListIndex = -1;
        cacheValid = false;
        Arrays.fill(cachedInputs, ItemStack.EMPTY);
        cachedNutrientFluid = null;
    }

    /** 配方状态翻转时向追踪玩家发送轻量同步包，驱动客户端光束渲染（仅变化时发包）。 */
    private void syncRunningState() {
        boolean newRunning = cachedRecipe != null;
        if (running == newRunning) return;
        running = newRunning;
        if (level instanceof ServerLevel serverLevel) {
            JDTEPacketHandler.sendToTrackingChunk(serverLevel, new ChunkPos(worldPosition),
                    new LifeSynthesisRunningPayload(worldPosition, newRunning));
        }
    }

    /**
     * 配方支持材料校验：从当前配方表收集全部输入行的物品并缓存为集合。
     * 集合为空（数据包未就绪）时放行，避免误拒正常操作；服务端在配方同步后生效。
     */
    private static boolean isSupportedMaterial(Level level, ItemStack stack) {
        if (level == null) return true;
        long generation = RecipeCacheSignal.generation();
        if (generation != supportedMaterialsGeneration) {
            supportedMaterialsGeneration = generation;
            Set<Item> items = new HashSet<>();
            for (LifeSynthesisRecipe recipe : level.getRecipeManager().getAllRecipesFor(JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get())) {
                for (LifeSynthesisRecipe.InputSlot slot : recipe.inputs()) {
                    for (ItemStack sample : slot.ingredient().getItems()) items.add(sample.getItem());
                }
            }
            supportedMaterials = Set.copyOf(items);
        }
        return supportedMaterials.isEmpty() || supportedMaterials.contains(stack.getItem());
    }

    private List<ItemStack> getInputStacks() {
        List<ItemStack> stacks = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) stacks.add(itemHandler.getStackInSlot(slot));
        return stacks;
    }

    // ============================================================
    // 直连输出
    // ============================================================

    private List<LifeSynthesisStructure.BoundaryNeighbor> getOutputNeighbors() {
        Direction facing = LifeSynthesisStructure.horizontalFacing(getBlockState());
        if (cachedBoundaryFacing != facing) {
            cachedBoundaryFacing = facing;
            cachedBoundaryNeighbors = LifeSynthesisStructure.boundaryNeighbors(worldPosition, facing);
            outputPreference.clear();
            outputPreference.addAll(cachedBoundaryNeighbors);
            neighborCooldown.clear();
        }
        return outputPreference;
    }

    private void moveNeighborToFront(LifeSynthesisStructure.BoundaryNeighbor neighbor) {
        if (outputPreference.size() < 2) return;
        int index = outputPreference.indexOf(neighbor);
        if (index > 0) {
            outputPreference.remove(index);
            outputPreference.add(0, neighbor);
        }
    }

    /** 供自动 I/O 按结构边界探测外部邻居（与蒸馏直连共用同一缓存）。 */
    public List<LifeSynthesisStructure.BoundaryNeighbor> getBoundaryNeighbors() {
        return getOutputNeighbors();
    }

    // ============================================================
    // 查询/状态
    // ============================================================

    /** 客户端渲染用：生产时平滑上升，批次完成重置时立即跟随，避免液柱阶梯跳动。 */
    public float getRenderProgress() {
        float target = getClientCultureProgress();
        if (renderSmoothProgress < 0F) {
            renderSmoothProgress = target;
            return target;
        }
        if (target < renderSmoothProgress) {
            renderSmoothProgress = target;
            return target;
        }
        renderSmoothProgress += (target - renderSmoothProgress) * 0.15F;
        return renderSmoothProgress;
    }

    private int getEffectiveSpeedMultiplier() {
        return UpgradeHelper.hasOverclock(this)
                ? JDTEConfig.COMMON.lifeSynthesisVat.overclockMaxSpeedMultiplier.get()
                : net.minecraft.util.Mth.clamp(multiplier, 1, JDTEConfig.COMMON.lifeSynthesisVat.maxSpeedMultiplier.get());
    }

    public int getMultiplier() {
        return isClientSide() ? syncedMultiplier
                : net.minecraft.util.Mth.clamp(multiplier, 1, getMaxSelectableMultiplier());
    }

    public void setMultiplier(int value) {
        int clamped = net.minecraft.util.Mth.clamp(value, 1, getMaxSelectableMultiplier());
        if (multiplier != clamped) {
            multiplier = clamped;
            setChanged();
            markDirtyClient();
        }
    }

    public int getMaxSelectableMultiplier() {
        if (isClientSide()) return Math.max(1, syncedMaxMultiplier);
        return UpgradeHelper.hasOverclock(this)
                ? JDTEConfig.COMMON.lifeSynthesisVat.overclockMaxSpeedMultiplier.get()
                : JDTEConfig.COMMON.lifeSynthesisVat.maxSpeedMultiplier.get();
    }

    /** 客户端显示用：当前配方进度 (0..1) 的比例表示。 */
    public float getClientCultureProgress() {
        int max = Math.max(1, syncedProcessTicks);
        return net.minecraft.util.Mth.clamp((float) syncedCultureWork / max, 0.0F, 1.0F);
    }

    public static int tierCode(String tier) {
        return switch (tier) {
            case "plant" -> 1;
            case "protein" -> 2;
            case "enriched" -> 3;
            default -> 0;
        };
    }

    public ItemStack getRenderInputItem() {
        if (cachedRenderInputRevision != renderInputRevision) {
            cachedRenderInput = ItemStack.EMPTY;
            for (int slot = 0; slot < INPUT_SLOTS; slot++) {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    cachedRenderInput = stack.copyWithCount(1);
                    break;
                }
            }
            cachedRenderInputRevision = renderInputRevision;
        }
        return cachedRenderInput;
    }

    public int getSyncedTierCode() { return syncedTierCode; }
    public boolean getSyncedRunning() { return isClientSide() ? syncedRunning : running; }
    public void applyClientSync(boolean running) { this.syncedRunning = running; }
    public int getSyncedPendingLifeFluid() { return syncedPendingLifeFluid; }
    public int getSyncedNutrientFluid() { return syncedNutrientFluid; }
    public int getSyncedTimeFluid() { return syncedTimeFluid; }
    public int getSyncedLifeFluid() { return syncedLifeFluid; }
    public int getSyncedFluidCapacity() { return Math.max(1, syncedFluidCapacity); }
    public ContainerData getVatData() { return vatData; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }
    public IFluidHandler getCombinedFluidHandler() { return combinedFluidHandler; }
    public JDTEFluidTank getNutrientTank() { return nutrientTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public JDTEFluidTank getLifeFluidTank() { return lifeFluidTank; }

    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.lifeSynthesisVat.energyCapacity.get()); }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public int getStandardEnergyCost() { return cachedRecipe == null ? 0 : cachedRecipe.energy(); }
    @Override public int getMaxMB() { return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.lifeSynthesisVat.fluidCapacity.get()); }
    @Override public JDTEFluidTank getFluidTank() { return timeFluidTank; }
    @Override public FluidContainerData getFluidContainerData() { return fluidData; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public boolean canRun() { return true; }

    private boolean syncExtraTankCapacities() {
        int capacity = getMaxMB();
        boolean changed = false;
        if (nutrientTank.getCapacity() != capacity) {
            setTankCapacity(nutrientTank, capacity);
            changed = true;
        }
        if (lifeFluidTank.getCapacity() != capacity) {
            setTankCapacity(lifeFluidTank, capacity);
            changed = true;
        }
        return changed;
    }

    private static void setTankCapacity(JDTEFluidTank tank, int capacity) {
        if (tank instanceof com.jdte.mixin.FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
            if (tank.getFluidAmount() > capacity) tank.getFluid().setAmount(capacity);
        }
    }

    private static int saturatingAdd(int left, int right) {
        if (right <= 0) return left;
        return left > Integer.MAX_VALUE - right ? Integer.MAX_VALUE : left + right;
    }

    private boolean isClientSide() { return level != null && level.isClientSide; }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("nutrientFluid", nutrientTank.serializeNBT());
        tag.put("timeFluid", timeFluidTank.serializeNBT());
        tag.put("lifeFluid", lifeFluidTank.serializeNBT());
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("cultureWork", cultureWork);
        tag.putInt("pendingLifeFluid", pendingLifeFluid);
        tag.putInt("multiplier", getMultiplier());
        tag.putInt("settlementTicker", settlementTicker);
        tag.putInt("acceleratedTicks", accumulatedAcceleratedTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
            // deserializeNBT 会按 NBT Size 重建槽列表；旧版存档只有 9 个输入槽时扩到 12 槽。
            // 注意 setSize 会清空列表（NonNullList.withSize 重建），而客户端每次 NBT 同步
            // 都经过 loadAdditional，槽数已足够时绝不能调用，否则输入槽图标会在每次同步后消失。
            if (itemHandler.getSlots() < INPUT_SLOTS) {
                List<ItemStack> legacy = new ArrayList<>(itemHandler.getSlots());
                for (int i = 0; i < itemHandler.getSlots(); i++) legacy.add(itemHandler.getStackInSlot(i));
                itemHandler.setSize(INPUT_SLOTS);
                for (int i = 0; i < legacy.size(); i++) itemHandler.setStackInSlot(i, legacy.get(i));
            }
        }
        renderInputRevision++;
        cachedRenderInputRevision = -1;
        if (tag.contains("nutrientFluid")) nutrientTank.deserializeNBT(tag.getCompound("nutrientFluid"));
        if (tag.contains("timeFluid")) timeFluidTank.deserializeNBT(tag.getCompound("timeFluid"));
        if (tag.contains("lifeFluid")) lifeFluidTank.deserializeNBT(tag.getCompound("lifeFluid"));
        if (tag.contains("energy")) energyStorage.setEnergy(tag.getInt("energy"));
        cultureWork = Math.max(0, tag.getInt("cultureWork"));
        pendingLifeFluid = Math.max(0, tag.getInt("pendingLifeFluid"));
        multiplier = tag.contains("multiplier") ? tag.getInt("multiplier")
                : JDTEConfig.COMMON.lifeSynthesisVat.defaultSpeedMultiplier.get();
        settlementTicker = Math.max(0, tag.getInt("settlementTicker"));
        accumulatedAcceleratedTicks = Math.max(0, tag.getInt("acceleratedTicks"));
        invalidateRecipeCache();
        neighborCooldown.clear();
    }

    private abstract class TankView implements IFluidHandler {
        protected abstract JDTEFluidTank[] tanks();
        @Override public int getTanks() { return tanks().length; }
        @Override public FluidStack getFluidInTank(int tank) { return valid(tank) ? tanks()[tank].getFluid() : FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return valid(tank) ? tanks()[tank].getCapacity() : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return valid(tank) && tanks()[tank].isFluidValid(stack); }
        protected boolean valid(int tank) { return tank >= 0 && tank < tanks().length; }
    }

    /** 养分罐 + 时间流体罐可插入，生命流体罐可抽取。 */
    private final class CombinedFluidHandler extends TankView {
        @Override protected JDTEFluidTank[] tanks() { return combinedTanks; }
        @Override public int fill(FluidStack stack, FluidAction action) {
            for (JDTEFluidTank tank : tanks()) {
                if (tank == lifeFluidTank) continue;
                if (tank.isFluidValid(stack)) return tank.fill(stack, action);
            }
            return 0;
        }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { return lifeFluidTank.drain(stack, action); }
        @Override public FluidStack drain(int amount, FluidAction action) { return lifeFluidTank.drain(amount, action); }
    }
}
