package com.jdte.common.blockentities;

import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LargeMineralExtractorCapacityTest {
    @Test
    void ultimateCapacityAllowsOversizedOutputStacks() {
        LargeMineralExtractorBE extractor = new LargeMineralExtractorBE(
                BlockPos.ZERO, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState());
        UpgradeItemStackHandler upgrades = UpgradeHelper.getUpgradeHandler(extractor);

        upgrades.setStackInSlot(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()));

        assertEquals(6_400_000, extractor.getOutputSlotLimit());
        assertEquals(6_400_000, extractor.getMachineHandler().getSlotLimit(extractor.outputStartSlot()));
    }
}
