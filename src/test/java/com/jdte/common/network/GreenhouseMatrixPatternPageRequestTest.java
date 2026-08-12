package com.jdte.common.network;

import com.jdte.common.network.data.GreenhouseMatrixPatternPagePayload;
import com.jdte.common.network.handler.GreenhouseMatrixPatternPageRequestValidator;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixPatternPageRequestTest {
    @Test
    void clampsRequestedPageAgainstLivePageCount() {
        assertEquals(0, GreenhouseMatrixPatternPageRequestValidator.clampPage(-5, 4));
        assertEquals(3, GreenhouseMatrixPatternPageRequestValidator.clampPage(99, 4));
        assertEquals(0, GreenhouseMatrixPatternPageRequestValidator.clampPage(2, 0));
    }

    @Test
    void requiresBothContainerIdAndControllerPositionToMatch() {
        BlockPos position = new BlockPos(1, 2, 3);
        GreenhouseMatrixPatternPagePayload valid = new GreenhouseMatrixPatternPagePayload(position, 17, 2);

        assertTrue(GreenhouseMatrixPatternPageRequestValidator.matches(17, position, valid));
        assertFalse(GreenhouseMatrixPatternPageRequestValidator.matches(18, position, valid));
        assertFalse(GreenhouseMatrixPatternPageRequestValidator.matches(17, position.above(), valid));
    }
}
