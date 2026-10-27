package com.jdte.common.integrations.ae2;

import com.jdte.common.upgrades.AECraftingReadMachinePolicy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AECraftingReadPolicyTest {
    @Test void onlyBusyCpuWithAJobAllowsProduction() {
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(true,false))));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(false,true))));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(true,true))));
    }
    @Test void aePauseKeepsPendingWorkWhileRedstoneOffResetsIt() {
        var pause = AECraftingReadMachinePolicy.productionState(false,true,true);
        assertFalse(pause.runWork());assertFalse(pause.resetInactiveState());
        var off = AECraftingReadMachinePolicy.productionState(false,false,true);
        assertFalse(off.runWork());assertTrue(off.resetInactiveState());
    }
    @Test void rollbackCannotBePausedByAnIdleCraftingNetwork() {
        assertTrue(AECraftingReadMachinePolicy.mayAdvanceFactoryPhase(false,true));
        assertFalse(AECraftingReadMachinePolicy.mayAdvanceFactoryPhase(false,false));
    }
    @Test void pausedFreezerDoesNotEvenQueryOrConsumeResources() {
        var decision = AECraftingReadMachinePolicy.freezer(false,true,true,()->{fail("resource check while paused");return true;});
        assertFalse(decision.runWork());assertTrue(decision.deactivate());assertFalse(decision.consumeResources());
    }
}
