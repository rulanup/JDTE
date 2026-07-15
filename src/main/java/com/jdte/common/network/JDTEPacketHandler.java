package com.jdte.common.network;

import com.jdte.JDTE;
import com.jdte.common.network.data.AutoIoConfigPayload;
import com.jdte.common.network.data.AutoIoConfigSyncPayload;
import com.jdte.common.network.data.BioCrusherPayload;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.network.data.GelGeneratorPayload;
import com.jdte.common.network.data.LifeExtractorPayload;
import com.jdte.common.network.data.PotionBrewerRecipeLockPayload;
import com.jdte.common.network.data.PotionBrewerRecipeLockSyncPayload;
import com.jdte.common.network.data.PotionBrewerFuelInputPayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.network.data.WrenchAreaAdjustPayload;
import com.jdte.common.network.data.WrenchAreaAdjustResultPayload;
import com.jdte.common.network.data.WrenchAreaSelectionPayload;
import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import com.jdte.common.network.data.EntitySuppressorPayload;
import com.jdte.common.network.data.EntitySuppressorSyncPayload;
import com.jdte.common.network.data.RangeBlockerPayload;
import com.jdte.common.network.data.RangeBlockerSyncPayload;
import com.jdte.common.network.handler.EntitySuppressorPacket;
import com.jdte.common.network.handler.EntitySuppressorSyncPacket;
import com.jdte.common.network.handler.RangeBlockerPacket;
import com.jdte.common.network.handler.RangeBlockerSyncPacket;
import com.jdte.common.network.handler.AutoIoConfigPacket;
import com.jdte.common.network.handler.BioCrusherPacket;
import com.jdte.common.network.handler.FilterPagePacket;
import com.jdte.common.network.handler.GelGeneratorPacket;
import com.jdte.common.network.handler.LifeExtractorPacket;
import com.jdte.common.network.handler.PotionBrewerRecipeLockPacket;
import com.jdte.common.network.handler.PotionBrewerFuelInputPacket;
import com.jdte.common.network.handler.TimeAcceleratorPacket;
import com.jdte.common.network.handler.WrenchAreaAdjustPacket;
import com.jdte.common.network.handler.WrenchAreaAdjustResultPacket;
import com.jdte.common.network.handler.WrenchAreaSelectionPacket;
import com.jdte.common.network.handler.SpawnEggRecipeSyncPacket;
import com.jdte.common.network.handler.LootFabricatorLootSyncPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class JDTEPacketHandler {
    public static void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(JDTE.MODID);
        registrar.playToServer(TimeAcceleratorPayload.TYPE, TimeAcceleratorPayload.STREAM_CODEC, TimeAcceleratorPacket.get()::handle);
        registrar.playToServer(GelGeneratorPayload.TYPE, GelGeneratorPayload.STREAM_CODEC, GelGeneratorPacket.get()::handle);
        registrar.playToServer(LifeExtractorPayload.TYPE, LifeExtractorPayload.STREAM_CODEC, LifeExtractorPacket.get()::handle);
        registrar.playToServer(BioCrusherPayload.TYPE, BioCrusherPayload.STREAM_CODEC, BioCrusherPacket.get()::handle);
        registrar.playToServer(FilterPagePayload.TYPE, FilterPagePayload.STREAM_CODEC, FilterPagePacket.get()::handle);
        registrar.playToServer(AutoIoConfigPayload.TYPE, AutoIoConfigPayload.STREAM_CODEC, AutoIoConfigPacket.get()::handleServer);
        registrar.playToClient(AutoIoConfigSyncPayload.TYPE, AutoIoConfigSyncPayload.STREAM_CODEC, AutoIoConfigPacket.get()::handleClient);
        registrar.playToServer(PotionBrewerRecipeLockPayload.TYPE, PotionBrewerRecipeLockPayload.STREAM_CODEC, PotionBrewerRecipeLockPacket.get()::handleServer);
        registrar.playToClient(PotionBrewerRecipeLockSyncPayload.TYPE, PotionBrewerRecipeLockSyncPayload.STREAM_CODEC, PotionBrewerRecipeLockPacket.get()::handleClient);
        registrar.playToServer(PotionBrewerFuelInputPayload.TYPE, PotionBrewerFuelInputPayload.STREAM_CODEC, PotionBrewerFuelInputPacket::handle);
        registrar.playToServer(WrenchAreaAdjustPayload.TYPE, WrenchAreaAdjustPayload.STREAM_CODEC, WrenchAreaAdjustPacket.get()::handle);
        registrar.playToClient(WrenchAreaAdjustResultPayload.TYPE, WrenchAreaAdjustResultPayload.STREAM_CODEC, WrenchAreaAdjustResultPacket.get()::handle);
        registrar.playToServer(WrenchAreaSelectionPayload.TYPE, WrenchAreaSelectionPayload.STREAM_CODEC, WrenchAreaSelectionPacket.get()::handleServer);
        registrar.playToClient(SpawnEggRecipeSyncPayload.TYPE, SpawnEggRecipeSyncPayload.STREAM_CODEC, SpawnEggRecipeSyncPacket::handle);
        registrar.playToClient(LootFabricatorLootSyncPayload.TYPE, LootFabricatorLootSyncPayload.STREAM_CODEC, LootFabricatorLootSyncPacket::handle);
        registrar.playToServer(EntitySuppressorPayload.TYPE, EntitySuppressorPayload.STREAM_CODEC, EntitySuppressorPacket::handle);
        registrar.playToClient(EntitySuppressorSyncPayload.TYPE, EntitySuppressorSyncPayload.STREAM_CODEC, EntitySuppressorSyncPacket::handle);
        registrar.playToServer(RangeBlockerPayload.TYPE, RangeBlockerPayload.STREAM_CODEC, RangeBlockerPacket::handle);
        registrar.playToClient(RangeBlockerSyncPayload.TYPE, RangeBlockerSyncPayload.STREAM_CODEC, RangeBlockerSyncPacket::handle);
    }
}
