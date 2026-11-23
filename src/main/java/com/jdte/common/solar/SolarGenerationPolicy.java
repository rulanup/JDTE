package com.jdte.common.solar;

public final class SolarGenerationPolicy {
    private static final int ADJACENT_POSITIONS = 8;

    private SolarGenerationPolicy() {
    }

    public static int adjacencyMultiplier(int sameTierNeighbors) {
        int boundedNeighbors = Math.clamp(sameTierNeighbors, 0, ADJACENT_POSITIONS);
        return ((boundedNeighbors + 1) / ADJACENT_POSITIONS) + 1;
    }

    public static float heightMultiplier(int minBuildHeight, int maxBuildHeight, int y) {
        int heightRange = maxBuildHeight - minBuildHeight;
        if (heightRange <= 0) {
            return 1.0F;
        }

        float midpoint = (minBuildHeight + maxBuildHeight) / 2.0F;
        float normalizedY = (y - midpoint) / heightRange;
        float middleCurve = Math.max(1.0F - normalizedY * normalizedY * 4.0F, 0.0F);
        return 0.05F + 0.95F * (1.0F - middleCurve);
    }

    public static int generatedPerTick(int baseGeneration, int sameTierNeighbors,
                                       int minBuildHeight, int maxBuildHeight, int y) {
        if (baseGeneration <= 0) {
            return 0;
        }
        float generated = baseGeneration
                * (float) adjacencyMultiplier(sameTierNeighbors)
                * heightMultiplier(minBuildHeight, maxBuildHeight, y);
        return generated >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) generated;
    }
}
