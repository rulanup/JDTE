package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.slots.RefinedFuelSlot;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ExtendedFluidGeneratorContainer extends BaseMachineContainer {
    private int fluidAmount;
    @Override public int getFluidAmount() { return player.level().isClientSide?fluidAmount:((com.jdte.common.blockentities.ExtendedFluidGeneratorBE)baseMachineBE).getFluidTank().getFluidAmount(); }
    public ExtendedFluidGeneratorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedFluidGeneratorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_FLUID_GENERATOR.get(), windowId, playerInventory, blockPos);
        addDataSlots(new SplitIntContainerData(new net.minecraft.world.inventory.ContainerData() {
            public int getCount() { return 1; }
            public int get(int key) { return player.level().isClientSide?fluidAmount:((com.jdte.common.blockentities.ExtendedFluidGeneratorBE)baseMachineBE).getFluidTank().getFluidAmount(); }
            public void set(int key,int value) { fluidAmount=value; }
        }));
        addPlayerSlots(playerInventory);
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addFuelSlotRange(machineHandler, 0, 80, 13, 1, 18);
    }

    protected int addFuelSlotRange(IItemHandler handler, int index, int x, int y, int count, int dx) {
        for (int i = 0; i < count; i++) {
            addSlot(new RefinedFuelSlot(handler, index++, x, y));
            x += dx;
        }
        return index;
    }

    @Override
    public boolean stillValid(Player player) {
        return baseMachineBE != null && !baseMachineBE.isRemoved() && player.level().getBlockEntity(pos)==baseMachineBE && stillValid(ContainerLevelAccess.create(player.level(), pos), player,
                JDTEBlocks.EXTENDED_FLUID_GENERATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return stillValid(player)?super.quickMoveStack(player,index):ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
    @Override public void clicked(int slot,int button,net.minecraft.world.inventory.ClickType type,Player player) {
        if(stillValid(player))super.clicked(slot,button,type,player);
    }
}
