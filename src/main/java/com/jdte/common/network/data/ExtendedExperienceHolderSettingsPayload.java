package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExtendedExperienceHolderSettingsPayload(
        BlockPos blockPos,
        int targetExp,
        boolean ownerOnly,
        boolean collectExp,
        boolean showParticles
) implements CustomPacketPayload {
    public static final Type<ExtendedExperienceHolderSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "extended_experience_holder_settings"));

    public static final StreamCodec<FriendlyByteBuf, ExtendedExperienceHolderSettingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ExtendedExperienceHolderSettingsPayload::blockPos,
                    ByteBufCodecs.INT, ExtendedExperienceHolderSettingsPayload::targetExp,
                    ByteBufCodecs.BOOL, ExtendedExperienceHolderSettingsPayload::ownerOnly,
                    ByteBufCodecs.BOOL, ExtendedExperienceHolderSettingsPayload::collectExp,
                    ByteBufCodecs.BOOL, ExtendedExperienceHolderSettingsPayload::showParticles,
                    ExtendedExperienceHolderSettingsPayload::new
            );

    @Override
    public Type<ExtendedExperienceHolderSettingsPayload> type() {
        return TYPE;
    }
}
