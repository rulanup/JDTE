package com.jdte.mixin;

import com.direwolf20.justdirethings.util.NBTHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NBTHelpers.class)
public abstract class BoundPositionLoadMixin {
    @Inject(method = "nbtToGlobalPos", at = @At("HEAD"), cancellable = true)
    private static void jdte$readLegacyPosition(CompoundTag tag, CallbackInfoReturnable<GlobalPos> result) {
        if (!(tag.get("blockpos") instanceof IntArrayTag coordinates)) return;
        int[] xyz = coordinates.getAsIntArray();
        var dimension = Identifier.tryParse(tag.getStringOr("dimension", ""));
        if (xyz.length == 3 && tag.contains("dimension") && dimension != null) {
            result.setReturnValue(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension),
                    new BlockPos(xyz[0], xyz[1], xyz[2])));
        }
    }
}
