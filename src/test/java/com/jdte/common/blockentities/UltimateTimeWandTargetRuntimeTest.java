package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static com.jdte.common.blockentities.UltimateTimeWandTargetRuntime.Route;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UltimateTimeWandTargetRuntimeTest {

    @Test
    void requestedTicksAreCappedBySharedBatchAndBudget() {
        assertEquals(64, UltimateTimeWandTargetRuntime.admit(1024, 64, 64));
        assertEquals(0, UltimateTimeWandTargetRuntime.admit(1024, 64, 0));
    }

    @Test
    void ae2RouteWinsWhenTickableExists() {
        assertEquals(Route.AE2, UltimateTimeWandTargetRuntime.route(true, true, true));
        assertEquals(Route.ORDINARY, UltimateTimeWandTargetRuntime.route(true, false, true));
        assertEquals(Route.ORDINARY, UltimateTimeWandTargetRuntime.route(false, false, true));
    }
}
