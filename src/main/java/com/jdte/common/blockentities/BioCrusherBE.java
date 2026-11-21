package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.integrations.ApothicSpawnerIntegration;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.BioCrusherDropEvents;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTETags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BioCrusherBE extends BaseMachineBE implements RedstoneControlledBE, AreaAffectingBE, FluidMachineBE, CoalescedAcceleratedMachine {
    public static final int MODE_HOSTILE = 0;
    public static final int MODE_FRIENDLY = 1;
    public static final int MODE_ALL = 2;
    public static final int BASE_OUTPUT_SLOT_COUNT = 18;
    public static final int BASE_OUTPUT_SLOTS_PER_CAPACITY_UPGRADE = 9;
    public static final int MAX_OUTPUT_SLOT_COUNT = BASE_OUTPUT_SLOT_COUNT + UpgradeType.CAPACITY.getMaxPerMachine() * BASE_OUTPUT_SLOTS_PER_CAPACITY_UPGRADE * 10;
    public static final int OUTPUT_SLOT_COUNT = BASE_OUTPUT_SLOT_COUNT;
    public static final int OUTPUT_SLOTS_PER_PAGE = 9;
    public static final int DEDICATED_UPGRADE_SLOT_COUNT = 2;

    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public final ContainerData bioCrusherData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected MachineInventoryHandler itemHandler;
    protected final ItemStackHandler lootingHandler;
    protected final ItemStackHandler sharpnessHandler;
    protected int mode = MODE_HOSTILE;
    protected int tickCounter = 0;

    protected BioCrusherBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = (createsOutputInventory() ? OUTPUT_SLOTS_PER_PAGE : 0) + DEDICATED_UPGRADE_SLOT_COUNT;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        areaAffectingData.xRadius = JDTEConfig.COMMON.bioCrusherBaseRadius.get();
        areaAffectingData.yRadius = JDTEConfig.COMMON.bioCrusherBaseRadius.get();
        areaAffectingData.zRadius = JDTEConfig.COMMON.bioCrusherBaseRadius.get();
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> f.is(JDTRegistration.XP_FLUID_SOURCE.get()));
        fluidContainerData = new FluidContainerData(this);

        itemHandler = createOutputHandler(createsOutputInventory() ? getMaxOutputSlotCount() : 0);

        // Dedicated upgrade handlers
        lootingHandler = new ItemStackHandler(JDTEConfig.COMMON.maxLootingUpgrades.get()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof LootingUpgradeItem;
            }
        };

        sharpnessHandler = new ItemStackHandler(JDTEConfig.COMMON.maxSharpnessUpgrades.get()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof SharpnessUpgradeItem;
            }
        };

        bioCrusherData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> mode;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) mode = value;
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    protected boolean createsOutputInventory() {
        return false;
    }

    public boolean hasOutputInventory() {
        return createsOutputInventory();
    }

    public int getActiveOutputSlotCount() {
        if (!hasOutputInventory()) {
            return 0;
        }
        int configuredSlots = UpgradeHelper.hasUltimateCapacity(this)
                ? getMaxOutputSlotCount()
                : BASE_OUTPUT_SLOT_COUNT + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * getOutputSlotsPerCapacityUpgrade();
        return Math.clamp(Math.max(configuredSlots, getOccupiedOutputSlotCount()), BASE_OUTPUT_SLOT_COUNT, getMaxOutputSlotCount());
    }

    private int getOccupiedOutputSlotCount() {
        int occupiedSlots = 0;
        for (int i = 0; i < itemHandler.size(); i++) {
            if (!itemHandler.copyStack(i).isEmpty()) {
                occupiedSlots = i + 1;
            }
        }
        if (occupiedSlots <= BASE_OUTPUT_SLOT_COUNT) {
            return BASE_OUTPUT_SLOT_COUNT;
        }
        return Math.min(getMaxOutputSlotCount(),
                ((occupiedSlots + OUTPUT_SLOTS_PER_PAGE - 1) / OUTPUT_SLOTS_PER_PAGE) * OUTPUT_SLOTS_PER_PAGE);
    }

    private static int getOutputSlotsPerCapacityUpgrade() {
        return BASE_OUTPUT_SLOTS_PER_CAPACITY_UPGRADE * JDTEConfig.COMMON.bioCrusherOutputSlotsPerCapacityUpgradeMultiplier.get();
    }

    private static int getMaxOutputSlotCount() {
        return BASE_OUTPUT_SLOT_COUNT + UpgradeType.CAPACITY.getMaxPerMachine() * getOutputSlotsPerCapacityUpgrade();
    }

    public ResourceHandler<ItemResource> getOutputItemHandler() {
        return new MachineAutomationHandler<>(itemHandler, slot -> false, slot -> slot < getActiveOutputSlotCount()) {
            @Override public int size() { return getActiveOutputSlotCount(); }
        };
    }

    public ItemStackHandler getLootingHandler() {
        return lootingHandler;
    }

    public ItemStackHandler getSharpnessHandler() {
        return sharpnessHandler;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        syncAreaRadius();
        if (isActiveRedstone() && canRun()) {
            processCrushing();
        }
    }

    @Override
    public boolean canRun() {
        return true;
    }

    protected abstract int getExtractInterval();

    protected abstract int getMaxEntitiesPerTick();

    public abstract int getEffectiveEnergyCost();

    public abstract boolean hasEnoughPower(int energyCost);

    public abstract int extractEnergy(int energy, boolean simulate);

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
        processAcceleratedCrushing(ticks);
    }

    protected void processAcceleratedCrushing(int ticks) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int interval = Math.max(1, getExtractInterval());
        long totalProgress = (long) tickCounter + ticks;
        long cycles = totalProgress / interval;
        tickCounter = (int) (totalProgress % interval);
        if (cycles <= 0) {
            setChanged();
            return;
        }

        int energyCost = getEffectiveEnergyCost();
        int maxCycles = (int) Math.min(cycles, 64);
        for (int i = 0; i < maxCycles; i++) {
            if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
                break;
            }
            int processed = executeOneCrushCycle(serverLevel, energyCost);
            if (processed == 0) {
                break;
            }
        }
        setChanged();
    }

    protected void processCrushing() {
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

        executeOneCrushCycle(serverLevel, energyCost);
    }

    protected int executeOneCrushCycle(ServerLevel serverLevel, int energyCost) {
        AABB area = getAABB(getBlockPos());
        int processed = 0;
        int maxEntities = getMaxEntitiesPerTick();

        for (Entity entity : serverLevel.getEntitiesOfClass(Entity.class, area, e -> isValidTarget(e))) {
            if (processed >= maxEntities) break;
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            int experience = killEntity(serverLevel, livingEntity);
            if (experience < 0) continue;

            fillExperienceFluid(experience);
            processed++;
        }

        if (processed > 0 && !UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(energyCost, false);
        }

        if (processed > 0) {
            setChanged();
        }
        return processed;
    }

    protected boolean isValidTarget(Entity entity) {
        if (entity instanceof Player) return false;
        if (entity.isSpectator()) return false;
        if (entity.getType().builtInRegistryHolder().is(JDTETags.BIO_CRUSHER_BLACKLIST)) return false;

        if (!(entity instanceof LivingEntity livingEntity) || livingEntity.isDeadOrDying()) return false;

        return switch (mode) {
            case MODE_HOSTILE -> isHostileTarget(entity);
            case MODE_FRIENDLY -> !isHostileTarget(entity);
            case MODE_ALL -> true;
            default -> false;
        };
    }

    protected int killEntity(ServerLevel serverLevel, LivingEntity entity) {
        FakePlayer fakePlayer = getFakePlayer(serverLevel);
        ItemStack previousMainHand = fakePlayer.getMainHandItem().copy();
        ItemStack weapon = createLootingWeapon(serverLevel);
        DamageSource playerDamage = serverLevel.damageSources().playerAttack(fakePlayer);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        try {
            BioCrusherDropEvents.CaptureResult<Boolean> result = BioCrusherDropEvents.capture(entity, () -> {
                // Vanilla EnderDragon.hurt() intercepts lethal damage (locks health at 1 and enters the
                // DYING animation phase, which never finishes outside an end fight), so kill it directly.
                if (entity instanceof EnderDragon dragon) {
                    createForcedDrops(serverLevel, entity, playerDamage);
                    removeEnderDragon(dragon);
                    BioCrusherDropEvents.captureExperienceIfAbsent(serverLevel, entity, fakePlayer);
                    return true;
                }
                entity.hurtServer(serverLevel, playerDamage, calculateDamage());
                if (!entity.isDeadOrDying()) {
                    if (JDTEConfig.COMMON.bioCrusherRespectDamageRestrictions.get()
                            || entity.getType().builtInRegistryHolder().is(JDTETags.BIO_CRUSHER_FORCE_KILL_BLACKLIST)) {
                        return false;
                    }

                    entity.setHealth(0.0F);
                    entity.die(playerDamage);
                }
                BioCrusherDropEvents.captureExperienceIfAbsent(serverLevel, entity, fakePlayer);
                return entity.isDeadOrDying() || entity.isRemoved();
            });

            if (!result.value()) return -1;
            applyLootingBonus(serverLevel, result.drops())
                    .forEach(stack -> addItemToInventory(stack, entity.position()));
            tryDropBossEssence(serverLevel, entity, getLootingLevel());
            return result.experience();
        } finally {
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, previousMainHand);
        }
    }

    private static void removeEnderDragon(EnderDragon dragon) {
        if (dragon.getDragonFight() != null) {
            dragon.getDragonFight().setDragonKilled(dragon);
        }
        dragon.setHealth(0.0F);
        dragon.remove(Entity.RemovalReason.KILLED);
    }

    private void fillExperienceFluid(long experience) {
        double multiplier = JDTEConfig.COMMON.bioCrusherExperienceFluidMultiplier.get();
        long fluidProduced = Math.round(Math.max(0L, experience) * multiplier);
        if (fluidProduced <= 0) return;

        fluidTank.insertFluid(
                new FluidStack(JDTRegistration.XP_FLUID_SOURCE.get(), (int) Math.min(fluidProduced, Integer.MAX_VALUE)));
    }

    private ItemStack createLootingWeapon(ServerLevel serverLevel) {
        ItemStack weapon = new ItemStack(Items.DIAMOND_SWORD);
        int lootingLevel = getLootingLevel();
        if (lootingLevel <= 0) return weapon;

        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(serverLevel.registryAccess().holderOrThrow(Enchantments.LOOTING), lootingLevel);
        EnchantmentHelper.setEnchantments(weapon, enchantments.toImmutable());
        return weapon;
    }

    private List<ItemStack> applyLootingBonus(ServerLevel serverLevel, Collection<ItemStack> baseDrops) {
        List<ItemStack> originalDrops = baseDrops.stream().map(ItemStack::copy).toList();
        List<ItemStack> result = new ArrayList<>(originalDrops);
        double extraDropChance = JDTEConfig.COMMON.lootingExtraDropChance.get();

        for (int level = 0; level < getLootingLevel(); level++) {
            if (serverLevel.getRandom().nextDouble() < extraDropChance) {
                originalDrops.forEach(stack -> result.add(stack.copy()));
            }
        }
        return result;
    }

    private void createForcedDrops(ServerLevel serverLevel, LivingEntity entity, DamageSource source) {
        Collection<ItemEntity> drops = new ArrayList<>();
        try {
            var lootTable = entity.getLootTable().map(serverLevel.getServer().reloadableRegistries()::getLootTable)
                    .orElse(net.minecraft.world.level.storage.loot.LootTable.EMPTY);
            net.minecraft.world.level.storage.loot.LootParams params = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, entity)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, entity.position())
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, source)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ATTACKING_ENTITY, source.getEntity())
                    .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.LAST_DAMAGE_PLAYER, (Player) source.getEntity())
                    .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);

            for (ItemStack stack : lootTable.getRandomItems(params)) {
                drops.add(new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), stack));
            }
            if (CommonHooks.onLivingDrops(entity, source, drops, true)) {
                drops.clear();
            }
        } catch (RuntimeException ignored) {
            drops.clear();
        }
    }

    private boolean isHostileTarget(Entity entity) {
        EntityType<?> type = entity.getType();
        return entity instanceof Enemy
                || type.getCategory() == MobCategory.MONSTER;
    }

    protected int calculateDamage() {
        int damage = JDTEConfig.COMMON.bioCrusherBaseDamage.get();
        int sharpnessCount = countDedicatedUpgrades(sharpnessHandler, JDTEConfig.COMMON.maxSharpnessUpgrades.get());
        damage += sharpnessCount * JDTEConfig.COMMON.sharpnessDamagePerUpgrade.get();
        return damage;
    }

    protected int getLootingLevel() {
        return countDedicatedUpgrades(lootingHandler, JDTEConfig.COMMON.maxLootingUpgrades.get());
    }

    private int countDedicatedUpgrades(ItemStackHandler handler, int max) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            count += handler.getStackInSlot(i).getCount();
        }
        return Math.min(count, max);
    }

    protected boolean tryDropBossEssence(ServerLevel serverLevel, LivingEntity entity, int lootingLevel) {
        net.minecraft.world.item.Item essenceItem = null;

        if (entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
            essenceItem = com.jdte.setup.JDTEItems.WITHER_ESSENCE.get();
        } else if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            essenceItem = com.jdte.setup.JDTEItems.ENDER_DRAGON_ESSENCE.get();
        } else if (entity instanceof net.minecraft.world.entity.monster.ElderGuardian) {
            essenceItem = com.jdte.setup.JDTEItems.ELDER_GUARDIAN_ESSENCE.get();
        }

        if (essenceItem == null) {
            return false;
        }

        if (entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                && serverLevel.getRandom().nextFloat() >= 0.05F) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                && serverLevel.getRandom().nextFloat() >= 0.10F) {
            return false;
        }

        // Successful boss essence rolls drop 1; looting can add extra.
        int count = 1;
        for (int i = 0; i < lootingLevel; i++) {
            if (level.getRandom().nextFloat() < 0.5f) {
                count++;
            }
        }

        ItemStack essenceStack = new ItemStack(essenceItem, count);
        addItemToInventory(essenceStack, entity.position());

        return true;
    }

    protected void addItemToInventory(ItemStack stack, Vec3 sourcePosition) {
        if (stack.isEmpty()) return;

        if (hasOutputInventory()) {
            int activeSlots = getActiveOutputSlotCount();
            for (int i = 0; i < activeSlots; i++) {
                ItemStack existing = itemHandler.copyStack(i);
                if (existing.isEmpty()) {
                    itemHandler.setStack(i, stack.copy());
                    return;
                } else if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
                    existing.grow(stack.getCount());
                    itemHandler.setStack(i, existing);
                    return;
                }
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            if (hasOutputInventory()) {
                Containers.dropItemStack(
                        serverLevel,
                        getBlockPos().getX() + 0.5D,
                        getBlockPos().getY() + 1.5D,
                        getBlockPos().getZ() + 0.5D,
                        stack.copy());
            } else {
                Containers.dropItemStack(
                        serverLevel,
                        sourcePosition.x,
                        sourcePosition.y,
                        sourcePosition.z,
                        stack.copy());
            }
        }
    }

    public boolean processSpawnerCrush(ServerLevel serverLevel, BlockPos spawnerPos, BaseSpawner spawner, SpawnData nextSpawnData, int spawnCount) {
        // Check energy
        int energyCost = getEffectiveEnergyCost();
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            return false;
        }

        // Get entity type from nextSpawnData instead of displayEntity
        // This is more reliable for modded spawners that use SpawnPotentials
        if (nextSpawnData == null) {
            return false;
        }

        BlockEntity spawnerBlockEntity = serverLevel.getBlockEntity(spawnerPos);
        if (ModList.get().isLoaded("apothic_spawners")) {
            spawnCount = ApothicSpawnerIntegration.getSpawnCount(spawnerBlockEntity, spawnCount);
        }
        if (spawnCount <= 0) return false;

        long totalExperience = 0;
        int processed = 0;
        for (int i = 0; i < spawnCount; i++) {
            int experience = generateSpawnerDrops(serverLevel, spawnerPos, spawner, nextSpawnData);
            if (experience < 0) continue;

            totalExperience += experience;
            processed++;
        }

        if (processed == 0) return false;

        fillExperienceFluid(totalExperience);

        // Extract energy
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(energyCost, false);
        }

        setChanged();
        return true;
    }

    protected int generateSpawnerDrops(ServerLevel serverLevel, BlockPos spawnerPos, BaseSpawner spawner, SpawnData spawnData) {
        Entity virtualEntity = EntityType.loadEntityRecursive(spawnData.getEntityToSpawn().copy(), serverLevel, EntitySpawnReason.SPAWNER, entity -> {
            entity.snapTo(spawnerPos.getX() + 0.5D, spawnerPos.getY() + 0.5D, spawnerPos.getZ() + 0.5D, entity.getYRot(), entity.getXRot());
            return entity;
        });
        if (!(virtualEntity instanceof LivingEntity livingEntity)) return -1;

        BlockEntity spawnerBlockEntity = serverLevel.getBlockEntity(spawnerPos);
        if (ModList.get().isLoaded("apothic_spawners")) {
            ApothicSpawnerIntegration.applySpawnModifiers(spawnerBlockEntity, virtualEntity);
        }

        if (livingEntity instanceof Mob mob) {
            boolean vanillaFinalizeSpawn = spawnData.getEntityToSpawn().size() == 1
                    && spawnData.getEntityToSpawn().getString("id").isPresent();
            EventHooks.finalizeMobSpawnSpawner(
                    mob,
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(spawnerPos),
                    EntitySpawnReason.SPAWNER,
                    null,
                    spawner,
                    vanillaFinalizeSpawn);
            spawnData.getEquipment().ifPresent(mob::equip);
        }

        FakePlayer fakePlayer = getFakePlayer(serverLevel);
        ItemStack previousMainHand = fakePlayer.getMainHandItem().copy();
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, createLootingWeapon(serverLevel));

        try {
            DamageSource playerDamage = serverLevel.damageSources().playerAttack(fakePlayer);
            BioCrusherDropEvents.CaptureResult<Boolean> result = BioCrusherDropEvents.capture(livingEntity, () -> {
                if (livingEntity instanceof EnderDragon dragon) {
                    createForcedDrops(serverLevel, livingEntity, playerDamage);
                    removeEnderDragon(dragon);
                    BioCrusherDropEvents.captureExperienceIfAbsent(serverLevel, livingEntity, fakePlayer);
                    return true;
                }
                livingEntity.hurt(playerDamage, Float.MAX_VALUE);
                if (!livingEntity.isDeadOrDying()) {
                    if (JDTEConfig.COMMON.bioCrusherRespectDamageRestrictions.get()
                            || livingEntity.getType().builtInRegistryHolder().is(JDTETags.BIO_CRUSHER_FORCE_KILL_BLACKLIST)) {
                        return false;
                    }
                    livingEntity.setHealth(0.0F);
                    livingEntity.die(playerDamage);
                }
                BioCrusherDropEvents.captureExperienceIfAbsent(serverLevel, livingEntity, fakePlayer);
                return livingEntity.isDeadOrDying() || livingEntity.isRemoved();
            });
            if (!result.value()) return -1;

            applyLootingBonus(serverLevel, result.drops())
                    .forEach(stack -> addItemToInventory(stack, livingEntity.position()));
            tryDropBossEssence(serverLevel, livingEntity, getLootingLevel());
            return result.experience();
        } catch (RuntimeException ignored) {
            return -1;
        } finally {
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, previousMainHand);
        }
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

    public ContainerData getBioCrusherData() {
        return bioCrusherData;
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.bioCrusherFluidCapacity.get());
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
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (hasOutputInventory()) {
            tag.putChild("inventory", itemHandler);
        }
        tag.putChild("fluidTank", fluidTank);
        tag.putChild("lootingHandler", lootingHandler);
        tag.putChild("sharpnessHandler", sharpnessHandler);
        tag.putInt("mode", mode);
        tag.putInt("tickCounter", tickCounter);
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (hasOutputInventory() && tag.keySet().contains("inventory")) {
            itemHandler.deserialize(tag.childOrEmpty("inventory"));
            ensureOutputHandlerSize();
        }
        tag.child("fluidTank").ifPresent(fluidTank::deserialize);
        if (tag.keySet().contains("lootingHandler")) {
            lootingHandler.deserialize(tag.childOrEmpty("lootingHandler"));
            compactDedicatedUpgradeHandler(lootingHandler, JDTEConfig.COMMON.maxLootingUpgrades.get());
        }
        if (tag.keySet().contains("sharpnessHandler")) {
            sharpnessHandler.deserialize(tag.childOrEmpty("sharpnessHandler"));
            compactDedicatedUpgradeHandler(sharpnessHandler, JDTEConfig.COMMON.maxSharpnessUpgrades.get());
        }
        mode = tag.getIntOr("mode", mode);
        tickCounter = tag.getIntOr("tickCounter", tickCounter);
        loadAreaSettings(tag);
    }

    private void compactDedicatedUpgradeHandler(ItemStackHandler handler, int maxCount) {
        ItemStack template = ItemStack.EMPTY;
        int total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (template.isEmpty()) {
                template = stack.copyWithCount(1);
            }
            total += stack.getCount();
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
        if (!template.isEmpty() && total > 0) {
            int count = Math.min(total, Math.min(maxCount, template.getMaxStackSize()));
            handler.setStackInSlot(0, template.copyWithCount(count));
        }
    }

    private MachineInventoryHandler createOutputHandler(int slots) {
        return new MachineInventoryHandler(slots) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                setChanged();
            }
        };
    }

    private void ensureOutputHandlerSize() {
        if (!hasOutputInventory()) {
            return;
        }
        if (itemHandler.size() >= getMaxOutputSlotCount()) {
            return;
        }
        MachineInventoryHandler resized = createOutputHandler(getMaxOutputSlotCount());
        for (int i = 0; i < itemHandler.size(); i++) {
            ItemStack stack = itemHandler.copyStack(i);
            if (!stack.isEmpty()) {
                resized.setStack(i, stack.copy());
            }
        }
        itemHandler = resized;
        setChanged();
    }
}
