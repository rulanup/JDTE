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

    public static int getInputMask(BaseMachineBE machine) {
        if (machine == null) {
            return AutoIoConfigData.DEFAULT_SIDE_MASK;
        }
        return supportsInput(machine)
                ? machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get()).getInputMask()
                : AutoIoConfigData.DEFAULT_SIDE_MASK;
    }

    public static int getOutputMask(BaseMachineBE machine) {
        if (machine == null) {
            return AutoIoConfigData.DEFAULT_SIDE_MASK;
        }
        return supportsOutput(machine)
                ? machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get()).getOutputMask()
                : AutoIoConfigData.DEFAULT_SIDE_MASK;
    }

    public static void setMasks(BaseMachineBE machine, int inputMask, int outputMask) {
        if (machine == null) {
            return;
        }
        int supportedInputMask = supportsInput(machine) ? inputMask : AutoIoConfigData.DEFAULT_SIDE_MASK;
        int supportedOutputMask = supportsOutput(machine) ? outputMask : AutoIoConfigData.DEFAULT_SIDE_MASK;
        machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get()).setMasks(supportedInputMask, supportedOutputMask);
        machine.markDirtyClient();
    }

    public static IoMasks cycleSide(int inputMask, int outputMask, int side,
                                    boolean supportsInput, boolean supportsOutput) {
        if (side < 0 || side >= AutoIoConfigData.SIDE_COUNT) {
            return new IoMasks(clampSideMask(inputMask), clampSideMask(outputMask));
        }
        int bit = 1 << side;
        if (supportsInput && !supportsOutput) {
            return (inputMask & bit) == 0
                    ? new IoMasks(inputMask | bit, outputMask & ~bit)
                    : new IoMasks(inputMask & ~bit, outputMask & ~bit);
        }
        if (!supportsInput && supportsOutput) {
            return (outputMask & bit) == 0
                    ? new IoMasks(inputMask & ~bit, outputMask | bit)
                    : new IoMasks(inputMask & ~bit, outputMask & ~bit);
        }
        if (!supportsInput && !supportsOutput) {
            return new IoMasks(inputMask & ~bit, outputMask & ~bit);
        }
        return switch (getMode(inputMask, outputMask, side)) {
            case MODE_OFF -> new IoMasks(inputMask | bit, outputMask | bit);
            case MODE_BOTH -> new IoMasks(inputMask | bit, outputMask & ~bit);
            case MODE_INPUT -> new IoMasks(inputMask & ~bit, outputMask | bit);
            default -> new IoMasks(inputMask & ~bit, outputMask & ~bit);
        };
    }

    public static int getMode(int inputMask, int outputMask, int side) {
        if (side < 0 || side >= AutoIoConfigData.SIDE_COUNT) {
            return MODE_OFF;
        }
        int bit = 1 << side;
        boolean input = (inputMask & bit) != 0;
        boolean output = (outputMask & bit) != 0;
        if (input && output) return MODE_BOTH;
        if (input) return MODE_INPUT;
        if (output) return MODE_OUTPUT;
        return MODE_OFF;
    }

    public static int clampSideMask(int sideMask) {
        return sideMask & AutoIoConfigData.ALL_SIDES_MASK;
    }

    public static boolean supportsInput(BaseMachineBE machine) {
        return AutoIoTransferHelper.supportsInput(machine);
    }

    public static boolean supportsOutput(BaseMachineBE machine) {
        return AutoIoTransferHelper.supportsOutput(machine);
    }

    public static final int MODE_OFF = 0;
    public static final int MODE_BOTH = 1;
    public static final int MODE_INPUT = 2;
    public static final int MODE_OUTPUT = 3;

    public record IoMasks(int inputMask, int outputMask) {
        public IoMasks {
            inputMask = clampSideMask(inputMask);
            outputMask = clampSideMask(outputMask);
        }
    }
}
