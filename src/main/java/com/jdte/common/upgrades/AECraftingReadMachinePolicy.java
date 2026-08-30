package com.jdte.common.upgrades;

/** Small side-effect decision shared by JDTE machine work boundaries. */
public final class AECraftingReadMachinePolicy {
    private AECraftingReadMachinePolicy() {}

    public record MachineWorkDecision(boolean runWork, boolean deactivate, boolean consumeResources) {}

    public static MachineWorkDecision production(boolean allowed, boolean redstoneActive) {
        boolean run = allowed && redstoneActive;
        return new MachineWorkDecision(run, !run, run);
    }

    public static MachineWorkDecision freezer(boolean allowed, boolean wantsFreeze,
                                               boolean redstoneActive, boolean hasResources) {
        boolean run = allowed && wantsFreeze && redstoneActive && hasResources;
        return new MachineWorkDecision(run, !run, run);
    }
}
