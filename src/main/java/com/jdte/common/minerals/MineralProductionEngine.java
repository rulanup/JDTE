package com.jdte.common.minerals;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class MineralProductionEngine {
    private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100L);

    private MineralProductionEngine() {
    }

    public static List<MineralEntry> select(List<MineralEntry> entries, Predicate<MineralEntry> filter) {
        return entries.stream().filter(filter).toList();
    }

    /** 合并多张清单中的同类矿物；权重饱和累加，避免重复候选和 long 溢出。 */
    public static List<MineralEntry> mergeWeightedEntries(List<MineralEntry> entries) {
        Map<ResourceLocation, MineralEntry> merged = new LinkedHashMap<>();
        for (MineralEntry entry : entries) {
            merged.merge(entry.oreId(), entry, (left, right) -> new MineralEntry(
                    left.oreId(),
                    saturatingAdd(left.weight(), right.weight()),
                    Math.min(left.minY(), right.minY()),
                    Math.max(left.maxY(), right.maxY()),
                    Math.max(left.veinSize(), right.veinSize()),
                    left.confidence()));
        }
        return List.copyOf(merged.values());
    }

    public static boolean allowsListedCandidate(boolean allowlist, boolean hasFilter, boolean listed) {
        return !hasFilter || (allowlist ? listed : !listed);
    }

    public static long scaleOutput(long amount, int resultCount) {
        if (amount <= 0L || resultCount <= 0) return 0L;
        return saturatingMultiply(amount, resultCount);
    }

    public static long accumulateWork(long pendingWork, long elapsedTicks, long multiplier, long maxPendingWork) {
        long limit = Math.max(0L, maxPendingWork);
        long pending = Math.max(0L, Math.min(pendingWork, limit));
        long added = saturatingMultiply(Math.max(0L, elapsedTicks), Math.max(0L, multiplier));
        return Math.min(limit, saturatingAdd(pending, added));
    }

    public static WorkAllocation workForTick(int selectedMultiplier, long baseProductionMultiplier,
                                             boolean accelerationAvailable) {
        long baseWork = Math.max(1L, baseProductionMultiplier);
        long acceleratedWork = accelerationAvailable
                ? saturatingMultiply(Math.max(0L, selectedMultiplier - 1L), baseWork) : 0L;
        return new WorkAllocation(baseWork, acceleratedWork);
    }

    public static boolean shouldSettle(long pendingBaseWork, long pendingAcceleratedWork,
                                       long processTicks, int settlementTicker, int settlementInterval) {
        long requiredWork = Math.max(1L, processTicks);
        return saturatingAdd(Math.max(0L, pendingBaseWork), Math.max(0L, pendingAcceleratedWork)) >= requiredWork
                || settlementTicker >= Math.max(1, settlementInterval);
    }

    public static CycleAllocation allocateCycles(long baseCycles, long acceleratedCycles, long settledCycles) {
        long settled = Math.max(0L, settledCycles);
        long base = Math.min(Math.max(0L, baseCycles), settled);
        long accelerated = Math.min(Math.max(0L, acceleratedCycles), settled - base);
        return new CycleAllocation(base, accelerated);
    }

    public static Batch distribute(List<MineralEntry> entries, long requestedCycles,
                                   long maxCycles, int fortunePercent, RandomSource random) {
        long cycles = Math.max(0L, Math.min(requestedCycles, Math.max(0L, maxCycles)));
        if (cycles == 0L || entries.isEmpty()) return new Batch(0L, Map.of());

        BigInteger totalWeight = entries.stream()
                .map(entry -> BigInteger.valueOf(entry.weight()))
                .reduce(BigInteger.ZERO, BigInteger::add);
        if (totalWeight.signum() <= 0) return new Batch(0L, Map.of());

        long scaledCycles = applyPercent(cycles, Math.max(0, fortunePercent), random);
        BigInteger scaled = BigInteger.valueOf(scaledCycles);
        Map<ResourceLocation, Long> amounts = new LinkedHashMap<>();
        List<Fraction> fractions = new ArrayList<>(entries.size());
        long assigned = 0L;
        for (MineralEntry entry : entries) {
            BigInteger[] quotient = scaled.multiply(BigInteger.valueOf(entry.weight()))
                    .divideAndRemainder(totalWeight);
            long amount = quotient[0].longValueExact();
            if (amount > 0L) amounts.merge(entry.oreId(), amount, MineralProductionEngine::saturatingAdd);
            assigned += amount;
            if (quotient[1].signum() > 0) fractions.add(new Fraction(entry.oreId(), quotient[1]));
        }

        long remainder = scaledCycles - assigned;
        BigInteger remainingFractionWeight = fractions.stream()
                .map(Fraction::remainder)
                .reduce(BigInteger.ZERO, BigInteger::add);
        for (long index = 0L; index < remainder && !fractions.isEmpty(); index++) {
            BigInteger roll = nextBigInteger(random, remainingFractionWeight);
            BigInteger cumulative = BigInteger.ZERO;
            int selected = fractions.size() - 1;
            for (int candidate = 0; candidate < fractions.size(); candidate++) {
                cumulative = cumulative.add(fractions.get(candidate).remainder());
                if (roll.compareTo(cumulative) < 0) {
                    selected = candidate;
                    break;
                }
            }
            Fraction chosen = fractions.remove(selected);
            amounts.merge(chosen.id(), 1L, MineralProductionEngine::saturatingAdd);
            remainingFractionWeight = remainingFractionWeight.subtract(chosen.remainder());
        }
        return new Batch(cycles, Map.copyOf(amounts));
    }

    static long applyPercent(long base, int bonusPercent, RandomSource random) {
        if (base <= 0L || bonusPercent <= 0) return Math.max(0L, base);
        BigInteger[] bonus = BigInteger.valueOf(base)
                .multiply(BigInteger.valueOf(bonusPercent))
                .divideAndRemainder(ONE_HUNDRED);
        BigInteger rounded = bonus[0];
        if (bonus[1].signum() > 0 && nextBigInteger(random, ONE_HUNDRED).compareTo(bonus[1]) < 0) {
            rounded = rounded.add(BigInteger.ONE);
        }
        return saturatingLong(BigInteger.valueOf(base).add(rounded));
    }

    private static BigInteger nextBigInteger(RandomSource random, BigInteger bound) {
        if (bound.signum() <= 0) return BigInteger.ZERO;
        int bits = bound.bitLength();
        BigInteger value;
        do {
            value = BigInteger.ZERO;
            int remaining = bits;
            while (remaining > 0) {
                int chunk = Math.min(30, remaining);
                value = value.shiftLeft(chunk).add(BigInteger.valueOf(random.nextInt(1 << chunk)));
                remaining -= chunk;
            }
        } while (value.compareTo(bound) >= 0);
        return value;
    }

    private static long saturatingLong(BigInteger value) {
        return value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0 ? Long.MAX_VALUE : value.longValue();
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left == 0L || right == 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private record Fraction(ResourceLocation id, BigInteger remainder) {
    }

    public record WorkAllocation(long baseWork, long acceleratedWork) {
        public long totalWork() {
            return saturatingAdd(baseWork, acceleratedWork);
        }
    }

    public record CycleAllocation(long baseCycles, long acceleratedCycles) {
        public long totalCycles() {
            return baseCycles + acceleratedCycles;
        }
    }

    public record Batch(long consumedCycles, Map<ResourceLocation, Long> amounts) {
        public long producedItems() {
            return amounts.values().stream().reduce(0L, MineralProductionEngine::saturatingAdd);
        }
    }
}