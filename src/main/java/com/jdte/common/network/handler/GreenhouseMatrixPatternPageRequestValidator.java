package com.jdte.common.network.handler;

import com.jdte.common.network.data.GreenhouseMatrixPatternPagePayload;
import net.minecraft.core.BlockPos;

public final class GreenhouseMatrixPatternPageRequestValidator {
    private GreenhouseMatrixPatternPageRequestValidator() {
    }

    public static int clampPage(int requestedPage, int pageCount) {
        return Math.clamp(requestedPage, 0, Math.max(0, pageCount - 1));
    }

    public static boolean matches(int openContainerId, BlockPos openController,
                                  GreenhouseMatrixPatternPagePayload payload) {
        return payload != null && openController != null
                && openContainerId == payload.containerId()
                && openController.equals(payload.blockPos());
    }
}
