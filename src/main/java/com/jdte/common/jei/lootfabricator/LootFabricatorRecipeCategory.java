package com.jdte.common.jei.lootfabricator;

import com.jdte.JDTE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEFluids;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import net.neoforged.neoforge.fluids.FluidStack;
import com.direwolf20.justdirethings.setup.Registration;

public class LootFabricatorRecipeCategory implements IRecipeCategory<LootFabricatorJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "loot_fabricator");
    public static final RecipeType<LootFabricatorJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, LootFabricatorJeiRecipe.class);
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDT_POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");
    private static final int WIDTH = 190;
    private static final int HEIGHT = 82;
    private static final int INPUT_X = 5;
    private static final int INPUT_Y = 5;
    private static final int FLUID_LIFE_X = 29;
    private static final int FLUID_TIME_X = 51;
    private static final int OUTPUT_X = 96;
    private static final int OUTPUT_Y = 5;
    private static final int ENERGY_X = 170;
    private final IDrawable icon;
    private final IDrawable fluidBackground;
    private final IDrawable fluidOverlay;

    public LootFabricatorRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(JDTEBlocks.LOOT_FABRICATOR.get()));
        fluidBackground = guiHelper.drawableBuilder(FLUID_BAR, 0, 0, 18, 72).setTextureSize(36, 72).build();
        fluidOverlay = guiHelper.drawableBuilder(FLUID_BAR, 18, 0, 18, 72).setTextureSize(36, 72).build();
    }

    @Override public RecipeType<LootFabricatorJeiRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.jdte.loot_fabricator"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LootFabricatorJeiRecipe recipe, IFocusGroup focuses) {
        int lifeFluidCost = LootFabricatorBE.getLifeFluidCost(recipe.spawnEgg());
        int baseTimeFluidCost = LootFabricatorBE.getBaseTimeFluidCost(recipe.spawnEgg());
        int maxTimeFluidCost = LootFabricatorBE.getMaxTimeFluidCost(recipe.spawnEgg());
        builder.addSlot(RecipeIngredientRole.CATALYST, INPUT_X + 1, INPUT_Y + 1)
                .addItemStack(recipe.spawnEgg())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.jdte.not_consumed").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.INPUT, FLUID_LIFE_X + 1, INPUT_Y + 1)
                .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                .setFluidRenderer(lifeFluidCost, false, 16, 70)
                .addFluidStack(JDTEFluids.LIFE_FLUID_SOURCE.get(), lifeFluidCost);
        builder.addSlot(RecipeIngredientRole.INPUT, FLUID_TIME_X + 1, INPUT_Y + 1)
                .setBackground(fluidBackground, -1, -1).setOverlay(fluidOverlay, -1, -1)
                .setFluidRenderer(maxTimeFluidCost, false, 16, 70)
                .addFluidStack(Registration.TIME_FLUID_SOURCE.get(), maxTimeFluidCost)
                .addRichTooltipCallback((view, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(new FluidStack(Registration.TIME_FLUID_SOURCE.get(), 1).getHoverName());
                    tooltip.add(Component.translatable("jei.jdte.loot_fabricator.time_fluid_range",
                            baseTimeFluidCost, maxTimeFluidCost).withStyle(ChatFormatting.GRAY));
                });
        for (int i = 0; i < recipe.possibleDrops().size(); i++) {
            LootFabricatorJeiRecipe.DisplayDrop drop = recipe.possibleDrops().get(i);
            int slot = i;
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX(slot) + 1, outputY(slot) + 1)
                    .addItemStack(drop.stack())
                    .addRichTooltipCallback((view, tooltip) -> addDropTooltip(tooltip, drop));
        }
    }

    @Override public ResourceLocation getRegistryName(LootFabricatorJeiRecipe recipe) { return recipe.id(); }

    @Override
    public void draw(LootFabricatorJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < LootFabricatorBE.INPUT_SLOTS; i++) {
            graphics.blitSprite(SLOT, INPUT_X, INPUT_Y + i * 18, 18, 18);
        }
        for (int i = 0; i < 16; i++) {
            graphics.blitSprite(SLOT, outputX(i), outputY(i), 18, 18);
        }
        drawArrow(graphics, 70, 30);
        drawEnergyBar(graphics, mouseX, mouseY);
    }

    private static void drawArrow(GuiGraphics graphics, int x, int y) {
        int progressWidth = (int) ((System.currentTimeMillis() / 30) % 24);
        drawArrowLayer(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrowLayer(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) drawArrowLayer(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF3DBB57);
    }

    private static void drawArrowLayer(GuiGraphics graphics, int x, int y, int width, int color) {
        int clamped = Math.clamp(width, 0, 24);
        if (clamped <= 0) return;
        int body = Math.min(16, clamped);
        graphics.fill(x, y + 5, x + body, y + 10, color);
        int head = clamped - 16;
        if (head > 0) drawHeadPart(graphics, x + 16, y + 4, Math.min(head, 2), 7, color);
        if (head > 2) drawHeadPart(graphics, x + 18, y + 3, Math.min(head - 2, 2), 9, color);
        if (head > 4) drawHeadPart(graphics, x + 20, y + 2, Math.min(head - 4, 2), 11, color);
        if (head > 6) drawHeadPart(graphics, x + 22, y + 1, Math.min(head - 6, 2), 13, color);
    }

    private static void drawHeadPart(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width > 0) graphics.fill(x, y, x + width, y + height, color);
    }

    private static int outputX(int index) {
        return OUTPUT_X + (index % 4) * 18;
    }

    private static int outputY(int index) {
        return OUTPUT_Y + (index / 4) * 18;
    }

    private static void drawEnergyBar(GuiGraphics graphics, double mouseX, double mouseY) {
        int fillHeight = 1 + (int) ((System.currentTimeMillis() / 35) % 70);
        graphics.blit(JDT_POWER_BAR, ENERGY_X, INPUT_Y, 0, 0, 18, 72, 36, 72);
        graphics.blit(JDT_POWER_BAR, ENERGY_X + 1, INPUT_Y + 70 - fillHeight,
                19, 70 - fillHeight, 16, fillHeight, 36, 72);
        if (mouseX >= ENERGY_X && mouseX < ENERGY_X + 18 && mouseY >= INPUT_Y && mouseY < INPUT_Y + 72) {
            graphics.renderTooltip(Minecraft.getInstance().font,
                    Component.translatable("jei.jdte.loot_fabricator.energy", LootFabricatorBE.ENERGY_COST),
                    (int) mouseX, (int) mouseY);
        }
    }

    private static void addDropTooltip(ITooltipBuilder tooltip, LootFabricatorJeiRecipe.DisplayDrop drop) {
        if (drop.minCount() != drop.maxCount()) {
            tooltip.add(Component.translatable("jei.jdte.loot_fabricator.count_range", drop.minCount(), drop.maxCount())
                    .withStyle(ChatFormatting.GRAY));
        } else if (drop.maxCount() > 1) {
            tooltip.add(Component.translatable("jei.jdte.loot_fabricator.count", drop.maxCount())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (!drop.chanceLabel().isEmpty()) {
            var chance = drop.chanceLabel().equals("conditional")
                    ? Component.translatable("jei.jdte.loot_fabricator.conditional_chance")
                    : Component.translatable("jei.jdte.loot_fabricator.chance", drop.chanceLabel());
            tooltip.add(chance.withStyle(ChatFormatting.GRAY));
        }
    }
}
