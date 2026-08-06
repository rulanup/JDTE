package com.jdte.client.screens;

import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public final class MineralSurveyScreen extends Screen {
    private static final int ENTRIES_PER_PAGE = 8;
    private static final int PAPER_WIDTH = 430;
    private static final int PAPER_HEIGHT = 252;
    private static final int PAPER_COLOR = 0xFFF0E2BD;
    private static final int PAPER_SHADOW = 0xA0000000;
    private static final int INK = 0xFF30271D;
    private static final int MUTED_INK = 0xFF75664F;
    private static final int RULE = 0xFF8A7657;
    private static final int NAV_WIDTH = 48;
    private static final int NAV_HEIGHT = 17;
    private final MineralSurveyData survey;
    private int page;

    public MineralSurveyScreen(MineralSurveyData survey) {
        super(Component.translatable("jdte.screen.mineral_survey.title"));
        this.survey = survey;
    }

    private void changePage(int delta) {
        int nextPage = Math.max(0, Math.min(page + delta, pageCount() - 1));
        if (nextPage == page) return;
        page = nextPage;
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private int pageCount() {
        return Math.max(1, (survey.entries().size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int paperX = paperLeft();
        int paperY = paperTop();
        int paperWidth = paperWidth();
        int paperHeight = paperHeight();
        drawPaper(graphics, paperX, paperY, paperWidth, paperHeight);
        drawMasthead(graphics, paperX, paperY, paperWidth);
        drawEntries(graphics, paperX, paperY, paperWidth);
        drawNavigation(graphics, mouseX, mouseY, paperX, paperY, paperWidth, paperHeight);
        graphics.drawCenteredString(font,
                Component.translatable("jdte.screen.mineral_survey.page", page + 1, pageCount()),
                width / 2, paperY + paperHeight - 21, MUTED_INK);
    }

    private void drawPaper(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x + 5, y + 6, x + width + 5, y + height + 6, PAPER_SHADOW);
        graphics.fill(x, y, x + width, y + height, PAPER_COLOR);
        graphics.fill(x, y, x + width, y + 2, RULE);
        graphics.fill(x, y + height - 2, x + width, y + height, RULE);
        graphics.fill(x, y, x + 2, y + height, RULE);
        graphics.fill(x + width - 2, y, x + width, y + height, RULE);
        for (int lineY = y + 5; lineY < y + height - 4; lineY += 7) {
            graphics.fill(x + 4, lineY, x + width - 4, lineY + 1, 0x0C5A4932);
        }
    }

    private void drawMasthead(GuiGraphics graphics, int x, int y, int width) {
        drawCenteredCrisp(graphics, title, x + width / 2, y + 11, INK);
        graphics.fill(x + width / 2 - 58, y + 24, x + width / 2 + 58, y + 25, RULE);

        drawCenteredCrisp(graphics,
                Component.translatable("jdte.screen.mineral_survey.edition", page + 1),
                x + width / 2, y + 29, MUTED_INK);
        graphics.fill(x + 12, y + 42, x + width - 12, y + 44, INK);
        graphics.drawCenteredString(font,
                Component.translatable("jdte.screen.mineral_survey.source",
                        survey.biomeId().toString(), survey.dimensionId().toString()),
                x + width / 2, y + 48, INK);
        graphics.drawCenteredString(font,
                Component.translatable("jdte.screen.mineral_survey.estimated"),
                x + width / 2, y + 59, MUTED_INK);
        graphics.fill(x + 12, y + 70, x + width - 12, y + 71, RULE);
    }

    private void drawEntries(GuiGraphics graphics, int x, int y, int width) {
        long totalWeight = survey.totalWeight();
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, survey.entries().size());
        int gap = 12;
        int columnWidth = (width - 36 - gap) / 2;
        int rowHeight = 39;
        for (int index = start; index < end; index++) {
            MineralEntry entry = survey.entries().get(index);
            int pageIndex = index - start;
            int column = pageIndex % 2;
            int row = pageIndex / 2;
            int entryX = x + 14 + column * (columnWidth + gap);
            int entryY = y + 77 + row * rowHeight;
            drawEntry(graphics, entry, totalWeight, entryX, entryY, columnWidth);
        }
        graphics.drawString(font,
                Component.translatable("jdte.screen.mineral_survey.archive",
                        survey.schemaVersion(), survey.entries().size()),
                x + 12, y + paperHeight() - 36, MUTED_INK, false);
    }

    private void drawEntry(GuiGraphics graphics, MineralEntry entry, long totalWeight,
                           int x, int y, int width) {
        ItemStack icon = BuiltInRegistries.BLOCK.getOptional(entry.oreId())
                .map(block -> new ItemStack(block.asItem()))
                .orElse(ItemStack.EMPTY);
        if (!icon.isEmpty()) graphics.renderItem(icon, x, y + 2);
        graphics.drawString(font, MineralSurveyItem.displayName(entry), x + 21, y, INK, false);
        String percent = MineralSurveyItem.formatPercent(entry.weight(), totalWeight);
        graphics.drawString(font, percent, x + width - font.width(percent), y, INK, false);
        graphics.fill(x + 21, y + 11, x + width, y + 12, RULE);

        Component details = Component.translatable("jdte.screen.mineral_survey.details",
                formatHeight(entry.minY()), formatHeight(entry.maxY()), entry.veinSize(),
                Component.translatable("jdte.mineral.confidence."
                        + entry.confidence().name().toLowerCase(Locale.ROOT)));
        var detailLines = font.split(details, Math.max(40, width - 21));
        for (int line = 0; line < Math.min(2, detailLines.size()); line++) {
            graphics.drawString(font, detailLines.get(line), x + 21, y + 15 + line * 9, MUTED_INK, false);
        }
    }

    private void drawNavigation(GuiGraphics graphics, int mouseX, int mouseY,
                                int paperX, int paperY, int paperWidth, int paperHeight) {
        int centerX = paperX + paperWidth / 2;
        int buttonY = paperY + paperHeight - 26;
        drawNavigationButton(graphics, centerX - 86, buttonY, true, page > 0,
                inBounds(centerX - 86, buttonY, NAV_WIDTH, NAV_HEIGHT, mouseX, mouseY));
        drawNavigationButton(graphics, centerX + 38, buttonY, false, page + 1 < pageCount(),
                inBounds(centerX + 38, buttonY, NAV_WIDTH, NAV_HEIGHT, mouseX, mouseY));
    }

    private void drawNavigationButton(GuiGraphics graphics, int x, int y, boolean previous,
                                      boolean active, boolean hovered) {
        int border = active ? INK : 0xFF9B8E76;
        int fill = !active ? 0xFFDACBA7 : hovered ? 0xFFE6D39F : 0xFFD2BD88;
        graphics.fill(x, y, x + NAV_WIDTH, y + NAV_HEIGHT, border);
        graphics.fill(x + 1, y + 1, x + NAV_WIDTH - 1, y + NAV_HEIGHT - 1, fill);
        graphics.fill(x + 3, y + 3, x + NAV_WIDTH - 3, y + 4, 0x508A7657);
        String arrow = previous ? "◀" : "▶";
        graphics.drawString(font, arrow, x + (NAV_WIDTH - font.width(arrow)) / 2, y + 4,
                active ? INK : MUTED_INK, false);
    }

    private void drawCenteredCrisp(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = paperLeft() + paperWidth() / 2;
            int buttonY = paperTop() + paperHeight() - 26;
            if (page > 0 && inBounds(centerX - 86, buttonY, NAV_WIDTH, NAV_HEIGHT, mouseX, mouseY)) {
                changePage(-1);
                return true;
            }
            if (page + 1 < pageCount()
                    && inBounds(centerX + 38, buttonY, NAV_WIDTH, NAV_HEIGHT, mouseX, mouseY)) {
                changePage(1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inBounds(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int paperWidth() {
        return Math.min(PAPER_WIDTH, Math.max(280, width - 24));
    }

    private int paperHeight() {
        return Math.min(PAPER_HEIGHT, Math.max(210, height - 24));
    }

    private int paperLeft() {
        return (width - paperWidth()) / 2;
    }

    private int paperTop() {
        return (height - paperHeight()) / 2;
    }

    private static String formatHeight(int height) {
        return height == Integer.MIN_VALUE || height == Integer.MAX_VALUE ? "?" : Integer.toString(height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}