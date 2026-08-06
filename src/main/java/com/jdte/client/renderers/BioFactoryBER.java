package com.jdte.client.renderers;

import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.integrations.ProductiveBeesBioFactoryIntegration;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.fml.ModList;

import java.util.WeakHashMap;

public class BioFactoryBER implements BlockEntityRenderer<BioFactoryBE> {
    private final EntityRenderDispatcher dispatcher;
    private final WeakHashMap<BioFactoryBE, DisplayCache> cache = new WeakHashMap<>();

    public BioFactoryBER(BlockEntityRendererProvider.Context context) {
        dispatcher = context.getEntityRenderer();
    }

    @Override public void render(BioFactoryBE factory, float partialTick, PoseStack poseStack,
                                 MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack specimen = factory.getMachineHandler().getStackInSlot(BioFactoryBE.SPECIMEN_SLOT);
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
        float scale = Math.min(0.68F / width, 0.68F / height) * 0.8F;
        long gameTime = factory.getLevel().getGameTime();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D, 0.5D);
        poseStack.scale(scale, scale, scale);
        dispatcher.render(current.entity, 0.0D, 0.0D, 0.0D,
                (gameTime + partialTick) * 1.5F % 360.0F, partialTick, poseStack, buffers, packedLight);
        poseStack.popPose();
    }

    private Entity createDisplayEntity(BioFactoryBE factory, ItemStack specimen) {
        try {
            if (specimen.getItem() instanceof SpawnEggItem egg) return egg.getType(specimen).create(factory.getLevel());
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
