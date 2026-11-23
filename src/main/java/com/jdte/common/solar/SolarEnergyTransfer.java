package com.jdte.common.solar;

import net.neoforged.neoforge.energy.IEnergyStorage;

public final class SolarEnergyTransfer {
    private SolarEnergyTransfer() {
    }

    public static Result push(SolarPanelEnergyStorage source, IEnergyStorage receiver, int offered) {
        if (receiver == null || !receiver.canReceive() || offered <= 0) {
            return new Result(0);
        }

        int available = source.extractEnergy(offered, true);
        if (available <= 0) {
            return new Result(0);
        }
        int simulated = receiver.receiveEnergy(available, true);
        if (simulated <= 0) {
            return new Result(0);
        }

        int reserved = source.extractEnergy(simulated, false);
        if (reserved <= 0) {
            return new Result(0);
        }
        int accepted = receiver.receiveEnergy(reserved, false);
        if (accepted < reserved) {
            source.addGeneratedEnergy(reserved - accepted);
        }
        return new Result(Math.max(0, accepted));
    }

    public record Result(int moved) {
    }
}
