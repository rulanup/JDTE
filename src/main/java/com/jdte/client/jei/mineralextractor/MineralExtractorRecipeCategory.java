package com.jdte.client.jei.mineralextractor;

import com.jdte.JDTE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Locale;

public class MineralExtractorRecipeCategory implements IRecipeCategory<MineralExtractorJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "mineral_extractor");
    public static final RecipeType<MineralExtractorJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, MineralExtractorJeiRecipe.class);

    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDT_POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluid_bar.png");

    private static final int WIDTH = 192;
    private static final int HEIGHT = 90;
    private static final int INPUT_X = 6;
    private static final int INPUT_Y = 43;
    private static final int FLUID_FORTUNE_X = 28;
    private static final int FLUID_ACCELERATION_X = 48;
    private static final int FLUID_Y = 16;
    private static final int ARROW_X = 68;
    private static final int ARROW_Y = 43;
    private static final int OUTPUT_START_X = 94;
    private static final int OUTPUT_START_Y = 16;
    private static final int ENERGY_X = 170;
    private static final int ENERGY_Y = 16;

    private final IDrawable icon;
    private final IDrawable fluidBackground;
    private final IDrawable fluidOverlay;

    public MineralExtractorRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(JDTEBlocks.MINERAL_EXTRACTOR.get()));
        fluidBackground = guiHelper.drawableBuilder(FLUID_BAR, 0, 0, 18, 72).setTextureSize(36, 72).build();
        fluidOverlay = guiHelper.drawableBuilder(FLUID_BAR, 18, 0, 18, 72).setTextureSize(36, 72).build();
    }

    @Override
    public RecipeType<MineralExtractorJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.jdte.mineral_extractor");
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
    public ResourceLocation getRegistryName(MineralExtractorJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MineralExtractorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, INPUT_X + 1, INPUT_Y + 1)
                .addItemStacks(List.of(recipe.surveyStack(), new ItemStack(JDTEItems.MINERAL_SURVEY.get())))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.jdte.not_consumed").withStyle(ChatFormatting.GRAY)));

        Fluid fortune = BuiltInRegistries.FLUID.getOptional(recipe.fortuneFluid()).orElse(Fluids.EMPTY);
        if (fortune != Fluids.EMPTY) {
            builder.addSlot(RecipeIngredientRole.INPUT, FLUID_FORTUNE_X + 1, FLUID_Y + 1)
                    .setBackground(fluidBackground, -1, -1)
                    .setOverlay(fluidOverlay, -1, -1)
                    .setFluidRenderer(Math.max(1, recipe.experienceFluidCost()), false, 16, 70)
                    .addFluidStack(fortune, Math.max(1, recipe.experienceFluidCost()))
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.clear();
                        tooltip.add(new FluidStack(fortune, 1).getHoverName());
                        tooltip.add(Component.translatable("jdte.screen.mineral_extractor.experience_fluid",
                                recipe.experienceFluidCost(), recipe.experienceFluidCost(), recipe.fortuneBonusPercent())
                                .withStyle(ChatFormatting.GRAY));
                    });
        }

        Fluid acceleration = BuiltInRegistries.FLUID.getOptional(recipe.accelerationFluid()).orElse(Fluids.EMPTY);
        if (acceleration != Fluids.EMPTY) {
            builder.addSlot(RecipeIngredientRole.INPUT, FLUID_ACCELERATION_X + 1, FLUID_Y + 1)
                    .setBackground(fluidBackground, -1, -1)
                    .setOverlay(fluidOverlay, -1, -1)
                    .setFluidRenderer(Math.max(1, recipe.timeFluidCost()), false, 16, 70)
                    .addFluidStack(acceleration, Math.max(1, recipe.timeFluidCost()))
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.clear();
                        tooltip.add(new FluidStack(acceleration, 1).getHoverName());
                        tooltip.add(Component.translatable("jdte.screen.mineral_extractor.time_fluid",
                                recipe.timeFluidCost(), recipe.timeFluidCost())
                                .withStyle(ChatFormatting.GRAY));
                    });
        }

        for (int i = 0; i < recipe.minerals().size(); i++) {
            MineralExtractorJeiRecipe.DisplayMineral mineral = recipe.minerals().get(i);
            int col = i % 4;
            int row = i / 4;
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_START_X + col * 18 + 1, OUTPUT_START_Y + row * 18 + 1)
                    .addItemStack(mineral.stack())
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.translatable("jei.jdte.mineral_extractor.chance", mineral.chancePercent())
                                .withStyle(ChatFormatting.GOLD));
                        tooltip.add(Component.translatable("jdte.screen.mineral_survey.details",
                                formatHeight(mineral.entry().minY()),
                                formatHeight(mineral.entry().maxY()),
                                mineral.entry().veinSize(),
                                Component.translatable("jdte.mineral.confidence."
                                        + mineral.entry().confidence().name().toLowerCase(Locale.ROOT)))
                                .withStyle(ChatFormatting.GRAY));
                    });
        }
    }

    @Override
    public void draw(MineralExtractorJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);

        Component biomeName = Component.translatable(net.minecraft.Util.makeDescriptionId("biome", recipe.biomeId()));
        int maxTitleWidth = recipe.totalPages() > 1 ? WIDTH - 40 : WIDTH - 12;
        var titleLines = Minecraft.getInstance().font.split(biomeName, maxTitleWidth);
        if (!titleLines.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, titleLines.getFirst(), 6, 5, 0xFF30271D, false);
        }
        if (recipe.totalPages() > 1) {
            String pageText = (recipe.outputPage() + 1) + "/" + recipe.totalPages();
            graphics.drawString(Minecraft.getInstance().font, pageText,
                    WIDTH - 6 - Minecraft.getInstance().font.width(pageText), 5, 0xFF75664F, false);
        }

        graphics.blitSprite(SLOT, INPUT_X, INPUT_Y, 18, 18);
        for (int i = 0; i < 16; i++) {
            int col = i % 4;
            int row = i / 4;
            graphics.blitSprite(SLOT, OUTPUT_START_X + col * 18, OUTPUT_START_Y + row * 18, 18, 18);
        }

        drawArrow(graphics, ARROW_X, ARROW_Y);

        int fill = 1 + (int) ((System.currentTimeMillis() / 35) % 70);
        graphics.blit(JDT_POWER_BAR, ENERGY_X, ENERGY_Y, 0, 0, 18, 72, 36, 72);
        graphics.blit(JDT_POWER_BAR, ENERGY_X + 1, ENERGY_Y + 70 - fill, 19, 70 - fill, 16, fill, 36, 72);
        if (mouseX >= ENERGY_X && mouseX < ENERGY_X + 18 && mouseY >= ENERGY_Y && mouseY < ENERGY_Y + 72) {
            graphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal(recipe.energyCost() + " FE / cycle"), (int) mouseX, (int) mouseY);
        }
    }

    private static void drawArrow(GuiGraphics graphics, int x, int y) {
        int progressWidth = (int) ((System.currentTimeMillis() / 80) % 24);
        drawArrowLayer(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrowLayer(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrowLayer(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF287B91);
        }
    }

    private static void drawArrowLayer(GuiGraphics graphics, int x, int y, int width, int color) {
        int headStart = Math.max(0, width - 8);
        if (headStart > 0) {
            graphics.fill(x, y + 4, x + headStart, y + 11, color);
        }
        for (int i = 0; i < Math.min(8, width); i++) {
            int currentX = x + headStart + i;
            graphics.fill(currentX, y + 7 - i, currentX + 1, y + 8 + i, color);
        }
    }

    private static String formatHeight(int height) {
        return height == Integer.MIN_VALUE || height == Integer.MAX_VALUE ? "?" : Integer.toString(height);
    }
}
