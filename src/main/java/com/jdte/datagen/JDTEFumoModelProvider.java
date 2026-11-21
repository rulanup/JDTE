package com.jdte.datagen;

import com.jdte.JDTE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public final class JDTEFumoModelProvider extends ModelProvider {
    public JDTEFumoModelProvider(PackOutput output) {
        super(output, JDTE.MODID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(JDTEBlocks.XINGYE_FUMO, JDTEBlocks.CAINIAOYO_FUMO, JDTEBlocks.MAIQILIN_FUMO);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(JDTEItems.XINGYE_FUMO, JDTEItems.CAINIAOYO_FUMO, JDTEItems.MAIQILIN_FUMO);
    }

    @Override
    protected void registerModels(BlockModelGenerators blocks, ItemModelGenerators items) {
        getKnownBlocks().map(Holder::value).forEach(block -> {
            var model = ModelLocationUtils.getModelLocation(block);
            blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                    BlockModelGenerators.plainVariant(model)).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
            items.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
        });
    }
}
