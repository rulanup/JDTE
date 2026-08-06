package com.jdte.common.network.data;

import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PotionBrewerRecipeLockSyncPayload(BlockPos blockPos, boolean locked, List<ItemStack> templates) {
    public static PotionBrewerRecipeLockSyncPayload decode(FriendlyByteBuf buf) {
        BlockPos blockPos = buf.readBlockPos();
        boolean locked = buf.readBoolean();
        int slots = buf.readVarInt();
        NonNullList<ItemStack> templates = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < slots; i++) {
            ItemStack stack = buf.readItem();
            if (i < templates.size()) {
                templates.set(i, stack);
            }
        }
        return new PotionBrewerRecipeLockSyncPayload(blockPos, locked, templates);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(locked);
        buf.writeVarInt(AdvancedPotionBrewerBE.TOTAL_SLOTS);
        for (int i = 0; i < AdvancedPotionBrewerBE.TOTAL_SLOTS; i++) {
            ItemStack stack = i < templates.size() ? templates.get(i) : ItemStack.EMPTY;
            buf.writeItem(stack);
        }
    }
}
