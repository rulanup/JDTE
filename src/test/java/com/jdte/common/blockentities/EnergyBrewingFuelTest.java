package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnergyBrewingFuelTest {
    private static class Buffer {
        int stored;
        boolean shortCommit;
        Buffer(int stored) { this.stored = stored; }
        int extract(int requested, boolean simulate) {
            int amount = Math.min(stored, requested);
            if (!simulate) {
                if (shortCommit) amount--;
                stored -= amount;
            }
            return amount;
        }
        boolean charge(int cost, boolean creative) {
            return EnergyBrewingFuel.tryCharge(cost, creative, () -> stored,
                    amount -> extract(amount, true), amount -> extract(amount, false),
                    previous -> stored = previous);
        }
    }
    @Test void paysExactlyOneConfiguredCharge() {
        var b = new Buffer(8000); assertTrue(b.charge(5000, false)); assertEquals(3000, b.stored);
    }
    @Test void insufficientChargeLeavesEveryFeUntouched() {
        var b = new Buffer(4999); assertFalse(b.charge(5000, false)); assertEquals(4999, b.stored);
    }
    @Test void zeroCostAllowsFuelWithoutEnergy() {
        var b = new Buffer(0); assertTrue(b.charge(0, false)); assertEquals(0, b.stored);
    }
    @Test void creativeDoesNotDebitTheBuffer() {
        var b = new Buffer(123); assertTrue(b.charge(5000, true)); assertEquals(123, b.stored);
    }
    @Test void unexpectedPartialCommitRestoresOwnedBuffer() {
        var b = new Buffer(8000); b.shortCommit = true;
        assertFalse(b.charge(5000, false)); assertEquals(8000, b.stored);
    }
}
