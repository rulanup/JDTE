package com.jdte.common.network.data;

import com.jdte.JDTE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PotionBrewerRecipeLockSyncPayload(BlockPos blockPos, boolean locked, List<ItemStack> templates) implements CustomPacketPayload {
    public static final Type<PotionBrewerRecipeLockSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "potion_brewer_recipe_lock_sync"));

    @Override
    public Type<PotionBrewerRecipeLockSyncPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionBrewerRecipeLockSyncPayload> STREAM_CODEC = StreamCodec.of(
            PotionBrewerRecipeLockSyncPayload::encode,
            PotionBrewerRecipeLockSyncPayload::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, PotionBrewerRecipeLockSyncPayload payload) {
        BlockPos.STREAM_CODEC.encode(buf, payload.blockPos());
        ByteBufCodecs.BOOL.encode(buf, payload.locked());
        buf.writeVarInt(AdvancedPotionBrewerBE.TOTAL_SLOTS);
        for (int i = 0; i < AdvancedPotionBrewerBE.TOTAL_SLOTS; i++) {
            ItemStack stack = i < payload.templates().size() ? payload.templates().get(i) : ItemStack.EMPTY;
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    }

    private static PotionBrewerRecipeLockSyncPayload decode(RegistryFriendlyByteBuf buf) {
        BlockPos blockPos = BlockPos.STREAM_CODEC.decode(buf);
        boolean locked = ByteBufCodecs.BOOL.decode(buf);
        int slots = buf.readVarInt();
        NonNullList<ItemStack> templates = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < slots; i++) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            if (i < templates.size()) {
                templates.set(i, stack);
            }
        }
        return new PotionBrewerRecipeLockSyncPayload(blockPos, locked, templates);
    }
}
