package com.jdte.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile$SpawnerLogicExt", remap = false)
public interface ApothSpawnerInvoker {
    @Invoker(value = "delay", remap = false)
    void jdte$apothDelay(Level level, BlockPos pos);
}
