package com.jdte.mixin;

import com.jdte.common.shutdown.ChunkMapShutdownPolicy;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapShutdownMixin {
    @Shadow @Final ServerLevel level;

    @Redirect(
            method = {"scheduleUnload", "lambda$scheduleUnload$0"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ChunkHolder;getSaveSyncFuture()Ljava/util/concurrent/CompletableFuture;"
            ),
            require = 2
    )
    private CompletableFuture<?> jdte$doNotWaitForOrphanedSaveBarrierDuringShutdown(ChunkHolder holder) {
        return ChunkMapShutdownPolicy.saveBarrier(level.getServer().isStopped(), holder.getSaveSyncFuture());
    }

}
