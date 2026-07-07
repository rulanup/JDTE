package com.jdte.common.autoioconfig;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class AutoIoConfigData implements INBTSerializable<CompoundTag> {
    public static final int SIDE_COUNT = 6;
    public static final int DEFAULT_SIDE_MASK = 0;
    public static final int ALL_SIDES_MASK = (1 << SIDE_COUNT) - 1;

    private int sideMask = DEFAULT_SIDE_MASK;

    public int getSideMask() {
        return sideMask & ALL_SIDES_MASK;
    }

    public void setSideMask(int sideMask) {
        this.sideMask = sideMask & ALL_SIDES_MASK;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sideMask", getSideMask());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setSideMask(nbt.getInt("sideMask"));
    }
}
