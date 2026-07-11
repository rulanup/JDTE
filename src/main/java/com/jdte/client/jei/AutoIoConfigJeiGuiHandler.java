package com.jdte.client.jei;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.client.AutoIoConfigScreenBridge;
import com.jdte.client.screens.AdvancedPotionBrewerScreen;
import com.jdte.client.screens.GelGeneratorScreen;
import com.jdte.client.screens.InfusionMachineScreen;
import com.jdte.client.screens.LootFabricatorScreen;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.jei.gelgenerator.GelGeneratorRecipeCategory;
import com.jdte.common.jei.infusion.InfusionRecipeCategory;
import com.jdte.common.jei.potionbrewer.PotionBrewerRecipeCategory;
import com.jdte.common.jei.lootfabricator.LootFabricatorRecipeCategory;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AutoIoConfigJeiGuiHandler implements IGuiContainerHandler<BaseMachineScreen<?>> {
    private static final int INFUSION_PROGRESS_X = 79;
    private static final int INFUSION_PROGRESS_Y = 37;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;

    @Override
    public List<Rect2i> getGuiExtraAreas(BaseMachineScreen<?> containerScreen) {
        if (containerScreen instanceof AutoIoConfigScreenBridge bridge) {
            return bridge.jdte$getAutoIoConfigExtraAreas();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(BaseMachineScreen<?> containerScreen, double guiMouseX, double guiMouseY) {
        if (containerScreen instanceof InfusionMachineScreen<?>) {
            return List.of(IGuiClickableArea.createBasic(
                    INFUSION_PROGRESS_X,
                    INFUSION_PROGRESS_Y,
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT,
                    InfusionRecipeCategory.RECIPE_TYPE));
        }
        if (containerScreen instanceof GelGeneratorScreen<?>) {
            GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();
            return List.of(IGuiClickableArea.createBasic(
                    config.getGelGenProgressArrowX(),
                    config.getGelGenProgressArrowY(),
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT,
                    GelGeneratorRecipeCategory.RECIPE_TYPE));
        }
        if (containerScreen instanceof AdvancedPotionBrewerScreen) {
            GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();
            return List.of(IGuiClickableArea.createBasic(
                    config.getPotionBrewerArrowX(),
                    config.getPotionBrewerArrowBottomY() - 28,
                    9,
                    28,
                    PotionBrewerRecipeCategory.RECIPE_TYPE));
        }
        if (containerScreen instanceof LootFabricatorScreen) {
            GuiUpgradeLayoutConfig config = GuiUpgradeLayoutConfig.getInstance();
            return List.of(IGuiClickableArea.createBasic(
                    config.getLootFabricatorProgressArrowX(),
                    config.getLootFabricatorProgressArrowY(),
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT,
                    LootFabricatorRecipeCategory.RECIPE_TYPE));
        }
        return Collections.emptyList();
    }
}
