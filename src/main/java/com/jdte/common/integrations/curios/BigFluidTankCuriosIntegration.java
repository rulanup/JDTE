package com.jdte.common.integrations.curios;

import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.util.ISlotHelper;

/** Ensures the dedicated tank slot is also present for players from old saves. */
public final class BigFluidTankCuriosIntegration {
    public static final String SLOT_ID = "big_fluid_tank";

    private BigFluidTankCuriosIntegration() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        ISlotHelper helper = CuriosApi.getSlotHelper();
        if (helper != null && helper.getSlotType(SLOT_ID).isPresent()
                && helper.getSlotsForType(player, SLOT_ID) <= 0) {
            helper.setSlotsForType(SLOT_ID, player, 1);
        }
    }
}
