package com.jdte.common.items;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedUpgradeItemTest {
    @Test
    void mapsAdvancedGelGeneratorToExtendedGelGenerator() {
        assertEquals(JDTEBlocks.EXTENDED_GEL_GENERATOR.get(),
                ExtendedUpgradeItem.targetFor(JDTEBlocks.ADVANCED_GEL_GENERATOR.get()));
    }

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

    @Test
    void replacementValidationAcceptsOnlyTheExpectedLiveBlockEntity() {
        Block extendedBlock = JDTEBlocks.EXTENDED_GENERATOR.get();
        BlockStateAndEntity replacement = replacementFor(extendedBlock);

        assertTrue(ExtendedUpgradeItem.isValidReplacement(
                replacement.state(), extendedBlock, replacement.entity(), replacement.entity().getType()));
    }

    @Test
    void replacementValidationRejectsMissingRemovedWrongAndInvalidEntities() {
        Block extendedBlock = JDTEBlocks.EXTENDED_GENERATOR.get();
        BlockStateAndEntity replacement = replacementFor(extendedBlock);
        BlockEntity expectedEntity = replacement.entity();
        BlockEntity freshEntity = replacementFor(extendedBlock).entity();
        BlockEntity wrongBlockEntity = ((EntityBlock) Registration.GeneratorT1.get()).newBlockEntity(
                BlockPos.ZERO, Registration.GeneratorT1.get().defaultBlockState());

        assertFalse(ExtendedUpgradeItem.isValidReplacement(
                replacement.state(), extendedBlock, null, expectedEntity.getType()));

        expectedEntity.setRemoved();
        assertFalse(ExtendedUpgradeItem.isValidReplacement(
                replacement.state(), extendedBlock, expectedEntity, expectedEntity.getType()));

        assertFalse(ExtendedUpgradeItem.isValidReplacement(
                replacement.state(), extendedBlock, wrongBlockEntity, expectedEntity.getType()));
        assertFalse(ExtendedUpgradeItem.isValidReplacement(
                Registration.GeneratorT1.get().defaultBlockState(), extendedBlock,
                freshEntity, freshEntity.getType()));
    }

    @Test
    void targetStateCopiesFacingWhenBothStatesHaveIt() {
        BlockState source = Registration.ExperienceHolder.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.WEST);
        BlockState targetDefault = JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState();

        BlockState target = ExtendedUpgradeItem.targetStateWithCompatibleFacing(source, targetDefault);

        assertNotNull(target);
        assertEquals(Direction.WEST, target.getValue(BlockStateProperties.FACING));
    }

    @Test
    void targetStateKeepsDefaultWhenNeitherStateHasFacing() {
        BlockState source = Registration.GeneratorT1.get().defaultBlockState();
        BlockState targetDefault = JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState();

        BlockState target = ExtendedUpgradeItem.targetStateWithCompatibleFacing(source, targetDefault);

        assertSame(targetDefault, target);
    }

    @Test
    void targetStateRejectsAsymmetricFacingProperties() {
        BlockState facingSource = Registration.ExperienceHolder.get().defaultBlockState();
        BlockState facingTarget = JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState();
        BlockState noFacingSource = Registration.GeneratorT1.get().defaultBlockState();
        BlockState noFacingTarget = JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState();

        assertNull(ExtendedUpgradeItem.targetStateWithCompatibleFacing(facingSource, noFacingTarget));
        assertNull(ExtendedUpgradeItem.targetStateWithCompatibleFacing(noFacingSource, facingTarget));
    }

    @Test
    void useOnChecksClientAndUnsupportedSourceBeforeAnyWorldMutation() throws Exception {
        String source = readExtendedUpgradeItemSource();
        int clientBranch = source.indexOf("if (level.isClientSide)");
        int unsupportedBranch = source.indexOf("if (extendedBlock == null)");
        int firstMutation = source.indexOf("level.removeBlockEntity(pos)");

        assertTrue(unsupportedBranch >= 0);
        assertTrue(clientBranch > unsupportedBranch);
        assertTrue(firstMutation > clientBranch);
        assertTrue(source.contains("return InteractionResult.PASS;"));
        assertTrue(source.contains("return InteractionResult.SUCCESS;"));
    }

    @Test
    void useOnValidatesBeforeMutationAndOnlyConsumesAfterReplacementValidation() throws Exception {
        String source = readExtendedUpgradeItemSource();
        int oldEntityGuard = source.indexOf("if (oldBE == null");
        int firstMutation = source.indexOf("level.removeBlockEntity(pos)");
        int replacementValidation = source.indexOf("if (!isValidReplacement(");
        int shrink = source.indexOf("context.getItemInHand().shrink(1);");

        assertTrue(oldEntityGuard >= 0);
        assertTrue(firstMutation > oldEntityGuard);
        assertTrue(replacementValidation > firstMutation);
        assertTrue(shrink > replacementValidation);
        assertTrue(source.contains("oldBE.getType().isValid(state)"));
        assertTrue(source.contains("expectedBE.getType().isValid(extendedState)"));
    }

    @Test
    void useOnRestoresOnSetFailureAndExceptionsAndLoadsCustomDataOnlyAfterValidation() throws Exception {
        String source = readExtendedUpgradeItemSource();
        int setFailure = source.indexOf("if (!level.setBlock(pos, extendedState, Block.UPDATE_ALL))");
        int firstRestore = source.indexOf("restoreOriginal(level, pos, state, data, oldBE)", setFailure);
        int catchRestore = source.indexOf("restoreOriginal(level, pos, state, data, oldBE)", firstRestore + 1);
        int replacementLoad = source.indexOf("newBE.loadCustomOnly(data, level.registryAccess())");
        int replacementGuard = source.indexOf("if (!isValidReplacement(");
        int restoreLoad = source.indexOf("restoredBE.loadCustomOnly(data, level.registryAccess())");
        int shrink = source.indexOf("context.getItemInHand().shrink(1);");

        assertTrue(setFailure >= 0);
        assertTrue(firstRestore > setFailure);
        assertTrue(catchRestore > firstRestore);
        assertTrue(replacementLoad > replacementGuard);
        assertTrue(restoreLoad > catchRestore);
        assertTrue(shrink > replacementLoad);
        assertTrue(source.contains("return InteractionResult.FAIL;"));
        assertTrue(source.contains("// Best-effort rollback"));
    }

    private static BlockStateAndEntity replacementFor(Block block) {
        BlockState state = block.defaultBlockState();
        BlockEntity entity = ((EntityBlock) block).newBlockEntity(BlockPos.ZERO, state);
        assertNotNull(entity);
        return new BlockStateAndEntity(state, entity);
    }

    private static String readExtendedUpgradeItemSource() throws Exception {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) {
            current = current.getParent();
        }
        assertNotNull(current, "Could not locate project root from test runtime path");
        return Files.readString(current.resolve(
                "src/main/java/com/jdte/common/items/ExtendedUpgradeItem.java"));
    }

    private record BlockStateAndEntity(BlockState state, BlockEntity entity) {
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
