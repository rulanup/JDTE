package com.jdte.common.network.data;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;
/** Bounded server-owned snapshot; UUID prevents applying a reused network entity ID. */
public record EntityAccelerationSyncPayload(int entityId,UUID uuid,int exponent,int totalTime,int remainingTime){
 public static EntityAccelerationSyncPayload decode(FriendlyByteBuf b){return new EntityAccelerationSyncPayload(b.readVarInt(),b.readUUID(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
 public void encode(FriendlyByteBuf b){b.writeVarInt(entityId);b.writeUUID(uuid);b.writeVarInt(exponent);b.writeVarInt(totalTime);b.writeVarInt(remainingTime);}
}
