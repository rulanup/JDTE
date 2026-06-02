package com.jdte.mixin;

import com.jdte.common.blockentities.BioCrusherBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
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
     * Hook the delay() call inside serverTick().
     * This is called once per spawn cycle, after all entities have been spawned.
     * By injecting here, we:
     * - Let vanilla handle spawnCount iterations
     * - Only execute once per cycle
     * - Properly handle all vanilla state (spawnDelay, spawnPotentials, etc.)
     */
    @Inject(
        method = "serverTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/BaseSpawner;delay(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
        ),
        cancellable = true
    )
    private void jdte$onDelay(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        // Check if there's a Bio Crusher above the spawner
        BlockPos crusherPos = pos.above();
        BlockState crusherState = level.getBlockState(crusherPos);

        // Check if the block above is a Bio Crusher
        if (crusherState.getBlock() instanceof com.jdte.common.blocks.AdvancedBioCrusherBlock ||
            crusherState.getBlock() instanceof com.jdte.common.blocks.ExtendedBioCrusherBlock) {

            BlockEntity be = level.getBlockEntity(crusherPos);
            if (be instanceof BioCrusherBE crusher) {
                BaseSpawner spawner = (BaseSpawner) (Object) this;
                BaseSpawnerAccessor accessor = (BaseSpawnerAccessor) spawner;

                // Process the spawner's mobs through the crusher
                // spawnCount is handled inside processSpawnerCrush
                crusher.processSpawnerCrush(level, pos, accessor);

                // Cancel the delay call - we already handle the delay in processSpawnerCrush
                // Actually, we should let vanilla handle the delay
                // ci.cancel();
            }
        }
    }

    // Handle Apothic Spawners
    // Use @Pseudo to avoid errors when Apothic Spawners is not installed
    @Pseudo
    @Mixin(targets = "dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile$SpawnerLogicExt")
    public static class ApothSpawnerMixin {

        @Inject(
            method = "serverTick",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/BaseSpawner;delay(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            ),
            cancellable = true
        )
        private void jdte$onDelay(ServerLevel level, BlockPos pos, CallbackInfo ci) {
            // Check if there's a Bio Crusher above the spawner
            BlockPos crusherPos = pos.above();
            BlockState crusherState = level.getBlockState(crusherPos);

            // Check if the block above is a Bio Crusher
            if (crusherState.getBlock() instanceof com.jdte.common.blocks.AdvancedBioCrusherBlock ||
                crusherState.getBlock() instanceof com.jdte.common.blocks.ExtendedBioCrusherBlock) {

                BlockEntity be = level.getBlockEntity(crusherPos);
                if (be instanceof BioCrusherBE crusher) {
                    // SpawnerLogicExt extends BaseSpawner
                    BaseSpawnerAccessor accessor = (BaseSpawnerAccessor) (Object) this;

                    // Process the spawner's mobs through the crusher
                    crusher.processSpawnerCrush(level, pos, accessor);
                }
            }
        }
    }
}
