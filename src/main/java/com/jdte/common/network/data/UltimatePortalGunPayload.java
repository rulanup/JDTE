package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * 顶级传送枪操作。
 * action: 0=选择槽位，1=追加当前位置，2=删除槽位，3=编辑槽位（名称/维度/坐标）。
 */
public record UltimatePortalGunPayload(int action, int position, String name,
                                       ResourceKey<Level> dimension, double x, double y, double z, boolean staysOpen)
        implements CustomPacketPayload {
    public static final int ACTION_SELECT = 0;
    public static final int ACTION_ADD_POSITION = 1;
    public static final int ACTION_REMOVE = 2;
    public static final int ACTION_EDIT = 3;

    public static final Type<UltimatePortalGunPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "ultimate_portal_gun"));

    @Override
    public Type<UltimatePortalGunPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, UltimatePortalGunPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.action());
                buf.writeInt(payload.position());
                buf.writeUtf(payload.name() != null ? payload.name() : "");
                buf.writeResourceKey(payload.dimension() != null ? payload.dimension() : Level.OVERWORLD);
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
                buf.writeBoolean(payload.staysOpen());
            },
            buf -> new UltimatePortalGunPayload(
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf(),
                    buf.readResourceKey(Registries.DIMENSION),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readBoolean()
            )
    );
}
