package com.jdte.common.greenhouse;

import java.util.function.Supplier;

/** Prevents matrix lifecycle cleanup from touching member chunks once final shutdown has begun. */
public final class GreenhouseMatrixMemberLookup {
    private GreenhouseMatrixMemberLookup() {
    }

    public static <T> T unlessServerStopped(boolean serverStopped, Supplier<T> loadedMemberLookup) {
        return serverStopped ? null : loadedMemberLookup.get();
    }
}
