package com.jdte.common.factory;

import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

public final class MekanismFactoryMoveIntegration {
    private MekanismFactoryMoveIntegration() {}

    public static ReactorCheck validate(BlockEntity blockEntity, BlockPos selectionMin, BlockPos selectionMax) {
        if (!(blockEntity instanceof TileEntityFissionReactorCasing casing)) return ReactorCheck.NOT_REACTOR;
        FissionReactorMultiblockData reactor = casing.getMultiblock();
        if (!reactor.isFormed()) return ReactorCheck.NOT_REACTOR;
        BlockPos min = reactor.getMinPos();
        BlockPos max = reactor.getMaxPos();
        return contains(selectionMin, selectionMax, min, max)
                ? ReactorCheck.COMPLETE_REACTOR
                : new ReactorCheck(true, false, min, max);
    }

    public static void quiesce(BlockEntity blockEntity) {
        if (blockEntity instanceof TileEntityFissionReactorCasing casing) {
            FissionReactorMultiblockData reactor = casing.getMultiblock();
            if (reactor.isFormed() && reactor.isActive()) casing.setReactorActive(false);
        }
    }

    public static boolean isQuiesced(BlockEntity blockEntity) {
        if (!(blockEntity instanceof TileEntityFissionReactorCasing casing)) return true;
        FissionReactorMultiblockData reactor = casing.getMultiblock();
        return !reactor.isFormed() || !reactor.isActive() && reactor.lastBurnRate <= 0;
    }

    public static void preserveTransmitterContents(BlockEntity blockEntity) {
        if (blockEntity instanceof TileEntityTransmitter transmitterTile) {
            transmitterTile.getTransmitter().validateAndTakeShare();
        }
    }

    public static RemovalDiagnostics captureRemovalDiagnostics(ServerLevel level, BlockPos pos,
                                                               BlockEntity blockEntity) {
        String blockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
        boolean mekanismBlock = blockId.startsWith("mekanism:") || blockId.startsWith("mekmm:");
        if (!mekanismBlock && blockEntity != null
                && !blockEntity.getClass().getName().startsWith("mekanism.")
                && !blockEntity.getClass().getName().startsWith("com.jerry.")) {
            return RemovalDiagnostics.IRRELEVANT;
        }

        String main = "none";
        BlockEntity chemicalSource = blockEntity;
        if (blockEntity instanceof TileEntityBoundingBlock bounding) {
            BlockPos mainPos = bounding.getMainPos();
            BlockEntity mainBlockEntity = level.getBlockEntity(mainPos);
            main = mainPos + "/" + BuiltInRegistries.BLOCK.getKey(level.getBlockState(mainPos).getBlock())
                    + "/" + (mainBlockEntity == null ? "null" : mainBlockEntity.getClass().getName());
            if (mainBlockEntity != null) chemicalSource = mainBlockEntity;
        }

        List<String> radioactive = new ArrayList<>();
        if (chemicalSource instanceof IMekanismChemicalHandler chemicalHandler) {
            for (IChemicalTank tank : chemicalHandler.getChemicalTanks(null)) {
                ChemicalStack stack = tank.getStack();
                if (!stack.isEmpty() && stack.isRadioactive()) {
                    radioactive.add(stack.getChemicalHolder().getRegisteredName() + "=" + stack.getAmount());
                }
            }
        }

        boolean shouldDump = chemicalSource instanceof TileEntityMekanism tile && tile.shouldDumpRadiation();
        double radiation = IRadiationManager.INSTANCE.getRadiationLevel(level, pos);
        String blockEntityClass = blockEntity == null ? "null" : blockEntity.getClass().getName();
        boolean relevant = !radioactive.isEmpty();
        return new RemovalDiagnostics(relevant, blockId, blockEntityClass, shouldDump,
                radioactive.isEmpty() ? "none" : String.join(",", radioactive), main, radiation);
    }

    public static void rotateMoveData(net.minecraft.nbt.CompoundTag data, Rotation rotation) {
        if (data.contains("connection", net.minecraft.nbt.Tag.TAG_INT_ARRAY)) {
            int[] connections = data.getIntArray("connection");
            if (connections.length == Direction.values().length) {
                int[] rotated = new int[connections.length];
                for (Direction direction : Direction.values()) {
                    rotated[rotation.rotate(direction).get3DDataValue()] = connections[direction.get3DDataValue()];
                }
                data.putIntArray("connection", rotated);
                data.remove("connections");
                data.remove("acceptors");
            }
        }
    }

    private static boolean contains(BlockPos selectionMin, BlockPos selectionMax, BlockPos min, BlockPos max) {
        return min.getX() >= selectionMin.getX() && min.getY() >= selectionMin.getY()
                && min.getZ() >= selectionMin.getZ() && max.getX() <= selectionMax.getX()
                && max.getY() <= selectionMax.getY() && max.getZ() <= selectionMax.getZ();
    }

    public record ReactorCheck(boolean reactor, boolean complete, BlockPos min, BlockPos max) {
        private static final ReactorCheck NOT_REACTOR = new ReactorCheck(false, true, BlockPos.ZERO, BlockPos.ZERO);
        private static final ReactorCheck COMPLETE_REACTOR = new ReactorCheck(true, true, BlockPos.ZERO, BlockPos.ZERO);
    }

    public record RemovalDiagnostics(boolean relevant, String blockId, String blockEntityClass,
                                     boolean shouldDumpRadiation, String radioactiveContents,
                                     String boundingMain, double radiationLevel) {
        private static final RemovalDiagnostics IRRELEVANT =
                new RemovalDiagnostics(false, "", "", false, "", "", 0);

        public String describe(String stage, double currentRadiation) {
            return "stage=" + stage + " block=" + blockId + " blockEntity=" + blockEntityClass
                    + " shouldDump=" + shouldDumpRadiation + " radioactive=" + radioactiveContents
                    + " boundingMain=" + boundingMain + " radiationBefore=" + radiationLevel
                    + " radiationNow=" + currentRadiation + " radiationDelta="
                    + (currentRadiation - radiationLevel);
        }
    }
}
