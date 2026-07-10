package com.jdte.common.integrations;

import com.brandon3055.draconicevolution.entity.GuardianCrystalEntity;
import com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity;
import com.brandon3055.draconicevolution.entity.guardian.GuardianFightManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class DraconicEvolutionIntegration {
    private DraconicEvolutionIntegration() {
    }

    public static boolean isGuardianCrystal(Entity entity) {
        return entity instanceof GuardianCrystalEntity;
    }

    public static boolean isGuardian(LivingEntity entity) {
        return entity instanceof DraconicGuardianEntity;
    }

    public static boolean destroyGuardianCrystal(Entity entity, DamageSource playerDamage) {
        if (!(entity instanceof GuardianCrystalEntity crystal) || crystal.isRemoved()) {
            return false;
        }

        crystal.setInvulnerable(false);
        crystal.setShieldPower(0.0F);
        crystal.hurt(playerDamage, Float.MAX_VALUE);
        return crystal.isRemoved();
    }

    public static boolean destroyOneGuardianCrystal(LivingEntity entity, DamageSource playerDamage) {
        if (!(entity instanceof DraconicGuardianEntity guardian)) {
            return false;
        }

        GuardianFightManager fightManager = guardian.getFightManager();
        if (fightManager == null) {
            return false;
        }

        for (GuardianCrystalEntity crystal : fightManager.getCrystals()) {
            if (destroyGuardianCrystal(crystal, playerDamage)) {
                return true;
            }
        }
        return false;
    }

    public static void attackGuardian(LivingEntity entity, DamageSource playerDamage) {
        if (entity instanceof DraconicGuardianEntity guardian) {
            guardian.setShieldPower(0.0F);
            guardian.setHealth(Math.min(guardian.getHealth(), 1.0F));
            guardian.attackEntityPartFrom(guardian.dragonPartHead, playerDamage, Float.MAX_VALUE);
            if (!guardian.isDeadOrDying()) {
                guardian.setHealth(0.0F);
                guardian.die(playerDamage);
            }
        }
    }
}
