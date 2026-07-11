package com.jdte.common.jei.gelgenerator;

import com.jdte.JDTE;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GelGeneratorRecipeCategory implements IRecipeCategory<GelGeneratorJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "gel_generator");
    public static final RecipeType<GelGeneratorJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, GelGeneratorJeiRecipe.class);

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDT_POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");

    private static final int GUI_Y_OFFSET = 26;
    private static final int WIDTH = 142;
    private static final int HEIGHT = 84;
    private static final int SLOT_SIZE = 18;
    private static final int FLUID_INNER_WIDTH = 16;
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int ITEM_RECIPE_X_SHIFT = -18;
    private static final int ENERGY_BAR_X_OFFSET_FROM_OUTPUT = 18;

    private final IDrawable icon;
    private final IDrawable fluidBarBackground;
    private final IDrawable fluidBarOverlay;

    public GelGeneratorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(com.jdte.setup.JDTEBlocks.ADVANCED_GEL_GENERATOR.get()));
        this.fluidBarBackground = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 0, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
        this.fluidBarOverlay = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 18, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
    }

    @Override
    public RecipeType<GelGeneratorJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.jdte.advanced_gel_generator");
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
    public void setRecipe(IRecipeLayoutBuilder builder, GelGeneratorJeiRecipe recipe, IFocusGroup focuses) {
        GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();

        builder.addSlot(RecipeIngredientRole.CATALYST, ingredientX(config.getGelGenGelX()), ingredientY(config.getGelGenGelY()))
                .addRichTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable("jei.jdte.not_consumed").withStyle(ChatFormatting.GRAY)))
                .addItemStack(recipe.gelStack());
        builder.addSlot(RecipeIngredientRole.INPUT, ingredientX(config.getGelGenFoodX()), ingredientY(config.getGelGenFoodY()))
                .addItemStacks(recipe.foodStacks());

        if (recipe.hasItemConversion()) {
            builder.addSlot(RecipeIngredientRole.INPUT, ingredientX(config.getGelGenInputStartX()), ingredientY(config.getGelGenInputStartY()))
                    .addItemStack(recipe.inputStack());
            builder.addSlot(RecipeIngredientRole.OUTPUT, ingredientX(itemOutputX(config)), ingredientY(config.getGelGenOutputStartY()))
                    .addItemStack(recipe.outputStack());
        }

        if (recipe.hasFluidConversion()) {
            builder.addSlot(RecipeIngredientRole.INPUT, fluidX(config.getGelGenInputStartX()), fluidY(config.getGelGenInputStartY()))
                    .setBackground(fluidBarBackground, -1, -1)
                    .setOverlay(fluidBarOverlay, -1, -1)
                    .setFluidRenderer(recipe.fluidAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                    .addFluidStack(recipe.inputFluid(), recipe.fluidAmount());
            builder.addSlot(RecipeIngredientRole.OUTPUT, fluidX(itemOutputX(config)), fluidY(config.getGelGenOutputStartY()))
                    .setBackground(fluidBarBackground, -1, -1)
                    .setOverlay(fluidBarOverlay, -1, -1)
                    .setFluidRenderer(recipe.fluidAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                    .addFluidStack(recipe.outputFluid(), recipe.fluidAmount());
        }
    }

    @Override
    public ResourceLocation getRegistryName(GelGeneratorJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void draw(GelGeneratorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawPanel(guiGraphics);
        drawSlotBorders(guiGraphics, recipe);
        drawProgressArrow(guiGraphics, recipe);
        if (recipe.hasItemConversion() || recipe.hasFluidConversion()) {
            drawEnergyBar(guiGraphics, recipe, mouseX, mouseY);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);
    }

    private void drawSlotBorders(GuiGraphics guiGraphics, GelGeneratorJeiRecipe recipe) {
        GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();
        drawSlot(guiGraphics, config.getGelGenGelX(), config.getGelGenGelY());
        drawSlot(guiGraphics, config.getGelGenFoodX(), config.getGelGenFoodY());

        if (recipe.hasItemConversion()) {
            drawItemColumnSlots(guiGraphics, config.getGelGenInputStartX(), config.getGelGenInputStartY(),
                    config.getGelGenInputSpacing(), config.getGelGenInputCount());
            drawItemColumnSlots(guiGraphics, itemOutputX(config), config.getGelGenOutputStartY(),
                    config.getGelGenOutputSpacing(), config.getGelGenOutputCount());
        }
    }

    private void drawItemColumnSlots(GuiGraphics guiGraphics, int startX, int startY, int spacing, int count) {
        for (int i = 0; i < count; i++) {
            drawSlot(guiGraphics, startX, startY + i * spacing);
        }
    }

    private void drawSlot(GuiGraphics guiGraphics, int machineX, int machineY) {
        guiGraphics.blitSprite(SLOT_SPRITE, slotX(machineX), slotY(machineY), SLOT_SIZE, SLOT_SIZE);
    }

    private void drawProgressArrow(GuiGraphics guiGraphics, GelGeneratorJeiRecipe recipe) {
        GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();
        int x = slotX(config.getGelGenProgressArrowX() + (recipe.hasItemConversion() || recipe.hasFluidConversion() ? ITEM_RECIPE_X_SHIFT : 0));
        int y = slotY(config.getGelGenProgressArrowY());
        int progressWidth = (int) ((System.currentTimeMillis() / 30) % 24);

        drawArrow(guiGraphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(guiGraphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrow(guiGraphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF3DBB57);
        }
    }

    private void drawEnergyBar(GuiGraphics guiGraphics, GelGeneratorJeiRecipe recipe, double mouseX, double mouseY) {
        GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();
        int x = slotX(energyBarX(config));
        int y = slotY(config.getGelGenOutputStartY());
        int fillHeight = 1 + (int) ((System.currentTimeMillis() / 35) % 70);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(JDT_POWER_BAR, x, y, 0, 0, 18, 72, 36, 72);
        guiGraphics.blit(JDT_POWER_BAR, x + 1, y + 70 - fillHeight, 19, 70 - fillHeight, 16, fillHeight, 36, 72);

        if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 72) {
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

    private static int slotX(int machineX) {
        return machineX;
    }

    private static int itemOutputX(GuiUpgradeLayoutConfig config) {
        return config.getGelGenOutputStartX() + ITEM_RECIPE_X_SHIFT;
    }

    private static int energyBarX(GuiUpgradeLayoutConfig config) {
        return itemOutputX(config) + ENERGY_BAR_X_OFFSET_FROM_OUTPUT;
    }

    private static int slotY(int machineY) {
        return machineY + GUI_Y_OFFSET;
    }

    private static int ingredientX(int machineX) {
        return slotX(machineX) + 1;
    }

    private static int ingredientY(int machineY) {
        return slotY(machineY) + 1;
    }

    private static int fluidX(int machineX) {
        return slotX(machineX) + 1;
    }

    private static int fluidY(int machineY) {
        return slotY(machineY) + 1;
    }
}
