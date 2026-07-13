package com.jdte.mixin;

import com.jdte.common.blockentities.RangeBlockerManager;
import com.jdte.setup.JDTEConfig;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Pseudo
@Mixin(targets = "mekanism.common.content.gear.mekasuit.ModuleMagneticAttractionUnit", remap = false)
public abstract class MekanismMagneticAttractionMixin {
    @ModifyArg(
            method = "tickServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
                    remap = false
            ),
            index = 2,
            require = 0,
            remap = false
    )
    private Predicate<ItemEntity> jdte$excludeDemagnetizedItems(Predicate<ItemEntity> original) {
        if (!JDTEConfig.COMMON.rangeBlockerMekanismIntegration.get()) return original;
        return original.and(item -> !RangeBlockerManager.isDemagnetized(item));
    }
}
