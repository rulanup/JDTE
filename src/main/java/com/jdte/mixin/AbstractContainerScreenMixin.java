package com.jdte.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends ScreenMixin {
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Shadow public abstract int getGuiLeft();
    @Shadow public abstract int getGuiTop();
}
