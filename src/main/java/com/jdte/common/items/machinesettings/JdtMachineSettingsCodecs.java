package com.jdte.common.items.machinesettings;

import com.jdte.common.utils.HandlerTagHelper;
import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockSwapperT1BE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.DropperT1BE;
import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.ExperienceHolderBE;
import com.direwolf20.justdirethings.common.blockentities.InventoryHolderBE;
import com.direwolf20.justdirethings.common.blockentities.ItemCollectorBE;
import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.PlayerAccessorBE;
import com.direwolf20.justdirethings.common.blockentities.SensorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

final class JdtMachineSettingsCodecs {
    private static final String CLICK_TYPE_KEY = "clickType";
    private static final String CLICK_TARGET_KEY = "clickTarget";
    private static final String SNEAKING_KEY = "sneaking";
    private static final String SHOW_FAKE_PLAYER_KEY = "showFakePlayer";
    private static final String MAX_HOLD_TICKS_KEY = "maxHoldTicks";
    private static final String DROP_COUNT_KEY = "dropCount";
    private static final String PICKUP_DELAY_KEY = "pickupDelay";
    private static final String SWAP_BLOCKS_KEY = "swapBlocks";
    private static final String SWAP_ENTITY_TYPE_KEY = "swapEntityType";
    private static final String SENSE_TARGET_KEY = "senseTarget";
    private static final String STRONG_SIGNAL_KEY = "strongSignal";
    private static final String SENSE_AMOUNT_KEY = "senseAmount";
    private static final String EQUALITY_KEY = "equality";
    private static final String SENSOR_BLOCK_STATE_PROPERTIES_KEY = "sensorBlockStateProperties";
    private static final String RESPECT_PICKUP_DELAY_KEY = "respectPickupDelay";
    private static final String SHOW_PARTICLES_KEY = "showParticles";
    private static final String TARGET_EXP_KEY = "targetExp";
    private static final String OWNER_ONLY_KEY = "ownerOnly";
    private static final String COLLECT_EXP_KEY = "collectExp";
    private static final String COMPARE_NBT_KEY = "compareNBT";
    private static final String FILTERS_ONLY_KEY = "filtersOnly";
    private static final String AUTOMATED_FILTERS_ONLY_KEY = "automatedFiltersOnly";
    private static final String COMPARE_COUNTS_KEY = "compareCounts";
    private static final String AUTOMATED_COMPARE_COUNTS_KEY = "automatedCompareCounts";
    private static final String RENDERED_SLOT_KEY = "renderedSlot";
    private static final String RENDER_PLAYER_KEY = "renderPlayer";
    private static final String FILTER_BASIC_HANDLER_KEY = "filterBasicHandler";
    private static final String FILTER_NBT_ITEMS_KEY = "Items";
    private static final String FILTER_NBT_SIZE_KEY = "Size";
    private static final String FILTER_NBT_SLOT_KEY = "Slot";
    private static final String SIDED_INVENTORY_TYPES_KEY = "sidedInventoryTypes";
    private static final String RENDER_PARADOX_KEY = "renderParadox";
    private static final String PARADOX_TARGET_TYPE_KEY = "paradoxTargetType";

    private static final int CLICK_TYPE_MIN = 0;
    private static final int CLICK_TYPE_MAX = 2;
    private static final int MAX_HOLD_TICKS_MIN = 1;
    private static final int MAX_HOLD_TICKS_MAX = 1200;
    private static final int DROP_COUNT_MIN = 1;
    private static final int DROP_COUNT_MAX = 64;
    private static final int PICKUP_DELAY_MIN = 0;
    private static final int PICKUP_DELAY_MAX = 1200;
    private static final int SENSE_AMOUNT_MIN = 0;
    private static final int SENSE_AMOUNT_MAX = 9999;
    private static final int EQUALITY_MIN = 0;
    private static final int EQUALITY_MAX = 2;
    private static final int SENSOR_FILTER_SLOT_MIN = 0;
    private static final int SENSOR_FILTER_SLOT_MAX = 35;
    private static final int TARGET_EXP_MIN = 0;
    private static final int TARGET_EXP_MAX = 1000;
    private static final int RENDERED_SLOT_MIN = 0;
    private static final int RENDERED_SLOT_MAX = 35;
    private static final int INVENTORY_HOLDER_FILTER_SLOT_COUNT = 41;
    private static final int PLAYER_ACCESSOR_TYPE_MIN = 0;
    private static final int PLAYER_ACCESSOR_TYPE_MAX = 2;
    private static final int PARADOX_TARGET_TYPE_MIN = 0;
    private static final int PARADOX_TARGET_TYPE_MAX = 2;

    private JdtMachineSettingsCodecs() {
    }

    static MachineSettingsCodec clicker() {
        return typedCodec(ClickerT1BE.class, (machine, ignoredRegistries, custom) -> {
            custom.putInt(CLICK_TYPE_KEY, machine.clickType);
            MachineSettingsCodecSupport.writeEnum(custom, CLICK_TARGET_KEY, machine.clickTarget);
            custom.putBoolean(SNEAKING_KEY, machine.sneaking);
            custom.putBoolean(SHOW_FAKE_PLAYER_KEY, machine.showFakePlayer);
            custom.putInt(MAX_HOLD_TICKS_KEY, machine.maxHoldTicks);
        }, (custom, registries) -> {
            Optional<ClickerT1BE.CLICK_TARGET> clickTarget = MachineSettingsCodecSupport.readEnum(
                    custom, CLICK_TARGET_KEY, ClickerT1BE.CLICK_TARGET.class);
            Optional<Boolean> sneaking = MachineSettingsCodecSupport.readBoolean(custom, SNEAKING_KEY);
            Optional<Boolean> showFakePlayer = MachineSettingsCodecSupport.readBoolean(custom, SHOW_FAKE_PLAYER_KEY);
            if (!(custom.get(CLICK_TYPE_KEY) instanceof net.minecraft.nbt.IntTag)
                    || !(custom.get(MAX_HOLD_TICKS_KEY) instanceof net.minecraft.nbt.IntTag)
                    || clickTarget.isEmpty()
                    || sneaking.isEmpty()
                    || showFakePlayer.isEmpty()) {
                return Optional.empty();
            }

            int clickType = custom.getIntOr(CLICK_TYPE_KEY, 0);
            int maxHoldTicks = custom.getIntOr(MAX_HOLD_TICKS_KEY, 0);
            if (!MachineSettingsCodecSupport.isInRange(clickType, CLICK_TYPE_MIN, CLICK_TYPE_MAX)
                    || !MachineSettingsCodecSupport.isInRange(maxHoldTicks, MAX_HOLD_TICKS_MIN, MAX_HOLD_TICKS_MAX)) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.clickType = clickType;
                machine.clickTarget = clickTarget.get();
                machine.sneaking = sneaking.get();
                machine.showFakePlayer = showFakePlayer.get();
                machine.maxHoldTicks = maxHoldTicks;
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec dropper() {
        return typedCodec(DropperT1BE.class, (machine, ignoredRegistries, custom) -> {
            custom.putInt(DROP_COUNT_KEY, machine.dropCount);
            custom.putInt(PICKUP_DELAY_KEY, machine.pickupDelay);
        }, (custom, registries) -> {
            if (!(custom.get(DROP_COUNT_KEY) instanceof net.minecraft.nbt.IntTag)
                    || !(custom.get(PICKUP_DELAY_KEY) instanceof net.minecraft.nbt.IntTag)) {
                return Optional.empty();
            }

            int dropCount = custom.getIntOr(DROP_COUNT_KEY, 0);
            int pickupDelay = custom.getIntOr(PICKUP_DELAY_KEY, 0);
            if (!MachineSettingsCodecSupport.isInRange(dropCount, DROP_COUNT_MIN, DROP_COUNT_MAX)
                    || !MachineSettingsCodecSupport.isInRange(pickupDelay, PICKUP_DELAY_MIN, PICKUP_DELAY_MAX)) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.dropCount = dropCount;
                machine.pickupDelay = pickupDelay;
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec blockBreaker() {
        return typedCodec(BlockBreakerT1BE.class, (machine, ignoredRegistries, custom) ->
                custom.putBoolean(SNEAKING_KEY, machine.sneaking), (custom, registries) -> {
            Optional<Boolean> sneaking = MachineSettingsCodecSupport.readBoolean(custom, SNEAKING_KEY);
            return sneaking.map(value -> machine -> {
                machine.sneaking = value;
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec blockSwapper() {
        return typedCodec(BlockSwapperT1BE.class, (machine, ignoredRegistries, custom) -> {
            custom.putBoolean(SWAP_BLOCKS_KEY, machine.swapBlocks);
            MachineSettingsCodecSupport.writeEnum(custom, SWAP_ENTITY_TYPE_KEY, machine.swap_entity_type);
        }, (custom, registries) -> {
            Optional<Boolean> swapBlocks = MachineSettingsCodecSupport.readBoolean(custom, SWAP_BLOCKS_KEY);
            Optional<BlockSwapperT1BE.SWAP_ENTITY_TYPE> entityType = MachineSettingsCodecSupport.readEnum(
                    custom, SWAP_ENTITY_TYPE_KEY, BlockSwapperT1BE.SWAP_ENTITY_TYPE.class);
            if (swapBlocks.isEmpty() || entityType.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.swapBlocks = swapBlocks.get();
                machine.swap_entity_type = entityType.get();
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec sensor() {
        return typedCodec(SensorT1BE.class, (machine, ignoredRegistries, custom) -> {
            MachineSettingsCodecSupport.writeEnum(custom, SENSE_TARGET_KEY, machine.sense_target);
            custom.putBoolean(STRONG_SIGNAL_KEY, machine.strongSignal);
            custom.putInt(SENSE_AMOUNT_KEY, machine.senseAmount);
            custom.putInt(EQUALITY_KEY, machine.equality);
            custom.put(SENSOR_BLOCK_STATE_PROPERTIES_KEY, machine.saveBlockStateProperties().copy());
        }, (custom, registries) -> {
            Optional<SensorT1BE.SENSE_TARGET> senseTarget = MachineSettingsCodecSupport.readEnum(
                    custom, SENSE_TARGET_KEY, SensorT1BE.SENSE_TARGET.class);
            Optional<Boolean> strongSignal = MachineSettingsCodecSupport.readBoolean(custom, STRONG_SIGNAL_KEY);
            if (!(custom.get(SENSE_AMOUNT_KEY) instanceof net.minecraft.nbt.IntTag)
                    || !(custom.get(EQUALITY_KEY) instanceof net.minecraft.nbt.IntTag)
                    || senseTarget.isEmpty()
                    || strongSignal.isEmpty()) {
                return Optional.empty();
            }

            int senseAmount = custom.getIntOr(SENSE_AMOUNT_KEY, 0);
            int equality = custom.getIntOr(EQUALITY_KEY, 0);
            if (!MachineSettingsCodecSupport.isInRange(senseAmount, SENSE_AMOUNT_MIN, SENSE_AMOUNT_MAX)
                    || !MachineSettingsCodecSupport.isInRange(equality, EQUALITY_MIN, EQUALITY_MAX)) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.sense_target = senseTarget.get();
                machine.strongSignal = strongSignal.get();
                machine.senseAmount = senseAmount;
                machine.equality = equality;
                machine.markDirtyClient();
            });
        }, (custom, registries) -> {
            if (!(custom.get(SENSOR_BLOCK_STATE_PROPERTIES_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
                return Optional.empty();
            }

            CompoundTag blockStateProperties = custom.getCompoundOrEmpty(SENSOR_BLOCK_STATE_PROPERTIES_KEY).copy();
            if (!isValidSensorBlockStateProperties(blockStateProperties)) {
                return Optional.empty();
            }
            return Optional.of(machine -> {
                machine.loadBlockStateProperties(blockStateProperties);
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec itemCollector() {
        return typedCodec(ItemCollectorBE.class, (machine, ignoredRegistries, custom) -> {
            custom.putBoolean(RESPECT_PICKUP_DELAY_KEY, machine.respectPickupDelay);
            custom.putBoolean(SHOW_PARTICLES_KEY, machine.showParticles);
        }, (custom, registries) -> {
            Optional<Boolean> respectPickupDelay = MachineSettingsCodecSupport.readBoolean(custom, RESPECT_PICKUP_DELAY_KEY);
            Optional<Boolean> showParticles = MachineSettingsCodecSupport.readBoolean(custom, SHOW_PARTICLES_KEY);
            if (respectPickupDelay.isEmpty() || showParticles.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.respectPickupDelay = respectPickupDelay.get();
                machine.showParticles = showParticles.get();
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec experienceHolder() {
        return typedCodec(ExperienceHolderBE.class, (machine, ignoredRegistries, custom) -> writeExperienceSettings(
                custom, machine.targetExp, machine.ownerOnly, machine.collectExp, machine.showParticles),
                (custom, registries) -> readExperienceSettings(custom).map(settings -> machine -> {
                    machine.targetExp = settings.targetExp();
                    machine.ownerOnly = settings.ownerOnly();
                    machine.collectExp = settings.collectExp();
                    machine.showParticles = settings.showParticles();
                    machine.markDirtyClient();
                }));
    }

    static MachineSettingsCodec energyTransmitter() {
        return typedCodec(EnergyTransmitterBE.class, (machine, ignoredRegistries, custom) ->
                custom.putBoolean(SHOW_PARTICLES_KEY, machine.showParticles), (custom, registries) -> {
            Optional<Boolean> showParticles = MachineSettingsCodecSupport.readBoolean(custom, SHOW_PARTICLES_KEY);
            return showParticles.map(value -> machine -> {
                machine.showParticles = value;
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec inventoryHolder() {
        return typedCodec(InventoryHolderBE.class, (machine, registries, custom) -> {
            custom.putBoolean(COMPARE_NBT_KEY, machine.compareNBT);
            custom.putBoolean(FILTERS_ONLY_KEY, machine.filtersOnly);
            custom.putBoolean(AUTOMATED_FILTERS_ONLY_KEY, machine.automatedFiltersOnly);
            custom.putBoolean(COMPARE_COUNTS_KEY, machine.compareCounts);
            custom.putBoolean(AUTOMATED_COMPARE_COUNTS_KEY, machine.automatedCompareCounts);
            custom.putInt(RENDERED_SLOT_KEY, machine.renderedSlot);
            custom.putBoolean(RENDER_PLAYER_KEY, machine.renderPlayer);
            custom.put(FILTER_BASIC_HANDLER_KEY, HandlerTagHelper.write(machine.filterBasicHandler, registries));
        }, (custom, registries) -> {
            Optional<Boolean> compareNbt = MachineSettingsCodecSupport.readBoolean(custom, COMPARE_NBT_KEY);
            Optional<Boolean> filtersOnly = MachineSettingsCodecSupport.readBoolean(custom, FILTERS_ONLY_KEY);
            Optional<Boolean> automatedFiltersOnly = MachineSettingsCodecSupport.readBoolean(custom, AUTOMATED_FILTERS_ONLY_KEY);
            Optional<Boolean> compareCounts = MachineSettingsCodecSupport.readBoolean(custom, COMPARE_COUNTS_KEY);
            Optional<Boolean> automatedCompareCounts = MachineSettingsCodecSupport.readBoolean(custom, AUTOMATED_COMPARE_COUNTS_KEY);
            Optional<Boolean> renderPlayer = MachineSettingsCodecSupport.readBoolean(custom, RENDER_PLAYER_KEY);
            if (!(custom.get(RENDERED_SLOT_KEY) instanceof net.minecraft.nbt.IntTag)
                    || compareNbt.isEmpty()
                    || filtersOnly.isEmpty()
                    || automatedFiltersOnly.isEmpty()
                    || compareCounts.isEmpty()
                    || automatedCompareCounts.isEmpty()
                    || renderPlayer.isEmpty()) {
                return Optional.empty();
            }

            int renderedSlot = custom.getIntOr(RENDERED_SLOT_KEY, 0);
            if (!MachineSettingsCodecSupport.isInRange(renderedSlot, RENDERED_SLOT_MIN, RENDERED_SLOT_MAX)) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.compareNBT = compareNbt.get();
                machine.filtersOnly = filtersOnly.get();
                machine.automatedFiltersOnly = automatedFiltersOnly.get();
                machine.compareCounts = compareCounts.get();
                machine.automatedCompareCounts = automatedCompareCounts.get();
                machine.renderedSlot = renderedSlot;
                machine.renderPlayer = renderPlayer.get();
                machine.markDirtyClient();
            });
        }, (custom, registries) -> {
            if (!(custom.get(FILTER_BASIC_HANDLER_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
                return Optional.empty();
            }

            var filterSettings = decodeInventoryHolderFilters(custom.getCompoundOrEmpty(FILTER_BASIC_HANDLER_KEY), registries);
            if (filterSettings.isEmpty()) return Optional.empty();
            return Optional.of(machine -> {
                HandlerTagHelper.read(machine.filterBasicHandler, registries, filterSettings.get());
                machine.rebuildFilterCache();
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec playerAccessor() {
        return typedCodec(PlayerAccessorBE.class, (machine, ignoredRegistries, custom) -> {
            CompoundTag sidedInventoryTypes = new CompoundTag();
            for (Direction direction : Direction.values()) {
                Integer type = machine.sidedInventoryTypes.get(direction);
                if (type != null) {
                    sidedInventoryTypes.putInt(direction.getSerializedName(), type);
                }
            }
            custom.put(SIDED_INVENTORY_TYPES_KEY, sidedInventoryTypes);
        }, (custom, registries) -> {
            if (!(custom.get(SIDED_INVENTORY_TYPES_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
                return Optional.empty();
            }

            CompoundTag sidedInventoryTypesTag = custom.getCompoundOrEmpty(SIDED_INVENTORY_TYPES_KEY);
            Map<Direction, Integer> sidedInventoryTypes = new EnumMap<>(Direction.class);
            for (String serializedDirection : sidedInventoryTypesTag.keySet()) {
                Direction direction = findDirection(serializedDirection);
                if (direction == null || !(sidedInventoryTypesTag.get(serializedDirection) instanceof net.minecraft.nbt.IntTag)) {
                    return Optional.empty();
                }

                int type = sidedInventoryTypesTag.getIntOr(serializedDirection, 0);
                if (!MachineSettingsCodecSupport.isInRange(type, PLAYER_ACCESSOR_TYPE_MIN, PLAYER_ACCESSOR_TYPE_MAX)) {
                    return Optional.empty();
                }
                sidedInventoryTypes.put(direction, type);
            }

            return Optional.of(machine -> {
                for (Map.Entry<Direction, Integer> entry : sidedInventoryTypes.entrySet()) {
                    machine.updateSidedInventory(entry.getKey(), entry.getValue());
                }
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec paradox() {
        return typedCodec(ParadoxMachineBE.class, (machine, ignoredRegistries, custom) -> {
            custom.putBoolean(RENDER_PARADOX_KEY, machine.renderParadox);
            custom.putInt(PARADOX_TARGET_TYPE_KEY, machine.targetType);
        }, (custom, registries) -> {
            Optional<Boolean> renderParadox = MachineSettingsCodecSupport.readBoolean(custom, RENDER_PARADOX_KEY);
            if (!(custom.get(PARADOX_TARGET_TYPE_KEY) instanceof net.minecraft.nbt.IntTag) || renderParadox.isEmpty()) {
                return Optional.empty();
            }

            int targetType = custom.getIntOr(PARADOX_TARGET_TYPE_KEY, 0);
            if (!MachineSettingsCodecSupport.isInRange(
                    targetType, PARADOX_TARGET_TYPE_MIN, PARADOX_TARGET_TYPE_MAX)) {
                return Optional.empty();
            }

            return Optional.of(machine -> {
                machine.renderParadox = renderParadox.get();
                machine.targetType = targetType;
                machine.markDirtyClient();
            });
        });
    }

    static MachineSettingsCodec extendedExperienceHolder() {
        return typedCodec(ExtendedExperienceHolderBE.class, (machine, ignoredRegistries, custom) -> writeExperienceSettings(
                custom, machine.targetExp, machine.ownerOnly, machine.collectExp, machine.showParticles),
                (custom, registries) -> readExperienceSettings(custom).map(settings -> machine -> {
                    machine.targetExp = settings.targetExp();
                    machine.ownerOnly = settings.ownerOnly();
                    machine.collectExp = settings.collectExp();
                    machine.showParticles = settings.showParticles();
                    machine.markDirtyClient();
                }));
    }

    private static void writeExperienceSettings(CompoundTag custom, int targetExp, boolean ownerOnly,
                                                boolean collectExp, boolean showParticles) {
        custom.putInt(TARGET_EXP_KEY, targetExp);
        custom.putBoolean(OWNER_ONLY_KEY, ownerOnly);
        custom.putBoolean(COLLECT_EXP_KEY, collectExp);
        custom.putBoolean(SHOW_PARTICLES_KEY, showParticles);
    }

    private static Optional<ExperienceSettings> readExperienceSettings(CompoundTag custom) {
        Optional<Boolean> ownerOnly = MachineSettingsCodecSupport.readBoolean(custom, OWNER_ONLY_KEY);
        Optional<Boolean> collectExp = MachineSettingsCodecSupport.readBoolean(custom, COLLECT_EXP_KEY);
        Optional<Boolean> showParticles = MachineSettingsCodecSupport.readBoolean(custom, SHOW_PARTICLES_KEY);
        if (!(custom.get(TARGET_EXP_KEY) instanceof net.minecraft.nbt.IntTag)
                || ownerOnly.isEmpty()
                || collectExp.isEmpty()
                || showParticles.isEmpty()) {
            return Optional.empty();
        }

        int targetExp = custom.getIntOr(TARGET_EXP_KEY, 0);
        if (!MachineSettingsCodecSupport.isInRange(targetExp, TARGET_EXP_MIN, TARGET_EXP_MAX)) {
            return Optional.empty();
        }
        return Optional.of(new ExperienceSettings(targetExp, ownerOnly.get(), collectExp.get(), showParticles.get()));
    }

    private static Direction findDirection(String serializedName) {
        for (Direction direction : Direction.values()) {
            if (direction.getSerializedName().equals(serializedName)) {
                return direction;
            }
        }
        return null;
    }

    private static Optional<CompoundTag> decodeInventoryHolderFilters(CompoundTag saved, HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        if (saved.get("stacks") instanceof ListTag stacks) {
            if (saved.contains(FILTER_NBT_SIZE_KEY) || saved.contains(FILTER_NBT_ITEMS_KEY)
                    || stacks.size() != INVENTORY_HOLDER_FILTER_SLOT_COUNT) return Optional.empty();
            for (Tag entry : stacks) {
                if (ItemStack.OPTIONAL_CODEC.parse(ops, entry).error().isPresent()) return Optional.empty();
            }
            return Optional.of(saved.copy());
        }
        if (!isValidInventoryHolderFilterSettings(saved, registries)) return Optional.empty();
        var stacks = new ListTag();
        for (int i = 0; i < INVENTORY_HOLDER_FILTER_SLOT_COUNT; i++) stacks.add(new CompoundTag());
        for (Tag entry : saved.getListOrEmpty(FILTER_NBT_ITEMS_KEY)) {
            CompoundTag stack = ((CompoundTag) entry).copy();
            int slot = stack.getIntOr(FILTER_NBT_SLOT_KEY, -1);
            stack.remove(FILTER_NBT_SLOT_KEY);
            stacks.set(slot, stack);
        }
        CompoundTag result = new CompoundTag();
        result.put("stacks", stacks);
        return Optional.of(result);
    }

    private static boolean isValidInventoryHolderFilterSettings(CompoundTag filterSettings,
                                                                 HolderLookup.Provider registries) {
        if (!(filterSettings.get(FILTER_NBT_SIZE_KEY) instanceof net.minecraft.nbt.IntTag)
                || filterSettings.getIntOr(FILTER_NBT_SIZE_KEY, -1) != INVENTORY_HOLDER_FILTER_SLOT_COUNT
                || !(filterSettings.get(FILTER_NBT_ITEMS_KEY) instanceof net.minecraft.nbt.ListTag)) {
            return false;
        }

        Tag rawItems = filterSettings.get(FILTER_NBT_ITEMS_KEY);
        if (!(rawItems instanceof ListTag items)) {
            return false;
        }

        Set<Integer> usedSlots = new HashSet<>();
        for (Tag rawEntry : items) {
            if (!(rawEntry instanceof CompoundTag itemTag)
                    || !(itemTag.get(FILTER_NBT_SLOT_KEY) instanceof net.minecraft.nbt.IntTag)) {
                return false;
            }

            int slot = itemTag.getIntOr(FILTER_NBT_SLOT_KEY, 0);
            if (!MachineSettingsCodecSupport.isInRange(
                    slot, 0, INVENTORY_HOLDER_FILTER_SLOT_COUNT - 1) || !usedSlots.add(slot)) {
                return false;
            }

            Optional<ItemStack> parsedStack = ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemTag).result();
            if (parsedStack.isEmpty() || parsedStack.get().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidSensorBlockStateProperties(CompoundTag blockStateProperties) {
        for (String slotKey : blockStateProperties.keySet()) {
            final int slot;
            try {
                slot = Integer.parseInt(slotKey);
            } catch (NumberFormatException ignored) {
                return false;
            }
            if (!MachineSettingsCodecSupport.isInRange(slot, SENSOR_FILTER_SLOT_MIN, SENSOR_FILTER_SLOT_MAX)
                    || !(blockStateProperties.get(slotKey) instanceof net.minecraft.nbt.ListTag)) {
                return false;
            }
        }
        return true;
    }

    private static <T extends BaseMachineBE> MachineSettingsCodec typedCodec(Class<T> machineType,
                                                                              Encoder<T> encoder,
                                                                              BiFunction<CompoundTag, HolderLookup.Provider,
                                                                                      Optional<Consumer<T>>> decoder) {
        return typedCodec(machineType, encoder, decoder, (custom, registries) -> Optional.of(machine -> {
        }));
    }

    private static <T extends BaseMachineBE> MachineSettingsCodec typedCodec(Class<T> machineType,
                                                                              Encoder<T> encoder,
                                                                              BiFunction<CompoundTag, HolderLookup.Provider,
                                                                                      Optional<Consumer<T>>> decoder,
                                                                              BiFunction<CompoundTag, HolderLookup.Provider,
                                                                                      Optional<Consumer<T>>> filterDecoder) {
        return new MachineSettingsCodec() {
            @Override
            public CompoundTag encode(BaseMachineBE machine, HolderLookup.Provider registries) {
                CompoundTag custom = MachineSettingsCodecRegistry.common().encode(machine, registries);
                if (machineType.isInstance(machine)) {
                    encoder.encode(machineType.cast(machine), registries, custom);
                }
                return custom;
            }

            @Override
            public Optional<PreparedSettings> decode(CompoundTag custom, HolderLookup.Provider registries) {
                Optional<PreparedSettings> commonSettings = MachineSettingsCodecRegistry.common().decode(custom, registries);
                Optional<Consumer<T>> machineSettings = decoder.apply(custom, registries);
                Optional<Consumer<T>> filterSettings = filterDecoder.apply(custom, registries);
                if (commonSettings.isEmpty() || machineSettings.isEmpty() || filterSettings.isEmpty()) {
                    return Optional.empty();
                }

                return Optional.of(new PreparedSettings() {
                    @Override
                    public void apply(BaseMachineBE machine) {
                        if (!machineType.isInstance(machine)) {
                            return;
                        }
                        commonSettings.get().apply(machine);
                        machineSettings.get().accept(machineType.cast(machine));
                    }

                    @Override
                    public void applyFilterSettings(BaseMachineBE machine) {
                        if (!machineType.isInstance(machine)) {
                            return;
                        }
                        filterSettings.get().accept(machineType.cast(machine));
                    }
                });
            }
        };
    }

    private record ExperienceSettings(int targetExp, boolean ownerOnly, boolean collectExp, boolean showParticles) {
    }

    @FunctionalInterface
    private interface Encoder<T extends BaseMachineBE> {
        void encode(T machine, HolderLookup.Provider registries, CompoundTag custom);
    }
}
