package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
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

    @Shadow
    protected abstract int addFilterSlots(IItemHandler handler, int index, int x, int y, int amount, int dx);

    @Inject(method = "addFilterSlots()V", at = @At("HEAD"), cancellable = true)
    private void jdte$addExtraFilterSlots(CallbackInfo ci) {
        if (baseMachineBE == null || filterHandler == null) return;

        int filterUpgrades = UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.FILTER);
        if (filterUpgrades <= 0) return;

        ci.cancel();

        int extraSlots = filterUpgrades * 9;
        int originalSlots = FILTER_SLOTS;
        int totalSlots = originalSlots + extraSlots;

        jdte$expandFilterHandler(totalSlots);
        FILTER_SLOTS = totalSlots;

        int slotsAdded = 0;
        int y = 54;

        while (slotsAdded < totalSlots) {
            int slotsInRow = Math.min(9, totalSlots - slotsAdded);
            addFilterSlots(filterHandler, slotsAdded, 8, y, slotsInRow, 18);
            slotsAdded += slotsInRow;
            y += 18;
        }
    }

    @Unique
    private void jdte$expandFilterHandler(int newSize) {
        ItemStack[] oldStacks = jdte$getStacks(filterHandler);
        ItemStack[] newStacks = new ItemStack[newSize];

        for (int i = 0; i < Math.min(oldStacks.length, newSize); i++) {
            newStacks[i] = oldStacks[i];
        }
        for (int i = oldStacks.length; i < newSize; i++) {
            newStacks[i] = ItemStack.EMPTY;
        }

        if (filterHandler instanceof FilterBasicHandlerAccessor accessor) {
            accessor.jdte$setStacks(newStacks);
        }
    }

    @Unique
    private ItemStack[] jdte$getStacks(FilterBasicHandler handler) {
        try {
            var field = handler.getClass().getSuperclass().getDeclaredField("stacks");
            field.setAccessible(true);
            return (ItemStack[]) field.get(handler);
        } catch (Exception e) {
            return new ItemStack[0];
        }
    }
}
