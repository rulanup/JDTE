package com.jdte.common.utils;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineDropDataHelperTest {
    private static final HolderLookup.Provider REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test
    void removesOnlyInventoriesThatOnRemoveDropsSeparately() {
        CompoundTag machineData = new CompoundTag();
        machineData.putInt("tickspeed", 7);
        machineData.putInt("direction", 4);
        machineData.putInt("exp", 1_234);
        machineData.putString("unknownMachineSetting", "preserve-me");
        machineData.putByteArray("rawPayload", new byte[]{3, 1, 4});
        machineData.put("inventory", serializedHandler(Items.DIAMOND));
        machineData.put("factoryPackageSlot", serializedHandler(Items.CHEST));
        machineData.put("upgrades", serializedHandler(Items.EMERALD));
        machineData.put("lootingHandler", serializedHandler(Items.GOLD_INGOT));
        machineData.put("sharpnessHandler", serializedHandler(Items.IRON_INGOT));

        CompoundTag energy = new CompoundTag();
        energy.putInt("energy", 98_765);
        CompoundTag autoIo = new CompoundTag();
        autoIo.putString("north", "output");
        CompoundTag attachments = new CompoundTag();
        attachments.put("justdirethings:machine_handler", serializedHandler(Items.COAL));
        attachments.put("justdirethings:generator_item_handler", serializedHandler(Items.CHARCOAL));
        attachments.put("justdirethings:generator_fluid_item_handler", serializedHandler(Items.BUCKET));
        attachments.put("jdte:upgrade_handler", serializedHandler(Items.REDSTONE));
        attachments.put("jdte:extended_upgrade_handler", serializedHandler(Items.LAPIS_LAZULI));
        attachments.put("justdirethings:energystorage_machines", energy);
        attachments.put("jdte:auto_io_config", autoIo);
        machineData.put("neoforge:attachments", attachments);

        ItemStack machineStack = stackWithMachineData(machineData);

        MachineDropDataHelper.removeSeparatelyDroppedItems(machineStack);

        CustomData customData = machineStack.get(JustDireDataComponents.CUSTOM_DATA_1);
        assertNotNull(customData);
        CompoundTag cleaned = customData.copyTag();
        assertEquals(7, cleaned.getInt("tickspeed"));
        assertEquals(4, cleaned.getInt("direction"));
        assertEquals(1_234, cleaned.getInt("exp"));
        assertEquals("preserve-me", cleaned.getString("unknownMachineSetting"));
        assertArrayEquals(new byte[]{3, 1, 4}, cleaned.getByteArray("rawPayload"));
        assertFalse(cleaned.contains("inventory"));
        assertFalse(cleaned.contains("factoryPackageSlot"));
        assertFalse(cleaned.contains("upgrades"));
        assertFalse(cleaned.contains("lootingHandler"));
        assertFalse(cleaned.contains("sharpnessHandler"));

        CompoundTag cleanedAttachments = cleaned.getCompound("neoforge:attachments");
        assertFalse(cleanedAttachments.contains("justdirethings:machine_handler"));
        assertFalse(cleanedAttachments.contains("justdirethings:generator_item_handler"));
        assertFalse(cleanedAttachments.contains("justdirethings:generator_fluid_item_handler"));
        assertFalse(cleanedAttachments.contains("jdte:upgrade_handler"));
        assertFalse(cleanedAttachments.contains("jdte:extended_upgrade_handler"));
        assertEquals(98_765,
                cleanedAttachments.getCompound("justdirethings:energystorage_machines").getInt("energy"));
        assertEquals("output", cleanedAttachments.getCompound("jdte:auto_io_config").getString("north"));
    }

    @Test
    void preservedSettingsRoundTripThroughARealMachineWithoutRestoringItsInventory() {
        UUID owner = UUID.randomUUID();
        BaseMachineBE source = machineWithOneSlot();
        source.setTickSpeed(9);
        source.setDirection(3);
        source.setPlacedBy(owner);
        source.getMachineHandler().setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        CompoundTag saved = new CompoundTag();
        source.saveAdditional(saved, REGISTRIES);
        assertTrue(saved.getCompound("neoforge:attachments")
                .contains("justdirethings:machine_handler"));

        ItemStack machineStack = stackWithMachineData(saved);
        MachineDropDataHelper.removeSeparatelyDroppedItems(machineStack);

        CompoundTag cleaned = machineStack.get(JustDireDataComponents.CUSTOM_DATA_1).copyTag();
        BaseMachineBE restored = machineWithOneSlot();
        restored.loadCustomOnly(cleaned, REGISTRIES);

        assertEquals(9, restored.getTickSpeed());
        assertEquals(3, restored.getDirection());
        assertEquals(owner, restored.placedByUUID);
        assertTrue(restored.getMachineHandler().getStackInSlot(0).isEmpty());
    }

    @Test
    void experienceAndMachineSettingsSurviveWhileUpgradeCardsStaySeparate() {
        ExtendedExperienceHolderBE source = new ExtendedExperienceHolderBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState());
        source.setTickSpeed(11);
        source.setDirection(2);
        source.exp = 54_321;
        source.targetExp = 37;
        source.collectExp = true;
        source.ownerOnly = true;
        source.showParticles = false;
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(
                0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        CompoundTag saved = new CompoundTag();
        source.saveAdditional(saved, REGISTRIES);
        assertTrue(saved.getCompound("neoforge:attachments")
                .contains("jdte:upgrade_handler"));

        ItemStack machineStack = stackWithMachineData(saved);
        MachineDropDataHelper.removeSeparatelyDroppedItems(machineStack);

        CompoundTag cleaned = machineStack.get(JustDireDataComponents.CUSTOM_DATA_1).copyTag();
        ExtendedExperienceHolderBE restored = new ExtendedExperienceHolderBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState());
        restored.loadCustomOnly(cleaned, REGISTRIES);

        assertEquals(11, restored.getTickSpeed());
        assertEquals(2, restored.getDirection());
        assertEquals(54_321, restored.exp);
        assertEquals(37, restored.targetExp);
        assertTrue(restored.collectExp);
        assertTrue(restored.ownerOnly);
        assertFalse(restored.showParticles);
        assertTrue(UpgradeHelper.getUpgradeHandler(restored).getStackInSlot(0).isEmpty());
    }

    @Test
    void removesCustomDataOnlyWhenNoPersistentSettingsRemain() {
        CompoundTag machineData = new CompoundTag();
        machineData.put("inventory", serializedHandler(Items.DIAMOND));
        CompoundTag attachments = new CompoundTag();
        attachments.put("jdte:upgrade_handler", serializedHandler(Items.REDSTONE));
        machineData.put("neoforge:attachments", attachments);
        ItemStack machineStack = stackWithMachineData(machineData);

        MachineDropDataHelper.removeSeparatelyDroppedItems(machineStack);

        assertFalse(machineStack.has(JustDireDataComponents.CUSTOM_DATA_1));
    }

    private static BaseMachineBE machineWithOneSlot() {
        BaseMachineBE machine = new BaseMachineBE(
                BlockEntityType.SIGN, BlockPos.ZERO, Blocks.OAK_SIGN.defaultBlockState());
        machine.MACHINE_SLOTS = 1;
        return machine;
    }

    private static ItemStack stackWithMachineData(CompoundTag machineData) {
        ItemStack stack = new ItemStack(Items.IRON_BLOCK);
        stack.set(JustDireDataComponents.CUSTOM_DATA_1, CustomData.of(machineData));
        return stack;
    }

    private static CompoundTag serializedHandler(net.minecraft.world.item.Item item) {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, new ItemStack(item));
        return handler.serializeNBT(REGISTRIES);
    }
}
