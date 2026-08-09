package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.autoioconfig.AutoIoTransferHelper;
import com.jdte.common.integrations.ae2.AEOutputNetwork;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class AEOutputManager {
    private static final int MAX_BACKOFF = 20;
    private static final int FLUID_TRANSFER_BUDGET = 64_000;
    private static final Map<BaseMachineBE, State> MACHINES =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private AEOutputManager() {
    }

    public static void refresh(BaseMachineBE machine) {
        if (machine == null) return;
        if (machine.getLevel() != null && !(machine.getLevel() instanceof ServerLevel)) {
            MACHINES.remove(machine);
            return;
        }
        // Attachment deserialization can call this before the handler has been fully attached.
        // Track first and let the post-tick validation prune machines without a usable card.
        MACHINES.computeIfAbsent(machine, ignored -> new State()).wake();
    }

    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ArrayList<Map.Entry<BaseMachineBE, State>> snapshot;
        synchronized (MACHINES) {
            snapshot = new ArrayList<>(MACHINES.entrySet());
        }
        for (Map.Entry<BaseMachineBE, State> entry : snapshot) {
            BaseMachineBE machine = entry.getKey();
            if (machine.isRemoved()) {
                MACHINES.remove(machine);
                continue;
            }
            if (machine.getLevel() != null && !(machine.getLevel() instanceof ServerLevel)) {
                MACHINES.remove(machine);
                continue;
            }
            if (!(machine.getLevel() instanceof ServerLevel level)) continue;
            if (level.getServer() != server) continue;
            if (!UpgradeHelper.hasAEOutputUpgrade(machine) || !AutoIoTransferHelper.supportsAEOutput(machine)) {
                MACHINES.remove(machine);
                continue;
            }
            State state = entry.getValue();
            if (level.getGameTime() < state.nextAttemptTick) continue;
            boolean moved = flush(level, machine);
            if (moved) {
                state.failureBackoff = 0;
                state.nextAttemptTick = level.getGameTime() + 1L;
                machine.setChanged();
            } else {
                state.failureBackoff = state.failureBackoff <= 0 ? 5 : Math.min(MAX_BACKOFF, state.failureBackoff * 2);
                state.nextAttemptTick = level.getGameTime() + state.failureBackoff;
            }
        }
    }

    public static void tickMatrix(GreenhouseMatrixControllerBE controller) {
        if (!(controller.getLevel() instanceof ServerLevel level) || !controller.isFormed()) return;
        ItemStack upgrade = controller.getAEOutputUpgrade();
        if (upgrade.isEmpty() || !AEOutputNetwork.isLinked(upgrade)) return;
        MatrixState state = controller.getAEOutputState();
        if (level.getGameTime() < state.nextAttemptTick) return;

        var greenhouses = controller.getGreenhouses();
        if (greenhouses.isEmpty()) return;
        List<AEOutputNetwork.ItemSource> sources = new ArrayList<>();
        for (var greenhousePos : greenhouses) {
            var blockEntity = level.getBlockEntity(greenhousePos);
            if (!(blockEntity instanceof BaseMachineBE machine)) continue;
            AutoIoTransferHelper.AEOutputRoutes routes = AutoIoTransferHelper.getAEOutputRoutes(machine);
            collectItemSources(machine, routes.itemSlots(), sources);
        }
        AEOutputNetwork.ItemTransferResult result = AEOutputNetwork.transferItems(level, upgrade, sources);
        for (BaseMachineBE changed : result.changedMachines()) changed.setChanged();
        if (result.moved() > 0L) {
            state.failureBackoff = 0;
            state.nextAttemptTick = level.getGameTime() + 1L;
            controller.setChanged();
        } else {
            state.failureBackoff = state.failureBackoff <= 0 ? 5 : Math.min(MAX_BACKOFF, state.failureBackoff * 2);
            state.nextAttemptTick = level.getGameTime() + state.failureBackoff;
        }
    }

    private static boolean flush(ServerLevel level, BaseMachineBE machine) {
        ItemStack upgrade = UpgradeHelper.getAEOutputUpgrade(machine);
        if (upgrade.isEmpty() || !AEOutputNetwork.isLinked(upgrade)) return false;
        AutoIoTransferHelper.AEOutputRoutes routes = AutoIoTransferHelper.getAEOutputRoutes(machine);
        long movedItems = flushItems(level, upgrade, machine, routes.itemSlots());
        int movedFluid = flushFluid(level, upgrade, routes.fluidOutput());
        return movedItems > 0 || movedFluid > 0;
    }

    private static long flushItems(ServerLevel level, ItemStack upgrade, BaseMachineBE machine, int[] slots) {
        List<AEOutputNetwork.ItemSource> sources = new ArrayList<>(slots.length);
        collectItemSources(machine, slots, sources);
        AEOutputNetwork.ItemTransferResult result = AEOutputNetwork.transferItems(level, upgrade, sources);
        for (BaseMachineBE changed : result.changedMachines()) changed.setChanged();
        return result.moved();
    }

    private static void collectItemSources(BaseMachineBE machine, int[] slots,
                                           List<AEOutputNetwork.ItemSource> target) {
        ItemStackHandler handler = machine.getMachineHandler();
        if (handler == null || slots.length == 0) return;
        for (int slot : slots) {
            if (slot < 0 || slot >= handler.getSlots() || handler.getStackInSlot(slot).isEmpty()) continue;
            target.add(new AEOutputNetwork.ItemSource(machine, handler, slot));
        }
    }

    private static int flushFluid(ServerLevel level, ItemStack upgrade, IFluidHandler source) {
        if (source == null) return 0;
        int budget = FLUID_TRANSFER_BUDGET;
        if (budget <= 0) return 0;
        FluidStack offered = source.drain(budget, IFluidHandler.FluidAction.SIMULATE);
        if (offered.isEmpty()) return 0;
        int accepted = AEOutputNetwork.insertFluid(level, upgrade, offered, true);
        if (accepted <= 0) return 0;
        FluidStack drained = source.drain(Math.min(accepted, offered.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return 0;
        int inserted = AEOutputNetwork.insertFluid(level, upgrade, drained, false);
        if (inserted < drained.getAmount()) {
            source.fill(drained.copyWithAmount(drained.getAmount() - Math.max(0, inserted)),
                    IFluidHandler.FluidAction.EXECUTE);
        }
        return Math.max(0, inserted);
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            synchronized (MACHINES) {
                MACHINES.keySet().removeIf(machine -> machine.getLevel() == level);
            }
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        synchronized (MACHINES) {
            MACHINES.keySet().removeIf(machine -> machine.getLevel() instanceof ServerLevel level
                    && level.getServer() == event.getServer());
        }
    }

    public static class State {
        int failureBackoff;
        long nextAttemptTick;

        private void wake() {
            failureBackoff = 0;
            nextAttemptTick = 0L;
        }
    }

    public static final class MatrixState extends State {
    }
}
