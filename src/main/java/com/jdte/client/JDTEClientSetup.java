package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.entityrenders.TimeAcceleratorEffectRenderer;
import com.jdte.client.renderers.TimeAcceleratorBER;
import com.jdte.client.screens.AdvancedTimeAcceleratorScreen;
import com.jdte.client.screens.BasicTimeAcceleratorScreen;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEEntities;
import com.jdte.setup.JDTEMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class JDTEClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(JDTEMenus.BASIC_TIME_ACCELERATOR.get(), BasicTimeAcceleratorScreen::new);
        event.register(JDTEMenus.ADVANCED_TIME_ACCELERATOR.get(), AdvancedTimeAcceleratorScreen::new);
        event.register(JDTEMenus.EXTENDED_CLICKER.get(), com.direwolf20.justdirethings.client.screens.ClickerT2Screen::new);
        event.register(JDTEMenus.EXTENDED_BLOCK_BREAKER.get(), com.direwolf20.justdirethings.client.screens.BlockBreakerT2Screen::new);
        event.register(JDTEMenus.EXTENDED_BLOCK_PLACER.get(), com.direwolf20.justdirethings.client.screens.BlockPlacerT2Screen::new);
        event.register(JDTEMenus.EXTENDED_BLOCK_SWAPPER.get(), com.direwolf20.justdirethings.client.screens.BlockSwapperT2Screen::new);
        event.register(JDTEMenus.EXTENDED_DROPPER.get(), com.direwolf20.justdirethings.client.screens.DropperT2Screen::new);
        event.register(JDTEMenus.EXTENDED_SENSOR.get(), com.direwolf20.justdirethings.client.screens.SensorT2Screen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_COLLECTOR.get(), com.direwolf20.justdirethings.client.screens.FluidCollectorT2Screen::new);
        event.register(JDTEMenus.EXTENDED_FLUID_PLACER.get(), com.direwolf20.justdirethings.client.screens.FluidPlacerT2Screen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(JDTEBlockEntities.BASIC_TIME_ACCELERATOR.get(), TimeAcceleratorBER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.ADVANCED_TIME_ACCELERATOR.get(), TimeAcceleratorBER::new);
        event.registerEntityRenderer(JDTEEntities.TIME_ACCELERATOR_EFFECT.get(), TimeAcceleratorEffectRenderer::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_CLICKER.get(), com.direwolf20.justdirethings.client.blockentityrenders.ClickerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BLOCK_BREAKER.get(), com.direwolf20.justdirethings.client.blockentityrenders.BlockBreakerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BLOCK_PLACER.get(), com.direwolf20.justdirethings.client.blockentityrenders.BlockPlacerT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_BLOCK_SWAPPER.get(), com.direwolf20.justdirethings.client.blockentityrenders.BlockSwapperT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_DROPPER.get(), com.direwolf20.justdirethings.client.blockentityrenders.DropperT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_SENSOR.get(), com.direwolf20.justdirethings.client.blockentityrenders.SensorT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_COLLECTOR.get(), com.direwolf20.justdirethings.client.blockentityrenders.FluidCollectorT2BER::new);
        event.registerBlockEntityRenderer(JDTEBlockEntities.EXTENDED_FLUID_PLACER.get(), com.direwolf20.justdirethings.client.blockentityrenders.FluidPlacerT2BER::new);
    }
}
