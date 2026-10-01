package com.jdte.common.manager;

import com.jdte.common.entities.EntityAccelerationData;
import com.jdte.common.items.UltimateTimeWandData;
import com.jdte.setup.JDTEAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Manages entity time acceleration applied by the Ultimate Time Wand.
 * During accelerated extra ticks, entity AI, travel physics, and entity collisions are suppressed.
 */
public final class EntityAccelerationManager {
    private static final ThreadLocal<Boolean> IS_ACCELERATING = ThreadLocal.withInitial(() -> false);

    private EntityAccelerationManager() {
    }

    /**
     * Returns true if the current thread is executing an accelerated sub-tick for an entity.
     * When true, entity AI, navigation, travel physics, and entity pushing are suppressed.
     */
    public static boolean isSuppressingAi() {
        return IS_ACCELERATING.get();
    }

    public static void onEntityTickPost(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide() || IS_ACCELERATING.get()) {
            return;
        }

        if (!entity.hasData(JDTEAttachments.ENTITY_ACCELERATION_DATA)) {
            return;
        }

        EntityAccelerationData data = entity.getData(JDTEAttachments.ENTITY_ACCELERATION_DATA);
        if (!data.isActive() || entity.isRemoved() || !entity.isAlive()) {
            entity.removeData(JDTEAttachments.ENTITY_ACCELERATION_DATA);
            return;
        }

        data.decrementRemainingTime();
        if (data.getRemainingTime() <= 0) {
            entity.removeData(JDTEAttachments.ENTITY_ACCELERATION_DATA);
            return;
        }

        int multiplier = UltimateTimeWandData.multiplierForExponent(data.getExponent());
        int extraTicks = multiplier - 1;
        if (extraTicks <= 0) {
            return;
        }

        runAcceleratedTicks(entity, extraTicks);

        if (entity.level() instanceof ServerLevel serverLevel && entity.tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    3, entity.getBbWidth() * 0.3D, entity.getBbHeight() * 0.25D, entity.getBbWidth() * 0.3D, 0.05D);
        }
    }

    public static void runAcceleratedTicks(Entity entity, int extraTicks) {
        IS_ACCELERATING.set(true);
        try {
            for (int i = 0; i < extraTicks; i++) {
                if (entity.isRemoved() || !entity.isAlive()) {
                    entity.removeData(JDTEAttachments.ENTITY_ACCELERATION_DATA);
                    break;
                }
                entity.tick();
            }
        } finally {
            IS_ACCELERATING.set(false);
        }
    }

    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        net.minecraft.world.item.ItemStack stack = event.getItemStack();
        if (!stack.is(com.jdte.setup.JDTEItems.ULTIMATE_TIME_WAND.get())) {
            return;
        }
        if (event.getTarget() instanceof net.minecraft.world.entity.LivingEntity) {
            return;
        }

        net.minecraft.world.entity.player.Player player = event.getEntity();
        net.minecraft.world.level.Level level = event.getLevel();
        if (!com.jdte.common.items.UltimateTimeWandItem.hasEntityAcceleration(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.jdte.ultimate_time_wand.requires_entity_acceleration")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
            }
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
            return;
        }

        if (com.jdte.common.items.UltimateTimeWandItem.applyToEntityTarget(serverLevel, player, stack, event.getTarget())) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        } else {
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
        event.setCanceled(true);
    }
}
