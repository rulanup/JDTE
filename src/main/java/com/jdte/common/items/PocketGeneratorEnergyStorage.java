package com.jdte.common.items;

import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemStackNoReceive;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

/** Native-compatible type, but reads the owning stack on every access rather than caching stale FE. */
public final class PocketGeneratorEnergyStorage extends EnergyStorageItemStackNoReceive {
    private final ItemStack owner;
    public PocketGeneratorEnergyStorage(ItemStack owner) { super(owner, LargePocketGeneratorItem.capacity()); this.owner = owner; }
    @Override public int getMaxEnergyStored() { return LargePocketGeneratorItem.capacity(); }
    @Override public int getEnergyStored() { return PocketGeneratorMath.bounded(owner.hasTag() ? owner.getTag().getInt("Energy") : 0, getMaxEnergyStored()); }
    @Override public boolean canExtract() { return true; }
    @Override public int receiveEnergy(int amount, boolean simulate) { return 0; }
    @Override public int forceReceiveEnergy(int amount, boolean simulate) {
        if (!LargePocketGeneratorItem.canMutate(owner)) return 0;
        int accepted = Math.max(0, Math.min(amount, getMaxEnergyStored() - getEnergyStored()));
        if (!simulate && accepted > 0) owner.getOrCreateTag().putInt("Energy", getEnergyStored() + accepted);
        return accepted;
    }
    @Override public int extractEnergy(int amount, boolean simulate) {
        if (!LargePocketGeneratorItem.canMutate(owner)) return 0;
        int extracted = Math.max(0, Math.min(amount, getEnergyStored()));
        if (!simulate && extracted > 0) owner.getOrCreateTag().putInt("Energy", getEnergyStored() - extracted);
        return extracted;
    }
    /** Reserve the available FE, then refund a compliant receiver's smaller actual acceptance. */
    public int transferTo(IEnergyStorage target, int limit) {
        if (target == this || !target.canReceive() || limit <= 0) return 0;
        int offered = Math.min(Math.max(0, limit), getEnergyStored());
        int planned = Math.max(0, Math.min(offered, target.receiveEnergy(offered, true)));
        int debit = extractEnergy(planned, false);
        if (debit == 0) return 0;
        int accepted;
        try { accepted = target.receiveEnergy(debit, false); }
        catch (RuntimeException failure) { forceReceiveEnergy(debit, false); throw failure; }
        if (accepted < 0 || accepted > debit) throw new IllegalStateException("FE receiver violated its acceptance contract");
        forceReceiveEnergy(debit - accepted, false);
        return accepted;
    }
}
