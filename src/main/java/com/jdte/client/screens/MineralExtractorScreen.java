package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.containers.MineralExtractorContainer;
import com.jdte.common.network.data.MineralExtractorOutputPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MineralExtractorScreen extends BaseMachineScreen<MineralExtractorContainer> {
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final int XP_COLOR = 0xFF67C93A;
    private static final int TIME_COLOR = 0xFF52A7E8;
    private final MineralExtractorContainer extractorContainer;
    private NumberButton multiplierButton;

    public MineralExtractorScreen(MineralExtractorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        extractorContainer = container;
    }

    @Override public void setTopSection() {
        extraWidth = 76;
        extraHeight = 0;
    }

    @Override public void init() {
        super.init();
        multiplierButton = addRenderableWidget(new NumberButton(
                getGuiLeft() + 146, topSectionTop + 42, 34, 12,
                extractorContainer.getMultiplier(), 1, extractorContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.mineral_extractor.multiplier"), button ->
                PacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue()))));
    }

    @Override public void addTickSpeedButton() {
    }

    @Override public void addFilterButtons() {
        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(
                getGuiLeft() + 146, topSectionTop + 58, filterData.allowlist, button -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = extractorContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(extractorContainer.getMultiplier(), multiplierButton.max));
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderSlots(graphics);
        renderEnergy(graphics);
        renderFluidBar(graphics, getGuiLeft() + 146, topSectionTop + 8,
                extractorContainer.getExperienceFluid(), extractorContainer.getFluidCapacity(), XP_COLOR);
        renderFluidBar(graphics, getGuiLeft() + 164, topSectionTop + 8,
                extractorContainer.getTimeFluid(), extractorContainer.getFluidCapacity(), TIME_COLOR);
        int progress = Math.clamp((int) ((long) extractorContainer.getProgress() * 22L
                / Math.max(1, extractorContainer.getProgressMax())), 0, 22);
        graphics.fill(getGuiLeft() + 38, topSectionTop + 32,
                getGuiLeft() + 60, topSectionTop + 37, 0xFF353535);
        graphics.fill(getGuiLeft() + 38, topSectionTop + 32,
                getGuiLeft() + 38 + progress, topSectionTop + 37, 0xFF4DBB63);
        graphics.drawString(font, Component.translatable(
                        extractorContainer.usesSurvey()
                                ? "jdte.screen.mineral_extractor.source.survey"
                                : "jdte.screen.mineral_extractor.source.local"),
                getGuiLeft() + 8, topSectionTop + 78, 0x404040, false);
        graphics.drawString(font, Component.translatable("jdte.screen.mineral_extractor.minerals",
                        extractorContainer.getMineralCount()),
                getGuiLeft() + 8, topSectionTop + 89, 0x404040, false);
        graphics.drawString(font, Component.translatable(stateTranslation()),
                getGuiLeft() + 8, topSectionTop + 100, stateColor(), false);
        renderPageControls(graphics);
    }

    private void renderSlots(GuiGraphics graphics) {
        for (int index = 0; index < Math.min(17, container.slots.size()); index++) {
            Slot slot = container.slots.get(index);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderEnergy(GuiGraphics graphics) {
        int maxEnergy = Math.max(1, extractorContainer.baseMachineBE instanceof MineralExtractorBE extractor
                ? extractor.getMaxEnergy() : 1);
        int fill = Math.clamp((int) ((long) extractorContainer.getEnergy() * 70L / maxEnergy), 0, 70);
        int x = topSectionLeft + getEnergyBarOffset();
        int y = topSectionTop + 5;
        graphics.blit(POWERBAR, x, y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWERBAR, x + 1, y + 70 - fill, 19, 69 - fill, 17, fill + 1, 36, 72);
    }

    private static void renderFluidBar(GuiGraphics graphics, int x, int y, int amount, int capacity, int color) {
        graphics.fill(x, y, x + 14, y + 30, 0xFF252525);
        int height = Math.clamp((int) ((long) amount * 28L / Math.max(1, capacity)), 0, 28);
        graphics.fill(x + 1, y + 29 - height, x + 13, y + 29, color);
    }

    private void renderPageControls(GuiGraphics graphics) {
        if (extractorContainer.getMaxOutputPage() <= 0) return;
        int y = topSectionTop + 84;
        graphics.blit(PREV, getGuiLeft() + 88, y, 0, 0, 8, 8, 8, 8);
        graphics.blit(NEXT, getGuiLeft() + 124, y, 0, 0, 8, 8, 8, 8);
        graphics.drawCenteredString(font,
                (extractorContainer.getOutputPage() + 1) + "/" + (extractorContainer.getMaxOutputPage() + 1),
                getGuiLeft() + 110, y, 0x404040);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && extractorContainer.getMaxOutputPage() > 0) {
            int y = topSectionTop + 84;
            int page = extractorContainer.getOutputPage();
            if (inside(mouseX, mouseY, getGuiLeft() + 88, y, 8, 8)) page--;
            else if (inside(mouseX, mouseY, getGuiLeft() + 124, y, 8, 8)) page++;
            else return super.mouseClicked(mouseX, mouseY, button);
            extractorContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new MineralExtractorOutputPagePayload(extractorContainer.getOutputPage()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (inside(mouseX, mouseY, getGuiLeft() + 146, topSectionTop + 8, 14, 30)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.mineral_extractor.experience_fluid",
                    extractorContainer.getExperienceFluid(), extractorContainer.getFluidCapacity(),
                    extractorContainer.getFortunePercent()), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, getGuiLeft() + 164, topSectionTop + 8, 14, 30)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.mineral_extractor.time_fluid",
                    extractorContainer.getTimeFluid(), extractorContainer.getFluidCapacity()), mouseX, mouseY);
        }
    }

    private String stateTranslation() {
        MineralExtractorBE.State state = MineralExtractorBE.State.values()[Math.floorMod(
                extractorContainer.getStateId(), MineralExtractorBE.State.values().length)];
        return "jdte.screen.mineral_extractor.state." + state.name().toLowerCase(java.util.Locale.ROOT);
    }

    private int stateColor() {
        return extractorContainer.getStateId() == MineralExtractorBE.State.RUNNING.ordinal() ? 0x208020 : 0xA04030;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}