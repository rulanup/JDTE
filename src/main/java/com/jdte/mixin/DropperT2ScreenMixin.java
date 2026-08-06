package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.DropperT2Screen;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DropperT2Screen.class, remap = false)
public abstract class DropperT2ScreenMixin {
    @Shadow protected int topSectionTop;

    @Shadow public abstract int getGuiLeft();

    /**
     * Forge's extended layout leaves the two dropper values in a vertical column.
     * Adjust the constructor arguments directly so later screen widget refreshes
     * cannot restore the original horizontal positions.
     */
    @ModifyArgs(
            remap = false,
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/direwolf20/justdirethings/client/screens/widgets/NumberButton;<init>(IIIIIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)V",
                    ordinal = 0))
    private void jdte$layoutDropCount(Args args) {
        args.set(0, getGuiLeft() + 20);
        args.set(1, topSectionTop + 25);
    }

    @ModifyArgs(
            remap = false,
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/direwolf20/justdirethings/client/screens/widgets/NumberButton;<init>(IIIIIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)V",
                    ordinal = 1))
    private void jdte$layoutPickupDelay(Args args) {
        args.set(0, getGuiLeft() + 20);
        args.set(1, topSectionTop + 41);
    }
}
