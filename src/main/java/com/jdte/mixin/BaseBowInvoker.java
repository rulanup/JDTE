package com.jdte.mixin;
import com.direwolf20.justdirethings.common.items.tools.basetools.BaseBow;
import com.direwolf20.justdirethings.common.entities.JustDireArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=BaseBow.class,remap=false)
public interface BaseBowInvoker {
    @Invoker(value="setupArrowAbilities",remap=false) void jdte$setupArrowAbilities(ItemStack bow,JustDireArrow arrow,Player player);
}
