package com.jdte.common.jei.lifesynthesis;

import com.jdte.JDTE;
import com.jdte.setup.JDTEItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class LifeSynthesisRecipeCategory implements IRecipeCategory<LifeSynthesisJeiRecipe> {
    public static final ResourceLocation UID = JDTE.id("life_synthesis");
    public static final RecipeType<LifeSynthesisJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, LifeSynthesisJeiRecipe.class);
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation FLUID_BAR = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/fluidbar.png");
    /** 紧凑布局：培养基、养分、进度、生命流体、FE；最多三列培养基。 */
    private static final int INPUT_X = 4;
    private static final int INPUT_Y = 7;
    private static final int SLOT_SPACING = 18;
    private static final int MAX_COLUMNS = 3;
    private static final int INPUT_FLUID_GAP = 8;
    private static final int PROGRESS_GAP = 6;
    private static final int PROGRESS_SIZE = 14;
    private static final int OUTPUT_GAP = 6;
    private static final int FLUID_SPACING = 24;
    private static final int FLUID_Y = 6;
    private static final int HEIGHT = 84;
    private static final int PULSE_Y = 31;
    private final IDrawable icon;
    private final IDrawable fluidBackground;
    private final IDrawable fluidOverlay;

    public LifeSynthesisRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(JDTEItems.LIFE_SYNTHESIS_VAT.get()));
        fluidBackground = helper.drawableBuilder(FLUID_BAR, 0, 0, 18, 72).setTextureSize(36, 72).build();
        fluidOverlay = helper.drawableBuilder(FLUID_BAR, 18, 0, 18, 72).setTextureSize(36, 72).build();
    }

    @Override public RecipeType<LifeSynthesisJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.jdte.life_synthesis_vat"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return layoutWidth(maxDisplayedInputs()); }
    @Override public int getHeight() { return HEIGHT; }
    @Override public ResourceLocation getRegistryName(LifeSynthesisJeiRecipe recipe) { return recipe.id(); }

    /** 按输入行数计算紧凑列数，超过三行时向下换行。 */
    private static int columns(int inputCount) {
        return Math.min(MAX_COLUMNS, Math.max(1, inputCount));
    }

    private static int fluidX(int inputCount) {
        return INPUT_X + columns(inputCount) * SLOT_SPACING + INPUT_FLUID_GAP;
    }

    private static int progressX(int inputCount) {
        return fluidX(inputCount) + 18 + PROGRESS_GAP;
    }

    private static int outputX(int inputCount) {
        return progressX(inputCount) + PROGRESS_SIZE + OUTPUT_GAP;
    }

    private static int energyX(int inputCount) {
        return outputX(inputCount) + FLUID_SPACING;
    }

    private static int maxDisplayedInputs() {
        return LifeSynthesisJeiRecipe.getRecipes().stream()
                .mapToInt(recipe -> recipe.inputs().size())
                .max()
                .orElse(1);
    }

    private static int contentOffsetX(int inputCount) {
        return Math.max(0, (layoutWidth(maxDisplayedInputs()) - layoutWidth(inputCount)) / 2);
    }

    /** 分类宽度只覆盖当前已加载配方真正需要的内容。 */
    private static int layoutWidth(int inputCount) {
        return energyX(inputCount) + 18 + 4;
    }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, LifeSynthesisJeiRecipe recipe, IFocusGroup focuses) {
        int count = recipe.inputs().size();
        int cols = columns(count);
        int offsetX = contentOffsetX(count);
        for (int input = 0; input < count; input++) {
            var recipeInput = recipe.inputs().get(input);
            builder.addSlot(RecipeIngredientRole.INPUT, offsetX + INPUT_X + (input % cols) * SLOT_SPACING,
                            INPUT_Y + (input / cols) * SLOT_SPACING)
                    .addItemStacks(recipeInput.stacks())
                    .addRichTooltipCallback((view, tooltip) -> {
                        if (recipeInput.count() > 1) tooltip.add(Component.translatable(
                                "jei.jdte.life_synthesis.count", recipeInput.count()).withStyle(ChatFormatting.GRAY));
                    });
        }
        if (!recipe.nutrient().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, offsetX + fluidX(count) + 1, FLUID_Y + 1)
                    .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                    .setFluidRenderer(Math.max(1, recipe.nutrient().getAmount()), false, 16, 70)
                    .addFluidStack(recipe.nutrient().getFluid(), recipe.nutrient().getAmount());
        }
        if (!recipe.output().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, offsetX + outputX(count) + 1, FLUID_Y + 1)
                    .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                    .setFluidRenderer(Math.max(1, recipe.output().getAmount()), false, 16, 70)
                    .addFluidStack(recipe.output().getFluid(), recipe.output().getAmount())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                            Component.translatable("jei.jdte.life_synthesis.tier." + recipe.tier())
                                    .withStyle(ChatFormatting.DARK_PURPLE)));
        }
    }

    @Override public void draw(LifeSynthesisJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                               double mouseX, double mouseY) {
        int count = recipe.inputs().size();
        int offsetX = contentOffsetX(count);
        int cols = columns(count);
        graphics.blitSprite(BACKGROUND, 0, 0, getWidth(), HEIGHT);
        for (int i = 0; i < count; i++) {
            graphics.blitSprite(SLOT, offsetX + INPUT_X - 1 + (i % cols) * SLOT_SPACING, INPUT_Y - 1 + (i / cols) * SLOT_SPACING, 18, 18);
        }
        int pulseX = offsetX + progressX(count);
        drawProductionPulse(graphics, pulseX, PULSE_Y);
        if (mouseX >= pulseX && mouseX < pulseX + 14 && mouseY >= PULSE_Y && mouseY < PULSE_Y + 14) {
            double seconds = recipe.processTicks() / 20.0D;
            String formattedSeconds = seconds == Math.rint(seconds)
                    ? Integer.toString((int) seconds) : String.format("%.1f", seconds);
            graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font,
                    Component.translatable("jei.jdte.bio_factory.process_time",
                            recipe.processTicks(), formattedSeconds), (int) mouseX, (int) mouseY);
        }
        int energyX = offsetX + energyX(count);
        int fill = 1 + (int) ((System.currentTimeMillis() / 35L) % 70L);
        graphics.blit(POWER_BAR, energyX, FLUID_Y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWER_BAR, energyX + 1, FLUID_Y + 70 - fill, 19, 70 - fill, 16, fill, 36, 72);
        if (mouseX >= energyX && mouseX < energyX + 18 && mouseY >= FLUID_Y && mouseY < FLUID_Y + 72) {
            graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font,
                    Component.literal(recipe.energy() + " FE"), (int) mouseX, (int) mouseY);
        }
    }

    private void drawProductionPulse(GuiGraphics graphics, int x, int y) {
        int stage = (int) ((System.currentTimeMillis() / 140L) % 6L);
        int outer = stage < 3 ? stage : 6 - stage;
        graphics.fill(x + 5, y + 2, x + 9, y + 14, 0xFF2B1A1A);
        graphics.fill(x + 1, y + 6, x + 13, y + 10, 0xFF6A2A2A);
        graphics.fill(x + 3, y + 4, x + 11, y + 12, 0xFFDC143C);
        graphics.fill(x + 5, y + 6, x + 9, y + 10, 0xFFF4D35E);
        if (outer >= 1) {
            graphics.fill(x, y + 3, x + 2, y + 5, 0xFF69C7C2);
            graphics.fill(x + 12, y + 11, x + 14, y + 13, 0xFF63A965);
        }
        if (outer >= 2) {
            graphics.fill(x + 11, y + 1, x + 13, y + 3, 0xFF69C7C2);
            graphics.fill(x + 1, y + 12, x + 3, y + 14, 0xFF63A965);
        }
    }
}
