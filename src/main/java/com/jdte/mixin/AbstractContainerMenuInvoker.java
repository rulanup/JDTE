package com.jdte.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuInvoker {
    @Invoker("addSlot")
    Slot jdte$invokeAddSlot(Slot slot);

    @Invoker("addDataSlots")
    void jdte$invokeAddDataSlots(ContainerData data);

    @Invoker("moveItemStackTo")
    boolean jdte$invokeMoveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
}
