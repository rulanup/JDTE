package com.jdte.common.network.data;
import net.minecraft.network.FriendlyByteBuf;
/** Resolves only the server's actual dedicated Curios slot; accepts no client item/NBT. */
public record OpenLargePotionCanisterPayload(){
    public void encode(FriendlyByteBuf buffer){}
    public static OpenLargePotionCanisterPayload decode(FriendlyByteBuf buffer){return new OpenLargePotionCanisterPayload();}
}
