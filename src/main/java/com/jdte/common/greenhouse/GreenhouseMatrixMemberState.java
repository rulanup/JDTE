package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;

/** Transient state: deliberately not serialized so an absent controller can never leave a saved greenhouse disabled. */
public final class GreenhouseMatrixMemberState {
    private BlockPos controller;

    public boolean claim(BlockPos controller) {
        if (controller == null || this.controller != null) return false;
        this.controller = controller.immutable();
        return true;
    }

    public boolean release(BlockPos controller) {
        if (this.controller == null || !this.controller.equals(controller)) return false;
        this.controller = null;
        return true;
    }

    public boolean managed() {
        return controller != null;
    }
}
