package com.jdte.common.utils;

public final class ContainerDataEncoding {
    private ContainerDataEncoding() {
    }

    public static int low16(int value) {
        return value & 0xFFFF;
    }

    public static int high16(int value) {
        return value >>> 16;
    }

    public static int withLow16(int current, int low) {
        return (current & 0xFFFF0000) | (low & 0xFFFF);
    }

    public static int withHigh16(int current, int high) {
        return (current & 0xFFFF) | ((high & 0xFFFF) << 16);
    }

    public static int combine16(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}