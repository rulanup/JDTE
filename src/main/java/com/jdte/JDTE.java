package com.jdte;

import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTECreativeTabs;
import com.jdte.setup.JDTEEntities;
import com.jdte.setup.JDTEFluids;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTEMenus;
import com.jdte.setup.JDTERecipes;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(JDTE.MODID)
public class JDTE {
    public static final String MODID = "jdte";

    public JDTE(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, JDTEConfig.COMMON_SPEC, JDTE.MODID + "/jdte.toml");
        JDTEBlocks.BLOCKS.register(modEventBus);
        JDTEItems.ITEMS.register(modEventBus);
        JDTEBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        JDTEMenus.MENUS.register(modEventBus);
        JDTEAttachments.ATTACHMENT_TYPES.register(modEventBus);
        JDTECreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        JDTEEntities.ENTITIES.register(modEventBus);
        JDTEFluids.FLUID_TYPES.register(modEventBus);
        JDTEFluids.FLUIDS.register(modEventBus);
        JDTEFluids.FLUID_BLOCKS.register(modEventBus);
        JDTEFluids.BUCKET_ITEMS.register(modEventBus);
        JDTERecipes.RECIPE_TYPES.register(modEventBus);
        JDTERecipes.RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(JDTEPacketHandler::registerNetworking);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Clicker fluid handler (with fluid storage upgrade)
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

        // Time Accelerator fluid handler
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.TimeAcceleratorBE accelerator ? accelerator.getFluidTank() : null,
                JDTEBlocks.BASIC_TIME_ACCELERATOR.get(),
                JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(),
                JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get()
        );

        // Time Accelerator energy storage
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.AdvancedTimeAcceleratorBE accelerator ? accelerator.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(),
                JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get()
        );

        // Extended Machines energy storage
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

        // Glue Activator energy storage (Advanced and Extended)
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get(),
                JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get()
        );

        // Gel Generator energy storage and fluid handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_GEL_GENERATOR.get(),
                JDTEBlocks.EXTENDED_GEL_GENERATOR.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.GelGeneratorBE generator ? generator.getFluidHandler() : null,
                JDTEBlocks.ADVANCED_GEL_GENERATOR.get(),
                JDTEBlocks.EXTENDED_GEL_GENERATOR.get()
        );

        // Fluid Stabilizer energy storage
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(),
                JDTEBlocks.EXTENDED_FLUID_STABILIZER.get()
        );

        // Item Sender energy storage (Advanced and Extended)
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_ITEM_SENDER.get(),
                JDTEBlocks.EXTENDED_ITEM_SENDER.get()
        );

        // Fluid Sender energy storage and fluid handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_FLUID_SENDER.get(),
                JDTEBlocks.EXTENDED_FLUID_SENDER.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.FluidSenderBE sender ? sender.getFluidTank() : null,
                JDTEBlocks.BASIC_FLUID_SENDER.get(),
                JDTEBlocks.ADVANCED_FLUID_SENDER.get(),
                JDTEBlocks.EXTENDED_FLUID_SENDER.get()
        );

        // Item Receiver energy storage (Advanced and Extended)
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_ITEM_RECEIVER.get(),
                JDTEBlocks.EXTENDED_ITEM_RECEIVER.get()
        );

        // Fluid Receiver energy storage and fluid handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_FLUID_RECEIVER.get(),
                JDTEBlocks.EXTENDED_FLUID_RECEIVER.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.FluidReceiverBE receiver ? receiver.getFluidTank() : null,
                JDTEBlocks.BASIC_FLUID_RECEIVER.get(),
                JDTEBlocks.ADVANCED_FLUID_RECEIVER.get(),
                JDTEBlocks.EXTENDED_FLUID_RECEIVER.get()
        );

        // Bio Crusher energy storage, fluid handler, and item handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_BIO_CRUSHER.get(),
                JDTEBlocks.EXTENDED_BIO_CRUSHER.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.BioCrusherBE crusher ? crusher.getFluidTank() : null,
                JDTEBlocks.ADVANCED_BIO_CRUSHER.get(),
                JDTEBlocks.EXTENDED_BIO_CRUSHER.get()
        );
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.BioCrusherBE crusher ? crusher.getMachineHandler() : null,
                JDTEBlocks.ADVANCED_BIO_CRUSHER.get(),
                JDTEBlocks.EXTENDED_BIO_CRUSHER.get()
        );

        // Life Extractor energy storage and fluid handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(),
                JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.LifeExtractorBE extractor ? extractor.getFluidTank() : null,
                JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(),
                JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get()
        );

        // Infusion Machine energy storage and fluid handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(),
                JDTEBlocks.EXTENDED_INFUSION_MACHINE.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.InfusionMachineBE infusion ? infusion.getFluidTank() : null,
                JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(),
                JDTEBlocks.EXTENDED_INFUSION_MACHINE.get()
        );

        // Item handler for Extended JDT machines (machine slots for tools/items)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.EXTENDED_CLICKER.get(),
                JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(),
                JDTEBlocks.EXTENDED_BLOCK_PLACER.get(),
                JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(),
                JDTEBlocks.EXTENDED_DROPPER.get(),
                JDTEBlocks.EXTENDED_SENSOR.get(),
                JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(),
                JDTEBlocks.EXTENDED_FLUID_PLACER.get()
        );

        // Item handler for Glue Activator (all tiers)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.BASIC_GLUE_ACTIVATOR.get(),
                JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get(),
                JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get()
        );

        // Item handler for Gel Generator (input/output slots)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.ADVANCED_GEL_GENERATOR.get(),
                JDTEBlocks.EXTENDED_GEL_GENERATOR.get()
        );

        // Item handler for Fluid Stabilizer (catalyst slot)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.BASIC_FLUID_STABILIZER.get(),
                JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(),
                JDTEBlocks.EXTENDED_FLUID_STABILIZER.get()
        );

        // Item handler for Item Sender (storage slots)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.BASIC_ITEM_SENDER.get(),
                JDTEBlocks.ADVANCED_ITEM_SENDER.get(),
                JDTEBlocks.EXTENDED_ITEM_SENDER.get()
        );

        // Item handler for Item Receiver (storage slots)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.BASIC_ITEM_RECEIVER.get(),
                JDTEBlocks.ADVANCED_ITEM_RECEIVER.get(),
                JDTEBlocks.EXTENDED_ITEM_RECEIVER.get()
        );

        // Item handler for Infusion Machine (input slots)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE baseMachineBE ? baseMachineBE.getMachineHandler() : null,
                JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(),
                JDTEBlocks.EXTENDED_INFUSION_MACHINE.get()
        );
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
