package com.jdte.mixin;

import com.jdte.common.blocks.AdvancedBioCrusherBlock;
import com.jdte.common.blocks.ExtendedBioCrusherBlock;
import com.jdte.common.integrations.DraconicStabilizedSpawnerIntegration;
import com.jdte.common.utils.SpawnerCrushHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
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
        BaseSpawner spawner = (BaseSpawner) (Object) this;
        SpawnData spawnData = ((BaseSpawnerInvoker) spawner).jdte$getOrCreateNextSpawnData(level, level.getRandom(), pos);
        int spawnCount = ((BaseSpawnerAccessor) spawner).jdte$getSpawnCount();
        if (SpawnerCrushHelper.tryCrush(level, pos, spawner, spawnData, spawnCount)) {
            ((BaseSpawnerInvoker) spawner).jdte$delay(level, pos);
            ci.cancel();
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
            BaseSpawner spawner = (BaseSpawner) (Object) this;
            SpawnData spawnData = ((BaseSpawnerInvoker) spawner).jdte$getOrCreateNextSpawnData(level, level.getRandom(), pos);
            int spawnCount = ((BaseSpawnerAccessor) spawner).jdte$getSpawnCount();
            if (SpawnerCrushHelper.tryCrush(level, pos, spawner, spawnData, spawnCount)) {
                ((ApothSpawnerInvoker) (Object) this).jdte$apothDelay(level, pos);
                ci.cancel();
            }
        }
    }

    // Handle Draconic Evolution's Stabilized Spawner.
    // Its logic extends BaseSpawner but overrides serverTick with its own ManagedShort
    // delay and MobSoul-based entity creation, so it never reaches the vanilla
    // interception point. Inject at the MobSoul.createEntity call site: DE's own delay
    // handling and player gating have already run, so the crush fires exactly on the
    // native spawn cycle and re-arms it through the private resetTimer.
    @Pseudo
    @Mixin(targets = "com.brandon3055.draconicevolution.blocks.tileentity.StabilizedSpawnerLogic", remap = false)
    public abstract static class DraconicSpawnerMixin {

        @Inject(
            method = "serverTick",
            at = @At(
                value = "INVOKE",
                target = "Lcom/brandon3055/draconicevolution/items/MobSoul;createEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/Entity;",
                remap = false
            ),
            cancellable = true,
            require = 0,
            remap = false
        )
        private void jdte$beforeSpawn(ServerLevel level, BlockPos pos, CallbackInfo ci) {
            BlockState above = level.getBlockState(pos.above());
            if (!(above.getBlock() instanceof AdvancedBioCrusherBlock)
                    && !(above.getBlock() instanceof ExtendedBioCrusherBlock)) {
                return;
            }
            if (ModList.get().isLoaded("draconicevolution")
                    && DraconicStabilizedSpawnerIntegration.tryCrush(level, pos, (BaseSpawner) (Object) this, this)) {
                ci.cancel();
            }
        }
    }
}
