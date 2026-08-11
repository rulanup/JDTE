package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixMemberStateTest {
    @Test
    void onlyOwningControllerCanReleaseMember() {
        GreenhouseMatrixMemberState state = new GreenhouseMatrixMemberState();
        BlockPos controllerA = new BlockPos(1, 2, 3);
        BlockPos controllerB = new BlockPos(4, 5, 6);

        assertTrue(state.claim(controllerA));
        assertFalse(state.claim(controllerA));
        assertFalse(state.claim(controllerB));
        assertFalse(state.release(controllerB));
        assertTrue(state.managed());
        assertTrue(state.release(controllerA));
        assertFalse(state.managed());
    }
}
