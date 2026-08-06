package com.jdte.setup;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.jdte.JDTE;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.player.LifeAppleData;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.mixin.EnergyStorageAccessor;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/** Forge 1.20.1 replacement for the NeoForge attachment-backed JDTE state. */
public final class JDTEAttachments {
    public static final Capability<UpgradeItemStackHandler> UPGRADE_HANDLER =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ExtendedUpgradeItemStackHandler> EXTENDED_UPGRADE_HANDLER =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<JDTEFluidTank> CLICKER_FLUID_TANK =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<AutoIoConfigData> AUTO_IO_CONFIG =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<FilterBasicHandler> FILTER_HANDLER =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<MachineEnergyStorage> MACHINE_ENERGY =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<LifeAppleData> LIFE_APPLE_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    private JDTEAttachments() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(UpgradeItemStackHandler.class);
        event.register(ExtendedUpgradeItemStackHandler.class);
        event.register(JDTEFluidTank.class);
        event.register(AutoIoConfigData.class);
        event.register(FilterBasicHandler.class);
        event.register(MachineEnergyStorage.class);
        event.register(LifeAppleData.class);
    }

    public static void attachBlockCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof BaseMachineBE machine) {
            MachineDataProvider provider = new MachineDataProvider(machine);
            event.addCapability(JDTE.id("machine_data"), provider);
            event.addListener(provider::invalidate);
        }
    }

    public static void attachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        PlayerDataProvider provider = new PlayerDataProvider();
        event.addCapability(JDTE.id("life_apple_data"), provider);
        event.addListener(provider::invalidate);
    }

    public static UpgradeItemStackHandler upgrades(BaseMachineBE machine) {
        return machine.getCapability(UPGRADE_HANDLER).orElseThrow(
                () -> new IllegalStateException("Missing JDTE upgrade capability for " + machine.getBlockPos()));
    }

    public static ExtendedUpgradeItemStackHandler extendedUpgrades(BaseMachineBE machine) {
        return machine.getCapability(EXTENDED_UPGRADE_HANDLER).orElseThrow(
                () -> new IllegalStateException("Missing JDTE extended upgrade capability for " + machine.getBlockPos()));
    }

    public static JDTEFluidTank clickerFluidTank(BaseMachineBE machine) {
        return machine.getCapability(CLICKER_FLUID_TANK).orElseThrow(
                () -> new IllegalStateException("Missing JDTE Clicker fluid capability for " + machine.getBlockPos()));
    }

    public static AutoIoConfigData autoIo(BaseMachineBE machine) {
        return machine.getCapability(AUTO_IO_CONFIG).orElseThrow(
                () -> new IllegalStateException("Missing JDTE auto I/O capability for " + machine.getBlockPos()));
    }

    public static FilterBasicHandler filter(BaseMachineBE machine) {
        return machine.getCapability(FILTER_HANDLER).orElseThrow(
                () -> new IllegalStateException("Missing JDTE filter capability for " + machine.getBlockPos()));
    }

    public static MachineEnergyStorage energy(BaseMachineBE machine) {
        return machine.getCapability(MACHINE_ENERGY).orElseThrow(
                () -> new IllegalStateException("Missing JDTE energy capability for " + machine.getBlockPos()));
    }

    public static LifeAppleData lifeApple(Player player) {
        return player.getCapability(LIFE_APPLE_DATA).orElseThrow(
                () -> new IllegalStateException("Missing JDTE Life Apple capability for " + player.getGameProfile().getName()));
    }

    private static final class MachineDataProvider implements ICapabilityProvider, net.minecraftforge.common.util.INBTSerializable<CompoundTag> {
        private static final int INITIAL_MACHINE_ENERGY_CAPACITY = 100000;

        private final UpgradeItemStackHandler upgrades;
        private final ExtendedUpgradeItemStackHandler extendedUpgrades;
        private final JDTEFluidTank clickerFluidTank;
        private final AutoIoConfigData autoIo = new AutoIoConfigData();
        private final FilterBasicHandler filterHandler = new FilterBasicHandler(9);
        private final MachineEnergyStorage machineEnergy;
        private final BaseMachineBE machine;
        private final LazyOptional<UpgradeItemStackHandler> upgradesOptional;
        private final LazyOptional<ExtendedUpgradeItemStackHandler> extendedUpgradesOptional;
        private final LazyOptional<JDTEFluidTank> clickerFluidOptional;
        private final LazyOptional<AutoIoConfigData> autoIoOptional;
        private final LazyOptional<FilterBasicHandler> filterOptional;
        private final LazyOptional<MachineEnergyStorage> machineEnergyOptional;
        private final LazyOptional<IEnergyStorage> energyOptional;
        private final LazyOptional<IFluidHandler> fluidOptional;
        private final LazyOptional<IItemHandler> itemOptional;
        private boolean machineEnergyInitialized;

        private MachineDataProvider(BaseMachineBE machine) {
            this.machine = machine;
            upgrades = new UpgradeItemStackHandler(machine);
            extendedUpgrades = new ExtendedUpgradeItemStackHandler(machine);
            clickerFluidTank = new JDTEFluidTank(UpgradeItemStackHandler.BASE_CLICKER_FLUID_CAPACITY);
            // Capabilities are attached from BlockEntity's superclass constructor. Calling a
            // subclass getMaxEnergy() here can ask for upgrades before this provider exists.
            machineEnergy = new MachineEnergyStorage(INITIAL_MACHINE_ENERGY_CAPACITY);
            upgradesOptional = LazyOptional.of(() -> upgrades);
            extendedUpgradesOptional = LazyOptional.of(() -> extendedUpgrades);
            clickerFluidOptional = LazyOptional.of(() -> clickerFluidTank);
            autoIoOptional = LazyOptional.of(() -> autoIo);
            filterOptional = LazyOptional.of(() -> filterHandler);
            machineEnergyOptional = LazyOptional.of(() -> machineEnergy);
            energyOptional = machine instanceof PoweredMachineBE powered
                    ? LazyOptional.of(powered::getEnergyStorage) : LazyOptional.empty();
            fluidOptional = machine instanceof FluidMachineBE fluidMachine
                    ? LazyOptional.of(fluidMachine::getFluidTank) : LazyOptional.empty();
            itemOptional = LazyOptional.of(machine::getMachineHandler);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            if (capability == UPGRADE_HANDLER) return upgradesOptional.cast();
            if (capability == EXTENDED_UPGRADE_HANDLER) return extendedUpgradesOptional.cast();
            if (capability == CLICKER_FLUID_TANK) return clickerFluidOptional.cast();
            if (capability == AUTO_IO_CONFIG) return autoIoOptional.cast();
            if (capability == FILTER_HANDLER) return filterOptional.cast();
            if (capability == MACHINE_ENERGY) {
                initializeMachineEnergy();
                return machineEnergyOptional.cast();
            }
            if (capability == ForgeCapabilities.ENERGY) return energyOptional.cast();
            if (capability == ForgeCapabilities.FLUID_HANDLER) return fluidOptional.cast();
            if (capability == ForgeCapabilities.ITEM_HANDLER) return itemOptional.cast();
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            initializeMachineEnergy();
            CompoundTag tag = new CompoundTag();
            tag.put("upgrade_handler", upgrades.serializeNBT());
            tag.put("extended_upgrade_handler", extendedUpgrades.serializeNBT());
            tag.put("clicker_fluid_tank", clickerFluidTank.serializeNBT());
            tag.put("auto_io_config", autoIo.serializeNBT());
            tag.put("filter_handler", filterHandler.serializeNBT());
            tag.putInt("machine_energy", machineEnergy.getEnergyStored());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("upgrade_handler")) upgrades.deserializeNBT(tag.getCompound("upgrade_handler"));
            if (tag.contains("extended_upgrade_handler")) extendedUpgrades.deserializeNBT(tag.getCompound("extended_upgrade_handler"));
            if (tag.contains("clicker_fluid_tank")) clickerFluidTank.deserializeNBT(tag.getCompound("clicker_fluid_tank"));
            if (tag.contains("auto_io_config")) autoIo.deserializeNBT(tag.getCompound("auto_io_config"));
            if (tag.contains("filter_handler")) filterHandler.deserializeNBT(tag.getCompound("filter_handler"));
            initializeMachineEnergy();
            if (tag.contains("machine_energy")) machineEnergy.setEnergy(tag.getInt("machine_energy"));
        }

        private void initializeMachineEnergy() {
            if (machineEnergyInitialized || !(machine instanceof PoweredMachineBE powered)) {
                return;
            }

            int capacity = Math.max(0, powered.getMaxEnergy());
            if (machineEnergy instanceof EnergyStorageAccessor accessor) {
                accessor.jdte$setCapacity(capacity);
                accessor.jdte$setMaxReceive(capacity);
                accessor.jdte$setMaxExtract(capacity);
            }
            machineEnergyInitialized = true;
        }

        private void invalidate() {
            upgradesOptional.invalidate();
            extendedUpgradesOptional.invalidate();
            clickerFluidOptional.invalidate();
            autoIoOptional.invalidate();
            filterOptional.invalidate();
            machineEnergyOptional.invalidate();
            energyOptional.invalidate();
            fluidOptional.invalidate();
            itemOptional.invalidate();
        }
    }

    private static final class PlayerDataProvider implements ICapabilityProvider, net.minecraftforge.common.util.INBTSerializable<CompoundTag> {
        private final LifeAppleData data = new LifeAppleData();
        private final LazyOptional<LifeAppleData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == LIFE_APPLE_DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.deserializeNBT(tag);
        }

        private void invalidate() {
            optional.invalidate();
        }
    }
}
