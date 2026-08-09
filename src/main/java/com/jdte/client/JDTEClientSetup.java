package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.entityrenders.TimeAcceleratorEffectRenderer;
import com.jdte.client.renderers.AdvancedItemCollectorBER;
import com.jdte.client.renderers.TimeAcceleratorBER;
import com.jdte.client.screens.*;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEEntities;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTEMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

@Mod.EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JDTEClientSetup {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                JDTEItems.FACTORY_PACKAGE.get(),
                ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "filled"),
                (stack, level, entity, seed) -> FactoryPackageItem.isFilled(stack) ? 1.0F : 0.0F));
        event.enqueueWork(() -> ItemProperties.register(
                JDTEItems.ULTIMATE_PORTAL_GUN.get(), JDTE.id("fullness"),
                (stack, level, entity, seed) -> UltimatePortalGunItem.getFullness(stack)));
        event.enqueueWork(JDTEClientSetup::registerScreens);
    }

    private static void registerScreens() {
        // Time Accelerators
        MenuScreens.register(JDTEMenus.BASIC_TIME_ACCELERATOR.get(), BasicTimeAcceleratorScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_TIME_ACCELERATOR.get(), AdvancedTimeAcceleratorScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_TIME_ACCELERATOR.get(), ExtendedTimeAcceleratorScreen::new);
        MenuScreens.register(JDTEMenus.TIME_FREEZER.get(), TimeFreezerScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_TIME_FREEZER.get(), ExtendedTimeFreezerScreen::new);

        // Extended Machines
        MenuScreens.register(JDTEMenus.EXTENDED_CLICKER.get(), com.direwolf20.justdirethings.client.screens.ClickerT2Screen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_BLOCK_BREAKER.get(), com.direwolf20.justdirethings.client.screens.BlockBreakerT2Screen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_BLOCK_PLACER.get(), com.direwolf20.justdirethings.client.screens.BlockPlacerT2Screen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_BLOCK_SWAPPER.get(), com.direwolf20.justdirethings.client.screens.BlockSwapperT2Screen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_DROPPER.get(), com.direwolf20.justdirethings.client.screens.DropperT2Screen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_SENSOR.get(), ExtendedSensorScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_FLUID_COLLECTOR.get(), com.direwolf20.justdirethings.client.screens.FluidCollectorT2Screen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_FLUID_PLACER.get(), com.direwolf20.justdirethings.client.screens.FluidPlacerT2Screen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_ITEM_COLLECTOR.get(), AdvancedItemCollectorScreen::new);
        MenuScreens.register(JDTEMenus.ENTITY_SUPPRESSOR.get(), EntitySuppressorScreen::new);
        MenuScreens.register(JDTEMenus.RANGE_BLOCKER.get(), RangeBlockerScreen::new);
        MenuScreens.register(JDTEMenus.FACTORY_PACKER.get(), FactoryPackerScreen::new);

        // Glue Activators
        MenuScreens.register(JDTEMenus.BASIC_GLUE_ACTIVATOR.get(), BasicGlueActivatorScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_GLUE_ACTIVATOR.get(), AdvancedGlueActivatorScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_GLUE_ACTIVATOR.get(), ExtendedGlueActivatorScreen::new);

        // Gel Generators
        MenuScreens.register(JDTEMenus.ADVANCED_GEL_GENERATOR.get(), AdvancedGelGeneratorScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_GEL_GENERATOR.get(), ExtendedGelGeneratorScreen::new);

        // Fluid Stabilizer
        MenuScreens.register(JDTEMenus.BASIC_FLUID_STABILIZER.get(), BasicFluidStabilizerScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_FLUID_STABILIZER.get(), AdvancedFluidStabilizerScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_FLUID_STABILIZER.get(), ExtendedFluidStabilizerScreen::new);

        // Item Senders
        MenuScreens.register(JDTEMenus.BASIC_ITEM_SENDER.get(), BasicItemSenderScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_ITEM_SENDER.get(), AdvancedItemSenderScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_ITEM_SENDER.get(), ExtendedItemSenderScreen::new);

        // Fluid Senders
        MenuScreens.register(JDTEMenus.BASIC_FLUID_SENDER.get(), BasicFluidSenderScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_FLUID_SENDER.get(), AdvancedFluidSenderScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_FLUID_SENDER.get(), ExtendedFluidSenderScreen::new);

        // Item Receivers
        MenuScreens.register(JDTEMenus.BASIC_ITEM_RECEIVER.get(), BasicItemReceiverScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_ITEM_RECEIVER.get(), AdvancedItemReceiverScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_ITEM_RECEIVER.get(), DefaultExtendedItemReceiverScreen::new);
        MenuScreens.register(JDTEMenus.CRYSTAL_INCUBATOR.get(), CrystalIncubatorScreen::new);
        MenuScreens.register(JDTEMenus.GREENHOUSE.get(), GreenhouseScreen::new);
        MenuScreens.register(JDTEMenus.LARGE_GREENHOUSE.get(), LargeGreenhouseScreen::new);
        MenuScreens.register(JDTEMenus.BIO_FACTORY.get(), BioFactoryScreen::new);
        MenuScreens.register(JDTEMenus.LIFE_BREEDER.get(), LifeBreederScreen::new);
        MenuScreens.register(JDTEMenus.LIFE_SYNTHESIS_VAT.get(), LifeSynthesisScreen::new);

        // Fluid Receivers
        MenuScreens.register(JDTEMenus.BASIC_FLUID_RECEIVER.get(), BasicFluidReceiverScreen::new);
        MenuScreens.register(JDTEMenus.ADVANCED_FLUID_RECEIVER.get(), AdvancedFluidReceiverScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_FLUID_RECEIVER.get(), ExtendedFluidReceiverScreen::new);

        // Life Extractor
        MenuScreens.register(JDTEMenus.ADVANCED_LIFE_EXTRACTOR.get(), AdvancedLifeExtractorScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_LIFE_EXTRACTOR.get(), ExtendedLifeExtractorScreen::new);

        // Infusion Machine
        MenuScreens.register(JDTEMenus.ADVANCED_INFUSION_MACHINE.get(), AdvancedInfusionMachineScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_INFUSION_MACHINE.get(), ExtendedInfusionMachineScreen::new);

        // Bio Crusher
        MenuScreens.register(JDTEMenus.ADVANCED_BIO_CRUSHER.get(), AdvancedBioCrusherScreen::new);
        MenuScreens.register(JDTEMenus.EXTENDED_BIO_CRUSHER.get(), ExtendedBioCrusherScreen::new);
        MenuScreens.register(JDTEMenus.LOOT_FABRICATOR.get(), LootFabricatorScreen::new);

        // Potion Brewer
        MenuScreens.register(JDTEMenus.ADVANCED_POTION_BREWER.get(), AdvancedPotionBrewerScreen::new);
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
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_ITEM_COLLECTOR.get(), AdvancedItemCollectorBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ENTITY_SUPPRESSOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.RANGE_BLOCKER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.FACTORY_PACKER.get(), com.jdte.client.renderers.AreaAffectingBER::new);

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
        event.registerBlockEntityRenderer(JDTEBlockEntities.CRYSTAL_INCUBATOR.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.GREENHOUSE.get(), com.jdte.client.renderers.GreenhouseBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.LARGE_GREENHOUSE.get(), com.jdte.client.renderers.LargeGreenhouseBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.BIO_FACTORY.get(), com.jdte.client.renderers.BioFactoryBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.LIFE_BREEDER.get(), com.jdte.client.renderers.AreaAffectingBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.LIFE_SYNTHESIS_VAT.get(), com.jdte.client.renderers.LifeSynthesisVatBER::new);

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
