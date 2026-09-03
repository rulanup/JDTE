package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.items.machinesettings.MachineSettingsTestFixtures;
import com.jdte.common.items.machinesettings.MachineSettingsCodec;
import com.jdte.common.items.machinesettings.MachineSettingsCodecRegistry;
import com.jdte.common.items.machinesettings.MachineSettingsSnapshot;
import com.jdte.common.items.machinesettings.MachineUpgradeHandlers;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedMachineSettingsCopierItemTest {
    private static final HolderLookup.Provider REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test
    void sameTypeMachinePastesParentSettingsAndAllSixAutoIoSides() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0b11_1111, 0b10_1010, true);
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0b00_0001, 0b00_0010, true);

        copier.saveSettings(null, source, stack);
        copier.loadSettings(null, target, stack);

        assertEquals(1, copier.baseLoadCalls);
        assertEquals(Optional.of(ResourceLocation.parse("justdirethings:clicker_t1")),
                AdvancedMachineSettingsCopierData.readMachineType(copiedData(stack)));
        assertEquals(Optional.of(new AdvancedMachineSettingsCopierData.Masks(0b11_1111, 0b10_1010)),
                AdvancedMachineSettingsCopierData.readMasks(copiedData(stack)));
        assertEquals(7, target.parentSetting);
        assertEquals(0b11_1111, target.inputMask);
        assertEquals(0b10_1010, target.outputMask);
    }

    @Test
    void saveAndLoadCopiesCommonCustomAutoIoAndAllUpgradesForSameType() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7,
                0b11_1111, 0b10_1010, true);
        source.setTickSpeed(9);
        source.setDirection(2);
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2,
                0b00_0001, 0b00_0010, true);
        target.setTickSpeed(3);
        target.setDirection(4);

        copier.saveSettings(null, source, stack);
        copier.loadSettings(null, target, stack);

        assertEquals(7, target.parentSetting);
        assertEquals(9, target.getTickSpeed());
        assertEquals(2, target.getDirection());
        assertEquals(0b11_1111, target.inputMask);
        assertEquals(0b10_1010, target.outputMask);
        assertEquals(JDTEItems.CAPACITY_UPGRADE.get(),
                UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
        assertTrue(MachineSettingsSnapshot.read(copiedData(stack)).isPresent());
    }

    @Test
    void sameTypeMachinePastesInstalledUpgradeCards() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0, 0, false);
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, false);

        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0,
                new ItemStack(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get()));

        copier.saveSettings(null, source, stack);
        copier.loadSettings(null, target, stack);

        assertEquals(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get(),
                UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
    }

    @Test
    void upgradePasteConsumesMatchingCardsFromPlayerInventory() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0, 0, false);
        Inventory inventory = new Inventory(null);
        copier.inventory = inventory;
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0,
                new ItemStack(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get()));
        copier.saveSettings(null, source, stack);
        inventory.setItem(0, new ItemStack(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get()));

        assertTrue(copier.consumeUpgradeCost(null, source, stack, null));
        assertTrue(inventory.getItem(0).isEmpty());
    }

    @Test
    void upgradePasteIsRejectedWithoutAllMatchingCards() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0, 0, false);
        Inventory inventory = new Inventory(null);
        copier.inventory = inventory;
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0,
                new ItemStack(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get()));
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(1,
                new ItemStack(com.jdte.setup.JDTEItems.OVERCLOCK_UPGRADE.get()));
        copier.saveSettings(null, source, stack);
        inventory.setItem(0, new ItemStack(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get()));

        assertFalse(copier.consumeUpgradeCost(null, source, stack, null));
        assertEquals(com.jdte.setup.JDTEItems.CAPACITY_UPGRADE.get(), inventory.getItem(0).getItem());
    }

    @Test
    void bioCrusherCopiesLootingAndSharpnessHandlers() {
        BioCrusherBE source = MachineSettingsTestFixtures.bioCrusher();
        source.getLootingHandler().setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get(), 2));
        source.getSharpnessHandler().setStackInSlot(0, new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get()));

        CompoundTag copiedData = new CompoundTag();
        MachineUpgradeHandlers.write(copiedData, source, REGISTRIES);

        BioCrusherBE target = MachineSettingsTestFixtures.bioCrusher();
        MachineUpgradeHandlers.prepare(copiedData, target, REGISTRIES).orElseThrow().apply(target);

        assertEquals(JDTEItems.LOOTING_UPGRADE.get(), target.getLootingHandler().getStackInSlot(0).getItem());
        assertEquals(2, target.getLootingHandler().getStackInSlot(0).getCount());
        assertEquals(JDTEItems.SHARPNESS_UPGRADE.get(), target.getSharpnessHandler().getStackInSlot(0).getItem());
    }

    @Test
    void customStandardUpgradeHandlersAppearOnceAndRoundTrip() {
        BioFactoryBE bioFactory = MachineSettingsTestFixtures.bioFactory();
        UpgradeHelper.getUpgradeHandler(bioFactory).setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
        assertSingleStandardHandlerRoundTrips(bioFactory, MachineSettingsTestFixtures.bioFactory(),
                JDTEItems.LOOTING_UPGRADE.get());

        AdvancedPotionBrewerBE brewer = MachineSettingsTestFixtures.potionBrewer();
        UpgradeHelper.getUpgradeHandler(brewer).setStackInSlot(0, new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get()));
        assertSingleStandardHandlerRoundTrips(brewer, MachineSettingsTestFixtures.potionBrewer(),
                JDTEItems.ENERGY_BREWING_UPGRADE.get());

        LootFabricatorBE fabricator = new LootFabricatorBE(BlockPos.ZERO,
                JDTEBlocks.LOOT_FABRICATOR.get().defaultBlockState());
        UpgradeHelper.getUpgradeHandler(fabricator).setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
        LootFabricatorBE fabricatorTarget = new LootFabricatorBE(BlockPos.ZERO,
                JDTEBlocks.LOOT_FABRICATOR.get().defaultBlockState());
        assertSingleStandardHandlerRoundTrips(fabricator, fabricatorTarget, JDTEItems.LOOTING_UPGRADE.get());
    }

    @Test
    void missingDedicatedUpgradePreventsPasteWithoutConsumingOtherCards() {
        BioCrusherBE source = MachineSettingsTestFixtures.bioCrusher();
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        source.getLootingHandler().setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get(), 2));
        source.getSharpnessHandler().setStackInSlot(0, new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get()));

        CompoundTag copiedData = new CompoundTag();
        MachineUpgradeHandlers.write(copiedData, source, REGISTRIES);
        AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType()));
        ItemStack copierStack = new ItemStack(Items.STICK);
        copierStack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

        TrackingCopierItem copier = newTrackingCopier();
        Inventory inventory = new Inventory(null);
        copier.inventory = inventory;
        inventory.setItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        inventory.setItem(1, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
        inventory.setItem(2, new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get()));

        assertFalse(copier.consumeUpgradeCost(null, source, copierStack, null));
        assertEquals(1, inventory.getItem(0).getCount());
        assertEquals(1, inventory.getItem(1).getCount());
        assertEquals(1, inventory.getItem(2).getCount());
    }

    @Test
    void malformedNamedUpgradeHandlerIsRejectedInsteadOfFallingBackToLegacyData() {
        BioCrusherBE source = MachineSettingsTestFixtures.bioCrusher();
        UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        source.getLootingHandler().setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
        CompoundTag copiedData = new CompoundTag();
        MachineUpgradeHandlers.write(copiedData, source, REGISTRIES);

        CompoundTag handlers = copiedData.getCompound(AdvancedMachineSettingsCopierData.UPGRADE_HANDLERS_KEY);
        CompoundTag looting = handlers.getCompound(MachineUpgradeHandlers.LOOTING_HANDLER);
        looting.putInt("Size", 0);
        handlers.put(MachineUpgradeHandlers.LOOTING_HANDLER, looting);
        copiedData.put(AdvancedMachineSettingsCopierData.UPGRADE_HANDLERS_KEY, handlers);

        BioCrusherBE target = MachineSettingsTestFixtures.bioCrusher();
        assertTrue(MachineUpgradeHandlers.prepare(copiedData, target, REGISTRIES).isEmpty());
        assertTrue(UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).isEmpty());
        assertTrue(target.getLootingHandler().getStackInSlot(0).isEmpty());
    }

    @Test
    void legacyStandardUpgradeTagStillLoads() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, false);
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
                ResourceLocation.parse("justdirethings:clicker_t1"));
        ItemStackHandler legacyHandler = new ItemStackHandler(UpgradeHelper.getUpgradeHandler(target).getSlots());
        legacyHandler.setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        AdvancedMachineSettingsCopierData.writeUpgrades(copiedData, legacyHandler.serializeNBT(REGISTRIES));
        stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

        copier.loadSettings(null, target, stack);

        assertEquals(JDTEItems.CAPACITY_UPGRADE.get(),
                UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
    }

    @Test
    void differentBlockEntityTypesRefuseToPasteBeforeBaseSettingsCanChange() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0b11_1111, 0b10_1010, true);
        TestMachine target = new TestMachine("justdirethings:block_placer_t1", 2, 0b00_0001, 0b00_0010, true);
        target.setTickSpeed(3);

        copier.saveSettings(null, source, stack);
        copier.loadSettings(null, target, stack);

        assertEquals(0, copier.baseLoadCalls);
        assertEquals(2, target.parentSetting);
        assertEquals(3, target.getTickSpeed());
        assertEquals(0b00_0001, target.inputMask);
        assertEquals(0b00_0010, target.outputMask);
    }

    @Test
    void malformedCustomSettingsDoNotPartiallyApplyParentSettings() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
                ResourceLocation.parse("justdirethings:clicker_t1"));
        CompoundTag settings = new CompoundTag();
        settings.putInt("schemaVersion", MachineSettingsSnapshot.CURRENT_SCHEMA);
        settings.putString("machineType", "justdirethings:clicker_t1");
        settings.putInt("tickSpeed", 0);
        settings.putInt("direction", 1);
        settings.put("custom", new CompoundTag());
        copiedData.put(AdvancedMachineSettingsCopierData.MACHINE_SETTINGS_KEY, settings);
        copiedData.putInt(TrackingCopierItem.PARENT_SETTING_KEY, 91);
        stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, true);

        copier.loadSettings(null, target, stack);

        assertEquals(0, copier.baseLoadCalls);
        assertEquals(2, target.parentSetting);
    }

    @Test
    void malformedCustomSettingsDoNotConsumeUpgradeCards() {
        TrackingCopierItem copier = newTrackingCopier();
        Inventory inventory = new Inventory(null);
        copier.inventory = inventory;
        ItemStack stack = new ItemStack(Items.STICK);
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
                ResourceLocation.parse("justdirethings:clicker_t1"));
        ItemStackHandler upgrades = new ItemStackHandler(4);
        upgrades.setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        AdvancedMachineSettingsCopierData.writeUpgrades(copiedData, upgrades.serializeNBT(REGISTRIES));
        CompoundTag settings = new CompoundTag();
        settings.putInt("schemaVersion", MachineSettingsSnapshot.CURRENT_SCHEMA);
        settings.putString("machineType", "justdirethings:clicker_t1");
        settings.putInt("tickSpeed", 0);
        settings.putInt("direction", 1);
        settings.put("custom", new CompoundTag());
        copiedData.put(AdvancedMachineSettingsCopierData.MACHINE_SETTINGS_KEY, settings);
        stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));
        inventory.setItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));

        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, false);

        assertFalse(copier.consumeUpgradeCost(null, target, stack, null));
        assertEquals(JDTEItems.CAPACITY_UPGRADE.get(), inventory.getItem(0).getItem());
        assertEquals(1, inventory.getItem(0).getCount());
    }

    @Test
    void conflictingSnapshotCommonFieldsDoNotPartiallyApplyParentSettings() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0, 0, false);
        source.setTickSpeed(9);
        copier.saveSettings(null, source, stack);

        CompoundTag copiedData = copiedData(stack);
        CompoundTag settings = copiedData.getCompound(AdvancedMachineSettingsCopierData.MACHINE_SETTINGS_KEY);
        CompoundTag custom = settings.getCompound("custom");
        custom.putInt("tickSpeed", 8);
        settings.put("custom", custom);
        copiedData.put(AdvancedMachineSettingsCopierData.MACHINE_SETTINGS_KEY, settings);
        stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, false);
        target.setTickSpeed(3);
        copier.loadSettings(null, target, stack);

        assertEquals(0, copier.baseLoadCalls);
        assertEquals(2, target.parentSetting);
        assertEquals(3, target.getTickSpeed());
    }

    @Test
    void copierFlagsStillControlParentSettingsWhileAdvancedSettingsRemainAvailable() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        MachineSettingsCopier.setSettings(stack, false, false, false, false);
        TestMachine source = new TestMachine("justdirethings:clicker_t1", 7, 0b11_1111, 0b10_1010, true);
        source.setTickSpeed(9);
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0b00_0001, 0b00_0010, true);
        target.setTickSpeed(3);

        copier.saveSettings(null, source, stack);
        copier.loadSettings(null, target, stack);

        assertEquals(2, target.parentSetting);
        assertEquals(9, target.getTickSpeed());
        assertEquals(0b11_1111, target.inputMask);
        assertEquals(0b10_1010, target.outputMask);
    }

    @Test
    void oldCopiedDataStillLoadsParentUpgradesAndAutoIo() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2,
                0b00_0001, 0b00_0010, true);
        CompoundTag copiedData = new CompoundTag();
        copiedData.putInt(TrackingCopierItem.PARENT_SETTING_KEY, 7);
        AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
                ResourceLocation.parse("justdirethings:clicker_t1"));
        AdvancedMachineSettingsCopierData.writeMasks(copiedData, 0b11_1111, 0b10_1010);
        ItemStackHandler upgrades = new ItemStackHandler(UpgradeHelper.getUpgradeHandler(target).getSlots());
        upgrades.setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
        AdvancedMachineSettingsCopierData.writeUpgrades(copiedData, upgrades.serializeNBT(REGISTRIES));
        stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

        copier.loadSettings(null, target, stack);

        assertEquals(7, target.parentSetting);
        assertEquals(0b11_1111, target.inputMask);
        assertEquals(0b10_1010, target.outputMask);
        assertEquals(JDTEItems.CAPACITY_UPGRADE.get(),
                UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
        assertTrue(MachineSettingsSnapshot.read(copiedData).isEmpty());
    }

    @Test
    void legacyCopiedDataWithoutTypeFingerprintIsRejected() {
        TrackingCopierItem copier = newTrackingCopier();
        ItemStack stack = new ItemStack(Items.STICK);
        TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0b00_0001, 0b00_0010, true);

        CompoundTag copiedData = new CompoundTag();
        copiedData.putInt(TrackingCopierItem.PARENT_SETTING_KEY, 7);
        AdvancedMachineSettingsCopierData.writeMasks(copiedData, 0b11_1111, 0b10_1010);
        stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

        copier.loadSettings(null, target, stack);

        assertEquals(0, copier.baseLoadCalls);
        assertEquals(2, target.parentSetting);
        assertEquals(0b00_0001, target.inputMask);
        assertEquals(0b00_0010, target.outputMask);
    }

    @Test
    void exactTypeFingerprintsFromJdteJdtAndDynaNamespacesAreAccepted() {
        List<String> typeIds = List.of(
                "jdte:advanced_item_sender",
                "justdirethings:clicker_t1",
                "justdynathings:energy_t1");

        for (String typeId : typeIds) {
            TrackingCopierItem copier = newTrackingCopier();
            ItemStack stack = new ItemStack(Items.STICK);
            TestMachine source = new TestMachine(typeId, 9, 0b11_0000, 0b00_1111, true);
            TestMachine target = new TestMachine(typeId, 1, 0, 0, true);

            copier.saveSettings(null, source, stack);
            copier.loadSettings(null, target, stack);

            assertEquals(1, copier.baseLoadCalls, typeId);
            assertEquals(9, target.parentSetting, typeId);
            assertEquals(0b11_0000, target.inputMask, typeId);
            assertEquals(0b00_1111, target.outputMask, typeId);
        }
    }

    private static net.minecraft.nbt.CompoundTag copiedData(ItemStack stack) {
        return stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag();
    }

    private static void assertSingleStandardHandlerRoundTrips(BaseMachineBE source, BaseMachineBE target,
                                                               net.minecraft.world.item.Item expectedUpgrade) {
        assertEquals(List.of(MachineUpgradeHandlers.STANDARD_HANDLER),
                MachineUpgradeHandlers.all(source).stream().map(MachineUpgradeHandlers.NamedHandler::name).toList());
        CompoundTag copiedData = new CompoundTag();
        MachineUpgradeHandlers.write(copiedData, source, REGISTRIES);
        MachineUpgradeHandlers.prepare(copiedData, target, REGISTRIES).orElseThrow().apply(target);
        assertEquals(expectedUpgrade, UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
    }

    private static TrackingCopierItem newTrackingCopier() {
        try {
            Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
            return (TrackingCopierItem) unsafe.allocateInstance(TrackingCopierItem.class);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to allocate TrackingCopierItem for test", e);
        }
    }

    private static final class TrackingCopierItem extends AdvancedMachineSettingsCopierItem {
        private static final String PARENT_SETTING_KEY = "testParentSetting";

        private int baseLoadCalls;
        private Inventory inventory;

        private boolean consumeUpgradeCost(net.minecraft.world.level.Level level, BaseMachineBE machine,
                                            ItemStack stack, Player player) {
            return super.consumeUpgradeCost(player, level, machine, stack);
        }

        @Override
        protected void saveBaseSettings(net.minecraft.world.level.Level level,
                                        net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                        ItemStack stack) {
            CompoundTag copiedData = new CompoundTag();
            if (MachineSettingsCopier.getCopyArea(stack)) {
                copiedData.putInt(PARENT_SETTING_KEY, ((TestMachine) blockEntity).parentSetting);
            }
            stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));
        }

        @Override
        protected void loadBaseSettings(net.minecraft.world.level.Level level,
                                        net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                        ItemStack stack) {
            baseLoadCalls++;
            if (MachineSettingsCopier.getCopyArea(stack)) {
                ((TestMachine) blockEntity).parentSetting = copiedData(stack).getInt(PARENT_SETTING_KEY);
            }
        }

        @Override
        protected ResourceLocation getBlockEntityTypeId(BaseMachineBE machine) {
            return machine instanceof TestMachine testMachine
                    ? testMachine.typeId
                    : super.getBlockEntityTypeId(machine);
        }

        @Override
        protected boolean hasConfigurableIo(BaseMachineBE machine) {
            return machine instanceof TestMachine testMachine
                    ? testMachine.configurableIo
                    : super.hasConfigurableIo(machine);
        }

        @Override
        protected int getInputMask(BaseMachineBE machine) {
            return machine instanceof TestMachine testMachine
                    ? testMachine.inputMask
                    : super.getInputMask(machine);
        }

        @Override
        protected int getOutputMask(BaseMachineBE machine) {
            return machine instanceof TestMachine testMachine
                    ? testMachine.outputMask
                    : super.getOutputMask(machine);
        }

        @Override
        protected void setMasks(BaseMachineBE machine, int inputMask, int outputMask) {
            if (machine instanceof TestMachine testMachine) {
                testMachine.inputMask = inputMask;
                testMachine.outputMask = outputMask;
                return;
            }
            super.setMasks(machine, inputMask, outputMask);
        }

        @Override
        protected HolderLookup.Provider getRegistryAccess(net.minecraft.world.level.Level level) {
            return REGISTRIES;
        }

        @Override
        protected Optional<MachineSettingsCodec> getMachineSettingsCodec(ResourceLocation machineType,
                                                                          BaseMachineBE machine) {
            return machine instanceof TestMachine
                    ? Optional.of(MachineSettingsCodecRegistry.common())
                    : super.getMachineSettingsCodec(machineType, machine);
        }

        @Override
        protected Inventory getPlayerInventory(Player player) {
            return inventory;
        }
    }

    private static final class TestMachine extends BaseMachineBE {
        private final ResourceLocation typeId;
        private final boolean configurableIo;
        private int parentSetting;
        private int inputMask;
        private int outputMask;

        private TestMachine(String typeId, int parentSetting, int inputMask, int outputMask, boolean configurableIo) {
            super(BlockEntityType.SIGN, BlockPos.ZERO, Blocks.OAK_SIGN.defaultBlockState());
            this.typeId = ResourceLocation.parse(typeId);
            this.parentSetting = parentSetting;
            this.inputMask = inputMask;
            this.outputMask = outputMask;
            this.configurableIo = configurableIo;
        }
    }
}
