package com.jdte.common.integrations.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Applied Flux bridge. This class is only loaded when appflux is present. */
final class AEExtractionEnergyIntegration {
    private static final long MAX = Integer.MAX_VALUE;
    private static final AEKey FE_KEY = FluxKey.of(EnergyType.FE);

    private AEExtractionEnergyIntegration() {
    }

    static void refill(MEStorage storage, IActionSource source, GlobalPos link, long gameTime,
                       ItemStack stack) {
        var energy = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
        if (energy == null) return;
        AEExtractionTransfer.Result result = AEExtractionTransfer.move(MAX,
                new AEExtractionTransfer.Source() {
                    @Override public long extract(long amount, boolean simulate) {
                        return storage.extract(FE_KEY, Math.min(MAX, amount),
                                simulate ? Actionable.SIMULATE : Actionable.MODULATE, source);
                    }
                    @Override public long restore(long amount) {
                        return storage.insert(FE_KEY, Math.min(MAX, amount), Actionable.MODULATE, source);
                    }
                },
                new AEExtractionTransfer.Sink() {
                    @Override public long insert(long amount, boolean simulate) {
                        try (var tx = Transaction.openRoot()) {
                            int accepted = energy.insert((int) Math.min(MAX, amount), tx);
                            if (!simulate) tx.commit();
                            return accepted;
                        }
                    }
                });
        if (result.unrestored() > 0) {
            AEExtractionNetworkIntegration.logRefundFailure(link, "fe", result.unrestored(), gameTime);
        }
    }
}
