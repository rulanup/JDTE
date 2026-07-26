package com.jdte.common.factory;

import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.setup.JDTEConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FactoryOperationReporter {
    public static final boolean DEBUG_LOGGING = Boolean.getBoolean("jdte.factoryPackerDebug");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_BLACKLIST_REPORT_ENTRIES = 16;

    public interface Host {
        UUID packageId();
        FactoryPackerBE.Phase phase();
        int sourceRetryCount();
        int cursor();
        int totalWork();
        int errorCode();
        UUID operationOwner();
    }

    private final Host host;
    private List<BlacklistedBlock> blacklistedBlocks = new ArrayList<>();
    private int blacklistedBlockCount;
    private FactoryPackerBE.Phase lastNotifiedPhase;

    public FactoryOperationReporter(Host host) {
        this.host = host;
    }

    public void resetBlacklist() {
        blacklistedBlocks = new ArrayList<>();
        blacklistedBlockCount = 0;
    }

    public void resetPhaseNotification() {
        lastNotifiedPhase = null;
    }

    public int blacklistedBlockCount() {
        return blacklistedBlockCount;
    }

    public void notifyPhaseChange(ServerLevel level) {
        FactoryPackerBE.Phase phase = host.phase();
        if (phase == FactoryPackerBE.Phase.IDLE || phase == lastNotifiedPhase) return;
        lastNotifiedPhase = phase;
        debugLog("phase", "cursor=" + host.cursor() + "/" + host.totalWork());
        if (!JDTEConfig.COMMON.factoryPackerChatNotifications.get()) return;
        sendOwnerMessage(level, Component.translatable("message.jdte.factory_packer.operation_phase",
                Component.translatable("screen.jdte.factory_packer.phase." + phase.ordinal()),
                Math.max(0, host.cursor()), Math.max(0, host.totalWork())).withStyle(ChatFormatting.GRAY));
    }

    public void notifyOperationResult(ServerLevel level) {
        int errorCode = host.errorCode();
        debugLog(errorCode == 0 ? "operation-complete" : "operation-failed",
                "errorCode=" + errorCode + " cursor=" + host.cursor() + "/" + host.totalWork());
        if (!JDTEConfig.COMMON.factoryPackerChatNotifications.get()) return;
        Component result = errorCode == 0
                ? Component.translatable("message.jdte.factory_packer.operation_complete")
                        .withStyle(ChatFormatting.GREEN)
                : Component.translatable("message.jdte.factory_packer.operation_failed", errorCode,
                        Component.translatable("screen.jdte.factory_packer.error." + errorCode))
                        .withStyle(ChatFormatting.RED);
        sendOwnerMessage(level, result);
    }

    public void reportSourceChange(ServerLevel level, String stage, BlockPos pos, BlockState expected,
                                   BlockState current, String reason) {
        int maxRetries = JDTEConfig.COMMON.factoryPackerSourceChangeRetries.get();
        boolean willRetry = host.sourceRetryCount() < maxRetries;
        debugLog("source-change", "stage=" + stage + " reason=" + reason + " pos=" + pos
                + " expected=" + blockStateDescription(expected) + " current=" + blockStateDescription(current)
                + " willRetry=" + willRetry + " nextRetry=" + (host.sourceRetryCount() + 1) + "/" + maxRetries);
        if (!JDTEConfig.COMMON.factoryPackerChatNotifications.get()) return;
        ServerPlayer player = owner(level);
        if (player == null) return;
        Component coordinates = interactiveCoordinates(pos, player.hasPermissions(2),
                "message.jdte.factory_packer.source_change_teleport");
        Component retry = willRetry
                ? Component.translatable("message.jdte.factory_packer.source_change_retry",
                        host.sourceRetryCount() + 1, maxRetries)
                : Component.translatable("message.jdte.factory_packer.source_change_final");
        player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.source_change",
                Component.translatable("message.jdte.factory_packer.source_stage." + stage),
                Component.translatable("message.jdte.factory_packer.source_reason." + reason), coordinates, retry)
                .withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.source_change_blocks",
                blockStateComponent(expected), blockStateComponent(current)).withStyle(ChatFormatting.YELLOW));
    }

    public void reportIncompleteReactor(ServerLevel level, BlockPos min, BlockPos max) {
        if (host.operationOwner() == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(host.operationOwner());
        if (player != null) {
            player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.incomplete_reactor",
                    min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ())
                    .withStyle(ChatFormatting.RED));
        }
    }

    public static String sourceChangeReason(BlockState expected, BlockState current) {
        if (expected.isAir()) return "unexpected_block";
        if (current.isAir()) return "missing_block";
        return "replaced_block";
    }

    private static Component blockStateComponent(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return Component.translatable("message.jdte.factory_packer.source_block",
                state.getBlock().getName(), id.toString());
    }

    public static String blockStateDescription(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + state;
    }

    private ServerPlayer owner(ServerLevel level) {
        return host.operationOwner() == null ? null
                : level.getServer().getPlayerList().getPlayer(host.operationOwner());
    }

    private void sendOwnerMessage(ServerLevel level, Component component) {
        ServerPlayer player = owner(level);
        if (player != null) player.sendSystemMessage(component);
    }

    public void debugLog(String event, String details) {
        if (DEBUG_LOGGING) {
            LOGGER.warn("[FactoryPacker/Debug] package={} phase={} retry={} event={} {}", host.packageId(),
                    host.phase(), host.sourceRetryCount(), event, details);
        }
    }

    public void rememberBlacklistedBlock(BlockState state, BlockPos pos) {
        blacklistedBlockCount++;
        if (blacklistedBlocks.size() < MAX_BLACKLIST_REPORT_ENTRIES) {
            blacklistedBlocks.add(new BlacklistedBlock(state, pos.immutable()));
        }
    }

    public void reportBlacklistedBlocks(ServerLevel level) {
        if (host.operationOwner() == null || blacklistedBlockCount == 0) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(host.operationOwner());
        if (player == null) return;

        player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.blacklist_summary",
                blacklistedBlockCount, FactoryPackerBE.BLACKLIST.location().toString())
                .withStyle(ChatFormatting.RED));
        boolean canTeleport = player.hasPermissions(2);
        for (BlacklistedBlock entry : blacklistedBlocks) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(entry.state().getBlock());
            Component coordinates = interactiveCoordinates(entry.pos(), canTeleport,
                    "message.jdte.factory_packer.blacklist_teleport");
            player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.blacklist_entry",
                    entry.state().getBlock().getName(), id.toString(), coordinates)
                    .withStyle(ChatFormatting.YELLOW));
        }
        int hidden = blacklistedBlockCount - blacklistedBlocks.size();
        if (hidden > 0) {
            player.sendSystemMessage(Component.translatable("message.jdte.factory_packer.blacklist_more", hidden)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component interactiveCoordinates(BlockPos pos, boolean canTeleport, String hoverKey) {
        MutableComponent coordinates = Component.literal("[" + pos.getX() + ", " + pos.getY() + ", "
                + pos.getZ() + "]").withStyle(ChatFormatting.AQUA);
        if (!canTeleport) return coordinates;
        String command = "/tp @s " + (pos.getX() + 0.5D) + " " + (pos.getY() + 1) + " "
                + (pos.getZ() + 0.5D);
        return coordinates.withStyle(style -> style.withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(hoverKey))));
    }

    private record BlacklistedBlock(BlockState state, BlockPos pos) {}
}
