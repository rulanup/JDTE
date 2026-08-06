package com.jdte.mixin;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.common.network.handler.TickSpeedPacket;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TickSpeedPacket.class)
public class TickSpeedPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void jdte$lockDelay(TickSpeedPayload payload, IPayloadContext context, CallbackInfo ci) {
        Player sender = context.player();
        AbstractContainerMenu container = sender.containerMenu;
        if (container instanceof BaseMachineContainer machineContainer
                && UpgradeHelper.usesLockedDelay(machineContainer.baseMachineBE)
                && (UpgradeHelper.hasOverclock(machineContainer.baseMachineBE) || UpgradeHelper.hasUndercLock(machineContainer.baseMachineBE))) {
            ci.cancel();
        }
    }
}
