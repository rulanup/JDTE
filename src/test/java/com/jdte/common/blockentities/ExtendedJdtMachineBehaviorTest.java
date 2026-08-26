package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.jdte.client.screens.ExtendedFluidGeneratorScreen;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.jdte.common.containers.ExtendedFluidGeneratorContainer;
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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
    void extendedFluidGeneratorContainerMatchesJdtMenuMethodShape() {
        Set<String> declaredMethods = Arrays.stream(ExtendedFluidGeneratorContainer.class.getDeclaredMethods())
                .filter(method -> !method.isSynthetic())
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertEquals(Set.of("addMachineSlots", "addFuelSlotRange", "stillValid", "quickMoveStack", "removed"),
                declaredMethods);
    }

    @Test
    void extendedFluidGeneratorContainerSourceLocksStillValidToExtendedBlock() throws Exception {
        Path projectRoot = findProjectRoot(Path.of(System.getProperty("user.dir", "")).toAbsolutePath());
        assertTrue(projectRoot != null, "Could not locate project root from test runtime path");
        String source = Files.readString(projectRoot.resolve(
                "src/main/java/com/jdte/common/containers/ExtendedFluidGeneratorContainer.java"));

        assertTrue(source.contains("JDTEMenus.EXTENDED_FLUID_GENERATOR.get()"));
        assertTrue(source.contains("JDTEBlocks.EXTENDED_FLUID_GENERATOR.get()"));
        assertTrue(source.contains("return stillValid(ContainerLevelAccess.create(player.level(), pos), player,"));
        assertTrue(source.contains("return super.quickMoveStack(player, index);"));
        assertTrue(source.contains("super.removed(player);"));
        assertTrue(!source.contains("getBurnRemaining("));
        assertTrue(!source.contains("getMaxBurn("));
    }

    @Test
    void extendedFluidGeneratorScreenUsesBaseMachineFluidDisplayPath() throws Exception {
        Set<String> declaredMethods = Arrays.stream(ExtendedFluidGeneratorScreen.class.getDeclaredMethods())
                .filter(method -> !method.isSynthetic())
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertEquals(Set.of("init", "setTopSection", "addTickSpeedButton", "addRedstoneButtons", "powerBarTooltip"),
                declaredMethods);
        assertTrue(BaseMachineContainer.class.isAssignableFrom(ExtendedFluidGeneratorContainer.class));

        Path projectRoot = findProjectRoot(Path.of(System.getProperty("user.dir", "")).toAbsolutePath());
        assertTrue(projectRoot != null, "Could not locate project root from test runtime path");
        String source = Files.readString(projectRoot.resolve(
                "src/main/java/com/jdte/client/screens/ExtendedFluidGeneratorScreen.java"));

        assertTrue(source.contains("extends BaseMachineScreen<ExtendedFluidGeneratorContainer>"));
        assertTrue(source.contains("generatorBE.getFePerFuelTick()"));
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
