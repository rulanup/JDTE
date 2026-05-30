package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.containers.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTEMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, JDTE.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BasicTimeAcceleratorContainer>> BASIC_TIME_ACCELERATOR = MENUS.register(
            "basic_time_accelerator", () -> IMenuTypeExtension.create(BasicTimeAcceleratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedTimeAcceleratorContainer>> ADVANCED_TIME_ACCELERATOR = MENUS.register(
            "advanced_time_accelerator", () -> IMenuTypeExtension.create(AdvancedTimeAcceleratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedTimeAcceleratorContainer>> EXTENDED_TIME_ACCELERATOR = MENUS.register(
            "extended_time_accelerator", () -> IMenuTypeExtension.create(ExtendedTimeAcceleratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedClickerContainer>> EXTENDED_CLICKER = MENUS.register(
            "extended_clicker", () -> IMenuTypeExtension.create(ExtendedClickerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBlockBreakerContainer>> EXTENDED_BLOCK_BREAKER = MENUS.register(
            "extended_block_breaker", () -> IMenuTypeExtension.create(ExtendedBlockBreakerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBlockPlacerContainer>> EXTENDED_BLOCK_PLACER = MENUS.register(
            "extended_block_placer", () -> IMenuTypeExtension.create(ExtendedBlockPlacerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBlockSwapperContainer>> EXTENDED_BLOCK_SWAPPER = MENUS.register(
            "extended_block_swapper", () -> IMenuTypeExtension.create(ExtendedBlockSwapperContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedDropperContainer>> EXTENDED_DROPPER = MENUS.register(
            "extended_dropper", () -> IMenuTypeExtension.create(ExtendedDropperContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedSensorContainer>> EXTENDED_SENSOR = MENUS.register(
            "extended_sensor", () -> IMenuTypeExtension.create(ExtendedSensorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidCollectorContainer>> EXTENDED_FLUID_COLLECTOR = MENUS.register(
            "extended_fluid_collector", () -> IMenuTypeExtension.create(ExtendedFluidCollectorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidPlacerContainer>> EXTENDED_FLUID_PLACER = MENUS.register(
            "extended_fluid_placer", () -> IMenuTypeExtension.create(ExtendedFluidPlacerContainer::new));

    // Glue Activator
    public static final DeferredHolder<MenuType<?>, MenuType<BasicGlueActivatorContainer>> BASIC_GLUE_ACTIVATOR = MENUS.register(
            "basic_glue_activator", () -> IMenuTypeExtension.create(BasicGlueActivatorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedGlueActivatorContainer>> ADVANCED_GLUE_ACTIVATOR = MENUS.register(
            "advanced_glue_activator", () -> IMenuTypeExtension.create(AdvancedGlueActivatorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedGlueActivatorContainer>> EXTENDED_GLUE_ACTIVATOR = MENUS.register(
            "extended_glue_activator", () -> IMenuTypeExtension.create(ExtendedGlueActivatorContainer::new));

    // Gel Generator
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedGelGeneratorContainer>> ADVANCED_GEL_GENERATOR = MENUS.register(
            "advanced_gel_generator", () -> IMenuTypeExtension.create(AdvancedGelGeneratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedGelGeneratorContainer>> EXTENDED_GEL_GENERATOR = MENUS.register(
            "extended_gel_generator", () -> IMenuTypeExtension.create(ExtendedGelGeneratorContainer::new));

    // Fluid Stabilizer
    public static final DeferredHolder<MenuType<?>, MenuType<FluidStabilizerContainer>> FLUID_STABILIZER = MENUS.register(
            "fluid_stabilizer", () -> IMenuTypeExtension.create(FluidStabilizerContainer::new));

    // Item Sender
    public static final DeferredHolder<MenuType<?>, MenuType<BasicItemSenderContainer>> BASIC_ITEM_SENDER = MENUS.register(
            "basic_item_sender", () -> IMenuTypeExtension.create(BasicItemSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedItemSenderContainer>> ADVANCED_ITEM_SENDER = MENUS.register(
            "advanced_item_sender", () -> IMenuTypeExtension.create(AdvancedItemSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedItemSenderContainer>> EXTENDED_ITEM_SENDER = MENUS.register(
            "extended_item_sender", () -> IMenuTypeExtension.create(ExtendedItemSenderContainer::new));

    // Fluid Sender
    public static final DeferredHolder<MenuType<?>, MenuType<BasicFluidSenderContainer>> BASIC_FLUID_SENDER = MENUS.register(
            "basic_fluid_sender", () -> IMenuTypeExtension.create(BasicFluidSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedFluidSenderContainer>> ADVANCED_FLUID_SENDER = MENUS.register(
            "advanced_fluid_sender", () -> IMenuTypeExtension.create(AdvancedFluidSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidSenderContainer>> EXTENDED_FLUID_SENDER = MENUS.register(
            "extended_fluid_sender", () -> IMenuTypeExtension.create(ExtendedFluidSenderContainer::new));

    // Item Receiver
    public static final DeferredHolder<MenuType<?>, MenuType<BasicItemReceiverContainer>> BASIC_ITEM_RECEIVER = MENUS.register(
            "basic_item_receiver", () -> IMenuTypeExtension.create(BasicItemReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedItemReceiverContainer>> ADVANCED_ITEM_RECEIVER = MENUS.register(
            "advanced_item_receiver", () -> IMenuTypeExtension.create(AdvancedItemReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedItemReceiverContainer>> EXTENDED_ITEM_RECEIVER = MENUS.register(
            "extended_item_receiver", () -> IMenuTypeExtension.create(ExtendedItemReceiverContainer::new));

    // Fluid Receiver
    public static final DeferredHolder<MenuType<?>, MenuType<BasicFluidReceiverContainer>> BASIC_FLUID_RECEIVER = MENUS.register(
            "basic_fluid_receiver", () -> IMenuTypeExtension.create(BasicFluidReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedFluidReceiverContainer>> ADVANCED_FLUID_RECEIVER = MENUS.register(
            "advanced_fluid_receiver", () -> IMenuTypeExtension.create(AdvancedFluidReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidReceiverContainer>> EXTENDED_FLUID_RECEIVER = MENUS.register(
            "extended_fluid_receiver", () -> IMenuTypeExtension.create(ExtendedFluidReceiverContainer::new));
}
