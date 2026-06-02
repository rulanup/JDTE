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
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class BioCrusherBE extends BaseMachineBE implements RedstoneControlledBE, AreaAffectingBE, FluidMachineBE {
    public static final int MODE_HOSTILE = 0;
    public static final int MODE_FRIENDLY = 1;
    public static final int MODE_ALL = 2;
    public static final int BASE_FLUID_CAPACITY = 16000;
    public static final int BASE_ENERGY_COST = 300;
    public static final float BASE_RADIUS = 2.5f;
    public static final int FLUID_PER_HP = 100;
    public static final int BASE_DAMAGE = 5;
    public static final int PROCESS_TIME = 20;

    // Cache for entity max health to avoid creating entities every time
    private static final java.util.Map<net.minecraft.world.entity.EntityType<?>, Float> HEALTH_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public final ContainerData bioCrusherData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    public AreaAffectingData areaAffectingData;
    protected final ItemStackHandler itemHandler;
    protected final ItemStackHandler lootingHandler;
    protected final ItemStackHandler sharpnessHandler;
    protected int mode = MODE_HOSTILE;
    protected int tickCounter = 0;
    protected int progress = 0;

    protected BioCrusherBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = 0;
        areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
        areaAffectingData.xRadius = BASE_RADIUS;
        areaAffectingData.yRadius = BASE_RADIUS;
        areaAffectingData.zRadius = BASE_RADIUS;
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> f.is(JDTEFluids.LIFE_FLUID_SOURCE.get()));
        fluidContainerData = new FluidContainerData(this);

        // Output handler for drops
        itemHandler = new ItemStackHandler(27) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };

        // Dedicated upgrade handlers
        lootingHandler = new ItemStackHandler(LootingUpgradeItem.MAX_LEVEL) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof LootingUpgradeItem;
            }
        };

        sharpnessHandler = new ItemStackHandler(SharpnessUpgradeItem.MAX_COUNT) {
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
                    case 1 -> progress;
                    case 2 -> PROCESS_TIME;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) mode = value;
                if (index == 1) progress = value;
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public ItemStackHandler getMachineHandler() {
        return itemHandler;
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

        AABB area = getAABB(getBlockPos());
        int processed = 0;
        int maxEntities = getMaxEntitiesPerTick();
        long totalFluidProduced = 0;

        for (Entity entity : serverLevel.getEntitiesOfClass(Entity.class, area, e -> isValidTarget(e))) {
            if (processed >= maxEntities) break;
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            float maxHealth = livingEntity.getMaxHealth();
            if (maxHealth <= 0) continue;

            // Calculate XP fluid production
            long fluidProduced = (long) (maxHealth * FLUID_PER_HP);
            if (fluidProduced > 0 && fluidTank.fill(new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), (int) Math.min(fluidProduced, Integer.MAX_VALUE)), IFluidHandler.FluidAction.SIMULATE) > 0) {
                fluidTank.fill(new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), (int) Math.min(fluidProduced, Integer.MAX_VALUE)), IFluidHandler.FluidAction.EXECUTE);
                totalFluidProduced += fluidProduced;
            }

            // Calculate damage with sharpness upgrades
            int damage = calculateDamage();

            // Kill the entity
            entity.hurt(serverLevel.damageSources().generic(), damage);
            if (entity.isAlive()) {
                entity.discard();
            }

            // Generate drops with looting
            generateDrops(livingEntity);
            processed++;
        }

        if (totalFluidProduced > 0 && !UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(energyCost, false);
        }

        if (processed > 0) {
            setChanged();
        }
    }

    protected boolean isValidTarget(Entity entity) {
        if (entity instanceof Player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity.isSpectator()) return false;

        return switch (mode) {
            case MODE_HOSTILE -> entity instanceof Mob mob && mob.isAggressive();
            case MODE_FRIENDLY -> !(entity instanceof Mob mob && mob.isAggressive());
            case MODE_ALL -> true;
            default -> false;
        };
    }

    protected int calculateDamage() {
        int damage = BASE_DAMAGE;
        int sharpnessCount = 0;
        for (int i = 0; i < sharpnessHandler.getSlots(); i++) {
            if (!sharpnessHandler.getStackInSlot(i).isEmpty()) {
                sharpnessCount++;
            }
        }
        damage += sharpnessCount * SharpnessUpgradeItem.DAMAGE_PER_UPGRADE;
        return damage;
    }

    protected int getLootingLevel() {
        int level = 0;
        for (int i = 0; i < lootingHandler.getSlots(); i++) {
            ItemStack stack = lootingHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                level++;
            }
        }
        return level;
    }

    protected void generateDrops(LivingEntity entity) {
        // This is a simplified drop generation
        // In a real implementation, you would use the entity's loot table
        // For now, we'll just generate some basic drops based on the entity type

        int lootingLevel = getLootingLevel();

        // Calculate extra drops from looting
        int extraDrops = 0;
        for (int i = 0; i < lootingLevel; i++) {
            if (level.random.nextFloat() < 0.5f) {
                extraDrops++;
            }
        }

        // Generate drops (simplified - in real implementation, use loot tables)
        // For now, just add some placeholder items
        // In a full implementation, you would call entity.getLootTable() and process it
    }

    public void processSpawnerCrush(ServerLevel serverLevel, BlockPos spawnerPos, com.jdte.mixin.BaseSpawnerAccessor accessor) {
        // Check energy
        int energyCost = getEffectiveEnergyCost();
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            return;
        }

        // Get entity type from nextSpawnData instead of displayEntity
        // This is more reliable for modded spawners that use SpawnPotentials
        SpawnData nextSpawnData = accessor.jdte$getNextSpawnData();
        if (nextSpawnData == null) {
            return;
        }

        net.minecraft.nbt.CompoundTag entityTag = nextSpawnData.getEntityToSpawn();
        java.util.Optional<net.minecraft.world.entity.EntityType<?>> entityTypeOpt = net.minecraft.world.entity.EntityType.by(entityTag);
        if (entityTypeOpt.isEmpty()) {
            return;
        }

        net.minecraft.world.entity.EntityType<?> entityType = entityTypeOpt.get();

        // Get spawn count from the spawner
        int spawnCount = accessor.jdte$getSpawnCount();

        // Get max health from cache or calculate it
        float maxHealth = HEALTH_CACHE.computeIfAbsent(entityType, type -> {
            try {
                if (type.create(serverLevel) instanceof LivingEntity livingEntity) {
                    return livingEntity.getMaxHealth();
                }
            } catch (Exception e) {
                // If we can't create the entity, use default health
            }
            return 20.0f;
        });

        // Produce life fluid (scaled by spawn count)
        long fluidProduced = (long) (maxHealth * FLUID_PER_HP * spawnCount);
        if (fluidProduced > 0 && fluidTank.fill(new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), (int) Math.min(fluidProduced, Integer.MAX_VALUE)), IFluidHandler.FluidAction.SIMULATE) > 0) {
            fluidTank.fill(new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), (int) Math.min(fluidProduced, Integer.MAX_VALUE)), IFluidHandler.FluidAction.EXECUTE);
        }

        // Generate drops using the entity's loot table (scaled by spawn count)
        for (int i = 0; i < spawnCount; i++) {
            generateSpawnerDrops(serverLevel, entityType);
        }

        // Extract energy
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(energyCost, false);
        }

        setChanged();
    }

    protected void generateSpawnerDrops(ServerLevel serverLevel, net.minecraft.world.entity.EntityType<?> entityType) {
        // Create a temporary entity to get its loot table
        net.minecraft.world.entity.Entity tempEntity = entityType.create(serverLevel);
        if (!(tempEntity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Set up the entity for loot generation
        livingEntity.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5);

        // Calculate looting level
        int lootingLevel = getLootingLevel();

        // Use the entity's loot table to generate drops
        try {
            // Get the entity's loot table resource key
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTableKey = livingEntity.getLootTable();

            // Get the loot table from the server
            net.minecraft.world.level.storage.loot.LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);

            // Create loot context
            net.minecraft.world.level.storage.loot.LootParams.Builder lootParamsBuilder = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(getBlockPos()))
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, livingEntity)
                    .withLuck(lootingLevel);

            // Generate drops
            java.util.List<net.minecraft.world.item.ItemStack> drops = lootTable.getRandomItems(lootParamsBuilder.create(net.minecraft.world.level.storage.loot.LootTable.DEFAULT_PARAM_SET));

            // Add drops to the machine's inventory
            for (net.minecraft.world.item.ItemStack drop : drops) {
                // Try to add to output handler
                boolean added = false;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    net.minecraft.world.item.ItemStack existing = itemHandler.getStackInSlot(i);
                    if (existing.isEmpty()) {
                        itemHandler.setStackInSlot(i, drop.copy());
                        added = true;
                        break;
                    } else if (ItemStack.isSameItemSameComponents(existing, drop) && existing.getCount() + drop.getCount() <= existing.getMaxStackSize()) {
                        existing.grow(drop.getCount());
                        itemHandler.setStackInSlot(i, existing);
                        added = true;
                        break;
                    }
                }
                // If inventory is full, drop the item in the world
                if (!added) {
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            serverLevel,
                            getBlockPos().getX() + 0.5,
                            getBlockPos().getY() + 1.5,
                            getBlockPos().getZ() + 0.5,
                            drop.copy()
                    );
                    serverLevel.addFreshEntity(itemEntity);
                }
            }
        } catch (Exception e) {
            // If loot generation fails, generate some basic drops based on entity type
            generateBasicDrops(serverLevel, livingEntity, lootingLevel);
        }
    }

    protected void generateBasicDrops(ServerLevel serverLevel, LivingEntity entity, int lootingLevel) {
        // Generate some basic drops based on entity type
        java.util.List<net.minecraft.world.item.ItemStack> drops = new java.util.ArrayList<>();

        // Add some basic drops based on entity type
        if (entity instanceof net.minecraft.world.entity.monster.Zombie) {
            drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH, 1 + lootingLevel));
            if (serverLevel.random.nextFloat() < 0.02f + (lootingLevel * 0.01f)) {
                drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 1));
            }
        } else if (entity instanceof net.minecraft.world.entity.monster.Skeleton) {
            drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BONE, 1 + lootingLevel));
            drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 1 + lootingLevel));
        } else if (entity instanceof net.minecraft.world.entity.monster.Spider) {
            drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING, 1 + lootingLevel));
            if (serverLevel.random.nextFloat() < 0.1f + (lootingLevel * 0.05f)) {
                drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPIDER_EYE, 1));
            }
        } else if (entity instanceof net.minecraft.world.entity.monster.Creeper) {
            drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GUNPOWDER, 1 + lootingLevel));
        } else {
            // Default drops for unknown entities
            drops.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BONE, 1));
        }

        // Add drops to the machine's inventory
        for (net.minecraft.world.item.ItemStack drop : drops) {
            // Try to add to output handler
            boolean added = false;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                net.minecraft.world.item.ItemStack existing = itemHandler.getStackInSlot(i);
                if (existing.isEmpty()) {
                    itemHandler.setStackInSlot(i, drop.copy());
                    added = true;
                    break;
                } else if (ItemStack.isSameItemSameComponents(existing, drop) && existing.getCount() + drop.getCount() <= existing.getMaxStackSize()) {
                    existing.grow(drop.getCount());
                    itemHandler.setStackInSlot(i, existing);
                    added = true;
                    break;
                }
            }
            // If inventory is full, drop the item in the world
            if (!added) {
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        serverLevel,
                        getBlockPos().getX() + 0.5,
                        getBlockPos().getY() + 1.5,
                        getBlockPos().getZ() + 0.5,
                        drop.copy()
                );
                serverLevel.addFreshEntity(itemEntity);
            }
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
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        tag.put("lootingHandler", lootingHandler.serializeNBT(provider));
        tag.put("sharpnessHandler", sharpnessHandler.serializeNBT(provider));
        tag.putInt("mode", mode);
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("progress", progress);
        saveAreaSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        if (tag.contains("lootingHandler")) {
            lootingHandler.deserializeNBT(provider, tag.getCompound("lootingHandler"));
        }
        if (tag.contains("sharpnessHandler")) {
            sharpnessHandler.deserializeNBT(provider, tag.getCompound("sharpnessHandler"));
        }
        if (tag.contains("mode")) {
            mode = tag.getInt("mode");
        }
        if (tag.contains("tickCounter")) {
            tickCounter = tag.getInt("tickCounter");
        }
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
        loadAreaSettings(tag);
    }
}
