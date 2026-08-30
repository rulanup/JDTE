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

    private static final class FakeSource implements AEExtractionTransfer.Source {
        private long amount;
        private long simulateLimit = Long.MAX_VALUE;
        private long actualLimit = Long.MAX_VALUE;
        private long restoreLimit = Long.MAX_VALUE;
        private final List<String> calls = new ArrayList<>();

        private FakeSource(long amount) { this.amount = amount; }

        @Override
        public long extract(long requested, boolean simulate) {
            calls.add((simulate ? "simulate:" : "actual:") + requested);
            long extracted = Math.min(amount, Math.min(requested, simulate ? simulateLimit : actualLimit));
            if (!simulate) amount -= extracted;
            return extracted;
        }

        @Override
        public long restore(long requested) {
            calls.add("restore:" + requested);
            long restored = Math.min(requested, restoreLimit);
            amount += restored;
            return restored;
        }

        private long amount() { return amount; }
    }

    private static final class FakeSink implements AEExtractionTransfer.Sink {
        private long simulateLimit;
        private long actualLimit;
        private final List<String> calls = new ArrayList<>();

        private FakeSink(long simulateLimit, long actualLimit) {
            this.simulateLimit = simulateLimit;
            this.actualLimit = actualLimit;
        }

        @Override
        public long insert(long requested, boolean simulate) {
            calls.add((simulate ? "simulate:" : "actual:") + requested);
            return Math.min(requested, simulate ? simulateLimit : actualLimit);
        }
    }
}
