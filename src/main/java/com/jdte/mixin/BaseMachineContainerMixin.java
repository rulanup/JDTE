package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.ExtendedUpgradeMachine;
import com.jdte.common.upgrades.ClickerFluidContainerData;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMachineContainer.class)
public abstract class BaseMachineContainerMixin extends BaseContainer {
    @Shadow public int FILTER_SLOTS;
    @Shadow public int MACHINE_SLOTS;
    @Shadow public BaseMachineBE baseMachineBE;
    @Shadow public net.minecraft.world.inventory.ContainerData fluidData;

    protected BaseMachineContainerMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jdte$addUpgradeSlots(MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos, CallbackInfo ci) {
        if (baseMachineBE == null) {
            return;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(baseMachineBE);
        int slotCount = handler instanceof ExtendedUpgradeItemStackHandler ? ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT : UpgradeItemStackHandler.SLOT_COUNT;
        for (int i = 0; i < slotCount; i++) {
            addSlot(new UpgradeSlot(handler, i, -10000, -10000));
        }

        if (baseMachineBE instanceof ClickerT1BE) {
            fluidData = new ClickerFluidContainerData(baseMachineBE);
            addDataSlots(fluidData);
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void jdte$quickMoveUpgrades(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (baseMachineBE == null || index < 0 || index >= slots.size()) {
            return;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(baseMachineBE);
        int upgradeSlotCount = handler instanceof ExtendedUpgradeItemStackHandler ? ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT : UpgradeItemStackHandler.SLOT_COUNT;
        int upgradeStart = MACHINE_SLOTS + FILTER_SLOTS;
        int upgradeEnd = upgradeStart + upgradeSlotCount;
        int playerStart = upgradeEnd;
        if (slots.size() < upgradeEnd) {
            return;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return;
        }

        ItemStack currentStack = slot.getItem();
        boolean fromUpgradeSlot = index >= upgradeStart && index < upgradeEnd;
        boolean playerUpgrade = index >= playerStart && UpgradeHelper.isUpgrade(currentStack);
        if (!fromUpgradeSlot && !playerUpgrade) {
            return;
        }

        ItemStack originalStack = currentStack.copy();
        if (fromUpgradeSlot) {
            if (!moveItemStackTo(currentStack, playerStart, slots.size(), true)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (!moveItemStackTo(currentStack, upgradeStart, upgradeEnd, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
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
}
