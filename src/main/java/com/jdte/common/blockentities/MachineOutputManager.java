package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.autoioconfig.AutoIoTransferHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Coalesces machine output changes into one adjacent-inventory flush per real server tick. */
public final class MachineOutputManager {
    private static final Map<ServerLevel, IdentityHashMap<BaseMachineBE, PendingOutput>> LEVELS =
            new IdentityHashMap<>();

    private MachineOutputManager() {
    }

    public static void submit(BaseMachineBE machine, int absoluteSlot) {
        if (!(machine.getLevel() instanceof ServerLevel level) || machine.isRemoved() || absoluteSlot < 0) return;
        PendingOutput pending = LEVELS.computeIfAbsent(level, ignored -> new IdentityHashMap<>())
                .computeIfAbsent(machine, ignored -> new PendingOutput());
        pending.slots.set(absoluteSlot);
    }

    public static void wake(BaseMachineBE machine) {
        OutputRange range = outputRange(machine);
        if (range == null) return;
        for (int slot = range.start(); slot < range.start() + range.count(); slot++) {
            ItemStack stack = machine.getMachineHandler().getStackInSlot(slot);
            if (!stack.isEmpty()) submit(machine, slot);
        }
        if (machine.getLevel() instanceof ServerLevel level) {
            PendingOutput pending = LEVELS.getOrDefault(level, new IdentityHashMap<>()).get(machine);
            if (pending != null) {
                pending.nextAttemptTick = 0L;
                pending.failureBackoff = 0;
            }
        }
    }

    private static OutputRange outputRange(BaseMachineBE machine) {
        if (machine instanceof GreenhouseBE greenhouse) {
            return new OutputRange(GreenhouseBE.OUTPUT_START_SLOT, greenhouse.getActiveOutputSlots());
        }
        if (machine instanceof CreativeGreenhouseBE greenhouse) {
            return new OutputRange(CreativeGreenhouseBE.OUTPUT_START_SLOT, greenhouse.getDistinctOutputTypes());
        }
        if (machine instanceof LargeGreenhouseBE greenhouse) {
            return new OutputRange(LargeGreenhouseBE.OUTPUT_START_SLOT, greenhouse.getActiveOutputSlots());
        }
        if (machine instanceof MineralExtractorBE extractor) {
            return new OutputRange(extractor.outputStartSlot(), extractor.getActiveOutputSlots());
        }
        return null;
    }

    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (Map.Entry<ServerLevel, IdentityHashMap<BaseMachineBE, PendingOutput>> levelEntry
                : new ArrayList<>(LEVELS.entrySet())) {
            ServerLevel level = levelEntry.getKey();
            if (level.getServer() != server) continue;
            IdentityHashMap<BaseMachineBE, PendingOutput> machines = levelEntry.getValue();
            List<Map.Entry<BaseMachineBE, PendingOutput>> snapshot = new ArrayList<>(machines.entrySet());
            for (Map.Entry<BaseMachineBE, PendingOutput> entry : snapshot) {
                BaseMachineBE machine = entry.getKey();
                PendingOutput pending = entry.getValue();
                if (machine.isRemoved() || machine.getLevel() != level) {
                    AutoIoTransferHelper.forgetEventDrivenOutput(machine);
                    machines.remove(machine);
                    continue;
                }
                pruneEmptySlots(machine, pending.slots);
                if (pending.slots.isEmpty()) {
                    machines.remove(machine);
                    continue;
                }
                long gameTime = level.getGameTime();
                if (gameTime < pending.nextAttemptTick) continue;

                int[] slots = pending.slots.stream().toArray();
                AutoIoTransferHelper.EventOutputResult result =
                        AutoIoTransferHelper.flushEventDrivenOutput(machine, slots);
                pruneEmptySlots(machine, pending.slots);
                if (result.moved()) {
                    pending.failureBackoff = 0;
                    pending.nextAttemptTick = gameTime + 1L;
                } else {
                    int maximum = JDTEConfig.COMMON.transferFailureBackoffMax.get();
                    int start = Math.min(JDTEConfig.COMMON.transferFailureBackoffStart.get(), maximum);
                    pending.failureBackoff = !result.outputEnabled()
                            ? maximum
                            : pending.failureBackoff <= 0 ? start : Math.min(maximum, pending.failureBackoff * 2);
                    pending.nextAttemptTick = gameTime + pending.failureBackoff;
                }
                if (pending.slots.isEmpty()) machines.remove(machine);
            }
            if (machines.isEmpty()) LEVELS.remove(level);
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            IdentityHashMap<BaseMachineBE, PendingOutput> removed = LEVELS.remove(level);
            if (removed != null) removed.keySet().forEach(AutoIoTransferHelper::forgetEventDrivenOutput);
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        LEVELS.entrySet().removeIf(entry -> {
            if (entry.getKey().getServer() != event.getServer()) return false;
            entry.getValue().keySet().forEach(AutoIoTransferHelper::forgetEventDrivenOutput);
            return true;
        });
    }

    private static void pruneEmptySlots(BaseMachineBE machine, BitSet slots) {
        int handlerSlots = machine.getMachineHandler().getSlots();
        for (int slot = slots.nextSetBit(0); slot >= 0; slot = slots.nextSetBit(slot + 1)) {
            if (slot >= handlerSlots || machine.getMachineHandler().getStackInSlot(slot).isEmpty()) slots.clear(slot);
            if (slot == Integer.MAX_VALUE) break;
        }
    }

    private record OutputRange(int start, int count) {
    }

    private static final class PendingOutput {
        private final BitSet slots = new BitSet();
        private long nextAttemptTick;
        private int failureBackoff;
    }
}
