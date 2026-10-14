package com.jdte.common.blockentities;

import com.jdte.common.entities.MachineLightningEffects;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChargedLightningRodTest {
    private ChargedLightningRodBE machine() {
        return new ChargedLightningRodBE(BlockPos.ZERO, JDTEBlocks.CHARGED_LIGHTNING_ROD.get().defaultBlockState());
    }

    @Test
    void thunderstormCardBypassesWeatherAndIsRestrictedToOneCopyOnThisMachine() {
        var rod = machine();
        var handler = UpgradeHelper.getUpgradeHandler(rod);
        var upgrade = new ItemStack(JDTEItems.THUNDERSTORM_UPGRADE.get());
        assertFalse(rod.canOperateInWeather());
        assertTrue(handler.insertItem(0, upgrade, false).isEmpty());
        assertTrue(rod.canOperateInWeather());
        assertFalse(handler.insertItem(1, upgrade, false).isEmpty());
        var freezer = new TimeFreezerBE(BlockPos.ZERO, JDTEBlocks.TIME_FREEZER.get().defaultBlockState());
        assertFalse(UpgradeHelper.getUpgradeHandler(freezer).isItemValid(0, upgrade));
        handler.extractItem(0, 1, false);
        assertFalse(rod.canOperateInWeather());
    }

    @Test
    void speedUpgradesUseMachineRatesAndPreserveTheirMutualExclusion() {
        var rod = machine();
        var handler = UpgradeHelper.getUpgradeHandler(rod);
        assertEquals(ChargedLightningRodMode.NORMAL, rod.getStrikeMode());
        handler.setStackInSlot(0, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()));
        assertEquals(ChargedLightningRodMode.OVERCLOCK, rod.getStrikeMode());
        assertEquals(200_000, rod.getStandardEnergyCost() * rod.getStrikeMode().strikesPerCycle);
        assertFalse(handler.isItemValid(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())));
        handler.setStackInSlot(0, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()));
        assertEquals(ChargedLightningRodMode.ULTIMATE, rod.getStrikeMode());
        assertEquals(5_000_000, rod.getStandardEnergyCost() * rod.getStrikeMode().strikesPerCycle);
        assertTrue(rod.getMaxEnergy() >= 5_000_000);
        assertFalse(UpgradeHelper.usesLockedDelay(rod));
    }

    @Test
    void presentationControlsAreIndependentAndPersistThroughMachineNbt() {
        var rod = machine();
        rod.setLightningEffects(false, true);
        var restored = machine();
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        restored.loadAdditional(rod.saveWithoutMetadata(registries), registries);
        assertFalse(restored.isLightningRenderEnabled());
        assertTrue(restored.isLightningSoundEnabled());
        restored.setLightningEffects(true, false);
        assertTrue(restored.isLightningRenderEnabled());
        assertFalse(restored.isLightningSoundEnabled());
    }

    @Test
    void settingsCopierCopiesEffectsWithoutCopyingStoredEnergy() {
        var source = machine();
        source.setLightningEffects(false, false);
        source.getEnergyStorage().setEnergy(900_000);
        var target = machine();
        target.getEnergyStorage().setEnergy(100_000);
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        var type = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
        var codec = com.jdte.common.items.machinesettings.MachineSettingsCodecRegistry.find(type, source).orElseThrow();
        codec.decode(codec.encode(source, registries), registries).orElseThrow().apply(target);
        assertFalse(target.isLightningRenderEnabled());
        assertFalse(target.isLightningSoundEnabled());
        assertEquals(100_000, target.getEnergyStorage().getEnergyStored());
    }

    @Test
    void lightningMixinSynchronizesHiddenModelsAndSoundWithoutDiscardingTheEntity() {
        var lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, null);
        var effects = assertInstanceOf(MachineLightningEffects.class, lightning);
        assertTrue(effects.jdte$isLightningRenderEnabled());
        effects.jdte$setLightningRenderEnabled(false);
        assertFalse(lightning.shouldRenderAtSqrDistance(1));
        assertFalse(lightning.isSilent());
        lightning.setSilent(true);
        effects.jdte$setLightningRenderEnabled(true);
        assertTrue(lightning.shouldRenderAtSqrDistance(1));
        assertTrue(lightning.isSilent());
        assertFalse(lightning.isRemoved());
        assertNotNull(lightning.getEntityData().packDirty());
    }
}
