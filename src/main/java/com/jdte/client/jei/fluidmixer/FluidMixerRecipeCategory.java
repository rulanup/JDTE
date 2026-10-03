package com.jdte.client.jei.fluidmixer;

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

public class FluidMixerRecipeCategory implements IRecipeCategory<FluidMixerJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "fluid_mixer");
    public static final RecipeType<FluidMixerJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, FluidMixerJeiRecipe.class);

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluid_bar.png");
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/mixer_arrow.png");

    private static final int WIDTH = 134;
    private static final int HEIGHT = 84;
    private static final int FLUID_INNER_WIDTH = 16;
    private static final int FLUID_INNER_HEIGHT = 70;

    private static final int TANK_A_X = 8;
    private static final int TANK_A_Y = 6;
    private static final int TANK_B_X = 32;
    private static final int TANK_B_Y = 6;
    private static final int ITEM_SLOT_X = 70;
    private static final int ITEM_SLOT_Y = 15;
    private static final int ARROW_X = 67;
    private static final int ARROW_Y = 43;
    private static final int OUTPUT_TANK_X = 106;
    private static final int OUTPUT_TANK_Y = 6;

    private final IDrawable icon;
    private final IDrawable fluidBarBackground;
    private final IDrawable fluidBarOverlay;

    public FluidMixerRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JDTEBlocks.ADVANCED_FLUID_MIXER.get()));
        this.fluidBarBackground = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 0, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
        this.fluidBarOverlay = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 18, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
    }

    @Override
    public RecipeType<FluidMixerJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.jdte.advanced_fluid_mixer");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FluidMixerJeiRecipe recipe, IFocusGroup focuses) {
        // 输入流体 A
        builder.addSlot(RecipeIngredientRole.INPUT, TANK_A_X + 1, TANK_A_Y + 1)
                .setBackground(fluidBarBackground, -1, -1)
                .setOverlay(fluidBarOverlay, -1, -1)
                .setFluidRenderer(recipe.fluidInputA().getAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                .addFluidStack(recipe.fluidInputA().getFluid(), recipe.fluidInputA().getAmount());

        // 可选物品催化剂
        if (!recipe.itemInputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, ITEM_SLOT_X + 1, ITEM_SLOT_Y + 1)
                    .addItemStacks(recipe.itemInputs());
        }

        // 输入流体 B
        builder.addSlot(RecipeIngredientRole.INPUT, TANK_B_X + 1, TANK_B_Y + 1)
                .setBackground(fluidBarBackground, -1, -1)
                .setOverlay(fluidBarOverlay, -1, -1)
                .setFluidRenderer(recipe.fluidInputB().getAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                .addFluidStack(recipe.fluidInputB().getFluid(), recipe.fluidInputB().getAmount());

        // 输出流体
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_TANK_X + 1, OUTPUT_TANK_Y + 1)
                .setBackground(fluidBarBackground, -1, -1)
                .setOverlay(fluidBarOverlay, -1, -1)
                .setFluidRenderer(recipe.output().getAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                .addFluidStack(recipe.output().getFluid(), recipe.output().getAmount());
    }

    @Override
    public ResourceLocation getRegistryName(FluidMixerJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void draw(FluidMixerJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawPanel(guiGraphics);
        if (!recipe.itemInputs().isEmpty()) {
            drawSlot(guiGraphics, ITEM_SLOT_X, ITEM_SLOT_Y);
            int cx = ITEM_SLOT_X + 9;
            int cy = ITEM_SLOT_Y + 19;
            guiGraphics.fill(cx - 3, cy, cx + 4, cy + 1, 0xFF4A4A52);
            guiGraphics.fill(cx - 2, cy + 1, cx + 3, cy + 2, 0xFF656570);
            guiGraphics.fill(cx - 1, cy + 2, cx + 2, cy + 3, 0xFF858595);
            guiGraphics.fill(cx, cy + 3, cx + 1, cy + 4, 0xFFA5A5BA);
        }
        drawProgressArrow(guiGraphics);

        // 绘制能量信息
        if (recipe.energy() > 0) {
            String cost = recipe.energy() + " FE";
            int costWidth = Minecraft.getInstance().font.width(cost);
            guiGraphics.drawString(Minecraft.getInstance().font, cost, WIDTH - costWidth - 4, HEIGHT - 10, 0xFF888888, false);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(SLOT_SPRITE, x, y, 18, 18);
    }

    private void drawProgressArrow(GuiGraphics guiGraphics) {
        int progressWidth = (int) ((System.currentTimeMillis() / 40) % 25);
        guiGraphics.blit(ARROW_TEXTURE, ARROW_X, ARROW_Y, 0, 0, 24, 17, 24, 34);
        if (progressWidth > 0) {
            guiGraphics.blit(ARROW_TEXTURE, ARROW_X, ARROW_Y, 0, 17, progressWidth, 17, 24, 34);
        }
    }
}
