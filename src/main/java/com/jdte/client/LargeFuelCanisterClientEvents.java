package com.jdte.client;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class LargeFuelCanisterClientEvents {
    @SubscribeEvent public static void key(InputEvent.Key event){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;if(event.getAction()==org.lwjgl.glfw.GLFW.GLFW_PRESS && JDTEKeyMappings.LARGE_POCKET_GENERATOR.isActiveAndMatches(com.mojang.blaze3d.platform.InputConstants.getKey(event.getKey(),event.getScanCode())) && com.jdte.common.containers.LargePocketGeneratorMenuAccess.resolve(mc.player,true).getItem() instanceof com.jdte.common.items.LargePocketGeneratorItem){LargePocketGeneratorCuriosKeys.consumeConflictingOpen(com.mojang.blaze3d.platform.InputConstants.getKey(event.getKey(),event.getScanCode()));while(JDTEKeyMappings.LARGE_POCKET_GENERATOR.consumeClick()){}com.jdte.common.network.JDTEPacketHandler.CHANNEL.sendToServer(new com.jdte.common.network.data.OpenLargePocketGeneratorPayload());}while(JDTEKeyMappings.LARGE_POTION_CANISTER.consumeClick())com.jdte.common.network.JDTEPacketHandler.CHANNEL.sendToServer(new com.jdte.common.network.data.OpenLargePotionCanisterPayload());while(JDTEKeyMappings.LARGE_FUEL_CANISTER.consumeClick())com.jdte.common.network.JDTEPacketHandler.CHANNEL.sendToServer(new com.jdte.common.network.data.OpenLargeFuelCanisterPayload());}
}
