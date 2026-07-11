package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.upgrades.UpgradeType;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LootFabricatorContainer extends BaseMachineContainer implements FilterPageHolder {
    private int outputPage;
    public LootFabricatorContainer(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, data.readBlockPos()); }
    public LootFabricatorContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.LOOT_FABRICATOR.get(), id, inventory, pos);
        if (baseMachineBE instanceof LootFabricatorBE machine) addDataSlots(machine.getMachineData());
        addShiftedPlayerSlots(player.getInventory());
    }

    @Override public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        for (int i = 0; i < LootFabricatorBE.INPUT_SLOTS; i++) addSlot(new SingleSlot(machineHandler, i, 20, 9 + i * 18));
        for (int i = 0; i < 16; i++) addSlot(new OutputSlot(machineHandler, i, 80 + (i % 4) * 18, 9 + (i / 4) * 18, this));
        if (baseMachineBE instanceof LootFabricatorBE machine) {
            for (int i = 0; i < LootFabricatorBE.UPGRADE_SLOTS; i++) {
                addSlot(new SingleSlot(machine.getUpgradeHandler(), i, 8 + (i % 9) * 18, 94 + (i / 9) * 18));
            }
        }
    }

    private void addShiftedPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        }
    }

    public int getProgress() { return baseMachineBE instanceof LootFabricatorBE machine ? machine.getMachineData().get(0) : 0; }
    public int getProgressMax() { return baseMachineBE instanceof LootFabricatorBE machine ? machine.getMachineData().get(1) : LootFabricatorBE.PROCESS_TIME; }
    public int getLifeFluidAmount() { return baseMachineBE instanceof LootFabricatorBE machine ? machine.getMachineData().get(3) : 0; }
    public int getTimeFluidAmount() { return baseMachineBE instanceof LootFabricatorBE machine ? machine.getMachineData().get(4) : 0; }
    public int getOutputPage() { return outputPage; }
    @Override public int jdte$getFilterPage() { return outputPage; }
    public int getMaxOutputPage() { return baseMachineBE instanceof LootFabricatorBE machine ? Math.max(0, (machine.getMachineData().get(2) - 1) / 16) : 0; }
    public void setOutputPage(int page) { outputPage = Math.clamp(page, 0, getMaxOutputPage()); }
    @Override public void jdte$setFilterPage(int page) { setOutputPage(page); }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.LOOT_FABRICATOR.get()); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerStart = 16 + LootFabricatorBE.UPGRADE_SLOTS + LootFabricatorBE.INPUT_SLOTS;
        if (index < playerStart) {
            if (!moveItemStackTo(stack, playerStart, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof SpawnEggItem) {
            if (!moveItemStackTo(stack, 0, LootFabricatorBE.INPUT_SLOTS, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof LootingUpgradeItem
                || stack.getItem() instanceof UpgradeCardItem card && card.getType() == UpgradeType.CAPACITY) {
            if (!moveItemStackTo(stack, 20, playerStart, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    private static class SingleSlot extends SlotItemHandler {
        SingleSlot(net.neoforged.neoforge.items.IItemHandler handler, int index, int x, int y) { super(handler, index, x, y); }
        @Override public int getMaxStackSize() { return 1; }
    }
    private static class OutputSlot extends SlotItemHandler {
        private final LootFabricatorContainer container;
        private final int pageSlot;
        OutputSlot(net.neoforged.neoforge.items.IItemHandler handler, int pageSlot, int x, int y, LootFabricatorContainer container) {
            super(handler, LootFabricatorBE.INPUT_SLOTS + pageSlot, x, y);
            this.container = container;
            this.pageSlot = pageSlot;
        }
        @Override public int getSlotIndex() { return LootFabricatorBE.INPUT_SLOTS + container.getOutputPage() * 16 + pageSlot; }
        @Override public ItemStack getItem() { return getItemHandler().getStackInSlot(getSlotIndex()); }
        @Override public void set(ItemStack stack) { ((net.neoforged.neoforge.items.IItemHandlerModifiable) getItemHandler()).setStackInSlot(getSlotIndex(), stack); setChanged(); }
        @Override public ItemStack remove(int amount) { return getItemHandler().extractItem(getSlotIndex(), amount, false); }
        @Override public int getMaxStackSize() { return getItemHandler().getSlotLimit(getSlotIndex()); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
