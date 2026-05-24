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
}
