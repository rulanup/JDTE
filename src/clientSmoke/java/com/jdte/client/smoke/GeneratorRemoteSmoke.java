package com.jdte.client.smoke;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class GeneratorRemoteSmoke {
    private static final Path CONTROL=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-generator-remote-control"));
    private static final BlockPos A=new BlockPos(1023,80,1024),B=A.east(2);
    private static int ticks,phase=-999,at,step;private static String role;
    private static void check(boolean v,String s){if(!v)throw new IllegalStateException("Generator client "+role+": "+s);}
    private static void mark(int p){try{Files.writeString(CONTROL.resolve(role+"-"+p),"PASS\n");}catch(java.io.IOException e){throw new IllegalStateException(e);}step=99;}
    private static int read(){try{return Integer.parseInt(Files.readString(CONTROL.resolve("phase")).trim());}catch(java.io.IOException e){return -999;}}
    private static void click(Minecraft mc,int slot,ClickType type){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,slot,0,type,mc.player);}
    private static void deposit(Minecraft mc){if(mc.player.containerMenu.getCarried().isEmpty())return;int n=mc.player.containerMenu.slots.size();int slot=java.util.stream.IntStream.range(n-36,n).filter(i->mc.player.containerMenu.slots.get(i).getItem().isEmpty()).findFirst().orElseThrow();click(mc,slot,ClickType.PICKUP);}
    private static void shiftCatalysts(Minecraft mc){int n=mc.player.containerMenu.slots.size();for(int i=n-36;i<n;i++)if(resource(mc.player.containerMenu.slots.get(i).getItem()))click(mc,i,ClickType.QUICK_MOVE);}
    private static boolean resource(ItemStack stack){return phase==5?stack.getItem() instanceof net.minecraft.world.item.BucketItem:stack.is(net.minecraft.world.item.Items.COAL);}
    private static void verify(Minecraft mc,BaseMachineContainer m){var be=(com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE)m.baseMachineBE;check(m.getEnergy()==be.getMaxEnergy()-(phase>=4?37:0),"server-authoritative owned FE sync");if(m instanceof com.jdte.common.containers.ExtendedFluidGeneratorContainer liquid)check(liquid.getFluidAmount()==200000-(phase>=4?7:0),"full-width actual fuel fluid sync");check(com.jdte.common.upgrades.UpgradeHelper.getUpgradeHandler(m.baseMachineBE).getSlots()==8,"eight synchronized upgrade slots");}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.generatorRemoteSmoke"))return;var mc=Minecraft.getInstance();if(role==null)role=mc.getUser().getName().endsWith("One")?"One":"Two";ticks++;
        int p=read();if(p!=phase){phase=p;at=ticks;step=0;}
        try{
            if(p==11){Files.writeString(CONTROL.resolve(role+"-stopped-11"),"PASS\n");mc.stop();return;}
            if(p==8){if(step==0){if(mc.level!=null)mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());step=99;}return;}
            if((p==7&&role.equals("One")||p==9)&&mc.player==null&&step==0&&ticks-at>25){net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25579"),new net.minecraft.client.multiplayer.ServerData("Generator fixture","127.0.0.1:25579",false),false);step=1;}
            check(ticks-at<3000,"timeout phase="+p+" step="+step+" screen="+mc.screen);if(mc.player==null||mc.level==null||step==99||ticks-at<25)return;
            int elapsed=ticks-at;
            if(p==-2||p==9){if(mc.level.getBlockEntity(A) instanceof com.jdte.common.blockentities.ExtendedGeneratorBE&&mc.level.getBlockEntity(B)!=null&&mc.player.getX()>1020)mark(p);}
            else if(p==1){if(!(mc.player.containerMenu instanceof BaseMachineContainer m))return;verify(mc,m);if(step==0){click(mc,0,ClickType.PICKUP);step=1;at=ticks;}else if(step==1&&elapsed>25){check(m.getCarried().isEmpty()||m.getCarried().is(net.minecraft.world.item.Items.COAL)&&m.getCarried().getCount()==64,"shared slot has one legal winner");deposit(mc);step=2;at=ticks;}else if(step==2&&elapsed>25){check(m.getCarried().isEmpty()&&m.slots.get(0).getItem().isEmpty(),"both menus agree shared slot is empty");mark(p);}}
            else if(p==2){if(!(mc.player.containerMenu instanceof BaseMachineContainer m))return;if(step==0){shiftCatalysts(mc);step=1;at=ticks;}else if(elapsed>25){check(m.slots.get(0).getItem().getCount()==64&&m.getCarried().isEmpty(),"shared catalyst Shift return synchronized");mark(p);}}
            else if(p==3){if(!(mc.player.containerMenu instanceof BaseMachineContainer m))return;if(step==0){for(int i=m.slots.size()-36;i<m.slots.size();i++)if(m.slots.get(i).getItem().is(JDTEItems.GENERATOR_UPGRADE.get())){click(mc,i,ClickType.QUICK_MOVE);click(mc,i,ClickType.QUICK_MOVE);}step=1;at=ticks;}else if(elapsed>25){check(com.jdte.common.upgrades.UpgradeHelper.hasGeneratorUpgrade(m.baseMachineBE),"concurrent native Shift upgrades synchronized");verify(mc,m);mark(p);}}
            else if(p==4){if(mc.screen!=null||!mc.player.getMainHandItem().is(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get()))return;if(step==0){mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(A),Direction.UP,A,false));step=1;at=ticks;}else if(elapsed>25)mark(p);}
            else if(p==5){if(!(mc.player.containerMenu instanceof com.jdte.common.containers.ExtendedFluidGeneratorContainer m))return;verify(mc,m);if(step==0){JDTEPacketHandler.CHANNEL.sendToServer(new AdvancedUpgradeStorageActionPayload(1,0,0,m.containerId));click(mc,0,ClickType.PICKUP);step=1;at=ticks;}else if(step==1&&elapsed>25){deposit(mc);step=2;at=ticks;}else if(step==2&&elapsed>25){shiftCatalysts(mc);step=3;at=ticks;}else if(step==3&&elapsed>25){check(m.slots.get(0).getItem().getCount()==1&&m.getCarried().isEmpty(),"Extended shared slot round trip");check(com.jdte.common.upgrades.UpgradeHelper.hasGeneratorUpgrade(m.baseMachineBE),"concurrent bag panel installs one mixing card");mark(p);}}
            else if(p==6&&role.equals("One")){if(step==0){click(mc,0,ClickType.PICKUP);step=1;at=ticks;}else if(elapsed>25){check(mc.player.containerMenu.getCarried().getCount()==1,"disconnect with real carried catalyst");mark(p);mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());}}
            else if(p==7){if(elapsed>40)mark(p);}
            else if(p==10){if(mc.player.containerMenu instanceof BaseMachineContainer m&&elapsed>30){verify(mc,m);check(m.getCarried().isEmpty(),"restart cursor settled");check(com.jdte.common.upgrades.UpgradeHelper.getUpgradeHandler(m.baseMachineBE).getSlots()==8,"restored correct menu tier");net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jdte-generator-remote-"+role+".png",mc.getMainRenderTarget(),msg->{});mark(p);}}
        }catch(Exception x){try{Files.writeString(CONTROL.resolve("client-failure-"+role),x.toString());}catch(java.io.IOException io){x.addSuppressed(io);}throw new IllegalStateException(x);}
    }
}
