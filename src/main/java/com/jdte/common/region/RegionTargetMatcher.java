package com.jdte.common.region;

import com.jdte.common.blockentities.EntitySuppressorBE;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;

import java.util.Set;

public final class RegionTargetMatcher {
    private RegionTargetMatcher() {}

    public static boolean matches(Entity entity, EntitySuppressorBE.Target target, boolean blacklist,
                                  Set<EntityType<?>> selectedTypes) {
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

    public static boolean isAlwaysProtected(Entity entity, boolean protectNamed, boolean protectTamed,
                                            boolean protectBosses) {
        if (entity instanceof PartEntity<?> part && part.getParent() != entity) {
            return isAlwaysProtected(part.getParent(), protectNamed, protectTamed, protectBosses);
        }
        if (entity instanceof Player) return true;
        if (entity.isPassenger() || entity.isVehicle()) return true;
        if (protectNamed && entity.hasCustomName()) return true;
        if (protectTamed && entity instanceof TamableAnimal tamable && tamable.isTame()) return true;
        return protectBosses
                && (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof ElderGuardian);
    }
}
