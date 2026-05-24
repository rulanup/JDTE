package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaAffectingBE.class)
public interface AreaAffectingBEMixin {
    @Shadow AreaAffectingData getAreaAffectingData();
    @Shadow BlockEntity getBlockEntity();

    @Inject(method = "setAreaSettings", at = @At("HEAD"), cancellable = true)
    private void jdte$rangeUpgradeArea(double x, double y, double z, int xo, int yo, int zo, boolean renderArea, CallbackInfo ci) {
        BlockEntity blockEntity = getBlockEntity();
        if (!(blockEntity instanceof BaseMachineBE machine)) {
            return;
        }

        double maxRadius = UpgradeHelper.getMaxAreaRadius(machine);
        int maxOffset = UpgradeHelper.getMaxAreaOffset(machine);
        getAreaAffectingData().xRadius = Math.max(0, Math.min(x, maxRadius));
        getAreaAffectingData().yRadius = Math.max(0, Math.min(y, maxRadius));
        getAreaAffectingData().zRadius = Math.max(0, Math.min(z, maxRadius));
        getAreaAffectingData().xOffset = Math.max(-maxOffset, Math.min(xo, maxOffset));
        getAreaAffectingData().yOffset = Math.max(-maxOffset, Math.min(yo, maxOffset));
        getAreaAffectingData().zOffset = Math.max(-maxOffset, Math.min(zo, maxOffset));
        getAreaAffectingData().renderArea = renderArea;
        getAreaAffectingData().area = null;
        machine.markDirtyClient();
        ci.cancel();
    }
}
