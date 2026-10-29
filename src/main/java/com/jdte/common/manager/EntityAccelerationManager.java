package com.jdte.common.manager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Synchronous server-only sub-ticks. Never retain entity references across a real tick. */
public final class EntityAccelerationManager {
    private static final ThreadLocal<Entity> ACTIVE = new ThreadLocal<>();
    private EntityAccelerationManager() {}
    public static boolean isSuppressingAi() { return ACTIVE.get()!=null; }
    public static boolean isAccelerating(Entity entity) { return ACTIVE.get()==entity; }
    public static int runAcceleratedTicks(Entity entity,int extraTicks) {
        if(extraTicks<=0 || entity instanceof Player || !(entity.level() instanceof ServerLevel level)
                || level.getServer().isStopped() || !level.getServer().isSameThread() || ACTIVE.get()!=null)return 0;
        int completed=0;
        ACTIVE.set(entity);
        try {
            for(int i=0;i<extraTicks;i++) {
                if(entity.level()!=level || entity.isRemoved() || !entity.isAlive()
                        || !level.hasChunkAt(entity.blockPosition()) || level.getEntity(entity.getUUID())!=entity)break;
                entity.tick();
                completed++;
            }
        } finally { ACTIVE.remove(); }
        return completed;
    }
}
