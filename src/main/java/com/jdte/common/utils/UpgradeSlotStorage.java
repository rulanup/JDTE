package com.jdte.common.utils;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;

import java.util.Map;
import java.util.WeakHashMap;

public class UpgradeSlotStorage {
    private static final Map<BaseMachineContainer, Integer> UPGRADE_SLOTS_MAP = new WeakHashMap<>();
    private static final Map<BaseMachineContainer, Integer> BASE_FILTER_SLOTS_MAP = new WeakHashMap<>();

    public static void setUpgradeSlots(BaseMachineContainer container, int slots) {
        UPGRADE_SLOTS_MAP.put(container, slots);
    }

    public static int getUpgradeSlots(BaseMachineContainer container) {
        return UPGRADE_SLOTS_MAP.getOrDefault(container, 0);
    }

    public static void setBaseFilterSlots(BaseMachineContainer container, int slots) {
        BASE_FILTER_SLOTS_MAP.put(container, slots);
    }

    public static int getBaseFilterSlots(BaseMachineContainer container) {
        return BASE_FILTER_SLOTS_MAP.getOrDefault(container, 0);
    }
}
