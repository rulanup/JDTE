package com.jdte.common.blockentities;

import net.neoforged.neoforge.energy.IEnergyStorage;

final class AdvancedEnergyTransmitterScheduler {
    private AdvancedEnergyTransmitterScheduler() {
    }

    static int attemptBudget(int targetCount, int configuredBudget) {
        return Math.min(Math.max(0, targetCount), Math.max(0, configuredBudget));
    }

    static int normalizeCursor(int cursor, int targetCount) {
        return targetCount <= 0 ? 0 : Math.floorMod(cursor, targetCount);
    }

    static int targetIndex(int startIndex, int attempted, int targetCount) {
        if (targetCount <= 0) {
            return 0;
        }
        return (int) Math.floorMod((long) startIndex + attempted, targetCount);
    }

    static int advanceCursor(int startIndex, int attempted, int targetCount) {
        if (targetCount <= 0) {
            return 0;
        }
        return targetIndex(startIndex, Math.max(1, attempted), targetCount);
    }

    static long scanBatchEnd(long scanIndex, long scanVolume, int configuredBudget) {
        long start = Math.max(0L, scanIndex);
        long budget = Math.max(0, configuredBudget);
        return Math.min(Math.max(0L, scanVolume), saturatingAdd(start, budget));
    }

    static long saturatingAdd(long current, long increment) {
        if (increment <= 0L) {
            return Math.max(0L, current);
        }
        long result = current + increment;
        return result < 0L || result < current ? Long.MAX_VALUE : result;
    }

    static long saturatingMultiply(long value, int multiplier) {
        if (value <= 0L || multiplier <= 0) {
            return 0L;
        }
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }

    static int clampToInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, value));
    }

    static long clampExternalResult(long requested, long reported) {
        return Math.min(Math.max(0L, requested), Math.max(0L, reported));
    }

    static long consumeReserve(long reserve, long consumed) {
        return Math.max(0L, Math.max(0L, reserve) - Math.max(0L, consumed));
    }

    static long boundedReceive(IEnergyStorage receiver, int demand, long available,
                               int maxCalls) {
        int remaining = Math.max(0, demand);
        long availableEnergy = Math.max(0L, available);
        long transferred = 0L;
        for (int call = 0;
             call < Math.max(0, maxCalls) && remaining > 0 && availableEnergy > 0L;
             call++) {
            int offered = (int) Math.min(Math.min(availableEnergy, Integer.MAX_VALUE), remaining);
            int accepted = Math.min(offered, Math.max(0,
                    receiver.receiveEnergy(offered, false)));
            if (accepted <= 0) {
                break;
            }
            remaining -= accepted;
            availableEnergy -= accepted;
            transferred = saturatingAdd(transferred, accepted);
        }
        return transferred;
    }
}