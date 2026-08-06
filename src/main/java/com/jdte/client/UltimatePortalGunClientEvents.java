package com.jdte.client;

import com.direwolf20.justdirethings.client.KeyBindings;
import com.jdte.JDTE;
import com.jdte.client.screens.UltimatePortalRadialMenu;
import com.jdte.common.items.UltimatePortalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * 顶级传送枪按 V 打开分页轮盘。
 * 以最高优先级消费 JDT 的 toggleTool 按键，避免 JDT 同时打开其 12 槽轮盘。
 */
@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public class UltimatePortalGunClientEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack gun = UltimatePortalGunItem.find(mc.player);
        if (gun.isEmpty()) {
            return;
        }
        if (KeyBindings.toggleTool.consumeClick()) {
            if (!(mc.screen instanceof UltimatePortalRadialMenu)) {
                mc.setScreen(new UltimatePortalRadialMenu(gun));
            }
        }
    }
}
