package com.jdte.smoke.mixin;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Probe-only, opt-in stack trace. Never packaged in JDTE's main JAR. No ticket/game behavior changes. */
@Mixin(value=ServerChunkCache.class,remap=false)
public abstract class GeneratorTicketTrace {
    // m_8456_ is the Forge 1.20.1 SRG name; this separate diagnostic source set has no refmap processor.
    @Inject(method={"getChunkFutureMainThread","m_8456_"},at=@At("HEAD"),remap=false)
    private void jdte$trace(int x,int z,ChunkStatus status,boolean load,CallbackInfoReturnable<?> ci) {
        if(!load||x!=194||z!=194||!Boolean.getBoolean("jdte.generatorTicketTrace"))return;
        System.setProperty("jdte.generatorTicketTrace","false");
        com.mojang.logging.LogUtils.getLogger().warn("Generator probe unexpected chunk-loading request after unforce",new IllegalStateException("Generator chunk ticket trace"));
    }
}
