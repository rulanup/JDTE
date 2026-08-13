package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GelGeneratorInputValidationTest {
    @Test
    void gelBlocksRemainPotentialConversionInputsForDynamicRecipes() {
        AdvancedGelGeneratorBE generator = new AdvancedGelGeneratorBE(
                BlockPos.ZERO, JDTEBlocks.ADVANCED_GEL_GENERATOR.get().defaultBlockState());
        ItemStack gel = new ItemStack(Registration.GooBlock_Tier1_ITEM.get());

        assertTrue(generator.getMachineHandler().isItemValid(GelGeneratorBE.INPUT_START_SLOT, gel));
    }
}
