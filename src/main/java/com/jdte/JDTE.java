package com.jdte;

import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTECreativeTabs;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEEntities;
import com.jdte.setup.JDTEFluids;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTEMenus;
import com.jdte.setup.JDTERecipes;
import com.jdte.common.commands.JDTECommands;
import com.jdte.common.blockentities.AdvancedItemCollectorManager;
import com.jdte.common.blockentities.EntitySuppressorManager;
import com.jdte.common.blockentities.ExtendedTimeAccelerationManager;
import com.jdte.common.blockentities.MachineOutputManager;
import com.jdte.common.blockentities.AEOutputManager;
import com.jdte.common.blockentities.RangeBlockerManager;
import com.jdte.common.blockentities.TimeFreezerManager;
import com.jdte.common.capabilities.MachineCapabilities;
import com.jdte.common.integrations.JDTEUltimineIntegration;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.items.TimeMultitoolMiningEvents;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemstack;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySources;
import com.jdte.common.integrations.ae2.AEOutputNetwork;
import com.jdte.common.minerals.MineralSourceReloadListener;
import com.jdte.common.minerals.MineralSurveyIndex;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.utils.BioCrusherDropCapture;
import com.jdte.common.utils.MobLootSpawnEggHelper;
import com.jdte.common.player.LifeAppleProgression;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.recipes.RecipeCacheSignal;
import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(JDTE.MODID)
public class JDTE {
    public static final String MODID = "jdte";

    public JDTE(IEventBus modEventBus, ModContainer modContainer) {
        net.neoforged.neoforge.common.NeoForgeMod.enableMilkFluid();
        modContainer.registerConfig(ModConfig.Type.COMMON, JDTEConfig.COMMON_SPEC, JDTE.MODID + "/jdte.toml");
        JDTEBlocks.BLOCKS.register(modEventBus);
        JDTEItems.ITEMS.register(modEventBus);
        JDTEDataComponents.DATA_COMPONENTS.register(modEventBus);
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
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(JDTEPacketHandler::registerNetworking);
        NeoForge.EVENT_BUS.addListener(JDTECommands::register);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BioCrusherDropCapture::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, TimeMultitoolMiningEvents::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BioCrusherDropCapture::onLivingExperienceDrop);
        NeoForge.EVENT_BUS.addListener(this::syncSpawnEggRecipes);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::clearMineralIndex);
        NeoForge.EVENT_BUS.addListener(LifeAppleProgression::onClone);
        NeoForge.EVENT_BUS.addListener(LifeAppleProgression::onLogin);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AdvancedItemCollectorManager::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AdvancedItemCollectorManager::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(AdvancedItemCollectorManager::onServerTick);
        NeoForge.EVENT_BUS.addListener(AdvancedItemCollectorManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerStopped);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AEOutputManager::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MachineOutputManager::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(AEOutputManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(AEOutputManager::onServerStopped);
        NeoForge.EVENT_BUS.addListener(MachineOutputManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(MachineOutputManager::onServerStopped);
        NeoForge.EVENT_BUS.addListener(TimeFreezerManager::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(TimeFreezerManager::onServerStopped);
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
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, com.jdte.common.factory.FactoryPermissionProbe::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(com.jdte.common.integrations.curios.BigFluidTankCuriosIntegration::onPlayerLoggedIn);
        if (ModList.get().isLoaded("ftbultimine")) {
            JDTEUltimineIntegration.register();
        }
    }

    private void syncSpawnEggRecipes(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            MineralSurveyIndex.rebuild(event.getPlayerList().getServer());
        }
        GreenhouseCropResolver.invalidateCaches();
        RecipeCacheSignal.invalidate();
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

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MineralSourceReloadListener());
    }

    private void clearMineralIndex(ServerStoppedEvent event) {
        MineralSurveyIndex.clear(event.getServer());
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        MachineCapabilities.register(event);
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidHandlerItemStack(
                        com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents.FLUID_CONTAINER,
                        stack, JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.get()) {
                    @Override
                    public boolean isFluidValid(int tank, FluidStack fluid) {
                        return fluid.is(Registration.PORTAL_FLUID_TYPE.get());
                    }

                    @Override
                    public boolean canFillFluidType(FluidStack fluid) {
                        return fluid.is(Registration.PORTAL_FLUID_TYPE.get());
                    }
                },
                JDTEItems.ULTIMATE_PORTAL_GUN.get());
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new EnergyStorageItemstack(
                        JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.get(), stack),
                JDTEItems.ULTIMATE_PORTAL_GUN.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidHandlerItemStack(
                        com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents.FLUID_CONTAINER,
                        stack, com.jdte.common.items.BigFluidTankItem.MAX_MB),
                JDTEItems.BIG_FLUID_TANK.get());
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new EnergyStorageItemstack(
                        com.jdte.common.items.TimeMultitoolItem.MAX_ENERGY, stack),
                JDTEItems.TIME_MULTITOOL.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidHandlerItemStack(
                        com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents.FLUID_CONTAINER,
                        stack, com.jdte.common.items.TimeMultitoolItem.MAX_TIME_FLUID) {
                    @Override
                    public boolean isFluidValid(int tank, FluidStack fluid) {
                        return fluid.is(Registration.TIME_FLUID_TYPE.get());
                    }

                    @Override
                    public boolean canFillFluidType(FluidStack fluid) {
                        return fluid.is(Registration.TIME_FLUID_TYPE.get());
                    }
                },
                JDTEItems.TIME_MULTITOOL.get());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AEOutputNetwork::registerLinkable);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
