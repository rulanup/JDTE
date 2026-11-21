package com.jdte.common.player;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class LifeAppleData implements ValueIOSerializable {
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
    public void serialize(ValueOutput output) {
        output.putLong("consumed", consumed);
    }

    @Override
    public void deserialize(ValueInput input) {
        setConsumed(input.getLongOr("consumed", 0L));
    }
}
