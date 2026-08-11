package com.jdte.common.integrations.ae2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AEItemBatchTransferTest {
    public static void main(String[] args) {
        AEItemBatchTransferTest test = new AEItemBatchTransferTest();
        test.transfersOversizedAndGroupedSourcesInOneBatch();
        test.extractsOnlySimulatedAcceptedAmount();
        test.restoresCommitShortfallToSourcesInReverseOrder();
        test.keepsDifferentKeysInSeparateBatches();
        test.transfersLongMaxFromAnInexhaustibleSourceWithoutDepletion();
        ItemStackBatchSourceTest.main(args);
    }

    @Test
    void transfersOversizedAndGroupedSourcesInOneBatch() {
        FakeSource first = new FakeSource("wheat", 2_100_000L);
        FakeSource second = new FakeSource("wheat", 900_000L);
        RecordingSink sink = new RecordingSink(Long.MAX_VALUE, Long.MAX_VALUE);

        AEItemBatchTransfer.Result<FakeSource> result =
                AEItemBatchTransfer.transfer(List.of(first, second), sink);

        assertEquals(3_000_000L, result.moved());
        assertEquals(0L, first.amount);
        assertEquals(0L, second.amount);
        assertEquals(List.of(3_000_000L), sink.simulatedAmounts);
        assertEquals(List.of(3_000_000L), sink.committedAmounts);
    }

    @Test
    void extractsOnlySimulatedAcceptedAmount() {
        FakeSource first = new FakeSource("wheat", 2_100_000L);
        FakeSource second = new FakeSource("wheat", 900_000L);
        RecordingSink sink = new RecordingSink(2_200_000L, Long.MAX_VALUE);

        AEItemBatchTransfer.Result<FakeSource> result =
                AEItemBatchTransfer.transfer(List.of(first, second), sink);

        assertEquals(2_200_000L, result.moved());
        assertEquals(0L, first.amount);
        assertEquals(800_000L, second.amount);
        assertEquals(List.of(3_000_000L), sink.simulatedAmounts);
        assertEquals(List.of(2_200_000L), sink.committedAmounts);
    }

    @Test
    void restoresCommitShortfallToSourcesInReverseOrder() {
        FakeSource first = new FakeSource("wheat", 2_100_000L);
        FakeSource second = new FakeSource("wheat", 900_000L);
        RecordingSink sink = new RecordingSink(Long.MAX_VALUE, 2_400_000L);

        AEItemBatchTransfer.Result<FakeSource> result =
                AEItemBatchTransfer.transfer(List.of(first, second), sink);

        assertEquals(2_400_000L, result.moved());
        assertEquals(0L, first.amount);
        assertEquals(600_000L, second.amount);
        assertEquals(0L, result.unrestored());
        assertEquals(2, result.changedSources().size());
        assertTrue(result.changedSources().containsAll(List.of(first, second)));
    }

    @Test
    void keepsDifferentKeysInSeparateBatches() {
        FakeSource plain = new FakeSource("wheat", 2_100_000L);
        FakeSource named = new FakeSource("named-wheat", 900_000L);
        RecordingSink sink = new RecordingSink(Long.MAX_VALUE, Long.MAX_VALUE);

        AEItemBatchTransfer.Result<FakeSource> result =
                AEItemBatchTransfer.transfer(List.of(plain, named), sink);

        assertEquals(3_000_000L, result.moved());
        assertEquals(List.of(2_100_000L, 900_000L), sink.simulatedAmounts);
        assertEquals(List.of(2_100_000L, 900_000L), sink.committedAmounts);
    }

    @Test
    void transfersLongMaxFromAnInexhaustibleSourceWithoutDepletion() {
        InfiniteItemBatchSource<String> source = new InfiniteItemBatchSource<>("wheat");
        RecordingSink sink = new RecordingSink(Long.MAX_VALUE, Long.MAX_VALUE);

        AEItemBatchTransfer.Result<InfiniteItemBatchSource<String>> result =
                AEItemBatchTransfer.transfer(List.of(source), sink);

        assertEquals(Long.MAX_VALUE, result.moved());
        assertEquals(Long.MAX_VALUE, source.available());
        assertEquals(List.of(Long.MAX_VALUE), sink.simulatedAmounts);
        assertEquals(List.of(Long.MAX_VALUE), sink.committedAmounts);
        assertEquals(0L, result.unrestored());
    }

    private static final class FakeSource implements AEItemBatchTransfer.Source<String> {
        private final String key;
        private long amount;

        private FakeSource(String key, long amount) {
            this.key = key;
            this.amount = amount;
        }

        @Override public String key() { return key; }
        @Override public long available() { return amount; }

        @Override public long extract(long requested, boolean simulate) {
            long extracted = Math.min(Math.max(0L, requested), amount);
            if (!simulate) amount -= extracted;
            return extracted;
        }

        @Override public long restore(long restored) {
            amount += restored;
            return restored;
        }
    }

    private static final class RecordingSink implements AEItemBatchTransfer.Sink<String> {
        private final long simulateLimit;
        private final long commitLimit;
        private final List<Long> simulatedAmounts = new ArrayList<>();
        private final List<Long> committedAmounts = new ArrayList<>();

        private RecordingSink(long simulateLimit, long commitLimit) {
            this.simulateLimit = simulateLimit;
            this.commitLimit = commitLimit;
        }

        @Override public long insert(String key, long amount, boolean simulate) {
            if (simulate) {
                simulatedAmounts.add(amount);
                return Math.min(amount, simulateLimit);
            }
            committedAmounts.add(amount);
            return Math.min(amount, commitLimit);
        }
    }
}
