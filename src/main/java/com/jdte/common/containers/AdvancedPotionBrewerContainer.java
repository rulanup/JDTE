package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AdvancedPotionBrewerContainer extends BaseMachineContainer {
    public final ContainerData brewerData;
    private final ContainerData waterFluidData;
    private final ContainerData timeFluidData;

    public AdvancedPotionBrewerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedPotionBrewerContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_POTION_BREWER.get(), windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof AdvancedPotionBrewerBE brewer) {
            brewerData = brewer.brewerData;
            addDataSlots(brewerData);
            waterFluidData = brewer.getWaterFluidData();
            addDataSlots(waterFluidData);
            timeFluidData = brewer.getTimeFluidData();
            addDataSlots(timeFluidData);
        } else {
            brewerData = null;
            waterFluidData = null;
            timeFluidData = null;
        }
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addSlot(new SingleItemSlot(machineHandler, AdvancedPotionBrewerBE.BOTTLE_SLOT_0, 56, 41));
        addSlot(new SingleItemSlot(machineHandler, AdvancedPotionBrewerBE.BOTTLE_SLOT_1, 79, 48));
        addSlot(new SingleItemSlot(machineHandler, AdvancedPotionBrewerBE.BOTTLE_SLOT_2, 102, 41));
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.INGREDIENT_SLOT, 79, 7));
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.FUEL_SLOT, 17, 7));
        for (int i = 0; i < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT; i++) {
            addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + i, 43 + i * 18, -21));
        }
        for (int i = 0; i < AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT; i++) {
            addSlot(new OutputSlot(machineHandler, AdvancedPotionBrewerBE.OUTPUT_SLOT_START + i, 128, 13 + i * 18));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack currentStack = slot.getItem();
        ItemStack originalStack = currentStack.copy();
        int playerStart = Math.max(AdvancedPotionBrewerBE.TOTAL_SLOTS, slots.size() - 36);
        int playerEnd = slots.size();

        if (index < AdvancedPotionBrewerBE.TOTAL_SLOTS) {
            if (!moveStackToRange(currentStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= playerStart) {
            if (!movePlayerStackToBrewer(player, currentStack)) {
                return super.quickMoveStack(player, index);
            }
        } else {
            return super.quickMoveStack(player, index);
        }

        if (currentStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (currentStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, currentStack);
        return originalStack;
    }

    private boolean movePlayerStackToBrewer(Player player, ItemStack stack) {
        if (stack.is(Items.BLAZE_POWDER)) {
            return moveToSingleSlot(stack, AdvancedPotionBrewerBE.FUEL_SLOT);
        }
        if (isPotionInput(player, stack)) {
            return moveToSlots(stack,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_0,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_1,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_2);
        }
        if (isBrewingIngredient(player, stack)) {
            return moveToSlots(stack, getIngredientSlots());
        }
        return false;
    }

    private boolean moveToSingleSlot(ItemStack stack, int slot) {
        return moveStackToRange(stack, slot, slot + 1, false);
    }

    private boolean moveToSlots(ItemStack stack, int... targetSlots) {
        boolean moved = false;
        for (int targetSlot : targetSlots) {
            if (stack.isEmpty()) {
                break;
            }
            if (moveToSingleSlot(stack, targetSlot)) {
                moved = true;
            }
        }
        return moved;
    }

    private int[] getIngredientSlots() {
        int[] slots = new int[1 + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT];
        slots[0] = AdvancedPotionBrewerBE.INGREDIENT_SLOT;
        for (int i = 0; i < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT; i++) {
            slots[i + 1] = AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + i;
        }
        return slots;
    }

    private boolean isPotionInput(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        PotionBrewing brewing = player.level().potionBrewing();
        return brewing.isInput(stack)
                || stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.GLASS_BOTTLE);
    }

    private boolean isBrewingIngredient(Player player, ItemStack stack) {
        return !stack.isEmpty() && player.level().potionBrewing().isIngredient(stack);
    }

    private boolean moveStackToRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean moved = false;
        int i = reverseDirection ? endIndex - 1 : startIndex;

        while (!stack.isEmpty() && isInMoveRange(i, startIndex, endIndex, reverseDirection)) {
            Slot slot = slots.get(i);
            if (slot.hasItem() && slot.mayPlace(stack)) {
                ItemStack existing = slot.getItem();
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    int maxSize = Math.min(slot.getMaxStackSize(existing), existing.getMaxStackSize());
                    int movable = Math.min(stack.getCount(), maxSize - existing.getCount());
                    if (movable > 0) {
                        stack.shrink(movable);
                        existing.grow(movable);
                        slot.setChanged();
                        moved = true;
                    }
                }
            }
            i += reverseDirection ? -1 : 1;
        }

        i = reverseDirection ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && isInMoveRange(i, startIndex, endIndex, reverseDirection)) {
            Slot slot = slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                int maxSize = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                int movable = Math.min(stack.getCount(), maxSize);
                if (movable > 0) {
                    ItemStack movedStack = stack.copyWithCount(movable);
                    stack.shrink(movable);
                    slot.set(movedStack);
                    slot.setChanged();
                    moved = true;
                }
            }
            i += reverseDirection ? -1 : 1;
        }

        return moved;
    }

    private boolean isInMoveRange(int index, int startIndex, int endIndex, boolean reverseDirection) {
        return reverseDirection ? index >= startIndex : index < endIndex;
    }

    public int getFuel() {
        return brewerData == null ? 0 : brewerData.get(2);
    }

    public int getBrewProgress() {
        return brewerData == null ? 0 : brewerData.get(0);
    }

    public int getBrewProgressMax() {
        return brewerData == null ? 1 : Math.max(1, brewerData.get(1));
    }

    public boolean isRecipeLocked() {
        return brewerData != null && brewerData.get(3) != 0;
    }

    public boolean isFuelInputEnabled() {
        return brewerData != null && brewerData.get(4) != 0;
    }

    public int getWaterFluidAmount() {
        return getFluidAmount(waterFluidData);
    }

    public Fluid getWaterFluidType() {
        return getFluidType(waterFluidData);
    }

    public FluidStack getWaterFluidStack() {
        return new FluidStack(getWaterFluidType(), getWaterFluidAmount());
    }

    public int getTimeFluidAmount() {
        return getFluidAmount(timeFluidData);
    }

    public Fluid getTimeFluidType() {
        return getFluidType(timeFluidData);
    }

    public FluidStack getTimeFluidStack() {
        return new FluidStack(getTimeFluidType(), getTimeFluidAmount());
    }

    public int getPotionBrewerFluidCapacity() {
        return baseMachineBE instanceof AdvancedPotionBrewerBE brewer ? brewer.getMaxMB() : AdvancedPotionBrewerBE.BASE_FLUID_CAPACITY;
    }

    private int getFluidAmount(ContainerData data) {
        return data == null ? 0 : ((data.get(2) << 16) | data.get(1));
    }

    private Fluid getFluidType(ContainerData data) {
        return data == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(data.get(0));
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.ADVANCED_POTION_BREWER.get());
    }

    private static class SingleItemSlot extends SlotItemHandler {
        private SingleItemSlot(net.neoforged.neoforge.items.IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    private static class OutputSlot extends SingleItemSlot {
        private OutputSlot(net.neoforged.neoforge.items.IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
