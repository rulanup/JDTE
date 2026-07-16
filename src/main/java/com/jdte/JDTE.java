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
import com.jdte.common.commands.JDTECommands;
import com.jdte.common.blockentities.AdvancedItemCollectorManager;
import com.jdte.common.blockentities.EntitySuppressorManager;
import com.jdte.common.blockentities.ExtendedTimeAccelerationManager;
import com.jdte.common.blockentities.RangeBlockerManager;
import com.jdte.common.integrations.JDTEUltimineIntegration;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.BioCrusherDropCapture;
import com.jdte.common.utils.MobLootSpawnEggHelper;
import com.jdte.common.player.LifeAppleProgression;
import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(JDTE.MODID)
public class JDTE {
    public static final String MODID = "jdte";

    public JDTE(IEventBus modEventBus, ModContainer modContainer) {
        net.neoforged.neoforge.common.NeoForgeMod.enableMilkFluid();
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
        NeoForge.EVENT_BUS.addListener(JDTECommands::register);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BioCrusherDropCapture::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BioCrusherDropCapture::onLivingExperienceDrop);
        NeoForge.EVENT_BUS.addListener(this::syncSpawnEggRecipes);
        NeoForge.EVENT_BUS.addListener(LifeAppleProgression::onClone);
        NeoForge.EVENT_BUS.addListener(LifeAppleProgression::onLogin);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AdvancedItemCollectorManager::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AdvancedItemCollectorManager::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(AdvancedItemCollectorManager::onServerTick);
        NeoForge.EVENT_BUS.addListener(AdvancedItemCollectorManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerTickPre);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerStopped);
        NeoForge.EVENT_BUS.addListener(EntitySuppressorManager::onEntityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntitySuppressorManager::onItemPickup);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntitySuppressorManager::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntitySuppressorManager::onMobSpawnPosition);
        NeoForge.EVENT_BUS.addListener(EntitySuppressorManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(RangeBlockerManager::onEntityTickPre);
        NeoForge.EVENT_BUS.addListener(RangeBlockerManager::onEntityTickPost);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(RangeBlockerManager::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onTeleport);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onExplosionDetonate);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onPlaySoundAtPosition);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onPlaySoundAtEntity);
        NeoForge.EVENT_BUS.addListener(RangeBlockerManager::onLevelUnload);
        if (ModList.get().isLoaded("ftbultimine")) {
            JDTEUltimineIntegration.register();
        }
    }

    private void syncSpawnEggRecipes(OnDatapackSyncEvent event) {
        MobLootSpawnEggHelper.invalidate(event.getPlayerList().getServer().getResourceManager());
        SpawnEggRecipeSyncPayload payload = new SpawnEggRecipeSyncPayload(
                MobLootSpawnEggHelper.getRecipeIds(event.getPlayerList().getServer().getResourceManager()));
        LootFabricatorLootSyncPayload lootPayload = new LootFabricatorLootSyncPayload(
                MobLootSpawnEggHelper.getLootDropsBySpawnEgg(event.getPlayerList().getServer().getResourceManager()));
        if (event.getPlayer() != null) {
            PacketDistributor.sendToPlayer(event.getPlayer(), payload);
            PacketDistributor.sendToPlayer(event.getPlayer(), lootPayload);
        } else {
            PacketDistributor.sendToAllPlayers(payload);
            PacketDistributor.sendToAllPlayers(lootPayload);
        }
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
                JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get(),
                JDTEBlocks.CRYSTAL_INCUBATOR.get()
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
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.EntitySuppressorBE suppressor ? suppressor.getEnergyStorage() : null,
                JDTEBlocks.ENTITY_SUPPRESSOR.get());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.RangeBlockerBE blocker ? blocker.getEnergyStorage() : null,
                JDTEBlocks.RANGE_BLOCKER.get());
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE fluidMachine ? fluidMachine.getFluidTank() : null,
                JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(),
                JDTEBlocks.EXTENDED_FLUID_PLACER.get()
        );
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.CrystalIncubatorBE incubator
                        ? incubator.getEnergyStorage() : null,
                JDTEBlocks.CRYSTAL_INCUBATOR.get());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.GreenhouseBE greenhouse
                        ? greenhouse.getEnergyStorage() : null,
                JDTEBlocks.GREENHOUSE.get());
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.GreenhouseBE greenhouse
                        ? greenhouse.getFluidTank() : null,
                JDTEBlocks.GREENHOUSE.get());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.BioFactoryBE factory
                        ? factory.getEnergyStorage() : null,
                JDTEBlocks.BIO_FACTORY.get());
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.BioFactoryBE factory
                        ? factory.getCombinedFluidHandler() : null,
                JDTEBlocks.BIO_FACTORY.get());

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
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.ExtendedBioCrusherBE crusher ? crusher.getOutputItemHandler() : null,
                JDTEBlocks.EXTENDED_BIO_CRUSHER.get()
        );

        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator ? fabricator.getEnergyStorage() : null,
                JDTEBlocks.LOOT_FABRICATOR.get());
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator ? fabricator.getFluidHandler() : null,
                JDTEBlocks.LOOT_FABRICATOR.get());
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator ? fabricator.getAutomationItemHandler() : null,
                JDTEBlocks.LOOT_FABRICATOR.get());

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

        // Potion Brewer energy storage, fluid handler, and item handler
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE powered ? powered.getEnergyStorage() : null,
                JDTEBlocks.ADVANCED_POTION_BREWER.get()
        );
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.AdvancedPotionBrewerBE brewer ? brewer.getFluidHandler() : null,
                JDTEBlocks.ADVANCED_POTION_BREWER.get()
        );
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.AdvancedPotionBrewerBE brewer ? brewer.getAutomationItemHandler(side) : null,
                JDTEBlocks.ADVANCED_POTION_BREWER.get()
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

        // Item handler for Gel Generator (inputs can be inserted; only outputs can be extracted)
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.GelGeneratorBE generator ? generator.getAutomationItemHandler() : null,
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
                JDTEBlocks.EXTENDED_ITEM_RECEIVER.get(),
                JDTEBlocks.CRYSTAL_INCUBATOR.get()
        );
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.GreenhouseBE greenhouse
                        ? greenhouse.getAutomationItemHandler() : null,
                JDTEBlocks.GREENHOUSE.get());
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> be instanceof com.jdte.common.blockentities.BioFactoryBE factory
                        ? factory.getAutomationItemHandler() : null,
                JDTEBlocks.BIO_FACTORY.get());

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
