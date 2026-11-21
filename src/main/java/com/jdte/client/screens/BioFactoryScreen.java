package com.jdte.client.screens;

import com.jdte.client.screens.util.MachineFluidBarRenderer;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.containers.BioFactoryContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.jdte.client.screens.util.GreenhouseSlotCountRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class BioFactoryScreen extends BaseMachineScreen<BioFactoryContainer> {
    private static final Identifier PREV = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final Identifier NEXT = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final int TANK_WIDTH = 18;
    private static final int TANK_HEIGHT = 72;
    private static final Component SPECIMEN = Component.translatable("jdte.slot.bio_factory_specimen");
    private static final Component FOOD = Component.translatable("jdte.slot.bio_factory_food");
    private static final Component OUTPUT = Component.translatable("jdte.slot.bio_factory_output");
    private final BioFactoryContainer factoryContainer;
    private NumberButton multiplierButton;

    public BioFactoryScreen(BioFactoryContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        factoryContainer = container;
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countLabel) {
        if (factoryContainer.isOutputSlot(slot) && stack.getCount() > 1) {
            GreenhouseSlotCountRenderer.extractRenderState(graphics, font, stack, slot, imageWidth, countLabel);
            return;
        }
        super.renderSlotContents(graphics, stack, slot, countLabel);
    }

    @Override public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getBioFactoryExtraWidth();
        extraHeight = layout.getBioFactoryExtraHeight();
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        renderMachineSlots(graphics);
        renderProgress(graphics);
        BioFactoryBE factory = factoryContainer.getFactory();
        var layout = GuiUpgradeLayoutConfig.getInstance();
        renderTank(graphics, layout.getBioFactoryLifeFluidX(), factory.getLifeFluidTank().getFluid(), factoryContainer.getLifeFluid());
        renderTank(graphics, layout.getBioFactoryTimeFluidX(), factory.getTimeFluidTank().getFluid(), factoryContainer.getTimeFluid());
        renderTank(graphics, layout.getBioFactoryProcessFluidX(), factory.getProcessFluidTank().getFluid(), factoryContainer.getProcessFluid());
        renderTank(graphics, layout.getBioFactoryProductFluidX(), factory.getProductFluidTank().getFluid(), factoryContainer.getProductFluid());
        if (factoryContainer.getMaxOutputPage() > 0) {
            int buttonSize = 12;
            graphics.blit(RenderPipelines.GUI_TEXTURED, PREV, getGuiLeft() + layout.getBioFactoryOutputPrevX(), getGuiTop() + layout.getBioFactoryOutputPrevY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
            graphics.blit(RenderPipelines.GUI_TEXTURED, NEXT, getGuiLeft() + layout.getBioFactoryOutputNextX(), getGuiTop() + layout.getBioFactoryOutputNextY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
            graphics.text(font, (factoryContainer.getOutputPage() + 1) + "/" + (factoryContainer.getMaxOutputPage() + 1),
                    getGuiLeft() + layout.getBioFactoryOutputPageTextX(), getGuiTop() + layout.getBioFactoryOutputPageTextY(), 0xFF555555, false);
        }
    }

    private void renderMachineSlots(GuiGraphicsExtractor graphics) {
        for (int i = 0; i < 1 + BioFactoryBE.INPUT_SLOTS + BioFactoryBE.BASE_OUTPUT_SLOTS; i++) {
            Slot slot = container.slots.get(i);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
        Slot specimen = container.slots.getFirst();
        if (!specimen.hasItem()) {
            GhostItemRenderer.draw(graphics, new ItemStack(Items.BEE_SPAWN_EGG),
                    getGuiLeft() + specimen.x, getGuiTop() + specimen.y, 0xB8888888);
        }
    }

    private void renderProgress(GuiGraphicsExtractor graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + layout.getBioFactoryProgressArrowX();
        int y = getGuiTop() + layout.getBioFactoryProgressArrowY();
        int progressWidth = factoryContainer.getProgress() * 24
                / Math.max(1, factoryContainer.getProcessTicks());
        drawArrow(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrow(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFFDC143C);
        }
    }

    private void drawArrow(GuiGraphicsExtractor graphics, int x, int y, int width, int color) {
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

    private void drawArrowHead(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        if (width > 0) graphics.fill(x, y, x + width, y + height, color);
    }

    private void renderTank(GuiGraphicsExtractor graphics, int relativeX, FluidStack stack, int amount) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + relativeX;
        int y = getGuiTop() + layout.getBioFactoryFluidY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);
        int height = Math.clamp((int) ((long) amount * 70L / Math.max(1, factoryContainer.getFluidCapacity())), 0, 70);
        if (height > 0 && !stack.isEmpty()) MachineFluidBarRenderer.renderStretchedFluid(graphics, stack, x + 1, y + 71, 16, height);
        graphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }


    @Override public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(getGuiLeft() + layout.getBioFactoryRedstoneButtonX(),
                getGuiTop() + layout.getBioFactoryRedstoneButtonY(),
                redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }
    @Override public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        multiplierButton = new NumberButton(getGuiLeft() + layout.getBioFactoryMultiplierButtonX(),
                getGuiTop() + layout.getBioFactoryMultiplierButtonY(),
                24, 12, factoryContainer.getMultiplier(), 1, factoryContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.bio_factory.multiplier"), button ->
                ClientPacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue())));
        addRenderableWidget(multiplierButton);
    }
    @Override protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = factoryContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(factoryContainer.getMultiplier(), multiplierButton.max));
        }
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && factoryContainer.getMaxOutputPage() > 0) {
            var layout = GuiUpgradeLayoutConfig.getInstance();
            int buttonSize = 12;
            int page = factoryContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + layout.getBioFactoryOutputPrevX(), getGuiTop() + layout.getBioFactoryOutputPrevY(), buttonSize, buttonSize, mouseX, mouseY)) page--;
            else if (MiscTools.inBounds(getGuiLeft() + layout.getBioFactoryOutputNextX(), getGuiTop() + layout.getBioFactoryOutputNextY(), buttonSize, buttonSize, mouseX, mouseY)) page++;
            else return super.mouseClicked(event, doubleClick);
            factoryContainer.setOutputPage(page);
            ClientPacketDistributor.sendToServer(new FilterPagePayload(factoryContainer.getOutputPage()));
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            if (factoryContainer.isSpecimenSlot(hoveredSlot)) graphics.setTooltipForNextFrame(font, SPECIMEN, mouseX, mouseY);
            else if (factoryContainer.isFoodSlot(hoveredSlot)) graphics.setTooltipForNextFrame(font, FOOD, mouseX, mouseY);
            else if (factoryContainer.isOutputSlot(hoveredSlot)) graphics.setTooltipForNextFrame(font, OUTPUT, mouseX, mouseY);
        }
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int fluidY = layout.getBioFactoryFluidY();
        int[] fluidXs = {layout.getBioFactoryLifeFluidX(), layout.getBioFactoryTimeFluidX(),
                layout.getBioFactoryProcessFluidX(), layout.getBioFactoryProductFluidX()};
        String[] keys = {"life_fluid", "time_fluid", "process_fluid", "product_fluid"};
        int[] amounts = {factoryContainer.getLifeFluid(), factoryContainer.getTimeFluid(),
                factoryContainer.getProcessFluid(), factoryContainer.getProductFluid()};
        for (int i = 0; i < fluidXs.length; i++) {
            if (MiscTools.inBounds(getGuiLeft() + fluidXs[i], getGuiTop() + fluidY, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("jdte.screen.bio_factory." + keys[i],
                        amounts[i], factoryContainer.getFluidCapacity()), mouseX, mouseY);
                return;
            }
        }
    }
}
