package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.item.ItemResource;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GelGeneratorInputValidationTest implements com.jdte.test.WorldRegistryTest {
    @Test
    void gelBlocksRemainPotentialConversionInputsForDynamicRecipes() {
        AdvancedGelGeneratorBE generator = new AdvancedGelGeneratorBE(
                BlockPos.ZERO, JDTEBlocks.ADVANCED_GEL_GENERATOR.get().defaultBlockState());
        ItemStack gel = new ItemStack(JDTRegistration.GooBlock_Tier1_ITEM.get());

        assertTrue(generator.getMachineHandler().isValid(GelGeneratorBE.INPUT_START_SLOT, ItemResource.of(gel)));
    }
}
