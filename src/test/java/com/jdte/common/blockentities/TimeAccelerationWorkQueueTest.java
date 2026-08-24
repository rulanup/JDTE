package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAccelerationWorkQueueTest {

    @Test
    void wandRemainderSurvivesABatchWhileSharingTheGlobalBudget() {
        TimeAccelerationWorkQueue<String, String> queue = new TimeAccelerationWorkQueue<>();
        queue.enqueue("wand-target", "wand", 1024, 0, 2048);
        queue.enqueue("machine-target", "machine", 64, 16, 2048);
        Map<String, Integer> executed = new LinkedHashMap<>();

        long usedBudget = queue.execute(128, 64,
                (target, requestedTicks, remainingBudget) ->
                        new TimeAccelerationWorkQueue.ExecutionResult(requestedTicks, true, false),
                (target, result, displayMultiplier) ->
                        executed.merge(target, result.executed(), Integer::sum));

        assertEquals(128, usedBudget);
        assertEquals(64, executed.get("wand-target"));
        assertEquals(64, executed.get("machine-target"));
        assertEquals(960, queue.pendingTicks("wand-target"));
        assertEquals(0, queue.pendingTicks("machine-target"));
    }
}
