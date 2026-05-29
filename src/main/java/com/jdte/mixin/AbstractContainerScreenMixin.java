package com.jdte.mixin;

import com.jdte.client.UpgradePopupDragHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends ScreenMixin {
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Shadow public abstract int getGuiLeft();
    @Shadow public abstract int getGuiTop();

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void jdte$mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof UpgradePopupDragHandler handler && handler.jdte$dragUpgradePopup(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void jdte$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof UpgradePopupDragHandler handler) {
            handler.jdte$releaseUpgradePopup(button);
        }
    }
}
