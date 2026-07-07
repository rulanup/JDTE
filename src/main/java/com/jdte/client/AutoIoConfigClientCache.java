package com.jdte.client;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.network.data.AutoIoConfigPayload;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public final class AutoIoConfigClientCache {
    private static final Map<BlockPos, Integer> SIDE_MASKS = new HashMap<>();

    private AutoIoConfigClientCache() {
    }

    public static int getSideMask(BaseMachineBE machine) {
        if (machine == null) {
            return AutoIoConfigData.DEFAULT_SIDE_MASK;
        }
        return SIDE_MASKS.getOrDefault(machine.getBlockPos(), AutoIoConfigData.DEFAULT_SIDE_MASK);
    }

    public static void setSideMask(BlockPos blockPos, int sideMask) {
        SIDE_MASKS.put(blockPos.immutable(), AutoIoConfigHelper.clampSideMask(sideMask));
    }

    public static void requestSync(BaseMachineBE machine) {
        if (machine == null) {
            return;
        }
        PacketDistributor.sendToServer(new AutoIoConfigPayload(machine.getBlockPos(), AutoIoConfigData.DEFAULT_SIDE_MASK, true));
    }

    public static void updateAndSend(BaseMachineBE machine, int sideMask) {
        if (machine == null) {
            return;
        }
        int clamped = AutoIoConfigHelper.clampSideMask(sideMask);
        setSideMask(machine.getBlockPos(), clamped);
        PacketDistributor.sendToServer(new AutoIoConfigPayload(machine.getBlockPos(), clamped, false));
    }
}
