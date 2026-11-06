package com.jdte.mixin;
import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemstack;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=EnergyStorageItemstack.class,remap=false)
public interface NativeItemEnergyAccessor {
    @Accessor("stack") ItemStack jdte$owner();
}
