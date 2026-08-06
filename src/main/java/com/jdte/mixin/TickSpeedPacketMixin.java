package com.jdte.mixin;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.common.network.handler.TickSpeedPacket;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = TickSpeedPacket.class, remap = false)
public class TickSpeedPacketMixin {
    @Inject(remap = false, method = "handle", at = @At("HEAD"), cancellable = true)
    private static void jdte$lockDelay(TickSpeedPayload payload, Supplier<NetworkEvent.Context> context,
                                       CallbackInfo ci) {
        ServerPlayer sender = context.get().getSender();
        if (sender == null) {
            return;
        }
        AbstractContainerMenu container = sender.containerMenu;
        if (container instanceof BaseMachineContainer machineContainer
                && UpgradeHelper.usesLockedDelay(machineContainer.baseMachineBE)
                && (UpgradeHelper.hasOverclock(machineContainer.baseMachineBE) || UpgradeHelper.hasUndercLock(machineContainer.baseMachineBE))) {
            ci.cancel();
        }
    }
}
