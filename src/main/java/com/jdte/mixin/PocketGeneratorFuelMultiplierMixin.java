package com.jdte.mixin;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Pinned source also resolves large-canister fuel speed in the original native generator. */
@Mixin(value=PocketGenerator.class,remap=false)
public abstract class PocketGeneratorFuelMultiplierMixin {
    @Inject(method="forceReceiveEnergy",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$internalGeneration(net.minecraftforge.energy.IEnergyStorage storage,int amount,boolean simulate,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
        if(storage instanceof com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemStackNoReceive nativeStorage)
            cir.setReturnValue(com.jdte.common.items.NativePocketGeneratorEnergy.generate(nativeStorage,amount,simulate));
    }
    @Redirect(method="initBurn",at=@At(value="INVOKE",target="Lcom/direwolf20/justdirethings/common/items/FuelCanister;getBurnSpeedMultiplier(Lnet/minecraft/world/item/ItemStack;)I"),remap=false)
    private int jdte$resolvedFuel(ItemStack fuel) { return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuel); }
}
