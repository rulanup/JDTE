package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.ExtendedTimeAcceleratorBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

/**
 * Explicit codecs for JDTE-owned machine configuration. Runtime resources, work queues and caches are deliberately
 * absent from every payload in this class.
 */
final class JdteMachineSettingsCodecs {
    private static final String MULTIPLIER_KEY = "multiplier";
    private static final String MODE_KEY = "mode";
    private static final String TARGET_KEY = "target";
    private static final String BLACKLIST_KEY = "blacklist";
    private static final String TIME_FREEZE_ENABLED_KEY = "timeFreezeEnabled";
    private static final String WEATHER_FREEZE_ENABLED_KEY = "weatherFreezeEnabled";
    private static final String AUTO_BALANCE_INPUTS_KEY = "autoBalanceInputs";
    private static final String SHOW_PARTICLES_KEY = "showParticles";
    private static final String HAS_BOUND_PLAYER_KEY = "hasBoundPlayer";
    private static final String BOUND_PLAYER_ID_KEY = "boundPlayerId";
    private static final String BOUND_PLAYER_NAME_KEY = "boundPlayerName";
    private static final String RECIPE_LOCKED_KEY = "recipeLocked";
    private static final String FUEL_INPUT_ENABLED_KEY = "fuelInputEnabled";
    private static final String RECIPE_LOCK_TEMPLATES_KEY = "recipeLockTemplates";
    private static final String TEMPLATE_SIZE_KEY = "size";
    private static final String TEMPLATE_SLOT_PREFIX = "slot_";

    private static final int MAX_BOUND_PLAYER_NAME_LENGTH = 64;

    private JdteMachineSettingsCodecs() {
    }

    static MachineSettingsCodec advancedTimeAccelerator() {
        return multiplier(AdvancedTimeAcceleratorBE.class, AdvancedTimeAcceleratorBE::getMultiplier,
                AdvancedTimeAcceleratorBE::setMultiplier,
                () -> JDTEConfig.COMMON.advancedTimeAcceleratorMaxMultiplier.get());
    }

    static MachineSettingsCodec extendedTimeAccelerator() {
        return multiplier(ExtendedTimeAcceleratorBE.class, ExtendedTimeAcceleratorBE::getMultiplier,
                ExtendedTimeAcceleratorBE::setMultiplier,
                () -> JDTEConfig.COMMON.extendedTimeAcceleratorMaxMultiplier.get());
    }

    static MachineSettingsCodec crystalIncubator() {
        return multiplier(CrystalIncubatorBE.class, CrystalIncubatorBE::getMultiplier,
                CrystalIncubatorBE::setMultiplier,
                () -> JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get());
    }

    static MachineSettingsCodec greenhouse() {
        return multiplier(GreenhouseBE.class, GreenhouseBE::getMultiplier, GreenhouseBE::setMultiplier,
                () -> maximum(JDTEConfig.COMMON.greenhouseMaxSpeedMultiplier.get(),
                        JDTEConfig.COMMON.greenhouseOverclockMaxSpeedMultiplier.get()));
    }

    static MachineSettingsCodec largeGreenhouse() {
        return multiplier(LargeGreenhouseBE.class, LargeGreenhouseBE::getMultiplier, LargeGreenhouseBE::setMultiplier,
                () -> maximum(JDTEConfig.COMMON.greenhouseMaxSpeedMultiplier.get(),
                        JDTEConfig.COMMON.greenhouseOverclockMaxSpeedMultiplier.get()));
    }

    static MachineSettingsCodec bioFactory() {
        return multiplier(BioFactoryBE.class, BioFactoryBE::getMultiplier, BioFactoryBE::setMultiplier,
                () -> maximum(JDTEConfig.COMMON.bioFactoryMaxSpeedMultiplier.get(),
                        JDTEConfig.COMMON.bioFactoryOverclockMaxSpeedMultiplier.get()));
    }

    static MachineSettingsCodec lifeBreeder() {
        return typedCodec(LifeBreederBE.class, (machine, ignoredRegistries, custom) -> {
            custom.putInt(MULTIPLIER_KEY, machine.getMultiplier());
            MachineSettingsCodecSupport.writeEnum(custom, MODE_KEY, machine.getMode());
        }, (custom, registries) -> {
            Optional<Integer> multiplier = readMultiplier(custom,
                    JDTEConfig.COMMON.lifeBreederMaxSpeedMultiplier.get());
            Optional<LifeBreederBE.Mode> mode = MachineSettingsCodecSupport.readEnum(
                    custom, MODE_KEY, LifeBreederBE.Mode.class);
            if (multiplier.isEmpty() || mode.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(machine -> {
                machine.setMultiplier(multiplier.get());
                machine.setMode(mode.get().ordinal());
            });
        });
    }

    static MachineSettingsCodec lifeSynthesisVat() {
        return multiplier(LifeSynthesisVatBE.class, LifeSynthesisVatBE::getMultiplier,
                LifeSynthesisVatBE::setMultiplier,
                () -> maximum(JDTEConfig.COMMON.lifeSynthesisVat.maxSpeedMultiplier.get(),
                        JDTEConfig.COMMON.lifeSynthesisVat.overclockMaxSpeedMultiplier.get()));
    }

    static MachineSettingsCodec mineralExtractor() {
        return multiplier(MineralExtractorBE.class, MineralExtractorBE::getMultiplier,
                MineralExtractorBE::setMultiplier,
                () -> maximum(JDTEConfig.COMMON.mineralExtractor.maxMultiplier.get(),
                        JDTEConfig.COMMON.mineralExtractor.overclockMaxMultiplier.get()));
    }

    static MachineSettingsCodec lifeExtractor() {
        return indexedMode(LifeExtractorBE.class, LifeExtractorBE::getMode, LifeExtractorBE::setMode,
                LifeExtractorBE.MODE_HOSTILE, LifeExtractorBE.MODE_ALL);
    }

    static MachineSettingsCodec bioCrusher() {
        return indexedMode(BioCrusherBE.class, BioCrusherBE::getMode, BioCrusherBE::setMode,
                BioCrusherBE.MODE_HOSTILE, BioCrusherBE.MODE_ALL);
    }

    static MachineSettingsCodec entitySuppressor() {
        return typedCodec(EntitySuppressorBE.class, (machine, ignoredRegistries, custom) -> {
            MachineSettingsCodecSupport.writeEnum(custom, MODE_KEY, machine.getMode());
            MachineSettingsCodecSupport.writeEnum(custom, TARGET_KEY, machine.getTarget());
            custom.putBoolean(BLACKLIST_KEY, machine.isBlacklist());
        }, (custom, registries) -> {
            Optional<EntitySuppressorBE.Mode> mode = MachineSettingsCodecSupport.readEnum(
                    custom, MODE_KEY, EntitySuppressorBE.Mode.class);
            Optional<EntitySuppressorBE.Target> target = MachineSettingsCodecSupport.readEnum(
                    custom, TARGET_KEY, EntitySuppressorBE.Target.class);
            Optional<Boolean> blacklist = MachineSettingsCodecSupport.readBoolean(custom, BLACKLIST_KEY);
            if (mode.isEmpty() || target.isEmpty() || blacklist.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(machine -> machine.setSettings(
                    mode.get().ordinal(), target.get().ordinal(), blacklist.get()));
        });
    }

    static MachineSettingsCodec rangeBlocker() {
        return typedCodec(RangeBlockerBE.class, (machine, ignoredRegistries, custom) -> {
            MachineSettingsCodecSupport.writeEnum(custom, MODE_KEY, machine.getMode());
            MachineSettingsCodecSupport.writeEnum(custom, TARGET_KEY, machine.getTarget());
            custom.putBoolean(BLACKLIST_KEY, machine.isBlacklist());
        }, (custom, registries) -> {
            Optional<RangeBlockerBE.Mode> mode = MachineSettingsCodecSupport.readEnum(
                    custom, MODE_KEY, RangeBlockerBE.Mode.class);
            Optional<EntitySuppressorBE.Target> target = MachineSettingsCodecSupport.readEnum(
                    custom, TARGET_KEY, EntitySuppressorBE.Target.class);
            Optional<Boolean> blacklist = MachineSettingsCodecSupport.readBoolean(custom, BLACKLIST_KEY);
            if (mode.isEmpty() || target.isEmpty() || blacklist.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(machine -> machine.setSettings(
                    mode.get().ordinal(), target.get().ordinal(), blacklist.get()));
        });
    }

    static MachineSettingsCodec timeFreezer() {
        return typedCodec(TimeFreezerBE.class, (machine, ignoredRegistries, custom) -> {
            custom.putBoolean(TIME_FREEZE_ENABLED_KEY, machine.isTimeFreezeEnabled());
            custom.putBoolean(WEATHER_FREEZE_ENABLED_KEY, machine.isWeatherFreezeEnabled());
        }, (custom, registries) -> {
            Optional<Boolean> timeFreezeEnabled = MachineSettingsCodecSupport.readBoolean(
                    custom, TIME_FREEZE_ENABLED_KEY);
            Optional<Boolean> weatherFreezeEnabled = MachineSettingsCodecSupport.readBoolean(
                    custom, WEATHER_FREEZE_ENABLED_KEY);
            if (timeFreezeEnabled.isEmpty() || weatherFreezeEnabled.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(machine -> {
                machine.setTimeFreezeEnabled(timeFreezeEnabled.get());
                machine.setWeatherFreezeEnabled(weatherFreezeEnabled.get());
            });
        });
    }

    static MachineSettingsCodec gelGenerator() {
        return typedCodec(GelGeneratorBE.class, (machine, ignoredRegistries, custom) ->
                custom.putBoolean(AUTO_BALANCE_INPUTS_KEY, machine.isAutoBalanceInputs()),
                (custom, registries) -> MachineSettingsCodecSupport.readBoolean(custom, AUTO_BALANCE_INPUTS_KEY)
                        .map(autoBalanceInputs -> machine -> machine.setAutoBalanceInputs(autoBalanceInputs)));
    }

    static MachineSettingsCodec advancedEnergyTransmitter() {
        return typedCodec(AdvancedEnergyTransmitterBE.class, (machine, ignoredRegistries, custom) -> {
            custom.putBoolean(SHOW_PARTICLES_KEY, machine.isShowingParticles());
            Optional<UUID> playerId = machine.getBoundPlayerIdForCopy();
            custom.putBoolean(HAS_BOUND_PLAYER_KEY, playerId.isPresent());
            if (playerId.isPresent()) {
                custom.putString(BOUND_PLAYER_ID_KEY, playerId.get().toString());
                custom.putString(BOUND_PLAYER_NAME_KEY, machine.getBoundPlayerNameForCopy());
            }
        }, (custom, registries) -> {
            Optional<Boolean> showParticles = MachineSettingsCodecSupport.readBoolean(custom, SHOW_PARTICLES_KEY);
            Optional<PlayerBinding> playerBinding = readPlayerBinding(custom);
            if (showParticles.isEmpty() || playerBinding.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(machine -> {
                machine.setShowParticles(showParticles.get());
                machine.applyCopiedPlayerBinding(playerBinding.get().playerId(), playerBinding.get().playerName());
            });
        });
    }

    static MachineSettingsCodec advancedPotionBrewer() {
        return typedCodec(AdvancedPotionBrewerBE.class, (machine, registries, custom) -> {
            boolean recipeLocked = machine.isRecipeLocked();
            custom.putBoolean(RECIPE_LOCKED_KEY, recipeLocked);
            custom.putBoolean(FUEL_INPUT_ENABLED_KEY, machine.isFuelInputEnabled());
            writeRecipeLockTemplates(custom, machine, recipeLocked, registries);
        }, (custom, registries) -> {
            Optional<Boolean> recipeLocked = MachineSettingsCodecSupport.readBoolean(custom, RECIPE_LOCKED_KEY);
            Optional<Boolean> fuelInputEnabled = MachineSettingsCodecSupport.readBoolean(custom, FUEL_INPUT_ENABLED_KEY);
            Optional<RecipeLockTemplates> templates = readRecipeLockTemplates(custom, registries);
            if (recipeLocked.isEmpty() || fuelInputEnabled.isEmpty() || templates.isEmpty()
                    || !recipeLocked.get() && templates.get().hasAnyTemplate()
                    || recipeLocked.get() && !templates.get().hasAnyTemplate()) {
                return Optional.empty();
            }
            return Optional.of(machine -> {
                machine.applyCopiedRecipeLock(recipeLocked.get(), templates.get().templates());
                machine.setFuelInputEnabled(fuelInputEnabled.get());
            });
        });
    }

    private static <T extends BaseMachineBE> MachineSettingsCodec multiplier(Class<T> machineType,
                                                                               ToIntFunction<T> getter,
                                                                               ObjIntConsumer<T> setter,
                                                                               IntSupplier maximum) {
        return typedCodec(machineType, (machine, ignoredRegistries, custom) ->
                custom.putInt(MULTIPLIER_KEY, getter.applyAsInt(machine)), (custom, registries) ->
                readMultiplier(custom, maximum.getAsInt()).map(value -> machine -> setter.accept(machine, value)));
    }

    private static <T extends BaseMachineBE> MachineSettingsCodec indexedMode(Class<T> machineType,
                                                                                ToIntFunction<T> getter,
                                                                                ObjIntConsumer<T> setter,
                                                                                int minimum,
                                                                                int maximum) {
        return typedCodec(machineType, (machine, ignoredRegistries, custom) ->
                custom.putInt(MODE_KEY, getter.applyAsInt(machine)), (custom, registries) -> {
            if (!custom.contains(MODE_KEY, Tag.TAG_INT)) {
                return Optional.empty();
            }
            int mode = custom.getInt(MODE_KEY);
            if (!MachineSettingsCodecSupport.isInRange(mode, minimum, maximum)) {
                return Optional.empty();
            }
            return Optional.of(machine -> setter.accept(machine, mode));
        });
    }

    private static Optional<Integer> readMultiplier(CompoundTag custom, int maximum) {
        if (!custom.contains(MULTIPLIER_KEY, Tag.TAG_INT)) {
            return Optional.empty();
        }
        int multiplier = custom.getInt(MULTIPLIER_KEY);
        if (!MachineSettingsCodecSupport.isInRange(multiplier, 1, Math.max(1, maximum))) {
            return Optional.empty();
        }
        return Optional.of(multiplier);
    }

    private static Optional<PlayerBinding> readPlayerBinding(CompoundTag custom) {
        Optional<Boolean> hasBoundPlayer = MachineSettingsCodecSupport.readBoolean(custom, HAS_BOUND_PLAYER_KEY);
        if (hasBoundPlayer.isEmpty()) {
            return Optional.empty();
        }
        if (!hasBoundPlayer.get()) {
            if (custom.contains(BOUND_PLAYER_ID_KEY) || custom.contains(BOUND_PLAYER_NAME_KEY)) {
                return Optional.empty();
            }
            return Optional.of(new PlayerBinding(Optional.empty(), ""));
        }
        if (!custom.contains(BOUND_PLAYER_ID_KEY, Tag.TAG_STRING)
                || !custom.contains(BOUND_PLAYER_NAME_KEY, Tag.TAG_STRING)) {
            return Optional.empty();
        }

        String serializedId = custom.getString(BOUND_PLAYER_ID_KEY);
        String playerName = custom.getString(BOUND_PLAYER_NAME_KEY);
        if (playerName.isEmpty() || playerName.length() > MAX_BOUND_PLAYER_NAME_LENGTH) {
            return Optional.empty();
        }
        try {
            UUID playerId = UUID.fromString(serializedId);
            if (!playerId.toString().equals(serializedId)) {
                return Optional.empty();
            }
            return Optional.of(new PlayerBinding(Optional.of(playerId), playerName));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static void writeRecipeLockTemplates(CompoundTag custom, AdvancedPotionBrewerBE machine,
                                                  boolean recipeLocked, HolderLookup.Provider registries) {
        CompoundTag templates = new CompoundTag();
        templates.putInt(TEMPLATE_SIZE_KEY, AdvancedPotionBrewerBE.TOTAL_SLOTS);
        for (int slot = 0; slot < AdvancedPotionBrewerBE.TOTAL_SLOTS; slot++) {
            ItemStack template = recipeLocked ? machine.getLockedRecipeTemplate(slot) : ItemStack.EMPTY;
            templates.put(TEMPLATE_SLOT_PREFIX + slot, template.isEmpty()
                    ? ItemStack.EMPTY.saveOptional(registries)
                    : template.copyWithCount(1).saveOptional(registries));
        }
        custom.put(RECIPE_LOCK_TEMPLATES_KEY, templates);
    }

    private static Optional<RecipeLockTemplates> readRecipeLockTemplates(CompoundTag custom,
                                                                           HolderLookup.Provider registries) {
        if (!custom.contains(RECIPE_LOCK_TEMPLATES_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag encodedTemplates = custom.getCompound(RECIPE_LOCK_TEMPLATES_KEY);
        if (!encodedTemplates.contains(TEMPLATE_SIZE_KEY, Tag.TAG_INT)
                || encodedTemplates.getInt(TEMPLATE_SIZE_KEY) != AdvancedPotionBrewerBE.TOTAL_SLOTS) {
            return Optional.empty();
        }

        boolean hasAnyTemplate = false;
        NonNullList<ItemStack> templates = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        for (int slot = 0; slot < AdvancedPotionBrewerBE.TOTAL_SLOTS; slot++) {
            String key = TEMPLATE_SLOT_PREFIX + slot;
            if (!encodedTemplates.contains(key, Tag.TAG_COMPOUND)) {
                return Optional.empty();
            }
            CompoundTag encodedStack = encodedTemplates.getCompound(key);
            ItemStack template = ItemStack.EMPTY;
            if (!encodedStack.isEmpty()) {
                final Optional<ItemStack> parsed;
                try {
                    parsed = ItemStack.parse(registries, encodedStack);
                } catch (RuntimeException ignored) {
                    return Optional.empty();
                }
                if (parsed.isEmpty() || parsed.get().isEmpty() || parsed.get().getCount() != 1) {
                    return Optional.empty();
                }
                template = parsed.get().copyWithCount(1);
            }
            if (!isRecipeLockTemplateSlot(slot) && !template.isEmpty()) {
                return Optional.empty();
            }
            templates.set(slot, template);
            hasAnyTemplate |= !template.isEmpty();
        }
        return Optional.of(new RecipeLockTemplates(templates, hasAnyTemplate));
    }

    private static boolean isRecipeLockTemplateSlot(int slot) {
        return slot == AdvancedPotionBrewerBE.INGREDIENT_SLOT
                || slot >= AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START
                && slot < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START
                + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT;
    }

    private static int maximum(int... values) {
        int maximum = 1;
        for (int value : values) {
            maximum = Math.max(maximum, value);
        }
        return maximum;
    }

    private static <T extends BaseMachineBE> MachineSettingsCodec typedCodec(Class<T> machineType,
                                                                              Encoder<T> encoder,
                                                                              Decoder<T> decoder) {
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
                Optional<Consumer<T>> machineSettings = decoder.decode(custom, registries);
                if (commonSettings.isEmpty() || machineSettings.isEmpty()) {
                    return Optional.empty();
                }

                return Optional.of(machine -> {
                    if (!machineType.isInstance(machine)) {
                        return;
                    }
                    commonSettings.get().apply(machine);
                    machineSettings.get().accept(machineType.cast(machine));
                });
            }
        };
    }

    private record PlayerBinding(Optional<UUID> playerId, String playerName) {
    }

    private record RecipeLockTemplates(NonNullList<ItemStack> templates, boolean hasAnyTemplate) {
    }

    @FunctionalInterface
    private interface Encoder<T extends BaseMachineBE> {
        void encode(T machine, HolderLookup.Provider registries, CompoundTag custom);
    }

    @FunctionalInterface
    private interface Decoder<T extends BaseMachineBE> {
        Optional<Consumer<T>> decode(CompoundTag custom, HolderLookup.Provider registries);
    }
}
