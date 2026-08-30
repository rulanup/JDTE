package com.jdte.common.blockentities;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/** Shared pending-work queue used by time accelerators and Ultimate Time Wands. */
final class TimeAccelerationWorkQueue<S, T> {
    private final Map<T, PendingTarget<S>> pending = new LinkedHashMap<>();
    private final ArrayDeque<T> queue = new ArrayDeque<>();
    private final Set<T> queued = new LinkedHashSet<>();

    boolean hasWork() {
        return !pending.isEmpty();
    }

    long highestPendingTicks(Set<T> targets) {
        long highest = 0L;
        for (T target : targets) {
            highest = Math.max(highest, pendingTicks(target));
        }
        return highest;
    }

    long pendingTicks(T target) {
        PendingTarget<S> work = pending.get(target);
        return work == null ? 0L : work.virtualTicks;
    }

    void retainContributors(BiPredicate<T, S> keepContributor) {
        boolean removed = false;
        var iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<T, PendingTarget<S>> entry = iterator.next();
            entry.getValue().retainContributors(source -> keepContributor.test(entry.getKey(), source));
            if (entry.getValue().virtualTicks <= 0) {
                iterator.remove();
                removed = true;
            }
        }
        if (removed) {
            queue.removeIf(target -> !pending.containsKey(target));
            queued.retainAll(pending.keySet());
        }
    }

    void enqueue(T target, S source, int workTicks, int displayMultiplier, long maxPending) {
        PendingTarget<S> work = pending.get(target);
        long currentTicks = work == null ? 0L : work.virtualTicks;
        long available = currentTicks >= maxPending ? 0L : maxPending - currentTicks;
        if (workTicks <= 0 || (long) workTicks > available) {
            throw new IllegalStateException("Accepted Time Accelerator work exceeds target capacity");
        }
        if (work == null) {
            work = new PendingTarget<>();
            pending.put(target, work);
            addToQueue(target);
        }
        work.add(source, workTicks, displayMultiplier);
    }

    long execute(long maxExecutions, int batchSize, Executor<T> executor, ExecutionListener<T> listener) {
        return execute(maxExecutions, batchSize, (target, source) -> true, executor, listener);
    }

    long execute(long maxExecutions, int batchSize, BiPredicate<T, S> keepContributor,
                 Executor<T> executor, ExecutionListener<T> listener) {
        long executedThisTick = 0L;
        while (!queue.isEmpty() && executedThisTick < maxExecutions) {
            T target = queue.removeFirst();
            queued.remove(target);
            PendingTarget<S> work = pending.get(target);
            if (work == null) {
                continue;
            }
            work.retainContributors(source -> keepContributor.test(target, source));
            if (work.virtualTicks <= 0) {
                pending.remove(target);
                continue;
            }
            long remainingBudget = maxExecutions - executedThisTick;
            int requested = TimeAcceleratorExecutionPolicy.requestedTicks(
                    work.virtualTicks, batchSize, remainingBudget);
            ExecutionResult result = executor.execute(target, requested, remainingBudget);
            if (!result.valid()) {
                pending.remove(target);
                continue;
            }
            if (result.executed() <= 0) {
                addToQueue(target);
                break;
            }

            executedThisTick += result.executed();
            int displayMultiplier = work.displayMultiplier();
            work.consume(result.executed());
            listener.onExecuted(target, result, displayMultiplier);
            if (result.idle() || work.virtualTicks <= 0) {
                pending.remove(target);
            } else {
                addToQueue(target);
            }
        }
        return executedThisTick;
    }

    private void addToQueue(T target) {
        if (queued.add(target)) {
            queue.addLast(target);
        }
    }

    private static int saturatingAdd(int left, int right) {
        long value = (long) left + right;
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    record ExecutionResult(int executed, boolean valid, boolean idle) {
    }

    @FunctionalInterface
    interface Executor<T> {
        ExecutionResult execute(T target, int requestedTicks, long remainingBudget);
    }

    @FunctionalInterface
    interface ExecutionListener<T> {
        void onExecuted(T target, ExecutionResult result, int displayMultiplier);
    }

    private static final class PendingTarget<S> {
        private long virtualTicks;
        private final Map<S, Contribution> contributions = new IdentityHashMap<>();

        private void add(S source, long ticks, int multiplier) {
            Contribution contribution = contributions.computeIfAbsent(source, ignored -> new Contribution());
            contribution.virtualTicks += ticks;
            contribution.multiplier = multiplier;
            virtualTicks += ticks;
        }

        private void retainContributors(java.util.function.Predicate<S> keepContributor) {
            var iterator = contributions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<S, Contribution> entry = iterator.next();
                if (!keepContributor.test(entry.getKey())) {
                    virtualTicks -= entry.getValue().virtualTicks;
                    iterator.remove();
                }
            }
        }

        private void consume(long ticks) {
            long remaining = ticks;
            var iterator = contributions.entrySet().iterator();
            while (iterator.hasNext() && remaining > 0) {
                Contribution contribution = iterator.next().getValue();
                long consumed = Math.min(remaining, contribution.virtualTicks);
                contribution.virtualTicks -= consumed;
                virtualTicks -= consumed;
                remaining -= consumed;
                if (contribution.virtualTicks <= 0) {
                    iterator.remove();
                }
            }
        }

        private int displayMultiplier() {
            int total = 0;
            for (Contribution contribution : contributions.values()) {
                total = saturatingAdd(total, contribution.multiplier);
            }
            return total;
        }
    }

    private static final class Contribution {
        private long virtualTicks;
        private int multiplier;
    }
}
