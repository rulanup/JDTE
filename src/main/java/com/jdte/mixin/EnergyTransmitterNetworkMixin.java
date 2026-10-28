package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.jdte.common.blockentities.TransmitterNetworkEnergy;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Correct Forge 1.1.4 accounting/persistence gaps for native peers in the same transmitter networks. */
@Mixin(value=EnergyTransmitterBE.class,remap=false)
public abstract class EnergyTransmitterNetworkMixin {
    @Inject(method="getTransmitterHandler",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$currentPeer(net.minecraft.core.BlockPos pos,CallbackInfoReturnable<net.minecraftforge.energy.IEnergyStorage> cir){cir.setReturnValue(TransmitterNetworkEnergy.handler((EnergyTransmitterBE)(Object)this,pos));}
    @Inject(method="getTotalEnergyStored",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$total(CallbackInfoReturnable<Integer> cir){cir.setReturnValue(TransmitterNetworkEnergy.total((EnergyTransmitterBE)(Object)this,false));}
    @Inject(method="getTotalMaxEnergyStored",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$capacity(CallbackInfoReturnable<Integer> cir){cir.setReturnValue(TransmitterNetworkEnergy.total((EnergyTransmitterBE)(Object)this,true));}
    @Inject(method="balanceEnergy",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$balance(CallbackInfo ci){TransmitterNetworkEnergy.balance((EnergyTransmitterBE)(Object)this);ci.cancel();}
    @Inject(method={"saveAdditional","m_183515_"},at=@At("TAIL"),remap=false)
    private void jdte$saveOwned(CompoundTag tag,CallbackInfo ci){tag.putInt("energy",((EnergyTransmitterBE)(Object)this).getEnergyStorage().getRealEnergyStored());}
    @Inject(method={"load","m_142466_"},at=@At("TAIL"),remap=false)
    private void jdte$loadOwned(CompoundTag tag,CallbackInfo ci){var owner=(EnergyTransmitterBE)(Object)this;if(tag.contains("energy",Tag.TAG_INT))owner.getEnergyStorage().setEnergy(Math.max(0,Math.min(owner.getMaxEnergy(),tag.getInt("energy"))));}
}
