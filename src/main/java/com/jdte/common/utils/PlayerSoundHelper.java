package com.jdte.common.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class PlayerSoundHelper {
    private PlayerSoundHelper() {}

    public static void notify(Player player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                    source, player.getX(), player.getY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
        }
    }
}
