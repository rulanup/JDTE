package com.jdte.common.network.data;

import com.jdte.JDTE;
import com.jdte.common.minerals.MineralSurveyData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MineralSurveyOpenPayload(MineralSurveyData survey) implements CustomPacketPayload {
    public static final Type<MineralSurveyOpenPayload> TYPE = new Type<>(JDTE.id("mineral_survey_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MineralSurveyOpenPayload> STREAM_CODEC =
            MineralSurveyData.STREAM_CODEC.map(MineralSurveyOpenPayload::new, MineralSurveyOpenPayload::survey);

    @Override
    public Type<MineralSurveyOpenPayload> type() {
        return TYPE;
    }
}