package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.slots.FuelSlot;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.items.IItemHandler;

public class ExtendedGeneratorContainer extends BaseMachineContainer {
    private final int[] counters=new int[2];
    public ExtendedGeneratorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedGeneratorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_GENERATOR.get(), windowId, playerInventory, blockPos);
        addDataSlots(new SplitIntContainerData(new ContainerData() {
            public int getCount() { return 2; }
            public int get(int key) {
                if (player.level().isClientSide) return counters[key];
                var generator=(com.jdte.common.blockentities.ExtendedGeneratorBE)baseMachineBE;
                return key==0?generator.burnRemaining:generator.maxBurn;
            }
            public void set(int key,int value) { counters[key]=value; }
        }));
        addPlayerSlots(playerInventory);
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addFuelSlotRange(machineHandler, 0, 80, 13, 1, 18);
    }

    public int getBurnRemaining() {
        return player.level().isClientSide?counters[0]:((com.jdte.common.blockentities.ExtendedGeneratorBE)baseMachineBE).burnRemaining;
    }

    public int getMaxBurn() {
        return player.level().isClientSide?counters[1]:((com.jdte.common.blockentities.ExtendedGeneratorBE)baseMachineBE).maxBurn;
    }

    protected int addFuelSlotRange(IItemHandler handler, int index, int x, int y, int count, int dx) {
        for (int i = 0; i < count; i++) {
            addSlot(new FuelSlot(handler, index++, x, y));
            x += dx;
        }
        return index;
    }

    @Override
    public boolean stillValid(Player player) {
        return baseMachineBE != null && !baseMachineBE.isRemoved() && player.level().getBlockEntity(pos)==baseMachineBE && stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_GENERATOR.get());
    }

    private int dataValue(int index) {
        ContainerData containerData = data;
        return containerData == null ? 0 : containerData.get(index);
    }
    @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player player,int index) {
        return stillValid(player)?super.quickMoveStack(player,index):net.minecraft.world.item.ItemStack.EMPTY;
    }
    @Override public void clicked(int slot,int button,net.minecraft.world.inventory.ClickType type,Player player) {
        if(stillValid(player))super.clicked(slot,button,type,player);
    }
}
