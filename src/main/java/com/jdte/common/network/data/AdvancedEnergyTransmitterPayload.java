package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AdvancedEnergyTransmitterPayload(BlockPos blockPos, boolean showParticles)
        implements CustomPacketPayload {
    public static final Type<AdvancedEnergyTransmitterPayload> TYPE = new Type<>(
            JDTE.id("advanced_energy_transmitter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedEnergyTransmitterPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, AdvancedEnergyTransmitterPayload::blockPos,
                    ByteBufCodecs.BOOL, AdvancedEnergyTransmitterPayload::showParticles,
                    AdvancedEnergyTransmitterPayload::new);

    @Override
    public Type<AdvancedEnergyTransmitterPayload> type() {
        return TYPE;
    }
}