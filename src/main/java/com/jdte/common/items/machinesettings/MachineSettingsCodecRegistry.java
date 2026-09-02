package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.*;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.*;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class MachineSettingsCodecRegistry {
    private static final MachineSettingsCodec COMMON = new CommonMachineSettingsCodec();
    private static final Map<ResourceLocation, RegisteredCodec> REGISTRY = new LinkedHashMap<>();

    static {
        register(JDTEBlockEntities.BASIC_TIME_ACCELERATOR.get(), BasicTimeAcceleratorBE.class);
        register(JDTEBlockEntities.ADVANCED_TIME_ACCELERATOR.get(), AdvancedTimeAcceleratorBE.class,
                JdteMachineSettingsCodecs.advancedTimeAccelerator());
        register(JDTEBlockEntities.EXTENDED_TIME_ACCELERATOR.get(), ExtendedTimeAcceleratorBE.class,
                JdteMachineSettingsCodecs.extendedTimeAccelerator());
        register(JDTEBlockEntities.TIME_FREEZER.get(), TimeFreezerBE.class,
                JdteMachineSettingsCodecs.timeFreezer());
        register(JDTEBlockEntities.EXTENDED_TIME_FREEZER.get(), ExtendedTimeFreezerBE.class,
                JdteMachineSettingsCodecs.timeFreezer());
        register(JDTEBlockEntities.EXTENDED_CLICKER.get(), ExtendedClickerBE.class, JdtMachineSettingsCodecs.clicker());
        register(JDTEBlockEntities.EXTENDED_GENERATOR.get(), ExtendedGeneratorBE.class);
        register(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(), ExtendedExperienceHolderBE.class,
                JdtMachineSettingsCodecs.extendedExperienceHolder());
        register(JDTEBlockEntities.EXTENDED_FLUID_GENERATOR.get(), ExtendedFluidGeneratorBE.class);
        register(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), ExtendedEnergyTransmitterBE.class,
                JdtMachineSettingsCodecs.energyTransmitter());
        register(JDTEBlockEntities.EXTENDED_BLOCK_BREAKER.get(), ExtendedBlockBreakerBE.class,
                JdtMachineSettingsCodecs.blockBreaker());
        register(JDTEBlockEntities.EXTENDED_BLOCK_PLACER.get(), ExtendedBlockPlacerBE.class);
        register(JDTEBlockEntities.EXTENDED_BLOCK_SWAPPER.get(), ExtendedBlockSwapperBE.class,
                JdtMachineSettingsCodecs.blockSwapper());
        register(JDTEBlockEntities.EXTENDED_DROPPER.get(), ExtendedDropperBE.class, JdtMachineSettingsCodecs.dropper());
        register(JDTEBlockEntities.EXTENDED_SENSOR.get(), ExtendedSensorBE.class, JdtMachineSettingsCodecs.sensor());
        register(JDTEBlockEntities.EXTENDED_FLUID_COLLECTOR.get(), ExtendedFluidCollectorBE.class);
        register(JDTEBlockEntities.EXTENDED_FLUID_PLACER.get(), ExtendedFluidPlacerBE.class);
        register(JDTEBlockEntities.ADVANCED_ITEM_COLLECTOR.get(), AdvancedItemCollectorBE.class);
        register(JDTEBlockEntities.ENTITY_SUPPRESSOR.get(), EntitySuppressorBE.class,
                JdteMachineSettingsCodecs.entitySuppressor());
        register(JDTEBlockEntities.RANGE_BLOCKER.get(), RangeBlockerBE.class,
                JdteMachineSettingsCodecs.rangeBlocker());
        register(JDTEBlockEntities.FACTORY_PACKER.get(), FactoryPackerBE.class);

        register(JDTEBlockEntities.BASIC_GLUE_ACTIVATOR.get(), BasicGlueActivatorBE.class);
        register(JDTEBlockEntities.ADVANCED_GLUE_ACTIVATOR.get(), AdvancedGlueActivatorBE.class);
        register(JDTEBlockEntities.EXTENDED_GLUE_ACTIVATOR.get(), ExtendedGlueActivatorBE.class);

        register(JDTEBlockEntities.ADVANCED_GEL_GENERATOR.get(), AdvancedGelGeneratorBE.class,
                JdteMachineSettingsCodecs.gelGenerator());
        register(JDTEBlockEntities.EXTENDED_GEL_GENERATOR.get(), ExtendedGelGeneratorBE.class,
                JdteMachineSettingsCodecs.gelGenerator());

        register(JDTEBlockEntities.BASIC_FLUID_STABILIZER.get(), BasicFluidStabilizerBE.class);
        register(JDTEBlockEntities.ADVANCED_FLUID_STABILIZER.get(), AdvancedFluidStabilizerBE.class);
        register(JDTEBlockEntities.EXTENDED_FLUID_STABILIZER.get(), ExtendedFluidStabilizerBE.class);

        register(JDTEBlockEntities.BASIC_ITEM_SENDER.get(), BasicItemSenderBE.class);
        register(JDTEBlockEntities.ADVANCED_ITEM_SENDER.get(), AdvancedItemSenderBE.class);
        register(JDTEBlockEntities.EXTENDED_ITEM_SENDER.get(), ExtendedItemSenderBE.class);
        register(JDTEBlockEntities.BASIC_FLUID_SENDER.get(), BasicFluidSenderBE.class);
        register(JDTEBlockEntities.ADVANCED_FLUID_SENDER.get(), AdvancedFluidSenderBE.class);
        register(JDTEBlockEntities.EXTENDED_FLUID_SENDER.get(), ExtendedFluidSenderBE.class);
        register(JDTEBlockEntities.BASIC_ITEM_RECEIVER.get(), BasicItemReceiverBE.class);
        register(JDTEBlockEntities.ADVANCED_ITEM_RECEIVER.get(), AdvancedItemReceiverBE.class);
        register(JDTEBlockEntities.EXTENDED_ITEM_RECEIVER.get(), ExtendedItemReceiverBE.class);
        register(JDTEBlockEntities.BASIC_FLUID_RECEIVER.get(), BasicFluidReceiverBE.class);
        register(JDTEBlockEntities.ADVANCED_FLUID_RECEIVER.get(), AdvancedFluidReceiverBE.class);
        register(JDTEBlockEntities.EXTENDED_FLUID_RECEIVER.get(), ExtendedFluidReceiverBE.class);

        register(JDTEBlockEntities.CRYSTAL_INCUBATOR.get(), CrystalIncubatorBE.class,
                JdteMachineSettingsCodecs.crystalIncubator());
        register(JDTEBlockEntities.GREENHOUSE.get(), GreenhouseBE.class, JdteMachineSettingsCodecs.greenhouse());
        register(JDTEBlockEntities.LARGE_GREENHOUSE.get(), LargeGreenhouseBE.class,
                JdteMachineSettingsCodecs.largeGreenhouse());
        register(JDTEBlockEntities.LIFE_SYNTHESIS_VAT.get(), LifeSynthesisVatBE.class,
                JdteMachineSettingsCodecs.lifeSynthesisVat());
        register(JDTEBlockEntities.BIO_FACTORY.get(), BioFactoryBE.class, JdteMachineSettingsCodecs.bioFactory());
        register(JDTEBlockEntities.LIFE_BREEDER.get(), LifeBreederBE.class,
                JdteMachineSettingsCodecs.lifeBreeder());
        register(JDTEBlockEntities.MINERAL_EXTRACTOR.get(), MineralExtractorBE.class,
                JdteMachineSettingsCodecs.mineralExtractor());
        register(JDTEBlockEntities.LARGE_MINERAL_EXTRACTOR.get(), LargeMineralExtractorBE.class,
                JdteMachineSettingsCodecs.mineralExtractor());

        register(JDTEBlockEntities.ADVANCED_LIFE_EXTRACTOR.get(), AdvancedLifeExtractorBE.class,
                JdteMachineSettingsCodecs.lifeExtractor());
        register(JDTEBlockEntities.EXTENDED_LIFE_EXTRACTOR.get(), ExtendedLifeExtractorBE.class,
                JdteMachineSettingsCodecs.lifeExtractor());
        register(JDTEBlockEntities.ADVANCED_INFUSION_MACHINE.get(), AdvancedInfusionMachineBE.class);
        register(JDTEBlockEntities.EXTENDED_INFUSION_MACHINE.get(), ExtendedInfusionMachineBE.class);
        register(JDTEBlockEntities.ADVANCED_POTION_BREWER.get(), AdvancedPotionBrewerBE.class,
                JdteMachineSettingsCodecs.advancedPotionBrewer());
        register(JDTEBlockEntities.ADVANCED_BIO_CRUSHER.get(), AdvancedBioCrusherBE.class,
                JdteMachineSettingsCodecs.bioCrusher());
        register(JDTEBlockEntities.EXTENDED_BIO_CRUSHER.get(), ExtendedBioCrusherBE.class,
                JdteMachineSettingsCodecs.bioCrusher());
        register(JDTEBlockEntities.LOOT_FABRICATOR.get(), LootFabricatorBE.class);
        register(JDTEBlockEntities.ADVANCED_ENERGY_TRANSMITTER.get(), AdvancedEnergyTransmitterBE.class,
                JdteMachineSettingsCodecs.advancedEnergyTransmitter());

        register(Registration.ClickerT1BE.get(), ClickerT1BE.class, JdtMachineSettingsCodecs.clicker());
        register(Registration.ClickerT2BE.get(), ClickerT2BE.class, JdtMachineSettingsCodecs.clicker());
        register(Registration.DropperT1BE.get(), DropperT1BE.class, JdtMachineSettingsCodecs.dropper());
        register(Registration.DropperT2BE.get(), DropperT2BE.class, JdtMachineSettingsCodecs.dropper());
        register(Registration.BlockBreakerT1BE.get(), BlockBreakerT1BE.class, JdtMachineSettingsCodecs.blockBreaker());
        register(Registration.BlockBreakerT2BE.get(), BlockBreakerT2BE.class, JdtMachineSettingsCodecs.blockBreaker());
        register(Registration.BlockPlacerT1BE.get(), BlockPlacerT1BE.class);
        register(Registration.BlockPlacerT2BE.get(), BlockPlacerT2BE.class);
        register(Registration.BlockSwapperT1BE.get(), BlockSwapperT1BE.class, JdtMachineSettingsCodecs.blockSwapper());
        register(Registration.BlockSwapperT2BE.get(), BlockSwapperT2BE.class, JdtMachineSettingsCodecs.blockSwapper());
        register(Registration.SensorT1BE.get(), SensorT1BE.class, JdtMachineSettingsCodecs.sensor());
        register(Registration.SensorT2BE.get(), SensorT2BE.class, JdtMachineSettingsCodecs.sensor());
        register(Registration.ItemCollectorBE.get(), ItemCollectorBE.class, JdtMachineSettingsCodecs.itemCollector());
        register(Registration.ExperienceHolderBE.get(), ExperienceHolderBE.class,
                JdtMachineSettingsCodecs.experienceHolder());
        register(Registration.EnergyTransmitterBE.get(), EnergyTransmitterBE.class,
                JdtMachineSettingsCodecs.energyTransmitter());
        register(Registration.GeneratorT1BE.get(), GeneratorT1BE.class);
        register(Registration.GeneratorFluidT1BE.get(), GeneratorFluidT1BE.class);
        register(Registration.FluidPlacerT1BE.get(), FluidPlacerT1BE.class);
        register(Registration.FluidPlacerT2BE.get(), FluidPlacerT2BE.class);
        register(Registration.FluidCollectorT1BE.get(), FluidCollectorT1BE.class);
        register(Registration.FluidCollectorT2BE.get(), FluidCollectorT2BE.class);
        register(Registration.InventoryHolderBE.get(), InventoryHolderBE.class, JdtMachineSettingsCodecs.inventoryHolder());
        register(Registration.PlayerAccessorBE.get(), PlayerAccessorBE.class, JdtMachineSettingsCodecs.playerAccessor());
        register(Registration.ParadoxMachineBE.get(), ParadoxMachineBE.class, JdtMachineSettingsCodecs.paradox());
    }

    private MachineSettingsCodecRegistry() {
    }

    public static MachineSettingsCodec common() {
        return COMMON;
    }

    public static Optional<MachineSettingsCodec> find(ResourceLocation typeId, BaseMachineBE machine) {
        RegisteredCodec registered = REGISTRY.get(typeId);
        if (registered == null || !registered.machineType().test(machine)) {
            return Optional.empty();
        }
        return Optional.of(registered.codec());
    }

    static Set<ResourceLocation> registeredTypeIds() {
        return Set.copyOf(REGISTRY.keySet());
    }

    private static void register(BlockEntityType<?> type, Class<? extends BaseMachineBE> machineClass) {
        register(type, machineClass, COMMON);
    }

    private static void register(BlockEntityType<?> type, Class<? extends BaseMachineBE> machineClass,
                                 MachineSettingsCodec codec) {
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        REGISTRY.put(typeId, new RegisteredCodec(machineClass::isInstance, codec));
    }

    private record RegisteredCodec(Predicate<BaseMachineBE> machineType, MachineSettingsCodec codec) {
    }

    private static final class CommonMachineSettingsCodec implements MachineSettingsCodec {
        @Override
        public CompoundTag encode(BaseMachineBE machine, HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(MachineSettingsCodecSupport.TICK_SPEED_KEY, machine.getTickSpeed());
            tag.putInt(MachineSettingsCodecSupport.DIRECTION_KEY, machine.getDirection());
            return tag;
        }

        @Override
        public Optional<PreparedSettings> decode(CompoundTag custom, HolderLookup.Provider registries) {
            if (!custom.contains(MachineSettingsCodecSupport.TICK_SPEED_KEY, net.minecraft.nbt.Tag.TAG_INT)
                    || !custom.contains(MachineSettingsCodecSupport.DIRECTION_KEY, net.minecraft.nbt.Tag.TAG_INT)) {
                return Optional.empty();
            }

            int tickSpeed = custom.getInt(MachineSettingsCodecSupport.TICK_SPEED_KEY);
            int direction = custom.getInt(MachineSettingsCodecSupport.DIRECTION_KEY);
            if (tickSpeed <= 0 || !MachineSettingsCodecSupport.isValidDirection(direction)) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.setTickSpeed(tickSpeed);
                machine.setDirection(direction);
            });
        }
    }
}
