package com.jdte.client.screens;

import com.jdte.client.screens.util.MachineFluidBarRenderer;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.client.screens.util.MachineBarMath;
import com.jdte.common.containers.LootFabricatorContainer;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.network.data.FilterPagePayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.setup.JDTEFluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class LootFabricatorScreen extends BaseMachineScreen<LootFabricatorContainer> {
    private static final Identifier PREV = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final Identifier NEXT = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
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
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        renderMachineSlotBackgrounds(graphics);
        renderProgressArrow(graphics);
        var layout = GuiUpgradeLayoutConfig.getInstance();
        renderFluidTank(graphics, getGuiLeft() + layout.getLootFabricatorLifeFluidX(), getGuiTop() + layout.getLootFabricatorLifeFluidY(),
                new FluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), Math.max(1, lootContainer.getLifeFluidAmount())),
                lootContainer.getLifeFluidAmount());
        renderFluidTank(graphics, getGuiLeft() + layout.getLootFabricatorTimeFluidX(), getGuiTop() + layout.getLootFabricatorTimeFluidY(),
                new FluidStack(JDTRegistration.TIME_FLUID_SOURCE.get(), Math.max(1, lootContainer.getTimeFluidAmount())),
                lootContainer.getTimeFluidAmount());
        if (lootContainer.getMaxOutputPage() > 0) {
            int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
            graphics.blit(RenderPipelines.GUI_TEXTURED, PREV, getGuiLeft() + layout.getLootFabricatorOutputPrevX(), getGuiTop() + layout.getLootFabricatorOutputPrevY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
            graphics.blit(RenderPipelines.GUI_TEXTURED, NEXT, getGuiLeft() + layout.getLootFabricatorOutputNextX(), getGuiTop() + layout.getLootFabricatorOutputNextY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
            graphics.text(font, (lootContainer.getOutputPage() + 1) + "/" + (lootContainer.getMaxOutputPage() + 1), getGuiLeft() + layout.getLootFabricatorOutputPageTextX(), getGuiTop() + layout.getLootFabricatorOutputPageTextY(), 0xFF404040, false);
        }
    }

    private void renderMachineSlotBackgrounds(GuiGraphicsExtractor graphics) {
        int machineSlots = lootContainer.getOutputSlotsPerPage() + com.jdte.common.blockentities.LootFabricatorBE.INPUT_SLOTS;
        for (int i = 0; i < Math.min(machineSlots, lootContainer.slots.size()); i++) {
            Slot slot = lootContainer.slots.get(i);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderProgressArrow(GuiGraphicsExtractor graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + layout.getLootFabricatorProgressArrowX();
        int y = getGuiTop() + layout.getLootFabricatorProgressArrowY();
        int progressWidth = lootContainer.getProgress() <= 0 ? 0 : Math.max(1, (lootContainer.getProgress() * 22) / Math.max(1, lootContainer.getProgressMax()));
        drawArrow(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) drawArrow(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF3DBB57);
    }

    private void drawArrow(GuiGraphicsExtractor graphics, int x, int y, int width, int color) {
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

    private void drawHeadPart(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        if (width > 0) graphics.fill(x, y, x + width, y + height, color);
    }

    private void renderFluidTank(GuiGraphicsExtractor graphics, int x, int y, FluidStack stack, int amount) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);
        int height = MachineBarMath.scaleClamped(amount, lootContainer.getFluidCapacity(), 70);
        if (height > 0) MachineFluidBarRenderer.renderFluid(graphics, stack, x + 1, y + 71, 16, height);
        graphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }


    @Override public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(getGuiLeft() + layout.getLootFabricatorSpeedButtonX(), getGuiTop() + layout.getLootFabricatorSpeedButtonY(), tickSpeed, button -> {
            tickSpeed = ((NumberButton) button).getValue();
            ClientPacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
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

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && lootContainer.getMaxOutputPage() > 0) {
            var layout = GuiUpgradeLayoutConfig.getInstance();
            int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
            int page = lootContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputPrevX(), getGuiTop() + layout.getLootFabricatorOutputPrevY(), buttonSize, buttonSize, mouseX, mouseY)) page--;
            else if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputNextX(), getGuiTop() + layout.getLootFabricatorOutputNextY(), buttonSize, buttonSize, mouseX, mouseY)) page++;
            else return super.mouseClicked(event, doubleClick);
            lootContainer.setOutputPage(page);
            ClientPacketDistributor.sendToServer(new FilterPagePayload(lootContainer.getOutputPage()));
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        var layout = GuiUpgradeLayoutConfig.getInstance();
        if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorLifeFluidX(), getGuiTop() + layout.getLootFabricatorLifeFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("jdte.screen.loot_fabricator.life_fluid", lootContainer.getLifeFluidAmount(), lootContainer.getFluidCapacity()), mouseX, mouseY);
        } else if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorTimeFluidX(), getGuiTop() + layout.getLootFabricatorTimeFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("jdte.screen.loot_fabricator.time_fluid", lootContainer.getTimeFluidAmount(), lootContainer.getFluidCapacity()), mouseX, mouseY);
        }
    }
}
