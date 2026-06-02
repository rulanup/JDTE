package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.UpgradeSlotStorage;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseMachineContainer.class)
public abstract class BaseMachineContainerFilterMixin {
    @Shadow public int FILTER_SLOTS;
    @Shadow public BaseMachineBE baseMachineBE;
    @Shadow public FilterBasicHandler filterHandler;

    @Inject(method = "addFilterSlots()V", at = @At("HEAD"), cancellable = true)
    private void jdte$addExtraFilterSlots(CallbackInfo ci) {
        if (baseMachineBE == null || filterHandler == null) return;

        ci.cancel();

        int originalSlots = UpgradeHelper.hasBaseFilterSlots(baseMachineBE) ? UpgradeHelper.getBaseFilterSlots(filterHandler) : 0;
        int totalSlots = UpgradeHelper.getMaxFilterSlots(originalSlots);

        UpgradeSlotStorage.setBaseFilterSlots((BaseMachineContainer) (Object) this, originalSlots);
        jdte$expandFilterHandler(totalSlots);
        UpgradeHelper.trimInactiveFilterSlots(baseMachineBE);
        FILTER_SLOTS = totalSlots;

        // Add all possible filter slots up front; inactive ones are hidden and disabled.
        int slotsAdded = 0;
        int y = 54;

        while (slotsAdded < totalSlots) {
            int slotsInRow = Math.min(9, totalSlots - slotsAdded);
            jdte$addDynamicFilterSlots(slotsAdded, 8, y, slotsInRow, 18, originalSlots);
            slotsAdded += slotsInRow;
            y += 18;
        }
    }

    @Unique
    private void jdte$addDynamicFilterSlots(int index, int x, int y, int amount, int dx, int baseFilterSlots) {
        for (int i = 0; i < amount; i++) {
            jdte$addSlot(new DynamicFilterSlot(filterHandler, index, x, y, (BaseMachineContainer) (Object) this, baseFilterSlots));
            x += dx;
            index++;
        }
    }

    @Unique
    private void jdte$addSlot(Slot slot) {
        try {
            var method = net.minecraft.world.inventory.AbstractContainerMenu.class.getDeclaredMethod("addSlot", Slot.class);
            method.setAccessible(true);
            method.invoke(this, slot);
        } catch (Exception e) {
            // ignore
        }
    }

    @Unique
    private void jdte$expandFilterHandler(int newSize) {
        NonNullList<ItemStack> oldStacks = jdte$getStacks(filterHandler);
        NonNullList<ItemStack> newStacks = NonNullList.withSize(newSize, ItemStack.EMPTY);

        for (int i = 0; i < Math.min(oldStacks.size(), newSize); i++) {
            newStacks.set(i, oldStacks.get(i));
        }

        try {
            java.lang.reflect.Field field = ItemStackHandler.class.getDeclaredField("stacks");
            field.setAccessible(true);
            field.set(filterHandler, newStacks);
        } catch (Exception e) {
            // Fallback: try through accessor if reflection fails
            if (filterHandler instanceof FilterBasicHandlerAccessor accessor) {
                accessor.jdte$setStacks(newStacks);
            }
        }
    }

    @Unique
    private NonNullList<ItemStack> jdte$getStacks(FilterBasicHandler handler) {
        try {
            var field = handler.getClass().getSuperclass().getDeclaredField("stacks");
            field.setAccessible(true);
            return (NonNullList<ItemStack>) field.get(handler);
        } catch (Exception e) {
            return NonNullList.withSize(0, ItemStack.EMPTY);
        }
    }
}
