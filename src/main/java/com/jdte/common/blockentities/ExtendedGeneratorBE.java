package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedGeneratorBE extends GeneratorT1BE implements ExtendedUpgradeMachine {
    public ExtendedGeneratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_GENERATOR.get(), pos, state);
    }
    private final GeneratorPorts ports = new GeneratorPorts(this);
    @Override public void tickServer() { if (ports.live()) super.tickServer(); }
    @Override public void providePowerAdjacent() { ports.sendAdjacent(getFEPerTick()); }
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
        if (GeneratorPorts.owns(cap)) return ports == null ? net.minecraftforge.common.util.LazyOptional.empty() : ports.get(cap);
        return super.getCapability(cap, side);
    }
    @Override public void onLoad() { if (ports != null) ports.loaded(); super.onLoad(); }
    @Override public void onChunkUnloaded() { ports.unloaded(); super.onChunkUnloaded(); }
    @Override public void invalidateCaps() { if (ports != null) ports.invalidate(); super.invalidateCaps(); }
    @Override public void reviveCaps() { super.reviveCaps(); if (ports != null) ports.revive(); }
}

