package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.ExtendedUpgradeMachine;
import com.jdte.common.upgrades.ClickerFluidContainerData;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.utils.UpgradeSlotStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMachineContainer.class)
public abstract class BaseMachineContainerMixin {
    @Shadow public int FILTER_SLOTS;
    @Shadow public int MACHINE_SLOTS;
    @Shadow public BaseMachineBE baseMachineBE;
    @Shadow public net.minecraft.world.inventory.ContainerData fluidData;

    @Unique
    public int jdte$UPGRADE_SLOTS = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdte$addUpgradeSlots(net.minecraft.world.inventory.MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos, CallbackInfo ci) {
        if (baseMachineBE == null) {
            return;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(baseMachineBE);
        if (handler == null) {
            return;
        }

        int slotCount = handler instanceof ExtendedUpgradeItemStackHandler ? ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT : UpgradeItemStackHandler.SLOT_COUNT;
        jdte$UPGRADE_SLOTS = slotCount;
        UpgradeSlotStorage.setUpgradeSlots((BaseMachineContainer) (Object) this, slotCount);

        // 添加升级槽位（位置将在Screen中设置）
        for (int i = 0; i < slotCount; i++) {
            jdte$addSlot(new UpgradeSlot(handler, i, 0, 0));
        }

        // 只有在没有 fluidData 时才添加
        if (baseMachineBE instanceof ClickerT1BE && fluidData == null) {
            fluidData = new ClickerFluidContainerData(baseMachineBE);
            jdte$addDataSlots(fluidData);
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
    private void jdte$addDataSlots(net.minecraft.world.inventory.ContainerData data) {
        try {
            var method = net.minecraft.world.inventory.AbstractContainerMenu.class.getDeclaredMethod("addDataSlots", net.minecraft.world.inventory.ContainerData.class);
            method.setAccessible(true);
            method.invoke(this, data);
        } catch (Exception e) {
            // ignore
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void jdte$quickMoveUpgrades(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (baseMachineBE == null || index < 0) {
            return;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(baseMachineBE);
        if (handler == null) {
            return;
        }

        int upgradeSlotCount = handler instanceof ExtendedUpgradeItemStackHandler ? ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT : UpgradeItemStackHandler.SLOT_COUNT;
        int upgradeStart = MACHINE_SLOTS + FILTER_SLOTS;
        int upgradeEnd = upgradeStart + upgradeSlotCount;

        BaseMachineContainer container = (BaseMachineContainer) (Object) this;
        if (index >= container.slots.size()) {
            return;
        }

        Slot slot = container.slots.get(index);
        if (!slot.hasItem()) {
            return;
        }

        ItemStack currentStack = slot.getItem();
        boolean fromUpgradeSlot = index >= upgradeStart && index < upgradeEnd;
        boolean fromPlayerSlot = index >= upgradeEnd;

        // 只处理升级卡相关的移动
        if (!fromUpgradeSlot && !(fromPlayerSlot && UpgradeHelper.isUpgrade(currentStack))) {
            return;
        }

        ItemStack originalStack = currentStack.copy();

        if (fromUpgradeSlot) {
            // 从升级槽移到玩家背包
            if (!moveItemStackTo(currentStack, upgradeEnd, container.slots.size(), true)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else {
            // 从玩家背包移到升级槽（只移动1个）
            ItemStack singleItem = currentStack.copyWithCount(1);
            if (moveItemStackTo(singleItem, upgradeStart, upgradeEnd, false)) {
                currentStack.shrink(1);
            } else {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }

        if (currentStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (currentStack.getCount() == originalStack.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        slot.onTake(player, currentStack);
        cir.setReturnValue(originalStack);
    }

    @Unique
    private boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        BaseMachineContainer container = (BaseMachineContainer) (Object) this;
        boolean moved = false;
        int i = reverseDirection ? endIndex - 1 : startIndex;

        while (true) {
            if (reverseDirection) {
                if (i < startIndex) break;
            } else {
                if (i >= endIndex) break;
            }

            Slot slot = container.slots.get(i);
            if (slot.mayPlace(stack) && slot.hasItem()) {
                ItemStack existing = slot.getItem();
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    int combined = existing.getCount() + stack.getCount();
                    int max = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (combined <= max) {
                        existing.setCount(combined);
                        stack.setCount(0);
                        slot.setChanged();
                        moved = true;
                        break;
                    } else if (existing.getCount() < max) {
                        stack.shrink(max - existing.getCount());
                        existing.setCount(max);
                        slot.setChanged();
                        moved = true;
                    }
                }
            }
            i += reverseDirection ? -1 : 1;
        }

        if (stack.isEmpty()) {
            return moved;
        }

        i = reverseDirection ? endIndex - 1 : startIndex;
        while (true) {
            if (reverseDirection) {
                if (i < startIndex) break;
            } else {
                if (i >= endIndex) break;
            }

            Slot slot = container.slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                slot.set(stack.copy());
                slot.setChanged();
                stack.setCount(0);
                moved = true;
                break;
            }
            i += reverseDirection ? -1 : 1;
        }

        return moved;
    }
}
