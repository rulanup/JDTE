package com.jdte.common.integrations;

import com.brandon3055.draconicevolution.api.crafting.IFusionInjector;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

public final class DraconicFusionEnergyIntegration {
    private static final boolean AVAILABLE = ModList.get().isLoaded("draconicevolution");

    private DraconicFusionEnergyIntegration() {
    }

    public static boolean isFusionInjector(BlockEntity blockEntity) {
        return AVAILABLE && blockEntity instanceof IFusionInjector;
    }

    public static long scaledDemand(BlockEntity blockEntity, long singleCallDemand,
                                    long operationMultiplier, long availableBudget) {
        if (!isFusionInjector(blockEntity) || singleCallDemand <= 0L
                || operationMultiplier <= 0L || availableBudget <= 0L) {
            return 0L;
        }
        IFusionInjector injector = (IFusionInjector) blockEntity;
        long gap = Math.max(0L, injector.getEnergyRequirement() - injector.getInjectorEnergy());
        long acceleratedDemand = saturatingMultiply(singleCallDemand, operationMultiplier);
        return Math.min(Math.min(gap, acceleratedDemand), availableBudget);
    }

    public static long receive(BlockEntity blockEntity, long offered) {
        if (!isFusionInjector(blockEntity) || offered <= 0L) {
            return 0L;
        }
        IFusionInjector injector = (IFusionInjector) blockEntity;
        long stored = Math.max(0L, injector.getInjectorEnergy());
        long gap = Math.max(0L, injector.getEnergyRequirement() - stored);
        long accepted = Math.min(gap, offered);
        if (accepted <= 0L) {
            return 0L;
        }
        injector.setInjectorEnergy(stored + accepted);
        blockEntity.setChanged();
        return accepted;
    }

    private static long saturatingMultiply(long value, long multiplier) {
        if (value <= 0L || multiplier <= 0L) {
            return 0L;
        }
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }
}