package com.jdte.common.factory;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.fml.ModList;

public final class FactoryBlockEntityMoveSupport {
    private FactoryBlockEntityMoveSupport() {}

    private static boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(modId);
    }

    public static BlockState rotateState(BlockState state, Rotation rotation) {
        if (rotation == Rotation.NONE) return state;
        BlockState rotated;
        if (rotation != Rotation.NONE && isModLoaded("ae2")) {
            rotated = AE2FactoryMoveIntegration.rotateState(state, rotation);
        } else {
            rotated = state.rotate(rotation);
        }
        for (var property : state.getProperties()) {
            if (property instanceof DirectionProperty directionProperty) {
                var before = state.getValue(directionProperty);
                var after = rotated.getValue(directionProperty);
                var expected = rotation.rotate(before);
                if (after == before && directionProperty.getPossibleValues().contains(expected)) {
                    rotated = rotated.setValue(directionProperty, expected);
                }
            }
        }
        return rotated;
    }

    public static CompoundTag rotateMoveData(CompoundTag data, Rotation rotation) {
        CompoundTag rotated = data;
        if (rotation != Rotation.NONE && isModLoaded("ae2")) {
            rotated = AE2FactoryMoveIntegration.rotateMoveData(rotated, rotation);
        }
        if (rotation != Rotation.NONE && isModLoaded("mekanism")) {
            MekanismFactoryMoveIntegration.rotateMoveData(rotated, rotation);
        }
        if (rotation != Rotation.NONE && isModLoaded("integrateddynamics")) {
            IntegratedDynamicsFactoryMoveIntegration.rotateMoveData(rotated, rotation);
        }
        return rotated;
    }

    public static void applyRotation(BlockEntity blockEntity, Rotation rotation) {
        if (rotation == Rotation.NONE || !(blockEntity instanceof AreaAffectingBE areaMachine)) return;
        AreaAffectingData data = areaMachine.getAreaAffectingData();
        double xRadius = data.xRadius;
        double zRadius = data.zRadius;
        int xOffset = data.xOffset;
        int zOffset = data.zOffset;

        switch (rotation) {
            case CLOCKWISE_90 -> areaMachine.setAreaSettings(zRadius, data.yRadius, xRadius,
                    -zOffset, data.yOffset, xOffset, data.renderArea);
            case CLOCKWISE_180 -> areaMachine.setAreaSettings(xRadius, data.yRadius, zRadius,
                    -xOffset, data.yOffset, -zOffset, data.renderArea);
            case COUNTERCLOCKWISE_90 -> areaMachine.setAreaSettings(zRadius, data.yRadius, xRadius,
                    zOffset, data.yOffset, -xOffset, data.renderArea);
            default -> { }
        }
    }

    public static CompoundTag beginMove(BlockEntity blockEntity) {
        if (JDTEConfig.COMMON.factoryPackerUseModMoveStrategies.get() && isModLoaded("ae2")) {
            return AE2FactoryMoveIntegration.beginMove(blockEntity);
        }
        return blockEntity.saveWithId();
    }

    public static MekanismFactoryMoveIntegration.ReactorCheck validateMekanismMove(BlockEntity blockEntity,
                                                                                   BlockPos selectionMin,
                                                                                   BlockPos selectionMax) {
        if (!isModLoaded("mekanismgenerators")) {
            return new MekanismFactoryMoveIntegration.ReactorCheck(false, true, BlockPos.ZERO, BlockPos.ZERO);
        }
        return MekanismFactoryMoveIntegration.validate(blockEntity, selectionMin, selectionMax);
    }

    public static void quiesceForMove(BlockEntity blockEntity) {
        if (isModLoaded("mekanismgenerators")) {
            MekanismFactoryMoveIntegration.quiesce(blockEntity);
        }
    }

    public static boolean isQuiescedForMove(BlockEntity blockEntity) {
        return !isModLoaded("mekanismgenerators")
                || MekanismFactoryMoveIntegration.isQuiesced(blockEntity);
    }

    public static void preserveMekanismTransmitterContents(BlockEntity blockEntity) {
        if (blockEntity != null && isModLoaded("mekanism")) {
            MekanismFactoryMoveIntegration.preserveTransmitterContents(blockEntity);
        }
    }

    public static MekanismFactoryMoveIntegration.RemovalDiagnostics captureMekanismRemovalDiagnostics(
            net.minecraft.server.level.ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
        if (!isModLoaded("mekanism")) {
            return new MekanismFactoryMoveIntegration.RemovalDiagnostics(false, "", "", false,
                    "", "", 0);
        }
        return MekanismFactoryMoveIntegration.captureRemovalDiagnostics(level, pos, blockEntity);
    }

    public static boolean isExpectedMultiblockTeardown(BlockState expected, BlockState current) {
        ResourceLocation expectedId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(expected.getBlock());
        if (expectedId.getNamespace().equals("mekanism") && expectedId.getPath().equals("bounding_block")) {
            return current.isAir();
        }
        if (expectedId.getNamespace().equals("immersiveengineering")) {
            return true;
        }
        return false;
    }

    public static void prepareRemoval(BlockEntity blockEntity) {
        if (blockEntity == null || !JDTEConfig.COMMON.factoryPackerUseModMoveStrategies.get()) return;
        if (isModLoaded("ae2cs")) {
            AE2CrystalScienceFactoryMoveIntegration.prepareRemoval(blockEntity);
        }
    }

    public static void prepareNetworkDetach(BlockEntity blockEntity) {
        if (blockEntity == null || !JDTEConfig.COMMON.factoryPackerUseModMoveStrategies.get()) return;
        if (isModLoaded("extendedae_plus")) {
            ExtendedAEPlusFactoryMoveIntegration.prepareNetworkDetach(blockEntity);
        }
    }

    public static void finalizeExternalMove(BlockEntity blockEntity, ResourceLocation sourceDimension,
                                            BlockPos sourcePos) {
        if (blockEntity == null || !JDTEConfig.COMMON.factoryPackerUseModMoveStrategies.get()) return;
        if (isModLoaded("ae2cs")) {
            AE2CrystalScienceFactoryMoveIntegration.finalizeMove(blockEntity, sourceDimension, sourcePos);
        }
    }

    public static boolean completeMove(BlockState state, CompoundTag data, Level level, BlockPos pos) {
        if (JDTEConfig.COMMON.factoryPackerUseModMoveStrategies.get() && isModLoaded("ae2")) {
            return AE2FactoryMoveIntegration.completeMove(state, data, level, pos);
        }
        BlockEntity blockEntity = BlockEntity.loadStatic(pos, state, data);
        if (blockEntity == null) return false;
        level.setBlockEntity(blockEntity);
        blockEntity.setChanged();
        return true;
    }
}
