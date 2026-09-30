package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.ExtendedEnergyTransmitterBE;
import com.jdte.common.items.EnergyOverloadUpgradeItem;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyOverloadUpgradeTest {

    @Test
    void itemRegistrationAndProperties() {
        assertNotNull(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get());
        assertInstanceOf(EnergyOverloadUpgradeItem.class, JDTEItems.ENERGY_OVERLOAD_UPGRADE.get());

        ItemStack stack = new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get());
        assertEquals(1, stack.getMaxStackSize());
        assertTrue(UpgradeHelper.isUpgrade(stack));
        assertTrue(UpgradeHelper.isEnergyOverloadUpgrade(stack));
        assertTrue(UpgradeCardInsertionHelper.isUpgradeCard(stack));
    }

    @Test
    void onlyEnergyTransmittersAreRecognized() {
        AdvancedEnergyTransmitterBE advanced = new AdvancedEnergyTransmitterBE(
                BlockPos.ZERO, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());
        ExtendedEnergyTransmitterBE extended = new ExtendedEnergyTransmitterBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get().defaultBlockState());
        GeneratorT1BE generator = new GeneratorT1BE(BlockPos.ZERO,
                com.direwolf20.justdirethings.setup.Registration.GeneratorT1.get().defaultBlockState());

        assertTrue(UpgradeHelper.isEnergyTransmitter(advanced));
        assertTrue(UpgradeHelper.isEnergyTransmitter(extended));
        assertFalse(UpgradeHelper.isEnergyTransmitter(generator));
    }

    @Test
    void handlerAcceptsOverloadOnlyOnTransmittersAndAtMostOne() {
        AdvancedEnergyTransmitterBE advanced = new AdvancedEnergyTransmitterBE(
                BlockPos.ZERO, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());
        UpgradeItemStackHandler advancedHandler = UpgradeHelper.getUpgradeHandler(advanced);
        ItemStack overloadStack = new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get());

        assertTrue(advancedHandler.isItemValid(0, overloadStack));
        advancedHandler.setStackInSlot(0, overloadStack);
        assertTrue(advanced.hasEnergyOverloadUpgrade());

        // Cannot insert a second overload upgrade into another slot
        assertFalse(advancedHandler.isItemValid(1, overloadStack));

        // Non-energy transmitter machine handler must reject it
        GeneratorT1BE generator = new GeneratorT1BE(BlockPos.ZERO,
                com.direwolf20.justdirethings.setup.Registration.GeneratorT1.get().defaultBlockState());
        UpgradeItemStackHandler generatorHandler = UpgradeHelper.getUpgradeHandler(generator);
        assertFalse(generatorHandler.isItemValid(0, overloadStack));
    }

    @Test
    void extendedEnergyTransmitterFePerTickReflectsOverload() {
        ExtendedEnergyTransmitterBE extended = new ExtendedEnergyTransmitterBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get().defaultBlockState()) {
            @Override
            public int getMaxEnergy() {
                return 1000;
            }
        };
        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(extended);

        assertFalse(UpgradeHelper.hasEnergyOverloadUpgrade(extended));
        // Without overload, delegates to super.fePerTick() which accesses unloaded JDT Config in test env
        assertThrows(IllegalStateException.class, extended::fePerTick);

        // With overload, intercepts and returns Integer.MAX_VALUE cleanly
        handler.setStackInSlot(0, new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()));
        assertTrue(UpgradeHelper.hasEnergyOverloadUpgrade(extended));
        assertEquals(Integer.MAX_VALUE, extended.fePerTick());

        // When removed, reverts to super.fePerTick()
        handler.setStackInSlot(0, ItemStack.EMPTY);
        assertFalse(UpgradeHelper.hasEnergyOverloadUpgrade(extended));
        assertThrows(IllegalStateException.class, extended::fePerTick);
    }
}
