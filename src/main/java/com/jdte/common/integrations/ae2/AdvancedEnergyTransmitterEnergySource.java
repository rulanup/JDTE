package com.jdte.common.integrations.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public interface AdvancedEnergyTransmitterEnergySource {
    enum Status {
        UNAVAILABLE,
        OFFLINE,
        ONLINE
    }

    AdvancedEnergyTransmitterEnergySource NONE = new AdvancedEnergyTransmitterEnergySource() {
    };

    default void ensureReady(ServerLevel level, BlockPos pos) {
    }

    default long extract(long maxFe) {
        return 0L;
    }

    default long insert(long maxFe) {
        return 0L;
    }

    default Status getStatus() {
        return Status.UNAVAILABLE;
    }

    default void save(CompoundTag tag) {
    }

    default void load(CompoundTag tag) {
    }

    default void destroy() {
    }

    default Object getGridNodeHost() {
        return null;
    }
}