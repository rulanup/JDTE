package com.jdte.common.network.data;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendedExperienceHolderPayloadTest {
    @Test
    void transferPayloadUsesJdteIdentityAndRoundTrips() {
        assertEquals("jdte", ExtendedExperienceHolderPayload.TYPE.id().getNamespace());
        assertEquals("extended_experience_holder", ExtendedExperienceHolderPayload.TYPE.id().getPath());

        RegistryFriendlyByteBuf buffer = buffer();
        try {
            BlockPos blockPos = new BlockPos(12, 64, -9);
            ExtendedExperienceHolderPayload.STREAM_CODEC.encode(
                    buffer, new ExtendedExperienceHolderPayload(blockPos, true, -1));

            assertEquals(new ExtendedExperienceHolderPayload(blockPos, true, -1),
                    ExtendedExperienceHolderPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void settingsPayloadUsesJdteIdentityAndRoundTripsEverySetting() {
        assertEquals("jdte", ExtendedExperienceHolderSettingsPayload.TYPE.id().getNamespace());
        assertEquals("extended_experience_holder_settings",
                ExtendedExperienceHolderSettingsPayload.TYPE.id().getPath());

        RegistryFriendlyByteBuf buffer = buffer();
        try {
            ExtendedExperienceHolderSettingsPayload expected =
                    new ExtendedExperienceHolderSettingsPayload(
                            new BlockPos(-4, 80, 21), 30, true, true, false);
            ExtendedExperienceHolderSettingsPayload.STREAM_CODEC.encode(buffer, expected);

            assertEquals(expected, ExtendedExperienceHolderSettingsPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }
}
