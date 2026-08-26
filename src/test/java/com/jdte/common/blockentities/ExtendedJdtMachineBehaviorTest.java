package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ExtendedJdtMachineBehaviorTest {
    @Test
    void extendedGeneratorUsesItsOwnTypeAndEightUpgradeSlots() {
        ExtendedGeneratorBE machine = new ExtendedGeneratorBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState());

        assertInstanceOf(GeneratorT1BE.class, machine);
        assertInstanceOf(ExtendedUpgradeMachine.class, machine);
        assertEquals(JDTEBlockEntities.EXTENDED_GENERATOR.get(), machine.getType());
        assertEquals(ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT,
                UpgradeHelper.getUpgradeHandler(machine).getSlots());

        ResourceLocation generatorId = ResourceLocation.fromNamespaceAndPath("jdte", "extended_generator");
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.get(), BuiltInRegistries.BLOCK.get(generatorId));
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.getId(), BuiltInRegistries.BLOCK.getKey(JDTEBlocks.EXTENDED_GENERATOR.get()));
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.getId(), BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.get(generatorId)));
    }
}
