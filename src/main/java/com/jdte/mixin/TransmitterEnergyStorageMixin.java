package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage;
import com.jdte.common.blockentities.TransmitterNetworkEnergy;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native-originated traffic must debit the same actual buffers as JDTE-originated traffic. */
@Mixin(value=TransmitterEnergyStorage.class,remap=false)
public abstract class TransmitterEnergyStorageMixin {
    @Shadow @Final private EnergyTransmitterBE energyTransmitterBE;
    @Inject(method="receiveEnergy",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$receive(int n,boolean simulate,CallbackInfoReturnable<Integer> cir){cir.setReturnValue(TransmitterNetworkEnergy.transfer(energyTransmitterBE,n,simulate,true));}
    @Inject(method="extractEnergy",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$extract(int n,boolean simulate,CallbackInfoReturnable<Integer> cir){cir.setReturnValue(TransmitterNetworkEnergy.transfer(energyTransmitterBE,n,simulate,false));}
}
