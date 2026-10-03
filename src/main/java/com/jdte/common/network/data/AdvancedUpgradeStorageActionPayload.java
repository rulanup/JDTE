package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdvancedUpgradeStorageActionPayload(int action, int slotIndex, int storageIndex) implements CustomPacketPayload {
    public static final Type<AdvancedUpgradeStorageActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "advanced_upgrade_storage_action"));

    public static final int ACTION_INSERT_ONE = 0;
    public static final int ACTION_INSERT_MAX = 1;
    public static final int ACTION_PICKUP_CURSOR = 2;
    public static final int ACTION_WITHDRAW_INVENTORY = 3;
    public static final int ACTION_DEPOSIT_CURSOR = 4;
    public static final int ACTION_DEPOSIT_CURSOR_ONE = 5;

    public AdvancedUpgradeStorageActionPayload(int action, int slotIndex) {
        this(action, slotIndex, 0);
    }

    @Override
    public Type<AdvancedUpgradeStorageActionPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, AdvancedUpgradeStorageActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AdvancedUpgradeStorageActionPayload::action,
            ByteBufCodecs.INT, AdvancedUpgradeStorageActionPayload::slotIndex,
            ByteBufCodecs.INT, AdvancedUpgradeStorageActionPayload::storageIndex,
            AdvancedUpgradeStorageActionPayload::new
    );
}
