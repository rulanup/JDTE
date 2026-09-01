package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
