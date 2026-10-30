package com.jdte.common.network.handler;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.network.PacketContext;
import com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload;
public final class ExtendedExperienceHolderSettingsPacket {
    private ExtendedExperienceHolderSettingsPacket(){}
    public static boolean apply(net.minecraft.world.entity.player.Player player,ExtendedExperienceHolderSettingsPayload payload){
        if(!(player.containerMenu instanceof ExtendedExperienceHolderContainer menu)||!menu.getBlockPos().equals(payload.blockPos())||!menu.stillValid(player)||payload.targetExp()<0||payload.targetExp()>1000)return false;
        var b=(ExtendedExperienceHolderBE)menu.baseMachineBE;
        if(b.ownerOnly&&!player.getUUID().equals(b.placedByUUID))return false;
        b.changeSettings(player,payload.targetExp(),b.ownerOnly,b.collectExp,b.showParticles);menu.broadcastChanges();return true;
    }
    public static void handle(ExtendedExperienceHolderSettingsPayload payload,PacketContext context){context.enqueueWork(()->apply(context.player(),payload));}
}
