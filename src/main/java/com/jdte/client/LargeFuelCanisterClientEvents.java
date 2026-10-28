package com.jdte.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class LargeFuelCanisterClientEvents {
    @SubscribeEvent public static void key(InputEvent.Key event){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;while(JDTEKeyMappings.LARGE_FUEL_CANISTER.consumeClick())com.jdte.common.network.JDTEPacketHandler.CHANNEL.sendToServer(new com.jdte.common.network.data.OpenLargeFuelCanisterPayload());}
}
