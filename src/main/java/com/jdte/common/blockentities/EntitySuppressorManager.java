package com.jdte.common.blockentities;

import com.jdte.setup.JDTEConfig;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.common.util.TriState;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EntitySuppressorManager {
    private static final Map<Level, LevelIndex> LEVELS = new ConcurrentHashMap<>();

    private EntitySuppressorManager() {}

    public static void register(EntitySuppressorBE suppressor) {
        if (suppressor.getLevel() == null) return;
        LevelIndex index = LEVELS.computeIfAbsent(suppressor.getLevel(), ignored -> new LevelIndex());
        index.remove(suppressor);
        index.add(suppressor);
    }

    public static void unregister(EntitySuppressorBE suppressor) {
        if (suppressor.getLevel() == null) return;
        LevelIndex index = LEVELS.get(suppressor.getLevel());
        if (index != null) index.remove(suppressor);
    }

    public static void refresh(EntitySuppressorBE suppressor) {
        if (!suppressor.isRemoved()) register(suppressor);
    }

    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (isAlwaysProtected(entity)) return;
        boolean server = entity.level() instanceof ServerLevel;
        if (matches(entity.level(), entity.position(), entity,
                EntitySuppressorBE.Mode.SUPPRESS_TICK, server)) {
            event.setCanceled(true);
        }
    }

    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Entity item = event.getItemEntity();
        if (matches(item.level(), item.position(), item, EntitySuppressorBE.Mode.SUPPRESS_TICK, true)) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (isAlwaysProtected(entity)) return;
        boolean server = event.getLevel() instanceof ServerLevel;
        boolean suppressed = matches(event.getLevel(), entity.position(), entity,
                EntitySuppressorBE.Mode.SUPPRESS_TICK, server);
        if (suppressed) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hasImpulse = false;
        }
        if (server && !event.loadedFromDisk()
                && matches(event.getLevel(), entity.position(), entity, EntitySuppressorBE.Mode.BLOCK_ENTITY, true)) {
            event.setCanceled(true);
        }
    }

    public static void onMobSpawnPosition(MobSpawnEvent.PositionCheck event) {
        Mob mob = event.getEntity();
        if (!(mob.level() instanceof ServerLevel) || isAlwaysProtected(mob)) return;
        if (matches(mob.level(), mob.position(), mob, EntitySuppressorBE.Mode.BLOCK_ENTITY, true)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) { LEVELS.remove(event.getLevel()); }

    public static boolean shouldSuppressParticle(Level level, double x, double y, double z) {
        return matches(level, x, y, z, null, EntitySuppressorBE.Mode.DISABLE_PARTICLES, false);
    }

    public static boolean shouldSuppressEntityVisual(Entity entity) {
        return !isAlwaysProtected(entity)
                && matches(entity.level(), entity.position(), entity, EntitySuppressorBE.Mode.SUPPRESS_TICK, false);
    }

    public static boolean shouldSuppressEntityRender(Entity entity) {
        return !isAlwaysProtected(entity)
                && matches(entity.level(), entity.position(), entity,
                EntitySuppressorBE.Mode.DISABLE_ENTITY_RENDERING, false);
    }

    public static boolean shouldSuppressBlockEntityRender(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        return level != null && !(blockEntity instanceof EntitySuppressorBE)
                && matches(level,
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5,
                null,
                EntitySuppressorBE.Mode.DISABLE_BLOCK_ENTITY_RENDERING, false);
    }

    public static void removeExistingEntities(EntitySuppressorBE suppressor) {
        if (!(suppressor.getLevel() instanceof ServerLevel level) || !suppressor.canOperateThisTick()) return;
        CachedSuppressor cached = CachedSuppressor.of(suppressor);
        for (Entity entity : level.getEntities((Entity) null, cached.area,
                entity -> !isAlwaysProtected(entity) && cached.matches(entity))) {
            entity.discard();
        }
    }

    private static boolean matches(Level level, Vec3 position, Entity entity,
                                   EntitySuppressorBE.Mode mode, boolean consumeEnergy) {
        return matches(level, position.x, position.y, position.z, entity, mode, consumeEnergy);
    }

    private static boolean matches(Level level, double x, double y, double z, Entity entity,
                                   EntitySuppressorBE.Mode mode, boolean consumeEnergy) {
        LevelIndex index = LEVELS.get(level);
        if (index == null) return false;
        for (CachedSuppressor cached : index.at(x, z)) {
            if (cached.mode != mode || !cached.area.contains(x, y, z)) continue;
            EntitySuppressorBE suppressor = cached.suppressor;
            if (suppressor.isRemoved() || (consumeEnergy && !suppressor.canOperateThisTick())) continue;
            if (!consumeEnergy && mode == EntitySuppressorBE.Mode.DISABLE_PARTICLES
                    && !suppressor.canSuppressParticlesClient()) continue;
            if (!consumeEnergy && mode == EntitySuppressorBE.Mode.SUPPRESS_TICK
                    && !suppressor.canSuppressEntitiesClient()) continue;
            if (!consumeEnergy && mode.disablesRendering()
                    && !suppressor.canSuppressRenderingClient()) continue;
            if (entity == null || cached.matches(entity)) return true;
        }
        return false;
    }

    private static boolean isAlwaysProtected(Entity entity) {
        if (entity instanceof PartEntity<?> part && part.getParent() != entity) return isAlwaysProtected(part.getParent());
        if (entity instanceof Player) return true;
        if (entity.isPassenger() || entity.isVehicle()) return true;
        if (JDTEConfig.COMMON.entitySuppressorProtectNamed.get() && entity.hasCustomName()) return true;
        if (JDTEConfig.COMMON.entitySuppressorProtectTamed.get()
                && entity instanceof TamableAnimal tamable && tamable.isTame()) return true;
        return JDTEConfig.COMMON.entitySuppressorProtectBosses.get()
                && (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof ElderGuardian);
    }

    private record CachedSuppressor(EntitySuppressorBE suppressor, AABB area,
                                    EntitySuppressorBE.Mode mode, EntitySuppressorBE.Target target,
                                    boolean blacklist, Set<net.minecraft.world.entity.EntityType<?>> selectedTypes) {
        static CachedSuppressor of(EntitySuppressorBE suppressor) {
            Set<net.minecraft.world.entity.EntityType<?>> types = Collections.newSetFromMap(new IdentityHashMap<>());
            for (int slot = 0; slot < suppressor.getFilterHandler().getSlots(); slot++) {
                ItemStack stack = suppressor.getFilterHandler().getStackInSlot(slot);
                if (stack.getItem() instanceof SpawnEggItem egg) types.add(egg.getType(stack));
            }
            return new CachedSuppressor(suppressor, suppressor.getIndexedArea(),
                    suppressor.getMode(), suppressor.getTarget(), suppressor.isBlacklist(), types);
        }

        boolean matches(Entity entity) {
            boolean categoryMatch = switch (target) {
                case HOSTILE -> entity instanceof Mob && entity.getType().getCategory() == MobCategory.MONSTER;
                case PASSIVE -> entity instanceof Mob && entity.getType().getCategory() != MobCategory.MONSTER;
                case ALL_LIVING -> entity instanceof LivingEntity;
                case SELECTED_TYPES -> selectedTypes.contains(entity.getType());
                case NON_LIVING -> !(entity instanceof LivingEntity);
                case ALL_TYPES -> true;
            };
            if (target == EntitySuppressorBE.Target.SELECTED_TYPES) return blacklist != categoryMatch;
            if (selectedTypes.isEmpty()) return categoryMatch;
            boolean listed = selectedTypes.contains(entity.getType());
            return categoryMatch && (blacklist ? !listed : listed);
        }
    }

    private static final class LevelIndex {
        private final Map<Long, Set<CachedSuppressor>> byChunk = new java.util.HashMap<>();
        private final Map<EntitySuppressorBE, Set<Long>> chunksBySuppressor = new IdentityHashMap<>();

        void add(EntitySuppressorBE suppressor) {
            CachedSuppressor cached = CachedSuppressor.of(suppressor);
            int minX = SectionPos.blockToSectionCoord(Mth.floor(cached.area.minX));
            int maxX = SectionPos.blockToSectionCoord(Mth.ceil(cached.area.maxX) - 1);
            int minZ = SectionPos.blockToSectionCoord(Mth.floor(cached.area.minZ));
            int maxZ = SectionPos.blockToSectionCoord(Mth.ceil(cached.area.maxZ) - 1);
            Set<Long> chunks = new LinkedHashSet<>();
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                long key = ChunkPos.asLong(x, z);
                chunks.add(key);
                byChunk.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(cached);
            }
            chunksBySuppressor.put(suppressor, chunks);
        }

        void remove(EntitySuppressorBE suppressor) {
            Set<Long> chunks = chunksBySuppressor.remove(suppressor);
            if (chunks == null) return;
            for (long key : chunks) {
                Set<CachedSuppressor> values = byChunk.get(key);
                if (values == null) continue;
                values.removeIf(value -> value.suppressor == suppressor);
                if (values.isEmpty()) byChunk.remove(key);
            }
        }

        Set<CachedSuppressor> at(double x, double z) {
            return byChunk.getOrDefault(ChunkPos.asLong(
                    SectionPos.blockToSectionCoord(Mth.floor(x)),
                    SectionPos.blockToSectionCoord(Mth.floor(z))), Collections.emptySet());
        }
    }
}
