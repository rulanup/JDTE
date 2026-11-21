package com.jdte.client.entityrenders;

import com.direwolf20.justdirethings.client.entityrenders.TimeWandEntityRender;
import com.direwolf20.justdirethings.client.entityrenders.TimeWandEntityRender.TimeWandRenderState;
import com.jdte.common.entities.TimeAcceleratorEffectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public final class TimeAcceleratorEffectRenderer extends EntityRenderer<TimeAcceleratorEffectEntity, TimeWandRenderState> {
    private final TimeWandEntityRender display;

    public TimeAcceleratorEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        display = new TimeWandEntityRender(context);
    }

    @Override
    public TimeWandRenderState createRenderState() {
        return display.createRenderState();
    }

    @Override
    public void extractRenderState(TimeAcceleratorEffectEntity entity, TimeWandRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        int total = entity.getTotalTime();
        state.blockIsAir = entity.level().getBlockState(entity.blockPosition()).isAir();
        state.remainingTimeTicks = Math.clamp(entity.getRemainingTime(), 0, Math.max(0, total));
        state.timeProgress = total > 0 ? (float) state.remainingTimeTicks / total : 0F;
        state.accelerationRate = entity.getAccelerationRate();
        state.tickRateProgress = Math.clamp(entity.getTickSpeed() / 4F, 0F, 1F);
    }

    @Override
    public void submit(TimeWandRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        display.submit(state, pose, collector, camera);
    }
}
