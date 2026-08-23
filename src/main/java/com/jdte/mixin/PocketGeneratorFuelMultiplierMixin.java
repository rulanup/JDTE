package com.jdte.mixin;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PocketGenerator.class)
public abstract class PocketGeneratorFuelMultiplierMixin {
    @Redirect(
            method = "initBurn",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/direwolf20/justdirethings/common/items/FuelCanister;getBurnSpeedMultiplier(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int jdte$useResolvedFuelMultiplier(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }
}
