package com.jdte.common.factory;

import com.jdte.common.blockentities.FactoryPackerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class FactoryPermissionProbe {
    private static final ThreadLocal<Boolean> PERMISSION_PROBE = ThreadLocal.withInitial(() -> false);

    private FactoryPermissionProbe() {}

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof FactoryPackerBE packer
                && packer.isBusy()) {
            event.setCanceled(true);
            event.getPlayer().displayClientMessage(
                    Component.translatable("message.jdte.factory_packer.cannot_break_busy"), true);
        }
    }

    public static boolean isPermissionProbe() { return PERMISSION_PROBE.get(); }

    public static boolean isBreakDenied(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer owner) {
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, pos, state, owner);
        PERMISSION_PROBE.set(true);
        boolean wasShiftKeyDown = owner.isShiftKeyDown();
        owner.setShiftKeyDown(true);
        try {
            return NeoForge.EVENT_BUS.post(breakEvent).isCanceled();
        } finally {
            owner.setShiftKeyDown(wasShiftKeyDown);
            PERMISSION_PROBE.remove();
        }
    }
}
