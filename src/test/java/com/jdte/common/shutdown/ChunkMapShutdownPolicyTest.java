package com.jdte.common.shutdown;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkMapShutdownPolicyTest {
    @Test
    void runningServerKeepsTheRealGenerationBarrier() {
        CompletableFuture<Void> generationBarrier = new CompletableFuture<>();

        CompletableFuture<?> selected = ChunkMapShutdownPolicy.saveBarrier(false, generationBarrier);

        assertSame(generationBarrier, selected);
        assertFalse(selected.isDone());
        assertFalse(ChunkMapShutdownPolicy.readyForUnload(false, false));
    }

    @Test
    void stoppedServerDoesNotWaitForAnOrphanedGenerationBarrier() {
        CompletableFuture<Void> generationBarrier = new CompletableFuture<>();

        CompletableFuture<?> selected = ChunkMapShutdownPolicy.saveBarrier(true, generationBarrier);

        assertTrue(selected.isDone());
        assertTrue(ChunkMapShutdownPolicy.readyForUnload(true, false));
    }
}
