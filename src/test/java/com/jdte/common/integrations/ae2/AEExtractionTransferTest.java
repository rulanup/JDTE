package com.jdte.common.integrations.ae2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AEExtractionTransferTest {
    @Test
    void movesOnlyTheAmountAcceptedByBothSides() {
        FakeSource source = new FakeSource(100);
        FakeSink sink = new FakeSink(80, 80);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);

        assertEquals(80, result.moved());
        assertEquals(20, source.amount());
        assertEquals(0, result.unrestored());
    }

    @Test
    void zeroDemandDoesNotCallEitherEndpoint() {
        FakeSource source = new FakeSource(100);
        FakeSink sink = new FakeSink(100, 100);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(0, source, sink);

        assertEquals(0, result.moved());
        assertEquals(0, result.unrestored());
        assertEquals(List.of(), source.calls);
        assertEquals(List.of(), sink.calls);
    }

    @Test
    void sourceSimulationShortfallLimitsTheTransfer() {
        FakeSource source = new FakeSource(40);
        FakeSink sink = new FakeSink(100, 100);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);

        assertEquals(40, result.moved());
        assertEquals(0, source.amount());
        assertEquals(0, result.unrestored());
        assertEquals(List.of("simulate:100", "actual:40"), source.calls);
        assertEquals(List.of("simulate:100", "actual:40"), sink.calls);
    }

    @Test
    void actualSourceShortfallIsPassedToTheSink() {
        FakeSource source = new FakeSource(100);
        source.actualLimit = 30;
        FakeSink sink = new FakeSink(100, 100);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);

        assertEquals(30, result.moved());
        assertEquals(70, source.amount());
        assertEquals(List.of("simulate:100", "actual:100"), source.calls);
        assertEquals(List.of("simulate:100", "actual:30"), sink.calls);
    }

    @Test
    void actualSinkShortfallRefundsTheSource() {
        FakeSource source = new FakeSource(100);
        FakeSink sink = new FakeSink(100, 60);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);

        assertEquals(60, result.moved());
        assertEquals(40, source.amount());
        assertEquals(0, result.unrestored());
        assertEquals(List.of("simulate:100", "actual:100", "restore:40"), source.calls);
        assertEquals(List.of("simulate:100", "actual:100"), sink.calls);
    }

    @Test
    void refundShortfallIsReported() {
        FakeSource source = new FakeSource(100);
        source.restoreLimit = 10;
        FakeSink sink = new FakeSink(100, 60);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);

        assertEquals(60, result.moved());
        assertEquals(10, source.amount());
        assertEquals(30, result.unrestored());
        assertEquals(List.of("simulate:100", "actual:100", "restore:40"), source.calls);
    }

    @Test
    void capsSingleOperationAtIntegerMaximum() {
        FakeSource source = new FakeSource(Long.MAX_VALUE);
        FakeSink sink = new FakeSink(Long.MAX_VALUE, Long.MAX_VALUE);

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(Long.MAX_VALUE, source, sink);

        assertEquals(Integer.MAX_VALUE, result.moved());
        assertEquals(List.of("simulate:" + Integer.MAX_VALUE, "actual:" + Integer.MAX_VALUE), source.calls);
        assertEquals(List.of("simulate:" + Integer.MAX_VALUE, "actual:" + Integer.MAX_VALUE), sink.calls);
    }


    @Test
    void callsEndpointsInTransactionalOrder() {
        List<String> calls = new ArrayList<>();
        FakeSource source = new FakeSource(100, calls);
        FakeSink sink = new FakeSink(100, 60, calls);

        AEExtractionTransfer.move(100, source, sink);

        assertEquals(List.of(
                "sink simulate:100",
                "source simulate:100",
                "source actual:100",
                "sink actual:100",
                "source restore:40"
        ), calls);
    }

    @Test
    void endpointValuesAreClampedToRequestedAmounts() {
        FakeSource source = new FakeSource(100);
        source.simulateLimit = 200;
        source.actualLimit = 200;
        FakeSink sink = new FakeSink(200, 200);
        sink.simulateLimit = 200;
        sink.actualLimit = 200;

        AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);

        assertEquals(100, result.moved());
        assertEquals(0, source.amount());
        assertEquals(0, result.unrestored());
    }

    @Test
    void negativeEndpointValuesAreClampedToZero() {
        FakeSource source = new FakeSource(100);
        FakeSink sink = new FakeSink(-1, 100);
        assertEquals(new AEExtractionTransfer.Result(0, 0), AEExtractionTransfer.move(100, source, sink));
        assertEquals(List.of(), source.calls);

        source = new FakeSource(100);
        source.simulateLimit = -1;
        sink = new FakeSink(100, 100);
        assertEquals(new AEExtractionTransfer.Result(0, 0), AEExtractionTransfer.move(100, source, sink));
        assertEquals(List.of("simulate:100"), source.calls);
        assertEquals(List.of("simulate:100"), sink.calls);

        source = new FakeSource(100);
        source.actualLimit = -1;
        sink = new FakeSink(100, 100);
        assertEquals(new AEExtractionTransfer.Result(0, 0), AEExtractionTransfer.move(100, source, sink));
        assertEquals(List.of("simulate:100", "actual:100"), source.calls);
        assertEquals(List.of("simulate:100"), sink.calls);

        source = new FakeSource(100);
        sink = new FakeSink(100, -1);
        assertEquals(new AEExtractionTransfer.Result(0, 0), AEExtractionTransfer.move(100, source, sink));
        assertEquals(100, source.amount());

        source = new FakeSource(100);
        source.restoreLimit = -1;
        sink = new FakeSink(100, 60);
        assertEquals(new AEExtractionTransfer.Result(60, 40), AEExtractionTransfer.move(100, source, sink));
        assertEquals(0, source.amount());
    }

    private static final class FakeSource implements AEExtractionTransfer.Source {
        private long amount;
        private long simulateLimit = Long.MAX_VALUE;
        private long actualLimit = Long.MAX_VALUE;
        private long restoreLimit = Long.MAX_VALUE;
        private final List<String> calls = new ArrayList<>();
        private final List<String> globalCalls;

        private FakeSource(long amount) {
            this(amount, null);
        }

        private FakeSource(long amount, List<String> globalCalls) {
            this.amount = amount;
            this.globalCalls = globalCalls;
        }

        @Override
        public long extract(long requested, boolean simulate) {
            calls.add((simulate ? "simulate:" : "actual:") + requested);
            if (globalCalls != null) {
                globalCalls.add("source " + (simulate ? "simulate:" : "actual:") + requested);
            }
            long extracted = Math.min(amount, Math.min(requested, simulate ? simulateLimit : actualLimit));
            if (!simulate) amount -= Math.max(0, extracted);
            return extracted;
        }

        @Override
        public long restore(long requested) {
            calls.add("restore:" + requested);
            if (globalCalls != null) globalCalls.add("source restore:" + requested);
            long restored = Math.min(requested, restoreLimit);
            amount += Math.max(0, restored);
            return restored;
        }

        private long amount() { return amount; }
    }

    private static final class FakeSink implements AEExtractionTransfer.Sink {
        private long simulateLimit;
        private long actualLimit;
        private final List<String> calls = new ArrayList<>();
        private final List<String> globalCalls;

        private FakeSink(long simulateLimit, long actualLimit) {
            this(simulateLimit, actualLimit, null);
        }

        private FakeSink(long simulateLimit, long actualLimit, List<String> globalCalls) {
            this.simulateLimit = simulateLimit;
            this.actualLimit = actualLimit;
            this.globalCalls = globalCalls;
        }

        @Override
        public long insert(long requested, boolean simulate) {
            calls.add((simulate ? "simulate:" : "actual:") + requested);
            if (globalCalls != null) {
                globalCalls.add("sink " + (simulate ? "simulate:" : "actual:") + requested);
            }
            return Math.min(requested, simulate ? simulateLimit : actualLimit);
        }
    }
}
