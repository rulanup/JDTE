package com.jdte.datagen;
import com.jdte.JDTE;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
public final class JDTEDataGenerators {
    private JDTEDataGenerators() {}
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new JDTEFumoBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new JDTEFumoItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(),
                new JDTEFumoLootTableProvider(output, event.getLookupProvider()));
    }
}