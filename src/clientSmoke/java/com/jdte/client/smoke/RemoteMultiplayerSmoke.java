package com.jdte.client.smoke;
import com.jdte.common.containers.*;
import com.jdte.common.network.*;
import com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** Opt-in network client, separately launched twice; no integrated-server shortcuts. */
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class RemoteMultiplayerSmoke {
    private static final Path CONTROL=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-remote-control"));
    private static int ticks,lastPhase=-1,at,step;
    private static String role;
    private static void check(boolean condition,String label){if(!condition)throw new IllegalStateException("Remote client "+role+": "+label);}
    private static void mark(int phase){try{Files.writeString(CONTROL.resolve(role+"-"+phase),"PASS\n");}catch(java.io.IOException e){throw new IllegalStateException(e);}step=99;}
    private static int phase(){try{return Integer.parseInt(Files.readString(CONTROL.resolve("phase")).trim());}catch(java.io.IOException e){return -1;}}
    private static void pick(Minecraft mc,int index){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,index,0,ClickType.PICKUP,mc.player);}
    private static void deposit(Minecraft mc){int index=java.util.stream.IntStream.range(mc.player.containerMenu.slots.size()-36,mc.player.containerMenu.slots.size()).filter(i->mc.player.containerMenu.slots.get(i).getItem().isEmpty()).findFirst().orElseThrow();pick(mc,index);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.remoteSmoke"))return;
        Minecraft mc=Minecraft.getInstance();if(role==null)role=mc.getUser().getName().endsWith("One")?"One":"Two";
        ticks++;int phase=phase();
        if(phase!=lastPhase){lastPhase=phase;at=ticks;step=0;}
        if(phase==8){if(step==0){if(mc.level!=null)mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());step=99;}return;}
        if((phase==7&&role.equals("One")||phase==9)&&mc.player==null&&step==0&&ticks-at>25){
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,
                net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25579"),new net.minecraft.client.multiplayer.ServerData("JDTE fixture","127.0.0.1:25579",false),false);
            step=1;
        }
        if(phase==11){try{Files.writeString(CONTROL.resolve(role+"-stopped-"+phase),"PASS\n");}catch(java.io.IOException e){throw new IllegalStateException(e);}mc.stop();return;}
        if(ticks-at>2200)throw new IllegalStateException("remote timeout "+role+" phase "+phase+" screen "+mc.screen);
        if(mc.player==null||mc.level==null||ticks-at<25)return;
        try{run(mc,phase);}catch(Throwable failure){try{Files.writeString(CONTROL.resolve("client-failure-"+role),failure.toString());}catch(java.io.IOException e){failure.addSuppressed(e);}throw failure;}
    }
    private static void run(Minecraft mc,int phase){
        if(step==99)return;
        int elapsed=ticks-at;
        if(phase==-2||phase==9){
            if(mc.level.getBlockEntity(new BlockPos(1023,80,1024)) instanceof com.jdte.common.blockentities.MineralExtractorBE && mc.player.getX()>1020)mark(phase);
        }else if(phase==1||phase==2){
            if(!(mc.player.containerMenu instanceof MineralExtractorContainer menu))return;
            if(step==0){if(phase==2){MineralPackets.requestPage(menu.containerId,role.equals("One")?1:3);step=1;at=ticks;}else{pick(mc,1);step=2;at=ticks;}}
            else if(step==1&&elapsed>20){check(menu.slots.get(1).getItem().getCount()==(role.equals("One")?800:4096),"independent page snapshot");pick(mc,1);step=2;at=ticks;}
            else if(step==2&&elapsed>20){check(menu.getCarried().getCount()==64,"legal remote cursor count");deposit(mc);step=3;at=ticks;}
            else if(step==3&&elapsed>20){check(menu.getCarried().isEmpty(),"cursor emptied after deposit");check(mc.player.getInventory().countItem(Items.IRON_ORE)==(phase==1?64:128),"acknowledged remote inventory");if(phase==2)net.minecraft.client.Screenshot.grab(mc.gameDirectory.toPath().toFile(),"jdte-remote-"+role+".png",mc.getMainRenderTarget(),message->{});mark(phase);}
        }else if(phase==3){
            if(!(mc.player.containerMenu instanceof MineralExtractorContainer menu))return;
            if(step<3&&elapsed>20){JDTEPacketHandler.CHANNEL.sendToServer(new AdvancedUpgradeStorageActionPayload(1,0,0,step==2?menu.containerId+1:menu.containerId));step++;at=ticks;}
            else if(step==3&&elapsed>25){check(com.jdte.common.upgrades.UpgradeHelper.countUpgrades(menu.baseMachineBE,com.jdte.common.upgrades.UpgradeType.CAPACITY)==3,"shared upgrade updates");mark(3);}
        }else if(phase==4){
            if(mc.screen!=null||!mc.player.getMainHandItem().is(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get()))return;
            if(step==0){var pos=new BlockPos(1023,80,1024);mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));step=1;at=ticks;}
            else if(elapsed>25)mark(4);
        }else if(phase==5){
            if(step==0){if(!mc.player.getMainHandItem().is(JDTEItems.ADVANCED_UPGRADE_STORAGE.get()))return;mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);step=1;at=ticks;}
            else if(mc.player.containerMenu instanceof AdvancedUpgradeStorageContainer menu){
                if(step==1&&elapsed>20){pick(mc,0);step=2;at=ticks;}
                else if(step==2&&elapsed>20){check(!menu.getCarried().isEmpty(),"remote bag extraction");deposit(mc);step=3;at=ticks;}
                else if(step==3&&elapsed>20){for(int i=36;i<menu.slots.size();i++)if(menu.slots.get(i).getItem().is(JDTEItems.CAPACITY_UPGRADE.get()))mc.gameMode.handleInventoryMouseClick(menu.containerId,i,0,ClickType.QUICK_MOVE,mc.player);step=4;at=ticks;}
                else if(step==4&&elapsed>25){check(menu.getCarried().isEmpty()&&menu.stillValid(mc.player),"bound bag remains valid after round trip");mark(5);}
            }
        }else if(phase==6&&role.equals("One")){
            if(step==0){check(mc.player.containerMenu instanceof AdvancedUpgradeStorageContainer,"disconnect from actual storage menu");pick(mc,0);step=1;at=ticks;}
            else if(elapsed>25){check(!mc.player.containerMenu.getCarried().isEmpty(),"disconnect with carried cards");mark(6);if(mc.level!=null)mc.level.disconnect();mc.clearLevel(new net.minecraft.client.gui.screens.TitleScreen());}
        }else if(phase==7){if(elapsed>40)mark(7);
        }else if(phase==10){if(mc.player.containerMenu instanceof MineralExtractorContainer menu&&elapsed>30){check(menu.getCarried().isEmpty(),"restart cursor is empty");check(menu.baseMachineBE instanceof com.jdte.common.blockentities.MineralExtractorBE be&&be.getMultiplier()==17,"restart machine options synchronized");mark(10);}}
    }
}
