package com.jdte.common.utils;

import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Set;

/** Removes item storage that the machine block also drops separately during removal. */
public final class MachineDropDataHelper {
    private static final String ATTACHMENTS = "neoforge:attachments";
    private static final Set<String> ITEM_STORAGE_KEYS = Set.of(
            "inventory",
            "factoryPackageSlot",
            "upgrades",
            "lootingHandler",
            "sharpnessHandler"
    );
    private static final Set<String> ITEM_STORAGE_ATTACHMENTS = Set.of(
            "justdirethings:machine_handler",
            "justdirethings:generator_item_handler",
            "justdirethings:generator_fluid_item_handler",
            "jdte:upgrade_handler",
            "jdte:extended_upgrade_handler"
    );

    private MachineDropDataHelper() {
    }

    public static void removeSeparatelyDroppedItems(ItemStack machineStack) {
        CustomData customData = machineStack.get(JustDireDataComponents.CUSTOM_DATA_1);
        if (customData == null) {
            return;
        }

        CompoundTag machineData = customData.copyTag();
        ITEM_STORAGE_KEYS.forEach(machineData::remove);

        if (machineData.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag attachments = machineData.getCompound(ATTACHMENTS);
            ITEM_STORAGE_ATTACHMENTS.forEach(attachments::remove);
            if (attachments.isEmpty()) {
                machineData.remove(ATTACHMENTS);
            }
        }

        if (machineData.isEmpty()) {
            machineStack.remove(JustDireDataComponents.CUSTOM_DATA_1);
        } else {
            machineStack.set(JustDireDataComponents.CUSTOM_DATA_1, CustomData.of(machineData));
        }
    }
}
