package com.jdte.client.smoke;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.ChargedLightningRodContainer;
import com.jdte.common.entities.MachineLightningEffects;
import com.jdte.setup.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class LightningClientSmoke {
 private static final BlockPos A=new BlockPos(32,110,32);private static int stage,ticks,at,hiddenFrames,visibleFrames,sounds;private static volatile boolean done;private static volatile RuntimeException failure;
 private static void check(boolean v,String s){if(!v)throw new IllegalStateException("Lightning GUI stage "+stage+": "+s);}
 private static void next(){stage++;at=ticks;com.mojang.logging.LogUtils.getLogger().info("Lightning GUI stage {}",stage);}
 private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var s=mc.getSingleplayerServer();var id=mc.player.getUUID();s.execute(()->{try{action.accept(s.getPlayerList().getPlayer(id));}catch(RuntimeException e){failure=e;throw e;}});}
 private static ChargedLightningRodBE be(ServerPlayer p){return (ChargedLightningRodBE)p.serverLevel().getBlockEntity(A);}
 private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});}
 private static mezz.jei.api.runtime.IJeiRuntime jei(){try{var f=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
 @SubscribeEvent public static void sound(net.minecraftforge.client.event.sound.PlaySoundEvent e){if(Boolean.getBoolean("jdte.lightningSmoke")&&e.getSound()!=null&&e.getSound().getLocation().toString().contains("lightning_bolt"))sounds++;}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception{if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.lightningSmoke"))return;if(failure!=null)throw failure;var mc=Minecraft.getInstance();int elapsed=++ticks-at;check(ticks<5000,"timeout");if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
  for(var e:mc.level.getEntitiesOfClass(LightningBolt.class,new AABB(A).inflate(5))){if(!((MachineLightningEffects)e).jdte$isLightningRenderEnabled()&&e.isSilent())hiddenFrames++;if(((MachineLightningEffects)e).jdte$isLightningRenderEnabled()&&!e.isSilent()&&mc.level.getSkyFlashTime()>0)visibleFrames++;}
  switch(stage){
   case 0->{if(mc.player.tickCount<60)return;server(mc,p->{var l=p.serverLevel();l.setWeatherParameters(6000,0,false,false);l.setRainLevel(0);l.setThunderLevel(0);l.setDayTime(6000);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK).set(false,l.getServer());for(var pos:BlockPos.betweenClosed(A.offset(-3,-1,-3),A.offset(3,4,3))){l.removeBlockEntity(pos);l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(A,JDTEBlocks.CHARGED_LIGHTNING_ROD.get().defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,Direction.UP));be(p).getEnergyStorage().setEnergy(9000001);p.getInventory().clearContent();p.getInventory().setItem(9,new ItemStack(JDTEItems.THUNDERSTORM_UPGRADE.get(),2));p.teleportTo(A.getX()+.5,A.getY(),A.getZ()+3.5);done=true;});next();}
   case 1->{if(!done||elapsed<35||!(mc.level.getBlockEntity(A) instanceof ChargedLightningRodBE))return;mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(A),Direction.UP,A,false));next();}
   case 2->{if(!(mc.player.containerMenu instanceof ChargedLightningRodContainer m)||elapsed<25)return;check(m.getEnergy()==9000001&&!m.isWeatherAllowed()&&m.getMode()==ChargedLightningRodMode.NORMAL,"full width FE and blocked clear-weather status");var missing=new BlockPos(1000000,120,1000000);check(!mc.level.hasChunkAt(missing),"unloaded menu-view fixture");var view=new ChargedLightningRodContainer(99,mc.player.getInventory(),missing);for(int i=0;i<8;i++)view.setData(i,0);check(view.slots.size()==m.slots.size()&&!mc.level.hasChunkAt(missing),"client missing-chunk view preserves exact data/upgrade slot protocol without loading/ticking target");shot(mc,"jdte-lightning-waiting.png");mc.gameMode.handleInventoryButtonClick(m.containerId,0);mc.gameMode.handleInventoryButtonClick(m.containerId,1);next();}
   case 3->{if(!(mc.player.containerMenu instanceof ChargedLightningRodContainer m)||elapsed<25)return;check(!m.isRenderEnabled()&&!m.isSoundEnabled(),"both menu controls synchronize");int slot=java.util.stream.IntStream.range(m.slots.size()-36,m.slots.size()).filter(i->m.slots.get(i).getItem().is(JDTEItems.THUNDERSTORM_UPGRADE.get())).findFirst().orElseThrow();mc.gameMode.handleInventoryMouseClick(m.containerId,slot,0,ClickType.QUICK_MOVE,mc.player);next();}
   case 4->{if(!(mc.player.containerMenu instanceof ChargedLightningRodContainer m)||elapsed<35)return;check(m.isWeatherAllowed()&&m.getEnergy()==8990001&&hiddenFrames>0,"actual hidden silent lightning and one10k charge after real card insertion");check(mc.level.getSkyFlashTime()==0&&sounds==0,"hidden/silent lightning produces no sky flash or client sound event");check(mc.player.getInventory().countItem(JDTEItems.THUNDERSTORM_UPGRADE.get())==1,"source maximum one installed, one loose");shot(mc,"jdte-lightning-hidden.png");mc.gameMode.handleInventoryButtonClick(m.containerId,0);mc.gameMode.handleInventoryButtonClick(m.containerId,1);for(int i=0;i<m.slots.size()-36;i++)if(m.slots.get(i).getItem().is(JDTEItems.THUNDERSTORM_UPGRADE.get()))mc.gameMode.handleInventoryMouseClick(m.containerId,i,0,ClickType.QUICK_MOVE,mc.player);next();}
   case 5->{if(!(mc.player.containerMenu instanceof ChargedLightningRodContainer m)||elapsed<25)return;check(m.isRenderEnabled()&&m.isSoundEnabled()&&!m.isWeatherAllowed()&&mc.player.getInventory().countItem(JDTEItems.THUNDERSTORM_UPGRADE.get())==2,"removal restores weather gate without losing cards/flags");var runtime=jei();check(runtime!=null,"JEI actual runtime");var list=runtime.getRecipeManager().createRecipeLookup(mezz.jei.api.constants.RecipeTypes.CRAFTING).get().filter(r->r.getId().equals(com.jdte.JDTE.id("charged_lightning_rod"))||r.getId().equals(com.jdte.JDTE.id("thunderstorm_upgrade"))).toList();check(list.size()==2,"both source shaped recipes visible");runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(mezz.jei.api.constants.RecipeTypes.CRAFTING),list,java.util.List.of());next();}
   case 6->{if(elapsed<30)return;shot(mc,"jdte-lightning-jei.png");mc.screen.onClose();mc.player.closeContainer();done=false;server(mc,p->{var l=p.serverLevel();l.setWeatherParameters(0,6000,true,true);l.setRainLevel(1);l.setThunderLevel(1);var t=be(p).saveWithFullMetadata();t.putInt("chargedLightningRodCooldown",0);be(p).load(t);done=true;});next();}
   case 7->{mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(A.above()));if(!done||visibleFrames<2)return;check(sounds>0,"restored client lightning sound events occur");shot(mc,"jdte-lightning-visible-storm.png");next();}
   case 8->{if(elapsed<20)return;Files.writeString(mc.gameDirectory.toPath().resolve("lightning-smoke-success.txt"),"PASS: real GUI full-width FE, weather gate, both toggles, inventory install/remove and max-one card conservation, hidden/silent client bolt metadata with no sky flash/sound events, real restored visible sky flash and lightning sound event, actual JEI both recipes. Weather deterministically configured through the real integrated server API.\n");mc.stop();stage=9;}
  }
 }
}
