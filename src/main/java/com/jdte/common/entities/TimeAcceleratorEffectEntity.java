package com.jdte.common.entities;

import com.direwolf20.justdirethings.common.entities.TimeWandEntity;
import com.jdte.setup.JDTEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TimeAcceleratorEffectEntity extends Entity {
    private static final EntityDataAccessor<Integer> TICKSPEED = SynchedEntityData.defineId(TimeAcceleratorEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING_TIME = SynchedEntityData.defineId(TimeAcceleratorEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TOTAL_TIME = SynchedEntityData.defineId(TimeAcceleratorEffectEntity.class, EntityDataSerializers.INT);
    private BlockPos blockPos;

    public TimeAcceleratorEffectEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    public TimeAcceleratorEffectEntity(Level level, BlockPos blockPos, int multiplier) {
        this(JDTEEntities.TIME_ACCELERATOR_EFFECT.get(), level);
        this.blockPos = blockPos;
        this.moveTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        this.setTickSpeed((int) (Math.log(multiplier) / Math.log(2)));
        this.setRemainingTime(10);
        this.setTotalTime(10);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (getRemainingTime() < 0) {
                this.remove(RemovalReason.DISCARDED);
            }
            setRemainingTime(getRemainingTime() - 1);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TICKSPEED, 2);
        builder.define(REMAINING_TIME, 10);
        builder.define(TOTAL_TIME, 10);
    }

    public int getTickSpeed() {
        return this.entityData.get(TICKSPEED);
    }

    public int getTotalTime() {
        return this.entityData.get(TOTAL_TIME);
    }

    public float getAccelerationRate() {
        return TimeWandEntity.calculateAccelRate(getTickSpeed());
    }

    public void setTickSpeed(int tickSpeed) {
        this.entityData.set(TICKSPEED, tickSpeed);
    }

    public void setTotalTime(int totalTime) {
        this.entityData.set(TOTAL_TIME, totalTime);
    }

    public int getRemainingTime() {
        return this.entityData.get(REMAINING_TIME);
    }

    public void setRemainingTime(int remainingTime) {
        this.entityData.set(REMAINING_TIME, remainingTime);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("tickSpeed"))
            this.entityData.set(TICKSPEED, compound.getInt("tickSpeed"));
        if (compound.contains("remainingTime"))
            this.entityData.set(REMAINING_TIME, compound.getInt("remainingTime"));
        if (compound.contains("totalTime"))
            this.entityData.set(TOTAL_TIME, compound.getInt("totalTime"));
        if (compound.contains("blockpos"))
            this.blockPos = NbtUtils.readBlockPos(compound, "blockpos").orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("tickSpeed", getTickSpeed());
        compound.putInt("remainingTime", getRemainingTime());
        if (blockPos != null)
            compound.put("blockpos", NbtUtils.writeBlockPos(blockPos));
        compound.putInt("totalTime", getTotalTime());
    }
}
