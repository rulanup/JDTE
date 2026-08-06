package com.jdte.common.minerals;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * Plans mineral output against an immutable inventory snapshot so capacity checks and commits use identical rules.
 */
public final class MineralOutputPlanner {
    private MineralOutputPlanner() {
    }

    public static long findMaxFitting(long requested, LongFunction<Plan> planFactory) {
        long low = 1L;
        long high = Math.max(0L, requested);
        long best = 0L;
        while (low <= high) {
            long candidate = low + (high - low) / 2L;
            if (planFactory.apply(candidate).fits()) {
                best = candidate;
                low = candidate + 1L;
            } else {
                high = candidate - 1L;
            }
        }
        return best;
    }

    public static Plan plan(List<SlotState> currentSlots, Map<ResourceLocation, Long> amounts,
                            Map<ResourceLocation, Integer> stackLimits) {
        List<SlotState> planned = new ArrayList<>(currentSlots);
        for (Map.Entry<ResourceLocation, Long> entry : amounts.entrySet()) {
            ResourceLocation itemId = entry.getKey();
            long remaining = Math.max(0L, entry.getValue());
            int itemLimit = Math.max(1, stackLimits.getOrDefault(itemId, 64));

            for (int slot = 0; slot < planned.size() && remaining > 0L; slot++) {
                SlotState state = planned.get(slot);
                if (!state.isEmpty() && state.itemId().equals(itemId)) {
                    int accepted = (int) Math.min(remaining, Math.max(0, state.limit() - state.count()));
                    if (accepted > 0) {
                        planned.set(slot, new SlotState(itemId, state.count() + accepted, state.limit()));
                        remaining -= accepted;
                    }
                }
            }
            for (int slot = 0; slot < planned.size() && remaining > 0L; slot++) {
                SlotState state = planned.get(slot);
                if (state.isEmpty()) {
                    int limit = Math.min(state.limit(), itemLimit);
                    int accepted = (int) Math.min(remaining, limit);
                    planned.set(slot, new SlotState(itemId, accepted, limit));
                    remaining -= accepted;
                }
            }
            if (remaining > 0L) return new Plan(false, List.copyOf(currentSlots));
        }
        return new Plan(true, List.copyOf(planned));
    }

    public record SlotState(ResourceLocation itemId, int count, int limit) {
        public SlotState {
            limit = Math.max(1, limit);
            count = Math.clamp(count, 0, limit);
            if (count == 0) itemId = null;
        }

        public static SlotState empty(int limit) {
            return new SlotState(null, 0, limit);
        }

        public boolean isEmpty() {
            return itemId == null || count == 0;
        }
    }

    public record Plan(boolean fits, List<SlotState> slots) {
    }
}