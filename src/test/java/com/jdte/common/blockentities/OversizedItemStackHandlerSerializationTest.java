package com.jdte.common.blockentities;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OversizedItemStackHandlerSerializationTest {
    private static final HolderLookup.Provider REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test
    void roundTripsHandlerStacksAboveVanillaSaveRange() {
        ItemStackHandler original = new ItemStackHandler(2);
        original.setStackInSlot(0, new ItemStack(Items.BEEHIVE, 616));
        original.setStackInSlot(1, new ItemStack(Items.BEEHIVE, 7620));

        CompoundTag tag = OversizedItemStackHandlerSerialization.serialize(original, REGISTRIES);
        ItemStackHandler restored = new ItemStackHandler(2);
        OversizedItemStackHandlerSerialization.deserialize(restored, REGISTRIES, tag);

        assertEquals(Items.BEEHIVE, restored.getStackInSlot(0).getItem());
        assertEquals(616, restored.getStackInSlot(0).getCount());
        assertEquals(7620, restored.getStackInSlot(1).getCount());
    }

    @Test
    void roundTripsPendingOutputsAboveVanillaSaveRange() {
        BioFactoryPendingOutput original = BioFactoryPendingOutput.of(
                List.of(new ItemStack(Items.BEEHIVE, 7620)),
                new FluidStack(Fluids.WATER, 250), 120, 40, 30, 10, false,
                new int[]{1, 2}, new int[]{2, 1});

        BioFactoryPendingOutput restored = BioFactoryPendingOutput.load(original.save(REGISTRIES), REGISTRIES);

        assertEquals(1, restored.items().size());
        assertEquals(Items.BEEHIVE, restored.items().getFirst().getItem());
        assertEquals(7620, restored.items().getFirst().getCount());
    }

    @Test
    void loadsHandlerStacksWrittenWithoutRealCount() {
        ItemStackHandler original = new ItemStackHandler(1);
        original.setStackInSlot(0, new ItemStack(Items.BEEHIVE, 3));

        ItemStackHandler restored = new ItemStackHandler(1);
        OversizedItemStackHandlerSerialization.deserialize(restored, REGISTRIES, original.serializeNBT(REGISTRIES));

        assertEquals(3, restored.getStackInSlot(0).getCount());
    }
}
