package com.jdte.client.entityrenders;

import com.direwolf20.justdirethings.client.entityrenders.TimeWandEntityRender;
import com.direwolf20.justdirethings.client.entityrenders.TimeWandEntityRender.TimeWandRenderState;
import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.common.items.UltimateTimeWandData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public final class UltimateTimeWandRenderer extends EntityRenderer<UltimateTimeWandEntity, TimeWandRenderState> {
    private final TimeWandEntityRender display;

    public UltimateTimeWandRenderer(EntityRendererProvider.Context context) {
        super(context);
        display = new TimeWandEntityRender(context);
    }

    @Override
    public TimeWandRenderState createRenderState() {
        return display.createRenderState();
    }

    @Override
    public void extractRenderState(UltimateTimeWandEntity entity, TimeWandRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        int total = entity.getTotalTime();
        state.blockIsAir = entity.level().getBlockState(entity.blockPosition()).isAir() || total <= 0;
        state.remainingTimeTicks = Math.clamp(entity.getRemainingTime(), 0, Math.max(0, total));
        state.timeProgress = total > 0 ? (float) state.remainingTimeTicks / total : 0F;
        state.accelerationRate = UltimateTimeWandData.multiplierForExponent(entity.getExponent());
        state.tickRateProgress = Math.clamp(state.accelerationRate / 4F, 0F, 1F);
    }

    @Override
    public void submit(TimeWandRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        display.submit(state, pose, collector, camera);
    }
}
