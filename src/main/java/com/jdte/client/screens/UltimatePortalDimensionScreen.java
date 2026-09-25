package com.jdte.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

public final class UltimatePortalDimensionScreen extends Screen {
    private final ItemStack portalGun;
    private final int slotSelected;
    private final List<ResourceKey<Level>> dimensions;
    private final String pendingName;
    private final String pendingX;
    private final String pendingY;
    private final String pendingZ;
    private int page;

    public UltimatePortalDimensionScreen(ItemStack portalGun, int slotSelected,
                                         List<ResourceKey<Level>> dimensions, int selectedDimension,
                                         String pendingName, String pendingX, String pendingY, String pendingZ) {
        super(Component.translatable("screen.jdte.ultimate_portal_gun.dimension_title"));
        this.portalGun = portalGun;
        this.slotSelected = slotSelected;
        this.dimensions = List.copyOf(dimensions);
        this.page = UltimatePortalDimensionPaging.clampPage(
                selectedDimension / UltimatePortalDimensionPaging.ITEMS_PER_PAGE, this.dimensions.size());
        this.pendingName = pendingName;
        this.pendingX = pendingX;
        this.pendingY = pendingY;
        this.pendingZ = pendingZ;
    }

    @Override
    protected void init() {
        super.init();
        int left = width / 2 - 120;
        int top = height / 2 - 92;
        List<ResourceKey<Level>> pageItems = UltimatePortalDimensionPaging.pageItems(dimensions, page);
        for (int index = 0; index < pageItems.size(); index++) {
            ResourceKey<Level> dimension = pageItems.get(index);
            int absoluteIndex = page * UltimatePortalDimensionPaging.ITEMS_PER_PAGE + index;
            addRenderableWidget(new ExtendedButton(left, top + index * 20, 240, 18,
                    Component.literal(dimension.location().toString()),
                    button -> selectDimension(absoluteIndex)));
        }
        int navigationY = top + UltimatePortalDimensionPaging.ITEMS_PER_PAGE * 20 + 8;
        addRenderableWidget(new ExtendedButton(left, navigationY, 70, 18,
                Component.translatable("screen.jdte.ultimate_portal_gun.prev_page"),
                button -> changePage(-1)));
        addRenderableWidget(new ExtendedButton(left + 85, navigationY, 70, 18,
                Component.translatable("screen.jdte.ultimate_portal_gun.back"),
                button -> returnToEditor(null)));
        addRenderableWidget(new ExtendedButton(left + 170, navigationY, 70, 18,
                Component.translatable("screen.jdte.ultimate_portal_gun.next_page"),
                button -> changePage(1)));
    }

    private void changePage(int delta) {
        int nextPage = UltimatePortalDimensionPaging.clampPage(page + delta, dimensions.size());
        if (nextPage != page) {
            page = nextPage;
            clearWidgets();
            init();
        }
    }

    private void selectDimension(int index) {
        if (index < 0 || index >= dimensions.size()) {
            return;
        }
        returnToEditor(dimensions.get(index));
    }

    private void returnToEditor(ResourceKey<Level> selectedDimension) {
        Minecraft.getInstance().setScreen(new UltimatePortalEditMenu(
                portalGun, slotSelected, selectedDimension,
                pendingName, pendingX, pendingY, pendingZ));
    }

    @Override
    public void onClose() {
        returnToEditor(null);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 120;
        int top = height / 2 - 108;
        graphics.drawCenteredString(font, title, width / 2, top, 0xFFFFFF);
        graphics.drawCenteredString(font,
                Component.translatable("screen.jdte.ultimate_portal_gun.page", page + 1,
                        UltimatePortalDimensionPaging.pageCount(dimensions.size())),
                width / 2, top + 160, 0xA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
