package com.jdte.common.blockentities;

import com.jdte.setup.JDTEConfig;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RangeBlockerManager {
    private static final String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";
    private static final String ALLOW_MACHINE_REMOTE_MOVEMENT = "AllowMachineRemoteMovement";
    private static final String OWNED_PREVENT = "jdte:range_blocker_owned_prevent";
    private static final String OWNED_ALLOW = "jdte:range_blocker_owned_allow";
    private static final double BOUNDARY_EPSILON = 1.0E-4D;
    private static final double PROJECTILE_BOUNDARY_MARGIN = 0.25D;

    private static final Map<Level, LevelIndex> LEVELS = new ConcurrentHashMap<>();
    private static final Map<Level, Map<LivingEntity, ContainmentState>> CONTAINED = new ConcurrentHashMap<>();
    private static final Map<Level, Map<Projectile, ProjectileState>> CONTAINED_PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<Level, Set<ItemEntity>> OWNED_DEMAGNETIZED = new ConcurrentHashMap<>();

    private RangeBlockerManager() {}

    public static void register(RangeBlockerBE blocker) {
        if (blocker.getLevel() == null) return;
        LevelIndex index = LEVELS.computeIfAbsent(blocker.getLevel(), ignored -> new LevelIndex());
        index.remove(blocker);
        index.add(blocker);
    }

    public static void unregister(RangeBlockerBE blocker) {
        if (blocker.getLevel() == null) return;
        LevelIndex index = LEVELS.get(blocker.getLevel());
        if (index != null) index.remove(blocker);
        releaseAssignments(blocker);
    }

    public static void refresh(RangeBlockerBE blocker) {
        releaseAssignments(blocker);
        if (!blocker.isRemoved()) register(blocker);
    }

    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity item) {
            updateDemagnetization(item);
            return;
        }
        if (entity instanceof Projectile projectile) {
            containProjectile(projectile);
            return;
        }
        if (!(entity instanceof LivingEntity living) || isAlwaysProtected(living)) return;

        Level level = living.level();
        Map<LivingEntity, ContainmentState> states = CONTAINED.computeIfAbsent(level, ignored -> new IdentityHashMap<>());
        ContainmentState state = states.get(living);
        if (state != null && !state.cached.canAffect(living, true)) {
            states.remove(living);
            state = null;
        }
        if (state == null) {
            CachedBlocker cached = find(level, living.position(), RangeBlockerBE.Mode.CONTAINMENT, living, null, true);
            if (cached == null) return;
            state = new ContainmentState(cached, living.position());
            states.put(living, state);
        }
        if (containsEntity(state.cached.area, living)) state.lastValidPosition = living.position();
    }

    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Projectile projectile) {
            Map<Projectile, ProjectileState> states = CONTAINED_PROJECTILES.get(projectile.level());
            ProjectileState state = states == null ? null : states.get(projectile);
            if (state != null && !containsBox(state.cached.area, projectile.getBoundingBox())) {
                states.remove(projectile);
                projectile.discard();
            }
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        Map<LivingEntity, ContainmentState> states = CONTAINED.get(living.level());
        if (states == null) return;
        ContainmentState state = states.get(living);
        if (state == null) return;
        if (!state.cached.canAffect(living, false)) {
            states.remove(living);
            return;
        }
        if (!containsEntity(state.cached.area, living)) confine(living, state);
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) {
            if (event.loadedFromDisk()) {
                CompoundTag data = item.getPersistentData();
                if (data.getBoolean(OWNED_PREVENT) || data.getBoolean(OWNED_ALLOW)) ownedItems(item.level()).add(item);
            }
            updateDemagnetization(item);
        }
        if (event.getEntity() instanceof Projectile projectile) assignProjectile(projectile, false);
    }

    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) {
            Set<ItemEntity> owned = OWNED_DEMAGNETIZED.get(event.getLevel());
            if (owned != null) owned.remove(item);
        }
        if (event.getEntity() instanceof Projectile projectile) {
            Map<Projectile, ProjectileState> projectiles = CONTAINED_PROJECTILES.get(event.getLevel());
            if (projectiles != null) projectiles.remove(projectile);
        }
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        Map<LivingEntity, ContainmentState> states = CONTAINED.get(event.getLevel());
        if (states != null) states.remove(living);
    }

    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living) || isAlwaysProtected(living)) return;
        Map<LivingEntity, ContainmentState> states = CONTAINED.get(living.level());
        ContainmentState state = states == null ? null : states.get(living);
        if (state != null && !state.cached.canAffect(living, true)) {
            states.remove(living);
            return;
        }
        if (state == null) {
            CachedBlocker cached = find(living.level(), living.position(), RangeBlockerBE.Mode.CONTAINMENT,
                    living, null, true);
            if (cached == null) return;
            state = new ContainmentState(cached, living.position());
            CONTAINED.computeIfAbsent(living.level(), ignored -> new IdentityHashMap<>())
                    .put(living, state);
        }
        Vec3 target = clampPosition(living, state.cached.area, event.getTarget(), state.lastValidPosition);
        event.setTargetX(target.x);
        event.setTargetY(target.y);
        event.setTargetZ(target.z);
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        LEVELS.remove(event.getLevel());
        CONTAINED.remove(event.getLevel());
        CONTAINED_PROJECTILES.remove(event.getLevel());
        OWNED_DEMAGNETIZED.remove(event.getLevel());
    }

    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!JDTEConfig.COMMON.rangeBlockerContainProjectileExplosions.get()) return;
        if (!(event.getExplosion().getDirectSourceEntity() instanceof Projectile projectile)) return;
        Map<Projectile, ProjectileState> states = CONTAINED_PROJECTILES.get(event.getLevel());
        ProjectileState state = states == null ? null : states.get(projectile);
        if (state == null) return;
        AABB area = state.cached.area;
        event.getAffectedBlocks().removeIf(pos -> !area.contains(Vec3.atCenterOf(pos)));
        event.getAffectedEntities().removeIf(entity -> !area.contains(entity.position()));
    }

    public static boolean isDemagnetized(ItemEntity item) {
        if (item.getPersistentData().contains(PREVENT_REMOTE_MOVEMENT)) return true;
        return applyDemagnetization(item);
    }

    private static void updateDemagnetization(ItemEntity item) {
        if (!applyDemagnetization(item)) clearOwnedDemagnetization(item);
    }

    private static boolean applyDemagnetization(ItemEntity item) {
        boolean consumeEnergy = item.level() instanceof ServerLevel;
        CachedBlocker cached = find(item.level(), item.position(), RangeBlockerBE.Mode.DEMAGNETIZATION,
                null, item.getItem(), consumeEnergy);
        if (cached == null) return false;

        CompoundTag data = item.getPersistentData();
        if (!data.contains(PREVENT_REMOTE_MOVEMENT)) {
            data.putBoolean(PREVENT_REMOTE_MOVEMENT, true);
            data.putBoolean(OWNED_PREVENT, true);
            ownedItems(item.level()).add(item);
            if (!data.contains(ALLOW_MACHINE_REMOTE_MOVEMENT)) {
                data.putBoolean(ALLOW_MACHINE_REMOTE_MOVEMENT, true);
                data.putBoolean(OWNED_ALLOW, true);
            }
        }
        return true;
    }

    private static void clearOwnedDemagnetization(ItemEntity item) {
        Set<ItemEntity> owned = OWNED_DEMAGNETIZED.get(item.level());
        if (owned == null || !owned.remove(item)) return;
        CompoundTag data = item.getPersistentData();
        if (data.getBoolean(OWNED_PREVENT)) data.remove(PREVENT_REMOTE_MOVEMENT);
        if (data.getBoolean(OWNED_ALLOW)) data.remove(ALLOW_MACHINE_REMOTE_MOVEMENT);
        data.remove(OWNED_PREVENT);
        data.remove(OWNED_ALLOW);
    }

    private static Set<ItemEntity> ownedItems(Level level) {
        return OWNED_DEMAGNETIZED.computeIfAbsent(level,
                ignored -> Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static CachedBlocker find(Level level, Vec3 position, RangeBlockerBE.Mode mode,
                                      LivingEntity living, ItemStack stack, boolean consumeEnergy) {
        LevelIndex index = LEVELS.get(level);
        if (index == null) return null;
        for (CachedBlocker cached : index.at(position)) {
            if (cached.mode != mode || !cached.area.contains(position)) continue;
            boolean matches = living != null ? cached.matchesLiving(living) : cached.matchesItem(stack);
            if (matches && cached.canAffect(living, consumeEnergy)) return cached;
        }
        return null;
    }

    private static void containProjectile(Projectile projectile) {
        if (!JDTEConfig.COMMON.rangeBlockerContainProjectiles.get()) {
            Map<Projectile, ProjectileState> states = CONTAINED_PROJECTILES.get(projectile.level());
            if (states != null) states.remove(projectile);
            return;
        }
        Map<Projectile, ProjectileState> states = CONTAINED_PROJECTILES.computeIfAbsent(
                projectile.level(), ignored -> new IdentityHashMap<>());
        ProjectileState state = states.get(projectile);
        if (state == null) {
            state = assignProjectile(projectile, true);
            if (state == null) return;
        } else if (!state.cached.canPower(true) || !state.cached.matchesProjectile(projectile)) {
            states.remove(projectile);
            return;
        }

        AABB nextBox = projectile.getBoundingBox().move(projectile.getDeltaMovement())
                .inflate(PROJECTILE_BOUNDARY_MARGIN);
        if (!containsBox(state.cached.area, nextBox)) {
            states.remove(projectile);
            projectile.discard();
        }
    }

    private static ProjectileState assignProjectile(Projectile projectile, boolean consumeEnergy) {
        if (!JDTEConfig.COMMON.rangeBlockerContainProjectiles.get()) return null;
        Entity owner = projectile.getOwner();
        if (owner instanceof Player) return null;
        if (owner == null && !JDTEConfig.COMMON.rangeBlockerContainOwnerlessProjectiles.get()) return null;

        LevelIndex index = LEVELS.get(projectile.level());
        if (index == null) return null;
        CachedBlocker cached = findProjectileInCandidates(index.at(projectile.position()), projectile,
                projectile.position(), consumeEnergy);
        if (cached == null && owner != null) {
            cached = findProjectileInCandidates(index.at(owner.position()), projectile, owner.position(), consumeEnergy);
        }
        if (cached == null) return null;
        ProjectileState state = new ProjectileState(cached);
        CONTAINED_PROJECTILES.computeIfAbsent(projectile.level(), ignored -> new IdentityHashMap<>())
                .put(projectile, state);
        return state;
    }

    private static CachedBlocker findProjectileInCandidates(Set<CachedBlocker> candidates, Projectile projectile,
                                                            Vec3 position, boolean consumeEnergy) {
        for (CachedBlocker cached : candidates) {
            if (cached.mode != RangeBlockerBE.Mode.CONTAINMENT || !cached.area.contains(position)) continue;
            if (cached.matchesProjectile(projectile) && cached.canPower(consumeEnergy)) return cached;
        }
        return null;
    }

    private static boolean containsEntity(AABB area, Entity entity) {
        return containsBox(area, entity.getBoundingBox());
    }

    private static boolean containsBox(AABB area, AABB box) {
        return box.minX >= area.minX - BOUNDARY_EPSILON && box.maxX <= area.maxX + BOUNDARY_EPSILON
                && box.minY >= area.minY - BOUNDARY_EPSILON && box.maxY <= area.maxY + BOUNDARY_EPSILON
                && box.minZ >= area.minZ - BOUNDARY_EPSILON && box.maxZ <= area.maxZ + BOUNDARY_EPSILON;
    }

    private static void confine(LivingEntity entity, ContainmentState state) {
        Vec3 current = entity.position();
        Vec3 target = clampPosition(entity, state.cached.area, current, state.lastValidPosition);
        Vec3 velocity = entity.getDeltaMovement();
        boolean clampX = Math.abs(target.x - current.x) > BOUNDARY_EPSILON;
        boolean clampY = Math.abs(target.y - current.y) > BOUNDARY_EPSILON;
        boolean clampZ = Math.abs(target.z - current.z) > BOUNDARY_EPSILON;
        entity.setPos(target.x, target.y, target.z);
        entity.setDeltaMovement(clampX ? 0.0D : velocity.x, clampY ? 0.0D : velocity.y,
                clampZ ? 0.0D : velocity.z);
        entity.hasImpulse = true;
        state.lastValidPosition = target;
    }

    private static Vec3 clampPosition(Entity entity, AABB area, Vec3 position, Vec3 fallback) {
        double halfWidth = entity.getBbWidth() * 0.5D;
        double minX = area.minX + halfWidth + BOUNDARY_EPSILON;
        double maxX = area.maxX - halfWidth - BOUNDARY_EPSILON;
        double minY = area.minY + BOUNDARY_EPSILON;
        double maxY = area.maxY - entity.getBbHeight() - BOUNDARY_EPSILON;
        double minZ = area.minZ + halfWidth + BOUNDARY_EPSILON;
        double maxZ = area.maxZ - halfWidth - BOUNDARY_EPSILON;
        if (minX > maxX || minY > maxY || minZ > maxZ) return fallback;
        return new Vec3(Mth.clamp(position.x, minX, maxX), Mth.clamp(position.y, minY, maxY),
                Mth.clamp(position.z, minZ, maxZ));
    }

    private static boolean isAlwaysProtected(Entity entity) {
        if (entity instanceof PartEntity<?> part && part.getParent() != entity) return isAlwaysProtected(part.getParent());
        if (entity instanceof Player) return true;
        if (entity.isPassenger() || entity.isVehicle()) return true;
        if (JDTEConfig.COMMON.rangeBlockerProtectNamed.get() && entity.hasCustomName()) return true;
        if (JDTEConfig.COMMON.rangeBlockerProtectTamed.get()
                && entity instanceof TamableAnimal tamable && tamable.isTame()) return true;
        return JDTEConfig.COMMON.rangeBlockerProtectBosses.get()
                && (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof ElderGuardian);
    }

    private static void releaseAssignments(RangeBlockerBE blocker) {
        if (blocker.getLevel() == null) return;
        Map<LivingEntity, ContainmentState> states = CONTAINED.get(blocker.getLevel());
        if (states != null) states.entrySet().removeIf(entry -> entry.getValue().cached.blocker == blocker);
        Map<Projectile, ProjectileState> projectiles = CONTAINED_PROJECTILES.get(blocker.getLevel());
        if (projectiles != null) projectiles.entrySet().removeIf(entry -> entry.getValue().cached.blocker == blocker);
    }

    private record CachedBlocker(RangeBlockerBE blocker, AABB area, RangeBlockerBE.Mode mode,
                                 boolean blacklist, Set<net.minecraft.world.entity.EntityType<?>> entityTypes,
                                 List<ItemStack> itemFilters) {
        static CachedBlocker of(RangeBlockerBE blocker) {
            Set<net.minecraft.world.entity.EntityType<?>> entityTypes =
                    Collections.newSetFromMap(new IdentityHashMap<>());
            List<ItemStack> itemFilters = new ArrayList<>();
            for (int slot = 0; slot < blocker.getFilterHandler().getSlots(); slot++) {
                ItemStack filter = blocker.getFilterHandler().getStackInSlot(slot);
                if (filter.isEmpty()) continue;
                itemFilters.add(filter.copy());
                if (filter.getItem() instanceof SpawnEggItem egg) entityTypes.add(egg.getType(filter));
            }
            return new CachedBlocker(blocker, blocker.getIndexedArea(), blocker.getMode(), blocker.isBlacklist(),
                    entityTypes, List.copyOf(itemFilters));
        }

        boolean canAffect(LivingEntity living, boolean consumeEnergy) {
            if (!canPower(consumeEnergy)) return false;
            if (living != null && isAlwaysProtected(living)) return false;
            return true;
        }

        boolean canPower(boolean consumeEnergy) {
            if (blocker.isRemoved() || !blocker.isFieldActive()) return false;
            return !consumeEnergy || blocker.canApplyEffectThisTick();
        }

        boolean matchesLiving(LivingEntity living) {
            if (entityTypes.isEmpty()) return true;
            boolean listed = entityTypes.contains(living.getType());
            return blacklist ? !listed : listed;
        }

        boolean matchesItem(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;
            if (itemFilters.isEmpty()) return true;
            boolean listed = false;
            for (ItemStack filter : itemFilters) {
                if (ItemStack.isSameItemSameComponents(filter, stack)) {
                    listed = true;
                    break;
                }
            }
            return blacklist ? !listed : listed;
        }

        boolean matchesProjectile(Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof Player) return false;
            if (owner == null) {
                return JDTEConfig.COMMON.rangeBlockerContainOwnerlessProjectiles.get() && entityTypes.isEmpty();
            }
            if (!(owner instanceof LivingEntity living)) return entityTypes.isEmpty();
            return matchesLiving(living);
        }
    }

    private static final class ContainmentState {
        private final CachedBlocker cached;
        private Vec3 lastValidPosition;

        private ContainmentState(CachedBlocker cached, Vec3 lastValidPosition) {
            this.cached = cached;
            this.lastValidPosition = lastValidPosition;
        }
    }

    private record ProjectileState(CachedBlocker cached) {}

    private static final class LevelIndex {
        private final Map<Long, Set<CachedBlocker>> byChunk = new java.util.HashMap<>();
        private final Map<RangeBlockerBE, Set<Long>> chunksByBlocker = new IdentityHashMap<>();

        void add(RangeBlockerBE blocker) {
            CachedBlocker cached = CachedBlocker.of(blocker);
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
            chunksByBlocker.put(blocker, chunks);
        }

        void remove(RangeBlockerBE blocker) {
            Set<Long> chunks = chunksByBlocker.remove(blocker);
            if (chunks == null) return;
            for (long key : chunks) {
                Set<CachedBlocker> values = byChunk.get(key);
                if (values == null) continue;
                values.removeIf(value -> value.blocker == blocker);
                if (values.isEmpty()) byChunk.remove(key);
            }
        }

        Set<CachedBlocker> at(Vec3 pos) {
            return byChunk.getOrDefault(ChunkPos.asLong(
                    SectionPos.blockToSectionCoord(Mth.floor(pos.x)),
                    SectionPos.blockToSectionCoord(Mth.floor(pos.z))), Collections.emptySet());
        }
    }
}
