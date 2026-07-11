package com.jdte.common.player;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class LifeAppleData implements INBTSerializable<CompoundTag> {
    private long consumed;

    public long getConsumed() {
        return consumed;
    }

    public void setConsumed(long consumed) {
        this.consumed = Math.max(0L, consumed);
    }

    public void increment() {
        if (consumed < Long.MAX_VALUE) {
            consumed++;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("consumed", consumed);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setConsumed(nbt.getLong("consumed"));
    }
}
