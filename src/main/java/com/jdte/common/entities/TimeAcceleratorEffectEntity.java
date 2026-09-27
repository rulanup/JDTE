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
    private int serverRemainingTicks = 15;

    public TimeAcceleratorEffectEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.serverRemainingTicks = 15;
    }

    public TimeAcceleratorEffectEntity(Level level, BlockPos blockPos, int multiplier) {
        this(JDTEEntities.TIME_ACCELERATOR_EFFECT.get(), level);
        this.blockPos = blockPos;
        this.moveTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        this.setTickSpeed(calculateExponent(multiplier));
        this.serverRemainingTicks = 15;
        this.setRemainingTime(15);
        this.setTotalTime(15);
    }

    public static int calculateExponent(int multiplier) {
        return (int) (Math.log(Math.max(1, multiplier)) / Math.log(2));
    }

    public void renew(int multiplier, int duration) {
        int newTickSpeed = calculateExponent(multiplier);
        if (getTickSpeed() != newTickSpeed) {
            setTickSpeed(newTickSpeed);
        }
        int safeDuration = Math.max(1, duration);
        this.serverRemainingTicks = safeDuration;
        if (getTotalTime() != safeDuration) {
            setTotalTime(safeDuration);
        }
        setRemainingTime(safeDuration);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            int current = getRemainingTime();
            if (current > 0) {
                this.entityData.set(REMAINING_TIME, current - 1);
            }
        } else {
            this.serverRemainingTicks--;
            if (this.serverRemainingTicks <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TICKSPEED, 2);
        builder.define(REMAINING_TIME, 15);
        builder.define(TOTAL_TIME, 15);
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
        this.serverRemainingTicks = remainingTime;
    }

    public int getServerRemainingTicks() {
        return this.serverRemainingTicks;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("tickSpeed"))
            this.entityData.set(TICKSPEED, compound.getInt("tickSpeed"));
        if (compound.contains("remainingTime")) {
            int remaining = compound.getInt("remainingTime");
            this.entityData.set(REMAINING_TIME, remaining);
            this.serverRemainingTicks = remaining;
        }
        if (compound.contains("totalTime"))
            this.entityData.set(TOTAL_TIME, compound.getInt("totalTime"));
        if (compound.contains("blockpos")) {
            this.blockPos = NbtUtils.readBlockPos(compound, "blockpos").orElse(null);
            if (this.blockPos != null) {
                this.moveTo(this.blockPos.getX() + 0.5, this.blockPos.getY(), this.blockPos.getZ() + 0.5);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("tickSpeed", getTickSpeed());
        compound.putInt("remainingTime", level().isClientSide ? getRemainingTime() : serverRemainingTicks);
        if (blockPos != null)
            compound.put("blockpos", NbtUtils.writeBlockPos(blockPos));
        compound.putInt("totalTime", getTotalTime());
    }
}
