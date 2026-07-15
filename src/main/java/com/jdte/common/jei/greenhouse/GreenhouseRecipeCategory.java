package com.jdte.common.jei.greenhouse;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.JDTE;
import com.jdte.setup.JDTEBlocks;
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

public class GreenhouseRecipeCategory implements IRecipeCategory<GreenhouseJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "greenhouse");
    public static final RecipeType<GreenhouseJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, GreenhouseJeiRecipe.class);
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation FLUID_BAR = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/fluidbar.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 84;
    private static final int INPUT_X = 10;
    private static final int INPUT_Y = 33;
    private static final int FLUID_X = 38;
    private static final int FLUID_Y = 6;
    private static final int OUTPUT_X = 80;
    private static final int OUTPUT_Y = 6;
    private static final int ENERGY_X = 156;
    private final IDrawable icon;
    private final IDrawable fluidBackground;
    private final IDrawable fluidOverlay;

    public GreenhouseRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(JDTEBlocks.GREENHOUSE.get()));
        fluidBackground = helper.drawableBuilder(FLUID_BAR, 0, 0, 18, 72).setTextureSize(36, 72).build();
        fluidOverlay = helper.drawableBuilder(FLUID_BAR, 18, 0, 18, 72).setTextureSize(36, 72).build();
    }

    @Override public RecipeType<GreenhouseJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.jdte.greenhouse"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GreenhouseJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, INPUT_X + 1, INPUT_Y + 1).addItemStack(recipe.seed());
        builder.addSlot(RecipeIngredientRole.INPUT, FLUID_X + 1, FLUID_Y + 1)
                .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                .setFluidRenderer(Math.max(1, recipe.timeFluid()), false, 16, 70)
                .addFluidStack(Registration.TIME_FLUID_SOURCE.get(), recipe.timeFluid());
        for (int i = 0; i < Math.min(16, recipe.outputs().size()); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + (i % 4) * 18 + 1,
                    OUTPUT_Y + (i / 4) * 18 + 1).addItemStack(recipe.outputs().get(i));
        }
    }

    @Override public ResourceLocation getRegistryName(GreenhouseJeiRecipe recipe) { return recipe.id(); }

    @Override
    public void draw(GreenhouseJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.blitSprite(BACKGROUND, 0, 0, WIDTH, HEIGHT);
        graphics.blitSprite(SLOT, INPUT_X, INPUT_Y, 18, 18);
        for (int i = 0; i < 16; i++) {
            graphics.blitSprite(SLOT, OUTPUT_X + (i % 4) * 18, OUTPUT_Y + (i / 4) * 18, 18, 18);
        }
        drawPlantProgress(graphics, 64, 31);
        int fill = 1 + (int) ((System.currentTimeMillis() / 35) % 70);
        graphics.blit(POWER_BAR, ENERGY_X, FLUID_Y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWER_BAR, ENERGY_X + 1, FLUID_Y + 70 - fill, 19, 70 - fill, 16, fill, 36, 72);
        if (mouseX >= ENERGY_X && mouseX < ENERGY_X + 18 && mouseY >= FLUID_Y && mouseY < FLUID_Y + 72) {
            graphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal(recipe.energy() + " FE"), (int) mouseX, (int) mouseY);
        }
    }

    private void drawPlantProgress(GuiGraphics graphics, int x, int y) {
        int stage = (int) ((System.currentTimeMillis() / 250) % 6);
        graphics.fill(x, y + 10, x + 12, y + 14, 0xFF38271E);
        graphics.fill(x + 1, y + 10, x + 11, y + 12, 0xFF6A4930);
        if (stage >= 1) graphics.fill(x + 5, y + 7, x + 7, y + 11, 0xFF4D8A42);
        if (stage >= 2) graphics.fill(x + 2, y + 7, x + 6, y + 9, 0xFF68A84F);
        if (stage >= 3) graphics.fill(x + 6, y + 4, x + 8, y + 11, 0xFF4D8A42);
        if (stage >= 4) graphics.fill(x + 7, y + 4, x + 11, y + 7, 0xFF79B957);
        if (stage >= 5) graphics.fill(x + 3, y + 2, x + 7, y + 5, 0xFF8BC55D);
    }
}
