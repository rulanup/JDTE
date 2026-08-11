package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.minerals.MineralSurveyData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class JDTEDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, JDTE.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MineralSurveyData>> MINERAL_SURVEY =
            DATA_COMPONENTS.register("mineral_survey", () -> DataComponentType.<MineralSurveyData>builder()
                    .persistent(MineralSurveyData.CODEC)
                    .networkSynchronized(MineralSurveyData.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TIME_MULTITOOL_SPEED_MODE =
            DATA_COMPONENTS.register("time_multitool_speed_mode", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .cacheEncoding()
                    .build());

    /** 顶级传送枪手动坐标槽位标记（与收藏列表索引对齐；true = 手动坐标，传送固定消耗 10 B）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Boolean>>> ULTIMATE_PORTAL_GUN_MANUAL_SLOTS =
            DATA_COMPONENTS.register("ultimate_portal_gun_manual_slots",
                    () -> DataComponentType.<List<Boolean>>builder()
                            .persistent(Codec.list(Codec.BOOL))
                            .networkSynchronized(ByteBufCodecs.BOOL.apply(ByteBufCodecs.collection(ArrayList::new)))
                            .cacheEncoding()
                            .build());

    private JDTEDataComponents() {
    }
}
