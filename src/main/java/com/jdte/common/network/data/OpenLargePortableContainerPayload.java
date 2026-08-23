package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenLargePortableContainerPayload(ContainerKind containerKind) implements CustomPacketPayload {
    public static final Type<OpenLargePortableContainerPayload> TYPE =
            new Type<>(JDTE.id("open_large_portable_container"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLargePortableContainerPayload> STREAM_CODEC =
            StreamCodec.of(OpenLargePortableContainerPayload::encode, OpenLargePortableContainerPayload::decode);

    public OpenLargePortableContainerPayload {
        if (containerKind == null) {
            throw new IllegalArgumentException("containerKind cannot be null");
        }
    }

    @Override
    public Type<OpenLargePortableContainerPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, OpenLargePortableContainerPayload payload) {
        ByteBufCodecs.INT.encode(buffer, payload.containerKind.networkId);
    }

    private static OpenLargePortableContainerPayload decode(RegistryFriendlyByteBuf buffer) {
        return new OpenLargePortableContainerPayload(ContainerKind.fromNetworkId(ByteBufCodecs.INT.decode(buffer)));
    }

    public enum ContainerKind {
        LARGE_POCKET_GENERATOR(0),
        LARGE_POTION_CANISTER(1),
        LARGE_FUEL_CANISTER(2);

        private final int networkId;

        ContainerKind(int networkId) {
            this.networkId = networkId;
        }

        public int networkId() {
            return networkId;
        }

        public static ContainerKind fromNetworkId(int networkId) {
            for (ContainerKind value : values()) {
                if (value.networkId == networkId) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown large portable container kind: " + networkId);
        }
    }
}
