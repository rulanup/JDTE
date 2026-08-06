package com.jdte.common.jei.biofactory;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.JDTE;
import com.jdte.setup.JDTEFluids;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BioFactoryRecipeCategory implements IRecipeCategory<BioFactoryJeiRecipe> {
    public static final ResourceLocation UID = JDTE.id("bio_factory");
    public static final RecipeType<BioFactoryJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, BioFactoryJeiRecipe.class);
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation FLUID_BAR = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/fluidbar.png");
    private static final int WIDTH = 207;
    private static final int HEIGHT = 84;
    private static final int FLUID_Y = 6;
    private static final int PROCESS_FLUID_X = 6;
    private static final int PRODUCT_FLUID_X = 119;
    private static final int LIFE_FLUID_X = 139;
    private static final int TIME_FLUID_X = 159;
    private static final int ENERGY_X = 179;
    private final IDrawable icon;
    private final IDrawable fluidBackground;
    private final IDrawable fluidOverlay;

    public BioFactoryRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(JDTEItems.BIO_FACTORY.get()));
        fluidBackground = helper.drawableBuilder(FLUID_BAR, 0, 0, 18, 72).setTextureSize(36, 72).build();
        fluidOverlay = helper.drawableBuilder(FLUID_BAR, 18, 0, 18, 72).setTextureSize(36, 72).build();
    }

    @Override public RecipeType<BioFactoryJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.jdte.bio_factory"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public ResourceLocation getRegistryName(BioFactoryJeiRecipe recipe) { return recipe.id(); }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, BioFactoryJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 30, 7)
                .addItemStacks(recipe.specimens())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.jdte.not_consumed").withStyle(ChatFormatting.GRAY)));
        for (int input = 0; input < Math.min(3, recipe.inputs().size()); input++) {
            var recipeInput = recipe.inputs().get(input);
            var slot = builder.addSlot(recipeInput.count() > 0 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST,
                    30, 25 + input * 18).addItemStacks(recipeInput.stacks());
            if (recipeInput.count() == 0) {
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.jdte.not_consumed").withStyle(ChatFormatting.GRAY)));
            }
        }
        for (int i = 0; i < Math.min(8, recipe.outputs().size()); i++) {
            var output = recipe.outputs().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 75 + (i % 2) * 18, 7 + (i / 2) * 18)
                    .addItemStacks(output.stacks())
                    .addRichTooltipCallback((view, tooltip) -> {
                        if (output.chance() < 1.0F) tooltip.add(Component.translatable(
                                "jei.jdte.bio_factory.chance", String.format("%.1f%%", output.chance() * 100.0F)));
                    });
        }
        builder.addSlot(RecipeIngredientRole.INPUT, LIFE_FLUID_X + 1, FLUID_Y + 1)
                .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                .setFluidRenderer(Math.max(1, recipe.lifeFluidAmount()), false, 16, 70)
                .addFluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), recipe.lifeFluidAmount());
        builder.addSlot(RecipeIngredientRole.INPUT, TIME_FLUID_X + 1, FLUID_Y + 1)
                .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                .setFluidRenderer(Math.max(1, recipe.timeFluidAmount()), false, 16, 70)
                .addFluidStack(Registration.TIME_FLUID_SOURCE.get(), recipe.timeFluidAmount());
        if (recipe.processFluid().isPresent()) {
            builder.addSlot(RecipeIngredientRole.INPUT, PROCESS_FLUID_X + 1, FLUID_Y + 1)
                    .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1).setFluidRenderer(
                            Math.max(1, recipe.processFluidAmount()), false, 16, 70)
                    .addFluidStack(BuiltInRegistries.FLUID.get(recipe.processFluid().get()), recipe.processFluidAmount());
        }
        if (recipe.outputFluid().isPresent()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, PRODUCT_FLUID_X + 1, FLUID_Y + 1)
                    .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1).setFluidRenderer(
                            Math.max(1, recipe.outputFluidAmount()), false, 16, 70)
                    .addFluidStack(BuiltInRegistries.FLUID.get(recipe.outputFluid().get()), recipe.outputFluidAmount());
        }
    }

    @Override public void draw(BioFactoryJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                               double mouseX, double mouseY) {
        graphics.blitSprite(BACKGROUND, 0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < 4; i++) graphics.blitSprite(SLOT, 29, 6 + i * 18, 18, 18);
        for (int i = 0; i < 8; i++) graphics.blitSprite(SLOT, 74 + (i % 2) * 18, 6 + (i / 2) * 18, 18, 18);
        if (recipe.processFluid().isEmpty()) drawEmptyTank(graphics, PROCESS_FLUID_X);
        if (recipe.outputFluid().isEmpty()) drawEmptyTank(graphics, PRODUCT_FLUID_X);
        drawProductionPulse(graphics, 53, 31);
        if (mouseX >= 53 && mouseX < 67 && mouseY >= 31 && mouseY < 45) {
            double seconds = recipe.processTicks() / 20.0D;
            String formattedSeconds = seconds == Math.rint(seconds)
                    ? Integer.toString((int) seconds) : String.format("%.1f", seconds);
            graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font,
                    Component.translatable("jei.jdte.bio_factory.process_time",
                            recipe.processTicks(), formattedSeconds), (int) mouseX, (int) mouseY);
        }
        int fill = 1 + (int) ((System.currentTimeMillis() / 35L) % 70L);
        graphics.blit(POWER_BAR, ENERGY_X, FLUID_Y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWER_BAR, ENERGY_X + 1, FLUID_Y + 70 - fill, 19, 70 - fill, 16, fill, 36, 72);
        if (mouseX >= ENERGY_X && mouseX < ENERGY_X + 18 && mouseY >= FLUID_Y && mouseY < FLUID_Y + 72) {
            graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font,
                    Component.literal(recipe.energy() + " FE"), (int) mouseX, (int) mouseY);
        }
    }

    private void drawEmptyTank(GuiGraphics graphics, int x) {
        graphics.blit(FLUID_BAR, x, FLUID_Y, 0, 0, 18, 72, 36, 72);
        graphics.blit(FLUID_BAR, x, FLUID_Y, 18, 0, 18, 72, 36, 72);
    }

    private void drawProductionPulse(GuiGraphics graphics, int x, int y) {
        int stage = (int) ((System.currentTimeMillis() / 140L) % 6L);
        int outer = stage < 3 ? stage : 6 - stage;
        graphics.fill(x + 5, y + 2, x + 9, y + 14, 0xFF5E3428);
        graphics.fill(x + 1, y + 6, x + 13, y + 10, 0xFF5E3428);
        graphics.fill(x + 3, y + 4, x + 11, y + 12, 0xFFCD7B2A);
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
