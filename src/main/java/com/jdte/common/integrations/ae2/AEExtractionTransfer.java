package com.jdte.common.integrations.ae2;

/** Performs a bounded extract/insert operation with rollback on commit shortfall. */
final class AEExtractionTransfer {
    private AEExtractionTransfer() {
    }

    interface Source {
        long extract(long amount, boolean simulate);
        long restore(long amount);
    }

    interface Sink {
        long insert(long amount, boolean simulate);
    }

    record Result(long moved, long unrestored) {
    }

    private static final long MAX_SINGLE_OPERATION = Integer.MAX_VALUE;

    static Result move(long requested, Source source, Sink sink) {
        long demand = Math.min(MAX_SINGLE_OPERATION, Math.max(0L, requested));
        if (demand == 0L) return new Result(0L, 0L);

        long accepted = clamp(sink.insert(demand, true), demand);
        if (accepted == 0L) return new Result(0L, 0L);

        long simulated = clamp(source.extract(accepted, true), accepted);
        if (simulated == 0L) return new Result(0L, 0L);

        long extracted = clamp(source.extract(simulated, false), simulated);
        if (extracted == 0L) return new Result(0L, 0L);

        long moved = clamp(sink.insert(extracted, false), extracted);
        long refund = extracted - moved;
        if (refund == 0L) return new Result(moved, 0L);
        long restored = clamp(source.restore(refund), refund);
        return new Result(moved, refund - restored);
    }

    private static long clamp(long value, long requested) {
        return Math.min(Math.max(0L, value), requested);
    }
}
