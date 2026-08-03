package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AdvancedEnergyTransmitterBindingPayload(BlockPos blockPos)
        implements CustomPacketPayload {
    public static final Type<AdvancedEnergyTransmitterBindingPayload> TYPE = new Type<>(
            JDTE.id("advanced_energy_transmitter_binding"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedEnergyTransmitterBindingPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, AdvancedEnergyTransmitterBindingPayload::blockPos,
                    AdvancedEnergyTransmitterBindingPayload::new);

    @Override
    public Type<AdvancedEnergyTransmitterBindingPayload> type() {
        return TYPE;
    }
}