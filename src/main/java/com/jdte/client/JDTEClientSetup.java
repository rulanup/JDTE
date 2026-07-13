package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.entityrenders.TimeAcceleratorEffectRenderer;
import com.jdte.client.renderers.TimeAcceleratorBER;
import com.jdte.client.screens.*;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEEntities;
import com.jdte.setup.JDTEMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class JDTEClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        // Time Accelerators
        event.register(JDTEMenus.BASIC_TIME_ACCELERATOR.get(), BasicTimeAcceleratorScreen::new);
        event.register(JDTEMenus.ADVANCED_TIME_ACCELERATOR.get(), AdvancedTimeAcceleratorScreen::new);
        event.register(JDTEMenus.EXTENDED_TIME_ACCELERATOR.get(), ExtendedTimeAcceleratorScreen::new);

        // Extended Machines
        event.register(JDTEMenus.EXTENDED_CLICKER.get(), com.direwolf20.justdirethings.client.screens.ClickerT2Screen::new);
        event.register(JDTEMenus.EXTENDED_BLOCK_BREAKER.get(), com.direwolf20.justdirethings.client.screens.BlockBreakerT2Screen::new);
        event.register(JDTEMenus.EXTENDED_BLOCK_PLACER.get(), com.direwolf20.justdirethings.client.screens.BlockPlacerT2Screen::new);
        event.register(JDTEMenus.EXTENDED_BLOCK_SWAPPER.get(), com.direwolf20.justdirethings.client.screens.BlockSwapperT2Screen::new);
        event.register(JDTEMenus.EXTENDED_DROPPER.get(), com.direwolf20.justdirethings.client.screens.DropperT2Screen::new);
        event.register(JDTEMenus.EXTENDED_SENSOR.get(), ExtendedSensorScreen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_COLLECTOR.get(), com.direwolf20.justdirethings.client.screens.FluidCollectorT2Screen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_PLACER.get(), com.direwolf20.justdirethings.client.screens.FluidPlacerT2Screen::new);
        event.register(JDTEMenus.ADVANCED_ITEM_COLLECTOR.get(), AdvancedItemCollectorScreen::new);

        // Glue Activators
        event.register(JDTEMenus.BASIC_GLUE_ACTIVATOR.get(), BasicGlueActivatorScreen::new);
        event.register(JDTEMenus.ADVANCED_GLUE_ACTIVATOR.get(), AdvancedGlueActivatorScreen::new);
        event.register(JDTEMenus.EXTENDED_GLUE_ACTIVATOR.get(), ExtendedGlueActivatorScreen::new);

        // Gel Generators
        event.register(JDTEMenus.ADVANCED_GEL_GENERATOR.get(), AdvancedGelGeneratorScreen::new);
        event.register(JDTEMenus.EXTENDED_GEL_GENERATOR.get(), ExtendedGelGeneratorScreen::new);

        // Fluid Stabilizer
        event.register(JDTEMenus.BASIC_FLUID_STABILIZER.get(), BasicFluidStabilizerScreen::new);
        event.register(JDTEMenus.ADVANCED_FLUID_STABILIZER.get(), AdvancedFluidStabilizerScreen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_STABILIZER.get(), ExtendedFluidStabilizerScreen::new);

        // Item Senders
        event.register(JDTEMenus.BASIC_ITEM_SENDER.get(), BasicItemSenderScreen::new);
        event.register(JDTEMenus.ADVANCED_ITEM_SENDER.get(), AdvancedItemSenderScreen::new);
        event.register(JDTEMenus.EXTENDED_ITEM_SENDER.get(), ExtendedItemSenderScreen::new);

        // Fluid Senders
        event.register(JDTEMenus.BASIC_FLUID_SENDER.get(), BasicFluidSenderScreen::new);
        event.register(JDTEMenus.ADVANCED_FLUID_SENDER.get(), AdvancedFluidSenderScreen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_SENDER.get(), ExtendedFluidSenderScreen::new);

        // Item Receivers
        event.register(JDTEMenus.BASIC_ITEM_RECEIVER.get(), BasicItemReceiverScreen::new);
        event.register(JDTEMenus.ADVANCED_ITEM_RECEIVER.get(), AdvancedItemReceiverScreen::new);
        event.register(JDTEMenus.EXTENDED_ITEM_RECEIVER.get(), ExtendedItemReceiverScreen::new);

        // Fluid Receivers
        event.register(JDTEMenus.BASIC_FLUID_RECEIVER.get(), BasicFluidReceiverScreen::new);
        event.register(JDTEMenus.ADVANCED_FLUID_RECEIVER.get(), AdvancedFluidReceiverScreen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_RECEIVER.get(), ExtendedFluidReceiverScreen::new);

        // Life Extractor
        event.register(JDTEMenus.ADVANCED_LIFE_EXTRACTOR.get(), AdvancedLifeExtractorScreen::new);
        event.register(JDTEMenus.EXTENDED_LIFE_EXTRACTOR.get(), ExtendedLifeExtractorScreen::new);

        // Infusion Machine
        event.register(JDTEMenus.ADVANCED_INFUSION_MACHINE.get(), AdvancedInfusionMachineScreen::new);
        event.register(JDTEMenus.EXTENDED_INFUSION_MACHINE.get(), ExtendedInfusionMachineScreen::new);

        // Bio Crusher
        event.register(JDTEMenus.ADVANCED_BIO_CRUSHER.get(), AdvancedBioCrusherScreen::new);
        event.register(JDTEMenus.EXTENDED_BIO_CRUSHER.get(), ExtendedBioCrusherScreen::new);
        event.register(JDTEMenus.LOOT_FABRICATOR.get(), LootFabricatorScreen::new);

        // Potion Brewer
        event.register(JDTEMenus.ADVANCED_POTION_BREWER.get(), AdvancedPotionBrewerScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(JDTEKeyMappings.WRENCH_AREA_MODIFIER);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Time Accelerators
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_TIME_ACCELERATOR.get(), TimeAcceleratorBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_TIME_ACCELERATOR.get(), TimeAcceleratorBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_TIME_ACCELERATOR.get(), TimeAcceleratorBER::new);
        event.registerEntityRenderer(JDTEEntities.TIME_ACCELERATOR_EFFECT.get(), TimeAcceleratorEffectRenderer::new);

        // Extended Machines
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_CLICKER.get(), com.direwolf20.justdirethings.client.blockentityrenders.ClickerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BLOCK_BREAKER.get(), com.direwolf20.justdirethings.client.blockentityrenders.BlockBreakerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BLOCK_PLACER.get(), com.direwolf20.justdirethings.client.blockentityrenders.BlockPlacerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BLOCK_SWAPPER.get(), com.direwolf20.justdirethings.client.blockentityrenders.BlockSwapperT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_DROPPER.get(), com.direwolf20.justdirethings.client.blockentityrenders.DropperT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_SENSOR.get(), com.direwolf20.justdirethings.client.blockentityrenders.SensorT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_COLLECTOR.get(), com.direwolf20.justdirethings.client.blockentityrenders.FluidCollectorT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_PLACER.get(), com.direwolf20.justdirethings.client.blockentityrenders.FluidPlacerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_ITEM_COLLECTOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Glue Activators - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_GLUE_ACTIVATOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_GLUE_ACTIVATOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_GLUE_ACTIVATOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Fluid Stabilizer - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_FLUID_STABILIZER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_FLUID_STABILIZER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_STABILIZER.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Item Senders - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_ITEM_SENDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_ITEM_SENDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_ITEM_SENDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Fluid Senders - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_FLUID_SENDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_FLUID_SENDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_SENDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Item Receivers - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_ITEM_RECEIVER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_ITEM_RECEIVER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_ITEM_RECEIVER.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Fluid Receivers - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_FLUID_RECEIVER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_FLUID_RECEIVER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_RECEIVER.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Life Extractor - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_LIFE_EXTRACTOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_LIFE_EXTRACTOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);

        // Bio Crusher - 使用AreaAffectingBER渲染区域
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_BIO_CRUSHER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BIO_CRUSHER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
    }
}
