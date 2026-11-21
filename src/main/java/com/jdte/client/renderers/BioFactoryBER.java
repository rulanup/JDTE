package com.jdte.client.renderers;

import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.integrations.ProductiveBeesBioFactoryIntegration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.fml.ModList;

import java.util.WeakHashMap;

public class BioFactoryBER implements BlockEntityRenderer<BioFactoryBE, BioFactoryBER.State> {
    public static final class State extends BlockEntityRenderState {
        EntityRenderState specimen;
        float scale;
        float angle;
    }
    private final EntityRenderDispatcher dispatcher;
    private final WeakHashMap<BioFactoryBE, DisplayCache> cache = new WeakHashMap<>();

    public BioFactoryBER(BlockEntityRendererProvider.Context context) {
        dispatcher = context.entityRenderer();
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(BioFactoryBE factory, State state, float partialTick,
                                             Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(factory, state, partialTick, camera, overlay);
        state.specimen = null;
        var inventory = factory.getMachineHandler();
        ItemStack specimen = inventory.getResource(BioFactoryBE.SPECIMEN_SLOT).toStack(
                inventory.getAmountAsInt(BioFactoryBE.SPECIMEN_SLOT));
        if (specimen.isEmpty() || factory.getLevel() == null) return;
        DisplayCache current = cache.get(factory);
        if (current == null || !ItemStack.isSameItemSameComponents(current.specimen, specimen)) {
            Entity entity = createDisplayEntity(factory, specimen);
            current = new DisplayCache(specimen.copy(), entity);
            cache.put(factory, current);
        }
        if (current.entity == null) return;
        float width = Math.max(0.5F, current.entity.getBbWidth());
        float height = Math.max(0.5F, current.entity.getBbHeight());
        state.scale = Math.min(0.68F / width, 0.68F / height) * 0.8F;
        state.angle = (factory.getLevel().getGameTime() + partialTick) * 1.5F % 360F;
        state.specimen = dispatcher.extractEntity(current.entity, partialTick);
        state.specimen.lightCoords = state.lightCoords;
        state.specimen.shadowPieces.clear();
    }

    @Override public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.specimen == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D, 0.5D);
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.angle));
        dispatcher.submit(state.specimen, camera, 0, 0, 0, poseStack, collector);
        poseStack.popPose();
    }

    private Entity createDisplayEntity(BioFactoryBE factory, ItemStack specimen) {
        try {
            if (ModList.get().isLoaded("productivebees")
                    && specimen.getItem() instanceof SpawnEggItem) {
                Entity bee = ProductiveBeesBioFactoryIntegration.createBee(specimen, factory.getLevel());
                if (bee != null) return bee;
            }
            if (specimen.getItem() instanceof SpawnEggItem egg) return egg.getType(specimen).create(factory.getLevel(), net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
            if (ModList.get().isLoaded("productivebees")) {
                return ProductiveBeesBioFactoryIntegration.createBee(specimen, factory.getLevel());
            }
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    @Override public int getViewDistance() { return 32; }

    private record DisplayCache(ItemStack specimen, Entity entity) { }
}
