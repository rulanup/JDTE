package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.DropperT2BE;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class ExtendedDropperBE extends DropperT2BE implements ExtendedUpgradeMachine {
    public ExtendedDropperBE(BlockPos pPos, BlockState pBlockState) {
        super(JDTEBlockEntities.EXTENDED_DROPPER.get(), pPos, pBlockState);
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        ItemStacksResourceHandler handler = getData(JDTRegistration.MACHINE_HANDLER);
        if (handler.size() < MACHINE_SLOTS) {
            ItemStacksResourceHandler resized = new MachineInventoryHandler(MACHINE_SLOTS);
            for (int i = 0; i < handler.size(); i++) {
                resized.set(i, handler.getResource(i), handler.getAmountAsInt(i));
            }
            setData(JDTRegistration.MACHINE_HANDLER, resized);
            setChanged();
            return resized;
        }
        return handler;
    }
}
