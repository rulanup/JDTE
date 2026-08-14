package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GreenhouseMatrixCapabilitySnapshotTest {
    private static final ResourceLocation WATER_ID = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation MILK_ID = ResourceLocation.fromNamespaceAndPath("neoforge", "milk");

    @Test
    void resolvesThousandsOfMembersOnceAndReusesFlattenedSlotIndex() {
        int memberCount = 3_582;
        List<BlockPos> positions = new ArrayList<>(memberCount);
        List<TestItemHandler> handlers = new ArrayList<>(memberCount);
        for (int index = 0; index < memberCount; index++) {
            positions.add(new BlockPos(index, 0, 0));
            handlers.add(new TestItemHandler());
        }
        AtomicInteger resolutions = new AtomicInteger();

        GreenhouseMatrixCapabilitySnapshot snapshot = GreenhouseMatrixCapabilitySnapshot.create(positions, pos -> {
            resolutions.incrementAndGet();
            TestItemHandler handler = handlers.get(pos.getX());
            return new GreenhouseMatrixCapabilitySnapshot.MachineTarget(
                    handler, 0, 2, 2, 5, new FluidTank(1_000), EmptyEnergyStorage.INSTANCE);
        });

        assertEquals(memberCount, resolutions.get());
        assertEquals(memberCount * 2, snapshot.itemSlots(true));
        assertEquals(memberCount * 3, snapshot.itemSlots(false));

        for (int index = 0; index < 10_000; index++) {
            int slot = Math.floorMod(index * 997, snapshot.itemSlots(false));
            GreenhouseMatrixCapabilitySnapshot.ItemTarget target = snapshot.itemTarget(false, slot);
            int member = slot / 3;
            assertSame(handlers.get(member), target.handler());
            assertEquals(2 + slot % 3, target.slot());
        }
        assertEquals(memberCount, resolutions.get(), "slot lookup must not rescan matrix members");
    }

    @Test
    void exposesOneAggregateFluidTankAndSaturatesLargeTotals() {
        FluidTank first = new FluidTank(1_500_000_000);
        FluidTank second = new FluidTank(1_500_000_000);
        first.setFluid(new FluidStack(Fluids.WATER, 30));
        second.setFluid(new FluidStack(Fluids.WATER, 40));

        GreenhouseMatrixCapabilitySnapshot snapshot = GreenhouseMatrixCapabilitySnapshot.create(
                List.of(BlockPos.ZERO, BlockPos.ZERO.above()), pos -> new GreenhouseMatrixCapabilitySnapshot.MachineTarget(
                        new TestItemHandler(), 0, 0, 0, 0,
                        pos.equals(BlockPos.ZERO) ? first : second, EmptyEnergyStorage.INSTANCE));

        assertEquals(1, snapshot.fluidTanks());
        assertEquals(70, snapshot.fluidInTank().getAmount());
        assertEquals(Fluids.WATER, snapshot.fluidInTank().getFluid());
        assertEquals(Integer.MAX_VALUE, snapshot.fluidCapacity());
    }

    @Test
    void keepsVirtualFluidTankExposedWhileMembersAreTemporarilyUnresolved() {
        GreenhouseMatrixCapabilitySnapshot snapshot = GreenhouseMatrixCapabilitySnapshot.create(
                List.of(BlockPos.ZERO), ignored -> null);

        assertEquals(1, snapshot.fluidTanks());
    }

    @Test
    void drainsOnlyTheRequiredFluidAndEnergyAcrossMemberStorages() {
        FluidTank firstFluid = new FluidTank(1_000);
        FluidTank secondFluid = new FluidTank(1_000);
        FluidTank lavaFluid = new FluidTank(1_000);
        FluidStack componentWater = new FluidStack(Fluids.WATER, 30);
        componentWater.set(DataComponents.CUSTOM_NAME, Component.literal("same fluid id"));
        firstFluid.setFluid(componentWater);
        secondFluid.setFluid(new FluidStack(Fluids.WATER, 40));
        lavaFluid.setFluid(new FluidStack(Fluids.LAVA, 50));
        int unchangedLava = lavaFluid.getFluidAmount();
        MutableEnergyStorage firstEnergy = new MutableEnergyStorage(50);
        MutableEnergyStorage secondEnergy = new MutableEnergyStorage(70);
        AtomicInteger index = new AtomicInteger();
        GreenhouseMatrixCapabilitySnapshot snapshot = GreenhouseMatrixCapabilitySnapshot.create(
                List.of(BlockPos.ZERO, BlockPos.ZERO.above(), BlockPos.ZERO.above(2)), ignored -> {
                    int current = index.getAndIncrement();
                    return new GreenhouseMatrixCapabilitySnapshot.MachineTarget(new TestItemHandler(), 0, 0, 0, 0,
                            current == 0 ? firstFluid : current == 1 ? secondFluid : lavaFluid,
                            current == 0 ? firstEnergy : current == 1 ? secondEnergy : EmptyEnergyStorage.INSTANCE);
                });

        assertEquals(70L, snapshot.fluidStoredLong(WATER_ID));
        assertEquals(0L, snapshot.fluidStoredLong(MILK_ID));
        assertEquals(120L, snapshot.energyStoredLong());
        assertEquals(60L, snapshot.drainFluid(WATER_ID, 60L));
        assertEquals(unchangedLava, lavaFluid.getFluidAmount());
        assertEquals(100L, snapshot.extractEnergy(100L));
        assertEquals(10L, snapshot.fluidStoredLong(WATER_ID));
        assertEquals(20L, snapshot.energyStoredLong());
    }

    private static final class TestItemHandler implements IItemHandler {
        @Override public int getSlots() { return 5; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
    }

    private enum EmptyEnergyStorage implements IEnergyStorage {
        INSTANCE;

        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private static final class MutableEnergyStorage implements IEnergyStorage {
        private int stored;
        private MutableEnergyStorage(int stored) { this.stored = stored; }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(stored, maxExtract);
            if (!simulate) stored -= extracted;
            return extracted;
        }
        @Override public int getEnergyStored() { return stored; }
        @Override public int getMaxEnergyStored() { return stored; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }
}
