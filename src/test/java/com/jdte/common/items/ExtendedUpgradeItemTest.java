package com.jdte.common.items;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedUpgradeItemTest {
    @Test
    void mapsOnlyTheFourOrdinaryJdtSources() {
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.get(),
                ExtendedUpgradeItem.targetFor(Registration.GeneratorT1.get()));
        assertEquals(JDTEBlocks.EXTENDED_FLUID_GENERATOR.get(),
                ExtendedUpgradeItem.targetFor(Registration.GeneratorFluidT1.get()));
        assertEquals(JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get(),
                ExtendedUpgradeItem.targetFor(Registration.ExperienceHolder.get()));
        assertEquals(JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get(),
                ExtendedUpgradeItem.targetFor(Registration.EnergyTransmitter.get()));

        assertNull(ExtendedUpgradeItem.targetFor(Registration.ClickerT1.get()));
        assertNull(ExtendedUpgradeItem.targetFor(Registration.BlockPlacerT1.get()));
        assertNull(ExtendedUpgradeItem.targetFor(JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get()));
    }

    @Test
    void eachExtendedTargetBlockEntityTypeAcceptsOnlyItsOwnTargetBlock() {
        List<Block> targets = List.of(
                JDTEBlocks.EXTENDED_GENERATOR.get(),
                JDTEBlocks.EXTENDED_FLUID_GENERATOR.get(),
                JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get(),
                JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get(),
                Registration.GeneratorT1.get(),
                Registration.GeneratorFluidT1.get(),
                Registration.ExperienceHolder.get(),
                Registration.EnergyTransmitter.get(),
                Registration.ClickerT1.get(),
                Registration.BlockPlacerT1.get(),
                JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get());

        assertAcceptsOnly(JDTEBlockEntities.EXTENDED_GENERATOR.get(),
                JDTEBlocks.EXTENDED_GENERATOR.get(), targets);
        assertAcceptsOnly(JDTEBlockEntities.EXTENDED_FLUID_GENERATOR.get(),
                JDTEBlocks.EXTENDED_FLUID_GENERATOR.get(), targets);
        assertAcceptsOnly(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(),
                JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get(), targets);
        assertAcceptsOnly(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(),
                JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get(), targets);
    }

    private static void assertAcceptsOnly(BlockEntityType<?> type, Block ownBlock, List<Block> targets) {
        BlockEntity factoryResult = ((EntityBlock) ownBlock).newBlockEntity(
                BlockPos.ZERO, ownBlock.defaultBlockState());
        assertNotNull(factoryResult);
        assertSame(type, factoryResult.getType());
        assertTrue(type.isValid(ownBlock.defaultBlockState()));
        targets.stream()
                .filter(block -> block != ownBlock)
                .forEach(block -> assertFalse(type.isValid(block.defaultBlockState())));
    }
}
