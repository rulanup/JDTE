package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExtendedExperienceHolderPayload(
        BlockPos blockPos,
        boolean add,
        int levels
) implements CustomPacketPayload {
    public static final Type<ExtendedExperienceHolderPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "extended_experience_holder"));

    public static final StreamCodec<FriendlyByteBuf, ExtendedExperienceHolderPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ExtendedExperienceHolderPayload::blockPos,
                    ByteBufCodecs.BOOL, ExtendedExperienceHolderPayload::add,
                    ByteBufCodecs.INT, ExtendedExperienceHolderPayload::levels,
                    ExtendedExperienceHolderPayload::new
            );

    @Override
    public Type<ExtendedExperienceHolderPayload> type() {
        return TYPE;
    }
}
