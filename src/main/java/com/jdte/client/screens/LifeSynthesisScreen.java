package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.containers.LifeSynthesisContainer;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.recipes.LifeSynthesisRecipe;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.setup.JDTERecipes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class LifeSynthesisScreen extends BaseMachineScreen<LifeSynthesisContainer> {
    /** 养分/时间罐紧贴培养基右侧；生命罐与红石按钮对齐玩家物品栏第九格（X=152）。 */
    private static final int[] TANK_X = {64, 86, 152};
    private static final int TANK_Y = -21;
    private static final int PROGRESS_X = 112;
    private static final int PROGRESS_Y = 7;
    /** 档位文字位于进度条正上方，并与进度条左缘对齐。 */
    private static final int STATUS_X = 112;
    private static final int STATUS_TIER_Y = -3;
    private static final int SPEED_X = PROGRESS_X + 2;
    private static final int SPEED_Y = 24;
    private static final int REDSTONE_X = 152;
    private static final int REDSTONE_Y = 51;
    private static final String[] TANK_KEYS = {"nutrient", "time", "life"};
    private static final Component INPUT_TOOLTIP = Component.translatable("jdte.slot.life_synthesis_vat_input");
    private final LifeSynthesisContainer vatContainer;
    private NumberButton multiplierButton;

    public LifeSynthesisScreen(LifeSynthesisContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        vatContainer = container;
    }

    @Override
    public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getLootFabricatorExtraWidth();
        extraHeight = layout.getLootFabricatorExtraHeight();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderEnergyBar(graphics);
        renderMachineSlotBackgrounds(graphics);
        renderCultureProgress(graphics);
        renderTierBadge(graphics);
        LifeSynthesisVatBE vat = vatContainer.getVat();
        renderTank(graphics, TANK_X[0], vat.getNutrientTank().getFluid(), vatContainer.getNutrientFluid());
        renderTank(graphics, TANK_X[1], vat.getTimeFluidTank().getFluid(), vatContainer.getTimeFluid());
        renderTank(graphics, TANK_X[2], vat.getLifeFluidTank().getFluid(), vatContainer.getLifeFluid());
    }

    /** JDT 基类使用 int 计算填充比例，扩容后的大容量会溢出并把填充高度算成 0。 */
    private void renderEnergyBar(GuiGraphics graphics) {
        int maxEnergy = Math.max(1, vatContainer.getVat().getMaxEnergy());
        int fill = Math.clamp((int) ((long) vatContainer.getEnergy() * 70L / maxEnergy), 0, 70);
        int x = topSectionLeft + getEnergyBarOffset();
        int y = topSectionTop + 5;
        graphics.blit(POWERBAR, x, y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWERBAR, x + 1, y + 70 - fill, 19, 69 - fill, 17, fill + 1, 36, 72);
    }

    private void renderMachineSlotBackgrounds(GuiGraphics graphics) {
        for (int i = 0; i < LifeSynthesisVatBE.INPUT_SLOTS; i++) {
            Slot slot = container.slots.get(i);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderCultureProgress(GuiGraphics graphics) {
        int x = getGuiLeft() + PROGRESS_X;
        int y = getGuiTop() + PROGRESS_Y;
        int progressWidth = Math.clamp(vatContainer.getProgress() * 24
                / Math.max(1, vatContainer.getProgressMax()), 0, 24);
        graphics.fill(x, y + 5, x + 24, y + 10, 0xFF2B1A1A);
        if (progressWidth > 0) graphics.fill(x, y + 5, x + progressWidth, y + 10, 0xFFDC143C);
        graphics.fill(x + 24, y + 3, x + 28, y + 12, 0xFF6A2A2A);
    }

    private void renderTierBadge(GuiGraphics graphics) {
        int tier = vatContainer.getTierCode();
        // 无配方（档位 0）时不再显示“待机”文字
        if (tier <= 0) return;
        Component label = switch (tier) {
            case 1 -> Component.translatable("jdte.tier.plant");
            case 2 -> Component.translatable("jdte.tier.protein");
            case 3 -> Component.translatable("jdte.tier.enriched");
            default -> Component.translatable("jdte.tier.none");
        };
        graphics.drawString(font, label, getGuiLeft() + STATUS_X, getGuiTop() + STATUS_TIER_Y, 0x555555, false);
    }

    private void renderTank(GuiGraphics graphics, int relativeX, FluidStack stack, int amount) {
        int x = getGuiLeft() + relativeX;
        int y = getGuiTop() + TANK_Y;
        graphics.blit(FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);
        int height = Math.clamp((int) ((long) amount * 70L / Math.max(1, vatContainer.getFluidCapacity())), 0, 70);
        if (height > 0 && !stack.isEmpty()) renderFluid(graphics, stack, x + 1, y + 71, 16, height);
        graphics.blit(FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderFluid(GuiGraphics graphics, FluidStack stack, int x, int bottom, int width, int height) {
        var extension = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());
        int tint = extension.getTintColor(stack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor((tint >> 16 & 255) / 255.0F, (tint >> 8 & 255) / 255.0F,
                (tint & 255) / 255.0F, (tint >>> 24) / 255.0F);
        PoseStack pose = graphics.pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(pose.last().pose(), x, bottom, 0).setUv(sprite.getU0(), sprite.getV1());
        buffer.addVertex(pose.last().pose(), x + width, bottom, 0).setUv(sprite.getU1(), sprite.getV1());
        buffer.addVertex(pose.last().pose(), x + width, bottom - height, 0).setUv(sprite.getU1(), sprite.getV0());
        buffer.addVertex(pose.last().pose(), x, bottom - height, 0).setUv(sprite.getU0(), sprite.getV0());
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void addRedstoneButtons() {
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + REDSTONE_X,
                getGuiTop() + REDSTONE_Y,
                redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addTickSpeedButton() {
        multiplierButton = new NumberButton(
                getGuiLeft() + SPEED_X,
                getGuiTop() + SPEED_Y,
                24, 12, vatContainer.getMultiplier(), 1, vatContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.life_synthesis_vat.multiplier"), button ->
                PacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue())));
        addRenderableWidget(multiplierButton);
    }

    /**
     * 基类流体条渲染到屏幕外：本机三个自定义罐已覆盖养分/时间/生命流体，
     * 基类条（显示 getFluidTank 的时间流体）会与第三个罐重叠并造成 tooltip 双显。
     */
    @Override
    public int getFluidBarOffset() {
        return 4096;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = vatContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(vatContainer.getMultiplier(), multiplierButton.max));
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem() && vatContainer.isInputSlot(hoveredSlot)) {
            graphics.renderTooltip(font, INPUT_TOOLTIP, mouseX, mouseY);
        }
        if (MiscTools.inBounds(getGuiLeft() + PROGRESS_X, getGuiTop() + PROGRESS_Y, 28, 12, mouseX, mouseY)) {
            LifeSynthesisRecipe recipe = currentRecipe();
            if (recipe != null) {
                // 配方 tooltip 固定显示在进度条上方、右移对齐进度条，避免遮挡槽位与罐
                List<FormattedText> lines = new ArrayList<>(recipeTooltip(recipe));
                graphics.renderTooltip(font, Language.getInstance().getVisualOrder(lines),
                        getGuiLeft() + PROGRESS_X + 20, getGuiTop() + PROGRESS_Y - 8);
                return;
            }
        }
        for (int i = 0; i < TANK_X.length; i++) {
            if (MiscTools.inBounds(getGuiLeft() + TANK_X[i], getGuiTop() + TANK_Y, 18, 72, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable("jdte.screen.life_synthesis_vat." + TANK_KEYS[i],
                        tankAmount(i), vatContainer.getFluidCapacity()), mouseX, mouseY);
                return;
            }
        }
    }

    /** 按服务端同步的配方序号取当前配方（服务端与客户端共享同一份同步配方列表）。 */
    private LifeSynthesisRecipe currentRecipe() {
        int index = vatContainer.getRecipeIndex();
        if (index < 0 || minecraft == null || minecraft.level == null) return null;
        List<RecipeHolder<LifeSynthesisRecipe>> recipes = minecraft.level.getRecipeManager()
                .getAllRecipesFor(JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get());
        if (index >= recipes.size()) return null;
        return recipes.get(index).value();
    }

    private List<Component> recipeTooltip(LifeSynthesisRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("jdte.screen.life_synthesis_vat.recipe_tooltip"));
        StringBuilder summary = new StringBuilder();
        for (LifeSynthesisRecipe.InputSlot slot : recipe.inputs()) {
            ItemStack[] samples = slot.ingredient().getItems();
            if (samples.length > 0) {
                if (!summary.isEmpty()) summary.append(" + ");
                summary.append(samples[0].getHoverName().getString()).append(" ×").append(slot.count());
            }
        }
        FluidStack nutrient = recipe.nutrient();
        if (!nutrient.isEmpty()) {
            if (!summary.isEmpty()) summary.append(" + ");
            summary.append(nutrient.getHoverName().getString()).append(' ').append(nutrient.getAmount()).append("mB");
        }
        FluidStack output = recipe.output();
        summary.append(" → ").append(output.getHoverName().getString()).append(' ').append(output.getAmount()).append("mB");
        lines.add(Component.literal(summary.toString()));
        lines.add(Component.translatable("jdte.screen.life_synthesis_vat.recipe_stats",
                recipe.energy(), recipe.processTicks()));
        return lines;
    }

    private int tankAmount(int index) {
        return switch (index) {
            case 0 -> vatContainer.getNutrientFluid();
            case 1 -> vatContainer.getTimeFluid();
            default -> vatContainer.getLifeFluid();
        };
    }
}