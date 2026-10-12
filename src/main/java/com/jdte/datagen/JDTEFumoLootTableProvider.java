package com.jdte.datagen;

import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class JDTEFumoLootTableProvider extends LootTableProvider {
    public JDTEFumoLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(new SubProviderEntry(JDTEFumoBlockLoot::new, LootContextParamSets.BLOCK)), lookupProvider);
    }

    private static final class JDTEFumoBlockLoot extends BlockLootSubProvider {
        private JDTEFumoBlockLoot(HolderLookup.Provider lookupProvider) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    JDTEBlocks.XINGYE_FUMO.get(),
                    JDTEBlocks.CAINIAOYO_FUMO.get(),
                    JDTEBlocks.MAIQILIN_FUMO.get());
        }

        @Override
        protected void generate() {
            dropSelf(JDTEBlocks.XINGYE_FUMO.get());
            dropSelf(JDTEBlocks.CAINIAOYO_FUMO.get());
            dropSelf(JDTEBlocks.MAIQILIN_FUMO.get());
        }
    }
}