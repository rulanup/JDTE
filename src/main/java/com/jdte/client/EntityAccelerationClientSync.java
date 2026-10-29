package com.jdte.client;
import com.jdte.common.entities.EntityAccelerationData;
import com.jdte.common.network.data.EntityAccelerationSyncPayload;
public final class EntityAccelerationClientSync {
 private EntityAccelerationClientSync(){}
 public static void accept(EntityAccelerationSyncPayload p){var world=net.minecraft.client.Minecraft.getInstance().level;if(world==null)return;var entity=world.getEntity(p.entityId());if(entity==null||!entity.getUUID().equals(p.uuid()))return;if(p.remainingTime()<=0)entity.getPersistentData().remove(EntityAccelerationData.KEY);else new EntityAccelerationData(p.exponent(),p.totalTime(),p.remainingTime()).store(entity);}
}
