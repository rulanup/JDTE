package com.jdte.client.screens;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.screens.util.MachineFluidBarRenderer;
import com.jdte.client.screens.util.GreenhouseSlotCountRenderer;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.containers.MineralExtractorContainer;
import com.jdte.common.network.data.MineralExtractorOutputPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.recipes.MineralExtractorResourceResolver;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.setup.JDTEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class MineralExtractorScreen extends BaseMachineScreen<MineralExtractorContainer> {
    private static final Identifier PREV = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final Identifier NEXT = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private final MineralExtractorContainer extractorContainer;
    private NumberButton multiplierButton;

    public MineralExtractorScreen(MineralExtractorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        extractorContainer = container;
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countLabel) {
        if (extractorContainer.isOutputSlot(slot) && stack.getCount() > 1) {
            GreenhouseSlotCountRenderer.extractRenderState(graphics, font, stack, slot, imageWidth, countLabel);
            return;
        }
        super.renderSlotContents(graphics, stack, slot, countLabel);
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
                ClientPacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue()))));
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

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        renderMachineSlotBackgrounds(graphics);
        renderSurveyGhost(graphics);
        renderProgress(graphics);
        renderFluidBars(graphics);
        renderStatus(graphics);
        renderPageControls(graphics);
    }

    private void renderMachineSlotBackgrounds(GuiGraphicsExtractor graphics) {
        for (int index = 0; index < Math.min(extractorContainer.getMachineSlotCount(), container.slots.size()); index++) {
            Slot slot = container.slots.get(index);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderSurveyGhost(GuiGraphicsExtractor graphics) {
        for (int index = 0; index < extractorContainer.getSurveySlotCount(); index++) {
            Slot surveySlot = container.slots.get(index);
            if (!surveySlot.hasItem()) {
                GhostItemRenderer.draw(graphics, new net.minecraft.world.item.ItemStack(JDTEItems.MINERAL_SURVEY.get()),
                        getGuiLeft() + surveySlot.x, getGuiTop() + surveySlot.y, 0xA6888888);
            }
        }
    }

    private void renderProgress(GuiGraphicsExtractor graphics) {
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

    private void renderFluidBars(GuiGraphicsExtractor graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        FluidDisplays displays = fluidDisplays();
        MineralExtractorFluidDisplay experience = displays.experience();
        MineralExtractorFluidDisplay acceleration = displays.acceleration();
        MachineFluidBarRenderer.renderBar(graphics,
                experience.renderedFluid(), experience.amount(), experience.capacity(),
                getGuiLeft() + layout.getMineralExtractorExperienceFluidX(),
                getGuiTop() + layout.getMineralExtractorExperienceFluidY());
        MachineFluidBarRenderer.renderBar(graphics,
                acceleration.renderedFluid(), acceleration.amount(), acceleration.capacity(),
                getGuiLeft() + layout.getMineralExtractorTimeFluidX(),
                getGuiTop() + layout.getMineralExtractorTimeFluidY());
    }

    private FluidDisplays fluidDisplays() {
        var roles = MineralExtractorResourceResolver.resolve(minecraft.level);
        int capacity = extractorContainer.getFluidCapacity();
        return new FluidDisplays(
                MineralExtractorFluidDisplay.create(
                        extractorContainer.getExperienceFluidType(),
                        BuiltInRegistries.FLUID.getOptional(roles.fortuneFluid()).orElse(Fluids.EMPTY),
                        extractorContainer.getExperienceFluid(), capacity),
                MineralExtractorFluidDisplay.create(
                        extractorContainer.getTimeFluidType(),
                        BuiltInRegistries.FLUID.getOptional(roles.accelerationFluid()).orElse(Fluids.EMPTY),
                        extractorContainer.getTimeFluid(), capacity));
    }

    static List<FormattedText> experienceTooltip(MineralExtractorFluidDisplay display, int fortunePercent) {
        return List.of(
                display.tooltipName(),
                Component.translatable("jdte.screen.mineral_extractor.experience_fluid",
                        display.amount(), display.capacity(), fortunePercent),
                Component.translatable("jdte.screen.mineral_extractor.fortune_optional")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("jdte.screen.mineral_extractor.base_power_only")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    static List<FormattedText> accelerationTooltip(MineralExtractorFluidDisplay display) {
        return List.of(
                display.tooltipName(),
                Component.translatable("jdte.screen.mineral_extractor.time_fluid",
                        display.amount(), display.capacity()),
                Component.translatable("jdte.screen.mineral_extractor.acceleration_optional")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("jdte.screen.mineral_extractor.base_power_only")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    private void renderStatus(GuiGraphicsExtractor graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        boolean large = extractorContainer.getSurveySlotCount() > 1;
        int x = getGuiLeft() + (large
                ? layout.getLargeMineralExtractorStatusX() : layout.getMineralExtractorStatusX());
        int y = getGuiTop() + (large
                ? layout.getLargeMineralExtractorStatusY() : layout.getMineralExtractorStatusY());
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        float scale = large ? 7.4F / 9.0F : 7.0F / 9.0F;
        graphics.pose().scale(scale, scale);
        Component state = Component.translatable(stateTranslation());
        if (large) {
            String text = state.getString();
            int separator = Math.max(text.indexOf('：'), text.indexOf(':'));
            String label = separator >= 0 ? text.substring(0, separator + 1) : text;
            String value = separator >= 0 ? text.substring(separator + 1).stripLeading() : "";
            graphics.text(font, label, 0, 0, stateColor(), false);
            if (!value.isEmpty()) {
                graphics.text(font, value, 0, font.lineHeight, stateColor(), false);
            }
        } else {
            graphics.text(font, state, 0, 0, stateColor(), false);
        }
        graphics.pose().popMatrix();
    }

    private void renderPageControls(GuiGraphicsExtractor graphics) {
        if (extractorContainer.getMaxOutputPage() <= 0) return;
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int size = layout.getMineralExtractorOutputPageButtonSize();
        int y = getGuiTop() + layout.getMineralExtractorOutputPageY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, PREV, getGuiLeft() + layout.getMineralExtractorOutputPrevX(), y,
                0, 0, size, size, size, size);
        graphics.blit(RenderPipelines.GUI_TEXTURED, NEXT, getGuiLeft() + layout.getMineralExtractorOutputNextX(), y,
                0, 0, size, size, size, size);
        graphics.text(font,
                (extractorContainer.getOutputPage() + 1) + "/" + (extractorContainer.getMaxOutputPage() + 1),
                getGuiLeft() + layout.getMineralExtractorOutputPageTextX(),
                getGuiTop() + layout.getMineralExtractorOutputPageTextY(), 0xFF404040);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
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
                return super.mouseClicked(event, doubleClick);
            }
            extractorContainer.setOutputPage(page);
            ClientPacketDistributor.sendToServer(new MineralExtractorOutputPagePayload(extractorContainer.getOutputPage()));
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
        Slot hoveredSurveySlot = hoveredEmptySurveySlot(mouseX, mouseY);
        if (MiscTools.inBounds(getGuiLeft() + layout.getMineralExtractorExperienceFluidX(),
                getGuiTop() + layout.getMineralExtractorExperienceFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(
                    experienceTooltip(fluidDisplays().experience(), extractorContainer.getFortunePercent())),
                    mouseX, mouseY);
        } else if (MiscTools.inBounds(getGuiLeft() + layout.getMineralExtractorTimeFluidX(),
                getGuiTop() + layout.getMineralExtractorTimeFluidY(), 18, 72, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(
                    accelerationTooltip(fluidDisplays().acceleration())), mouseX, mouseY);
        } else if (hoveredSurveySlot != null) {
            graphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(java.util.List.of(
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

    private record FluidDisplays(MineralExtractorFluidDisplay experience,
                                 MineralExtractorFluidDisplay acceleration) {
    }
}
