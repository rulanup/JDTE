package com.jdte.common.items.machinesettings;

import com.jdte.common.blockentities.AdvancedBioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.AdvancedGelGeneratorBE;
import com.jdte.common.blockentities.AdvancedLifeExtractorBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.BasicTimeAcceleratorBE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT2BE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.direwolf20.justdirethings.common.blockentities.DropperT2BE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.ExtendedBioCrusherBE;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.blockentities.ExtendedTimeAcceleratorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.direwolf20.justdirethings.common.blockentities.InventoryHolderBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.PlayerAccessorBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.direwolf20.justdirethings.common.blockentities.SensorT2BE;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class MachineSettingsTestFixtures {
    public static final HolderLookup.Provider REGISTRIES =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private MachineSettingsTestFixtures() {
    }

    public static BaseMachineBE baseMachine() {
        return new BaseMachineBE(BlockEntityType.SIGN, BlockPos.ZERO, Blocks.OAK_SIGN.defaultBlockState());
    }

    public static ClickerT2BE clicker() {
        return createJdtMachine("com.direwolf20.justdirethings.common.blockentities.ClickerT2BE",
                "justdirethings:clickert2", "ClickerT2BE");
    }

    public static DropperT2BE dropper() {
        return createJdtMachine("com.direwolf20.justdirethings.common.blockentities.DropperT2BE",
                "justdirethings:droppert2", "DropperT2BE");
    }

    public static SensorT2BE sensor() {
        return createJdtMachine("com.direwolf20.justdirethings.common.blockentities.SensorT2BE",
                "justdirethings:sensort2", "SensorT2BE");
    }

    public static InventoryHolderBE inventoryHolder() {
        return createJdtMachine("com.direwolf20.justdirethings.common.blockentities.InventoryHolderBE",
                "justdirethings:inventory_holder", "InventoryHolderBE");
    }

    public static PlayerAccessorBE playerAccessor() {
        return createJdtMachine("com.direwolf20.justdirethings.common.blockentities.PlayerAccessorBE",
                "justdirethings:playeraccessor", "PlayerAccessorBE");
    }

    public static ParadoxMachineBE paradox() {
        return createJdtMachine("com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE",
                "justdirethings:paradoxmachine", "ParadoxMachineBE");
    }

    public static AdvancedTimeAcceleratorBE advancedTimeAccelerator() {
        return new AdvancedTimeAcceleratorBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get().defaultBlockState());
    }

    public static ExtendedTimeAcceleratorBE extendedTimeAccelerator() {
        return new ExtendedTimeAcceleratorBE(BlockPos.ZERO, JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get().defaultBlockState());
    }

    public static CrystalIncubatorBE crystalIncubator() {
        return new CrystalIncubatorBE(BlockPos.ZERO, JDTEBlocks.CRYSTAL_INCUBATOR.get().defaultBlockState());
    }

    public static GreenhouseBE greenhouse() {
        return new GreenhouseBE(BlockPos.ZERO, JDTEBlocks.GREENHOUSE.get().defaultBlockState());
    }

    public static LargeGreenhouseBE largeGreenhouse() {
        return new LargeGreenhouseBE(BlockPos.ZERO, JDTEBlocks.LARGE_GREENHOUSE.get().defaultBlockState());
    }

    public static BioFactoryBE bioFactory() {
        return new BioFactoryBE(BlockPos.ZERO, JDTEBlocks.BIO_FACTORY.get().defaultBlockState());
    }

    public static LifeBreederBE lifeBreeder() {
        return new LifeBreederBE(BlockPos.ZERO, JDTEBlocks.LIFE_BREEDER.get().defaultBlockState());
    }

    public static AdvancedLifeExtractorBE lifeExtractor() {
        return new AdvancedLifeExtractorBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get().defaultBlockState());
    }

    public static LifeSynthesisVatBE lifeSynthesisVat() {
        return new LifeSynthesisVatBE(BlockPos.ZERO, JDTEBlocks.LIFE_SYNTHESIS_VAT.get().defaultBlockState());
    }

    public static MineralExtractorBE mineralExtractor() {
        return new MineralExtractorBE(BlockPos.ZERO, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
    }

    public static LargeMineralExtractorBE largeMineralExtractor() {
        return new LargeMineralExtractorBE(BlockPos.ZERO, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState());
    }

    public static AdvancedGelGeneratorBE advancedGelGenerator() {
        return new AdvancedGelGeneratorBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_GEL_GENERATOR.get().defaultBlockState());
    }

    public static AdvancedBioCrusherBE bioCrusher() {
        return new AdvancedBioCrusherBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_BIO_CRUSHER.get().defaultBlockState());
    }

    public static EntitySuppressorBE entitySuppressor() {
        return new EntitySuppressorBE(BlockPos.ZERO, JDTEBlocks.ENTITY_SUPPRESSOR.get().defaultBlockState());
    }

    public static RangeBlockerBE rangeBlocker() {
        return new RangeBlockerBE(BlockPos.ZERO, JDTEBlocks.RANGE_BLOCKER.get().defaultBlockState());
    }

    public static TimeFreezerBE timeFreezer() {
        return new TimeFreezerBE(BlockPos.ZERO, JDTEBlocks.TIME_FREEZER.get().defaultBlockState());
    }

    public static AdvancedEnergyTransmitterBE advancedEnergyTransmitter() {
        return new AdvancedEnergyTransmitterBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());
    }

    public static ExtendedExperienceHolderBE extendedExperienceHolder() {
        return new ExtendedExperienceHolderBE(BlockPos.ZERO, JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState());
    }

    public static AdvancedPotionBrewerBE potionBrewer() {
        return new AdvancedPotionBrewerBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_POTION_BREWER.get().defaultBlockState());
    }

    public static void applyCodec(BaseMachineBE source, BaseMachineBE target) {
        ResourceLocation type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
        MachineSettingsCodec codec = MachineSettingsCodecRegistry.find(type, source).orElseThrow();
        CompoundTag encoded = codec.encode(source, REGISTRIES);
        MachineSettingsCodecRegistry.find(type, target).orElseThrow()
                .decode(encoded, REGISTRIES).orElseThrow().apply(target);
    }

    public static Map<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>> blockStateProperties() {
        return Map.of(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
    }

    public static UUID targetOwnerId(AdvancedEnergyTransmitterBE machine) {
        try {
            Field field = AdvancedEnergyTransmitterBE.class.getDeclaredField("boundPlayerId");
            field.setAccessible(true);
            return (UUID) field.get(machine);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to read AdvancedEnergyTransmitterBE owner id", e);
        }
    }

    public static ResourceLocation jdtBlockEntityTypeId(String fieldName) {
        try {
            return ResourceLocation.parse(resolveBlockEntityTypeId(fieldName));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to resolve JDT block entity type id: " + fieldName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseMachineBE> T createJdtMachine(String className, String blockId, String typeFieldName) {
        try {
            Class<?> rawClass = Class.forName(className);
            Class<? extends BaseMachineBE> machineClass = (Class<? extends BaseMachineBE>) rawClass.asSubclass(BaseMachineBE.class);
            ResourceLocation id = ResourceLocation.parse(blockId);
            Block block = BuiltInRegistries.BLOCK.getOptional(id)
                    .orElseThrow(() -> new AssertionError("Missing block registry entry: " + blockId));
            BlockState state = block.defaultBlockState();
            Constructor<? extends BaseMachineBE> ctor =
                    machineClass.getDeclaredConstructor(BlockPos.class, BlockState.class);
            ctor.setAccessible(true);
            return (T) ctor.newInstance(BlockPos.ZERO, state);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalStateException) {
                return instantiateWithoutConstructor(className, blockId, typeFieldName);
            }
            throw new AssertionError("Failed to create machine fixture " + className, e);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to create machine fixture " + className, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseMachineBE> T instantiateWithoutConstructor(String className, String blockId,
                                                                             String typeFieldName) {
        try {
            Class<?> rawClass = Class.forName(className);
            Class<? extends BaseMachineBE> machineClass = (Class<? extends BaseMachineBE>) rawClass.asSubclass(BaseMachineBE.class);
            BaseMachineBE machine = allocate(machineClass);
            ResourceLocation blockIdLocation = ResourceLocation.parse(blockId);
            Block block = BuiltInRegistries.BLOCK.getOptional(blockIdLocation)
                    .orElseThrow(() -> new AssertionError("Missing block registry entry: " + blockId));
            setField(net.minecraft.world.level.block.entity.BlockEntity.class, machine, "type",
                    resolveBlockEntityType(typeFieldName));
            setField(net.minecraft.world.level.block.entity.BlockEntity.class, machine, "worldPosition", BlockPos.ZERO);
            setField(net.minecraft.world.level.block.entity.BlockEntity.class, machine, "blockState", block.defaultBlockState());
            setField(BaseMachineBE.class, machine, "direction", 2);
            setField(BaseMachineBE.class, machine, "tickSpeed", 1);
            return (T) machine;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to create machine fixture " + className, e);
        }
    }

    private static BlockEntityType<?> resolveBlockEntityType(String fieldName) throws ReflectiveOperationException {
        Class<?> registrationClass = Class.forName("com.direwolf20.justdirethings.setup.Registration");
        Field field = registrationClass.getField(fieldName);
        Object holder = field.get(null);
        if (!(holder instanceof Supplier<?> supplier)) {
            throw new AssertionError("Missing block entity type registry entry: " + fieldName);
        }
        Object value = supplier.get();
        if (!(value instanceof BlockEntityType<?> type)) {
            throw new AssertionError("Missing block entity type registry entry: " + fieldName);
        }
        return type;
    }

    private static String resolveBlockEntityTypeId(String fieldName) throws ReflectiveOperationException {
        BlockEntityType<?> type = resolveBlockEntityType(fieldName);
        ResourceLocation key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (key == null) {
            throw new AssertionError("Missing block entity type registry entry: " + fieldName);
        }
        return key.toString();
    }

    private static BaseMachineBE allocate(Class<? extends BaseMachineBE> machineClass) throws ReflectiveOperationException {
        Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
        return (BaseMachineBE) unsafe.allocateInstance(machineClass);
    }

    private static void setField(Class<?> owner, Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setField(Class<?> owner, Object target, String fieldName, int value) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(target, value);
    }
}
