package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PotionBrewerRecipeLockPayload(BlockPos blockPos, boolean locked, boolean request) implements CustomPacketPayload {
    public static final Type<PotionBrewerRecipeLockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "potion_brewer_recipe_lock"));

    @Override
    public Type<PotionBrewerRecipeLockPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, PotionBrewerRecipeLockPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PotionBrewerRecipeLockPayload::blockPos,
            ByteBufCodecs.BOOL, PotionBrewerRecipeLockPayload::locked,
            ByteBufCodecs.BOOL, PotionBrewerRecipeLockPayload::request,
            PotionBrewerRecipeLockPayload::new
    );
}
