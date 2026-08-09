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
import com.jdte.common.blockentities.TimeFreezerManager;
import com.jdte.common.blockentities.GreenhouseOutputManager;
import com.jdte.common.blockentities.RangeBlockerManager;
import com.jdte.common.integrations.JDTEUltimineIntegration;
import com.jdte.common.integrations.curios.BigFluidTankCuriosIntegration;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.utils.BioCrusherDropCapture;
import com.jdte.common.utils.MobLootSpawnEggHelper;
import com.jdte.common.player.LifeAppleProgression;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.recipes.RecipeCacheSignal;
import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.PacketDistributor;

@Mod(JDTE.MODID)
public class JDTE {
    public static final String MODID = "jdte";

    public JDTE() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JDTEConfig.COMMON_SPEC, JDTE.MODID + "/jdte.toml");
        JDTEBlocks.BLOCKS.register(modEventBus);
        JDTEItems.ITEMS.register(modEventBus);
        JDTEBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        JDTEMenus.MENUS.register(modEventBus);
        JDTECreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        JDTEEntities.ENTITIES.register(modEventBus);
        JDTEFluids.FLUID_TYPES.register(modEventBus);
        JDTEFluids.FLUIDS.register(modEventBus);
        JDTEFluids.FLUID_BLOCKS.register(modEventBus);
        JDTEFluids.BUCKET_ITEMS.register(modEventBus);
        JDTERecipes.RECIPE_TYPES.register(modEventBus);
        JDTERecipes.RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(JDTEAttachments::registerCapabilities);
        modEventBus.addListener(JDTEPacketHandler::registerNetworking);
        MinecraftForge.EVENT_BUS.addGenericListener(net.minecraft.world.level.block.entity.BlockEntity.class, JDTEAttachments::attachBlockCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(net.minecraft.world.entity.Entity.class, JDTEAttachments::attachPlayerCapabilities);
        MinecraftForge.EVENT_BUS.addListener(JDTECommands::register);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, BioCrusherDropCapture::onLivingDrops);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, BioCrusherDropCapture::onLivingExperienceDrop);
        MinecraftForge.EVENT_BUS.addListener(this::syncSpawnEggRecipes);
        MinecraftForge.EVENT_BUS.addListener(LifeAppleProgression::onClone);
        MinecraftForge.EVENT_BUS.addListener(LifeAppleProgression::onLogin);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, AdvancedItemCollectorManager::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, AdvancedItemCollectorManager::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(AdvancedItemCollectorManager::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(AdvancedItemCollectorManager::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerTickPost);
        MinecraftForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(ExtendedTimeAccelerationManager::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(TimeFreezerManager::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(TimeFreezerManager::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, GreenhouseOutputManager::onServerTickPost);
        MinecraftForge.EVENT_BUS.addListener(GreenhouseOutputManager::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(GreenhouseOutputManager::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntitySuppressorManager::onItemPickup);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntitySuppressorManager::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntitySuppressorManager::onMobSpawnPosition);
        MinecraftForge.EVENT_BUS.addListener(EntitySuppressorManager::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(RangeBlockerManager::onEntityLeave);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onTeleport);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onExplosionDetonate);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RangeBlockerManager::onPlaySoundAtPosition);
        MinecraftForge.EVENT_BUS.addListener(RangeBlockerManager::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, com.jdte.common.factory.FactoryPermissionProbe::onBlockBreak);
        if (ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.addListener(BigFluidTankCuriosIntegration::onPlayerLoggedIn);
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> com.jdte.client.JDTEClientMod::new);
        if (ModList.get().isLoaded("ftbultimine")) {
            JDTEUltimineIntegration.register();
        }
    }

    private void syncSpawnEggRecipes(OnDatapackSyncEvent event) {
        GreenhouseCropResolver.invalidateCaches();
        RecipeCacheSignal.invalidate();
        MobLootSpawnEggHelper.invalidate(event.getPlayerList().getServer().getResourceManager());
        SpawnEggRecipeSyncPayload payload = new SpawnEggRecipeSyncPayload(
                MobLootSpawnEggHelper.getRecipeIds(event.getPlayerList().getServer().getResourceManager()));
        LootFabricatorLootSyncPayload lootPayload = new LootFabricatorLootSyncPayload(
                MobLootSpawnEggHelper.getLootDropsBySpawnEgg(event.getPlayerList().getServer().getResourceManager()));
        if (event.getPlayer() != null) {
            JDTEPacketHandler.sendToPlayer(event.getPlayer(), payload);
            JDTEPacketHandler.sendToPlayer(event.getPlayer(), lootPayload);
        } else {
            JDTEPacketHandler.sendToAll(payload);
            JDTEPacketHandler.sendToAll(lootPayload);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
