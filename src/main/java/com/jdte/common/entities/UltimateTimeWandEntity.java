package com.jdte.common.entities;

import com.jdte.common.blockentities.ExtendedTimeAccelerationManager;
import com.jdte.common.items.UltimateTimeWandData;
import com.jdte.setup.JDTEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Server-owned acceleration request created by the Ultimate Time Wand.
 */
public class UltimateTimeWandEntity extends Entity {
    public static final int DEFAULT_DURATION = 600;

    private static final String TARGET_TAG = "target";
    private static final String EXPONENT_TAG = "exponent";
    private static final String TOTAL_TIME_TAG = "totalTime";
    private static final String REMAINING_TIME_TAG = "remainingTime";

    private static final EntityDataAccessor<Integer> EXPONENT =
            SynchedEntityData.defineId(UltimateTimeWandEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TOTAL_TIME =
            SynchedEntityData.defineId(UltimateTimeWandEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING_TIME =
            SynchedEntityData.defineId(UltimateTimeWandEntity.class, EntityDataSerializers.INT);

    private BlockPos target;

    public UltimateTimeWandEntity(EntityType<? extends UltimateTimeWandEntity> entityType, Level level) {
        super(entityType, level);
    }

    public UltimateTimeWandEntity(Level level, BlockPos target, int exponent) {
        this(level, target, exponent, DEFAULT_DURATION);
    }

    public UltimateTimeWandEntity(Level level, BlockPos target, int exponent, int duration) {
        this(JDTEEntities.ULTIMATE_TIME_WAND.get(), level);
        int safeDuration = Math.max(1, duration);
        applyState(new WandState(target, exponent, safeDuration, safeDuration));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel) || target == null || getRemainingTime() <= 0) {
            discard();
            return;
        }

        int requestedTicks = UltimateTimeWandData.multiplierForExponent(getExponent());
        if (!ExtendedTimeAccelerationManager.submitWand(this, serverLevel, target, requestedTicks)) {
            discard();
            return;
        }

        setRemainingTime(getRemainingTime() - 1);
        if (getRemainingTime() <= 0) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(EXPONENT, 0);
        builder.define(TOTAL_TIME, DEFAULT_DURATION);
        builder.define(REMAINING_TIME, DEFAULT_DURATION);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        applyState(loadState(tag));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.merge(saveState(state()));
    }

    public BlockPos getTarget() {
        return target;
    }

    public int getExponent() {
        return entityData.get(EXPONENT);
    }

    public int getTotalTime() {
        return entityData.get(TOTAL_TIME);
    }

    public int getRemainingTime() {
        return entityData.get(REMAINING_TIME);
    }

    private void setRemainingTime(int remainingTime) {
        entityData.set(REMAINING_TIME, Math.max(0, Math.min(getTotalTime(), remainingTime)));
    }

    public void merge(int exponentStep) {
        applyState(merge(state(), exponentStep, UltimateTimeWandData.MAX_EXPONENT));
    }

    /** Applies a fully planned state or restores the prior state after a failed item transaction. */
    public void applyWandState(WandState state) {
        applyState(state);
    }

    public WandState state() {
        return new WandState(target, getExponent(), getTotalTime(), getRemainingTime());
    }

    public static CompoundTag saveState(WandState state) {
        CompoundTag tag = new CompoundTag();
        tag.put(TARGET_TAG, NbtUtils.writeBlockPos(state.target()));
        tag.putInt(EXPONENT_TAG, state.exponent());
        tag.putInt(TOTAL_TIME_TAG, state.totalTime());
        tag.putInt(REMAINING_TIME_TAG, state.remainingTime());
        return tag;
    }

    public static WandState loadState(CompoundTag tag) {
        BlockPos target = NbtUtils.readBlockPos(tag, TARGET_TAG).orElse(BlockPos.ZERO);
        return new WandState(target, tag.getInt(EXPONENT_TAG), tag.getInt(TOTAL_TIME_TAG), tag.getInt(REMAINING_TIME_TAG));
    }

    public static WandState merge(WandState state, int exponentStep, int maxExponent) {
        int totalTime = Math.max(0, state.totalTime());
        int remainingTime = Math.max(0, Math.min(totalTime, state.remainingTime()));
        int elapsedTime = totalTime - remainingTime;
        int mergedRemainingTime = (int) Math.min(totalTime, (long) remainingTime + elapsedTime / 2L);
        int mergedExponent = (int) Math.min(Math.max(0, maxExponent),
                (long) Math.max(0, state.exponent()) + Math.max(0, exponentStep));
        return new WandState(state.target(), mergedExponent, totalTime, mergedRemainingTime);
    }

    private void applyState(WandState state) {
        target = state.target();
        entityData.set(EXPONENT, Math.max(0, Math.min(UltimateTimeWandData.MAX_EXPONENT, state.exponent())));
        entityData.set(TOTAL_TIME, Math.max(0, state.totalTime()));
        entityData.set(REMAINING_TIME, Math.max(0, Math.min(getTotalTime(), state.remainingTime())));
        if (target != null) {
            moveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
        }
    }

    public record WandState(BlockPos target, int exponent, int totalTime, int remainingTime) {
    }
}
