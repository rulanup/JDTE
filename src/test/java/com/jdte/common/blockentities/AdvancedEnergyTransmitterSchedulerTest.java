package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvancedEnergyTransmitterSchedulerTest {

    @Test
    void attemptBudgetIsBoundedByTargetsAndConfiguration() {
        assertEquals(0, AdvancedEnergyTransmitterScheduler.attemptBudget(-1, 10));
        assertEquals(0, AdvancedEnergyTransmitterScheduler.attemptBudget(10, -1));
        assertEquals(4, AdvancedEnergyTransmitterScheduler.attemptBudget(4, 10));
        assertEquals(3, AdvancedEnergyTransmitterScheduler.attemptBudget(10, 3));
    }

    @Test
    void cursorNormalizationAndIndexingWrapInBothDirections() {
        assertEquals(0, AdvancedEnergyTransmitterScheduler.normalizeCursor(7, 0));
        assertEquals(4, AdvancedEnergyTransmitterScheduler.normalizeCursor(-1, 5));
        assertEquals(1, AdvancedEnergyTransmitterScheduler.targetIndex(4, 2, 5));
    }

    @Test
    void boundedRoundsVisitTargetsFairlyAcrossOperations() {
        int targetCount = 5;
        int budget = 2;
        int cursor = 0;
        List<Integer> visited = new ArrayList<>();

        for (int operation = 0; operation < 3; operation++) {
            int attempted = AdvancedEnergyTransmitterScheduler.attemptBudget(targetCount, budget);
            for (int offset = 0; offset < attempted; offset++) {
                visited.add(AdvancedEnergyTransmitterScheduler.targetIndex(cursor, offset, targetCount));
            }
            cursor = AdvancedEnergyTransmitterScheduler.advanceCursor(cursor, attempted, targetCount);
        }

        assertEquals(List.of(0, 1, 2, 3, 4, 0), visited);
        assertEquals(1, cursor);
    }

    @Test
    void zeroAttemptRoundStillMovesPastAStalledTarget() {
        assertEquals(3, AdvancedEnergyTransmitterScheduler.advanceCursor(2, 0, 5));
        assertEquals(0, AdvancedEnergyTransmitterScheduler.advanceCursor(0, 0, 0));
    }

    @Test
    void scanBatchNeverExceedsBudgetOrVolume() {
        assertEquals(15L, AdvancedEnergyTransmitterScheduler.scanBatchEnd(10, 100, 5));
        assertEquals(100L, AdvancedEnergyTransmitterScheduler.scanBatchEnd(98, 100, 5));
        assertEquals(10L, AdvancedEnergyTransmitterScheduler.scanBatchEnd(10, 100, -1));
        assertEquals(5L, AdvancedEnergyTransmitterScheduler.scanBatchEnd(-10, 100, 5));
        assertEquals(Long.MAX_VALUE,
                AdvancedEnergyTransmitterScheduler.scanBatchEnd(
                        Long.MAX_VALUE - 2L, Long.MAX_VALUE, 10));
    }

    @Test
    void transferStatisticsSaturateInsteadOfOverflowing() {
        assertEquals(15L, AdvancedEnergyTransmitterScheduler.saturatingAdd(10L, 5L));
        assertEquals(Long.MAX_VALUE,
                AdvancedEnergyTransmitterScheduler.saturatingAdd(Long.MAX_VALUE - 2L, 10L));
        assertEquals(10L, AdvancedEnergyTransmitterScheduler.saturatingAdd(10L, -5L));
    }

    @Test
    void networkBatchAccountingClampsUntrustedStorageResults() {
        assertEquals(0L, AdvancedEnergyTransmitterScheduler.clampExternalResult(100L, -1L));
        assertEquals(40L, AdvancedEnergyTransmitterScheduler.clampExternalResult(100L, 40L));
        assertEquals(100L, AdvancedEnergyTransmitterScheduler.clampExternalResult(100L, 140L));
        assertEquals(60L, AdvancedEnergyTransmitterScheduler.consumeReserve(100L, 40L));
        assertEquals(100L, AdvancedEnergyTransmitterScheduler.consumeReserve(100L, -1L));
        assertEquals(0L, AdvancedEnergyTransmitterScheduler.consumeReserve(100L, 140L));
    }

    @Test
    void transferBudgetMultiplicationSaturatesAndClampsSafely() {
        assertEquals(0L, AdvancedEnergyTransmitterScheduler.saturatingMultiply(-1L, 8));
        assertEquals(2_147_483_648L,
                AdvancedEnergyTransmitterScheduler.saturatingMultiply(268_435_456L, 8));
        assertEquals(34_359_738_368L,
                AdvancedEnergyTransmitterScheduler.saturatingMultiply(268_435_456L, 128L));
        assertEquals(Long.MAX_VALUE,
                AdvancedEnergyTransmitterScheduler.saturatingMultiply(Long.MAX_VALUE, 2));
        assertEquals(0, AdvancedEnergyTransmitterScheduler.clampToInt(-1L));
        assertEquals(Integer.MAX_VALUE,
                AdvancedEnergyTransmitterScheduler.clampToInt(2_147_483_648L));
    }

    @Test
    void boundedPlayerReceiveRepeatsPerCallLimitedTransfers() {
        LimitedReceiver receiver = new LimitedReceiver(1_000, 100);

        assertEquals(500L, AdvancedEnergyTransmitterScheduler.boundedReceive(
                receiver, 1_000, 1_000L, 5));
        assertEquals(5, receiver.calls);
        assertEquals(500, receiver.stored);
    }

    @Test
    void boundedPlayerReceiveStopsImmediatelyWhenTargetRejects() {
        LimitedReceiver receiver = new LimitedReceiver(1_000, 0);

        assertEquals(0L, AdvancedEnergyTransmitterScheduler.boundedReceive(
                receiver, 1_000, 1_000L, 16));
        assertEquals(1, receiver.calls);
    }

    @Test
    void boundedPlayerReceiveNeverExceedsAvailableEnergy() {
        LimitedReceiver receiver = new LimitedReceiver(1_000, 100);

        assertEquals(250L, AdvancedEnergyTransmitterScheduler.boundedReceive(
                receiver, 1_000, 250L, 16));
        assertEquals(250, receiver.stored);
    }

    private static final class LimitedReceiver implements IEnergyStorage {
        private final int capacity;
        private final int perCall;
        private int stored;
        private int calls;

        private LimitedReceiver(int capacity, int perCall) {
            this.capacity = capacity;
            this.perCall = perCall;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!simulate) {
                calls++;
            }
            int accepted = Math.min(Math.min(maxReceive, perCall), capacity - stored);
            if (!simulate) {
                stored += accepted;
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return stored;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}