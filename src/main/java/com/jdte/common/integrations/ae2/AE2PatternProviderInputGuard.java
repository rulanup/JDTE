package com.jdte.common.integrations.ae2;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.crafting.ICraftingProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class AE2PatternProviderInputGuard {
    private AE2PatternProviderInputGuard() {
    }

    public static boolean isPatternProviderAt(Level level, BlockPos pos) {
        IInWorldGridNodeHost host = GridHelper.getNodeHost(level, pos);
        if (host == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            IGridNode node = host.getGridNode(direction);
            if (node != null && node.getService(ICraftingProvider.class) != null) {
                return true;
            }
        }
        return false;
    }
}
