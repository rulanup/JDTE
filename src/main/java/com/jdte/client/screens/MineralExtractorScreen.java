package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.screens.util.MachineFluidBarRenderer;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.containers.MineralExtractorContainer;
import com.jdte.common.network.data.MineralExtractorOutputPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.recipes.MineralExtractorResourceResolver;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.setup.JDTEItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class MineralExtractorScreen extends BaseMachineScreen<MineralExtractorContainer> {
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private final MineralExtractorContainer extractorContainer;
    private NumberButton multiplierButton;

    public MineralExtractorScreen(MineralExtractorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        extractorContainer = container;
    }

    @Override public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getMineralExtractorExtraWidth();
        extraHeight = layout.getMineralExtractorExtraHeight();
    }

    @Override public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        boolean large = extractorContainer.getSurveySlotCount() > 1;
        multiplierButton = addRenderableWidget(new NumberButton(
                getGuiLeft() + (large ? layout.getLargeMineralExtractorSpeedX() : layout.getMineralExtractorSpeedX()),
                getGuiTop() + (large ? layout.getLargeMineralExtractorSpeedY() : layout.getMineralExtractorSpeedY()), 24, 12,
                extractorContainer.getMultiplier(), 1, extractorContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.mineral_extractor.multiplier"), button ->
                PacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue()))));
    }

    @Override public void addFilterButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(
                getGuiLeft() + layout.getMineralExtractorAllowlistX(),
                getGuiTop() + layout.getMineralExtractorAllowlistY(), filterData.allowlist, button -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
    }

    @Override public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + layout.getMineralExtractorRedstoneX(),
                getGuiTop() + layout.getMineralExtractorRedstoneY(), redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
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
        renderMachineSlotBackgrounds(graphics);
        renderSurveyGhost(graphics);
        renderProgress(graphics);
        renderFluidBars(graphics);
        renderStatus(graphics);
        renderPageControls(graphics);
    }

    private void renderMachineSlotBackgrounds(GuiGraphics graphics) {
        for (int index = 0; index < Math.min(extractorContainer.getMachineSlotCount(), container.slots.size()); index++) {
            Slot slot = container.slots.get(index);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderSurveyGhost(GuiGraphics graphics) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.35F);
        for (int index = 0; index < extractorContainer.getSurveySlotCount(); index++) {
            Slot surveySlot = container.slots.get(index);
            if (!surveySlot.hasItem()) {
                graphics.renderFakeItem(new net.minecraft.world.item.ItemStack(JDTEItems.MINERAL_SURVEY.get()),
                        getGuiLeft() + surveySlot.x, getGuiTop() + surveySlot.y);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderProgress(GuiGraphics graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        boolean large = extractorContainer.getSurveySlotCount() > 1;
        int x = getGuiLeft() + (large
                ? layout.getLargeMineralExtractorProgressX() : layout.getMineralExtractorProgressX());
        int y = getGuiTop() + (large
                ? layout.getLargeMineralExtractorProgressY() : layout.getMineralExtractorProgressY());
        int progress = extractorContainer.getStateId() == MineralExtractorBE.State.RUNNING.ordinal()
                ? Math.clamp((int) ((long) extractorContainer.getProgress() * 24L
                        / Math.max(1, extractorContainer.getProgressMax())), 0, 24)
                : 0;

        // Eclipse Alloy housing, Blazegold rim, and a segmented mineral-blue work channel.
        graphics.fill(x, y, x + 28, y + 9, 0xFF15151A);
        graphics.fill(x + 1, y + 1, x + 27, y + 8, 0xFFB66A24);
        graphics.fill(x + 2, y + 2, x + 26, y + 7, 0xFF252A30);
        if (progress > 0) {
            graphics.fill(x + 2, y + 2, x + 2 + progress, y + 7, 0xFF287B91);
            graphics.fill(x + 2, y + 2, x + 2 + progress, y + 3, 0xFF72D4E5);
            graphics.fill(x + 2, y + 6, x + 2 + progress, y + 7, 0xFF174B5B);
        }
        for (int segment = 4; segment < 24; segment += 4) {
            graphics.fill(x + 2 + segment, y + 3, x + 3 + segment, y + 6, 0x8020272C);
        }
    }

    private void renderFluidBars(GuiGraphics graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int capacity = extractorContainer.getFluidCapacity();
        int experience = extractorContainer.getExperienceFluid();
        int time = extractorContainer.getTimeFluid();
        var roles = MineralExtractorResourceResolver.resolve(
                minecraft == null || minecraft.level == null ? null : minecraft.level.getRecipeManager());
        Fluid experienceFluid = displayFluid(extractorContainer.getExperienceFluidType(),
                BuiltInRegistries.FLUID.getOptional(roles.fortuneFluid()).orElse(Fluids.EMPTY));
        Fluid timeFluid = displayFluid(extractorContainer.getTimeFluidType(),
                BuiltInRegistries.FLUID.getOptional(roles.accelerationFluid()).orElse(Fluids.EMPTY));
        MachineFluidBarRenderer.renderBar(graphics,
                new FluidStack(experienceFluid, Math.max(1, experience)), experience, capacity,
                getGuiLeft() + layout.getMineralExtractorExperienceFluidX(),
                getGuiTop() + layout.getMineralExtractorExperienceFluidY());
        MachineFluidBarRenderer.renderBar(graphics,
                new FluidStack(timeFluid, Math.max(1, time)), time, capacity,
                getGuiLeft() + layout.getMineralExtractorTimeFluidX(),
                getGuiTop() + layout.getMineralExtractorTimeFluidY());
    }

    static Fluid displayFluid(Fluid actual, Fluid configured) {
        return actual == null || actual == Fluids.EMPTY ? configured : actual;
    }

    private void renderStatus(GuiGraphics graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        boolean large = extractorContainer.getSurveySlotCount() > 1;
        int x = getGuiLeft() + (large
                ? layout.getLargeMineralExtractorStatusX() : layout.getMineralExtractorStatusX());
        int y = getGuiTop() + (large
                ? layout.getLargeMineralExtractorStatusY() : layout.getMineralExtractorStatusY());
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        float scale = large ? 7.4F / 9.0F : 7.0F / 9.0F;
        graphics.pose().scale(scale, scale, 1.0F);
        Component state = Component.translatable(stateTranslation());
        if (large) {
            String text = state.getString();
            int separator = Math.max(text.indexOf('：'), text.indexOf(':'));
            String label = separator >= 0 ? text.substring(0, separator + 1) : text;
            String value = separator >= 0 ? text.substring(separator + 1).stripLeading() : "";
            graphics.drawString(font, label, 0, 0, stateColor(), false);
            if (!value.isEmpty()) {
                graphics.drawString(font, value, 0, font.lineHeight, stateColor(), false);
            }
        } else {
            graphics.drawString(font, state, 0, 0, stateColor(), false);
        }
        graphics.pose().popPose();
    }

    private void renderPageControls(GuiGraphics graphics) {
        if (extractorContainer.getMaxOutputPage() <= 0) return;
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int size = layout.getMineralExtractorOutputPageButtonSize();
        int y = getGuiTop() + layout.getMineralExtractorOutputPageY();
        graphics.blit(PREV, getGuiLeft() + layout.getMineralExtractorOutputPrevX(), y,
                0, 0, size, size, size, size);
        graphics.blit(NEXT, getGuiLeft() + layout.getMineralExtractorOutputNextX(), y,
                0, 0, size, size, size, size);
        graphics.drawString(font,
                (extractorContainer.getOutputPage() + 1) + "/" + (extractorContainer.getMaxOutputPage() + 1),
                getGuiLeft() + layout.getMineralExtractorOutputPageTextX(),
                getGuiTop() + layout.getMineralExtractorOutputPageTextY(), 0x404040);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && extractorContainer.getMaxOutputPage() > 0) {
            var layout = GuiUpgradeLayoutConfig.getInstance();
            int size = layout.getMineralExtractorOutputPageButtonSize();
            int y = getGuiTop() + layout.getMineralExtractorOutputPageY();
            int page = extractorContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + layout.getMineralExtractorOutputPrevX(), y,
                    size, size, mouseX, mouseY)) {
                page--;
            } else if (MiscTools.inBounds(getGuiLeft() + layout.getMineralExtractorOutputNextX(), y,
                    size, size, mouseX, mouseY)) {
                page++;
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            extractorContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new MineralExtractorOutputPagePayload(extractorContainer.getOutputPage()));
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
        Slot hoveredSurveySlot = hoveredEmptySurveySlot(mouseX, mouseY);
        if (MiscTools.inBounds(getGuiLeft() + layout.getMineralExtractorExperienceFluidX(),
                getGuiTop() + layout.getMineralExtractorExperienceFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.mineral_extractor.experience_fluid",
                    extractorContainer.getExperienceFluid(), extractorContainer.getFluidCapacity(),
                    extractorContainer.getFortunePercent()), mouseX, mouseY);
        } else if (MiscTools.inBounds(getGuiLeft() + layout.getMineralExtractorTimeFluidX(),
                getGuiTop() + layout.getMineralExtractorTimeFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.mineral_extractor.time_fluid",
                    extractorContainer.getTimeFluid(), extractorContainer.getFluidCapacity()), mouseX, mouseY);
        } else if (hoveredSurveySlot != null) {
            graphics.renderTooltip(font, Language.getInstance().getVisualOrder(java.util.List.of(
                    Component.translatable("jdte.screen.mineral_extractor.survey_slot.title"),
                    Component.translatable("jdte.screen.mineral_extractor.survey_slot.description")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("jdte.screen.mineral_extractor.survey_slot.local")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("jdte.screen.mineral_extractor.source.local")
                            .withStyle(ChatFormatting.AQUA),
                    Component.translatable("jdte.screen.mineral_extractor.minerals",
                                    extractorContainer.getMineralCount())
                            .withStyle(ChatFormatting.GRAY)
            )), mouseX, mouseY);
        }
    }

    private Slot hoveredEmptySurveySlot(int mouseX, int mouseY) {
        for (int index = 0; index < extractorContainer.getSurveySlotCount(); index++) {
            Slot slot = container.slots.get(index);
            if (!slot.hasItem() && MiscTools.inBounds(
                    getGuiLeft() + slot.x, getGuiTop() + slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private String stateTranslation() {
        MineralExtractorBE.State state = MineralExtractorBE.State.values()[Math.floorMod(
                extractorContainer.getStateId(), MineralExtractorBE.State.values().length)];
        return "jdte.screen.mineral_extractor.compact_state."
                + state.name().toLowerCase(java.util.Locale.ROOT);
    }

    private int stateColor() {
        return extractorContainer.getStateId() == MineralExtractorBE.State.RUNNING.ordinal() ? 0x208020 : 0xA04030;
    }
}
