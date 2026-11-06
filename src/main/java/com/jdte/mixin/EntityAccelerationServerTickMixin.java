package com.jdte.mixin;
import com.jdte.common.manager.EntityAccelerationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Forge lacks NeoForge's generic post-entity-tick event. Hook the completed natural server call. */
@Mixin(ServerLevel.class)
public abstract class EntityAccelerationServerTickMixin {
 @Inject(method="tickNonPassenger",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;tick()V",shift=At.Shift.AFTER))
 private void jdte$afterNaturalTick(Entity entity,CallbackInfo ci){EntityAccelerationManager.onNaturalTick(entity);}
 @Inject(method="tickPassenger",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;rideTick()V",shift=At.Shift.AFTER))
 private void jdte$afterPassengerTick(Entity parent,Entity passenger,CallbackInfo ci){EntityAccelerationManager.onNaturalTick(passenger);}
}
