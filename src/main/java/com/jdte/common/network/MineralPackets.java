package com.jdte.common.network;

import com.jdte.JDTE;
import com.jdte.common.containers.MineralExtractorContainer;
import com.jdte.common.minerals.MineralSurveyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

/** Versioned, direction-restricted mineral UI messages. */
public final class MineralPackets {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(JDTE.id("minerals"),
            () -> "2", "2"::equals, "2"::equals);
    private MineralPackets() { }
    public record OpenSurvey(MineralSurveyData survey) { }
    public record Catalog(boolean reset, boolean complete, java.util.List<MineralSurveyData> surveys) { }
    public record Slots(int containerId, int page, java.util.List<net.minecraft.world.item.ItemStack> stacks) { }
    public record Page(int containerId, int page) { }
    public static void register() {
        CHANNEL.messageBuilder(OpenSurvey.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((msg, buf) -> MineralSurveyData.write(buf, msg.survey()))
                .decoder(buf -> new OpenSurvey(MineralSurveyData.read(buf)))
                .consumerMainThread((msg, ctx) -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.jdte.client.MineralClientPackets.open(msg.survey()));
                    ctx.get().setPacketHandled(true);
                }).add();
        CHANNEL.messageBuilder(Slots.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MineralPackets::writeSlots).decoder(MineralPackets::readSlots)
                .consumerMainThread((msg, ctx) -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.jdte.client.MineralClientPackets.slots(msg));
                    ctx.get().setPacketHandled(true);
                }).add();
        CHANNEL.messageBuilder(Catalog.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((msg, buf) -> { buf.writeBoolean(msg.reset()); buf.writeBoolean(msg.complete());
                    buf.writeCollection(msg.surveys(), MineralSurveyData::write); })
                .decoder(buf -> {
                    boolean reset = buf.readBoolean(), complete = buf.readBoolean();
                    int count = buf.readVarInt();
                    if (count < 0 || count > 32) throw new io.netty.handler.codec.DecoderException("Invalid mineral catalog chunk");
                    java.util.List<MineralSurveyData> surveys = new java.util.ArrayList<>(count);
                    for (int i = 0; i < count; i++) surveys.add(MineralSurveyData.read(buf));
                    return new Catalog(reset, complete, surveys);
                })
                .consumerMainThread((msg, ctx) -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.jdte.client.MineralSurveyClientCache.accept(msg.reset(), msg.complete(), msg.surveys()));
                    ctx.get().setPacketHandled(true);
                }).add();
        CHANNEL.messageBuilder(Page.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder((msg, buf) -> { buf.writeVarInt(msg.containerId()); buf.writeVarInt(msg.page()); })
                .decoder(buf -> new Page(buf.readVarInt(), buf.readVarInt()))
                .consumerMainThread((msg, ctx) -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null && player.containerMenu instanceof MineralExtractorContainer menu
                            && menu.containerId == msg.containerId() && menu.stillValid(player)) {
                        menu.setOutputPage(msg.page());
                        menu.broadcastChanges();
                    }
                    ctx.get().setPacketHandled(true);
                }).add();
    }
    // Vanilla 1.20 writes stack counts as a byte. Mineral outputs can hold 6.4 million items.
    public static void writeSlots(Slots msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId()); buf.writeVarInt(msg.page());
        if (msg.stacks().size() != 16) throw new IllegalArgumentException("Expected 16 output slots");
        for (var stack : msg.stacks()) {
            buf.writeItem(stack.copyWithCount(1));
            buf.writeVarInt(stack.getCount());
        }
    }
    public static Slots readSlots(FriendlyByteBuf buf) {
        int id = buf.readVarInt(), page = buf.readVarInt();
        if (page < 0 || page > 3) throw new io.netty.handler.codec.DecoderException("Invalid mineral output page");
        java.util.List<net.minecraft.world.item.ItemStack> stacks = new java.util.ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            var stack = buf.readItem(); int count = buf.readVarInt();
            if (count < 0 || count > 6_400_000) throw new io.netty.handler.codec.DecoderException("Invalid mineral output count");
            stack.setCount(count); stacks.add(stack);
        }
        return new Slots(id, page, stacks);
    }
    public static void sendSlots(ServerPlayer player, Slots slots) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), slots);
    }
    public static void sendCatalog(ServerPlayer player) {
        var surveys = com.jdte.common.minerals.MineralSurveyIndex.surveys(player.server);
        for (int start = 0; start < Math.max(1, surveys.size()); start += 32) {
            int end = Math.min(start + 32, surveys.size());
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Catalog(start == 0,
                    end == surveys.size(), surveys.subList(start, end)));
        }
    }
    public static void sendSurvey(ServerPlayer player, MineralSurveyData survey) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenSurvey(survey));
    }
    public static void requestPage(int containerId, int page) { CHANNEL.sendToServer(new Page(containerId, page)); }
}
