package com.jdte.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow protected Font font;
    @Shadow protected int width;
    @Shadow protected int height;
}
