package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT2BE;
import com.jdte.common.blockentities.AdvancedBioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.AdvancedGelGeneratorBE;
import com.jdte.common.blockentities.AdvancedLifeExtractorBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.ExtendedBioCrusherBE;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.blockentities.ExtendedTimeAcceleratorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.TimeFreezerBE;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
}
