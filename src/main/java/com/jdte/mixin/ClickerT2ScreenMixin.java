package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.ClickerT2Screen;
import com.direwolf20.justdirethings.common.containers.ClickerT2Container;
import com.jdte.common.blockentities.ExtendedClickerBE;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickerT2Screen.class)
public abstract class ClickerT2ScreenMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdte$loadExtendedClickerSettings(ClickerT2Container container, Inventory inventory,
                                                   Component title, CallbackInfo ci) {
        ClickerT2Screen screen = (ClickerT2Screen) (Object) this;
        if (((BaseMachineScreenAccessor) screen).jdte$getBaseMachineBE() instanceof ExtendedClickerBE clicker) {
            screen.clickType = clicker.clickType;
            screen.clickTarget = clicker.clickTarget.ordinal();
            screen.sneaking = clicker.sneaking;
            screen.showFakePlayer = clicker.showFakePlayer;
            screen.maxHoldTicks = clicker.maxHoldTicks;
        }
    }
}
