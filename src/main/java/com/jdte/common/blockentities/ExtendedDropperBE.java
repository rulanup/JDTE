package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.DropperT2BE;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ExtendedDropperBE extends DropperT2BE implements ExtendedUpgradeMachine {
    public ExtendedDropperBE(BlockPos pPos, BlockState pBlockState) {
        super(JDTEBlockEntities.EXTENDED_DROPPER.get(), pPos, pBlockState);
    }

    @Override
    public ItemStackHandler getMachineHandler() {
        ItemStackHandler handler = getData(Registration.MACHINE_HANDLER);
        if (handler.getSlots() < MACHINE_SLOTS) {
            ItemStackHandler resized = new ItemStackHandler(MACHINE_SLOTS);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) resized.setStackInSlot(i, stack);
            }
            setData(Registration.MACHINE_HANDLER, resized);
            setChanged();
            return resized;
        }
        return handler;
    }
}
