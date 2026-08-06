package com.jdte.common.integrations.curios;

import com.jdte.JDTE;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 确保所有玩家（包括旧档）在登录时自动获得 Curios 的 "tank" 槽位。
 * Curios 的槽位注册（datapack）不会自动添加到已存在的玩家数据。
 */
public class BigFluidTankCuriosIntegration {
    public static final String TANK_SLOT_ID = "big_fluid_tank";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        if (!ModList.get().isLoaded("curios")) {
            return;
        }
        top.theillusivec4.curios.api.type.util.ISlotHelper slotHelper = top.theillusivec4.curios.api.CuriosApi.getSlotHelper();
        if (slotHelper == null) {
            return;
        }
        if (slotHelper.getSlotType(TANK_SLOT_ID).isEmpty()) {
            return;
        }
        if (slotHelper.getSlotsForType(player, TANK_SLOT_ID) <= 0) {
            slotHelper.setSlotsForType(TANK_SLOT_ID, player, 1);
        }
    }
}
