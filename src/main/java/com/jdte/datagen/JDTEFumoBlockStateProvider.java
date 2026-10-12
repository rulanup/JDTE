package com.jdte.datagen;
import com.jdte.JDTE;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
public class JDTEFumoBlockStateProvider extends BlockStateProvider {
    public JDTEFumoBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, JDTE.MODID, existingFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {
        registerFumo(JDTEBlocks.XINGYE_FUMO.get(), "xingye_fumo");
        registerFumo(JDTEBlocks.CAINIAOYO_FUMO.get(), "cainiaoyo_fumo");
        registerFumo(JDTEBlocks.MAIQILIN_FUMO.get(), "maiqilin_fumo");
    }
    private void registerFumo(Block block, String modelName) {
        horizontalBlock(block, models().getExistingFile(modLoc("block/" + modelName)));
    }
}