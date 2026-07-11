package com.jdte.common.player;

import com.jdte.JDTE;
import com.jdte.setup.JDTEAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class LifeAppleProgression {
    private static final ResourceLocation HEALTH_ID = JDTE.id("life_apple_health");
    private static final ResourceLocation ARMOR_ID = JDTE.id("life_apple_armor");
    private static final ResourceLocation TOUGHNESS_ID = JDTE.id("life_apple_armor_toughness");

    private LifeAppleProgression() {
    }

    public static void consume(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            entity.heal(20.0F);
            return;
        }
        LifeAppleData data = player.getData(JDTEAttachments.LIFE_APPLE_DATA);
        data.increment();
        apply(player);
        player.heal(20.0F);
    }

    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        long consumed = event.getOriginal().getData(JDTEAttachments.LIFE_APPLE_DATA).getConsumed();
        event.getEntity().getData(JDTEAttachments.LIFE_APPLE_DATA).setConsumed(consumed);
        apply(event.getEntity());
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        apply(event.getEntity());
    }

    public static void apply(Player player) {
        long consumed = player.getData(JDTEAttachments.LIFE_APPLE_DATA).getConsumed();
        replaceModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID,
                saturated(healthPercent(consumed) / 100.0D), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        double defense = saturated(defenseBonus(consumed));
        replaceModifier(player.getAttribute(Attributes.ARMOR), ARMOR_ID, defense, AttributeModifier.Operation.ADD_VALUE);
        replaceModifier(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_ID, defense, AttributeModifier.Operation.ADD_VALUE);
    }

    public static double healthPercent(long consumed) {
        return consumed * 0.01D
                + (consumed / 50L) * 0.1D
                + (consumed / 100L) * 0.2D
                + (consumed / 1000L) * 100.0D;
    }

    public static double defenseBonus(long consumed) {
        return consumed * 0.01D + (consumed / 10L) * 0.05D;
    }

    private static void replaceModifier(AttributeInstance attribute, ResourceLocation id, double amount,
                                        AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(id);
        if (amount > 0.0D) {
            attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static double saturated(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : Double.MAX_VALUE / 4.0D;
    }
}
