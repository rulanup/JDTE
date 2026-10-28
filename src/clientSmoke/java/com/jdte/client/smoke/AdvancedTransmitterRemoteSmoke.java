package com.jdte.client.smoke;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** One real independent client disconnects/reconnects while its dedicated server continues ticking. */
@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class AdvancedTransmitterRemoteSmoke {
    private static int stage,ticks;private static boolean busy;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception{
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.advancedTransmitterRemote")||busy)return;
        var mc=Minecraft.getInstance();var folder=Path.of(System.getProperty("jdte.advancedTransmitterControl"));
        if(++ticks>5000)throw new IllegalStateException("Advanced remote timeout stage "+stage);
        if(!Files.exists(folder.resolve("phase")))return;int phase=Integer.parseInt(Files.readString(folder.resolve("phase")).trim());
        if(phase==1&&stage==0&&mc.player!=null){stage=1;busy=true;try{mc.level.disconnect();mc.clearLevel(new TitleScreen());}finally{busy=false;}}
        else if(phase==2&&stage==1&&mc.player==null){stage=2;ConnectScreen.startConnecting(new TitleScreen(),mc,ServerAddress.parseString("127.0.0.1:25579"),new ServerData("Advanced transmitter fixture","127.0.0.1:25579",false),false);}
        else if(phase==3&&stage==2&&mc.player!=null){stage=3;Files.writeString(folder.resolve("client-success.txt"),"PASS: actual independent client completed dedicated login/logout/reconnect while server stayed online\n");mc.stop();}
    }
}
