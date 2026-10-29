package com.jdte.client.smoke;

import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.common.manager.EntityAccelerationManager;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** Client-only opt-in driver: all mutations use vanilla client interaction packets. */
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class WandRemoteSmoke {
 private static final Path CONTROL=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-wand-remote-control"));
 private static final BlockPos A=new BlockPos(1023,80,1024);private static int ticks,phase=-999,at,step;private static String role;
 private static void check(boolean v,String s){if(!v)throw new IllegalStateException("Wand client "+role+": "+s);}
 private static void mark(int p)throws java.io.IOException{Files.writeString(CONTROL.resolve(role+"-"+p),"PASS\n");step=99;}
 private static int read(){try{return Integer.parseInt(Files.readString(CONTROL.resolve("phase")).trim());}catch(java.io.IOException e){return -999;}}
 private static void click(Minecraft mc,int slot,int button){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,slot,button,ClickType.PICKUP,mc.player);}
 private static Cow cow(Minecraft mc){return mc.level.getEntitiesOfClass(Cow.class,new AABB(A).inflate(8)).stream().findFirst().orElse(null);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.wandRemoteSmoke"))return;var mc=Minecraft.getInstance();if(role==null)role=mc.getUser().getName().endsWith("One")?"One":"Two";ticks++;int p=read();if(p!=phase){phase=p;at=ticks;step=0;}
 try{
  if(p==11){mc.stop();return;}
  if(p==8){if(step==0){if(mc.level!=null)mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());step=99;}return;}
  if(((p==7&&role.equals("One"))||p==9)&&mc.player==null&&step==0&&ticks-at>25){net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25579"),new net.minecraft.client.multiplayer.ServerData("Wand live fixture","127.0.0.1:25579",false),false);step=1;}
  check(ticks-at<3000,"timeout phase "+p);if(mc.player==null||mc.level==null||step==99||ticks-at<25)return;
  if(p==-2||p==9){if(mc.level.getBlockEntity(A) instanceof net.minecraft.world.level.block.entity.FurnaceBlockEntity&&cow(mc)!=null&&mc.player.getX()>1020)mark(p);return;}
  if(p==1){if(step==0){mc.setScreen(new InventoryScreen(mc.player));click(mc,9,0);step=1;at=ticks;}else if(step==1&&ticks-at>7){check(mc.player.containerMenu.getCarried().is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()),"real card cursor");click(mc,36,1);step=2;at=ticks;}else if(step==2&&ticks-at>7){click(mc,9,0);step=3;at=ticks;}else if(step==3&&ticks-at>15){check(UltimateTimeWandItem.hasEntityAcceleration(mc.player.getMainHandItem()),"right-click inventory attachment synchronized");check(mc.player.getInventory().getItem(9).getCount()==1&&mc.player.containerMenu.getCarried().isEmpty(),"one card consumed, empty cursor");net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jdte-wand-inventory-"+role+".png",mc.getMainRenderTarget(),msg->{});mc.player.closeContainer();mark(p);}return;}
  if(p==2){mc.options.keyShift.setDown(true);if(step<2&&ticks-at>20+step*12){mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(A).add(0,0,.5),Direction.SOUTH,A,false));step++;}if(step==2&&ticks-at>70){mc.options.keyShift.setDown(false);mark(p);}return;}
  if(p==3){mc.options.keyShift.setDown(false);var cow=cow(mc);if(cow==null)return;if(step<2&&ticks-at>20+step*12){mc.gameMode.interact(mc.player,cow,InteractionHand.MAIN_HAND);step++;}if(step==2&&ticks-at>70)mark(p);return;}
  if(p==4||p==10){var animal=cow(mc);if(animal==null||EntityAccelerationManager.get(animal)==null)return;check(EntityAccelerationManager.get(animal).getExponent()==4,"bounded S2C entity effect");var requests=mc.level.getEntitiesOfClass(UltimateTimeWandEntity.class,new AABB(A).inflate(1));check(requests.size()==1&&requests.get(0).getExponent()==4&&requests.get(0).getRemainingTime()>0,"one synchronized block display/timer");mc.player.setYRot(180);mc.player.setXRot(12);if(p==10){var expected=Files.readString(CONTROL.resolve("wand-"+role)).trim().split(" ");var w=mc.player.getMainHandItem();check(w.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored()==Integer.parseInt(expected[0]),"exact restored FE");check(w.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).getFluidInTank(0).getAmount()==Integer.parseInt(expected[1]),"exact restored fluid");check(Math.abs(UltimateTimeWandItem.pendingFluid(w)-Double.parseDouble(expected[2]))<1e-12,"exact saved fractional debit");}
   if(ticks-at>40){net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jdte-wand-active-"+role+"-"+p+".png",mc.getMainRenderTarget(),msg->{});mark(p);}return;}
  if(p==6&&role.equals("One")){mark(p);mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());return;}
  if(p==7&&ticks-at>45)mark(p);
 }catch(Exception x){try{Files.writeString(CONTROL.resolve("client-failure-"+role),x.toString());}catch(java.io.IOException y){x.addSuppressed(y);}throw new IllegalStateException(x);}
 }
}
