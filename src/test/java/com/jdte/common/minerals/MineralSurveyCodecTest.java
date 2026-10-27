package com.jdte.common.minerals;

import com.google.gson.JsonParser;
import com.jdte.common.utils.ContainerDataEncoding;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MineralSurveyCodecTest {
    private static MineralSurveyData survey() {
        return MineralSurveyData.create(Long.MAX_VALUE, ResourceLocation.withDefaultNamespace("plains"),
                ResourceLocation.withDefaultNamespace("overworld"), List.of(new MineralEntry(
                        ResourceLocation.withDefaultNamespace("iron_ore"), Long.MAX_VALUE, -64, 319, 9, MineralEntry.Confidence.EXACT)));
    }
    @Test void nbtRoundTripPreservesLongValues() {
        var tag = MineralSurveyData.CODEC.encodeStart(NbtOps.INSTANCE, survey()).result().orElseThrow();
        assertEquals(survey(), MineralSurveyData.CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow());
    }
    @Test void networkRoundTripPreservesSurvey() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            MineralSurveyData.write(buffer, survey());
            assertEquals(survey(), MineralSurveyData.read(buffer));
        } finally { buffer.release(); }
    }
    @Test void invalidConfidenceIsAnErrorAndOptionalFieldUsesSchemaDefault() {
        var json = JsonParser.parseString("{\"ore\":\"minecraft:iron_ore\",\"weight\":1,\"confidence\":\"unknown\"}");
        assertTrue(MineralEntry.Confidence.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"unknown\"")).error().isPresent());
        // DFU optionalFieldOf deliberately uses the declared default for invalid optional metadata.
        assertEquals(MineralEntry.Confidence.ESTIMATED,
                MineralEntry.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow().confidence());
    }
    @Test void excessiveEntryCountIsRejectedBeforeReadingEntries() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeVarInt(1); buffer.writeVarLong(1);
            buffer.writeResourceLocation(ResourceLocation.withDefaultNamespace("plains"));
            buffer.writeResourceLocation(ResourceLocation.withDefaultNamespace("overworld"));
            buffer.writeVarInt(4097);
            assertThrows(DecoderException.class, () -> MineralSurveyData.read(buffer));
        } finally { buffer.release(); }
    }
    @Test void containerDataPreservesValuesAcrossSignedShortPackets() {
        for (int value : new int[]{0, 32768, 65535, 65536, 72000, 6_400_000, Integer.MAX_VALUE}) {
            int low = (short) ContainerDataEncoding.low16(value), high = (short) ContainerDataEncoding.high16(value);
            assertEquals(value, ContainerDataEncoding.combine16(low, high));
            assertEquals(value, ContainerDataEncoding.withHigh16(ContainerDataEncoding.withLow16(0, low), high));
        }
    }
    @Test void networkDecoderLeavesFollowingFieldsIntact() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            MineralSurveyData.write(buffer, survey()); buffer.writeInt(0x12345678);
            assertEquals(survey(), MineralSurveyData.read(buffer));
            assertEquals(0x12345678, buffer.readInt());
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }
}
