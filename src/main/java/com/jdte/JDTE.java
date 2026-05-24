package com.jdte;

import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTECreativeTabs;
import com.jdte.setup.JDTEEntities;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTEMenus;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(JDTE.MODID)
public class JDTE {
    public static final String MODID = "jdte";

    public JDTE(IEventBus modEventBus, ModContainer modContainer) {
        JDTEBlocks.BLOCKS.register(modEventBus);
        JDTEItems.ITEMS.register(modEventBus);
        JDTEBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        JDTEMenus.MENUS.register(modEventBus);
        JDTEAttachments.ATTACHMENT_TYPES.register(modEventBus);
        JDTECreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        JDTEEntities.ENTITIES.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(JDTEPacketHandler::registerNetworking);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> {
                    if (be instanceof ClickerT1BE clicker && UpgradeHelper.hasFluidStorageUpgrade(clicker)) {
                        return UpgradeHelper.getClickerFluidTank(clicker);
                    }
                    return null;
                },
                Registration.ClickerT1.get(),
                Registration.ClickerT2.get(),
                JDTEBlocks.EXTENDED_CLICKER.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.TimeAcceleratorBE accelerator ? accelerator.getFluidTank() : null,
                JDTEBlocks.BASIC_TIME_ACCELERATOR.get(),
                JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get()
        );
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.AdvancedTimeAcceleratorBE accelerator ? accelerator.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get()
        );
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.EXTENDED_CLICKER.get(),
                JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(),
                JDTEBlocks.EXTENDED_BLOCK_PLACER.get(),
                JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(),
                JDTEBlocks.EXTENDED_DROPPER.get(),
                JDTEBlocks.EXTENDED_SENSOR.get(),
                JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(),
                JDTEBlocks.EXTENDED_FLUID_PLACER.get()
        );
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
