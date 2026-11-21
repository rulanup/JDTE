package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class CreativeEnergyCellBE extends BlockEntity {
    // A receiver may accept just one FE per call. Bound server work independently of FE throughput.
    private static final int MAX_INSERT_CALLS = 256;
    private final List<BlockCapabilityCache<EnergyHandler, Direction>> neighbors = new ArrayList<>(6);

    public CreativeEnergyCellBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.CREATIVE_ENERGY_CELL.get(), pos, state);
    }

    public void chargeNeighbors(ServerLevel level) {
        if (neighbors.isEmpty()) {
            for (Direction side : Direction.values()) {
                neighbors.add(BlockCapabilityCache.create(Capabilities.Energy.BLOCK, level,
                        worldPosition.relative(side), side.getOpposite()));
            }
        }
        for (var neighbor : neighbors) {
            var receiver = neighbor.getCapability();
            if (receiver != null) charge(receiver);
        }
    }

    public static void charge(EnergyHandler receiver) {
        long stored = receiver.getAmountAsLong();
        try (var transaction = Transaction.openRoot()) {
            for (int call = 0; call < MAX_INSERT_CALLS; call++) {
                if (receiver.insert(Integer.MAX_VALUE, transaction) == 0) break;
                long updated = receiver.getAmountAsLong();
                // Forwarding ports can accept energy without buffering it locally.
                if (updated <= stored || updated >= receiver.getCapacityAsLong()) break;
                stored = updated;
            }
            transaction.commit();
        }
    }
}
