package com.jdte.client;

import com.direwolf20.justdirethings.client.KeyBindings;
import com.jdte.JDTE;
import com.jdte.client.screens.UltimatePortalRadialMenu;
import com.jdte.common.items.UltimatePortalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opens the Ultimate Portal Gun editor with JDT's V key. */
@Mod.EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public final class UltimatePortalGunClientEvents {
    private UltimatePortalGunClientEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        ItemStack gun = UltimatePortalGunItem.find(minecraft.player);
        if (!gun.isEmpty() && KeyBindings.toggleTool.consumeClick()
                && !(minecraft.screen instanceof UltimatePortalRadialMenu)) {
            minecraft.setScreen(new UltimatePortalRadialMenu(gun));
        }
    }
}
