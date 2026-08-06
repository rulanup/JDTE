package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT2BE;
import com.direwolf20.justdirethings.common.blockentities.BlockPlacerT2BE;
import com.direwolf20.justdirethings.common.blockentities.BlockSwapperT2BE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT2BE;
import com.direwolf20.justdirethings.common.blockentities.DropperT2BE;
import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT2BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT2BE;
import com.direwolf20.justdirethings.common.blockentities.SensorT2BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({BlockBreakerT2BE.class, BlockPlacerT2BE.class, BlockSwapperT2BE.class, ClickerT2BE.class, DropperT2BE.class, FluidCollectorT2BE.class, FluidPlacerT2BE.class, SensorT2BE.class})
public abstract class PoweredMachineDefaultMaxEnergyMixin {
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity((BaseMachineBE) (Object) this, 100000);
    }
}
