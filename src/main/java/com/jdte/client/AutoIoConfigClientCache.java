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
    private static final Map<BlockPos, AutoIoConfigHelper.IoMasks> SIDE_MASKS = new HashMap<>();

    private AutoIoConfigClientCache() {
    }

    public static int getInputMask(BaseMachineBE machine) {
        if (machine == null) {
            return AutoIoConfigData.DEFAULT_SIDE_MASK;
        }
        return getMasks(machine).inputMask();
    }

    public static int getOutputMask(BaseMachineBE machine) {
        if (machine == null) {
            return AutoIoConfigData.DEFAULT_SIDE_MASK;
        }
        return getMasks(machine).outputMask();
    }

    public static void setMasks(BlockPos blockPos, int inputMask, int outputMask) {
        SIDE_MASKS.put(blockPos.immutable(), new AutoIoConfigHelper.IoMasks(inputMask, outputMask));
    }

    public static void requestSync(BaseMachineBE machine) {
        if (machine == null) {
            return;
        }
        PacketDistributor.sendToServer(new AutoIoConfigPayload(machine.getBlockPos(),
                AutoIoConfigData.DEFAULT_SIDE_MASK, AutoIoConfigData.DEFAULT_SIDE_MASK, true));
    }

    public static void updateAndSend(BaseMachineBE machine, int inputMask, int outputMask) {
        if (machine == null) {
            return;
        }
        AutoIoConfigHelper.IoMasks masks = new AutoIoConfigHelper.IoMasks(inputMask, outputMask);
        setMasks(machine.getBlockPos(), masks.inputMask(), masks.outputMask());
        PacketDistributor.sendToServer(new AutoIoConfigPayload(
                machine.getBlockPos(), masks.inputMask(), masks.outputMask(), false));
    }

    private static AutoIoConfigHelper.IoMasks getMasks(BaseMachineBE machine) {
        return SIDE_MASKS.getOrDefault(machine.getBlockPos(),
                new AutoIoConfigHelper.IoMasks(AutoIoConfigData.DEFAULT_SIDE_MASK, AutoIoConfigData.DEFAULT_SIDE_MASK));
    }
}
