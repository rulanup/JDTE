package com.jdte.common.mixin;

import com.jdte.common.utils.DedicatedUpgradeDropHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseMachineBlockMixinTest {
    @Test
    void dropsAndClearsDedicatedUpgradeHandlers() {
        ItemStackHandler handler = new ItemStackHandler(2);
        handler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        List<ItemStack> drops = new ArrayList<>();

        DedicatedUpgradeDropHelper.drop(handler, drops::add);

        assertEquals(1, drops.size());
        assertEquals(Items.DIAMOND, drops.getFirst().getItem());
        assertTrue(handler.getStackInSlot(0).isEmpty());
    }
}
