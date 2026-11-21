package com.jdte.common.blocks;

import com.jdte.test.TestWorldRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluidContainerTransferTest {
    @Test
    void movesWholeBucketsOrLeavesBothHandlersUnchanged() {
        TestWorldRegistries.load();
        var inventory = new ItemStacksResourceHandler(1);
        inventory.set(0, ItemResource.of(Items.BUCKET), 1);
        var bucket = new BucketResourceHandler(ItemAccess.forHandlerIndexStrict(inventory, 0));
        var tank = new FluidStacksResourceHandler(1, 2_000);
        var water = FluidResource.of(Fluids.WATER);
        tank.set(0, water, 800);
        assertFalse(FluidContainerTransfer.transfer(bucket, tank));
        assertEquals(800, tank.getAmountAsInt(0));
        assertTrue(inventory.getResource(0).is(Items.BUCKET));
        tank.set(0, water, 1_500);
        assertTrue(FluidContainerTransfer.transfer(bucket, tank));
        assertEquals(500, tank.getAmountAsInt(0));
        assertTrue(inventory.getResource(0).is(Items.WATER_BUCKET));
        tank.set(0, water, 1_200);
        assertFalse(FluidContainerTransfer.transfer(bucket, tank));
        assertEquals(1_200, tank.getAmountAsInt(0));
        assertTrue(inventory.getResource(0).is(Items.WATER_BUCKET));
        tank.set(0, water, 500);
        assertTrue(FluidContainerTransfer.transfer(bucket, tank));
        assertEquals(1_500, tank.getAmountAsInt(0));
        assertTrue(inventory.getResource(0).is(Items.BUCKET));
    }
}
