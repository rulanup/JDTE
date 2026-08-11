package com.jdte.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;

public final class TimeMultitoolMiningEvents {
    private TimeMultitoolMiningEvents() {
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof TimeMultitoolItem tool)) {
            return;
        }

        TimeMultitoolMiningPolicy.Decision singleTarget = tool.miningBatchDecision(stack, 1);
        if (!singleTarget.powered() || singleTarget.speedMultiplier() <= 1) {
            return;
        }

        BlockPos origin = event.getPosition().orElse(null);
        if (origin == null) {
            return;
        }

        Set<BlockPos> targets = tool.getBreakBlockPositions(
                stack, player.level(), origin, player, event.getState());
        int targetCount = (int) targets.stream()
                .filter(pos -> !player.level().getBlockState(pos).isAir())
                .count();
        TimeMultitoolMiningPolicy.Decision decision = tool.miningBatchDecision(stack, targetCount);
        if (decision.powered() && decision.speedMultiplier() > 1) {
            event.setNewSpeed(event.getNewSpeed() * decision.speedMultiplier());
        }
    }
}
