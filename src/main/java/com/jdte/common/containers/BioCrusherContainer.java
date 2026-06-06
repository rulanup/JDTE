package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class BioCrusherContainer extends BaseMachineContainer {
    protected ContainerData bioCrusherData;

    protected BioCrusherContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof BioCrusherBE crusher) {
            bioCrusherData = crusher.getBioCrusherData();
            addDataSlots(bioCrusherData);
        }
        addPlayerSlots(playerInventory);
    }

    @Override
    public void addMachineSlots() {
        if (baseMachineBE instanceof BioCrusherBE crusher) {
            // Output slots (16 slots = 4x4 grid for drops)
            for (int i = 0; i < 16; i++) {
                int row = i / 4;
                int col = i % 4;
                addSlot(new SlotItemHandler(crusher.getMachineHandler(), i, 8 + col * 18, 18 + row * 18));
            }

            // Looting upgrade slots (2 slots)
            for (int i = 0; i < 2; i++) {
                addSlot(new SlotItemHandler(crusher.getLootingHandler(), i, 152, 18 + i * 18) {
                    @Override
                    public boolean mayPlace(net.minecraft.world.item.ItemStack stack) {
                        return stack.getItem() instanceof LootingUpgradeItem;
                    }
                });
            }

            // Sharpness upgrade slots (2 slots)
            for (int i = 0; i < 2; i++) {
                addSlot(new SlotItemHandler(crusher.getSharpnessHandler(), i, 152, 72 + i * 18) {
                    @Override
                    public boolean mayPlace(net.minecraft.world.item.ItemStack stack) {
                        return stack.getItem() instanceof SharpnessUpgradeItem;
                    }
                });
            }
        }
    }

    public int getMode() {
        return bioCrusherData == null ? 0 : bioCrusherData.get(0);
    }

    public int getProgress() {
        return bioCrusherData == null ? 0 : bioCrusherData.get(1);
    }

    public int getProcessTime() {
        return bioCrusherData == null ? 20 : bioCrusherData.get(2);
    }
}
