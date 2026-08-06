package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.containers.FilterPageHolder;
import com.jdte.common.network.data.FilterPagePayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FilterPagePacket {
    private static final FilterPagePacket INSTANCE = new FilterPagePacket();

    public static FilterPagePacket get() {
        return INSTANCE;
    }

    public void handle(FilterPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container instanceof FilterPageHolder holder) {
                holder.jdte$setFilterPage(payload.page());
            }
        });
    }
}
