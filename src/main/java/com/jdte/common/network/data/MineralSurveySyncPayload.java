package com.jdte.common.network.data;

import com.jdte.JDTE;
import com.jdte.common.minerals.MineralSurveyData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record MineralSurveySyncPayload(List<MineralSurveyData> surveys) implements CustomPacketPayload {
    public static final Type<MineralSurveySyncPayload> TYPE = new Type<>(JDTE.id("mineral_survey_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MineralSurveySyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> MineralSurveyData.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, payload.surveys()),
            buf -> new MineralSurveySyncPayload(MineralSurveyData.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf))
    );

    public MineralSurveySyncPayload {
        surveys = List.copyOf(surveys);
    }

    @Override
    public Type<MineralSurveySyncPayload> type() {
        return TYPE;
    }
}
