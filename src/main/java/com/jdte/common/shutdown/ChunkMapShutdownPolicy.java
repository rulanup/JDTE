package com.jdte.common.shutdown;

import java.util.concurrent.CompletableFuture;

/** Avoids an unresolvable chunk-generation/save dependency only after the server has entered final shutdown. */
public final class ChunkMapShutdownPolicy {
    // ChunkMap compares the captured and current unload barriers by identity.
    private static final CompletableFuture<?> FINAL_UNLOAD = CompletableFuture.completedFuture(null);
    private ChunkMapShutdownPolicy() {
    }

    public static CompletableFuture<?> saveBarrier(boolean serverStopped, CompletableFuture<?> normalBarrier) {
        return serverStopped ? FINAL_UNLOAD : normalBarrier;
    }
}
