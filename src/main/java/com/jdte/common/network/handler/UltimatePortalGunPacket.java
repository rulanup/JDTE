package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UltimatePortalGunPacket {
    private static final UltimatePortalGunPacket INSTANCE = new UltimatePortalGunPacket();

    public static UltimatePortalGunPacket get() {
        return INSTANCE;
    }

    public void handle(UltimatePortalGunPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack stack = UltimatePortalGunItem.find(player);
            if (stack.isEmpty()) {
                return;
            }
            switch (payload.action()) {
                case UltimatePortalGunPayload.ACTION_SELECT -> {
                    int size = UltimatePortalGunItem.getDestinations(stack).size();
                    int position = size == 0 ? 0 : Math.max(0, Math.min(payload.position(), size - 1));
                    PortalGunV2.setFavoritePosition(stack, position);
                    PortalGunV2.setStayOpen(stack, payload.staysOpen());
                }
                case UltimatePortalGunPayload.ACTION_ADD_POSITION -> {
                    Vec3 position = player.position();
                    Direction facing = MiscHelpers.getFacingDirection(player);
                    NBTHelpers.PortalDestination destination = new NBTHelpers.PortalDestination(
                            new NBTHelpers.GlobalVec3(player.level().dimension(), position),
                            facing == Direction.DOWN ? Direction.NORTH : facing,
                            payload.name().isEmpty() ? "UNNAMED" : payload.name());
                    int fillPosition = payload.position() >= 0 ? payload.position()
                            : PortalGunV2.getFavoritePosition(stack);
                    UltimatePortalGunItem.fillDestination(stack, fillPosition, destination);
                    PortalGunV2.setFavoritePosition(stack, fillPosition);
                }
                case UltimatePortalGunPayload.ACTION_REMOVE ->
                        UltimatePortalGunItem.clearDestination(stack, payload.position());
                case UltimatePortalGunPayload.ACTION_EDIT -> {
                    Vec3 position = new Vec3(payload.x(), payload.y(), payload.z());
                    NBTHelpers.PortalDestination existing = getExisting(stack, payload.position());
                    Direction facing = existing != null && !existing.equals(NBTHelpers.PortalDestination.EMPTY)
                            ? existing.direction() : MiscHelpers.getFacingDirection(player);
                    NBTHelpers.PortalDestination destination = new NBTHelpers.PortalDestination(
                            new NBTHelpers.GlobalVec3(payload.dimension(), position), facing,
                            payload.name().isEmpty() ? "UNNAMED" : payload.name());
                    UltimatePortalGunItem.setDestination(stack, payload.position(), destination);
                }
            }
        });
    }

    private static NBTHelpers.PortalDestination getExisting(ItemStack stack, int position) {
        java.util.List<NBTHelpers.PortalDestination> list = UltimatePortalGunItem.getDestinations(stack);
        if (position >= 0 && position < list.size()) {
            return list.get(position);
        }
        return null;
    }
}
