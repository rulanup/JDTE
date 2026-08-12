package com.jdte.common.greenhouse;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GreenhouseMatrixMemberLookupTest {
    @Test
    void shutdownNeverQueriesMemberChunks() {
        AtomicInteger queries = new AtomicInteger();

        Object member = GreenhouseMatrixMemberLookup.unlessServerStopped(true, () -> {
            queries.incrementAndGet();
            return new Object();
        });

        assertNull(member);
        assertEquals(0, queries.get());
    }

    @Test
    void runningServerMayQueryAnAlreadyLoadedMember() {
        AtomicInteger queries = new AtomicInteger();

        Object member = GreenhouseMatrixMemberLookup.unlessServerStopped(false, () -> {
            queries.incrementAndGet();
            return new Object();
        });

        assertEquals(1, queries.get());
        assertEquals(1, member == null ? 0 : 1);
    }
}
