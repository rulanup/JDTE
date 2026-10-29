package com.jdte.client.smoke;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.BioExtractorContainer;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class BioRemoteSmoke {
 private static final Path CONTROL=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-bio-remote-control"));private static final BlockPos A=new BlockPos(1023,80,1024);private static int ticks,phase=-999,at,step;private static String role;
 private static void check(boolean v,String s){if(!v)throw new IllegalStateException("Bio client "+role+": "+s);}
 private static void click(Minecraft mc,int slot,ClickType type){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,slot,0,type,mc.player);}
 private static void mark(int p)throws java.io.IOException{Files.writeString(CONTROL.resolve(role+"-"+p),"PASS\n");step=99;}
 private static int read(){try{return Integer.parseInt(Files.readString(CONTROL.resolve("phase")).trim());}catch(java.io.IOException e){return -999;}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.bioRemoteSmoke"))return;var mc=Minecraft.getInstance();if(role==null)role=mc.getUser().getName().endsWith("One")?"One":"Two";ticks++;int p=read();if(p!=phase){phase=p;at=ticks;step=0;}try{
  if(p==11){mc.stop();return;}if(p==8){if(step==0){if(mc.level!=null)mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());step=99;}return;}
  if(((p==7&&role.equals("One"))||p==9)&&mc.player==null&&step==0&&ticks-at>25){net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25579"),new net.minecraft.client.multiplayer.ServerData("Live Bio fixture","127.0.0.1:25579",false),false);step=1;}
  check(ticks-at<3000,"timeout phase "+p);if(mc.player==null||mc.level==null||step==99||ticks-at<30)return;
  if(p==-2||p==9){if(mc.level.getBlockEntity(A) instanceof AdvancedBioExtractorBE&&mc.player.getX()>1020)mark(p);return;}
  if(p==6&&role.equals("One")){mc.player.closeContainer();mark(p);mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());return;}
  if(p==7){if(ticks-at>80)mark(p);return;}
  if(!(mc.player.containerMenu instanceof BioExtractorContainer m))return;
  check(m.getEnergy()>0&&m.getFluidAmount()>=0,"valid live authoritative FE/milk values");
  if(p==1||p==3){if(ticks-at<130){if(ticks%7==0){for(int i=0;i<((BioExtractorBE)m.baseMachineBE).getOutputSlotCount();i++)if(m.slots.get(i).getItem().is(Items.BLUE_WOOL)){click(mc,i,ClickType.QUICK_MOVE);click(mc,i,ClickType.QUICK_MOVE);}}}else{check(m.getCarried().isEmpty(),"repeated output clicks settle empty cursor");mark(p);}}
  else if(p==2||p==4){if(step==0){int n=m.slots.size();for(int i=n-36;i<n;i++)if(m.slots.get(i).getItem().is(JDTEItems.FILTER_UPGRADE.get())){click(mc,i,ClickType.QUICK_MOVE);click(mc,i,ClickType.QUICK_MOVE);}step=1;at=ticks;}else if(ticks-at>50){check(com.jdte.common.upgrades.UpgradeHelper.countUpgrades(m.baseMachineBE,com.jdte.common.upgrades.UpgradeType.FILTER)==2,"shared upgrade installation sync");mark(p);}}
  else if(p==5){if(ticks-at<80){if(ticks%9==0)for(int i=0;i<m.slots.size()-36;i++)if(m.slots.get(i).getItem().is(JDTEItems.FILTER_UPGRADE.get())){click(mc,i,ClickType.QUICK_MOVE);click(mc,i,ClickType.QUICK_MOVE);}}else{check(com.jdte.common.upgrades.UpgradeHelper.countUpgrades(m.baseMachineBE,com.jdte.common.upgrades.UpgradeType.FILTER)==0,"shared card removal sync");mark(p);}}
  else if(p==10&&ticks-at>45){check(m.getCarried().isEmpty(),"restart cursor");net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jdte-bio-remote-"+role+".png",mc.getMainRenderTarget(),msg->{});mark(p);}
 }catch(Exception x){try{Files.writeString(CONTROL.resolve("client-failure-"+role),x.toString());}catch(java.io.IOException y){x.addSuppressed(y);}throw new IllegalStateException(x);}}
}
