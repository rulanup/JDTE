package com.jdte.mixin;

import com.jdte.common.blockentities.BioCrusherBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public class SpawnerMixin {

    /**
     * Intercept the spawn cycle before vanilla creates entities.
     * A crusher directly above the spawner consumes the cycle, generates drops and XP fluid,
     * then lets vanilla pick the next spawn delay/data without spawning entities.
     */
    @Inject(
        method = "serverTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/BaseSpawner;getOrCreateNextSpawnData(Lnet/minecraft/world/level/Level;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/SpawnData;"
        ),
        cancellable = true
    )
    private void jdte$beforeSpawn(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        jdte$tryCrushSpawner(level, pos, (BaseSpawner) (Object) this, ci);
    }

    private static void jdte$tryCrushSpawner(ServerLevel level, BlockPos pos, BaseSpawner spawner, CallbackInfo ci) {
        BlockPos crusherPos = pos.above();
        BlockState crusherState = level.getBlockState(crusherPos);

        if (crusherState.getBlock() instanceof com.jdte.common.blocks.AdvancedBioCrusherBlock ||
            crusherState.getBlock() instanceof com.jdte.common.blocks.ExtendedBioCrusherBlock) {

            BlockEntity be = level.getBlockEntity(crusherPos);
            if (be instanceof BioCrusherBE crusher) {
                BaseSpawnerAccessor accessor = (BaseSpawnerAccessor) spawner;
                SpawnData spawnData = ((BaseSpawnerInvoker) spawner).jdte$getOrCreateNextSpawnData(level, level.getRandom(), pos);
                if (crusher.processSpawnerCrush(level, pos, spawner, spawnData, accessor.jdte$getSpawnCount())) {
                    ((BaseSpawnerInvoker) spawner).jdte$delay(level, pos);
                    ci.cancel();
                }
            }
        }
    }

    // Handle Apothic Spawners
    // Use @Pseudo to avoid errors when Apothic Spawners is not installed
    @Pseudo
    @Mixin(targets = "dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile$SpawnerLogicExt", remap = false)
    public abstract static class ApothSpawnerMixin {

        @Inject(
            method = "serverTick",
            at = @At(
                value = "INVOKE",
                target = "Ldev/shadowsoffire/apothic_spawners/block/ApothSpawnerTile$SpawnerLogicExt;getOrCreateNextSpawnData(Lnet/minecraft/world/level/Level;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/SpawnData;",
                remap = false
            ),
            cancellable = true,
            require = 0,
            remap = false
        )
        private void jdte$beforeSpawn(ServerLevel level, BlockPos pos, CallbackInfo ci) {
            BlockPos crusherPos = pos.above();
            BlockState crusherState = level.getBlockState(crusherPos);

            if (crusherState.getBlock() instanceof com.jdte.common.blocks.AdvancedBioCrusherBlock ||
                crusherState.getBlock() instanceof com.jdte.common.blocks.ExtendedBioCrusherBlock) {

                BlockEntity be = level.getBlockEntity(crusherPos);
                if (be instanceof BioCrusherBE crusher) {
                    SpawnData spawnData = ((BaseSpawnerInvoker) (Object) this).jdte$getOrCreateNextSpawnData(level, level.getRandom(), pos);
                    int spawnCount = ((BaseSpawnerAccessor) (Object) this).jdte$getSpawnCount();
                    if (crusher.processSpawnerCrush(level, pos, (BaseSpawner) (Object) this, spawnData, spawnCount)) {
                        ((ApothSpawnerInvoker) (Object) this).jdte$apothDelay(level, pos);
                        ci.cancel();
                    }
                }
            }
        }
    }
}
