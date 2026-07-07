package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.client.AutoIoConfigClientCache;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.network.data.AutoIoConfigPayload;
import com.jdte.common.network.data.AutoIoConfigSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AutoIoConfigPacket {
    private static final AutoIoConfigPacket INSTANCE = new AutoIoConfigPacket();

    public static AutoIoConfigPacket get() {
        return INSTANCE;
    }

    public void handleServer(AutoIoConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (!(container instanceof BaseMachineContainer machineContainer)) {
                return;
            }
            BaseMachineBE machine = machineContainer.baseMachineBE;
            if (machine == null) {
                return;
            }
            if (!machine.getBlockPos().equals(payload.blockPos()) || !AutoIoConfigHelper.hasConfigurableIo(machine)) {
                return;
            }
            if (!payload.request()) {
                AutoIoConfigHelper.setSideMask(machine, payload.sideMask());
            }
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new AutoIoConfigSyncPayload(
                        machine.getBlockPos(),
                        AutoIoConfigHelper.getSideMask(machine)));
            }
        });
    }

    public void handleClient(AutoIoConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> AutoIoConfigClientCache.setSideMask(payload.blockPos(), payload.sideMask()));
    }
}
