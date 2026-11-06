package com.jdte.common.integrations.ae2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AEItemBatchTransfer {
    private AEItemBatchTransfer() {
    }

    interface Source<K> {
        K key();
        long available();
        long extract(long amount, boolean simulate);
        long restore(long amount);
    }

    interface Sink<K> {
        long insert(K key, long amount, boolean simulate);
    }

    record Result<S>(long moved, Set<S> changedSources, long unrestored) {
    }

    static <K, S extends Source<K>> Result<S> transfer(List<S> sources, Sink<K> sink) {
        Map<K, List<S>> grouped = new LinkedHashMap<>();
        for (S source : sources) {
            if (source == null || source.key() == null || source.available() <= 0L) continue;
            grouped.computeIfAbsent(source.key(), ignored -> new ArrayList<>()).add(source);
        }

        long moved = 0L;
        long unrestored = 0L;
        Map<S, Long> netRemoved = new LinkedHashMap<>();
        for (Map.Entry<K, List<S>> entry : grouped.entrySet()) {
            long total = 0L;
            for (S source : entry.getValue()) total = saturatedAdd(total, Math.max(0L, source.available()));
            if (total <= 0L) continue;

            long accepted = clamp(sink.insert(entry.getKey(), total, true), total);
            if (accepted <= 0L) continue;

            long extractedTotal = 0L;
            List<Extraction<S>> extractions = new ArrayList<>();
            for (S source : entry.getValue()) {
                long remaining = accepted - extractedTotal;
                if (remaining <= 0L) break;
                long simulated = clamp(source.extract(remaining, true), remaining);
                if (simulated <= 0L) continue;
                long extracted = clamp(source.extract(simulated, false), simulated);
                if (extracted <= 0L) continue;
                extractedTotal = saturatedAdd(extractedTotal, extracted);
                extractions.add(new Extraction<>(source, extracted));
                netRemoved.merge(source, extracted, AEItemBatchTransfer::saturatedAdd);
            }
            if (extractedTotal <= 0L) continue;
            long inserted = clamp(sink.insert(entry.getKey(), extractedTotal, false), extractedTotal);
            moved = saturatedAdd(moved, inserted);
            long toRestore = extractedTotal - inserted;
            for (int index = extractions.size() - 1; index >= 0 && toRestore > 0L; index--) {
                Extraction<S> extraction = extractions.get(index);
                long requested = Math.min(toRestore, extraction.amount());
                long restored = clamp(extraction.source().restore(requested), requested);
                if (restored <= 0L) continue;
                toRestore -= restored;
                netRemoved.computeIfPresent(extraction.source(),
                        (ignored, removed) -> Math.max(0L, removed - restored));
            }
            unrestored = saturatedAdd(unrestored, toRestore);
        }
        Set<S> changed = new LinkedHashSet<>();
        netRemoved.forEach((source, removed) -> {
            if (removed > 0L) changed.add(source);
        });
        return new Result<>(moved, Set.copyOf(changed), unrestored);
    }

    private static long clamp(long value, long maximum) {
        return Math.min(Math.max(0L, value), Math.max(0L, maximum));
    }

    private static long saturatedAdd(long left, long right) {
        if (right <= 0L) return left;
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private record Extraction<S>(S source, long amount) {
    }
}
