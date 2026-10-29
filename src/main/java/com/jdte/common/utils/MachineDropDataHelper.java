package com.jdte.common.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.util.Set;

/** Forge NBT equivalent of the source helper: retain machine state but never duplicate separately dropped items. */
public final class MachineDropDataHelper {
    private static final Set<String> ITEM_KEYS = Set.of("machineHandler", "itemHandler", "inventory", "factoryPackageSlot", "upgrades", "lootingHandler", "sharpnessHandler");
    private MachineDropDataHelper() { }
    public static void removeSeparatelyDroppedItems(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("JustDiresBEData")) return;
        CompoundTag data = stack.getTag().getCompound("JustDiresBEData");
        ITEM_KEYS.forEach(data::remove);
        CompoundTag caps = data.getCompound("ForgeCaps");
        CompoundTag machine = caps.getCompound("jdte:machine_data");
        machine.remove("upgrade_handler"); machine.remove("extended_upgrade_handler");
        if (data.isEmpty()) stack.getTag().remove("JustDiresBEData");
    }
}
