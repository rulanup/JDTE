package com.jdte.common.entities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data stored on an entity that is currently accelerated by the Ultimate Time Wand.
 */
public class EntityAccelerationData {
    public static final Codec<EntityAccelerationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("exponent").forGetter(EntityAccelerationData::getExponent),
            Codec.INT.fieldOf("totalTime").forGetter(EntityAccelerationData::getTotalTime),
            Codec.INT.fieldOf("remainingTime").forGetter(EntityAccelerationData::getRemainingTime)
    ).apply(instance, EntityAccelerationData::new));

    public static final StreamCodec<ByteBuf, EntityAccelerationData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EntityAccelerationData::getExponent,
            ByteBufCodecs.VAR_INT, EntityAccelerationData::getTotalTime,
            ByteBufCodecs.VAR_INT, EntityAccelerationData::getRemainingTime,
            EntityAccelerationData::new
    );

    private int exponent;
    private int totalTime;
    private int remainingTime;

    public EntityAccelerationData() {
        this(0, 0, 0);
    }

    public EntityAccelerationData(int exponent, int totalTime, int remainingTime) {
        this.exponent = exponent;
        this.totalTime = totalTime;
        this.remainingTime = remainingTime;
    }

    public int getExponent() {
        return exponent;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void decrementRemainingTime() {
        this.remainingTime--;
    }

    public boolean isActive() {
        return remainingTime > 0;
    }
}
