package com.jdte.mixin;

import com.direwolf20.justdirethings.util.FakePlayerUtil;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FakePlayerUtil.class, remap = false)
public abstract class FakePlayerUtilCleanupMixin {
    @Redirect(
            method = "cleanupFakePlayerFromUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V")
    )
    private static void jdte$clearFakePlayerInventory(Inventory inventory) {
        inventory.items.replaceAll(stack -> stack.getItem() instanceof BucketItem ? stack : ItemStack.EMPTY);
        inventory.armor.replaceAll(stack -> ItemStack.EMPTY);
        inventory.offhand.replaceAll(stack -> ItemStack.EMPTY);
        inventory.dropAll();
    }
}
