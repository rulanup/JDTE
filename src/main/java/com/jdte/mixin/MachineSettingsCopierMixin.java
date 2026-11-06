package com.jdte.mixin;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.jdte.common.items.machinesettings.MachineSettingsTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=MachineSettingsCopier.class,remap=false)
public abstract class MachineSettingsCopierMixin {
    @Redirect(method={"useOn","m_6225_"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",remap=true),remap=false)
    private BlockEntity jdte$resolveMineralPart(Level level,BlockPos pos){return MachineSettingsTargets.resolve(level,pos);}
}
