package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.LootFabricatorContainer;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.network.data.FilterPagePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import com.direwolf20.justdirethings.util.MiscTools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEFluids;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class LootFabricatorScreen extends BaseMachineScreen<LootFabricatorContainer> {
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private final LootFabricatorContainer lootContainer;
    public LootFabricatorScreen(LootFabricatorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.lootContainer = container;
    }
    @Override public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getLootFabricatorExtraWidth();
        extraHeight = layout.getLootFabricatorExtraHeight();
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderMachineSlotBackgrounds(graphics);
        renderProgressArrow(graphics);
        var layout = GuiUpgradeLayoutConfig.getInstance();
        renderFluidTank(graphics, getGuiLeft() + layout.getLootFabricatorLifeFluidX(), getGuiTop() + layout.getLootFabricatorLifeFluidY(),
                new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), Math.max(1, lootContainer.getLifeFluidAmount())),
                lootContainer.getLifeFluidAmount());
        renderFluidTank(graphics, getGuiLeft() + layout.getLootFabricatorTimeFluidX(), getGuiTop() + layout.getLootFabricatorTimeFluidY(),
                new FluidStack(Registration.TIME_FLUID_SOURCE.get(), Math.max(1, lootContainer.getTimeFluidAmount())),
                lootContainer.getTimeFluidAmount());
        if (lootContainer.getMaxOutputPage() > 0) {
            int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
            graphics.blit(PREV, getGuiLeft() + layout.getLootFabricatorOutputPrevX(), getGuiTop() + layout.getLootFabricatorOutputPrevY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
            graphics.blit(NEXT, getGuiLeft() + layout.getLootFabricatorOutputNextX(), getGuiTop() + layout.getLootFabricatorOutputNextY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
            graphics.drawString(font, (lootContainer.getOutputPage() + 1) + "/" + (lootContainer.getMaxOutputPage() + 1), getGuiLeft() + layout.getLootFabricatorOutputPageTextX(), getGuiTop() + layout.getLootFabricatorOutputPageTextY(), 0x404040, false);
        }
    }

    private void renderMachineSlotBackgrounds(GuiGraphics graphics) {
        int machineSlots = lootContainer.getOutputSlotsPerPage() + com.jdte.common.blockentities.LootFabricatorBE.INPUT_SLOTS;
        for (int i = 0; i < Math.min(machineSlots, lootContainer.slots.size()); i++) {
            Slot slot = lootContainer.slots.get(i);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderProgressArrow(GuiGraphics graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + layout.getLootFabricatorProgressArrowX();
        int y = getGuiTop() + layout.getLootFabricatorProgressArrowY();
        int progressWidth = lootContainer.getProgress() <= 0 ? 0 : Math.max(1, (lootContainer.getProgress() * 22) / Math.max(1, lootContainer.getProgressMax()));
        drawArrow(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) drawArrow(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF3DBB57);
    }

    private void drawArrow(GuiGraphics graphics, int x, int y, int width, int color) {
        int clamped = Math.clamp(width, 0, 24);
        if (clamped <= 0) return;
        int body = Math.min(16, clamped);
        graphics.fill(x, y + 5, x + body, y + 10, color);
        int head = clamped - 16;
        if (head > 0) drawHeadPart(graphics, x + 16, y + 4, Math.min(head, 2), 7, color);
        if (head > 2) drawHeadPart(graphics, x + 18, y + 3, Math.min(head - 2, 2), 9, color);
        if (head > 4) drawHeadPart(graphics, x + 20, y + 2, Math.min(head - 4, 2), 11, color);
        if (head > 6) drawHeadPart(graphics, x + 22, y + 1, Math.min(head - 6, 2), 13, color);
    }

    private void drawHeadPart(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width > 0) graphics.fill(x, y, x + width, y + height, color);
    }

    private void renderFluidTank(GuiGraphics graphics, int x, int y, FluidStack stack, int amount) {
        graphics.blit(FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);
        int height = Math.min(70, (amount * 70) / Math.max(1, lootContainer.getFluidCapacity()));
        if (height > 0) renderFluidStack(graphics, stack, x + 1, y + 71, 16, height);
        graphics.blit(FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderFluidStack(GuiGraphics graphics, FluidStack stack, int startX, int startY, int width, int height) {
        var extension = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());
        int tint = extension.getTintColor(stack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor((tint >> 16 & 255) / 255.0F, (tint >> 8 & 255) / 255.0F, (tint & 255) / 255.0F, 1.0F);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        int textureWidth = sprite.contents().width();
        int textureHeight = sprite.contents().height();
        for (int yOffset = 0; yOffset < height; yOffset += textureHeight) {
            int drawHeight = Math.min(textureHeight, height - yOffset);
            int drawY = startY - yOffset - drawHeight;
            float vMax = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * drawHeight / textureHeight;
            for (int xOffset = 0; xOffset < width; xOffset += textureWidth) {
                int drawWidth = Math.min(textureWidth, width - xOffset);
                float uMax = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * drawWidth / textureWidth;
                buffer.addVertex(pose.last().pose(), startX + xOffset, drawY + drawHeight, 0).setUv(sprite.getU0(), vMax);
                buffer.addVertex(pose.last().pose(), startX + xOffset + drawWidth, drawY + drawHeight, 0).setUv(uMax, vMax);
                buffer.addVertex(pose.last().pose(), startX + xOffset + drawWidth, drawY, 0).setUv(uMax, sprite.getV0());
                buffer.addVertex(pose.last().pose(), startX + xOffset, drawY, 0).setUv(sprite.getU0(), sprite.getV0());
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        pose.popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(getGuiLeft() + layout.getLootFabricatorSpeedButtonX(), getGuiTop() + layout.getLootFabricatorSpeedButtonY(), tickSpeed, button -> {
            tickSpeed = ((NumberButton) button).getValue();
            PacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
        }));
    }

    @Override public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(getGuiLeft() + layout.getLootFabricatorRedstoneButtonX(), getGuiTop() + layout.getLootFabricatorRedstoneButtonY(),
                redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && lootContainer.getMaxOutputPage() > 0) {
            var layout = GuiUpgradeLayoutConfig.getInstance();
            int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
            int page = lootContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputPrevX(), getGuiTop() + layout.getLootFabricatorOutputPrevY(), buttonSize, buttonSize, mouseX, mouseY)) page--;
            else if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputNextX(), getGuiTop() + layout.getLootFabricatorOutputNextY(), buttonSize, buttonSize, mouseX, mouseY)) page++;
            else return super.mouseClicked(mouseX, mouseY, button);
            lootContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new FilterPagePayload(lootContainer.getOutputPage()));
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        var layout = GuiUpgradeLayoutConfig.getInstance();
        if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorLifeFluidX(), getGuiTop() + layout.getLootFabricatorLifeFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.loot_fabricator.life_fluid", lootContainer.getLifeFluidAmount(), lootContainer.getFluidCapacity()), mouseX, mouseY);
        } else if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorTimeFluidX(), getGuiTop() + layout.getLootFabricatorTimeFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.loot_fabricator.time_fluid", lootContainer.getTimeFluidAmount(), lootContainer.getFluidCapacity()), mouseX, mouseY);
        }
    }
}
