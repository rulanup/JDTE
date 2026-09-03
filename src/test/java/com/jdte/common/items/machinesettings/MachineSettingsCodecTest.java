package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockSwapperT1BE;
import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.ExperienceHolderBE;
import com.direwolf20.justdirethings.common.blockentities.ItemCollectorBE;
import com.direwolf20.justdirethings.common.blockentities.InventoryHolderBE;
import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.PlayerAccessorBE;
import com.direwolf20.justdirethings.common.blockentities.SensorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT2BE;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.AdvancedBioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.AdvancedGelGeneratorBE;
import com.jdte.common.blockentities.AdvancedLifeExtractorBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.ExtendedBioCrusherBE;
import com.jdte.common.blockentities.ExtendedBlockBreakerBE;
import com.jdte.common.blockentities.ExtendedBlockPlacerBE;
import com.jdte.common.blockentities.ExtendedBlockSwapperBE;
import com.jdte.common.blockentities.ExtendedClickerBE;
import com.jdte.common.blockentities.ExtendedDropperBE;
import com.jdte.common.blockentities.ExtendedEnergyTransmitterBE;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.blockentities.ExtendedFluidCollectorBE;
import com.jdte.common.blockentities.ExtendedFluidPlacerBE;
import com.jdte.common.blockentities.ExtendedGelGeneratorBE;
import com.jdte.common.blockentities.ExtendedSensorBE;
import com.jdte.common.blockentities.ExtendedTimeAcceleratorBE;
import com.jdte.common.blockentities.ExtendedTimeFreezerBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineSettingsCodecTest {
    private static final RegistryAccess.Frozen REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test
    void commonCodecCopiesTickSpeedAndDirection() {
        BaseMachineBE target = MachineSettingsTestFixtures.baseMachine();
        MachineSettingsCodec codec = MachineSettingsCodecRegistry.common();
        CompoundTag encoded = new CompoundTag();
        encoded.putInt("tickSpeed", 9);
        encoded.putInt("direction", 2);

        MachineSettingsCodec.PreparedSettings prepared = codec.decode(encoded, REGISTRIES).orElseThrow();
        prepared.apply(target);

        assertEquals(9, target.getTickSpeed());
        assertEquals(2, target.getDirection());
    }

    @Test
    void commonCodecEncodesOnlyTheCommonFields() {
        BaseMachineBE source = MachineSettingsTestFixtures.baseMachine();
        source.setTickSpeed(11);
        source.setDirection(4);

        CompoundTag encoded = MachineSettingsCodecRegistry.common().encode(source, REGISTRIES);

        assertEquals(2, encoded.getAllKeys().size());
        assertEquals(11, encoded.getInt("tickSpeed"));
        assertEquals(4, encoded.getInt("direction"));
        assertTrue(encoded.contains("tickSpeed"));
        assertTrue(encoded.contains("direction"));
    }

    @Test
    void commonCodecRejectsInvalidTickSpeed() {
        CompoundTag encoded = new CompoundTag();
        encoded.putInt("tickSpeed", 0);
        encoded.putInt("direction", 2);

        assertTrue(MachineSettingsCodecRegistry.common().decode(encoded, REGISTRIES).isEmpty());
    }

    @Test
    void commonCodecRejectsInvalidDirection() {
        CompoundTag encoded = new CompoundTag();
        encoded.putInt("tickSpeed", 1);
        encoded.putInt("direction", 6);

        assertTrue(MachineSettingsCodecRegistry.common().decode(encoded, REGISTRIES).isEmpty());
    }

    @Test
    void registryFindsARealJdtMachineByItsRegisteredType() {
        ClickerT2BE machine = MachineSettingsTestFixtures.clicker();
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType());

        assertFalse(MachineSettingsCodecRegistry.find(typeId, machine).isEmpty());
    }

    @Test
    void registryRejectsATypeIdWhenTheMachineInstanceDoesNotMatch() {
        BaseMachineBE machine = MachineSettingsTestFixtures.baseMachine();

        assertTrue(MachineSettingsCodecRegistry.find(
                MachineSettingsTestFixtures.jdtBlockEntityTypeId("ClickerT2BE"), machine).isEmpty());
    }

    @Test
    void fixtureFactoriesConstructTheRequestedRealMachines() {
        List<BaseMachineBE> machines = List.of(
                MachineSettingsTestFixtures.baseMachine(),
                MachineSettingsTestFixtures.clicker(),
                MachineSettingsTestFixtures.dropper(),
                MachineSettingsTestFixtures.sensor(),
                MachineSettingsTestFixtures.inventoryHolder(),
                MachineSettingsTestFixtures.playerAccessor(),
                MachineSettingsTestFixtures.paradox(),
                MachineSettingsTestFixtures.advancedTimeAccelerator(),
                MachineSettingsTestFixtures.extendedTimeAccelerator(),
                MachineSettingsTestFixtures.crystalIncubator(),
                MachineSettingsTestFixtures.greenhouse(),
                MachineSettingsTestFixtures.largeGreenhouse(),
                MachineSettingsTestFixtures.bioFactory(),
                MachineSettingsTestFixtures.lifeBreeder(),
                MachineSettingsTestFixtures.lifeExtractor(),
                MachineSettingsTestFixtures.lifeSynthesisVat(),
                MachineSettingsTestFixtures.mineralExtractor(),
                MachineSettingsTestFixtures.largeMineralExtractor(),
                MachineSettingsTestFixtures.advancedGelGenerator(),
                MachineSettingsTestFixtures.bioCrusher(),
                MachineSettingsTestFixtures.entitySuppressor(),
                MachineSettingsTestFixtures.rangeBlocker(),
                MachineSettingsTestFixtures.timeFreezer(),
                MachineSettingsTestFixtures.advancedEnergyTransmitter(),
                MachineSettingsTestFixtures.extendedExperienceHolder(),
                MachineSettingsTestFixtures.potionBrewer());

        assertEquals(26, machines.size());
        assertInstanceOf(ClickerT2BE.class, machines.get(1));
        assertInstanceOf(com.direwolf20.justdirethings.common.blockentities.DropperT2BE.class, machines.get(2));
        assertInstanceOf(com.direwolf20.justdirethings.common.blockentities.SensorT2BE.class, machines.get(3));
        assertInstanceOf(com.direwolf20.justdirethings.common.blockentities.InventoryHolderBE.class, machines.get(4));
        assertInstanceOf(com.direwolf20.justdirethings.common.blockentities.PlayerAccessorBE.class, machines.get(5));
        assertInstanceOf(com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE.class, machines.get(6));
        assertInstanceOf(AdvancedTimeAcceleratorBE.class, machines.get(7));
        assertInstanceOf(ExtendedTimeAcceleratorBE.class, machines.get(8));
        assertInstanceOf(CrystalIncubatorBE.class, machines.get(9));
        assertInstanceOf(GreenhouseBE.class, machines.get(10));
        assertInstanceOf(LargeGreenhouseBE.class, machines.get(11));
        assertInstanceOf(BioFactoryBE.class, machines.get(12));
        assertInstanceOf(LifeBreederBE.class, machines.get(13));
        assertInstanceOf(AdvancedLifeExtractorBE.class, machines.get(14));
        assertInstanceOf(LifeSynthesisVatBE.class, machines.get(15));
        assertInstanceOf(MineralExtractorBE.class, machines.get(16));
        assertInstanceOf(LargeMineralExtractorBE.class, machines.get(17));
        assertInstanceOf(AdvancedGelGeneratorBE.class, machines.get(18));
        assertInstanceOf(AdvancedBioCrusherBE.class, machines.get(19));
        assertInstanceOf(EntitySuppressorBE.class, machines.get(20));
        assertInstanceOf(RangeBlockerBE.class, machines.get(21));
        assertInstanceOf(TimeFreezerBE.class, machines.get(22));
        assertInstanceOf(AdvancedEnergyTransmitterBE.class, machines.get(23));
        assertInstanceOf(ExtendedExperienceHolderBE.class, machines.get(24));
        assertInstanceOf(AdvancedPotionBrewerBE.class, machines.get(25));
    }

    @Test
    void clickerCodecCopiesAllFiveClickSettings() {
        ClickerT2BE source = MachineSettingsTestFixtures.clicker();
        source.setClickerSettings(2, 1, true, false, 37);
        ClickerT2BE target = MachineSettingsTestFixtures.clicker();
        target.setClickerSettings(0, 0, false, true, 1);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(2, target.clickType);
        assertEquals(1, target.clickTarget.ordinal());
        assertTrue(target.sneaking);
        assertFalse(target.showFakePlayer);
        assertEquals(37, target.maxHoldTicks);
    }

    @Test
    void dropperCodecCopiesDropCountAndPickupDelay() {
        var source = MachineSettingsTestFixtures.dropper();
        source.setDropperSettings(7, 19);
        var target = MachineSettingsTestFixtures.dropper();
        target.setDropperSettings(1, 0);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(7, target.dropCount);
        assertEquals(19, target.pickupDelay);
    }

    @Test
    void blockBreakerCodecCopiesSneaking() {
        BlockBreakerT1BE source = MachineSettingsTestFixtures.blockBreakerT1();
        source.setBreakerSettings(true);
        BlockBreakerT1BE target = MachineSettingsTestFixtures.blockBreakerT1();
        target.setBreakerSettings(false);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.sneaking);
    }

    @Test
    void blockSwapperCodecCopiesSwapSettingsButNotBoundPartner() {
        BlockSwapperT1BE source = MachineSettingsTestFixtures.blockSwapperT1();
        source.setSwapperSettings(true, 5);
        source.setBoundTo(GlobalPos.of(Level.NETHER, new BlockPos(11, 12, 13)));
        BlockSwapperT1BE target = MachineSettingsTestFixtures.blockSwapperT1();
        GlobalPos targetBoundTo = GlobalPos.of(Level.OVERWORLD, new BlockPos(3, 4, 5));
        target.setBoundTo(targetBoundTo);
        target.setSwapperSettings(false, 0);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.swapBlocks);
        assertEquals(source.swap_entity_type, target.swap_entity_type);
        assertEquals(targetBoundTo, target.boundTo);
    }

    @Test
    void sensorCodecCopiesOrdinaryValuesWithoutApplyingBlockStateProperties() {
        var source = MachineSettingsTestFixtures.sensor();
        source.getFilterHandler().setStackInSlot(0, new ItemStack(Items.FURNACE));
        source.sense_target = SensorT1BE.SENSE_TARGET.values()[1];
        source.strongSignal = true;
        source.senseAmount = 13;
        source.equality = 2;
        source.addBlockStateProperty(0, MachineSettingsTestFixtures.blockStateProperties());
        var target = MachineSettingsTestFixtures.sensor();
        target.getFilterHandler().setStackInSlot(0, new ItemStack(Items.FURNACE));
        target.addBlockStateProperty(0, MachineSettingsTestFixtures.blockStateProperties(Direction.WEST));
        CompoundTag targetBlockStateProperties = target.saveBlockStateProperties();
        target.sense_target = SensorT1BE.SENSE_TARGET.values()[0];
        target.strongSignal = false;
        target.senseAmount = 0;
        target.equality = 0;

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(source.sense_target, target.sense_target);
        assertEquals(13, target.senseAmount);
        assertEquals(2, target.equality);
        assertTrue(target.strongSignal);
        assertEquals(targetBlockStateProperties, target.saveBlockStateProperties());
    }

    @Test
    void sensorCodecAppliesBlockStatePropertiesOnlyWhenFilterSettingsAreRequested() {
        var source = MachineSettingsTestFixtures.sensor();
        source.getFilterHandler().setStackInSlot(0, new ItemStack(Items.FURNACE));
        source.addBlockStateProperty(0, MachineSettingsTestFixtures.blockStateProperties());
        var target = MachineSettingsTestFixtures.sensor();
        target.getFilterHandler().setStackInSlot(0, new ItemStack(Items.FURNACE));
        target.addBlockStateProperty(0, MachineSettingsTestFixtures.blockStateProperties(Direction.WEST));
        CompoundTag targetBlockStateProperties = target.saveBlockStateProperties();
        MachineSettingsCodec.PreparedSettings prepared = preparedSettings(source);

        prepared.apply(target);
        assertEquals(targetBlockStateProperties, target.saveBlockStateProperties());

        prepared.applyFilterSettings(target);
        assertEquals(source.saveBlockStateProperties(), target.saveBlockStateProperties());
    }

    @Test
    void itemCollectorCodecCopiesPickupDelayAndParticles() {
        ItemCollectorBE source = MachineSettingsTestFixtures.itemCollector();
        source.setSettings(true, false);
        ItemCollectorBE target = MachineSettingsTestFixtures.itemCollector();
        target.setSettings(false, true);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.respectPickupDelay);
        assertFalse(target.showParticles);
    }

    @Test
    void experienceHolderCodecCopiesDisplaySettingsButNotStoredExperience() {
        ExperienceHolderBE source = MachineSettingsTestFixtures.experienceHolder();
        source.targetExp = 321;
        source.ownerOnly = true;
        source.collectExp = false;
        source.showParticles = true;
        source.exp = 7;
        ExperienceHolderBE target = MachineSettingsTestFixtures.experienceHolder();
        target.targetExp = 14;
        target.ownerOnly = false;
        target.collectExp = true;
        target.showParticles = false;
        target.exp = 83;
        Player targetPlayer = MachineSettingsTestFixtures.runtimePlayer();
        MachineSettingsTestFixtures.setExperienceHolderCurrentPlayer(target, targetPlayer);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(321, target.targetExp);
        assertTrue(target.ownerOnly);
        assertFalse(target.collectExp);
        assertTrue(target.showParticles);
        assertEquals(83, target.exp);
        assertSame(targetPlayer, MachineSettingsTestFixtures.experienceHolderCurrentPlayer(target));
    }

    @Test
    void energyTransmitterCodecCopiesParticlesButNotRuntimeTargets() {
        EnergyTransmitterBE source = MachineSettingsTestFixtures.energyTransmitter();
        source.setEnergyTransmitterSettings(true);
        EnergyTransmitterBE target = MachineSettingsTestFixtures.energyTransmitter();
        target.setEnergyTransmitterSettings(false);
        Set<BlockPos> targetPositions = MachineSettingsTestFixtures.energyTransmitterTargets(target);
        BlockPos targetPosition = new BlockPos(8, 9, 10);
        targetPositions.add(targetPosition);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.showParticles);
        assertEquals(Set.of(targetPosition), targetPositions);
    }

    @Test
    void inventoryHolderCodecCopiesOrdinarySettingsWithoutApplyingFilterHandler() {
        InventoryHolderBE source = MachineSettingsTestFixtures.inventoryHolder();
        source.saveSettings(true, true, true, false, false, true, 4);
        source.filterBasicHandler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        InventoryHolderBE target = MachineSettingsTestFixtures.inventoryHolder();
        target.saveSettings(false, false, false, true, true, false, 0);
        ItemStack targetStack = new ItemStack(Items.COBBLESTONE, 3);
        target.getMachineHandler().setStackInSlot(0, targetStack);
        target.filterBasicHandler.setStackInSlot(0, new ItemStack(Items.DIRT));
        CompoundTag targetFilterSettings = target.filterBasicHandler.serializeNBT(REGISTRIES);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.compareNBT);
        assertTrue(target.filtersOnly);
        assertFalse(target.automatedFiltersOnly);
        assertTrue(target.compareCounts);
        assertFalse(target.automatedCompareCounts);
        assertEquals(4, target.renderedSlot);
        assertTrue(target.renderPlayer);
        assertEquals(targetFilterSettings, target.filterBasicHandler.serializeNBT(REGISTRIES));
        assertEquals(targetStack, target.getMachineHandler().getStackInSlot(0));
    }

    @Test
    void inventoryHolderCodecAppliesFilterHandlerOnlyWhenFilterSettingsAreRequested() {
        InventoryHolderBE source = MachineSettingsTestFixtures.inventoryHolder();
        source.filterBasicHandler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        InventoryHolderBE target = MachineSettingsTestFixtures.inventoryHolder();
        target.filterBasicHandler.setStackInSlot(0, new ItemStack(Items.DIRT));
        CompoundTag targetFilterSettings = target.filterBasicHandler.serializeNBT(REGISTRIES);
        MachineSettingsCodec.PreparedSettings prepared = preparedSettings(source);

        prepared.apply(target);
        assertEquals(targetFilterSettings, target.filterBasicHandler.serializeNBT(REGISTRIES));

        prepared.applyFilterSettings(target);
        assertEquals(source.filterBasicHandler.serializeNBT(REGISTRIES), target.filterBasicHandler.serializeNBT(REGISTRIES));
    }

    @Test
    void inventoryHolderCodecRejectsAnInvalidFilterHandlerSizeWithoutMutatingTarget() {
        assertInventoryHolderFilterSnapshotRejected(filterSettings -> filterSettings.putInt("Size", 40));
    }

    @Test
    void inventoryHolderCodecRejectsAnOutOfRangeFilterHandlerSlotWithoutMutatingTarget() {
        assertInventoryHolderFilterSnapshotRejected(filterSettings ->
                filterSettings.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putInt("Slot", 41));
    }

    @Test
    void inventoryHolderCodecRejectsANonCompoundFilterHandlerEntryWithoutMutatingTarget() {
        assertInventoryHolderFilterSnapshotRejected(filterSettings -> {
            ListTag entries = new ListTag();
            entries.add(net.minecraft.nbt.StringTag.valueOf("not-an-item"));
            filterSettings.put("Items", entries);
        });
    }

    @Test
    void inventoryHolderCodecRejectsAnInvalidFilterHandlerItemWithoutMutatingTarget() {
        assertInventoryHolderFilterSnapshotRejected(filterSettings -> {
            CompoundTag invalidItem = new CompoundTag();
            invalidItem.putInt("Slot", 0);
            invalidItem.putString("id", "minecraft:not_a_real_item");
            ListTag entries = new ListTag();
            entries.add(invalidItem);
            filterSettings.put("Items", entries);
        });
    }

    @Test
    void playerAccessorCodecCopiesAllSixSidedInventoryTypes() {
        PlayerAccessorBE source = MachineSettingsTestFixtures.playerAccessor();
        for (Direction direction : Direction.values()) {
            source.updateSidedInventory(direction, direction.ordinal() % 3);
        }
        PlayerAccessorBE target = MachineSettingsTestFixtures.playerAccessor();
        for (Direction direction : Direction.values()) {
            target.updateSidedInventory(direction, (direction.ordinal() + 1) % 3);
        }

        MachineSettingsTestFixtures.applyCodec(source, target);

        for (Direction direction : Direction.values()) {
            assertEquals(direction.ordinal() % 3, target.sidedInventoryTypes.get(direction));
        }
    }

    @Test
    void playerAccessorCodecRejectsAnInvalidSidedInventoryType() {
        PlayerAccessorBE source = MachineSettingsTestFixtures.playerAccessor();
        source.updateSidedInventory(Direction.NORTH, 1);
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
        MachineSettingsCodec codec = MachineSettingsCodecRegistry.find(typeId, source).orElseThrow();
        CompoundTag encoded = codec.encode(source, REGISTRIES);
        encoded.getCompound("sidedInventoryTypes").putInt(Direction.NORTH.getSerializedName(), 3);

        assertTrue(codec.decode(encoded, REGISTRIES).isEmpty());
    }

    @Test
    void paradoxCodecCopiesOnlyRenderAndTargetSettings() {
        ParadoxMachineBE source = MachineSettingsTestFixtures.paradox();
        source.setRenderParadox(true, 2);
        CompoundTag sourceSnapshot = new CompoundTag();
        sourceSnapshot.putInt("marker", 42);
        source.snapshotData = sourceSnapshot;
        ParadoxMachineBE target = MachineSettingsTestFixtures.paradox();
        CompoundTag targetSnapshot = new CompoundTag();
        targetSnapshot.putInt("marker", 91);
        target.snapshotData = targetSnapshot;

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.renderParadox);
        assertEquals(2, target.targetType);
        assertSame(targetSnapshot, target.snapshotData);
    }

    @Test
    void registryFindsAllJdtTiersAndExtendedVariantsByExactType() {
        List<BaseMachineBE> machines = List.of(
                MachineSettingsTestFixtures.clickerT1(),
                MachineSettingsTestFixtures.clicker(),
                MachineSettingsTestFixtures.dropperT1(),
                MachineSettingsTestFixtures.dropper(),
                MachineSettingsTestFixtures.blockBreakerT1(),
                MachineSettingsTestFixtures.blockBreakerT2(),
                MachineSettingsTestFixtures.blockSwapperT1(),
                MachineSettingsTestFixtures.blockSwapperT2(),
                MachineSettingsTestFixtures.sensorT1(),
                MachineSettingsTestFixtures.sensor(),
                MachineSettingsTestFixtures.itemCollector(),
                MachineSettingsTestFixtures.experienceHolder(),
                MachineSettingsTestFixtures.energyTransmitter(),
                MachineSettingsTestFixtures.inventoryHolder(),
                MachineSettingsTestFixtures.playerAccessor(),
                MachineSettingsTestFixtures.paradox(),
                MachineSettingsTestFixtures.extendedClicker(),
                MachineSettingsTestFixtures.extendedBlockBreaker(),
                MachineSettingsTestFixtures.extendedBlockPlacer(),
                MachineSettingsTestFixtures.extendedBlockSwapper(),
                MachineSettingsTestFixtures.extendedDropper(),
                MachineSettingsTestFixtures.extendedSensor(),
                MachineSettingsTestFixtures.extendedFluidCollector(),
                MachineSettingsTestFixtures.extendedFluidPlacer(),
                MachineSettingsTestFixtures.extendedEnergyTransmitter(),
                MachineSettingsTestFixtures.extendedExperienceHolder());

        for (BaseMachineBE machine : machines) {
            ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType());
            assertTrue(MachineSettingsCodecRegistry.find(typeId, machine).isPresent(),
                    () -> "Missing Codec for exact block entity type " + typeId);
        }
    }

    @Test
    void registryUsesTheCommonCodecForExtendedBlockPlacer() {
        ExtendedBlockPlacerBE source = MachineSettingsTestFixtures.extendedBlockPlacer();
        source.setTickSpeed(9);
        source.setDirection(Direction.WEST.ordinal());
        ExtendedBlockPlacerBE target = MachineSettingsTestFixtures.extendedBlockPlacer();
        target.setTickSpeed(1);
        target.setDirection(Direction.NORTH.ordinal());

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(9, target.getTickSpeed());
        assertEquals(Direction.WEST.ordinal(), target.getDirection());
    }

    @Test
    void multiplierCodecCopiesEveryAdjustableMultiplier() {
        assertMultiplierCopied(MachineSettingsTestFixtures.advancedTimeAccelerator(),
                MachineSettingsTestFixtures.advancedTimeAccelerator(), 23,
                AdvancedTimeAcceleratorBE::setMultiplier, AdvancedTimeAcceleratorBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.extendedTimeAccelerator(),
                MachineSettingsTestFixtures.extendedTimeAccelerator(), 23,
                ExtendedTimeAcceleratorBE::setMultiplier, ExtendedTimeAcceleratorBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.crystalIncubator(),
                MachineSettingsTestFixtures.crystalIncubator(), 23,
                CrystalIncubatorBE::setMultiplier, CrystalIncubatorBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.greenhouse(),
                MachineSettingsTestFixtures.greenhouse(), 23,
                GreenhouseBE::setMultiplier, GreenhouseBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.largeGreenhouse(),
                MachineSettingsTestFixtures.largeGreenhouse(), 23,
                LargeGreenhouseBE::setMultiplier, LargeGreenhouseBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.bioFactory(),
                MachineSettingsTestFixtures.bioFactory(), 23,
                BioFactoryBE::setMultiplier, BioFactoryBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.lifeBreeder(),
                MachineSettingsTestFixtures.lifeBreeder(), 23,
                LifeBreederBE::setMultiplier, LifeBreederBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.lifeSynthesisVat(),
                MachineSettingsTestFixtures.lifeSynthesisVat(), 23,
                LifeSynthesisVatBE::setMultiplier, LifeSynthesisVatBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.mineralExtractor(),
                MachineSettingsTestFixtures.mineralExtractor(), 23,
                MineralExtractorBE::setMultiplier, MineralExtractorBE::getMultiplier);
        assertMultiplierCopied(MachineSettingsTestFixtures.largeMineralExtractor(),
                MachineSettingsTestFixtures.largeMineralExtractor(), 23,
                LargeMineralExtractorBE::setMultiplier, LargeMineralExtractorBE::getMultiplier);
    }

    @Test
    void modeCodecCopiesBreederExtractorCrusherSuppressorAndRangeBlockerModes() {
        LifeBreederBE breeder = MachineSettingsTestFixtures.lifeBreeder();
        breeder.setMode(LifeBreederBE.Mode.GROW_ONLY.ordinal());
        LifeBreederBE breederTarget = MachineSettingsTestFixtures.lifeBreeder();
        MachineSettingsTestFixtures.applyCodec(breeder, breederTarget);
        assertEquals(LifeBreederBE.Mode.GROW_ONLY, breederTarget.getMode());

        LifeExtractorBE extractor = MachineSettingsTestFixtures.lifeExtractor();
        extractor.setMode(LifeExtractorBE.MODE_ALL);
        LifeExtractorBE extractorTarget = MachineSettingsTestFixtures.lifeExtractor();
        MachineSettingsTestFixtures.applyCodec(extractor, extractorTarget);
        assertEquals(LifeExtractorBE.MODE_ALL, extractorTarget.getMode());

        BioCrusherBE crusher = MachineSettingsTestFixtures.bioCrusher();
        crusher.setMode(BioCrusherBE.MODE_FRIENDLY);
        BioCrusherBE crusherTarget = MachineSettingsTestFixtures.bioCrusher();
        MachineSettingsTestFixtures.applyCodec(crusher, crusherTarget);
        assertEquals(BioCrusherBE.MODE_FRIENDLY, crusherTarget.getMode());

        EntitySuppressorBE suppressor = MachineSettingsTestFixtures.entitySuppressor();
        suppressor.setSettings(EntitySuppressorBE.Mode.DISABLE_ENTITY_RENDERING.ordinal(),
                EntitySuppressorBE.Target.NON_LIVING.ordinal(), true);
        EntitySuppressorBE suppressorTarget = MachineSettingsTestFixtures.entitySuppressor();
        MachineSettingsTestFixtures.applyCodec(suppressor, suppressorTarget);
        assertEquals(EntitySuppressorBE.Mode.DISABLE_ENTITY_RENDERING, suppressorTarget.getMode());
        assertEquals(EntitySuppressorBE.Target.NON_LIVING, suppressorTarget.getTarget());
        assertTrue(suppressorTarget.isBlacklist());

        RangeBlockerBE blocker = MachineSettingsTestFixtures.rangeBlocker();
        blocker.setSettings(RangeBlockerBE.Mode.SILENCE.ordinal(),
                EntitySuppressorBE.Target.SELECTED_TYPES.ordinal(), true);
        RangeBlockerBE blockerTarget = MachineSettingsTestFixtures.rangeBlocker();
        MachineSettingsTestFixtures.applyCodec(blocker, blockerTarget);
        assertEquals(RangeBlockerBE.Mode.SILENCE, blockerTarget.getMode());
        assertEquals(EntitySuppressorBE.Target.SELECTED_TYPES, blockerTarget.getTarget());
        assertTrue(blockerTarget.isBlacklist());
    }

    @Test
    void toggleCodecCopiesFreezerGelGeneratorAndEnergyTransmitterSettings() {
        TimeFreezerBE freezer = MachineSettingsTestFixtures.timeFreezer();
        freezer.setTimeFreezeEnabled(false);
        freezer.setWeatherFreezeEnabled(true);
        TimeFreezerBE freezerTarget = MachineSettingsTestFixtures.timeFreezer();
        freezerTarget.setTimeFreezeEnabled(true);
        freezerTarget.setWeatherFreezeEnabled(false);
        MachineSettingsTestFixtures.applyCodec(freezer, freezerTarget);
        assertFalse(freezerTarget.isTimeFreezeEnabled());
        assertTrue(freezerTarget.isWeatherFreezeEnabled());

        ExtendedTimeFreezerBE extendedFreezer = MachineSettingsTestFixtures.extendedTimeFreezer();
        extendedFreezer.setTimeFreezeEnabled(false);
        extendedFreezer.setWeatherFreezeEnabled(false);
        ExtendedTimeFreezerBE extendedFreezerTarget = MachineSettingsTestFixtures.extendedTimeFreezer();
        MachineSettingsTestFixtures.applyCodec(extendedFreezer, extendedFreezerTarget);
        assertFalse(extendedFreezerTarget.isTimeFreezeEnabled());
        assertFalse(extendedFreezerTarget.isWeatherFreezeEnabled());

        AdvancedGelGeneratorBE gelGenerator = MachineSettingsTestFixtures.advancedGelGenerator();
        gelGenerator.setAutoBalanceInputs(true);
        AdvancedGelGeneratorBE gelGeneratorTarget = MachineSettingsTestFixtures.advancedGelGenerator();
        MachineSettingsTestFixtures.applyCodec(gelGenerator, gelGeneratorTarget);
        assertTrue(gelGeneratorTarget.isAutoBalanceInputs());

        ExtendedGelGeneratorBE extendedGelGenerator = MachineSettingsTestFixtures.extendedGelGenerator();
        extendedGelGenerator.setAutoBalanceInputs(true);
        ExtendedGelGeneratorBE extendedGelGeneratorTarget = MachineSettingsTestFixtures.extendedGelGenerator();
        MachineSettingsTestFixtures.applyCodec(extendedGelGenerator, extendedGelGeneratorTarget);
        assertTrue(extendedGelGeneratorTarget.isAutoBalanceInputs());

        AdvancedEnergyTransmitterBE transmitter = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        transmitter.setShowParticles(false);
        AdvancedEnergyTransmitterBE transmitterTarget = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        MachineSettingsTestFixtures.applyCodec(transmitter, transmitterTarget);
        assertFalse(transmitterTarget.isShowingParticles());
    }

    @Test
    void potionBrewerCodecCopiesLockedTemplatesAndFuelInputWithoutBrewingRuntime() {
        AdvancedPotionBrewerBE source = MachineSettingsTestFixtures.potionBrewer();
        NonNullList<ItemStack> templates = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        templates.set(AdvancedPotionBrewerBE.INGREDIENT_SLOT, new ItemStack(Items.NETHER_WART));
        source.applyCopiedRecipeLock(true, templates);
        source.setFuelInputEnabled(false);

        AdvancedPotionBrewerBE target = MachineSettingsTestFixtures.potionBrewer();
        target.setFuelInputEnabled(true);
        target.brewerData.set(0, 8);
        target.brewerData.set(2, 17);
        ItemStack targetInput = new ItemStack(Items.POTION);
        target.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.BOTTLE_SLOT_0, targetInput.copy());
        target.getWaterFluidTank().fill(new FluidStack(Fluids.WATER, 123), IFluidHandler.FluidAction.EXECUTE);
        target.energyStorage.setEnergy(91);

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertTrue(target.isRecipeLocked());
        ItemStack copiedTemplate = target.getLockedRecipeTemplate(AdvancedPotionBrewerBE.INGREDIENT_SLOT);
        assertTrue(ItemStack.isSameItemSameComponents(
                templates.get(AdvancedPotionBrewerBE.INGREDIENT_SLOT), copiedTemplate));
        assertEquals(1, copiedTemplate.getCount());
        assertFalse(target.isFuelInputEnabled());
        assertEquals(8, target.getBrewProgress());
        assertEquals(17, target.getFuel());
        assertTrue(ItemStack.isSameItemSameComponents(targetInput,
                target.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.BOTTLE_SLOT_0)));
        assertEquals(123, target.getWaterFluidTank().getFluidAmount());
        assertEquals(91, target.energyStorage.getEnergyStored());
    }

    @Test
    void advancedEnergyTransmitterCodecCopiesPlayerBindingAndParticlesWithoutChangingPlacedBy() {
        UUID boundPlayer = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID targetOwner = UUID.fromString("22222222-2222-2222-2222-222222222222");
        AdvancedEnergyTransmitterBE source = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        source.applyCopiedPlayerBinding(Optional.of(boundPlayer), "BoundPlayer");
        source.setShowParticles(false);

        AdvancedEnergyTransmitterBE target = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        target.setPlacedBy(targetOwner);
        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(Optional.of(boundPlayer), target.getBoundPlayerIdForCopy());
        assertEquals("BoundPlayer", target.getBoundPlayerNameForCopy());
        assertFalse(target.isShowingParticles());
        assertEquals(targetOwner, MachineSettingsTestFixtures.targetOwnerId(target));
    }

    @Test
    void advancedEnergyTransmitterCodecCanClearAnExistingPlayerBinding() {
        AdvancedEnergyTransmitterBE source = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        AdvancedEnergyTransmitterBE target = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        target.applyCopiedPlayerBinding(Optional.of(
                UUID.fromString("33333333-3333-3333-3333-333333333333")), "PreviousPlayer");

        MachineSettingsTestFixtures.applyCodec(source, target);

        assertEquals(Optional.empty(), target.getBoundPlayerIdForCopy());
        assertEquals("", target.getBoundPlayerNameForCopy());
    }

    @Test
    void jdteCodecRejectsMalformedSpecializedSettingsBeforeMutatingTarget() {
        LifeBreederBE source = MachineSettingsTestFixtures.lifeBreeder();
        source.setMultiplier(7);
        LifeBreederBE target = MachineSettingsTestFixtures.lifeBreeder();
        target.setMultiplier(3);
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
        MachineSettingsCodec codec = MachineSettingsCodecRegistry.find(typeId, source).orElseThrow();
        CompoundTag encoded = codec.encode(source, REGISTRIES);
        encoded.putString("mode", "not_a_mode");

        assertTrue(codec.decode(encoded, REGISTRIES).isEmpty());
        assertEquals(3, target.getMultiplier());
    }

    @Test
    void jdteCodecRejectsOutOfRangeMultiplierMalformedBindingAndInvalidRecipeTemplate() {
        LifeBreederBE breeder = MachineSettingsTestFixtures.lifeBreeder();
        MachineSettingsCodec breederCodec = codecFor(breeder);
        CompoundTag badMultiplier = breederCodec.encode(breeder, REGISTRIES);
        badMultiplier.putInt("multiplier", Integer.MAX_VALUE);
        assertTrue(breederCodec.decode(badMultiplier, REGISTRIES).isEmpty());

        AdvancedEnergyTransmitterBE transmitter = MachineSettingsTestFixtures.advancedEnergyTransmitter();
        MachineSettingsCodec transmitterCodec = codecFor(transmitter);
        CompoundTag badBinding = transmitterCodec.encode(transmitter, REGISTRIES);
        badBinding.putBoolean("hasBoundPlayer", true);
        badBinding.putString("boundPlayerId", "not-a-uuid");
        badBinding.putString("boundPlayerName", "BoundPlayer");
        assertTrue(transmitterCodec.decode(badBinding, REGISTRIES).isEmpty());

        AdvancedPotionBrewerBE brewer = MachineSettingsTestFixtures.potionBrewer();
        NonNullList<ItemStack> templates = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        templates.set(AdvancedPotionBrewerBE.INGREDIENT_SLOT, new ItemStack(Items.NETHER_WART));
        brewer.applyCopiedRecipeLock(true, templates);
        MachineSettingsCodec brewerCodec = codecFor(brewer);
        CompoundTag badTemplate = brewerCodec.encode(brewer, REGISTRIES);
        badTemplate.getCompound("recipeLockTemplates").put("slot_0",
                new ItemStack(Items.NETHER_WART).saveOptional(REGISTRIES));
        assertTrue(brewerCodec.decode(badTemplate, REGISTRIES).isEmpty());
    }

    @Test
    void registryCoversEveryCurrentJdteAndJdtBaseMachineTypeExactly() {
        Set<ResourceLocation> expected = new HashSet<>(JDTEBlockEntities.BLOCK_ENTITIES.getEntries().stream()
                .map(holder -> typeId(holder.get()))
                .collect(Collectors.toSet()));
        expected.addAll(Set.of(
                typeId(Registration.ItemCollectorBE.get()),
                typeId(Registration.BlockBreakerT1BE.get()),
                typeId(Registration.BlockBreakerT2BE.get()),
                typeId(Registration.BlockPlacerT1BE.get()),
                typeId(Registration.BlockPlacerT2BE.get()),
                typeId(Registration.ClickerT1BE.get()),
                typeId(Registration.ClickerT2BE.get()),
                typeId(Registration.SensorT1BE.get()),
                typeId(Registration.SensorT2BE.get()),
                typeId(Registration.DropperT1BE.get()),
                typeId(Registration.DropperT2BE.get()),
                typeId(Registration.GeneratorT1BE.get()),
                typeId(Registration.GeneratorFluidT1BE.get()),
                typeId(Registration.EnergyTransmitterBE.get()),
                typeId(Registration.BlockSwapperT1BE.get()),
                typeId(Registration.BlockSwapperT2BE.get()),
                typeId(Registration.PlayerAccessorBE.get()),
                typeId(Registration.FluidPlacerT1BE.get()),
                typeId(Registration.FluidPlacerT2BE.get()),
                typeId(Registration.FluidCollectorT1BE.get()),
                typeId(Registration.FluidCollectorT2BE.get()),
                typeId(Registration.ParadoxMachineBE.get()),
                typeId(Registration.InventoryHolderBE.get()),
                typeId(Registration.ExperienceHolderBE.get())));

        assertEquals(expected, MachineSettingsCodecRegistry.registeredTypeIds());
    }

    private static <T extends BaseMachineBE> void assertMultiplierCopied(
            T source, T target, int expected, BiConsumer<T, Integer> setter, ToIntFunction<T> getter) {
        setter.accept(source, expected);
        MachineSettingsTestFixtures.applyCodec(source, target);
        assertEquals(expected, getter.applyAsInt(target));
    }

    private static MachineSettingsCodec codecFor(BaseMachineBE machine) {
        return MachineSettingsCodecRegistry.find(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType()), machine).orElseThrow();
    }

    private static ResourceLocation typeId(BlockEntityType<?> type) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
    }

    private void assertInventoryHolderFilterSnapshotRejected(Consumer<CompoundTag> tamper) {
        InventoryHolderBE source = MachineSettingsTestFixtures.inventoryHolder();
        source.filterBasicHandler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        InventoryHolderBE target = MachineSettingsTestFixtures.inventoryHolder();
        target.saveSettings(false, false, false, true, true, false, 0);
        target.filterBasicHandler.setStackInSlot(0, new ItemStack(Items.DIRT));
        CompoundTag targetFilterSettings = target.filterBasicHandler.serializeNBT(REGISTRIES);
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
        MachineSettingsCodec codec = MachineSettingsCodecRegistry.find(typeId, source).orElseThrow();
        CompoundTag encoded = codec.encode(source, REGISTRIES);

        tamper.accept(encoded.getCompound("filterBasicHandler"));

        assertTrue(codec.decode(encoded, REGISTRIES).isEmpty());
        assertFalse(target.compareNBT);
        assertEquals(targetFilterSettings, target.filterBasicHandler.serializeNBT(REGISTRIES));
    }

    private MachineSettingsCodec.PreparedSettings preparedSettings(BaseMachineBE source) {
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
        MachineSettingsCodec codec = MachineSettingsCodecRegistry.find(typeId, source).orElseThrow();
        return codec.decode(codec.encode(source, REGISTRIES), REGISTRIES).orElseThrow();
    }
}
