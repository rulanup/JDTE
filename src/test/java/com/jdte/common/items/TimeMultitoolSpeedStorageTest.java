package com.jdte.common.items;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeMultitoolSpeedStorageTest {
    private static final DataComponentType<Integer> TEST_COMPONENT = DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build();

    @Test
    void storesAndReadsTheSelectedModeWithoutUsingJdtAbilityKeys() {
        ItemStack stack = new ItemStack(Items.STICK);

        TimeMultitoolSpeedStorage.set(stack, TEST_COMPONENT, TimeMultitoolSpeedMode.TWO_FIFTY_SIX);

        assertEquals(TimeMultitoolSpeedMode.TWO_FIFTY_SIX,
                TimeMultitoolSpeedStorage.get(stack, TEST_COMPONENT));
    }

    @Test
    void missingOrInvalidComponentValueFallsBackToOneX() {
        ItemStack stack = new ItemStack(Items.STICK);
        assertEquals(TimeMultitoolSpeedMode.ONE, TimeMultitoolSpeedStorage.get(stack, TEST_COMPONENT));

        stack.set(TEST_COMPONENT, 99);
        assertEquals(TimeMultitoolSpeedMode.ONE, TimeMultitoolSpeedStorage.get(stack, TEST_COMPONENT));
    }
}
