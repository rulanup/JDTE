package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.minerals.MineralSurveyData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class JDTEDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, JDTE.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MineralSurveyData>> MINERAL_SURVEY =
            DATA_COMPONENTS.register("mineral_survey", () -> DataComponentType.<MineralSurveyData>builder()
                    .persistent(MineralSurveyData.CODEC)
                    .networkSynchronized(MineralSurveyData.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    private JDTEDataComponents() {
    }
}