package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.screens.util.GreenhouseSlotCountRenderer;
import com.jdte.common.blockentities.CreativeGreenhouseBE;
import com.jdte.common.containers.CreativeGreenhouseContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Cosmetic-only view of server-synchronised Creative Greenhouse catalog slots. */
public class CreativeGreenhouseScreen extends BaseMachineScreen<CreativeGreenhouseContainer> {
    private static final String LONG_MAX_LABEL = "9.22E";
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final Component SEED_TOOLTIP = Component.translatable("jdte.slot.greenhouse_seed");
    private static final Component OUTPUT_TOOLTIP = Component.translatable("jdte.slot.greenhouse_output");
    private final CreativeGreenhouseContainer greenhouseContainer;

    public CreativeGreenhouseScreen(CreativeGreenhouseContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        greenhouseContainer = container;
    }

    @Override
    public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getLootFabricatorExtraWidth();
        extraHeight = layout.getLootFabricatorExtraHeight();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderMachineSlotBackgrounds(graphics);
        renderOutputPage(graphics);
        if (greenhouseContainer.hasCatalogOverflow()) {
            graphics.drawString(font, Component.translatable("jdte.screen.creative_greenhouse.overflow",
                    greenhouseContainer.getActiveOutputLimit()),
                    getGuiLeft() + 8, getGuiTop() + 6, 0xAA3333, false);
        }
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, String countLabel) {
        if (greenhouseContainer.isOutputSlot(slot) && !stack.isEmpty()) {
            GreenhouseSlotCountRenderer.render(graphics, font, stack, slot, imageWidth, LONG_MAX_LABEL);
            return;
        }
        super.renderSlotContents(graphics, stack, slot, countLabel);
    }

    @Override public void addRedstoneButtons() { }
    @Override public void addTickSpeedButton() { }

    private void renderMachineSlotBackgrounds(GuiGraphics graphics) {
        int machineSlots = CreativeGreenhouseBE.INPUT_SLOTS + greenhouseContainer.getOutputSlotsPerPage();
        for (int i = 0; i < Math.min(machineSlots, container.slots.size()); i++) {
            Slot slot = container.slots.get(i);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderOutputPage(GuiGraphics graphics) {
        if (greenhouseContainer.getMaxOutputPage() <= 0) return;
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
        graphics.blit(PREV, getGuiLeft() + layout.getLootFabricatorOutputPrevX(),
                getGuiTop() + layout.getLootFabricatorOutputPrevY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
        graphics.blit(NEXT, getGuiLeft() + layout.getLootFabricatorOutputNextX(),
                getGuiTop() + layout.getLootFabricatorOutputNextY(), 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
        graphics.drawString(font, (greenhouseContainer.getOutputPage() + 1) + "/"
                        + (greenhouseContainer.getMaxOutputPage() + 1),
                getGuiLeft() + layout.getLootFabricatorOutputPageTextX(),
                getGuiTop() + layout.getLootFabricatorOutputPageTextY(), 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && greenhouseContainer.getMaxOutputPage() > 0) {
            var layout = GuiUpgradeLayoutConfig.getInstance();
            int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
            int page = greenhouseContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputPrevX(),
                    getGuiTop() + layout.getLootFabricatorOutputPrevY(), buttonSize, buttonSize, mouseX, mouseY)) {
                page--;
            } else if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputNextX(),
                    getGuiTop() + layout.getLootFabricatorOutputNextY(), buttonSize, buttonSize, mouseX, mouseY)) {
                page++;
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            greenhouseContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new FilterPagePayload(greenhouseContainer.getOutputPage()));
            if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && greenhouseContainer.isOutputSlot(hoveredSlot) && hoveredSlot.hasItem()) {
            ItemStack output = hoveredSlot.getItem();
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(output));
            tooltip.add(Component.translatable("jdte.screen.creative_greenhouse.infinite", Long.toString(Long.MAX_VALUE)));
            if (greenhouseContainer.hasCatalogOverflow()) {
                tooltip.add(Component.translatable("jdte.screen.creative_greenhouse.overflow",
                        greenhouseContainer.getActiveOutputLimit()));
            }
            graphics.renderTooltip(font, tooltip, output.getTooltipImage(), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            graphics.renderTooltip(font, greenhouseContainer.isPlantTemplateSlot(hoveredSlot) ? SEED_TOOLTIP : OUTPUT_TOOLTIP,
                    mouseX, mouseY);
        }
    }
}
