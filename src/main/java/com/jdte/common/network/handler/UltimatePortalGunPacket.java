package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.network.PacketContext;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Validates and applies Ultimate Portal Gun edits on the server. */
public final class UltimatePortalGunPacket {
    private UltimatePortalGunPacket() {
    }

    public static void handle(UltimatePortalGunPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack stack = UltimatePortalGunItem.find(player);
            if (stack.isEmpty()) {
                return;
            }
            switch (payload.action()) {
                case UltimatePortalGunPayload.ACTION_SELECT -> {
                    int size = UltimatePortalGunItem.getDestinations(stack).size();
                    UltimatePortalGunItem.setFavoritePosition(stack,
                            Math.max(0, Math.min(payload.position(), Math.max(0, size - 1))));
                    PortalGunV2.setStayOpen(stack, payload.staysOpen());
                }
                case UltimatePortalGunPayload.ACTION_ADD_POSITION -> {
                    int position = payload.position() >= 0 ? payload.position()
                            : UltimatePortalGunItem.getFavoritePosition(stack);
                    Direction facing = MiscHelpers.getFacingDirection(player);
                    if (facing == Direction.DOWN) {
                        facing = Direction.NORTH;
                    }
                    UltimatePortalGunItem.fillDestination(stack, position,
                            new NBTHelpers.PortalDestination(player.level().dimension(), player.position(), facing,
                                    payload.name().isEmpty() ? "UNNAMED" : payload.name()));
                    UltimatePortalGunItem.setFavoritePosition(stack, position);
                }
                case UltimatePortalGunPayload.ACTION_REMOVE ->
                        UltimatePortalGunItem.clearDestination(stack, payload.position());
                case UltimatePortalGunPayload.ACTION_EDIT -> edit(player, stack, payload);
                default -> {
                }
            }
        });
    }

    private static void edit(ServerPlayer player, ItemStack stack, UltimatePortalGunPayload payload) {
        if (payload.dimension() == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(payload.dimension());
        if (id == null) {
            return;
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, id);
        if (player.server.getLevel(dimension) == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.jdte.ultimate_portal_gun.dimension_missing"), true);
            return;
        }
        NBTHelpers.PortalDestination existing = getExisting(stack, payload.position());
        Direction facing = existing == null ? MiscHelpers.getFacingDirection(player) : existing.facing();
        if (facing == Direction.DOWN) {
            facing = Direction.NORTH;
        }
        UltimatePortalGunItem.setDestination(stack, payload.position(), new NBTHelpers.PortalDestination(
                dimension, new Vec3(payload.x(), payload.y(), payload.z()), facing,
                payload.name().isEmpty() ? "UNNAMED" : payload.name()));
    }

    private static NBTHelpers.PortalDestination getExisting(ItemStack stack, int position) {
        var destinations = UltimatePortalGunItem.getDestinations(stack);
        return position >= 0 && position < destinations.size() ? destinations.get(position) : null;
    }
}
