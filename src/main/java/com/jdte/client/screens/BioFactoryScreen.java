package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.containers.BioFactoryContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.setup.JDTEFluids;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class BioFactoryScreen extends BaseMachineScreen<BioFactoryContainer> {
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final int[] TANK_X = {159, 179, -5, 119};
    private static final int TANK_Y = -21;
    private static final Component SPECIMEN = Component.translatable("jdte.slot.bio_factory_specimen");
    private static final Component FOOD = Component.translatable("jdte.slot.bio_factory_food");
    private static final Component OUTPUT = Component.translatable("jdte.slot.bio_factory_output");
    private final BioFactoryContainer factoryContainer;
    private NumberButton multiplierButton;

    public BioFactoryScreen(BioFactoryContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        factoryContainer = container;
    }

    @Override public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getLootFabricatorExtraWidth();
        extraHeight = layout.getLootFabricatorExtraHeight();
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderMachineSlots(graphics);
        renderProgress(graphics);
        BioFactoryBE factory = factoryContainer.getFactory();
        renderTank(graphics, TANK_X[0], factory.getLifeFluidTank().getFluid(), factoryContainer.getLifeFluid());
        renderTank(graphics, TANK_X[1], factory.getTimeFluidTank().getFluid(), factoryContainer.getTimeFluid());
        renderTank(graphics, TANK_X[2], factory.getProcessFluidTank().getFluid(), factoryContainer.getProcessFluid());
        renderTank(graphics, TANK_X[3], factory.getProductFluidTank().getFluid(), factoryContainer.getProductFluid());
        if (factoryContainer.getMaxOutputPage() > 0) {
            graphics.blit(PREV, getGuiLeft() + 75, getGuiTop() + 53, 0, 0, 12, 12, 12, 12);
            graphics.blit(NEXT, getGuiLeft() + 105, getGuiTop() + 53, 0, 0, 12, 12, 12, 12);
            graphics.drawString(font, (factoryContainer.getOutputPage() + 1) + "/" + (factoryContainer.getMaxOutputPage() + 1),
                    getGuiLeft() + 89, getGuiTop() + 55, 0x555555, false);
        }
    }

    private void renderMachineSlots(GuiGraphics graphics) {
        for (int i = 0; i < 1 + BioFactoryBE.INPUT_SLOTS + BioFactoryBE.BASE_OUTPUT_SLOTS; i++) {
            Slot slot = container.slots.get(i);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
        Slot specimen = container.slots.getFirst();
        if (!specimen.hasItem()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.28F);
            graphics.renderFakeItem(new ItemStack(Items.BEE_SPAWN_EGG),
                    getGuiLeft() + specimen.x, getGuiTop() + specimen.y);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderProgress(GuiGraphics graphics) {
        int x = getGuiLeft() + 42;
        int y = getGuiTop() + 7;
        int progressWidth = factoryContainer.getProgress() * 24
                / Math.max(1, factoryContainer.getProcessTicks());
        drawArrow(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrow(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFFDC143C);
        }
    }

    private void drawArrow(GuiGraphics graphics, int x, int y, int width, int color) {
        int clampedWidth = Math.clamp(width, 0, 24);
        if (clampedWidth <= 0) return;
        int bodyWidth = Math.min(16, clampedWidth);
        if (bodyWidth > 0) graphics.fill(x, y + 5, x + bodyWidth, y + 10, color);
        int headWidth = clampedWidth - 16;
        if (headWidth <= 0) return;
        drawArrowHead(graphics, x + 16, y + 4, Math.min(headWidth, 2), 7, color);
        if (headWidth > 2) drawArrowHead(graphics, x + 18, y + 3, Math.min(headWidth - 2, 2), 9, color);
        if (headWidth > 4) drawArrowHead(graphics, x + 20, y + 2, Math.min(headWidth - 4, 2), 11, color);
        if (headWidth > 6) drawArrowHead(graphics, x + 22, y + 1, Math.min(headWidth - 6, 2), 13, color);
    }

    private void drawArrowHead(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width > 0) graphics.fill(x, y, x + width, y + height, color);
    }

    private void renderTank(GuiGraphics graphics, int relativeX, FluidStack stack, int amount) {
        int x = getGuiLeft() + relativeX;
        int y = getGuiTop() + TANK_Y;
        graphics.blit(FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);
        int height = Math.clamp((int) ((long) amount * 70L / Math.max(1, factoryContainer.getFluidCapacity())), 0, 70);
        if (height > 0 && !stack.isEmpty()) renderFluid(graphics, stack, x + 1, y + 71, 16, height);
        graphics.blit(FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderFluid(GuiGraphics graphics, FluidStack stack, int x, int bottom, int width, int height) {
        var extension = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());
        int tint = extension.getTintColor(stack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor((tint >> 16 & 255) / 255.0F, (tint >> 8 & 255) / 255.0F,
                (tint & 255) / 255.0F, (tint >>> 24) / 255.0F);
        PoseStack pose = graphics.pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(pose.last().pose(), x, bottom, 0).setUv(sprite.getU0(), sprite.getV1());
        buffer.addVertex(pose.last().pose(), x + width, bottom, 0).setUv(sprite.getU1(), sprite.getV1());
        buffer.addVertex(pose.last().pose(), x + width, bottom - height, 0).setUv(sprite.getU1(), sprite.getV0());
        buffer.addVertex(pose.last().pose(), x, bottom - height, 0).setUv(sprite.getU0(), sprite.getV0());
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override public void addRedstoneButtons() {
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(getGuiLeft() + 139, getGuiTop() + 37,
                redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }
    @Override public void addTickSpeedButton() {
        multiplierButton = new NumberButton(getGuiLeft() + 42, getGuiTop() + 38,
                24, 12, factoryContainer.getMultiplier(), 1, factoryContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.bio_factory.multiplier"), button ->
                PacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue())));
        addRenderableWidget(multiplierButton);
    }
    @Override protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = factoryContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(factoryContainer.getMultiplier(), multiplierButton.max));
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && factoryContainer.getMaxOutputPage() > 0) {
            int page = factoryContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + 75, getGuiTop() + 53, 12, 12, mouseX, mouseY)) page--;
            else if (MiscTools.inBounds(getGuiLeft() + 105, getGuiTop() + 53, 12, 12, mouseX, mouseY)) page++;
            else return super.mouseClicked(mouseX, mouseY, button);
            factoryContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new FilterPagePayload(factoryContainer.getOutputPage()));
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            if (factoryContainer.isSpecimenSlot(hoveredSlot)) graphics.renderTooltip(font, SPECIMEN, mouseX, mouseY);
            else if (factoryContainer.isFoodSlot(hoveredSlot)) graphics.renderTooltip(font, FOOD, mouseX, mouseY);
            else if (factoryContainer.isOutputSlot(hoveredSlot)) graphics.renderTooltip(font, OUTPUT, mouseX, mouseY);
        }
        String[] keys = {"life_fluid", "time_fluid", "process_fluid", "product_fluid"};
        int[] amounts = {factoryContainer.getLifeFluid(), factoryContainer.getTimeFluid(),
                factoryContainer.getProcessFluid(), factoryContainer.getProductFluid()};
        for (int i = 0; i < TANK_X.length; i++) {
            if (MiscTools.inBounds(getGuiLeft() + TANK_X[i], getGuiTop() + TANK_Y, 18, 72, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable("jdte.screen.bio_factory." + keys[i],
                        amounts[i], factoryContainer.getFluidCapacity()), mouseX, mouseY);
                return;
            }
        }
    }
}
