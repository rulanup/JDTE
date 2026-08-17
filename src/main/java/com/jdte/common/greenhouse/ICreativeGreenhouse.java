package com.jdte.common.greenhouse;

import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.item.ItemStack;

/**
 * Marker implemented by creative-greenhouse machines that live in another mod
 * (e.g. {@code jdte_matrix}). JDTE's upgrade, auto-I/O, machine-output and AE-output
 * systems recognise a creative greenhouse through this interface only, so they keep
 * working when the concrete implementation is provided by a dependent mod.
 */
public interface ICreativeGreenhouse {
    /** Number of seed-template input slots. */
    int inputSlots();

    /** First output slot index (immediately after the input slots). */
    int outputStartSlot();

    /** Number of distinct product types currently exposed by the catalog. */
    int distinctOutputTypes();

    /** Prototype stack (count 1) of the product at the given catalog entry, or empty. */
    ItemStack catalogPrototypeAt(int entry);

    /** Whether the machine supports the given JDTE upgrade card type. */
    boolean isSupportedUpgrade(UpgradeType type);
}
