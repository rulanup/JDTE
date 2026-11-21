package com.jdte.datagen;
import net.neoforged.neoforge.data.event.GatherDataEvent;
public final class JDTEDataGenerators {
    private JDTEDataGenerators() {}
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(JDTEFumoModelProvider::new);
        event.createProvider(JDTEFumoLootTableProvider::new);
    }

    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(JDTEFumoLootTableProvider::new);
    }
}
