package com.jdte.client.jei;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.client.AutoIoConfigScreenBridge;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collections;
import java.util.List;

public class AutoIoConfigJeiGuiHandler implements IGuiContainerHandler<BaseMachineScreen<?>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(BaseMachineScreen<?> containerScreen) {
        if (containerScreen instanceof AutoIoConfigScreenBridge bridge) {
            return bridge.jdte$getAutoIoConfigExtraAreas();
        }
        return Collections.emptyList();
    }
}
