package com.jdte.common.integrations.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class AdvancedItemCollectorAE2Integration {
    private AdvancedItemCollectorAE2Integration() {
    }

    public static ItemStack insertCollectedStack(ItemStack stack, AdvancedItemCollectorBE collector, boolean simulate) {
        if (!(collector.getLevel() instanceof ServerLevel level)) {
            return stack;
        }

        Direction facing = collector.getBlockState().getValue(BlockStateProperties.FACING);
        BlockPos storagePos = collector.getBlockPos().relative(facing);
        BlockEntity storageEntity = level.getBlockEntity(storagePos);
        MEStorage storage = storageEntity == null
                ? null
                : storageEntity.getCapability(Capabilities.STORAGE, facing.getOpposite()).orElse(null);
        if (storage == null) {
            return stack;
        }

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) {
            return stack;
        }

        IActionSource source = IActionSource.empty();
        long accepted = storage.insert(key, stack.getCount(), Actionable.SIMULATE, source);
        if (accepted < stack.getCount()) {
            return stack;
        }
        if (simulate) {
            return ItemStack.EMPTY;
        }

        long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, source);
        int remainder = stack.getCount() - (int) Math.min(stack.getCount(), Math.max(0L, inserted));
        return remainder == 0 ? ItemStack.EMPTY : stack.copyWithCount(remainder);
    }
}
