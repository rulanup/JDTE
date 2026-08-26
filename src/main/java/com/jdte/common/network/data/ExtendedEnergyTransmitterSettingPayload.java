package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExtendedEnergyTransmitterSettingPayload(boolean showParticles) implements CustomPacketPayload {
    public static final Type<ExtendedEnergyTransmitterSettingPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "extended_energy_transmitter_setting"));

    public static final StreamCodec<FriendlyByteBuf, ExtendedEnergyTransmitterSettingPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ExtendedEnergyTransmitterSettingPayload::showParticles,
                    ExtendedEnergyTransmitterSettingPayload::new);

    @Override
    public Type<ExtendedEnergyTransmitterSettingPayload> type() {
        return TYPE;
    }
}
