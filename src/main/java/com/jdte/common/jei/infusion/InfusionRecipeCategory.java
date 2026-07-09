package com.jdte.common.jei.infusion;

import com.jdte.JDTE;
import com.jdte.setup.JDTEBlocks;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class InfusionRecipeCategory implements IRecipeCategory<InfusionJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "infusion");
    public static final RecipeType<InfusionJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, InfusionJeiRecipe.class);

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDT_POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");

    private static final int WIDTH = 160;
    private static final int HEIGHT = 84;
    private static final int SLOT_SIZE = 18;
    private static final int FLUID_INNER_WIDTH = 16;
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int INPUT_SLOT_X = 16;
    private static final int FLUID_BAR_X = 42;
    private static final int OUTPUT_SLOT_X = 108;
    private static final int ENERGY_BAR_X = 134;
    private static final int ITEM_SLOT_Y = 33;
    private static final int FLUID_BAR_Y = 6;
    private static final int ARROW_X = 72;
    private static final int ARROW_Y = 37;

    private final IDrawable icon;
    private final IDrawable fluidBarBackground;
    private final IDrawable fluidBarOverlay;

    public InfusionRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get()));
        this.fluidBarBackground = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 0, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
        this.fluidBarOverlay = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 18, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
    }

    @Override
    public RecipeType<InfusionJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.jdte.advanced_infusion_machine");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X + 1, ITEM_SLOT_Y + 1)
                .addItemStack(recipe.inputStack());
        builder.addSlot(RecipeIngredientRole.INPUT, FLUID_BAR_X + 1, FLUID_BAR_Y + 1)
                .setBackground(fluidBarBackground, -1, -1)
                .setOverlay(fluidBarOverlay, -1, -1)
                .setFluidRenderer(recipe.fluidStack().getAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                .addFluidStack(recipe.fluidStack().getFluid(), recipe.fluidStack().getAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X + 1, ITEM_SLOT_Y + 1)
                .addItemStack(recipe.outputStack());
    }

    @Override
    public void draw(InfusionJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawPanel(guiGraphics);
        drawSlot(guiGraphics, INPUT_SLOT_X, ITEM_SLOT_Y);
        drawSlot(guiGraphics, OUTPUT_SLOT_X, ITEM_SLOT_Y);
        drawProgressArrow(guiGraphics);
        drawEnergyBar(guiGraphics, recipe, mouseX, mouseY);
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(SLOT_SPRITE, x, y, SLOT_SIZE, SLOT_SIZE);
    }

    private void drawProgressArrow(GuiGraphics guiGraphics) {
        int progressWidth = (int) ((System.currentTimeMillis() / 30) % 24);

        drawArrow(guiGraphics, ARROW_X, ARROW_Y, 24, 0xFF2B2B2B);
        drawArrow(guiGraphics, ARROW_X + 1, ARROW_Y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrow(guiGraphics, ARROW_X + 1, ARROW_Y + 1, Math.min(22, progressWidth), 0xFFDC143C);
        }
    }

    private void drawEnergyBar(GuiGraphics guiGraphics, InfusionJeiRecipe recipe, double mouseX, double mouseY) {
        int fillHeight = 1 + (int) ((System.currentTimeMillis() / 35) % 70);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(JDT_POWER_BAR, ENERGY_BAR_X, FLUID_BAR_Y, 0, 0, 18, 72, 36, 72);
        guiGraphics.blit(JDT_POWER_BAR, ENERGY_BAR_X + 1, FLUID_BAR_Y + 70 - fillHeight, 19, 70 - fillHeight, 16, fillHeight, 36, 72);

        if (mouseX >= ENERGY_BAR_X && mouseX < ENERGY_BAR_X + 18 && mouseY >= FLUID_BAR_Y && mouseY < FLUID_BAR_Y + 72) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal(recipe.energyCost() + " FE"), (int) mouseX, (int) mouseY);
        }
    }

    private void drawArrow(GuiGraphics guiGraphics, int x, int y, int width, int color) {
        int clampedWidth = Math.max(0, Math.min(24, width));
        if (clampedWidth <= 0) return;

        int bodyWidth = Math.min(16, clampedWidth);
        if (bodyWidth > 0) {
            guiGraphics.fill(x, y + 5, x + bodyWidth, y + 10, color);
        }
        int headWidth = clampedWidth - 16;
        if (headWidth <= 0) return;

        drawHeadPart(guiGraphics, x + 16, y + 4, Math.min(headWidth, 2), 7, color);
        if (headWidth > 2) drawHeadPart(guiGraphics, x + 18, y + 3, Math.min(headWidth - 2, 2), 9, color);
        if (headWidth > 4) drawHeadPart(guiGraphics, x + 20, y + 2, Math.min(headWidth - 4, 2), 11, color);
        if (headWidth > 6) drawHeadPart(guiGraphics, x + 22, y + 1, Math.min(headWidth - 6, 2), 13, color);
    }

    private void drawHeadPart(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        if (width > 0) {
            guiGraphics.fill(x, y, x + width, y + height, color);
        }
    }
}
