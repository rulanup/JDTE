package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.GeneratorT1Screen;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GeneratorT1Screen.class)
public abstract class GeneratorT1ScreenFuelTooltipMixin {
    @Redirect(
            method = "renderTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/direwolf20/justdirethings/common/items/FuelCanister;getBurnSpeedMultiplier(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int jdte$useResolvedFuelMultiplier(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }
}
