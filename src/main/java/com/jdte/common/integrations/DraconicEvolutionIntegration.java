package com.jdte.common.integrations;

import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.entity.GuardianCrystalEntity;
import com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity;
import com.brandon3055.draconicevolution.entity.guardian.GuardianFightManager;
import com.brandon3055.draconicevolution.init.DEContent;
import com.jdte.common.utils.LootDropInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

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

    public static void addLootFabricatorDrops(LivingEntity entity, RandomSource random, List<ItemStack> drops) {
        if (entity instanceof EnderDragon) {
            drops.add(new ItemStack(DEContent.DRAGON_HEART.get()));
        }
        if (entity instanceof EnderDragon || entity instanceof DraconicGuardianEntity) {
            int dustCount = rollDragonDustCount(random);
            if (dustCount > 0) {
                drops.add(new ItemStack(DEContent.DUST_DRACONIUM.get(), dustCount));
            }
        }
    }

    public static void addLootFabricatorPreviewDrops(EntityType<?> entityType, Map<ResourceLocation, LootDropInfo> drops) {
        if (entityType == EntityType.ENDER_DRAGON) {
            addPreviewDrop(drops, BuiltInRegistries.ITEM.getKey(DEContent.DRAGON_HEART.get()), 1, 1);
        }
        if (entityType == EntityType.ENDER_DRAGON || entityType == DEContent.ENTITY_DRACONIC_GUARDIAN.get()) {
            int[] dustRange = dragonDustCountRange();
            if (dustRange[1] > 0) {
                addPreviewDrop(drops, BuiltInRegistries.ITEM.getKey(DEContent.DUST_DRACONIUM.get()), dustRange[0], dustRange[1]);
            }
        }
    }

    private static int rollDragonDustCount(RandomSource random) {
        int base = Math.max(0, DEConfig.dragonDustLootModifier);
        if (base <= 0) return 0;
        return (int) (base * 0.9D + random.nextDouble() * base * 0.2D);
    }

    private static int[] dragonDustCountRange() {
        int base = Math.max(0, DEConfig.dragonDustLootModifier);
        if (base <= 0) return new int[]{0, 0};
        int min = Math.max(0, (int) Math.floor(base * 0.9D));
        int max = Math.max(min, (int) Math.ceil(base * 1.1D) - 1);
        return new int[]{min, max};
    }

    private static void addPreviewDrop(Map<ResourceLocation, LootDropInfo> drops, ResourceLocation itemId, int minCount, int maxCount) {
        drops.merge(itemId, new LootDropInfo(itemId, minCount, maxCount, ""), (left, right) ->
                new LootDropInfo(itemId, Math.min(left.minCount(), right.minCount()),
                        Math.max(left.maxCount(), right.maxCount()), mergeChance(left.chanceLabel(), right.chanceLabel())));
    }

    private static String mergeChance(String left, String right) {
        if (left == null || left.isEmpty()) return right == null ? "" : right;
        if (right == null || right.isEmpty() || left.equals(right)) return left;
        return "conditional";
    }
}
