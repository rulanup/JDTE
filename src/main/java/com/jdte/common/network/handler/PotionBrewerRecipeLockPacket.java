package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.network.data.PotionBrewerRecipeLockPayload;
import com.jdte.common.network.data.PotionBrewerRecipeLockSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PotionBrewerRecipeLockPacket {
    private static final PotionBrewerRecipeLockPacket INSTANCE = new PotionBrewerRecipeLockPacket();

    public static PotionBrewerRecipeLockPacket get() {
        return INSTANCE;
    }

    public void handleServer(PotionBrewerRecipeLockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (!(container instanceof BaseMachineContainer machineContainer) || !(machineContainer.baseMachineBE instanceof AdvancedPotionBrewerBE brewer)) {
                return;
            }
            if (!brewer.getBlockPos().equals(payload.blockPos())) {
                return;
            }
            if (!payload.request()) {
                brewer.setRecipeLocked(payload.locked());
            }
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new PotionBrewerRecipeLockSyncPayload(
                        brewer.getBlockPos(),
                        brewer.isRecipeLocked(),
                        brewer.copyLockedRecipeTemplates()));
            }
        });
    }

    public void handleClient(PotionBrewerRecipeLockSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.jdte.client.PotionBrewerRecipeLockClientCache.set(
                payload.blockPos(),
                payload.locked(),
                payload.templates()));
    }
}
