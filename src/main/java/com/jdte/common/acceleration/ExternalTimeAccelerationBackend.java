package com.jdte.common.acceleration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public interface ExternalTimeAccelerationBackend {
    void beginFrame(MinecraftServer server);

    TargetResolution resolve(ServerLevel level, BlockPos pos);

    void submit(TargetHandle target, Object contributor, int additionalCycles);

    void executeFrame(MinecraftServer server);

    void clearFrame(MinecraftServer server);

    default void onLevelUnload(ServerLevel level) {
    }

    default void onServerStopped(MinecraftServer server) {
    }

    interface TargetHandle {
    }

    enum TargetState {
        NOT_EXTERNAL,
        INACTIVE_EXTERNAL,
        ACTIVE_EXTERNAL
    }

    record TargetResolution(TargetState state, TargetHandle handle) {
        public TargetResolution {
            Objects.requireNonNull(state, "state");
            if ((state == TargetState.ACTIVE_EXTERNAL) != (handle != null)) {
                throw new IllegalArgumentException("Only active external targets have handles");
            }
        }

        public static TargetResolution notExternal() {
            return new TargetResolution(TargetState.NOT_EXTERNAL, null);
        }

        public static TargetResolution inactiveExternal() {
            return new TargetResolution(TargetState.INACTIVE_EXTERNAL, null);
        }

        public static TargetResolution active(TargetHandle handle) {
            return new TargetResolution(TargetState.ACTIVE_EXTERNAL,
                    Objects.requireNonNull(handle, "handle"));
        }
    }
}
