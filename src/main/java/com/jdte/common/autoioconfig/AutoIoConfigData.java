package com.jdte.common.autoioconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class AutoIoConfigData implements INBTSerializable<CompoundTag> {
    public static final int SIDE_COUNT = 6;
    public static final int DEFAULT_SIDE_MASK = 0;
    public static final int ALL_SIDES_MASK = (1 << SIDE_COUNT) - 1;

    private int inputMask = DEFAULT_SIDE_MASK;
    private int outputMask = DEFAULT_SIDE_MASK;
    private int transferCooldown;
    private int failureBackoff;
    private long lastTransferGameTime = Long.MIN_VALUE;

    public boolean beginRealServerTick(long gameTime) {
        if (lastTransferGameTime == gameTime) {
            return false;
        }
        lastTransferGameTime = gameTime;
        return true;
    }

    public int getInputMask() {
        return inputMask & ALL_SIDES_MASK;
    }

    public int getOutputMask() {
        return outputMask & ALL_SIDES_MASK;
    }

    public void setMasks(int inputMask, int outputMask) {
        this.inputMask = inputMask & ALL_SIDES_MASK;
        this.outputMask = outputMask & ALL_SIDES_MASK;
        resetTransferState();
    }

    public int getTransferCooldown() {
        return transferCooldown;
    }

    public void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = Math.max(0, transferCooldown);
    }

    public int getFailureBackoff() {
        return failureBackoff;
    }

    public void setFailureBackoff(int failureBackoff) {
        this.failureBackoff = Math.max(0, failureBackoff);
    }

    public void resetTransferState() {
        transferCooldown = 0;
        failureBackoff = 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("inputMask", getInputMask());
        tag.putInt("outputMask", getOutputMask());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("inputMask") || nbt.contains("outputMask")) {
            setMasks(nbt.getInt("inputMask"), nbt.getInt("outputMask"));
            return;
        }
        int legacyMask = nbt.getInt("sideMask");
        setMasks(legacyMask, legacyMask);
    }
}
