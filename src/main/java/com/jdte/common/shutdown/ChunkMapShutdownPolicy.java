package com.jdte.common.shutdown;

import java.util.concurrent.CompletableFuture;

/** Avoids an unresolvable chunk-generation/save dependency only after the server has entered final shutdown. */
public final class ChunkMapShutdownPolicy {
    private ChunkMapShutdownPolicy() {
    }

    public static CompletableFuture<?> saveBarrier(boolean serverStopped, CompletableFuture<?> normalBarrier) {
        return serverStopped ? CompletableFuture.completedFuture(null) : normalBarrier;
    }

    public static boolean readyForUnload(boolean serverStopped, boolean normallyReady) {
        return serverStopped || normallyReady;
    }
}
