package com.jdte.client.smoke;
import com.jdte.common.containers.LargePotionCanisterContainer;
import com.jdte.common.items.LargePotionCanisterData;
import com.direwolf20.justdirethings.common.items.PotionCanister;
import net.minecraft.client.Minecraft;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class PotionRemoteSmoke {
 private static final Path CONTROL=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-potion-remote-control"));
 private static String role;private static int ticks,phase=-999,at,step,oldId;
 private static void check(boolean value,String text){if(!value)throw new IllegalStateException("Potion client "+role+" phase "+phase+": "+text);}
 private static int read(){try{return Integer.parseInt(Files.readString(CONTROL.resolve("phase")).trim());}catch(java.io.IOException x){return -999;}}
 private static void mark(int n)throws java.io.IOException{Files.writeString(CONTROL.resolve(role+"-"+n),"PASS\n");step=99;}
 private static void shift(Minecraft mc,int slot){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,slot,0,ClickType.QUICK_MOVE,mc.player);}
 private static void stale(Minecraft mc){mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundContainerClickPacket(oldId,0,0,0,ClickType.PICKUP,ItemStack.EMPTY,new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>()));}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
  if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.potionRemoteSmoke"))return;
  var mc=Minecraft.getInstance();if(role==null)role=mc.getUser().getName().endsWith("One")?"One":"Two";ticks++;int p=read();if(p!=phase){phase=p;at=ticks;step=0;}
  try{
   if(p==11){mc.stop();return;}
   if(p==8){if(step==0){if(mc.level!=null)mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());step=99;}return;}
   if(((p==7&&role.equals("Two"))||p==9)&&mc.player==null&&step==0&&ticks-at>25){net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25579"),new net.minecraft.client.multiplayer.ServerData("Potion fixture","127.0.0.1:25579",false),false);step=1;}
   check(ticks-at<3600,"timeout");if(mc.player==null||mc.level==null||step==99||ticks-at<25)return;
   if(p==1){if(step==0){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);step=1;at=ticks;}else if(step==1&&ticks-at>20&&mc.player.containerMenu instanceof LargePotionCanisterContainer m){oldId=m.containerId;shift(mc,1);shift(mc,2);step=2;at=ticks;}else if(step==2&&ticks-at>25){var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(m.slots.get(0).getItem().getCount()==2&&PotionCanister.getPotionAmount(m.getCurrentStack())==0,"two partial inputs persist across real packets");mark(p);}return;}
   if(p==2){if(step==0){shift(mc,3);shift(mc,4);step=1;at=ticks;}else if(step==1&&ticks-at>25){var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(PotionCanister.getPotionAmount(m.getCurrentStack())==1000&&m.slots.get(0).getItem().is(Items.GLASS_BOTTLE)&&m.slots.get(0).getItem().getCount()==4,"complete source batch");shift(mc,0);shift(mc,0);step=2;at=ticks;}else if(step==2&&ticks-at>25){check(mc.player.getInventory().countItem(Items.GLASS_BOTTLE)==4,"repeated output clicks yield four bottles only");mark(p);}return;}
   if(p==3){if(role.equals("One")){stale(mc);check(mc.player.getMainHandItem().isEmpty(),"controlled transfer removed original held item");mark(p);}else if(step==0){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);step=1;at=ticks;}else if(ticks-at>25&&mc.player.containerMenu instanceof LargePotionCanisterContainer m){check(m.getCurrentStack().getTag().getString("fixture_owner").equals("One")&&m.slots.get(0).getItem().getCount()==3&&PotionCanister.getPotionAmount(m.getCurrentStack())==1000,"actual new owner's menu sees transferred pending3/full content");mark(p);}return;}
   if(p==4){if(role.equals("One")){stale(mc);stale(mc);mark(p);}else if(step==0){shift(mc,1);step=1;at=ticks;}else if(step==1&&ticks-at>25){var m=(LargePotionCanisterContainer)mc.player.containerMenu;check(PotionCanister.getPotionAmount(m.getCurrentStack())==2000&&m.slots.get(0).getItem().getCount()==4,"new owner's fourth potion commits exactly once");shift(mc,0);shift(mc,0);step=2;at=ticks;}else if(step==2&&ticks-at>25){check(mc.player.getInventory().countItem(Items.GLASS_BOTTLE)==8,"new batch returns four more bottles, not duplicated");net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jdte-potion-handoff.png",mc.getMainRenderTarget(),m->{});mark(p);}return;}
   if(p==5){if(role.equals("One")){mark(p);}else if(step==0){if(!(mc.player.containerMenu instanceof LargePotionCanisterContainer m)||m.slots.get(0).getItem().getCount()!=2)return;mc.gameMode.handleInventoryMouseClick(m.containerId,0,0,ClickType.PICKUP,mc.player);step=1;at=ticks;}else if(ticks-at>25){check(mc.player.containerMenu.getCarried().is(Items.POTION)&&mc.player.containerMenu.getCarried().getCount()==1,"real cursor holds exactly one potion before logout");mark(p);}return;}
   if(p==6){mark(p);if(role.equals("Two")){mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());}return;}
   if(p==7){if(role.equals("One")){check(mc.player.getInventory().countItem(Items.GLASS_BOTTLE)==4,"other player's resource state unchanged");mark(p);}else if(step<=1){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);step=2;at=ticks;}else if(ticks-at>25&&mc.player.containerMenu instanceof LargePotionCanisterContainer m&&mc.player.getInventory().countItem(Items.POTION)==1){check(m.slots.get(0).getItem().getCount()==1&&mc.player.getInventory().countItem(Items.POTION)==1&&mc.player.containerMenu.getCarried().isEmpty()&&PotionCanister.getPotionAmount(m.getCurrentStack())==2000,"real reconnect keeps pending slot and returned cursor potion once");mark(p);}return;}
   if(p==9){check(mc.player.containerMenu.getCarried().isEmpty(),"fresh server has no stale cursor");mark(p);return;}
   if(p==10){if(role.equals("One")){check(mc.player.getInventory().countItem(Items.GLASS_BOTTLE)==4&&mc.player.getMainHandItem().isEmpty(),"exact first player inventory after restart");mark(p);}else if(step==0){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);step=1;at=ticks;}else if(ticks-at>25&&mc.player.containerMenu instanceof LargePotionCanisterContainer m){check(PotionCanister.getPotionAmount(m.getCurrentStack())==2000&&m.slots.get(0).getItem().getCount()==1&&mc.player.getInventory().countItem(Items.POTION)==1&&mc.player.getInventory().countItem(Items.GLASS_BOTTLE)==8&&LargePotionCanisterData.effects(m.getCurrentStack()).stream().anyMatch(e->e.getDuration()==321&&e.getAmplifier()==2),"actual reopened restarted GUI restores complete contents and exact ledger");net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jdte-potion-restarted.png",mc.getMainRenderTarget(),x->{});mark(p);}return;}
  }catch(Exception x){try{Files.writeString(CONTROL.resolve("client-failure-"+role),x.toString());}catch(java.io.IOException y){x.addSuppressed(y);}throw new IllegalStateException(x);}
 }
}
