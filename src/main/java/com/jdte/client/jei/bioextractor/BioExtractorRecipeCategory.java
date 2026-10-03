package com.jdte.client.jei.bioextractor;

import com.jdte.JDTE;
import com.jdte.common.recipes.BioFactoryOutput;
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
import net.neoforged.neoforge.fluids.FluidStack;

public class BioExtractorRecipeCategory implements IRecipeCategory<BioExtractorJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "bio_extractor");
    public static final RecipeType<BioExtractorJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, BioExtractorJeiRecipe.class);

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluid_bar.png");

    private static final int WIDTH = 168;
    private static final int HEIGHT = 70;
    private static final int FLUID_INNER_WIDTH = 16;
    private static final int FLUID_INNER_HEIGHT = 50;

    private static final int ENTITY_SLOT_X = 14;
    private static final int ENTITY_SLOT_Y = 24;
    private static final int ARROW_X = 42;
    private static final int ARROW_Y = 27;
    private static final int ITEM_OUTPUT_X = 74;
    private static final int ITEM_OUTPUT_Y = 24;
    private static final int FLUID_TANK_X = 140;
    private static final int FLUID_TANK_Y = 8;

    private final IDrawable icon;
    private final IDrawable fluidBarBackground;
    private final IDrawable fluidBarOverlay;

    public BioExtractorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get()));
        this.fluidBarBackground = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 0, 0, 18, 52)
                .setTextureSize(36, 72)
                .build();
        this.fluidBarOverlay = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 18, 0, 18, 52)
                .setTextureSize(36, 72)
                .build();
    }

    @Override
    public RecipeType<BioExtractorJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.jdte.advanced_bio_extractor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BioExtractorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, ENTITY_SLOT_X + 1, ENTITY_SLOT_Y + 1)
                .addItemStack(recipe.entityIcon());

        int itemIndex = 0;
        for (BioFactoryOutput output : recipe.outputItems()) {
            if (itemIndex >= 3) break;
            builder.addSlot(RecipeIngredientRole.OUTPUT, ITEM_OUTPUT_X + itemIndex * 20 + 1, ITEM_OUTPUT_Y + 1)
                    .addItemStack(output.stack());
            itemIndex++;
        }

        if (recipe.outputFluid().isPresent()) {
            FluidStack fluid = recipe.outputFluid().get();
            builder.addSlot(RecipeIngredientRole.OUTPUT, FLUID_TANK_X + 1, FLUID_TANK_Y + 1)
                    .setBackground(fluidBarBackground, -1, -1)
                    .setOverlay(fluidBarOverlay, -1, -1)
                    .setFluidRenderer(fluid.getAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount());
        }
    }

    @Override
    public ResourceLocation getRegistryName(BioExtractorJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void draw(BioExtractorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);

        guiGraphics.blitSprite(SLOT_SPRITE, ENTITY_SLOT_X, ENTITY_SLOT_Y, 18, 18);

        for (int i = 0; i < Math.min(3, Math.max(1, recipe.outputItems().size())); i++) {
            guiGraphics.blitSprite(SLOT_SPRITE, ITEM_OUTPUT_X + i * 20, ITEM_OUTPUT_Y, 18, 18);
        }

        guiGraphics.fill(ARROW_X, ARROW_Y + 5, ARROW_X + 20, ARROW_Y + 9, 0xFF4A90D9);

        String entityTitle = recipe.entityType().getPath();
        guiGraphics.drawString(Minecraft.getInstance().font, entityTitle, ENTITY_SLOT_X, 8, 0xFF444444, false);

        String info = (recipe.cooldown() / 20) + "s | " + recipe.energy() + " FE";
        guiGraphics.drawString(Minecraft.getInstance().font, info, 42, 54, 0xFF777777, false);
    }
}
