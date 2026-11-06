package com.jdte.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends ScreenMixin {
    @org.spongepowered.asm.mixin.injection.Inject(method = "renderSlot", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void jdte$renderMineralOutputCounts(net.minecraft.client.gui.GuiGraphics graphics, net.minecraft.world.inventory.Slot slot,
                                               org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if ((Object) this instanceof com.jdte.client.screens.MineralExtractorScreen screen
                && screen.getMenu().isOutputSlot(slot) && slot.getItem().getCount() > 64) {
            com.jdte.client.screens.util.GreenhouseSlotCountRenderer.render(graphics, font, slot.getItem(), slot, imageWidth,
                    com.jdte.common.utils.StackCountLabels.compact(slot.getItem().getCount()));
            ci.cancel();
        }
    }

    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Shadow public abstract int getGuiLeft();
    @Shadow public abstract int getGuiTop();
}
