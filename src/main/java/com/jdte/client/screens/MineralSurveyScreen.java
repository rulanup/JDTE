package com.jdte.client.screens;

import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class MineralSurveyScreen extends Screen {
    private static final int ENTRIES_PER_PAGE = 7;
    private final MineralSurveyData survey;
    private int page;
    private Button previousButton;
    private Button nextButton;

    public MineralSurveyScreen(MineralSurveyData survey) {
        super(Component.translatable("jdte.screen.mineral_survey.title"));
        this.survey = survey;
    }

    @Override
    protected void init() {
        int buttonY = height - 32;
        previousButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(width / 2 - 70, buttonY, 40, 20)
                .build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(width / 2 + 30, buttonY, 40, 20)
                .build());
        updateButtons();
    }

    private void changePage(int delta) {
        page = Math.max(0, Math.min(page + delta, pageCount() - 1));
        updateButtons();
    }

    private void updateButtons() {
        if (previousButton == null || nextButton == null) return;
        previousButton.active = page > 0;
        nextButton.active = page + 1 < pageCount();
    }

    private int pageCount() {
        return Math.max(1, (survey.entries().size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        graphics.drawCenteredString(font,
                Component.translatable("jdte.screen.mineral_survey.source", survey.biomeId(), survey.dimensionId()),
                width / 2, 31, 0xA0E8FF);
        graphics.drawCenteredString(font,
                Component.translatable("jdte.screen.mineral_survey.estimated"),
                width / 2, 44, 0x909090);

        long totalWeight = survey.totalWeight();
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, survey.entries().size());
        int x = Math.max(12, width / 2 - 150);
        int y = 64;
        for (int index = start; index < end; index++) {
            MineralEntry entry = survey.entries().get(index);
            graphics.drawString(font,
                    Component.translatable("jdte.screen.mineral_survey.entry",
                            MineralSurveyItem.displayName(entry),
                            MineralSurveyItem.formatPercent(entry.weight(), totalWeight)),
                    x, y, 0xFFFFFF, false);
            graphics.drawString(font,
                    Component.translatable("jdte.screen.mineral_survey.details",
                            formatHeight(entry.minY()), formatHeight(entry.maxY()), entry.veinSize(),
                            Component.translatable("jdte.mineral.confidence."
                                    + entry.confidence().name().toLowerCase(Locale.ROOT))),
                    x + 8, y + 11, 0xA0A0A0, false);
            y += 25;
        }

        graphics.drawCenteredString(font,
                Component.translatable("jdte.screen.mineral_survey.page", page + 1, pageCount()),
                width / 2, height - 26, 0xB0B0B0);
    }

    private static String formatHeight(int height) {
        return height == Integer.MIN_VALUE || height == Integer.MAX_VALUE ? "?" : Integer.toString(height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}