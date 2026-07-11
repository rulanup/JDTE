package com.jdte.common.autoioconfig;

import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.DropperT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.ExtendedSensorBE;
import com.jdte.common.blockentities.FluidStabilizerBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.blockentities.GlueActivatorBE;
import com.jdte.common.blockentities.InfusionMachineBE;
import com.jdte.common.blockentities.ItemReceiverBE;
import com.jdte.common.blockentities.ItemSenderBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.setup.JDTEAttachments;

public final class AutoIoConfigHelper {
    private AutoIoConfigHelper() {
    }

    public static boolean hasConfigurableIo(BaseMachineBE machine) {
        if (machine == null || machine instanceof ExtendedSensorBE) {
            return false;
        }
        if (machine instanceof FluidMachineBE) {
            return true;
        }
        if (machine instanceof GelGeneratorBE
                || machine instanceof GlueActivatorBE
                || machine instanceof FluidStabilizerBE
                || machine instanceof ItemSenderBE
                || machine instanceof ItemReceiverBE
                || machine instanceof BioCrusherBE
                || machine instanceof InfusionMachineBE
                || machine instanceof AdvancedPotionBrewerBE) {
            return true;
        }
        if (machine instanceof LootFabricatorBE) {
            return true;
        }
        return machine instanceof ClickerT1BE
                || machine instanceof BlockBreakerT1BE
                || machine instanceof BlockPlacerT1BE
                || machine instanceof DropperT1BE
                || machine instanceof FluidCollectorT1BE
                || machine instanceof FluidPlacerT1BE;
    }

    public static int getSideMask(BaseMachineBE machine) {
        if (machine == null) {
            return AutoIoConfigData.DEFAULT_SIDE_MASK;
        }
        return machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get()).getSideMask();
    }

    public static void setSideMask(BaseMachineBE machine, int sideMask) {
        if (machine == null) {
            return;
        }
        machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get()).setSideMask(sideMask);
        machine.markDirtyClient();
    }

    public static int toggleSide(int sideMask, int side) {
        if (side < 0 || side >= AutoIoConfigData.SIDE_COUNT) {
            return clampSideMask(sideMask);
        }
        return clampSideMask(sideMask ^ (1 << side));
    }

    public static int clampSideMask(int sideMask) {
        return sideMask & AutoIoConfigData.ALL_SIDES_MASK;
    }
}
