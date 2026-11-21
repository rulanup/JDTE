package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreativeEnergyCellTest {
    @Test
    void fillsPastTheReceiversSingleTransferLimit() {
        var receiver = new SimpleEnergyHandler(100_000, 1_000, 0);
        CreativeEnergyCellBE.charge(receiver);
        assertEquals(100_000, receiver.getAmountAsLong());
    }

    @Test
    void forwardingPortWithNoLocalCapacityReceivesOneFullOffer() {
        var receiver = new SimpleEnergyHandler(0) {
            long forwarded;

            @Override
            public int insert(int amount, TransactionContext transaction) {
                forwarded += amount;
                return amount;
            }
        };
        CreativeEnergyCellBE.charge(receiver);
        assertEquals(Integer.MAX_VALUE, receiver.forwarded);
    }
}
