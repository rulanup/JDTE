package com.jdte.common.blockentities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

final class OversizedItemStackHandlerSerialization {
    static final int MAX_CODEC_COUNT = 99;
    private static final String REAL_COUNT_TAG = "jdteRealCount";

    private OversizedItemStackHandlerSerialization() {}

    static void serialize(MachineInventoryHandler handler, ValueOutput output) {
        handler.writeUnresolvedItems(output);
        output.putInt("Size", handler.size());
        var items = output.childrenList("Items");
        for (int slot = 0; slot < handler.size(); slot++) {
            if (handler.getResource(slot).isEmpty()) continue;
            var entry = items.addChild();
            serializeStack(handler.getResource(slot).toStack(handler.getAmountAsInt(slot)), entry);
            entry.putInt("Slot", slot);
        }
    }

    static void serializeStack(ItemStack stack, ValueOutput output) {
        int count = stack.getCount();
        if (count > MAX_CODEC_COUNT) {
            output.store(ItemStack.MAP_CODEC, stack.copyWithCount(MAX_CODEC_COUNT));
            output.putInt(REAL_COUNT_TAG, count);
        } else {
            output.store(ItemStack.MAP_CODEC, stack);
        }
    }

    static ItemStack deserializeStack(ValueInput input) {
        ItemStack stack = input.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY);
        return stack.isEmpty() ? stack : stack.copyWithCount(input.getIntOr(REAL_COUNT_TAG, stack.getCount()));
    }

}
