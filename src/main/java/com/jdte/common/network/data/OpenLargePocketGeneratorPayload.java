package com.jdte.common.network.data;
import net.minecraft.network.FriendlyByteBuf;
/** Resolves only the server's actual dedicated Curios slot; accepts no client item/NBT. */
public record OpenLargePocketGeneratorPayload(){
    public void encode(FriendlyByteBuf buffer){}
    public static OpenLargePocketGeneratorPayload decode(FriendlyByteBuf buffer){return new OpenLargePocketGeneratorPayload();}
}
