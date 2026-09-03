package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.ExtendedUpgradeMachine;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.items.AdvancedMachineSettingsCopierData;
import com.jdte.common.upgrades.AdvancedPotionBrewerUpgradeItemStackHandler;
import com.jdte.common.upgrades.BioFactoryUpgradeItemStackHandler;
import com.jdte.common.upgrades.ExtendedUpgradeItemStackHandler;
import com.jdte.common.upgrades.LootFabricatorUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serializes every real upgrade handler on a machine and prepares replacement contents before any handler changes.
 */
public final class MachineUpgradeHandlers {
    public static final String STANDARD_HANDLER = "standard";
    public static final String LOOTING_HANDLER = "lootingHandler";
    public static final String SHARPNESS_HANDLER = "sharpnessHandler";

    private MachineUpgradeHandlers() {
    }

    public record NamedHandler(String name, ItemStackHandler handler) {
        public NamedHandler {
            if (name == null || name.isBlank() || handler == null) {
                throw new IllegalArgumentException("Upgrade handler name and value are required");
            }
        }
    }

    public static List<NamedHandler> all(BaseMachineBE machine) {
        List<NamedHandler> handlers = new ArrayList<>();
        UpgradeItemStackHandler standard = UpgradeHelper.getUpgradeHandler(machine);
        if (standard != null) {
            handlers.add(new NamedHandler(STANDARD_HANDLER, standard));
        }
        if (machine instanceof BioCrusherBE crusher) {
            handlers.add(new NamedHandler(LOOTING_HANDLER, crusher.getLootingHandler()));
            handlers.add(new NamedHandler(SHARPNESS_HANDLER, crusher.getSharpnessHandler()));
        }
        return List.copyOf(handlers);
    }

    /**
     * Writes both the legacy standard-handler node and the versioned named handler collection.
     */
    public static void write(CompoundTag copiedData, BaseMachineBE machine, HolderLookup.Provider registries) {
        if (copiedData == null || machine == null || registries == null) {
            return;
        }

        List<NamedHandler> handlers = all(machine);
        NamedHandler standard = handlerByName(handlers, STANDARD_HANDLER);
        if (standard == null) {
            AdvancedMachineSettingsCopierData.clearUpgrades(copiedData);
        } else {
            AdvancedMachineSettingsCopierData.writeUpgrades(copiedData,
                    standard.handler().serializeNBT(registries));
        }

        if (handlers.isEmpty()) {
            AdvancedMachineSettingsCopierData.clearUpgradeHandlers(copiedData);
            return;
        }

        CompoundTag encodedHandlers = new CompoundTag();
        for (NamedHandler handler : handlers) {
            encodedHandlers.put(handler.name(), handler.handler().serializeNBT(registries));
        }
        AdvancedMachineSettingsCopierData.writeUpgradeHandlers(copiedData, encodedHandlers);
    }

    /**
     * Decodes and validates upgrade contents without mutating the target. New named data takes precedence over legacy
     * standard-only data.
     */
    public static Optional<PreparedHandlers> prepare(CompoundTag copiedData, BaseMachineBE machine,
                                                      HolderLookup.Provider registries) {
        if (copiedData == null || machine == null || registries == null) {
            return Optional.empty();
        }

        List<NamedHandler> targetHandlers = all(machine);
        Optional<CompoundTag> named = AdvancedMachineSettingsCopierData.readUpgradeHandlers(copiedData);
        if (named.isPresent()) {
            return prepareNamed(named.get(), machine, targetHandlers, registries);
        }

        Optional<CompoundTag> legacy = AdvancedMachineSettingsCopierData.readUpgrades(copiedData);
        if (legacy.isEmpty()) {
            return Optional.of(new PreparedHandlers(List.of(), registries));
        }

        NamedHandler standard = handlerByName(targetHandlers, STANDARD_HANDLER);
        if (standard == null) {
            return Optional.empty();
        }
        return readHandler(STANDARD_HANDLER, legacy.get(), machine, standard.handler(), registries)
                .map(handler -> new PreparedHandlers(List.of(handler), registries));
    }

    private static Optional<PreparedHandlers> prepareNamed(CompoundTag encodedHandlers, BaseMachineBE machine,
                                                            List<NamedHandler> targetHandlers,
                                                            HolderLookup.Provider registries) {
        if (encodedHandlers.getAllKeys().size() != targetHandlers.size()) {
            return Optional.empty();
        }

        List<NamedHandler> decoded = new ArrayList<>(targetHandlers.size());
        for (NamedHandler target : targetHandlers) {
            if (!encodedHandlers.contains(target.name(), Tag.TAG_COMPOUND)) {
                return Optional.empty();
            }
            Optional<NamedHandler> handler = readHandler(target.name(), encodedHandlers.getCompound(target.name()),
                    machine, target.handler(), registries);
            if (handler.isEmpty()) {
                return Optional.empty();
            }
            decoded.add(handler.get());
        }
        return Optional.of(new PreparedHandlers(decoded, registries));
    }

    private static Optional<NamedHandler> readHandler(String name, CompoundTag encoded, BaseMachineBE machine,
                                                       ItemStackHandler target, HolderLookup.Provider registries) {
        if (!encoded.contains("Size", Tag.TAG_INT) || encoded.getInt("Size") != target.getSlots()
                || !encoded.contains("Items", Tag.TAG_LIST)) {
            return Optional.empty();
        }

        ItemStackHandler decoded = new ItemStackHandler(target.getSlots());
        try {
            decoded.deserializeNBT(registries, encoded);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
        if (decoded.getSlots() != target.getSlots()
                || !decoded.serializeNBT(registries).equals(encoded)
                || !hasValidContents(name, machine, target, decoded)) {
            return Optional.empty();
        }
        return Optional.of(new NamedHandler(name, decoded));
    }

    private static boolean hasValidContents(String name, BaseMachineBE machine, ItemStackHandler target,
                                            ItemStackHandler decoded) {
        if (STANDARD_HANDLER.equals(name)) {
            return hasValidStandardContents(machine, target, decoded);
        }

        int total = 0;
        for (int slot = 0; slot < decoded.getSlots(); slot++) {
            ItemStack stack = decoded.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getCount() < 1 || stack.getCount() > target.getSlotLimit(slot)
                    || !target.isItemValid(slot, stack)) {
                return false;
            }
            total += stack.getCount();
        }
        return total <= target.getSlots();
    }

    private static boolean hasValidStandardContents(BaseMachineBE machine, ItemStackHandler target,
                                                    ItemStackHandler decoded) {
        if (!(target instanceof UpgradeItemStackHandler)) {
            return false;
        }
        UpgradeItemStackHandler validation = validationHandler(machine);
        if (validation.getSlots() != target.getSlots()) {
            return false;
        }

        for (int slot = 0; slot < decoded.getSlots(); slot++) {
            ItemStack stack = decoded.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getCount() != 1 || !validation.isItemValid(slot, stack)) {
                return false;
            }
            validation.setStackInSlot(slot, stack.copy());
            ItemStack accepted = validation.getStackInSlot(slot);
            if (!ItemStack.isSameItemSameComponents(accepted, stack) || accepted.getCount() != 1) {
                return false;
            }
        }
        return true;
    }

    private static UpgradeItemStackHandler validationHandler(BaseMachineBE machine) {
        if (machine instanceof AdvancedPotionBrewerBE brewer) {
            return new AdvancedPotionBrewerUpgradeItemStackHandler(brewer);
        }
        if (machine instanceof BioFactoryBE factory) {
            return new BioFactoryUpgradeItemStackHandler(factory);
        }
        if (machine instanceof LootFabricatorBE fabricator) {
            return new LootFabricatorUpgradeItemStackHandler(fabricator);
        }
        if (machine instanceof ExtendedUpgradeMachine) {
            return new ExtendedUpgradeItemStackHandler(machine);
        }
        return new UpgradeItemStackHandler(machine);
    }

    private static NamedHandler handlerByName(List<NamedHandler> handlers, String name) {
        for (NamedHandler handler : handlers) {
            if (handler.name().equals(name)) {
                return handler;
            }
        }
        return null;
    }

    public static final class PreparedHandlers {
        private final List<HandlerContents> contents;
        private final List<ItemStack> requiredUpgrades;
        private final HolderLookup.Provider registries;

        private PreparedHandlers(List<NamedHandler> handlers, HolderLookup.Provider registries) {
            List<HandlerContents> copiedContents = new ArrayList<>(handlers.size());
            List<ItemStack> required = new ArrayList<>();
            for (NamedHandler handler : handlers) {
                List<ItemStack> stacks = copyStacks(handler.handler());
                copiedContents.add(new HandlerContents(handler.name(), handler.handler().serializeNBT(registries), stacks));
                for (ItemStack stack : stacks) {
                    for (int count = 0; count < stack.getCount(); count++) {
                        required.add(stack.copyWithCount(1));
                    }
                }
            }
            contents = List.copyOf(copiedContents);
            requiredUpgrades = copyStacks(required);
            this.registries = registries;
        }

        public List<ItemStack> requiredUpgrades() {
            return copyStacks(requiredUpgrades);
        }

        /**
         * Replaces only the handlers included by the snapshot. Legacy data consequently leaves dedicated handlers
         * untouched. Any unexpected application failure restores the previous handler contents.
         */
        public void apply(BaseMachineBE machine) {
            Map<String, ItemStackHandler> targets = new LinkedHashMap<>();
            for (NamedHandler handler : all(machine)) {
                targets.put(handler.name(), handler.handler());
            }

            List<HandlerContents> previous = new ArrayList<>(contents.size());
            for (HandlerContents content : contents) {
                ItemStackHandler target = targets.get(content.name());
                if (target == null || target.getSlots() != content.stacks().size()) {
                    throw new IllegalArgumentException("Target machine does not expose upgrade handler " + content.name());
                }
                previous.add(new HandlerContents(content.name(), target.serializeNBT(registries), copyStacks(target)));
            }

            try {
                for (HandlerContents content : contents) {
                    replaceContents(targets.get(content.name()), content);
                }
                UpgradeHelper.syncCapacities(machine);
            } catch (RuntimeException failure) {
                for (HandlerContents content : previous) {
                    try {
                        replaceContents(targets.get(content.name()), content);
                    } catch (RuntimeException rollbackFailure) {
                        failure.addSuppressed(rollbackFailure);
                    }
                }
                UpgradeHelper.syncCapacities(machine);
                throw failure;
            }
        }

        private void replaceContents(ItemStackHandler target, HandlerContents content) {
            target.deserializeNBT(registries, content.serialized());
            if (target.getSlots() != content.stacks().size()) {
                throw new IllegalStateException("Copied upgrade handler changed its slot count");
            }
            for (int slot = 0; slot < content.stacks().size(); slot++) {
                ItemStack expected = content.stacks().get(slot);
                ItemStack applied = target.getStackInSlot(slot);
                if (expected.isEmpty() ? !applied.isEmpty()
                        : !ItemStack.isSameItemSameComponents(expected, applied)
                        || expected.getCount() != applied.getCount()) {
                    throw new IllegalStateException("Rejected copied upgrade in slot " + slot);
                }
            }
        }

        private record HandlerContents(String name, CompoundTag serialized, List<ItemStack> stacks) {
            private HandlerContents {
                serialized = serialized.copy();
                stacks = copyStacks(stacks);
            }
        }
    }

    private static List<ItemStack> copyStacks(ItemStackHandler handler) {
        List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            stacks.add(handler.getStackInSlot(slot).copy());
        }
        return List.copyOf(stacks);
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).toList();
    }
}
