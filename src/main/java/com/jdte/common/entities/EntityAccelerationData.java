package com.jdte.common.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/** Forge persistent entity data replaces the serialized NeoForge attachment. */
public final class EntityAccelerationData {
    public static final String KEY="jdte:entity_acceleration";
    private final int exponent,totalTime;
    private int remainingTime;
    public EntityAccelerationData(int exponent,int totalTime,int remainingTime){this.exponent=Math.max(0,Math.min(15,exponent));this.totalTime=Math.max(0,Math.min(1000000,totalTime));this.remainingTime=Math.max(0,Math.min(this.totalTime,remainingTime));}
    public int getExponent(){return exponent;} public int getTotalTime(){return totalTime;} public int getRemainingTime(){return remainingTime;}
    public boolean isActive(){return remainingTime>0;}
    public void decrementRemainingTime(){remainingTime=Math.max(0,remainingTime-1);}
    public CompoundTag save(){var t=new CompoundTag();t.putInt("exponent",exponent);t.putInt("totalTime",totalTime);t.putInt("remainingTime",remainingTime);return t;}
    public static EntityAccelerationData read(Entity entity){if(!entity.getPersistentData().contains(KEY,10))return null;var t=entity.getPersistentData().getCompound(KEY);return new EntityAccelerationData(t.getInt("exponent"),t.getInt("totalTime"),t.getInt("remainingTime"));}
    public void store(Entity entity){entity.getPersistentData().put(KEY,save());}
}
