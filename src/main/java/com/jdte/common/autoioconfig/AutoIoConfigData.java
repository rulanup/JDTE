package com.jdte.common.autoioconfig;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class AutoIoConfigData implements ValueIOSerializable {
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
    public void serialize(ValueOutput output) {
        output.putInt("inputMask", getInputMask());
        output.putInt("outputMask", getOutputMask());
    }

    @Override
    public void deserialize(ValueInput input) {
        var inbound = input.getInt("inputMask");
        var outbound = input.getInt("outputMask");
        if (inbound.isPresent() || outbound.isPresent()) {
            setMasks(inbound.orElse(0), outbound.orElse(0));
            return;
        }
        int legacyMask = input.getIntOr("sideMask", 0);
        setMasks(legacyMask, legacyMask);
    }
}
