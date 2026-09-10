package com.jdte.common.acceleration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalTimeAccelerationBackendsTest {
    @Test
    void currentIsEmptyWithoutABackend() {
        assertTrue(ExternalTimeAccelerationBackends.current().isEmpty());
    }

    @Test
    void onlyOneBackendCanBeRegistered() {
        StubBackend first = new StubBackend();
        StubBackend second = new StubBackend();

        try (ExternalTimeAccelerationBackends.Registration ignored =
                     ExternalTimeAccelerationBackends.register(first)) {
            assertSame(first, ExternalTimeAccelerationBackends.current().orElseThrow());
            assertThrows(IllegalStateException.class,
                    () -> ExternalTimeAccelerationBackends.register(second));
        }

        assertTrue(ExternalTimeAccelerationBackends.current().isEmpty());
    }

    @Test
    void nullBackendCannotBeRegistered() {
        assertThrows(NullPointerException.class,
                () -> ExternalTimeAccelerationBackends.register(null));
        assertTrue(ExternalTimeAccelerationBackends.current().isEmpty());
    }

    @Test
    void repeatedCloseDoesNotRemoveALaterBackend() {
        StubBackend first = new StubBackend();
        StubBackend second = new StubBackend();
        ExternalTimeAccelerationBackends.Registration firstRegistration =
                ExternalTimeAccelerationBackends.register(first);

        firstRegistration.close();
        try (ExternalTimeAccelerationBackends.Registration ignored =
                     ExternalTimeAccelerationBackends.register(second)) {
            firstRegistration.close();
            assertSame(second, ExternalTimeAccelerationBackends.current().orElseThrow());
        }

        assertTrue(ExternalTimeAccelerationBackends.current().isEmpty());
    }

    @Test
    void staleRegistrationDoesNotRemoveTheSameBackendWhenReregistered() {
        StubBackend backend = new StubBackend();
        ExternalTimeAccelerationBackends.Registration firstRegistration =
                ExternalTimeAccelerationBackends.register(backend);

        firstRegistration.close();
        try (ExternalTimeAccelerationBackends.Registration ignored =
                     ExternalTimeAccelerationBackends.register(backend)) {
            firstRegistration.close();
            assertSame(backend, ExternalTimeAccelerationBackends.current().orElseThrow());
        }

        assertTrue(ExternalTimeAccelerationBackends.current().isEmpty());
    }

    @Test
    void targetResolutionFactoriesCreateTheThreeValidStates() {
        ExternalTimeAccelerationBackend.TargetHandle handle = new StubHandle();
        ExternalTimeAccelerationBackend.TargetResolution notExternal =
                ExternalTimeAccelerationBackend.TargetResolution.notExternal();
        ExternalTimeAccelerationBackend.TargetResolution inactiveExternal =
                ExternalTimeAccelerationBackend.TargetResolution.inactiveExternal();
        ExternalTimeAccelerationBackend.TargetResolution active =
                ExternalTimeAccelerationBackend.TargetResolution.active(handle);

        assertEquals(ExternalTimeAccelerationBackend.TargetState.NOT_EXTERNAL, notExternal.state());
        assertNull(notExternal.handle());
        assertEquals(ExternalTimeAccelerationBackend.TargetState.INACTIVE_EXTERNAL, inactiveExternal.state());
        assertNull(inactiveExternal.handle());
        assertEquals(ExternalTimeAccelerationBackend.TargetState.ACTIVE_EXTERNAL, active.state());
        assertSame(handle, active.handle());
    }

    @Test
    void activeFactoryRejectsNullHandle() {
        assertThrows(NullPointerException.class,
                () -> ExternalTimeAccelerationBackend.TargetResolution.active(null));
    }

    @Test
    void activeResolutionRequiresAHandle() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExternalTimeAccelerationBackend.TargetResolution(
                        ExternalTimeAccelerationBackend.TargetState.ACTIVE_EXTERNAL, null));
    }

    @Test
    void nonActiveResolutionsRejectHandles() {
        ExternalTimeAccelerationBackend.TargetHandle handle = new StubHandle();

        assertThrows(IllegalArgumentException.class,
                () -> new ExternalTimeAccelerationBackend.TargetResolution(
                        ExternalTimeAccelerationBackend.TargetState.NOT_EXTERNAL, handle));
        assertThrows(IllegalArgumentException.class,
                () -> new ExternalTimeAccelerationBackend.TargetResolution(
                        ExternalTimeAccelerationBackend.TargetState.INACTIVE_EXTERNAL, handle));
    }

    @Test
    void targetResolutionRequiresAState() {
        assertThrows(NullPointerException.class,
                () -> new ExternalTimeAccelerationBackend.TargetResolution(null, null));
    }

    private static final class StubBackend implements ExternalTimeAccelerationBackend {
        @Override
        public void beginFrame(MinecraftServer server) {
        }

        @Override
        public TargetResolution resolve(ServerLevel level, BlockPos pos) {
            return TargetResolution.notExternal();
        }

        @Override
        public void submit(TargetHandle target, Object contributor, int additionalCycles) {
        }

        @Override
        public void executeFrame(MinecraftServer server) {
        }

        @Override
        public void clearFrame(MinecraftServer server) {
        }
    }

    private static final class StubHandle implements ExternalTimeAccelerationBackend.TargetHandle {
    }
}
