package com.jdte.client.smoke;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class OptionalClientSmoke {
 private static int frames;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.optionalSmoke"))return;
  var mc=Minecraft.getInstance();if(!(mc.screen instanceof TitleScreen)||mc.getOverlay()!=null)return;
  if(++frames<80)return;
  for(String mod:new String[]{"ae2","curios","jei","jade"})if(ModList.get().isLoaded(mod))throw new IllegalStateException("Optional fixture unexpectedly loaded "+mod);
  var tank=new net.minecraft.world.item.ItemStack(com.jdte.setup.JDTEItems.BIG_FLUID_TANK.get());
  if(!tank.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())throw new IllegalStateException("Tank fluid capability missing without Curios");
  try{Files.writeString(mc.gameDirectory.toPath().resolve("optional-client-success.txt"),"PASS: real client title, registrations and tank capability without AE2, Curios, JEI or Jade\n");}catch(java.io.IOException ex){throw new IllegalStateException(ex);}
  mc.stop();
 }
}
