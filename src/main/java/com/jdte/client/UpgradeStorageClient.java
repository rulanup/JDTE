package com.jdte.client;

/** Keeps the key-state query out of common item initialization on dedicated servers. */
public final class UpgradeStorageClient {
    private UpgradeStorageClient() { }
    public static boolean shiftDown() { return net.minecraft.client.gui.screens.Screen.hasShiftDown(); }
}
