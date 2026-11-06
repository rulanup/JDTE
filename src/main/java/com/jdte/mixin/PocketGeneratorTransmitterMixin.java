package com.jdte.mixin;
import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.jdte.common.items.LargePocketGeneratorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Forge native code casts item storage to unrelated machine storage before calling tryBurn. */
@Mixin(value=EnergyTransmitterBE.class,remap=false)
public abstract class PocketGeneratorTransmitterMixin {
    @Inject(method="drainFromSlot",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$largePocketSource(CallbackInfo ci) {
        var machine=(EnergyTransmitterBE)(Object)this; var stack=machine.getMachineHandler().getStackInSlot(0);
        if (!(stack.getItem() instanceof LargePocketGeneratorItem generator)) return;
        if (machine.getLevel()!=null && !machine.getLevel().isClientSide && !machine.isRemoved() && machine.getLevel().hasChunkAt(machine.getBlockPos())) {
            var energy=LargePocketGeneratorItem.energy(stack); generator.tryBurn(energy,stack);
            energy.transferTo(machine.getEnergyStorage(),machine.fePerTick());
        }
        ci.cancel();
    }
}
