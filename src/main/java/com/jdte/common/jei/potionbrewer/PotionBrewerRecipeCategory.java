package com.jdte.common.jei.potionbrewer;

import com.jdte.JDTE;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
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
import net.minecraft.world.item.Items;

public class PotionBrewerRecipeCategory implements IRecipeCategory<PotionBrewerJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "advanced_potion_brewer");
    public static final RecipeType<PotionBrewerJeiRecipe> RECIPE_TYPE = new RecipeType<>(UID, PotionBrewerJeiRecipe.class);

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation JDT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("justdirethings", "background");
    private static final ResourceLocation JDT_POWER_BAR = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/powerbar.png");
    private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");
    private static final ResourceLocation BREWING_STAND_BG = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final ResourceLocation FUEL_LENGTH_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length");
    private static final ResourceLocation BREW_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final ResourceLocation BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles");

    private static final int WIDTH = 190;
    private static final int HEIGHT = 100;
    private static final int MACHINE_X_OFFSET = 32;
    private static final int MACHINE_Y_OFFSET = 25;
    private static final int BREWING_CORE_SLOT_OFFSET = -1;
    private static final int OUTPUT_GHOST_OFFSET = 2;
    private static final int SLOT_SIZE = 18;
    private static final int FLUID_INNER_WIDTH = 16;
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int FUEL_BAR_WIDTH = 18;
    private static final int FUEL_BAR_HEIGHT = 4;
    private static final int BREW_PROGRESS_WIDTH = 9;
    private static final int BREW_PROGRESS_HEIGHT = 28;
    private static final int BUBBLES_WIDTH = 12;
    private static final int BUBBLES_HEIGHT = 29;
    private static final int VANILLA_BOTTLE_GHOST_SRC_X = 57;
    private static final int VANILLA_BOTTLE_GHOST_SRC_Y = 52;
    private static final int VANILLA_BOTTLE_GHOST_SIZE = 16;
    private static final int[] BUBBLE_LENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    private final IDrawable icon;
    private final IDrawable fluidBarBackground;
    private final IDrawable fluidBarOverlay;

    public PotionBrewerRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JDTEBlocks.ADVANCED_POTION_BREWER.get()));
        this.fluidBarBackground = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 0, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
        this.fluidBarOverlay = guiHelper.drawableBuilder(JDTE_FLUID_BAR, 18, 0, 18, 72)
                .setTextureSize(36, 72)
                .build();
    }

    @Override
    public RecipeType<PotionBrewerJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.jdte.advanced_potion_brewer");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PotionBrewerJeiRecipe recipe, IFocusGroup focuses) {
        GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();

        builder.addSlot(RecipeIngredientRole.CATALYST, brewingCoreIngredientX(config.getPotionBrewerFuelSlotX()), brewingCoreIngredientY(config.getPotionBrewerFuelSlotY()))
                .addItemStack(new ItemStack(Items.BLAZE_POWDER));

        addBottleInputSlot(builder, config.getPotionBrewerBottleSlot0X(), config.getPotionBrewerBottleSlot0Y(), recipe);
        addBottleInputSlot(builder, config.getPotionBrewerBottleSlot1X(), config.getPotionBrewerBottleSlot1Y(), recipe);
        addBottleInputSlot(builder, config.getPotionBrewerBottleSlot2X(), config.getPotionBrewerBottleSlot2Y(), recipe);

        for (int i = 0; i < recipe.ingredientStacks().size(); i++) {
            addIngredientStepSlot(builder, config, i, recipe.ingredientStacks().get(i));
        }

        if (recipe.hasFluidInput()) {
            builder.addSlot(RecipeIngredientRole.INPUT, fluidX(config.getPotionBrewerWaterFluidX()), fluidY(config.getPotionBrewerWaterFluidY()))
                    .setBackground(fluidBarBackground, -1, -1)
                    .setOverlay(fluidBarOverlay, -1, -1)
                    .setFluidRenderer(recipe.fluidStack().getAmount(), false, FLUID_INNER_WIDTH, FLUID_INNER_HEIGHT)
                    .addFluidStack(recipe.fluidStack().getFluid(), recipe.fluidStack().getAmount());
        }

        for (int i = 0; i < config.getPotionBrewerOutputCount(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT,
                            ingredientX(config.getPotionBrewerOutputStartX()),
                            ingredientY(config.getPotionBrewerOutputStartY() + i * config.getPotionBrewerOutputSpacing()))
                    .addItemStack(recipe.outputStack());
        }
    }

    @Override
    public void draw(PotionBrewerJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();

        drawPanel(guiGraphics);
        drawBrewingBackground(guiGraphics, config);
        drawFluidBars(guiGraphics, config);
        drawItemSlots(guiGraphics, config);
        drawOutputBottleGhosts(guiGraphics, config);
        drawFuelBar(guiGraphics, config);
        drawBubbles(guiGraphics, config);
        drawBrewProgress(guiGraphics, config);
        drawEnergyBar(guiGraphics, recipe, config, mouseX, mouseY);
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blitSprite(JDT_BACKGROUND, 0, 0, WIDTH, HEIGHT);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blitSprite(SLOT_SPRITE, x, y, SLOT_SIZE, SLOT_SIZE);
    }

    private void drawBrewingBackground(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        guiGraphics.blit(BREWING_STAND_BG,
                slotX(config.getPotionBrewerBgX()),
                slotY(config.getPotionBrewerBgY()),
                config.getPotionBrewerBgSrcX(),
                config.getPotionBrewerBgSrcY(),
                config.getPotionBrewerBgWidth(),
                config.getPotionBrewerBgHeight());
    }

    private void drawFluidBars(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        drawFluidBar(guiGraphics, config.getPotionBrewerWaterFluidX(), config.getPotionBrewerWaterFluidY());
    }

    private void drawFluidBar(GuiGraphics guiGraphics, int machineX, int machineY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(JDTE_FLUID_BAR, slotX(machineX), slotY(machineY), 0, 0, 18, 72, 36, 72);
        guiGraphics.blit(JDTE_FLUID_BAR, slotX(machineX), slotY(machineY), 18, 0, 18, 72, 36, 72);
    }

    private void drawItemSlots(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        drawBrewingCoreSlot(guiGraphics, config.getPotionBrewerFuelSlotX(), config.getPotionBrewerFuelSlotY());
        drawBrewingCoreSlot(guiGraphics, config.getPotionBrewerIngredientSlotX(), config.getPotionBrewerIngredientSlotY());
        drawBrewingCoreSlot(guiGraphics, config.getPotionBrewerBottleSlot0X(), config.getPotionBrewerBottleSlot0Y());
        drawBrewingCoreSlot(guiGraphics, config.getPotionBrewerBottleSlot1X(), config.getPotionBrewerBottleSlot1Y());
        drawBrewingCoreSlot(guiGraphics, config.getPotionBrewerBottleSlot2X(), config.getPotionBrewerBottleSlot2Y());

        for (int i = 0; i < config.getPotionBrewerExtraIngredientCount(); i++) {
            drawSlot(guiGraphics,
                    slotX(config.getPotionBrewerExtraIngredientStartX() + i * config.getPotionBrewerExtraIngredientSpacing()),
                    slotY(config.getPotionBrewerExtraIngredientStartY()));
        }

        for (int i = 0; i < config.getPotionBrewerOutputCount(); i++) {
            drawSlot(guiGraphics,
                    slotX(config.getPotionBrewerOutputStartX()),
                    slotY(config.getPotionBrewerOutputStartY() + i * config.getPotionBrewerOutputSpacing()));
        }
    }

    private void drawOutputBottleGhosts(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        for (int i = 0; i < config.getPotionBrewerOutputCount(); i++) {
            guiGraphics.blit(BREWING_STAND_BG,
                    slotX(config.getPotionBrewerOutputStartX()) + OUTPUT_GHOST_OFFSET,
                    slotY(config.getPotionBrewerOutputStartY() + i * config.getPotionBrewerOutputSpacing()) + OUTPUT_GHOST_OFFSET,
                    VANILLA_BOTTLE_GHOST_SRC_X,
                    VANILLA_BOTTLE_GHOST_SRC_Y,
                    VANILLA_BOTTLE_GHOST_SIZE,
                    VANILLA_BOTTLE_GHOST_SIZE);
        }
    }

    private void drawBrewingCoreSlot(GuiGraphics guiGraphics, int machineX, int machineY) {
        drawSlot(guiGraphics, brewingCoreSlotX(machineX), brewingCoreSlotY(machineY));
    }

    private void drawFuelBar(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        int fuelWidth = 1 + (int) ((System.currentTimeMillis() / 90) % FUEL_BAR_WIDTH);
        guiGraphics.blitSprite(FUEL_LENGTH_SPRITE,
                FUEL_BAR_WIDTH,
                FUEL_BAR_HEIGHT,
                0,
                0,
                slotX(config.getPotionBrewerFuelBarX()),
                slotY(config.getPotionBrewerFuelBarBottomY() - FUEL_BAR_HEIGHT),
                fuelWidth,
                FUEL_BAR_HEIGHT);
    }

    private void drawBubbles(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        int bubbleHeight = BUBBLE_LENGTHS[(int) ((System.currentTimeMillis() / 180) % BUBBLE_LENGTHS.length)];
        if (bubbleHeight > 0) {
            guiGraphics.blitSprite(BUBBLES_SPRITE,
                    BUBBLES_WIDTH,
                    BUBBLES_HEIGHT,
                    0,
                    BUBBLES_HEIGHT - bubbleHeight,
                    slotX(config.getPotionBrewerBubblesX()),
                    slotY(config.getPotionBrewerBubblesBottomY() - bubbleHeight),
                    BUBBLES_WIDTH,
                    bubbleHeight);
        }
    }

    private void drawBrewProgress(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config) {
        int progressHeight = 1 + (int) ((System.currentTimeMillis() / 35) % BREW_PROGRESS_HEIGHT);
        guiGraphics.blitSprite(BREW_PROGRESS_SPRITE,
                BREW_PROGRESS_WIDTH,
                BREW_PROGRESS_HEIGHT,
                0,
                0,
                slotX(config.getPotionBrewerArrowX()),
                slotY(config.getPotionBrewerArrowBottomY() - BREW_PROGRESS_HEIGHT),
                BREW_PROGRESS_WIDTH,
                progressHeight);
    }

    private void drawEnergyBar(GuiGraphics guiGraphics, PotionBrewerJeiRecipe recipe, GuiUpgradeLayoutConfig config, double mouseX, double mouseY) {
        int x = energyBarX(config);
        int y = energyBarY(config);
        int fillHeight = 1 + (int) ((System.currentTimeMillis() / 35) % 70);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(JDT_POWER_BAR, x, y, 0, 0, 18, 72, 36, 72);
        guiGraphics.blit(JDT_POWER_BAR, x + 1, y + 70 - fillHeight, 19, 70 - fillHeight, 16, fillHeight, 36, 72);

        if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 72) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal(recipe.energyCost() + " FE"), (int) mouseX, (int) mouseY);
        }
    }

    private static int slotX(int machineX) {
        return machineX + MACHINE_X_OFFSET;
    }

    private static int slotY(int machineY) {
        return machineY + MACHINE_Y_OFFSET;
    }

    private static int ingredientX(int machineX) {
        return slotX(machineX) + 1;
    }

    private static int ingredientY(int machineY) {
        return slotY(machineY) + 1;
    }

    private static int brewingCoreSlotX(int machineX) {
        return slotX(machineX) + BREWING_CORE_SLOT_OFFSET;
    }

    private static int brewingCoreSlotY(int machineY) {
        return slotY(machineY) + BREWING_CORE_SLOT_OFFSET;
    }

    private static int brewingCoreIngredientX(int machineX) {
        return brewingCoreSlotX(machineX) + 1;
    }

    private static int brewingCoreIngredientY(int machineY) {
        return brewingCoreSlotY(machineY) + 1;
    }

    private static int fluidX(int machineX) {
        return slotX(machineX) + 1;
    }

    private static int fluidY(int machineY) {
        return slotY(machineY) + 1;
    }

    private static int energyBarX(GuiUpgradeLayoutConfig config) {
        return slotX(config.getPotionBrewerWaterFluidX() - 18);
    }

    private static int energyBarY(GuiUpgradeLayoutConfig config) {
        return slotY(config.getPotionBrewerWaterFluidY());
    }

    private static void addBottleInputSlot(IRecipeLayoutBuilder builder, int machineX, int machineY, PotionBrewerJeiRecipe recipe) {
        builder.addSlot(RecipeIngredientRole.INPUT, brewingCoreIngredientX(machineX), brewingCoreIngredientY(machineY))
                .addItemStack(recipe.inputStack());
    }

    private static void addIngredientStepSlot(IRecipeLayoutBuilder builder, GuiUpgradeLayoutConfig config, int step, java.util.List<ItemStack> stacks) {
        if (step == 0) {
            builder.addSlot(RecipeIngredientRole.INPUT,
                            brewingCoreIngredientX(config.getPotionBrewerIngredientSlotX()),
                            brewingCoreIngredientY(config.getPotionBrewerIngredientSlotY()))
                    .addItemStacks(stacks);
            return;
        }
        if (step > config.getPotionBrewerExtraIngredientCount()) {
            return;
        }

        builder.addSlot(RecipeIngredientRole.INPUT,
                        ingredientX(config.getPotionBrewerExtraIngredientStartX() + (step - 1) * config.getPotionBrewerExtraIngredientSpacing()),
                        ingredientY(config.getPotionBrewerExtraIngredientStartY()))
                .addItemStacks(stacks);
    }
}
