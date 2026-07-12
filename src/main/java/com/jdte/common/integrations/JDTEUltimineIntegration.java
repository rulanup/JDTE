package com.jdte.common.integrations;

import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.upgrades.UpgradeCardInsertionHelper;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Optional;

public class JDTEUltimineIntegration {
    public static void register() {
        RegisterRightClickHandlerEvent.REGISTER.register(event -> event.registerHandler((context, hand, positions) -> {
            ServerPlayer player = context.player();
            ItemStack stack = player.getItemInHand(hand);
            if (!player.mayBuild()) {
                return 0;
            }

            Level level = player.level();
            if (UpgradeCardInsertionHelper.isUpgradeCard(stack) && player.isShiftKeyDown()) {
                int handled = 0;
                for (BlockPos pos : positions) {
                    if (stack.isEmpty() && !player.getAbilities().instabuild) {
                        break;
                    }
                    if (UpgradeCardInsertionHelper.insertAll(level, pos, player, stack) > 0) {
                        handled++;
                    }
                }
                return handled;
            }

            if (!(stack.getItem() instanceof EclipseAlloyWrenchItem)) {
                return 0;
            }

            int handled = 0;
            for (BlockPos pos : positions) {
                if (player.isShiftKeyDown()) {
                    if (EclipseAlloyWrenchItem.pickupMachine(level, pos, player)) {
                        handled++;
                    }
                } else if (EclipseAlloyWrenchItem.rotateMachine(level, pos)) {
                    handled++;
                }
            }
            return handled;
        }));
    }

    public static Optional<Collection<BlockPos>> getCurrentSelection(ServerPlayer player) {
        FTBUltimine instance = FTBUltimine.getInstance();
        if (instance == null || !instance.getOrCreatePlayerData(player).isPressed()) {
            return Optional.empty();
        }
        return FTBUltimineAPI.api().currentBlockSelection(player);
    }
}
