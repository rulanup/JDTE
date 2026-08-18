package com.jdte.common.integrations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Reads optional Just Dyna Things configuration without linking to a version-specific config class. */
final class JustDynaThingsConfig {
    private static final String COMMON_CONFIG = "com.devdyna.justdynathings.config.CommonConfig";

    private JustDynaThingsConfig() {
    }

    static int getInt(String fieldName, int fallback) {
        Object value = getValue(fieldName);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    static boolean getBoolean(String fieldName, boolean fallback) {
        Object value = getValue(fieldName);
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static Object getValue(String fieldName) {
        try {
            Class<?> configClass = Class.forName(COMMON_CONFIG);
            Field field = configClass.getField(fieldName);
            Object configValue = field.get(null);
            Method get = configValue.getClass().getMethod("get");
            return get.invoke(configValue);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }
}
