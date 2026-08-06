package com.jdte.common.factory;

import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

final class AE2CrystalScienceFactoryMoveIntegration {
    private AE2CrystalScienceFactoryMoveIntegration() {}

    static void prepareRemoval(BlockEntity blockEntity) {
        if (!(blockEntity instanceof EnderBroadcasterBlockEntity broadcaster)
                || broadcaster.getLevel() == null || broadcaster.getLevel().isClientSide()) return;

        String bandName = broadcaster.getBandName();
        broadcaster.onChunkUnloaded();
        if (bandName == null || bandName.isEmpty()) return;

        var band = FrequencyBandManager.getBand(bandName);
        if (band == null) return;
        GlobalPos oldPosition = GlobalPos.of(broadcaster.getLevel().dimension(), broadcaster.getBlockPos());
        band.undeclareSender(oldPosition);
        band.undeclareReceiver(oldPosition);
        FrequencyBandManager.markRuntimeDirty(broadcaster.getLevel().getServer(), bandName);
        FrequencyBandManager.markDirty();
    }

    static void finalizeMove(BlockEntity blockEntity, ResourceLocation sourceDimension, BlockPos sourcePos) {
        if (!(blockEntity instanceof EnderBroadcasterBlockEntity broadcaster)
                || broadcaster.getLevel() == null || broadcaster.getLevel().isClientSide()
                || sourceDimension == null) return;
        String bandName = broadcaster.getBandName();
        if (bandName == null || bandName.isEmpty()) return;
        var band = FrequencyBandManager.getBand(bandName);
        if (band == null) return;

        ResourceKey<Level> sourceKey = ResourceKey.create(Registries.DIMENSION, sourceDimension);
        GlobalPos oldPosition = GlobalPos.of(sourceKey, sourcePos);
        band.undeclareSender(oldPosition);
        band.undeclareReceiver(oldPosition);
        FrequencyBandManager.markRuntimeDirty(broadcaster.getLevel().getServer(), bandName);
        FrequencyBandManager.markDirty();
    }
}
