package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.utils.ContainerDataEncoding;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LifeSynthesisContainer extends BaseMachineContainer {
    public LifeSynthesisContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public LifeSynthesisContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.LIFE_SYNTHESIS_VAT.get(), id, inventory, pos);
        if (baseMachineBE instanceof LifeSynthesisVatBE vat) addDataSlots(vat.getVatData());
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        LifeSynthesisVatBE vat = (LifeSynthesisVatBE) baseMachineBE;
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int columns = 3;
        for (int i = 0; i < LifeSynthesisVatBE.INPUT_SLOTS; i++) {
            int x = layout.getGreenhouseInputStartX() + (i % columns) * layout.getGreenhouseInputSpacing();
            int y = layout.getGreenhouseInputStartY() + (i / columns) * layout.getGreenhouseInputSpacing();
            addSlot(new SlotItemHandler(vat.getMachineHandler(), i, x, y));
        }
    }

    public int getProgress() { return getMachineData(0, 0); }
    public int getProgressMax() { return getMachineData(1, 1); }
    public int getNutrientFluid() { return ContainerDataEncoding.combine16(getMachineData(2, 0), getMachineData(11, 0)); }
    public int getTimeFluid() { return ContainerDataEncoding.combine16(getMachineData(3, 0), getMachineData(12, 0)); }
    public int getLifeFluid() { return ContainerDataEncoding.combine16(getMachineData(4, 0), getMachineData(13, 0)); }
    public int getFluidCapacity() { return ContainerDataEncoding.combine16(getMachineData(5, 1), getMachineData(14, 0)); }
    public int getPendingLifeFluid() { return ContainerDataEncoding.combine16(getMachineData(6, 0), getMachineData(15, 0)); }
    public int getTierCode() { return getMachineData(7, 0); }
    public int getMultiplier() { return getMachineData(8, 1); }
    public int getMaxMultiplier() { return getMachineData(9, 32); }
    public int getRecipeIndex() { return getMachineData(10, -1); }
    public LifeSynthesisVatBE getVat() { return (LifeSynthesisVatBE) baseMachineBE; }

    private int getMachineData(int index, int fallback) {
        return baseMachineBE instanceof LifeSynthesisVatBE vat ? vat.getVatData().get(index) : fallback;
    }

    public boolean isInputSlot(Slot slot) {
        int menuIndex = slots.indexOf(slot);
        return menuIndex >= 0 && menuIndex < LifeSynthesisVatBE.INPUT_SLOTS;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.LIFE_SYNTHESIS_VAT.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlotCount = LifeSynthesisVatBE.INPUT_SLOTS;
        int playerStart = machineSlotCount + LifeSynthesisVatBE.UPGRADE_SLOTS;
        if (index < playerStart) {
            if (!moveStackTo(stack, playerStart, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof UpgradeCardItem) {
            if (!moveStackTo(stack, machineSlotCount, playerStart, false)) return ItemStack.EMPTY;
        } else {
            if (!moveStackTo(stack, 0, machineSlotCount, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    private boolean moveStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        int index = reverse ? end - 1 : start;
        while (!stack.isEmpty() && (reverse ? index >= start : index < end)) {
            Slot target = slots.get(index);
            if (target.mayPlace(stack) && target.hasItem()) {
                ItemStack existing = target.getItem();
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    int max = Math.min(target.getMaxStackSize(stack), stack.getMaxStackSize());
                    int transferable = Math.min(stack.getCount(), max - existing.getCount());
                    if (transferable > 0) {
                        existing.grow(transferable);
                        stack.shrink(transferable);
                        target.setChanged();
                        moved = true;
                    }
                }
            }
            index += reverse ? -1 : 1;
        }
        index = reverse ? end - 1 : start;
        while (!stack.isEmpty() && (reverse ? index >= start : index < end)) {
            Slot target = slots.get(index);
            if (!target.hasItem() && target.mayPlace(stack)) {
                int count = Math.min(stack.getCount(), target.getMaxStackSize(stack));
                target.set(stack.copyWithCount(count));
                stack.shrink(count);
                target.setChanged();
                moved = true;
            }
            index += reverse ? -1 : 1;
        }
        return moved;
    }
}