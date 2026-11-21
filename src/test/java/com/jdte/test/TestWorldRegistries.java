package com.jdte.test;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.WorldDataConfiguration;

public final class TestWorldRegistries {
    private static RegistryAccess.Frozen registries;
    private TestWorldRegistries() {}

    public static synchronized RegistryAccess.Frozen load() {
        if (registries != null) return registries;
        var packs = new WorldLoader.PackConfig(ServerPacksSource.createVanillaTrustedRepository(),
                WorldDataConfiguration.DEFAULT, true, true);
        var config = new WorldLoader.InitConfig(packs, Commands.CommandSelection.DEDICATED, PermissionSet.ALL_PERMISSIONS);
        registries = WorldLoader.load(config, context -> new WorldLoader.DataLoadOutput<>(null, context.datapackDimensions()),
                (resources, managers, layers, ignored) -> {
                    resources.close();
                    return layers.compositeAccess();
                }, Runnable::run, Runnable::run).join();
        return registries;
    }
}
