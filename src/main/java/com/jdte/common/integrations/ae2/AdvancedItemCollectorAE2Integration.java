package com.jdte.common.integrations.ae2;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class AdvancedItemCollectorAE2Integration {
    private AdvancedItemCollectorAE2Integration() {
    }

    /**
     * Atomically inserts a collected stack into ME storage. A non-empty result means the
     * complete stack was not accepted and the caller should use the normal item capability.
     */
    public static ItemStack insertCollectedStack(ItemStack stack, AdvancedItemCollectorBE collector,
                                                 boolean simulate) {
        if (!(collector.getLevel() instanceof ServerLevel level)) return stack;

        Direction facing = collector.getBlockState().getValue(BlockStateProperties.FACING);
        MEStorage storage = level.getCapability(
                AECapabilities.ME_STORAGE,
                collector.getBlockPos().relative(facing),
                facing.getOpposite());
        if (storage == null) return stack;

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) return stack;

        IActionSource actionSource = IActionSource.empty();
        long accepted = storage.insert(key, stack.getCount(), Actionable.SIMULATE, actionSource);
        if (accepted < stack.getCount()) return stack;
        if (simulate) return ItemStack.EMPTY;

        long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, actionSource);
        int remainder = stack.getCount() - (int) Math.min(stack.getCount(), Math.max(0L, inserted));
        return remainder == 0 ? ItemStack.EMPTY : stack.copyWithCount(remainder);
    }

}
