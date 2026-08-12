package com.jdte.common.solar;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolarEnergyTransferTest {
    @Test
    void finiteSourcePaysOnlyWhatTheReceiverActuallyAccepts() {
        SolarPanelEnergyStorage source = SolarPanelEnergyStorage.finite(1_000);
        source.addGeneratedEnergy(1_000);
        RecordingReceiver receiver = new RecordingReceiver(250, 250);

        SolarEnergyTransfer.Result result = SolarEnergyTransfer.push(source, receiver, 600);

        assertEquals(250, result.moved());
        assertEquals(750, source.getEnergyStored());
        assertEquals(1, receiver.simulateCalls);
        assertEquals(1, receiver.executeCalls);
    }

    @Test
    void executeShortfallDoesNotOverdrawTheSource() {
        SolarPanelEnergyStorage source = SolarPanelEnergyStorage.finite(1_000);
        source.addGeneratedEnergy(1_000);
        RecordingReceiver receiver = new RecordingReceiver(500, 100);

        SolarEnergyTransfer.Result result = SolarEnergyTransfer.push(source, receiver, 500);

        assertEquals(100, result.moved());
        assertEquals(900, source.getEnergyStored());
    }

    @Test
    void creativeSourceTransfersOneBoundedRequestWithoutLooping() {
        SolarPanelEnergyStorage source = SolarPanelEnergyStorage.creative();
        RecordingReceiver receiver = new RecordingReceiver(Integer.MAX_VALUE, Integer.MAX_VALUE);

        SolarEnergyTransfer.Result result = SolarEnergyTransfer.push(source, receiver, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, result.moved());
        assertEquals(1, receiver.simulateCalls);
        assertEquals(1, receiver.executeCalls);
        assertEquals(Integer.MAX_VALUE, source.getEnergyStored());
    }

    private static final class RecordingReceiver implements IEnergyStorage {
        private final int simulatedAcceptance;
        private final int actualAcceptance;
        private int simulateCalls;
        private int executeCalls;

        private RecordingReceiver(int simulatedAcceptance, int actualAcceptance) {
            this.simulatedAcceptance = simulatedAcceptance;
            this.actualAcceptance = actualAcceptance;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (simulate) {
                simulateCalls++;
                return Math.min(maxReceive, simulatedAcceptance);
            }
            executeCalls++;
            return Math.min(maxReceive, actualAcceptance);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override
        public int getEnergyStored() { return 0; }
        @Override
        public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override
        public boolean canExtract() { return false; }
        @Override
        public boolean canReceive() { return true; }
    }
}
