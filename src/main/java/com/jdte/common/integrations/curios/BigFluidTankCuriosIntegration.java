package com.jdte.common.integrations.curios;

import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.JDTE;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

/**
 * 确保所有玩家（包括旧档）在登录时自动获得 Curios 的 "tank" 槽位。
 * Curios 的槽位注册（datapack）不会自动添加到已存在的玩家数据。
 */
public class BigFluidTankCuriosIntegration {
    public static final String TANK_SLOT_ID = "big_fluid_tank";
    private static final List<String> SLOT_IDS = List.of(
            TANK_SLOT_ID,
            LargePortableContainerMenus.LARGE_POCKET_GENERATOR_SLOT,
            LargePortableContainerMenus.LARGE_POTION_CANISTER_SLOT,
            LargePortableContainerMenus.LARGE_FUEL_CANISTER_SLOT
    );

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
        for (String slotId : SLOT_IDS) {
            if (slotHelper.getSlotType(slotId).isEmpty()) {
                continue;
            }
            if (slotHelper.getSlotsForType(player, slotId) <= 0) {
                slotHelper.setSlotsForType(slotId, player, 1);
            }
        }
    }
}
