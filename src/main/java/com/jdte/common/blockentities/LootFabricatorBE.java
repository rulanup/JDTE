package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import com.jdte.common.recipes.MachineRecipeHelper;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.mojang.authlib.GameProfile;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.LootFabricatorUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.ContainerDataEncoding;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.fluids.FluidStack;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.jdte.mixin.FluidTankAccessor;

import com.jdte.common.recipes.BioFactoryOutput;
import com.jdte.common.recipes.LootFabricatorRecipe;
import com.jdte.setup.JDTERecipes;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LootFabricatorBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, CoalescedAcceleratedMachine {
    private static final Identifier PRODUCTIVE_BEES_CONFIGURABLE_EGG =
            Identifier.fromNamespaceAndPath("productivebees", "spawn_egg_configurable_bee");
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int UPGRADE_SLOTS = 8;
    public static final int BASE_ENERGY_CAPACITY = 500_000;
    public static final int BASE_FLUID_CAPACITY = 64_000;
    public static final int DEFAULT_LIFE_FLUID_COST = 20;
    public static final int DEFAULT_BASE_TIME_FLUID_COST = 1;
    public static final int ENERGY_COST = 5_000;
    public static final int PROCESS_TIME = 20;
    public static final int MAX_TICK_SPEED = 1200;
    public static final int BOSS_COST_MULTIPLIER = 10;
    public static final int ENDER_DRAGON_COST_MULTIPLIER = 100;
    private static final String LOOTING_PLAYER_NAME = "[JDTE Loot Fabricator]";
    private static final GameProfile LOOTING_PLAYER_PROFILE = new GameProfile(
            UUID.nameUUIDFromBytes("jdte:loot_fabricator".getBytes(StandardCharsets.UTF_8)),
            LOOTING_PLAYER_NAME);

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank lifeFluidTank;
    public final JDTEFluidTank timeFluidTank;
    public final RedstoneControlData redstoneControlData = new RedstoneControlData();
    public final ContainerData machineData;
    private final MachineInventoryHandler itemHandler;
    private final LootFabricatorUpgradeItemStackHandler upgradeHandler;
    private final ResourceHandler<FluidResource> fluidHandler;
    private final ResourceHandler<ItemResource> automationItemHandler;
    private int progress;
    private int nextInputSlot;
    private int timeFluidCreditUnits;
    private long activeOutputSlotsTick = Long.MIN_VALUE;
    private int cachedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private boolean costCacheFresh;
    private boolean inputCacheDirty = true;
    private final boolean[] cachedValidInputs = new boolean[INPUT_SLOTS];
    private int costCacheTickSpeed = -1;
    private int costCacheUpgradeVersion = -1;
    private final ItemStack[] costCacheInputs = new ItemStack[INPUT_SLOTS];
    private final int[] cachedLifeFluidCosts = new int[INPUT_SLOTS];
    private final int[] cachedTimeFluidCostUnits = new int[INPUT_SLOTS];
    private int cachedEnergyCost;
    private int cachedProcessTime = PROCESS_TIME;
    private ItemStack cachedLootingWeapon = ItemStack.EMPTY;
    private int cachedWeaponLootingLevel = -1;
    private int syncedProcessTime = PROCESS_TIME;
    private int syncedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedLifeFluid;
    private int syncedTimeFluid;
    private int syncedFluidCapacity = BASE_FLUID_CAPACITY;

    public LootFabricatorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LOOT_FABRICATOR.get(), pos, state);
        MACHINE_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;
        tickSpeed = PROCESS_TIME;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
        lifeFluidTank = new JDTEFluidTank(BASE_FLUID_CAPACITY, fluid -> fluid.is(JDTEFluids.LIFE_FLUID_SOURCE.get()));
        timeFluidTank = new JDTEFluidTank(BASE_FLUID_CAPACITY, fluid -> fluid.getFluid() instanceof TimeFluid);
        itemHandler = new MachineInventoryHandler(MACHINE_SLOTS) {
            @Override protected int getCapacity(int slot, ItemResource resource) {
                return slot < INPUT_SLOTS ? 1 : Math.min(64, super.getCapacity(slot, resource));
            }
            @Override public boolean isValid(int slot, ItemResource resource) {
                return resource.isEmpty() || slot >= INPUT_SLOTS || isSupportedLootTemplate(resource.toStack(1));
            }
            @Override protected void onContentsChanged(int slot, ItemStack previous) {
                inputCacheDirty = true;
                costCacheFresh = false;
                setChanged();
            }
        };
        upgradeHandler = new LootFabricatorUpgradeItemStackHandler(this);
        fluidHandler = new CombinedResourceHandler<>(lifeFluidTank, timeFluidTank);
        automationItemHandler = new MachineAutomationHandler<>(itemHandler, slot -> slot < INPUT_SLOTS,
                slot -> slot >= INPUT_SLOTS && slot < INPUT_SLOTS + getActiveOutputSlots()) {
            @Override public int size() { return INPUT_SLOTS + getActiveOutputSlots(); }
        };
        machineData = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> isClientSide() ? syncedProcessTime : getProcessTime();
                    case 2 -> isClientSide() ? syncedActiveOutputSlots : getActiveOutputSlots();
                    case 3 -> ContainerDataEncoding.low16(isClientSide() ? syncedLifeFluid : lifeFluidTank.getFluidAmount());
                    case 4 -> ContainerDataEncoding.low16(isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount());
                    case 5 -> ContainerDataEncoding.low16(isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity());
                    case 6 -> ContainerDataEncoding.high16(isClientSide() ? syncedLifeFluid : lifeFluidTank.getFluidAmount());
                    case 7 -> ContainerDataEncoding.high16(isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount());
                    case 8 -> ContainerDataEncoding.high16(isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity());
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> syncedProcessTime = value;
                    case 2 -> syncedActiveOutputSlots = value;
                    case 3 -> syncedLifeFluid = ContainerDataEncoding.withLow16(syncedLifeFluid, value);
                    case 4 -> syncedTimeFluid = ContainerDataEncoding.withLow16(syncedTimeFluid, value);
                    case 5 -> syncedFluidCapacity = ContainerDataEncoding.withLow16(syncedFluidCapacity, value);
                    case 6 -> syncedLifeFluid = ContainerDataEncoding.withHigh16(syncedLifeFluid, value);
                    case 7 -> syncedTimeFluid = ContainerDataEncoding.withHigh16(syncedTimeFluid, value);
                    case 8 -> syncedFluidCapacity = ContainerDataEncoding.withHigh16(syncedFluidCapacity, value);
                    default -> { }
                }
            }
            @Override public int getCount() { return 9; }
        };
    }

    private boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    @Override public void tickServer() {
        super.tickServer();
        if (level != null && level.getGameTime() % 20L == 0L) syncFluidCapacities();
        if (!isActiveRedstone() || !canRun()) {
            resetProgress();
            return;
        }
        processLoot();
    }

    /**
     * Recomputes the per-template fluid/energy costs and the process time only when the
     * inputs, upgrade set, or tick speed actually changed; the per-tick resource check
     * below runs against these cached values instead of re-scanning upgrades.
     */
    private void refreshCostCache() {
        if (costCacheFresh && !inputCacheDirty && costCacheTickSpeed == tickSpeed
                && costCacheUpgradeVersion == upgradeHandler.getContentVersion()) {
            return;
        }
        costCacheFresh = true;
        inputCacheDirty = false;
        costCacheTickSpeed = tickSpeed;
        costCacheUpgradeVersion = upgradeHandler.getContentVersion();
        cachedEnergyCost = getEffectiveEnergyCost();
        cachedProcessTime = getProcessTime();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack stack = itemHandler.copyStack(slot);
            costCacheInputs[slot] = stack.copy();
            cachedValidInputs[slot] = isSupportedLootTemplate(stack);
            LootFabricatorRecipe recipe = findRecipe(stack);
            cachedLifeFluidCosts[slot] = stack.isEmpty() ? 0 : (recipe != null ? applyLootingFluidCostIncrease(recipe.lifeFluid(), getLootingLevel()) : getEffectiveLifeFluidCost(stack));
            cachedTimeFluidCostUnits[slot] = stack.isEmpty() ? 0 : (recipe != null ? applyLootingFluidCostIncrease(recipe.timeFluid(), getLootingLevel()) : getEffectiveTimeFluidCostUnits(stack));
        }
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
        processAcceleratedLoot(ticks);
    }

    private boolean hasFreeOutputSpace() {
        int active = getActiveOutputSlots();
        for (int i = 0; i < active; i++) {
            ItemStack stack = itemHandler.copyStack(INPUT_SLOTS + i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void processAcceleratedLoot(int ticks) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        refreshCostCache();
        int processTime = Math.max(1, cachedProcessTime);
        long totalProgress = (long) progress + ticks;
        long batches = totalProgress / processTime;
        progress = (int) (totalProgress % processTime);
        if (batches <= 0) {
            setChanged();
            return;
        }

        int maxBatches = (int) Math.min(batches, 128);
        for (int b = 0; b < maxBatches; b++) {
            if (!hasFreeOutputSpace()) {
                if (UpgradeHelper.hasAEOutputUpgrade(this)) {
                    com.jdte.common.manager.AEOutputManager.flush(serverLevel, this);
                }
                if (!hasFreeOutputSpace()) break;
            }
            int processCount = 0;
            int lifeFluidCost = 0;
            int timeFluidCostUnits = 0;
            int lastOccupiedSlot = -1;
            for (int i = 0; i < INPUT_SLOTS; i++) {
                int slot = (nextInputSlot + i) % INPUT_SLOTS;
                if (!cachedValidInputs[slot]) continue;
                lastOccupiedSlot = slot;
                processCount++;
                lifeFluidCost = safeAddCost(lifeFluidCost, cachedLifeFluidCosts[slot]);
                timeFluidCostUnits = safeAddCost(timeFluidCostUnits, cachedTimeFluidCostUnits[slot]);
            }
            LootFabricatorFluidCost.Settlement requiredTimeFluid =
                    LootFabricatorFluidCost.settle(timeFluidCostUnits, timeFluidCreditUnits);
            if (processCount == 0
                    || lifeFluidTank.getFluidAmount() < lifeFluidCost
                    || timeFluidTank.getFluidAmount() < requiredTimeFluid.drainMb()
                    || !hasEnoughPower(cachedEnergyCost * processCount)) {
                break;
            }
            if (!executeOneLootCycle(serverLevel, processCount, lifeFluidCost, timeFluidCostUnits, lastOccupiedSlot)) {
                break;
            }
            if (UpgradeHelper.hasAdvancedAEOutputUpgrade(this)) {
                com.jdte.common.manager.AEOutputManager.flush(serverLevel, this);
            }
        }
        setChanged();
    }

    private void processLoot() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        refreshCostCache();
        int processCount = 0;
        int lifeFluidCost = 0;
        int timeFluidCostUnits = 0;
        int lastOccupiedSlot = -1;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            int slot = (nextInputSlot + i) % INPUT_SLOTS;
            if (!cachedValidInputs[slot]) continue;
            lastOccupiedSlot = slot;
            processCount++;
            lifeFluidCost = safeAddCost(lifeFluidCost, cachedLifeFluidCosts[slot]);
            timeFluidCostUnits = safeAddCost(timeFluidCostUnits, cachedTimeFluidCostUnits[slot]);
        }
        LootFabricatorFluidCost.Settlement requiredTimeFluid =
                LootFabricatorFluidCost.settle(timeFluidCostUnits, timeFluidCreditUnits);
        if (processCount == 0
                || lifeFluidTank.getFluidAmount() < lifeFluidCost
                || timeFluidTank.getFluidAmount() < requiredTimeFluid.drainMb()
                || !hasEnoughPower(cachedEnergyCost * processCount)) {
            resetProgress();
            return;
        }
        progress++;
        if (progress < cachedProcessTime) {
            setChanged();
            return;
        }

        executeOneLootCycle(serverLevel, processCount, lifeFluidCost, timeFluidCostUnits, lastOccupiedSlot);
    }

    private boolean executeOneLootCycle(ServerLevel serverLevel, int processCount, int lifeFluidCost, int timeFluidCostUnits, int lastOccupiedSlot) {
        List<ItemStack> allDrops = new ArrayList<>();
        int successfulProcesses = 0;
        int successfulLifeFluidCost = 0;
        int successfulTimeFluidCostUnits = 0;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            int inputSlot = (nextInputSlot + i) % INPUT_SLOTS;
            if (!cachedValidInputs[inputSlot]) continue;
            ItemStack spawnEgg = costCacheInputs[inputSlot];
            List<ItemStack> drops = rollLoot(serverLevel, spawnEgg).stream()
                    .filter(drop -> !drop.isEmpty())
                    .toList();
            if (drops.isEmpty()) continue;
            allDrops.addAll(drops);
            successfulProcesses++;
            successfulLifeFluidCost = safeAddCost(successfulLifeFluidCost, cachedLifeFluidCosts[inputSlot]);
            successfulTimeFluidCostUnits = safeAddCost(successfulTimeFluidCostUnits, cachedTimeFluidCostUnits[inputSlot]);
        }
        if (successfulProcesses == 0 || allDrops.isEmpty()) {
            resetProgress();
            return false;
        }
        if (!itemHandler.insertBatch(INPUT_SLOTS, INPUT_SLOTS + getActiveOutputSlots(), allDrops, null)) {
            resetProgress();
            return false;
        }
        lifeFluidTank.extractFluid(successfulLifeFluidCost);
        LootFabricatorFluidCost.Settlement timeFluidSettlement = LootFabricatorFluidCost.settle(
                successfulTimeFluidCostUnits, timeFluidCreditUnits);
        timeFluidTank.extractFluid(timeFluidSettlement.drainMb());
        timeFluidCreditUnits = timeFluidSettlement.remainingCreditUnits();
        extractEnergy(cachedEnergyCost * successfulProcesses, false);
        nextInputSlot = (lastOccupiedSlot + 1) % INPUT_SLOTS;
        progress = 0;
        setChanged();
        return true;
    }

    private List<ItemStack> rollLoot(ServerLevel level, ItemStack eggStack) {
        LootFabricatorRecipe recipe = findRecipe(eggStack);
        if (recipe != null) {
            List<ItemStack> drops = new ArrayList<>();
            double lootingMult = 1.0 + getLootingLevel() * 0.5;
            for (BioFactoryOutput output : recipe.outputs()) {
                ItemStack rolled = output.roll(level.getRandom(), lootingMult);
                if (!rolled.isEmpty()) {
                    drops.add(rolled);
                }
            }
            return drops;
        }
        if (!(eggStack.getItem() instanceof SpawnEggItem)) return List.of();
        SpawnEggItem egg = (SpawnEggItem) eggStack.getItem();
        EntityType<?> type = egg.getType(eggStack);
        if (type == null) return List.of();
        Entity entity = type.create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        if (!(entity instanceof LivingEntity living)) return List.of();
        living.snapTo(getBlockPos().getCenter());
        // Loot tables read Looting from the attacking entity. The factory keeps one
        // dedicated player per level/profile and releases it when that level unloads.
        FakePlayer player = FakePlayerFactory.get(level, createLootingProfile());
        player.setItemInHand(InteractionHand.MAIN_HAND, createLootingWeapon(level));
        try {
            var damage = level.damageSources().playerAttack(player);
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, living)
                    .withParameter(LootContextParams.ORIGIN, living.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damage)
                    .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                    .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .create(LootContextParamSets.ENTITY);
            List<ItemStack> drops = new ArrayList<>();
            living.getLootTable().ifPresent(table -> drops.addAll(level.getServer().reloadableRegistries()
                    .getLootTable(table).getRandomItems(params)));
            addVanillaBossDrops(living, drops);
            return applyLootingBonus(level, drops);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            discardLootEntity(living);
        }
    }

    static void discardLootEntity(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    static GameProfile createLootingProfile() {
        return LOOTING_PLAYER_PROFILE;
    }

    @Override public boolean canRun() { return true; }

    private static void addVanillaBossDrops(LivingEntity living, List<ItemStack> drops) {
        if (living.getType() == EntityType.WITHER && drops.stream().noneMatch(stack -> stack.is(Items.NETHER_STAR))) {
            drops.add(new ItemStack(Items.NETHER_STAR));
        }
    }

    private List<ItemStack> applyLootingBonus(ServerLevel level, List<ItemStack> baseDrops) {
        if (getLootingLevel() <= 0 || baseDrops.isEmpty()) return baseDrops;
        List<ItemStack> originalDrops = baseDrops.stream().map(ItemStack::copy).toList();
        List<ItemStack> result = new ArrayList<>(originalDrops);
        double extraDropChance = JDTEConfig.COMMON.lootingExtraDropChance.get();
        for (int levelIndex = 0; levelIndex < getLootingLevel(); levelIndex++) {
            if (level.getRandom().nextDouble() < extraDropChance) {
                originalDrops.forEach(stack -> result.add(stack.copy()));
            }
        }
        return result;
    }

    private ItemStack createLootingWeapon(ServerLevel level) {
        int lootingLevel = getLootingLevel();
        if (lootingLevel == cachedWeaponLootingLevel) return cachedLootingWeapon;
        cachedWeaponLootingLevel = lootingLevel;
        ItemStack weapon = new ItemStack(Items.DIAMOND_SWORD);
        if (lootingLevel > 0) {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(level.registryAccess().holderOrThrow(Enchantments.LOOTING), lootingLevel);
            EnchantmentHelper.setEnchantments(weapon, enchantments.toImmutable());
        }
        cachedLootingWeapon = weapon;
        return weapon;
    }

    public LootFabricatorRecipe findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return null;
        for (RecipeHolder<LootFabricatorRecipe> holder : MachineRecipeHelper.recipes(level).byType(JDTERecipes.LOOT_FABRICATOR_RECIPE_TYPE.get())) {
            if (holder.value().matches(stack)) {
                return holder.value();
            }
        }
        return null;
    }

    private boolean isSupportedLootTemplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (findRecipe(stack) != null) return true;
        if (!(stack.getItem() instanceof SpawnEggItem)) return false;
        return !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(PRODUCTIVE_BEES_CONFIGURABLE_EGG);
    }

    private void resetProgress() {
        if (progress == 0) return;
        progress = 0;
        setChanged();
    }

    public int getActiveOutputSlots() {
        long gameTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (activeOutputSlotsTick == gameTick) return cachedActiveOutputSlots;
        int configured = BASE_OUTPUT_SLOTS + countCapacity(-1) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!itemHandler.copyStack(INPUT_SLOTS + i).isEmpty()) occupied = ((i / 16) + 1) * 16;
        }
        cachedActiveOutputSlots = Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
        activeOutputSlotsTick = gameTick;
        return cachedActiveOutputSlots;
    }
    public int getLootingLevel() { return upgradeHandler.getLootingCount(); }
    public int getProcessTime() { return Math.clamp(UpgradeHelper.getEffectiveTickSpeed(this, tickSpeed), 1, MAX_TICK_SPEED); }
    private int getTimeFluidCostUnits() { return getConfiguredBaseTimeFluidCost() * Math.max(1, PROCESS_TIME / getProcessTime()); }
    private int getEffectiveLifeFluidCost(ItemStack spawnEgg) { return applyLootingFluidCostIncrease(getLifeFluidCost(spawnEgg), getLootingLevel()); }
    private int getEffectiveTimeFluidCostUnits(ItemStack spawnEgg) { return applyLootingFluidCostIncrease(safeMultiplyCost(getTimeFluidCostUnits(), getBossCostMultiplier(spawnEgg)), getLootingLevel()); }
    public static int getConfiguredLifeFluidCost() { return Math.max(1, JDTEConfig.COMMON.lootFabricatorLifeFluidCost.get()); }
    public static int getConfiguredBaseTimeFluidCost() { return Math.max(1, JDTEConfig.COMMON.lootFabricatorBaseTimeFluidCost.get()); }
    public static int getConfiguredLootingFluidCostIncreasePercent() { return Math.max(0, JDTEConfig.COMMON.lootFabricatorLootingFluidCostIncreasePercent.get()); }
    public static int getLifeFluidCost(ItemStack spawnEgg) { return safeMultiplyCost(getConfiguredLifeFluidCost(), getBossCostMultiplier(spawnEgg)); }
    public static int getBaseTimeFluidCostUnits(ItemStack spawnEgg) { return safeMultiplyCost(getConfiguredBaseTimeFluidCost(), getBossCostMultiplier(spawnEgg)); }
    public static int getMaxTimeFluidCostUnits(ItemStack spawnEgg) { return safeMultiplyCost(getBaseTimeFluidCostUnits(spawnEgg), PROCESS_TIME); }
    public static int getTimeFluidDisplayAmount(int costUnits) {
        return LootFabricatorFluidCost.displayAmount(costUnits);
    }
    public static String formatTimeFluidCost(int costUnits) {
        return LootFabricatorFluidCost.format(costUnits);
    }
    public static int getBossCostMultiplier(ItemStack spawnEgg) {
        if (!(spawnEgg.getItem() instanceof SpawnEggItem egg)) return 1;
        EntityType<?> type = egg.getType(spawnEgg);
        if (type == EntityType.ENDER_DRAGON) return ENDER_DRAGON_COST_MULTIPLIER;
        if (type == EntityType.WITHER || type == EntityType.ELDER_GUARDIAN) return BOSS_COST_MULTIPLIER;
        return 1;
    }
    private static int applyLootingFluidCostIncrease(int cost, int lootingLevel) {
        int percent = safeAddCost(100, safeMultiplyCost(Math.max(0, lootingLevel), getConfiguredLootingFluidCostIncreasePercent()));
        return applyPercentCost(cost, percent);
    }
    private static int applyPercentCost(int cost, int percent) {
        if (cost <= 0 || percent <= 0) return 0;
        long result = ((long) cost * percent + 99L) / 100L;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }
    private static int safeAddCost(int left, int right) {
        if (left >= Integer.MAX_VALUE - right) return Integer.MAX_VALUE;
        return left + right;
    }
    private static int safeMultiplyCost(int value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Integer.MAX_VALUE / multiplier) return Integer.MAX_VALUE;
        return value * multiplier;
    }
    private static int clampRawTickSpeed(int value) { return Math.clamp(value, 1, MAX_TICK_SPEED); }
    private int countCapacity(int ignored) { return UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY); }

    @Override public ItemStacksResourceHandler getMachineHandler() { return itemHandler; }
    public LootFabricatorUpgradeItemStackHandler getUpgradeHandler() { return upgradeHandler; }
    public JDTEFluidTank getLifeFluidTank() { return lifeFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public ResourceHandler<FluidResource> getFluidHandler() { return fluidHandler; }
    public ResourceHandler<ItemResource> getAutomationItemHandler() { return automationItemHandler; }
    public ContainerData getMachineData() { return machineData; }
    public int getMaxFluidCapacity() { return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY); }
    public int getEffectiveEnergyCost() { return UpgradeHelper.adjustEnergyCost(this, ENERGY_COST); }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, BASE_ENERGY_CAPACITY); }
    @Override public ContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public int getStandardEnergyCost() { return ENERGY_COST; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }
    @Override public BlockEntity getBlockEntity() { return this; }

    private void syncFluidCapacities() {
        int capacity = getMaxFluidCapacity();
        syncTankCapacity(lifeFluidTank, capacity);
        syncTankCapacity(timeFluidTank, capacity);
    }

    private void syncTankCapacity(JDTEFluidTank tank, int capacity) {
        if (tank instanceof FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
        }
    }

    @Override public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", itemHandler);
        tag.putChild("upgrades", upgradeHandler);
        tag.putChild("lifeFluid", lifeFluidTank);
        tag.putChild("timeFluid", timeFluidTank);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("nextInputSlot", nextInputSlot);
        tag.putInt("timeFluidCreditUnits", timeFluidCreditUnits);
    }

    @Override public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("inventory")) itemHandler.deserialize(tag.childOrEmpty("inventory"));
        if (tag.keySet().contains("upgrades")) upgradeHandler.deserialize(tag.childOrEmpty("upgrades"));
        if (tag.keySet().contains("lifeFluid")) lifeFluidTank.deserialize(tag.childOrEmpty("lifeFluid"));
        if (tag.keySet().contains("timeFluid")) timeFluidTank.deserialize(tag.childOrEmpty("timeFluid"));
        energyStorage.setEnergy(tag.getIntOr("energy", 0));
        progress = tag.getIntOr("progress", 0);
        nextInputSlot = tag.getIntOr("nextInputSlot", 0);
        timeFluidCreditUnits = Math.clamp(
                tag.getIntOr("timeFluidCreditUnits", 0), 0, LootFabricatorFluidCost.UNITS_PER_MB - 1);
        if (!tag.keySet().contains("tickspeed") && tag.keySet().contains("tickSpeed")) tickSpeed = tag.getIntOr("tickSpeed", 0);
        tickSpeed = clampRawTickSpeed(tickSpeed);
        syncFluidCapacities();
        costCacheFresh = false;
        inputCacheDirty = true;
    }
}
