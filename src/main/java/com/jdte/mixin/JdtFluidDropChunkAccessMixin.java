package com.jdte.mixin;

import com.direwolf20.justdirethings.common.events.EntityEvents;
import com.jdte.common.utils.LoadedFluidDropLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value=EntityEvents.class,remap=false)
public abstract class JdtFluidDropChunkAccessMixin {
    @Redirect(method="levelTick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",remap=true),remap=false)
    private static BlockState jdte$withoutChunkLoading(Level level,BlockPos pos) {return LoadedFluidDropLookup.state(level,pos);}
}
