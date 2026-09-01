package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.AdvancedBioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.AdvancedGelGeneratorBE;
import com.jdte.common.blockentities.AdvancedLifeExtractorBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.BasicTimeAcceleratorBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.ExtendedBioCrusherBE;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.blockentities.ExtendedLifeExtractorBE;
import com.jdte.common.blockentities.ExtendedTimeAcceleratorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class MachineSettingsCodecRegistry {
    private static final MachineSettingsCodec COMMON = new CommonMachineSettingsCodec();
    private static final Map<ResourceLocation, RegisteredCodec> REGISTRY = new LinkedHashMap<>();

    static {
        register(JDTEBlockEntities.BASIC_TIME_ACCELERATOR.get(), BasicTimeAcceleratorBE.class);
        register(JDTEBlockEntities.ADVANCED_TIME_ACCELERATOR.get(), AdvancedTimeAcceleratorBE.class);
        register(JDTEBlockEntities.EXTENDED_TIME_ACCELERATOR.get(), ExtendedTimeAcceleratorBE.class);
        register(JDTEBlockEntities.CRYSTAL_INCUBATOR.get(), CrystalIncubatorBE.class);
        register(JDTEBlockEntities.GREENHOUSE.get(), GreenhouseBE.class);
        register(JDTEBlockEntities.LARGE_GREENHOUSE.get(), LargeGreenhouseBE.class);
        register(JDTEBlockEntities.BIO_FACTORY.get(), BioFactoryBE.class);
        register(JDTEBlockEntities.LIFE_BREEDER.get(), LifeBreederBE.class);
        register(JDTEBlockEntities.ADVANCED_LIFE_EXTRACTOR.get(), AdvancedLifeExtractorBE.class);
        register(JDTEBlockEntities.EXTENDED_LIFE_EXTRACTOR.get(), ExtendedLifeExtractorBE.class);
        register(JDTEBlockEntities.LIFE_SYNTHESIS_VAT.get(), LifeSynthesisVatBE.class);
        register(JDTEBlockEntities.MINERAL_EXTRACTOR.get(), MineralExtractorBE.class);
        register(JDTEBlockEntities.LARGE_MINERAL_EXTRACTOR.get(), LargeMineralExtractorBE.class);
        register(JDTEBlockEntities.ADVANCED_GEL_GENERATOR.get(), AdvancedGelGeneratorBE.class);
        register(JDTEBlockEntities.ADVANCED_BIO_CRUSHER.get(), AdvancedBioCrusherBE.class);
        register(JDTEBlockEntities.EXTENDED_BIO_CRUSHER.get(), ExtendedBioCrusherBE.class);
        register(JDTEBlockEntities.ENTITY_SUPPRESSOR.get(), EntitySuppressorBE.class);
        register(JDTEBlockEntities.RANGE_BLOCKER.get(), RangeBlockerBE.class);
        register(JDTEBlockEntities.TIME_FREEZER.get(), TimeFreezerBE.class);
        register(JDTEBlockEntities.ADVANCED_ENERGY_TRANSMITTER.get(), AdvancedEnergyTransmitterBE.class);
        register(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(), ExtendedExperienceHolderBE.class);
        register(JDTEBlockEntities.ADVANCED_POTION_BREWER.get(), AdvancedPotionBrewerBE.class);

        registerJdtType("ClickerT2BE", "com.direwolf20.justdirethings.common.blockentities.ClickerT2BE");
        registerJdtType("DropperT2BE", "com.direwolf20.justdirethings.common.blockentities.DropperT2BE");
        registerJdtType("SensorT2BE", "com.direwolf20.justdirethings.common.blockentities.SensorT2BE");
        registerJdtType("InventoryHolderBE", "com.direwolf20.justdirethings.common.blockentities.InventoryHolderBE");
        registerJdtType("PlayerAccessorBE", "com.direwolf20.justdirethings.common.blockentities.PlayerAccessorBE");
        registerJdtType("ParadoxMachineBE", "com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE");
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

    private static void register(BlockEntityType<?> type, Class<? extends BaseMachineBE> machineClass) {
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        REGISTRY.put(typeId, new RegisteredCodec(machineClass::isInstance, COMMON));
    }

    private static void registerJdtType(String registrationFieldName, String className) {
        jdtTypeId(registrationFieldName)
                .ifPresent(typeId -> registerIfPresent(typeId, className));
    }

    private static void registerIfPresent(ResourceLocation typeId, String className) {
        Optional<Class<? extends BaseMachineBE>> machineClass = loadMachineClass(className);
        machineClass.ifPresent(type -> REGISTRY.put(typeId, new RegisteredCodec(type::isInstance, COMMON)));
    }

    private static Optional<ResourceLocation> jdtTypeId(String registrationFieldName) {
        try {
            Class<?> registrationClass = Class.forName("com.direwolf20.justdirethings.setup.Registration");
            Field field = registrationClass.getField(registrationFieldName);
            Object holder = field.get(null);
            if (!(holder instanceof Supplier<?> supplier)) {
                return Optional.empty();
            }
            Object value = supplier.get();
            if (!(value instanceof BlockEntityType<?> type)) {
                return Optional.empty();
            }
            return Optional.ofNullable(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type));
        } catch (ReflectiveOperationException | LinkageError e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends BaseMachineBE>> loadMachineClass(String className) {
        try {
            return Optional.of((Class<? extends BaseMachineBE>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
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
