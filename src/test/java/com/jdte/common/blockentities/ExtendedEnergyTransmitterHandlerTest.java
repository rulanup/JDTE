package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExtendedEnergyTransmitterHandlerTest {
    @Test
    void ownPositionResolvesTheExtendedTransmitterStorage() {
        TransmitterEnergyStorage expected = new TransmitterEnergyStorage(1_000, null);
        ExtendedEnergyTransmitterBE transmitter = transmitterWithStorage(expected);

        assertSame(expected, transmitter.getTransmitterHandler(BlockPos.ZERO));
    }

    @Test
    void removedTransmitterTargetReturnsNullInsteadOfDereferencingAJdtCacheMiss() throws Exception {
        ExtendedEnergyTransmitterBE transmitter = transmitterWithStorage(
                new TransmitterEnergyStorage(1_000, null));
        transmitter.setLevel(missingTargetLevel());

        assertDoesNotThrow(() -> assertNull(transmitter.getTransmitterHandler(new BlockPos(1, 0, 0))));
    }

    private static ExtendedEnergyTransmitterBE transmitterWithStorage(TransmitterEnergyStorage storage) {
        return new ExtendedEnergyTransmitterBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get().defaultBlockState()) {
            @Override
            public TransmitterEnergyStorage getEnergyStorage() {
                return storage;
            }
        };
    }

    private static MissingTargetLevel missingTargetLevel() throws ReflectiveOperationException {
        var field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var unsafe = (sun.misc.Unsafe) field.get(null);
        return (MissingTargetLevel) unsafe.allocateInstance(MissingTargetLevel.class);
    }

    /** Models a target removed after JDT's transmitter-position scan. */
    private static final class MissingTargetLevel extends ServerLevel {
        private MissingTargetLevel() {
            super(null, null, null, null, null, null, null, false, 0, java.util.List.of(), false, null);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return Blocks.AIR.defaultBlockState();
        }
    }
}
