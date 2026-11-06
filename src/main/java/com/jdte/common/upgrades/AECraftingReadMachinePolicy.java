package com.jdte.common.upgrades;

import java.util.function.BooleanSupplier;

/** Small side-effect decisions shared by JDTE machine work boundaries. */
public final class AECraftingReadMachinePolicy {
    private AECraftingReadMachinePolicy() {}

    public record MachineWorkDecision(boolean runWork, boolean deactivate, boolean consumeResources) {}

    public record ProductionDecision(boolean runWork, boolean resetInactiveState) {}

    public static MachineWorkDecision production(boolean allowed, boolean redstoneActive) {
        return production(allowed, redstoneActive, true);
    }

    public static MachineWorkDecision production(boolean allowed, boolean redstoneActive, boolean canRun) {
        ProductionDecision decision = productionState(allowed, redstoneActive, canRun);
        return new MachineWorkDecision(decision.runWork(), !decision.runWork(), decision.runWork());
    }

    public static ProductionDecision productionState(boolean allowed, boolean redstoneActive, boolean canRun) {
        boolean operational = redstoneActive && canRun;
        return new ProductionDecision(allowed && operational, !operational);
    }

    public static MachineWorkDecision freezer(boolean allowed, boolean wantsFreeze,
                                               boolean redstoneActive, BooleanSupplier hasResources) {
        boolean eligible = allowed && wantsFreeze && redstoneActive;
        boolean run = eligible && hasResources.getAsBoolean();
        return new MachineWorkDecision(run, !run, run);
    }

    public static boolean mayAdvanceFactoryPhase(boolean allowed, boolean safetyPhase) {
        return allowed || safetyPhase;
    }
}
