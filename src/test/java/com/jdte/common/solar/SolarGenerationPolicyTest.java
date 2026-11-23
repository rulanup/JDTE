package com.jdte.common.solar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolarGenerationPolicyTest {
    @Test
    void adjacencyUsesTheOriginalEightPositionThreshold() {
        assertEquals(1, SolarGenerationPolicy.adjacencyMultiplier(0));
        assertEquals(1, SolarGenerationPolicy.adjacencyMultiplier(6));
        assertEquals(2, SolarGenerationPolicy.adjacencyMultiplier(7));
        assertEquals(2, SolarGenerationPolicy.adjacencyMultiplier(8));
    }

    @Test
    void adjacencyRejectsCountsOutsideTheEightPositionRing() {
        assertEquals(1, SolarGenerationPolicy.adjacencyMultiplier(-1));
        assertEquals(2, SolarGenerationPolicy.adjacencyMultiplier(9));
    }

    @Test
    void heightCurveMatchesTheOriginalMiddleAndEdgeOutput() {
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(-64, 319, -64), 0.0001F);
        assertEquals(0.05F, SolarGenerationPolicy.heightMultiplier(-64, 319, 127), 0.0001F);
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(-64, 319, 319), 0.0001F);
    }

    @Test
    void invalidBuildHeightFallsBackToFullOutput() {
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(64, 64, 64));
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(65, 64, 64));
    }

    @Test
    void generatedOutputFloorsAndSaturates() {
        assertEquals(2_304, SolarGenerationPolicy.generatedPerTick(46_080, 0, -64, 319, 127));
        assertEquals(92_160, SolarGenerationPolicy.generatedPerTick(46_080, 8, -64, 319, -64));
        assertEquals(Integer.MAX_VALUE,
                SolarGenerationPolicy.generatedPerTick(Integer.MAX_VALUE, 8, -64, 319, -64));
    }
}
