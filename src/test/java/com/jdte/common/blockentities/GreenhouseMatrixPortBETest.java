package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseMatrixPortBETest {
    @Test
    void changingControllerStoresLinkBeforeInvalidatingCapabilityCache() {
        BlockPos oldController = new BlockPos(1, 2, 3);
        BlockPos newController = new BlockPos(4, 5, 6);
        AtomicReference<BlockPos> stored = new AtomicReference<>(oldController);
        List<String> calls = new ArrayList<>();

        GreenhouseMatrixPortBE.updateControllerLink(oldController, newController, value -> {
            stored.set(value);
            calls.add("store");
        }, () -> calls.add("invalidate"));

        assertEquals(newController, stored.get());
        assertEquals(List.of("store", "invalidate"), calls);
    }

    @Test
    void unchangedControllerDoesNotInvalidateCapabilityCache() {
        BlockPos controller = new BlockPos(1, 2, 3);
        AtomicInteger stores = new AtomicInteger();
        AtomicInteger invalidations = new AtomicInteger();

        GreenhouseMatrixPortBE.updateControllerLink(controller, controller,
                ignored -> stores.incrementAndGet(), invalidations::incrementAndGet);

        assertEquals(0, stores.get());
        assertEquals(0, invalidations.get());
    }

    @Test
    void unlinkAlsoInvalidatesCapabilityCache() {
        BlockPos controller = new BlockPos(1, 2, 3);
        AtomicReference<BlockPos> stored = new AtomicReference<>(controller);
        AtomicInteger invalidations = new AtomicInteger();

        GreenhouseMatrixPortBE.updateControllerLink(controller, null, stored::set, invalidations::incrementAndGet);

        assertEquals(null, stored.get());
        assertEquals(1, invalidations.get());
    }
}
