package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.entities.TimeAcceleratorEffectEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTEEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, JDTE.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TimeAcceleratorEffectEntity>> TIME_ACCELERATOR_EFFECT = ENTITIES.register(
            "time_accelerator_effect", () -> EntityType.Builder.<TimeAcceleratorEffectEntity>of(TimeAcceleratorEffectEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .noSave()
                    .build("time_accelerator_effect"));
}
