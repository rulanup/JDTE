package com.jdte.common.integrations.ae2;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class AdvancedItemCollectorAE2Integration {
    private AdvancedItemCollectorAE2Integration() {
    }

    /**
     * @return the source remainder, or {@code null} when the target has no ME storage capability
     */
    public static ItemStack tryTransfer(IItemHandlerModifiable source, int slot, ItemStack stack,
                                        AdvancedItemCollectorBE collector, Player player) {
        if (!(collector.getLevel() instanceof ServerLevel level)) return null;

        Direction facing = collector.getBlockState().getValue(BlockStateProperties.FACING);
        MEStorage storage = level.getCapability(
                AECapabilities.ME_STORAGE,
                collector.getBlockPos().relative(facing),
                facing.getOpposite());
        if (storage == null) return null;

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) return stack;
        IActionSource actionSource = IActionSource.ofPlayer(player);

        long simulated = storage.insert(key, stack.getCount(), Actionable.SIMULATE, actionSource);
        if (simulated < stack.getCount()) return stack;

        int movable = stack.getCount();
        source.setStackInSlot(slot, ItemStack.EMPTY);

        long inserted = storage.insert(key, movable, Actionable.MODULATE, actionSource);
        int notInserted = movable - (int) Math.min(movable, Math.max(0L, inserted));
        if (notInserted > 0) restoreToSourceSlot(source, slot, stack, notInserted);
        return source.getStackInSlot(slot);
    }

    private static void restoreToSourceSlot(IItemHandlerModifiable source, int slot,
                                            ItemStack template, int amount) {
        ItemStack current = source.getStackInSlot(slot);
        ItemStack restored = current.isEmpty() ? template.copyWithCount(amount) : current.copy();
        if (!current.isEmpty()) restored.grow(amount);
        source.setStackInSlot(slot, restored);
    }
}
