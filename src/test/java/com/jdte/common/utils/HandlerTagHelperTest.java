package com.jdte.common.utils;

import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.player.LifeAppleData;
import com.jdte.test.TestWorldRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandlerTagHelperTest implements com.jdte.test.WorldRegistryTest {
    private static HolderLookup.Provider registries;

    @BeforeAll
    static void loadRegistries() {
        registries = TestWorldRegistries.load();
    }

    @Test
    void copierAcceptsLegacySlotIntsAndRejectsOutOfBoundsAndUnknownData() {
        var source = new ItemStackHandler(4);
        source.setStackInSlot(2, new ItemStack(Items.IRON_INGOT, 3));
        var legacy = HandlerTagHelper.write(source, registries);
        var entry = legacy.getListOrEmpty("Items").getCompoundOrEmpty(0);
        entry.putInt("Slot", 2);
        var restored = new ItemStackHandler(4);
        HandlerTagHelper.read(restored, registries, legacy);
        assertEquals(3, restored.getStackInSlot(2).getCount());
        var current = HandlerTagHelper.write(restored, registries);
        assertTrue(HandlerTagHelper.matchesItemHandlerTag(legacy, current));
        entry.putInt("Slot", 258);
        assertFalse(HandlerTagHelper.matchesItemHandlerTag(legacy, current));
        entry.putInt("Slot", 2);
        entry.putInt("extra", 1);
        assertFalse(HandlerTagHelper.matchesItemHandlerTag(legacy, current));
    }

    @Test
    void autoIoLoadsLegacySidesAndPrefersExplicitMasks() {
        var tag = new CompoundTag();
        tag.putInt("sideMask", 65);
        var config = new AutoIoConfigData();
        HandlerTagHelper.read(config, registries, tag);
        assertEquals(1, config.getInputMask());
        assertEquals(1, config.getOutputMask());
        tag.putInt("outputMask", 32);
        HandlerTagHelper.read(config, registries, tag);
        assertEquals(0, config.getInputMask());
        assertEquals(32, config.getOutputMask());
        var saved = HandlerTagHelper.write(config, registries);
        assertEquals(32, saved.getIntOr("outputMask", -1));
        assertEquals(0, saved.getIntOr("inputMask", -1));
    }

    @Test
    void lifeAppleCountRoundTripsAtLongLimit() {
        var consumed = new LifeAppleData();
        consumed.setConsumed(Long.MAX_VALUE);
        var restored = new LifeAppleData();
        HandlerTagHelper.read(restored, registries, HandlerTagHelper.write(consumed, registries));
        restored.increment();
        assertEquals(Long.MAX_VALUE, restored.getConsumed());
    }
}
