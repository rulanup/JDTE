package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

/** Forge 1.20.1 packet for the Ultimate Portal Gun slot editor. */
public record UltimatePortalGunPayload(int action, int position, String name, @Nullable String dimension,
                                       double x, double y, double z, boolean staysOpen) {
    public static final int ACTION_SELECT = 0;
    public static final int ACTION_ADD_POSITION = 1;
    public static final int ACTION_REMOVE = 2;
    public static final int ACTION_EDIT = 3;

    public static void encode(UltimatePortalGunPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.action);
        buf.writeVarInt(payload.position);
        buf.writeUtf(payload.name == null ? "" : payload.name, 64);
        buf.writeUtf(payload.dimension == null ? "" : payload.dimension, 128);
        buf.writeDouble(payload.x);
        buf.writeDouble(payload.y);
        buf.writeDouble(payload.z);
        buf.writeBoolean(payload.staysOpen);
    }

    public static UltimatePortalGunPayload decode(FriendlyByteBuf buf) {
        int action = buf.readVarInt();
        int position = buf.readVarInt();
        String name = buf.readUtf(64);
        String dimension = buf.readUtf(128);
        return new UltimatePortalGunPayload(action, position, name, dimension.isEmpty() ? null : dimension,
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }
}
