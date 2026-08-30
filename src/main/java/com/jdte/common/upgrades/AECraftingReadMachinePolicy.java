package com.jdte.common.upgrades;

/** Pure decisions shared by state-machine work boundaries and their tests. */
public final class AECraftingReadMachinePolicy {
    private AECraftingReadMachinePolicy() {}

    public record WorkDecision(boolean allowed, boolean deactivate, boolean consumeResources) {}

    public static WorkDecision decideWork(boolean allowed, boolean wantsFreeze, boolean redstoneActive,
                                          boolean hasResources) {
        boolean active = allowed && wantsFreeze && redstoneActive && hasResources;
        return new WorkDecision(active, !active, active);
    }

    public static boolean mayAdvance(boolean allowed, boolean redstoneActive) {
        return allowed && redstoneActive;
    }
}
