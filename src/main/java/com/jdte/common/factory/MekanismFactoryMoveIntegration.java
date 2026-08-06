package com.jdte.common.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Keeps Factory Packer's generic move path functional while the 1.20.1
 * Mekanism fission/transmitter APIs are rebound.
 */
public final class MekanismFactoryMoveIntegration {
    private MekanismFactoryMoveIntegration() {
    }

    public static ReactorCheck validate(BlockEntity blockEntity, BlockPos selectionMin, BlockPos selectionMax) {
        return new ReactorCheck(false, true, BlockPos.ZERO, BlockPos.ZERO);
    }

    public static void quiesce(BlockEntity blockEntity) {
    }

    public static boolean isQuiesced(BlockEntity blockEntity) {
        return true;
    }

    public static void preserveTransmitterContents(BlockEntity blockEntity) {
    }

    public static RemovalDiagnostics captureRemovalDiagnostics(ServerLevel level, BlockPos pos,
                                                               BlockEntity blockEntity) {
        return new RemovalDiagnostics(false, "", "", false, "", "", 0);
    }

    public static double currentRadiation(ServerLevel level, BlockPos pos) {
        return 0.0D;
    }

    public static void rotateMoveData(CompoundTag data, Rotation rotation) {
        if (!data.contains("connection", Tag.TAG_INT_ARRAY)) {
            return;
        }
        int[] connections = data.getIntArray("connection");
        if (connections.length != Direction.values().length) {
            return;
        }
        int[] rotated = new int[connections.length];
        for (Direction direction : Direction.values()) {
            rotated[rotation.rotate(direction).get3DDataValue()] = connections[direction.get3DDataValue()];
        }
        data.putIntArray("connection", rotated);
        data.remove("connections");
        data.remove("acceptors");
    }

    public record ReactorCheck(boolean reactor, boolean complete, BlockPos min, BlockPos max) {
    }

    public record RemovalDiagnostics(boolean relevant, String blockId, String blockEntityClass,
                                     boolean shouldDumpRadiation, String radioactiveContents,
                                     String boundingMain, double radiationLevel) {
        public String describe(String stage, double currentRadiation) {
            return "stage=" + stage + " block=" + blockId + " blockEntity=" + blockEntityClass
                    + " shouldDump=" + shouldDumpRadiation + " radioactive=" + radioactiveContents
                    + " boundingMain=" + boundingMain + " radiationBefore=" + radiationLevel
                    + " radiationNow=" + currentRadiation + " radiationDelta="
                    + (currentRadiation - radiationLevel);
        }
    }
}
