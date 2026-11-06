package com.jdte.common.acceleration;

import java.util.Objects;
import java.util.Optional;

public final class ExternalTimeAccelerationBackends {
    private static volatile ExternalTimeAccelerationBackend backend;
    private static Object registrationToken;

    private ExternalTimeAccelerationBackends() {
    }

    public static Optional<ExternalTimeAccelerationBackend> current() {
        return Optional.ofNullable(backend);
    }

    public static synchronized Registration register(ExternalTimeAccelerationBackend candidate) {
        Objects.requireNonNull(candidate, "candidate");
        if (backend != null) {
            throw new IllegalStateException("A time acceleration backend is already registered");
        }
        Object token = new Object();
        registrationToken = token;
        backend = candidate;
        return () -> unregister(token);
    }

    private static synchronized void unregister(Object token) {
        if (registrationToken == token) {
            registrationToken = null;
            backend = null;
        }
    }

    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        @Override
        void close();
    }
}
