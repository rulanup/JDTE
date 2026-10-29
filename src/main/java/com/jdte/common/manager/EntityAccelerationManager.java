package com.jdte.common.manager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Synchronous server-only sub-ticks. Never retain entity references across a real tick. */
public final class EntityAccelerationManager {
    private static final ThreadLocal<Entity> ACTIVE = new ThreadLocal<>();
    private static final java.util.Map<net.minecraft.server.MinecraftServer,java.util.Map<java.util.UUID,Integer>> LAST_NATURAL=new java.util.IdentityHashMap<>();
    public static com.jdte.common.entities.EntityAccelerationData get(Entity entity){return com.jdte.common.entities.EntityAccelerationData.read(entity);}
    public static void set(Entity entity,com.jdte.common.entities.EntityAccelerationData data){data.store(entity);sync(entity,null);}
    public static void clear(Entity entity){entity.getPersistentData().remove(com.jdte.common.entities.EntityAccelerationData.KEY);sync(entity,null);}
    private static void sync(Entity entity,net.minecraft.server.level.ServerPlayer player){if(entity.level().isClientSide)return;var d=get(entity);var packet=new com.jdte.common.network.data.EntityAccelerationSyncPayload(entity.getId(),entity.getUUID(),d==null?0:d.getExponent(),d==null?0:d.getTotalTime(),d==null?0:d.getRemainingTime());
        com.jdte.common.network.JDTEPacketHandler.CHANNEL.send(player==null?net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(()->entity):net.minecraftforge.network.PacketDistributor.PLAYER.with(()->player),packet);}
    public static void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event){if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player&&get(event.getTarget())!=null)sync(event.getTarget(),player);}
    public static void onNaturalTick(Entity entity){
        if(!(entity.level() instanceof ServerLevel level)||isSuppressingAi()||!level.getServer().isSameThread())return;
        if(com.jdte.common.blockentities.EntitySuppressorManager.shouldSuppressEntityTick(entity))return;
        var data=get(entity);if(data==null)return;
        if(!com.jdte.common.items.UltimateTimeWandItem.isValidEntityTarget(entity)||!data.isActive()){clear(entity);return;}
        var recent=LAST_NATURAL.computeIfAbsent(level.getServer(),ignored->new java.util.HashMap<>());int now=level.getServer().getTickCount();
        if(java.util.Objects.equals(recent.put(entity.getUUID(),now),now))return;
        data.decrementRemainingTime();if(!data.isActive()){clear(entity);return;}data.store(entity);
        int extra=com.jdte.common.items.UltimateTimeWandData.multiplierForExponent(data.getExponent())-1;
        if(extra>0)runAcceleratedTicks(entity,extra);
        if(entity.isRemoved()||!entity.isAlive()){clear(entity);return;}
        if(data.getRemainingTime()%20==0)sync(entity,null);
        if(entity.tickCount%4==0)level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,entity.getX(),entity.getY()+entity.getBbHeight()*.5,entity.getZ(),3,entity.getBbWidth()*.3,entity.getBbHeight()*.25,entity.getBbWidth()*.3,.05);
    }
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event){if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var map=LAST_NATURAL.get(event.getServer());if(map!=null){int now=event.getServer().getTickCount();map.entrySet().removeIf(e->now-e.getValue()>1);}}
    public static void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event){LAST_NATURAL.remove(event.getServer());ACTIVE.remove();}
    public static void onEntityInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event){var stack=event.getItemStack();if(!(stack.getItem() instanceof com.jdte.common.items.UltimateTimeWandItem))return;
        if(event.getTarget() instanceof net.minecraft.world.entity.LivingEntity)return;
        if(!com.jdte.common.items.UltimateTimeWandItem.hasEntityAcceleration(stack)){if(!event.getLevel().isClientSide)event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.translatable("message.jdte.ultimate_time_wand.requires_entity_acceleration"),true);event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);event.setCanceled(true);return;}
        boolean accepted=event.getLevel().isClientSide || com.jdte.common.items.UltimateTimeWandItem.applyToEntityTarget((ServerLevel)event.getLevel(),event.getEntity(),stack,event.getTarget());
        event.setCancellationResult(accepted?net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide):net.minecraft.world.InteractionResult.FAIL);event.setCanceled(true);
    }
    private EntityAccelerationManager() {}
    public static boolean isSuppressingAi() { return ACTIVE.get()!=null; }
    public static boolean isAccelerating(Entity entity) { return ACTIVE.get()==entity; }
    public static int runAcceleratedTicks(Entity entity,int extraTicks) {
        if(extraTicks<=0 || entity instanceof Player || !(entity.level() instanceof ServerLevel level)
                || (!level.getServer().isRunning() || level.getServer().isStopped()) || !level.getServer().isSameThread() || ACTIVE.get()!=null)return 0;
        int completed=0;
        ACTIVE.set(entity);
        try {
            for(int i=0;i<extraTicks;i++) {
                if(!level.getServer().isRunning() || level.getServer().isStopped() || entity.level()!=level || entity.isRemoved() || !entity.isAlive()
                        || !level.hasChunkAt(entity.blockPosition()) || level.getEntity(entity.getUUID())!=entity)break;
                entity.tick();
                completed++;
            }
        } finally { ACTIVE.remove(); }
        return completed;
    }
}
