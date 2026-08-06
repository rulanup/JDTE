package com.jdte.common.blockentities;

import com.jdte.common.region.RegionChunkIndex;
import com.jdte.common.region.RegionTargetMatcher;
import com.jdte.setup.JDTEConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.level.ExplosionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
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

    private static final RegionChunkIndex<RangeBlockerBE, CachedBlocker> INDEX = new RegionChunkIndex<>();
    private static final Map<Level, Map<Entity, ContainmentState>> CONTAINED = new ConcurrentHashMap<>();
    private static final Map<Level, Map<Projectile, ProjectileState>> CONTAINED_PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<Level, Set<ItemEntity>> OWNED_DEMAGNETIZED = new ConcurrentHashMap<>();

    private RangeBlockerManager() {}

    public static void register(RangeBlockerBE blocker) {
        if (blocker.getLevel() == null) return;
        CachedBlocker cached = CachedBlocker.of(blocker);
        INDEX.add(blocker.getLevel(), blocker, cached, cached.area);
    }

    public static void unregister(RangeBlockerBE blocker) {
        if (blocker.getLevel() == null) return;
        INDEX.remove(blocker.getLevel(), blocker);
        releaseAssignments(blocker);
    }

    public static void refresh(RangeBlockerBE blocker) {
        releaseAssignments(blocker);
        if (!blocker.isRemoved()) register(blocker);
    }

    public static void onEntityTickPre(Entity entity) {
        if (entity instanceof ItemEntity item) {
            updateDemagnetization(item);
        }
        if (entity instanceof Projectile projectile) {
            containProjectile(projectile);
            return;
        }
        if (isAlwaysProtected(entity)) return;

        Level level = entity.level();
        Map<Entity, ContainmentState> states = CONTAINED.computeIfAbsent(level, ignored -> new IdentityHashMap<>());
        ContainmentState state = states.get(entity);
        if (state != null && !state.cached.canAffect(entity, true)) {
            states.remove(entity);
            state = null;
        }
        if (state == null) {
            CachedBlocker cached = find(level, entity.position(), RangeBlockerBE.Mode.CONTAINMENT,
                    entity, null, true);
            if (cached == null) return;
            state = new ContainmentState(cached, entity.position());
            states.put(entity, state);
        }
        if (containsEntity(state.cached.area, entity)) state.lastValidPosition = entity.position();
    }

    public static void onEntityTickPost(Entity entity) {
        if (entity instanceof Projectile projectile) {
            Map<Projectile, ProjectileState> states = CONTAINED_PROJECTILES.get(projectile.level());
            ProjectileState state = states == null ? null : states.get(projectile);
            if (state != null && !containsBox(state.cached.area, projectile.getBoundingBox())) {
                states.remove(projectile);
                projectile.discard();
            }
            return;
        }
        Map<Entity, ContainmentState> states = CONTAINED.get(entity.level());
        if (states == null) return;
        ContainmentState state = states.get(entity);
        if (state == null) return;
        if (!state.cached.canAffect(entity, false)) {
            states.remove(entity);
            return;
        }
        if (!containsEntity(state.cached.area, entity)) confine(entity, state);
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
        Map<Entity, ContainmentState> states = CONTAINED.get(event.getLevel());
        if (states != null) states.remove(event.getEntity());
    }

    public static void onTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (isAlwaysProtected(entity)) return;
        Map<Entity, ContainmentState> states = CONTAINED.get(entity.level());
        ContainmentState state = states == null ? null : states.get(entity);
        if (state != null && !state.cached.canAffect(entity, true)) {
            states.remove(entity);
            return;
        }
        if (state == null) {
            CachedBlocker cached = find(entity.level(), entity.position(), RangeBlockerBE.Mode.CONTAINMENT,
                    entity, null, true);
            if (cached == null) return;
            state = new ContainmentState(cached, entity.position());
            CONTAINED.computeIfAbsent(entity.level(), ignored -> new IdentityHashMap<>())
                    .put(entity, state);
        }
        Vec3 target = clampPosition(entity, state.cached.area, event.getTarget(), state.lastValidPosition);
        event.setTargetX(target.x);
        event.setTargetY(target.y);
        event.setTargetZ(target.z);
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        INDEX.clear(event.getLevel());
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

    public static void onPlaySoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
        if (shouldSuppressSound(event.getLevel(), event.getPosition())) {
            event.setCanceled(true);
        }
    }

    public static void onPlaySoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        if (shouldSuppressSound(event.getLevel(), event.getEntity().position())) {
            event.setCanceled(true);
        }
    }

    public static boolean shouldSuppressSound(Level level, Vec3 position) {
        for (CachedBlocker cached : INDEX.entriesAt(level, position)) {
            if (cached.mode == RangeBlockerBE.Mode.SILENCE
                    && cached.area.contains(position) && cached.canPower(false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasActiveSilenceField(Level level) {
        for (CachedBlocker cached : INDEX.entries(level)) {
            if (cached.blocker.getMode() == RangeBlockerBE.Mode.SILENCE && cached.blocker.isFieldActive()) return true;
        }
        return false;
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
                                      Entity entity, ItemStack stack, boolean consumeEnergy) {
        for (CachedBlocker cached : INDEX.entriesAt(level, position)) {
            if (cached.mode != mode || !cached.area.contains(position)) continue;
            boolean matches = entity != null ? cached.matches(entity) : cached.matchesItem(stack);
            if (matches && cached.canAffect(entity, consumeEnergy)) return cached;
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

        CachedBlocker cached = findProjectileInCandidates(
                INDEX.entriesAt(projectile.level(), projectile.position()), projectile,
                projectile.position(), consumeEnergy);
        if (cached == null && owner != null) {
            cached = findProjectileInCandidates(INDEX.entriesAt(projectile.level(), owner.position()),
                    projectile, owner.position(), consumeEnergy);
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

    private static void confine(Entity entity, ContainmentState state) {
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
        return RegionTargetMatcher.isAlwaysProtected(entity,
                JDTEConfig.COMMON.rangeBlockerProtectNamed.get(),
                JDTEConfig.COMMON.rangeBlockerProtectTamed.get(),
                JDTEConfig.COMMON.rangeBlockerProtectBosses.get());
    }

    private static void releaseAssignments(RangeBlockerBE blocker) {
        if (blocker.getLevel() == null) return;
        Map<Entity, ContainmentState> states = CONTAINED.get(blocker.getLevel());
        if (states != null) states.entrySet().removeIf(entry -> entry.getValue().cached.blocker == blocker);
        Map<Projectile, ProjectileState> projectiles = CONTAINED_PROJECTILES.get(blocker.getLevel());
        if (projectiles != null) projectiles.entrySet().removeIf(entry -> entry.getValue().cached.blocker == blocker);
    }

    private record CachedBlocker(RangeBlockerBE blocker, AABB area, RangeBlockerBE.Mode mode,
                                 EntitySuppressorBE.Target target, boolean blacklist,
                                 Set<net.minecraft.world.entity.EntityType<?>> entityTypes,
                                 List<ItemStack> itemFilters) {
        static CachedBlocker of(RangeBlockerBE blocker) {
            Set<net.minecraft.world.entity.EntityType<?>> entityTypes =
                    Collections.newSetFromMap(new IdentityHashMap<>());
            List<ItemStack> itemFilters = new ArrayList<>();
            for (int slot = 0; slot < blocker.getFilterHandler().getSlots(); slot++) {
                ItemStack filter = blocker.getFilterHandler().getStackInSlot(slot);
                if (filter.isEmpty()) continue;
                itemFilters.add(filter.copy());
                if (filter.getItem() instanceof SpawnEggItem egg) entityTypes.add(egg.getType(filter.getTag()));
            }
            return new CachedBlocker(blocker, blocker.getIndexedArea(), blocker.getMode(), blocker.getTarget(),
                    blocker.isBlacklist(), entityTypes, List.copyOf(itemFilters));
        }

        boolean canAffect(Entity entity, boolean consumeEnergy) {
            if (!canPower(consumeEnergy)) return false;
            if (entity != null && isAlwaysProtected(entity)) return false;
            return true;
        }

        boolean canPower(boolean consumeEnergy) {
            if (blocker.isRemoved() || !blocker.isFieldActive()) return false;
            return !consumeEnergy || blocker.canApplyEffectThisTick();
        }

        boolean matches(Entity entity) {
            return RegionTargetMatcher.matches(entity, target, blacklist, entityTypes);
        }

        boolean matchesItem(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;
            if (itemFilters.isEmpty()) return true;
            boolean listed = false;
            for (ItemStack filter : itemFilters) {
                if (ItemStack.isSameItemSameTags(filter, stack)) {
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
                return JDTEConfig.COMMON.rangeBlockerContainOwnerlessProjectiles.get()
                        && (target == EntitySuppressorBE.Target.NON_LIVING
                        || target == EntitySuppressorBE.Target.ALL_TYPES)
                        && matches(projectile);
            }
            if (target == EntitySuppressorBE.Target.NON_LIVING
                    || target == EntitySuppressorBE.Target.ALL_TYPES) {
                return matches(projectile);
            }
            return matches(owner);
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
}
