package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.jdte.client.screens.ExtendedGeneratorScreen;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedJdtMachineBehaviorTest {
    @Test
    void extendedGeneratorUsesItsOwnTypeAndEightUpgradeSlots() {
        ExtendedGeneratorBE machine = new ExtendedGeneratorBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState());

        assertInstanceOf(GeneratorT1BE.class, machine);
        assertInstanceOf(ExtendedUpgradeMachine.class, machine);
        assertEquals(JDTEBlockEntities.EXTENDED_GENERATOR.get(), machine.getType());
        assertEquals(ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT,
                UpgradeHelper.getUpgradeHandler(machine).getSlots());

        ResourceLocation generatorId = ResourceLocation.fromNamespaceAndPath("jdte", "extended_generator");
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.get(), BuiltInRegistries.BLOCK.get(generatorId));
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.getId(), BuiltInRegistries.BLOCK.getKey(JDTEBlocks.EXTENDED_GENERATOR.get()));
        assertEquals(JDTEBlocks.EXTENDED_GENERATOR.getId(), BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.get(generatorId)));
        assertEquals(generatorId, JDTEMenus.EXTENDED_GENERATOR.getId());
    }

    @Test
    void extendedFluidGeneratorKeepsJdtFluidGeneratorContract() {
        ExtendedFluidGeneratorBE machine = new ExtendedFluidGeneratorBE(
                BlockPos.ZERO, JDTEBlocks.EXTENDED_FLUID_GENERATOR.get().defaultBlockState());

        assertInstanceOf(GeneratorFluidT1BE.class, machine);
        assertInstanceOf(ExtendedUpgradeMachine.class, machine);
        assertEquals(JDTEBlockEntities.EXTENDED_FLUID_GENERATOR.get(), machine.getType());
        assertEquals(ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT,
                UpgradeHelper.getUpgradeHandler(machine).getSlots());

        ResourceLocation generatorId = ResourceLocation.fromNamespaceAndPath("jdte", "extended_fluid_generator");
        assertEquals(JDTEBlocks.EXTENDED_FLUID_GENERATOR.get(), BuiltInRegistries.BLOCK.get(generatorId));
        assertEquals(JDTEBlocks.EXTENDED_FLUID_GENERATOR.getId(),
                BuiltInRegistries.BLOCK.getKey(JDTEBlocks.EXTENDED_FLUID_GENERATOR.get()));
        assertEquals(JDTEBlocks.EXTENDED_FLUID_GENERATOR.getId(),
                BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.get(generatorId)));
        assertEquals(generatorId, JDTEMenus.EXTENDED_FLUID_GENERATOR.getId());
    }

    @Test
    void extendedGeneratorContainerBindsItsOwnMenuTypeAndStillValidBlockCheck() throws Exception {
        Path projectRoot = findProjectRoot(Path.of(System.getProperty("user.dir", "")).toAbsolutePath());
        assertTrue(projectRoot != null, "Could not locate project root from test runtime path");
        String source = Files.readString(projectRoot.resolve(
                "src/main/java/com/jdte/common/containers/ExtendedGeneratorContainer.java"));

        assertTrue(source.contains("JDTEMenus.EXTENDED_GENERATOR.get()"));
        assertTrue(source.contains("JDTEBlocks.EXTENDED_GENERATOR.get()"));
        assertTrue(source.contains("return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_GENERATOR.get())"));
    }

    @Test
    void extendedGeneratorScreenUsesResolvedFuelMultiplierLogicForLargeFuelCanisters() {
        ItemStack largeFuelCanister = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());
        FuelCanister.setBurnSpeed(largeFuelCanister, 2.0D);

        assertEquals(PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(largeFuelCanister),
                ExtendedGeneratorScreen.burnSpeedMultiplierTooltipValue(largeFuelCanister));
    }

    @Test
    void extendedGeneratorScreenSourceUsesPortableFuelBurnSpeedHelper() throws Exception {
        Path projectRoot = findProjectRoot(Path.of(System.getProperty("user.dir", "")).toAbsolutePath());
        assertTrue(projectRoot != null, "Could not locate project root from test runtime path");
        String source = Files.readString(projectRoot.resolve(
                "src/main/java/com/jdte/client/screens/ExtendedGeneratorScreen.java"));

        assertTrue(source.contains("PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier"));
    }

    private static Path findProjectRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.exists(current.resolve("gradle.properties"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
