package com.jdte.common.network.data;
import net.minecraft.network.FriendlyByteBuf;
/** The key opens only the validated dedicated Curios slot, never a client-supplied item/NBT. */
public record OpenLargeFuelCanisterPayload(){
    public void encode(FriendlyByteBuf buf){}
    public static OpenLargeFuelCanisterPayload decode(FriendlyByteBuf buf){return new OpenLargeFuelCanisterPayload();}
}
