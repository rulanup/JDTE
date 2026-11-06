package com.jdte.client.screens;

import com.jdte.common.containers.CraftingPatternContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CraftingPatternScreen extends AbstractContainerScreen<CraftingPatternContainer> {
    private Button save;

    public CraftingPatternScreen(CraftingPatternContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 196;
        inventoryLabelY = 102;
    }

    @Override
    protected void init() {
        super.init();
        save = addRenderableWidget(Button.builder(Component.translatable("jdte.screen.crafting_pattern.save"),
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0))
                .bounds(leftPos + 100, topPos + 79, 66, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("jdte.screen.crafting_pattern.clear"),
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1))
                .bounds(leftPos + 20, topPos + 79, 66, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF373737);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFFC6C6C6);
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF373737);
            graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0xFF8B8B8B);
        }
        graphics.drawString(font, "→", leftPos + 100, topPos + 43, 0x404040, false);
        graphics.drawCenteredString(font, Component.translatable(menu.hasValidRecipe()
                ? "jdte.screen.crafting_pattern.valid" : "jdte.screen.crafting_pattern.invalid"),
                leftPos + 134, topPos + 61, menu.hasValidRecipe() ? 0x225522 : 0x883333);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        save.active = menu.hasValidRecipe();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
