package com.jdte.common.player;

import com.jdte.JDTE;
import com.jdte.setup.JDTEAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class LifeAppleProgression {
    private static final String HEALTH_NAME = JDTE.id("life_apple_health").toString();
    private static final String ARMOR_NAME = JDTE.id("life_apple_armor").toString();
    private static final String TOUGHNESS_NAME = JDTE.id("life_apple_armor_toughness").toString();
    private static final UUID HEALTH_ID = stableModifierId(HEALTH_NAME);
    private static final UUID ARMOR_ID = stableModifierId(ARMOR_NAME);
    private static final UUID TOUGHNESS_ID = stableModifierId(TOUGHNESS_NAME);

    private LifeAppleProgression() {
    }

    public static void consume(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            entity.heal(20.0F);
            return;
        }
        LifeAppleData data = JDTEAttachments.lifeApple(player);
        data.increment();
        apply(player);
        player.heal(20.0F);
    }

    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        try {
            long consumed = JDTEAttachments.lifeApple(event.getOriginal()).getConsumed();
            JDTEAttachments.lifeApple(event.getEntity()).setConsumed(consumed);
        } finally {
            event.getOriginal().invalidateCaps();
        }
        apply(event.getEntity());
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        apply(event.getEntity());
    }

    public static void apply(Player player) {
        long consumed = JDTEAttachments.lifeApple(player).getConsumed();
        replaceModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, HEALTH_NAME,
                saturated(healthPercent(consumed) / 100.0D), AttributeModifier.Operation.MULTIPLY_BASE);
        double defense = saturated(defenseBonus(consumed));
        replaceModifier(player.getAttribute(Attributes.ARMOR), ARMOR_ID, ARMOR_NAME, defense, AttributeModifier.Operation.ADDITION);
        replaceModifier(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_ID, TOUGHNESS_NAME, defense, AttributeModifier.Operation.ADDITION);
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

    private static void replaceModifier(AttributeInstance attribute, UUID id, String name, double amount,
                                        AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(id);
        if (amount > 0.0D) {
            attribute.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    private static UUID stableModifierId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    private static double saturated(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : Double.MAX_VALUE / 4.0D;
    }
}
