package com.jdte.common.blockentities;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BioFactoryPendingOutputTest {
    private static final HolderLookup.Provider REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test
    void keepsGeneratedOutputsStableWhileBlocked() {
        BioFactoryPendingOutput pending = BioFactoryPendingOutput.of(
                List.of(new ItemStack(Items.BEEHIVE, 3)),
                new FluidStack(Fluids.WATER, 250), 120, 40, 30, 10, false,
                new int[]{1, 2}, new int[]{2, 1});

        ItemStack first = pending.items().getFirst();
        first.setCount(1);

        assertEquals(3, BioFactoryPendingOutput.of(
                List.of(new ItemStack(Items.BEEHIVE, 3)), FluidStack.EMPTY,
                0, 0, 0, 0, true, null, null).items().getFirst().getCount());
        assertEquals(1, pending.items().getFirst().getCount());
        assertEquals(250, pending.fluid().getAmount());
    }

    @Test
    void roundTripsPendingOutputs() {
        BioFactoryPendingOutput original = BioFactoryPendingOutput.of(
                List.of(new ItemStack(Items.BEEHIVE, 3)),
                new FluidStack(Fluids.WATER, 250), 120, 40, 30, 10, false,
                new int[]{1, 2}, new int[]{2, 1});

        CompoundTag tag = original.save(REGISTRIES);
        BioFactoryPendingOutput restored = BioFactoryPendingOutput.load(tag, REGISTRIES);

        assertEquals(1, restored.items().size());
        assertEquals(Items.BEEHIVE, restored.items().getFirst().getItem());
        assertEquals(3, restored.items().getFirst().getCount());
        assertEquals(Fluids.WATER, restored.fluid().getFluid());
        assertEquals(250, restored.fluid().getAmount());
        assertEquals(120, restored.energyCost());
        assertEquals(40, restored.timeCost());
        assertEquals(30, restored.lifeCost());
        assertEquals(10, restored.processCost());
        assertTrue(!restored.creative());
        assertEquals(2, restored.inputSlots().length);
        assertEquals(2, restored.inputCounts()[0]);
        assertTrue(restored.items().getFirst() != original.items().getFirst());
    }
}
