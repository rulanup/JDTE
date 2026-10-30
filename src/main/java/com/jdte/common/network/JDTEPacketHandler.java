package com.jdte.common.network;

import com.jdte.JDTE;
import com.jdte.common.network.data.AutoIoConfigPayload;
import com.jdte.common.network.data.AutoIoConfigSyncPayload;
import com.jdte.common.network.data.BioCrusherPayload;
import com.jdte.common.network.data.EntitySuppressorPayload;
import com.jdte.common.network.data.EntitySuppressorSyncPayload;
import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload;
import com.jdte.common.network.data.FactoryPackagePreviewRequestPayload;
import com.jdte.common.network.data.FactoryPackageRotatePayload;
import com.jdte.common.network.data.FactoryPackerStartPayload;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.network.data.GelGeneratorPayload;
import com.jdte.common.network.data.LifeBreederModePayload;
import com.jdte.common.network.data.LifeExtractorPayload;
import com.jdte.common.network.data.LifeSynthesisRunningPayload;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import com.jdte.common.network.data.PotionBrewerFuelInputPayload;
import com.jdte.common.network.data.PotionBrewerRecipeLockPayload;
import com.jdte.common.network.data.PotionBrewerRecipeLockSyncPayload;
import com.jdte.common.network.data.RangeBlockerPayload;
import com.jdte.common.network.data.RangeBlockerSyncPayload;
import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.network.data.TimeFreezerPayload;
import com.jdte.common.network.data.UltimatePortalGunPayload;
import com.jdte.common.network.data.WrenchAreaAdjustPayload;
import com.jdte.common.network.data.WrenchAreaAdjustResultPayload;
import com.jdte.common.network.data.WrenchAreaSelectionPayload;
import com.jdte.common.network.handler.AutoIoConfigPacket;
import com.jdte.common.network.handler.BioCrusherPacket;
import com.jdte.common.network.handler.EntitySuppressorPacket;
import com.jdte.common.network.handler.EntitySuppressorSyncPacket;
import com.jdte.common.network.handler.FactoryPackagePreviewChunkPacket;
import com.jdte.common.network.handler.FactoryPackagePreviewRequestPacket;
import com.jdte.common.network.handler.FactoryPackageRotatePacket;
import com.jdte.common.network.handler.FactoryPackerStartPacket;
import com.jdte.common.network.handler.FilterPagePacket;
import com.jdte.common.network.handler.GelGeneratorPacket;
import com.jdte.common.network.handler.LifeBreederModePacket;
import com.jdte.common.network.handler.LifeExtractorPacket;
import com.jdte.common.network.handler.LifeSynthesisRunningPacket;
import com.jdte.common.network.handler.LootFabricatorLootSyncPacket;
import com.jdte.common.network.handler.PotionBrewerFuelInputPacket;
import com.jdte.common.network.handler.PotionBrewerRecipeLockPacket;
import com.jdte.common.network.handler.RangeBlockerPacket;
import com.jdte.common.network.handler.RangeBlockerSyncPacket;
import com.jdte.common.network.handler.SpawnEggRecipeSyncPacket;
import com.jdte.common.network.handler.TimeAcceleratorPacket;
import com.jdte.common.network.handler.TimeFreezerPacket;
import com.jdte.common.network.handler.UltimatePortalGunPacket;
import com.jdte.common.network.handler.WrenchAreaAdjustPacket;
import com.jdte.common.network.handler.WrenchAreaAdjustResultPacket;
import com.jdte.common.network.handler.WrenchAreaSelectionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Forge 1.20.1 message registration for all JDTE client/server interactions. */
public final class JDTEPacketHandler {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            JDTE.id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int nextPacketId;

    private JDTEPacketHandler() {
    }

    public static void registerNetworking(FMLCommonSetupEvent event) {
        registerServer(com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload.class,com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload::encode,com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload::decode,com.jdte.common.network.handler.ExtendedExperienceHolderSettingsPacket::handle);
        registerServer(com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload.class,
                com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload::encode,
                com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload::decode,
                com.jdte.common.network.handler.AdvancedUpgradeStorageActionPacket::handle);
        registerServer(com.jdte.common.network.data.OpenLargeFuelCanisterPayload.class, com.jdte.common.network.data.OpenLargeFuelCanisterPayload::encode, com.jdte.common.network.data.OpenLargeFuelCanisterPayload::decode, (payload, context) -> context.enqueueWork(() -> com.jdte.common.containers.LargeFuelCanisterMenuAccess.open(context.player(), true)));
        registerServer(com.jdte.common.network.data.AdvancedEnergyTransmitterPayload.class, com.jdte.common.network.data.AdvancedEnergyTransmitterPayload::encode, com.jdte.common.network.data.AdvancedEnergyTransmitterPayload::decode, com.jdte.common.network.handler.AdvancedEnergyTransmitterPacket::handle);
        registerServer(com.jdte.common.network.data.AdvancedEnergyTransmitterBindingPayload.class, com.jdte.common.network.data.AdvancedEnergyTransmitterBindingPayload::encode, com.jdte.common.network.data.AdvancedEnergyTransmitterBindingPayload::decode, com.jdte.common.network.handler.AdvancedEnergyTransmitterBindingPacket::handle);
        registerServer(com.jdte.common.network.data.ExtendedEnergyTransmitterSettingPayload.class, com.jdte.common.network.data.ExtendedEnergyTransmitterSettingPayload::encode, com.jdte.common.network.data.ExtendedEnergyTransmitterSettingPayload::decode, com.jdte.common.network.handler.ExtendedEnergyTransmitterPacket::handle);
        registerServer(TimeAcceleratorPayload.class, TimeAcceleratorPayload::encode, TimeAcceleratorPayload::decode,
                TimeAcceleratorPacket.get()::handle);
        registerServer(TimeFreezerPayload.class, TimeFreezerPayload::encode, TimeFreezerPayload::decode,
                TimeFreezerPacket::handle);
        registerServer(UltimatePortalGunPayload.class, UltimatePortalGunPayload::encode,
                UltimatePortalGunPayload::decode, UltimatePortalGunPacket::handle);
        registerServer(GelGeneratorPayload.class, GelGeneratorPayload::encode, GelGeneratorPayload::decode,
                GelGeneratorPacket.get()::handle);
        registerServer(LifeExtractorPayload.class, LifeExtractorPayload::encode, LifeExtractorPayload::decode,
                LifeExtractorPacket.get()::handle);
        registerServer(BioCrusherPayload.class, BioCrusherPayload::encode, BioCrusherPayload::decode,
                BioCrusherPacket.get()::handle);
        registerServer(FilterPagePayload.class, FilterPagePayload::encode, FilterPagePayload::decode,
                FilterPagePacket.get()::handle);
        registerServer(AutoIoConfigPayload.class, AutoIoConfigPayload::encode, AutoIoConfigPayload::decode,
                AutoIoConfigPacket.get()::handleServer);
        registerClient(com.jdte.common.network.data.EntityAccelerationSyncPayload.class,com.jdte.common.network.data.EntityAccelerationSyncPayload::encode,com.jdte.common.network.data.EntityAccelerationSyncPayload::decode,
            (payload,context)->context.enqueueWork(()->com.jdte.client.EntityAccelerationClientSync.accept(payload)));
        registerClient(AutoIoConfigSyncPayload.class, AutoIoConfigSyncPayload::encode, AutoIoConfigSyncPayload::decode,
                AutoIoConfigPacket.get()::handleClient);
        registerServer(PotionBrewerRecipeLockPayload.class, PotionBrewerRecipeLockPayload::encode,
                PotionBrewerRecipeLockPayload::decode, PotionBrewerRecipeLockPacket.get()::handleServer);
        registerClient(PotionBrewerRecipeLockSyncPayload.class, PotionBrewerRecipeLockSyncPayload::encode,
                PotionBrewerRecipeLockSyncPayload::decode, PotionBrewerRecipeLockPacket.get()::handleClient);
        registerServer(PotionBrewerFuelInputPayload.class, PotionBrewerFuelInputPayload::encode,
                PotionBrewerFuelInputPayload::decode, PotionBrewerFuelInputPacket::handle);
        registerServer(WrenchAreaAdjustPayload.class, WrenchAreaAdjustPayload::encode, WrenchAreaAdjustPayload::decode,
                WrenchAreaAdjustPacket.get()::handle);
        registerClient(WrenchAreaAdjustResultPayload.class, WrenchAreaAdjustResultPayload::encode,
                WrenchAreaAdjustResultPayload::decode, WrenchAreaAdjustResultPacket.get()::handle);
        registerServer(WrenchAreaSelectionPayload.class, WrenchAreaSelectionPayload::encode,
                WrenchAreaSelectionPayload::decode, WrenchAreaSelectionPacket.get()::handleServer);
        registerClient(SpawnEggRecipeSyncPayload.class, SpawnEggRecipeSyncPayload::encode,
                SpawnEggRecipeSyncPayload::decode, SpawnEggRecipeSyncPacket::handle);
        registerClient(LootFabricatorLootSyncPayload.class, LootFabricatorLootSyncPayload::encode,
                LootFabricatorLootSyncPayload::decode, LootFabricatorLootSyncPacket::handle);
        registerServer(EntitySuppressorPayload.class, EntitySuppressorPayload::encode, EntitySuppressorPayload::decode,
                EntitySuppressorPacket::handle);
        registerClient(EntitySuppressorSyncPayload.class, EntitySuppressorSyncPayload::encode,
                EntitySuppressorSyncPayload::decode, EntitySuppressorSyncPacket::handle);
        registerServer(RangeBlockerPayload.class, RangeBlockerPayload::encode, RangeBlockerPayload::decode,
                RangeBlockerPacket::handle);
        registerClient(RangeBlockerSyncPayload.class, RangeBlockerSyncPayload::encode,
                RangeBlockerSyncPayload::decode, RangeBlockerSyncPacket::handle);
        registerServer(FactoryPackerStartPayload.class, FactoryPackerStartPayload::encode, FactoryPackerStartPayload::decode,
                FactoryPackerStartPacket::handle);
        registerServer(FactoryPackageRotatePayload.class, FactoryPackageRotatePayload::encode,
                FactoryPackageRotatePayload::decode, FactoryPackageRotatePacket::handle);
        registerServer(FactoryPackagePreviewRequestPayload.class, FactoryPackagePreviewRequestPayload::encode,
                FactoryPackagePreviewRequestPayload::decode, FactoryPackagePreviewRequestPacket::handle);
        registerClient(FactoryPackagePreviewChunkPayload.class, FactoryPackagePreviewChunkPayload::encode,
                FactoryPackagePreviewChunkPayload::decode, FactoryPackagePreviewChunkPacket::handle);
        registerServer(LifeBreederModePayload.class, LifeBreederModePayload::encode, LifeBreederModePayload::decode,
                LifeBreederModePacket::handle);
        registerClient(LifeSynthesisRunningPayload.class, LifeSynthesisRunningPayload::encode,
                LifeSynthesisRunningPayload::decode, LifeSynthesisRunningPacket::handle);
        registerServer(com.jdte.common.network.data.OpenLargePotionCanisterPayload.class,com.jdte.common.network.data.OpenLargePotionCanisterPayload::encode,com.jdte.common.network.data.OpenLargePotionCanisterPayload::decode,(payload,context)->context.enqueueWork(()->com.jdte.common.containers.LargePotionCanisterMenuAccess.open(context.player(),true)));
        registerServer(com.jdte.common.network.data.OpenLargePocketGeneratorPayload.class,com.jdte.common.network.data.OpenLargePocketGeneratorPayload::encode,com.jdte.common.network.data.OpenLargePocketGeneratorPayload::decode,(payload,context)->context.enqueueWork(()->com.jdte.common.containers.LargePotionCanisterMenuAccess.open(context.player(),true)));
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendToTrackingChunk(ServerLevel level, ChunkPos chunkPos, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x, chunkPos.z)), message);
    }

    private static <M> void registerServer(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder,
                                           Function<FriendlyByteBuf, M> decoder,
                                           BiConsumer<M, PacketContext> handler) {
        register(type, encoder, decoder, handler, NetworkDirection.PLAY_TO_SERVER);
    }

    private static <M> void registerClient(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder,
                                           Function<FriendlyByteBuf, M> decoder,
                                           BiConsumer<M, PacketContext> handler) {
        register(type, encoder, decoder, handler, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <M> void register(Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, M> decoder, BiConsumer<M, PacketContext> handler,
                                     NetworkDirection direction) {
        CHANNEL.registerMessage(nextPacketId++, type, encoder, decoder,
                (message, contextSupplier) -> dispatch(message, contextSupplier, handler), Optional.of(direction));
    }

    private static <M> void dispatch(M message, Supplier<NetworkEvent.Context> contextSupplier,
                                     BiConsumer<M, PacketContext> handler) {
        NetworkEvent.Context context = contextSupplier.get();
        handler.accept(message, new PacketContext(context));
        context.setPacketHandled(true);
    }
}
