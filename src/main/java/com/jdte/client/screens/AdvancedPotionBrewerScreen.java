package com.jdte.client.screens;

import net.minecraft.client.input.MouseButtonEvent;
import com.jdte.client.screens.util.MachineFluidBarRenderer;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.PotionBrewerRecipeLockClientCache;
import com.jdte.client.screens.util.MachineBarMath;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.containers.AdvancedPotionBrewerContainer;
import com.jdte.common.network.data.PotionBrewerFuelInputPayload;
import com.jdte.common.network.data.PotionBrewerRecipeLockPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedPotionBrewerScreen extends BaseMachineScreen<AdvancedPotionBrewerContainer> {
    private static final Identifier BREWING_STAND_BG = Identifier.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final Identifier FUEL_LENGTH_SPRITE = Identifier.withDefaultNamespace("container/brewing_stand/fuel_length");
    private static final Identifier BREW_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final Identifier BUBBLES_SPRITE = Identifier.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final Identifier RECIPE_LOCK_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/waterbreathing.png");
    private static final Identifier FUEL_INPUT_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/item/abilityupgrades/blank.png");
    private static final Component SLOT_LOCK_ON = Component.translatable("jdte.screen.slot_lock.on");
    private static final Component SLOT_LOCK_OFF = Component.translatable("jdte.screen.slot_lock.off");
    private static final int VANILLA_BOTTLE_GHOST_SRC_X = 57;
    private static final int VANILLA_BOTTLE_GHOST_SRC_Y = 52;
    private static final int VANILLA_BOTTLE_GHOST_SIZE = 16;
    private static final int FUEL_BAR_WIDTH = 18;
    private static final int FUEL_BAR_HEIGHT = 4;
    private static final int BREW_PROGRESS_WIDTH = 9;
    private static final int BREW_PROGRESS_HEIGHT = 28;
    private static final int BUBBLES_WIDTH = 12;
    private static final int BUBBLES_HEIGHT = 29;
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int[] BUBBLE_LENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public AdvancedPotionBrewerScreen(AdvancedPotionBrewerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public void init() {
        super.init();
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(new RecipeLockWidget(
                getGuiLeft() + layout.getPotionBrewerRecipeLockButtonX(),
                getGuiTop() + layout.getPotionBrewerRecipeLockButtonY()));
        addRenderableWidget(new FuelInputWidget(
                getGuiLeft() + layout.getPotionBrewerFuelInputButtonX(),
                getGuiTop() + layout.getPotionBrewerFuelInputButtonY()));
        ClientPacketDistributor.sendToServer(new PotionBrewerRecipeLockPayload(brewerContainer().getBlockPos(), false, true));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = getGuiLeft();
        int y = getGuiTop();
        var layout = GuiUpgradeLayoutConfig.getInstance();

        AdvancedPotionBrewerContainer brewerContainer = (AdvancedPotionBrewerContainer) container;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BREWING_STAND_BG,
                x + layout.getPotionBrewerBgX(),
                y + layout.getPotionBrewerBgY(),
                layout.getPotionBrewerBgSrcX(),
                layout.getPotionBrewerBgSrcY(),
                layout.getPotionBrewerBgWidth(),
                layout.getPotionBrewerBgHeight(), 256, 256);

        renderOutputBottleGhosts(guiGraphics, x, y, layout);
        renderLockedRecipeGhosts(guiGraphics);
        renderPotionBrewerFluidTanks(guiGraphics, x, y, brewerContainer, layout);
        renderFuelBar(guiGraphics, x, y, brewerContainer, layout);
        renderBrewBubbles(guiGraphics, x, y, brewerContainer, layout);
        renderBrewArrow(guiGraphics, x, y, brewerContainer, layout);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        renderPotionBrewerFluidTooltips(guiGraphics, mouseX, mouseY);
        renderPotionBrewerSlotTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(
                getGuiLeft() + layout.getPotionBrewerSpeedButtonX(),
                getGuiTop() + layout.getPotionBrewerSpeedButtonY(),
                tickSpeed, b -> {
                    tickSpeed = ((NumberButton) b).getValue();
                    ClientPacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
                }));
    }

    @Override
    public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + layout.getPotionBrewerRedstoneButtonX(),
                getGuiTop() + layout.getPotionBrewerRedstoneButtonY(),
                redstoneMode.ordinal(), b -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    private void renderOutputBottleGhosts(GuiGraphicsExtractor guiGraphics, int x, int y, GuiUpgradeLayoutConfig layout) {
        for (int i = 0; i < AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT; i++) {
            if (container.slots.get(AdvancedPotionBrewerBE.OUTPUT_SLOT_START + i).hasItem()) {
                continue;
            }
            int slotX = x + layout.getPotionBrewerOutputStartX();
            int slotY = y + layout.getPotionBrewerOutputStartY() + i * layout.getPotionBrewerOutputSpacing();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BREWING_STAND_BG,
                    slotX + 1,
                    slotY + 1,
                    VANILLA_BOTTLE_GHOST_SRC_X,
                    VANILLA_BOTTLE_GHOST_SRC_Y,
                    VANILLA_BOTTLE_GHOST_SIZE,
                    VANILLA_BOTTLE_GHOST_SIZE, 256, 256);
        }
    }

    private void renderLockedRecipeGhosts(GuiGraphicsExtractor guiGraphics) {
        if (!isRecipeLocked()) {
            return;
        }
        for (int i = 0; i < AdvancedPotionBrewerBE.TOTAL_SLOTS && i < container.slots.size(); i++) {
            if (!shouldRenderRecipeLockGhost(i)) {
                continue;
            }
            Slot slot = container.slots.get(i);
            if (slot.hasItem()) {
                continue;
            }
            ItemStack template = PotionBrewerRecipeLockClientCache.getTemplate(brewerContainer().getBlockPos(), i);
            if (template.isEmpty()) {
                continue;
            }
            GhostItemRenderer.draw(guiGraphics, template, getGuiLeft() + slot.x, getGuiTop() + slot.y, 0x99707070);
        }
    }

    private boolean shouldRenderRecipeLockGhost(int slot) {
        return slot == AdvancedPotionBrewerBE.INGREDIENT_SLOT
                || (slot >= AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START
                && slot < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT);
    }

    private void renderPotionBrewerSlotTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (!(hoveredSlot instanceof ResourceHandlerSlot machineSlot) || machineSlot.getResourceHandler() != container.machineHandler) {
            return;
        }
        int slot = machineSlot.getSlotIndex();
        Component slotLabel = getPotionBrewerSlotLabel(slot);
        if (slotLabel == null || hoveredSlot.hasItem()) {
            return;
        }

        List<FormattedText> lines = new ArrayList<>();
        lines.add(slotLabel);
        ItemStack lockedTemplate = getLockedTemplateForTooltip(slot);
        if (!lockedTemplate.isEmpty()) {
            lines.add(Component.translatable("jdte.screen.slot_locked_item", lockedTemplate.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        guiGraphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(lines), mouseX, mouseY);
    }

    private Component getPotionBrewerSlotLabel(int slot) {
        if (slot >= AdvancedPotionBrewerBE.BOTTLE_SLOT_0 && slot <= AdvancedPotionBrewerBE.BOTTLE_SLOT_2) {
            return Component.translatable("jdte.screen.potion_brewer.bottle_slot", slot - AdvancedPotionBrewerBE.BOTTLE_SLOT_0 + 1);
        }
        if (slot == AdvancedPotionBrewerBE.INGREDIENT_SLOT) {
            return Component.translatable("jdte.screen.potion_brewer.ingredient_slot", 1);
        }
        if (slot == AdvancedPotionBrewerBE.FUEL_SLOT) {
            return Component.translatable("jdte.screen.potion_brewer.fuel_slot");
        }
        if (slot >= AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START
                && slot < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT) {
            return Component.translatable("jdte.screen.potion_brewer.ingredient_slot", slot - AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 2);
        }
        if (slot >= AdvancedPotionBrewerBE.OUTPUT_SLOT_START
                && slot < AdvancedPotionBrewerBE.OUTPUT_SLOT_START + AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT) {
            return Component.translatable("jdte.screen.potion_brewer.output_slot", slot - AdvancedPotionBrewerBE.OUTPUT_SLOT_START + 1);
        }
        return null;
    }

    private ItemStack getLockedTemplateForTooltip(int slot) {
        if (!isRecipeLocked() || !shouldRenderRecipeLockGhost(slot)) {
            return ItemStack.EMPTY;
        }
        return PotionBrewerRecipeLockClientCache.getTemplate(brewerContainer().getBlockPos(), slot);
    }

    private boolean isRecipeLocked() {
        return brewerContainer().isRecipeLocked() || PotionBrewerRecipeLockClientCache.isLocked(brewerContainer().getBlockPos());
    }

    private AdvancedPotionBrewerContainer brewerContainer() {
        return (AdvancedPotionBrewerContainer) container;
    }

    private void renderPotionBrewerFluidTanks(GuiGraphicsExtractor guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int capacity = Math.max(1, brewerContainer.getPotionBrewerFluidCapacity());
        renderFluidTank(guiGraphics,
                x + layout.getPotionBrewerWaterFluidX(),
                y + layout.getPotionBrewerWaterFluidY(),
                brewerContainer.getWaterFluidStack(),
                brewerContainer.getWaterFluidAmount(),
                capacity);
        renderFluidTank(guiGraphics,
                x + layout.getPotionBrewerTimeFluidX(),
                y + layout.getPotionBrewerTimeFluidY(),
                brewerContainer.getTimeFluidStack(),
                brewerContainer.getTimeFluidAmount(),
                capacity);
    }

    private void renderFluidTank(GuiGraphicsExtractor guiGraphics, int x, int y, FluidStack fluidStack, int amount, int capacity) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);

        int fluidHeight = MachineBarMath.scaleClamped(amount, capacity, FLUID_INNER_HEIGHT);
        if (fluidHeight > 0) {
            MachineFluidBarRenderer.renderFluid(guiGraphics, fluidStack, x + 1, y + 71, 16, fluidHeight);
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderPotionBrewerFluidTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        AdvancedPotionBrewerContainer brewerContainer = brewerContainer();
        int capacity = brewerContainer.getPotionBrewerFluidCapacity();

        int waterX = getGuiLeft() + layout.getPotionBrewerWaterFluidX();
        int waterY = getGuiTop() + layout.getPotionBrewerWaterFluidY();
        if (MiscTools.inBounds(waterX, waterY, 18, 72, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, brewerContainer.getWaterFluidStack(), brewerContainer.getWaterFluidAmount(), capacity, mouseX, mouseY);
            return;
        }

        int timeX = getGuiLeft() + layout.getPotionBrewerTimeFluidX();
        int timeY = getGuiTop() + layout.getPotionBrewerTimeFluidY();
        if (MiscTools.inBounds(timeX, timeY, 18, 72, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, brewerContainer.getTimeFluidStack(), brewerContainer.getTimeFluidAmount(), capacity, mouseX, mouseY);
        }
    }

    private void renderFluidTooltip(GuiGraphicsExtractor guiGraphics, FluidStack fluidStack, int amount, int capacity, int mouseX, int mouseY) {
        guiGraphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(amount), MagicHelpers.withSuffix(capacity))
        )), mouseX, mouseY);
    }


    private void renderFuelBar(GuiGraphicsExtractor guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int fuel = brewerContainer.getFuel();
        if (fuel > 0) {
            int fuelWidth = Mth.clamp((FUEL_BAR_WIDTH * fuel + AdvancedPotionBrewerBE.FUEL_PER_BLAZE - 1) / AdvancedPotionBrewerBE.FUEL_PER_BLAZE,
                    0,
                    FUEL_BAR_WIDTH);
            if (fuelWidth > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_LENGTH_SPRITE,
                        FUEL_BAR_WIDTH,
                        FUEL_BAR_HEIGHT,
                        0,
                        0,
                        x + layout.getPotionBrewerFuelBarX(),
                        y + layout.getPotionBrewerFuelBarBottomY() - FUEL_BAR_HEIGHT,
                        fuelWidth,
                        FUEL_BAR_HEIGHT);
            }
        }
    }

    private void renderBrewBubbles(GuiGraphicsExtractor guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int progress = brewerContainer.getBrewProgress();
        int maxProgress = brewerContainer.getBrewProgressMax();
        if (maxProgress > 0 && progress > 0) {
            int remainingTicks = Math.max(0, maxProgress - progress);
            int bubbleHeight = BUBBLE_LENGTHS[remainingTicks / 2 % BUBBLE_LENGTHS.length];
            if (bubbleHeight > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUBBLES_SPRITE,
                        BUBBLES_WIDTH,
                        BUBBLES_HEIGHT,
                        0,
                        BUBBLES_HEIGHT - bubbleHeight,
                        x + layout.getPotionBrewerBubblesX(),
                        y + layout.getPotionBrewerBubblesBottomY() - bubbleHeight,
                        BUBBLES_WIDTH,
                        bubbleHeight);
            }
        }
    }

    private void renderBrewArrow(GuiGraphicsExtractor guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int progress = brewerContainer.getBrewProgress();
        int maxProgress = brewerContainer.getBrewProgressMax();
        if (maxProgress > 0 && progress > 0) {
            int arrowHeight = (progress * BREW_PROGRESS_HEIGHT) / maxProgress;
            if (arrowHeight > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BREW_PROGRESS_SPRITE,
                        BREW_PROGRESS_WIDTH,
                        BREW_PROGRESS_HEIGHT,
                        0,
                        0,
                        x + layout.getPotionBrewerArrowX(),
                        y + layout.getPotionBrewerArrowBottomY() - BREW_PROGRESS_HEIGHT,
                        BREW_PROGRESS_WIDTH,
                        arrowHeight);
            }
        }
    }

    private class RecipeLockWidget extends AbstractWidget {
        private boolean lastTooltipLocked = false;

        private RecipeLockWidget(int x, int y) {
            super(x, y, 16, 16, SLOT_LOCK_OFF);
            setTooltip(Tooltip.create(SLOT_LOCK_OFF));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean locked = isRecipeLocked();
            setMessage(locked ? SLOT_LOCK_ON : SLOT_LOCK_OFF);
            if (locked != lastTooltipLocked) {
                setTooltip(Tooltip.create(locked ? SLOT_LOCK_ON : SLOT_LOCK_OFF));
                lastTooltipLocked = locked;
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RECIPE_LOCK_ICON, getX(), getY(), 0, 0, 16, 16, 16, 16, locked ? -1 : 0xFF545454);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            boolean nextLocked = !isRecipeLocked();
            ClientPacketDistributor.sendToServer(new PotionBrewerRecipeLockPayload(brewerContainer().getBlockPos(), nextLocked, false));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {
            this.defaultButtonNarrationText(narrationOutput);
        }
    }

    private Component getFuelInputTooltip(boolean enabled) {
        return Component.translatable(enabled
                ? "jdte.screen.potion_brewer.fuel_input.on"
                : "jdte.screen.potion_brewer.fuel_input.off");
    }

    private class FuelInputWidget extends AbstractWidget {
        private boolean lastTooltipEnabled;

        private FuelInputWidget(int x, int y) {
            super(x, y, 16, 16, Component.empty());
            lastTooltipEnabled = brewerContainer().isFuelInputEnabled();
            setTooltip(Tooltip.create(getFuelInputTooltip(lastTooltipEnabled)));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean enabled = brewerContainer().isFuelInputEnabled();
            if (enabled != lastTooltipEnabled) {
                setTooltip(Tooltip.create(getFuelInputTooltip(enabled)));
                lastTooltipEnabled = enabled;
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FUEL_INPUT_ICON, getX(), getY(), 0, 0, 16, 16, 16, 16, enabled ? -1 : 0xFF545454);

            var pose = guiGraphics.pose();
            pose.pushMatrix();
            pose.translate(getX() + 4.0F, getY() + 4.0F);
            pose.scale(0.5F, 0.5F);
            guiGraphics.fakeItem(new ItemStack(Items.BLAZE_POWDER), 0, 0);
            pose.popMatrix();
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            ClientPacketDistributor.sendToServer(new PotionBrewerFuelInputPayload(
                    brewerContainer().getBlockPos(), !brewerContainer().isFuelInputEnabled()));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {
            this.defaultButtonNarrationText(narrationOutput);
        }
    }
}
