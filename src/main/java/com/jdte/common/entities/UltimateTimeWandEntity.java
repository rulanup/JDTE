package com.jdte.common.entities;

import com.jdte.common.manager.ExtendedTimeAccelerationManager;
import com.jdte.common.items.UltimateTimeWandData;
import com.jdte.setup.JDTEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
        if (level().isClientSide()) {
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
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(EXPONENT, 0);
        builder.define(TOTAL_TIME, DEFAULT_DURATION);
        builder.define(REMAINING_TIME, DEFAULT_DURATION);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        applyState(tag.read(WandState.CODEC).orElse(WandState.EMPTY));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.store(WandState.CODEC, state());
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
        applyState(merge(state(), exponentStep, UltimateTimeWandData.ULTIMATE_MAX_EXPONENT));
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
        tag.store(WandState.CODEC, state);
        return tag;
    }

    public static WandState loadState(CompoundTag tag) {
        return tag.read(WandState.CODEC).orElse(WandState.EMPTY);
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
        entityData.set(EXPONENT, Math.max(0, Math.min(UltimateTimeWandData.ULTIMATE_MAX_EXPONENT, state.exponent())));
        entityData.set(TOTAL_TIME, Math.max(0, state.totalTime()));
        entityData.set(REMAINING_TIME, Math.max(0, Math.min(getTotalTime(), state.remainingTime())));
        if (target != null) {
            snapTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
        }
    }

    public record WandState(BlockPos target, int exponent, int totalTime, int remainingTime) {
        private static final WandState EMPTY = new WandState(BlockPos.ZERO, 0, 0, 0);
        private static final MapCodec<WandState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockPos.CODEC.optionalFieldOf(TARGET_TAG, BlockPos.ZERO).forGetter(WandState::target),
                Codec.INT.optionalFieldOf(EXPONENT_TAG, 0).forGetter(WandState::exponent),
                Codec.INT.optionalFieldOf(TOTAL_TIME_TAG, 0).forGetter(WandState::totalTime),
                Codec.INT.optionalFieldOf(REMAINING_TIME_TAG, 0).forGetter(WandState::remainingTime)
        ).apply(instance, WandState::new));
    }
}
