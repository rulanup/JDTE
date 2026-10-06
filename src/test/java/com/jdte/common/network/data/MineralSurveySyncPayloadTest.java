package com.jdte.common.network.data;

import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MineralSurveySyncPayloadTest {
    @Test
    void syncPayloadUsesJdteIdentityAndRoundTrips() {
        assertEquals("jdte", MineralSurveySyncPayload.TYPE.id().getNamespace());
        assertEquals("mineral_survey_sync", MineralSurveySyncPayload.TYPE.id().getPath());

        RegistryFriendlyByteBuf buffer = buffer();
        try {
            MineralEntry copper = new MineralEntry(
                    ResourceLocation.parse("minecraft:copper_ore"), 100L, -16, 112, 10,
                    MineralEntry.Confidence.EXACT);
            MineralEntry iron = new MineralEntry(
                    ResourceLocation.parse("minecraft:iron_ore"), 50L, -64, 72, 8,
                    MineralEntry.Confidence.ESTIMATED);
            MineralSurveyData plainsSurvey = MineralSurveyData.create(
                    42L,
                    ResourceLocation.parse("minecraft:plains"),
                    ResourceLocation.parse("minecraft:overworld"),
                    List.of(copper, iron));
            MineralSurveySyncPayload expected = new MineralSurveySyncPayload(List.of(plainsSurvey));

            MineralSurveySyncPayload.STREAM_CODEC.encode(buffer, expected);
            MineralSurveySyncPayload decoded = MineralSurveySyncPayload.STREAM_CODEC.decode(buffer);

            assertEquals(expected.surveys().size(), decoded.surveys().size());
            MineralSurveyData decodedSurvey = decoded.surveys().getFirst();
            assertEquals(plainsSurvey.biomeId(), decodedSurvey.biomeId());
            assertEquals(plainsSurvey.dimensionId(), decodedSurvey.dimensionId());
            assertEquals(plainsSurvey.indexVersion(), decodedSurvey.indexVersion());
            assertEquals(plainsSurvey.entries().size(), decodedSurvey.entries().size());
            assertEquals(plainsSurvey.entries().get(0), decodedSurvey.entries().get(0));
            assertEquals(plainsSurvey.entries().get(1), decodedSurvey.entries().get(1));
        } finally {
            buffer.release();
        }
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }
}
