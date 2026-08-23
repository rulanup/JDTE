package com.jdte.common.containers;

import net.minecraft.network.RegistryFriendlyByteBuf;

public record LargePortableContainerSource(SourceType sourceType, String curiosSlotId) {
    public static LargePortableContainerSource mainHand() {
        return new LargePortableContainerSource(SourceType.MAIN_HAND, "");
    }

    public static LargePortableContainerSource curios(String slotId) {
        return new LargePortableContainerSource(SourceType.CURIOS_SLOT, slotId);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(sourceType.ordinal());
        buffer.writeUtf(curiosSlotId);
    }

    public static LargePortableContainerSource decode(RegistryFriendlyByteBuf buffer) {
        SourceType sourceType = SourceType.values()[buffer.readVarInt()];
        String curiosSlotId = buffer.readUtf();
        return new LargePortableContainerSource(sourceType, curiosSlotId);
    }

    public enum SourceType {
        MAIN_HAND,
        CURIOS_SLOT
    }
}
