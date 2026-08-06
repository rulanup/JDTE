package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = AreaAffectingBE.class, remap = false)
public interface AreaAffectingBEMixin {
    /**
     * Forge JDT exposes this as an interface default method, so an overwrite is
     * required; injector methods cannot target interfaces on Mixin 0.8.
     */
    @Overwrite
    default void setAreaSettings(double x, double y, double z, int xo, int yo, int zo, boolean renderArea) {
        AreaAffectingBE self = (AreaAffectingBE) (Object) this;
        BlockEntity blockEntity = self.getBlockEntity();
        if (!(blockEntity instanceof BaseMachineBE machine)) {
            return;
        }

        double maxRadius = UpgradeHelper.getMaxAreaRadius(machine);
        int maxOffset = UpgradeHelper.getMaxAreaOffset(machine);
        AreaAffectingData data = self.getAreaAffectingData();
        data.xRadius = Math.max(0, Math.min(x, maxRadius));
        data.yRadius = Math.max(0, Math.min(y, maxRadius));
        data.zRadius = Math.max(0, Math.min(z, maxRadius));
        data.xOffset = Math.max(-maxOffset, Math.min(xo, maxOffset));
        data.yOffset = Math.max(-maxOffset, Math.min(yo, maxOffset));
        data.zOffset = Math.max(-maxOffset, Math.min(zo, maxOffset));
        data.renderArea = renderArea;
        data.area = null;
        machine.markDirtyClient();
    }
}
