package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record AdvancedUpgradeStorageActionPayload(int action, int slotIndex, int storageIndex, int containerId) {

    public static final int ACTION_INSERT_ONE = 0;
    public static final int ACTION_INSERT_MAX = 1;
    public static final int ACTION_PICKUP_CURSOR = 2;
    public static final int ACTION_WITHDRAW_INVENTORY = 3;
    public static final int ACTION_DEPOSIT_CURSOR = 4;
    public static final int ACTION_DEPOSIT_CURSOR_ONE = 5;

    public static void encode(AdvancedUpgradeStorageActionPayload p, FriendlyByteBuf b) { b.writeInt(p.action()); b.writeInt(p.slotIndex()); b.writeInt(p.storageIndex()); b.writeVarInt(p.containerId()); }
    public static AdvancedUpgradeStorageActionPayload decode(FriendlyByteBuf b) { return new AdvancedUpgradeStorageActionPayload(b.readInt(), b.readInt(), b.readInt(), b.readVarInt()); }
}
