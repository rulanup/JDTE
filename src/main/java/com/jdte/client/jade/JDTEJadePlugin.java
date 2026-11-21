package com.jdte.client.jade;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.JDTE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blocks.AdvancedEnergyTransmitterBlock;
import com.jdte.common.blocks.GreenhouseBlock;
import com.jdte.common.blocks.LargeGreenhouseBlock;
import com.jdte.common.blocks.LargeGreenhousePartBlock;
import com.jdte.common.blocks.LargeMineralExtractorBlock;
import com.jdte.common.blocks.LargeMineralExtractorPartBlock;
import com.jdte.common.blocks.MineralExtractorBlock;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.items.EssenceConversionUpgradeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WailaPlugin(JDTE.MODID)
public class JDTEJadePlugin implements IWailaPlugin {
    private static final Identifier UID = JDTE.id("installed_upgrades");
    private static final Identifier TRANSMITTER_STATUS_UID = JDTE.id("advanced_energy_transmitter_status");
    private static final Identifier MINERAL_EXTRACTOR_STATUS_UID = JDTE.id("mineral_extractor_status");
    private static final Identifier GREENHOUSE_STATUS_UID = JDTE.id("greenhouse_status");
    private static final String TAG_UPGRADES = "jdte_upgrades";
    private static final String TAG_CONVERSION_PATTERNS = "jdte_conversion_pattern_status";
    private static final String TAG_ME_STATUS = "jdte_me_status";
    private static final String TAG_PLAYER_BOUND = "jdte_player_bound";
    private static final String TAG_PLAYER_ONLINE = "jdte_player_online";
    private static final String TAG_PLAYER_NAME = "jdte_player_name";
    private static final String TAG_EXTRACTOR_STATE = "jdte_extractor_state";
    private static final String TAG_EXTRACTOR_SURVEY = "jdte_extractor_survey";
    private static final String TAG_EXTRACTOR_MINERALS = "jdte_extractor_minerals";
    private static final String TAG_EXTRACTOR_MULTIPLIER = "jdte_extractor_multiplier";
    private static final String TAG_EXTRACTOR_MAX_MULTIPLIER = "jdte_extractor_max_multiplier";
    private static final String TAG_GREENHOUSE_ULTIMATE_CAPACITY_WARNING = "jdte_greenhouse_ultimate_capacity_warning";
    @Override
    public void register(IWailaCommonRegistration registration) {
        var upgrade = new UpgradeDataProvider();
        var transmitterStatus = new TransmitterStatusDataProvider();
        var mineralExtractorStatus = new MineralExtractorStatusDataProvider();
        var greenhouseStatus = new GreenhouseStatusDataProvider();
        registration.registerBlockDataProvider(upgrade, BaseMachineBE.class);
        registration.registerBlockDataProvider(upgrade, LargeGreenhousePartBlock.class);
        registration.registerBlockDataProvider(upgrade, LargeMineralExtractorPartBlock.class);
        registration.registerBlockDataProvider(
                transmitterStatus, AdvancedEnergyTransmitterBE.class);
        registration.registerBlockDataProvider(
                mineralExtractorStatus, MineralExtractorBE.class);
        registration.registerBlockDataProvider(
                mineralExtractorStatus, LargeMineralExtractorPartBlock.class);
        registration.registerBlockDataProvider(
                greenhouseStatus, GreenhouseBE.class);
        registration.registerBlockDataProvider(
                greenhouseStatus, LargeGreenhouseBE.class);
        registration.registerBlockDataProvider(
                greenhouseStatus, LargeGreenhousePartBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        var upgrade = new UpgradeComponentProvider();
        var transmitterStatus = new TransmitterStatusComponentProvider();
        var mineralExtractorStatus = new MineralExtractorStatusComponentProvider();
        var greenhouseStatus = new GreenhouseStatusComponentProvider();
        registration.registerBlockComponent(upgrade, BaseMachineBlock.class);
        registration.registerBlockComponent(upgrade, LargeGreenhousePartBlock.class);
        registration.registerBlockComponent(upgrade, LargeMineralExtractorPartBlock.class);
        registration.registerBlockComponent(
                transmitterStatus, AdvancedEnergyTransmitterBlock.class);
        registration.registerBlockComponent(
                mineralExtractorStatus, MineralExtractorBlock.class);
        registration.registerBlockComponent(
                mineralExtractorStatus, LargeMineralExtractorBlock.class);
        registration.registerBlockComponent(
                mineralExtractorStatus, LargeMineralExtractorPartBlock.class);
        registration.registerBlockComponent(
                greenhouseStatus, GreenhouseBlock.class);
        registration.registerBlockComponent(
                greenhouseStatus, LargeGreenhouseBlock.class);
        registration.registerBlockComponent(
                greenhouseStatus, LargeGreenhousePartBlock.class);
    }

    private static class MineralExtractorStatusDataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            MineralExtractorBE extractor = accessor.getBlockEntity() instanceof MineralExtractorBE direct
                    ? direct
                    : accessor.getBlock() instanceof LargeMineralExtractorPartBlock part
                    ? part.getController(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())
                    : null;
            if (extractor == null) return;
            data.putInt(TAG_EXTRACTOR_STATE, extractor.getMachineState().ordinal());
            data.putBoolean(TAG_EXTRACTOR_SURVEY, extractor.isSurveySource());
            data.putInt(TAG_EXTRACTOR_MINERALS, extractor.getMachineData().get(11));
            data.putInt(TAG_EXTRACTOR_MULTIPLIER, extractor.getMultiplier());
            data.putInt(TAG_EXTRACTOR_MAX_MULTIPLIER, extractor.getMaxSelectableMultiplier());
        }

        @Override
        public Identifier getUid() {
            return MINERAL_EXTRACTOR_STATUS_UID;
        }
    }

    private static class MineralExtractorStatusComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            MineralExtractorBE.State[] states = MineralExtractorBE.State.values();
            MineralExtractorBE.State state = states[Math.floorMod(data.getIntOr(TAG_EXTRACTOR_STATE, 0), states.length)];
            ChatFormatting stateColor = state == MineralExtractorBE.State.RUNNING
                    ? ChatFormatting.GREEN
                    : state == MineralExtractorBE.State.IDLE ? ChatFormatting.GRAY : ChatFormatting.RED;
            tooltip.add(Component.translatable("jdte.screen.mineral_extractor.state."
                    + state.name().toLowerCase(Locale.ROOT)).withStyle(stateColor));
            tooltip.add(Component.translatable(data.getBooleanOr(TAG_EXTRACTOR_SURVEY, false)
                    ? "jdte.screen.mineral_extractor.source.survey"
                    : "jdte.screen.mineral_extractor.source.local").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jdte.screen.mineral_extractor.minerals",
                    data.getIntOr(TAG_EXTRACTOR_MINERALS, 0)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.jdte.mineral_extractor.speed",
                    data.getIntOr(TAG_EXTRACTOR_MULTIPLIER, 0), data.getIntOr(TAG_EXTRACTOR_MAX_MULTIPLIER, 0))
                    .withStyle(ChatFormatting.GRAY));
        }

        @Override
        public Identifier getUid() {
            return MINERAL_EXTRACTOR_STATUS_UID;
        }
    }

    private static class TransmitterStatusDataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof AdvancedEnergyTransmitterBE transmitter)) {
                return;
            }
            data.putInt(TAG_ME_STATUS, transmitter.getEnergyNetworkStatus().ordinal());
            data.putBoolean(TAG_PLAYER_BOUND, transmitter.hasBoundPlayer());
            data.putBoolean(TAG_PLAYER_ONLINE, transmitter.isBoundPlayerOnline());
            data.putString(TAG_PLAYER_NAME, transmitter.getBoundPlayerName());
        }

        @Override
        public Identifier getUid() {
            return TRANSMITTER_STATUS_UID;
        }
    }

    private static class TransmitterStatusComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            AdvancedEnergyTransmitterEnergySource.Status[] statuses =
                    AdvancedEnergyTransmitterEnergySource.Status.values();
            int ordinal = Math.floorMod(data.getIntOr(TAG_ME_STATUS, 0), statuses.length);
            AdvancedEnergyTransmitterEnergySource.Status status = statuses[ordinal];
            String meKey = switch (status) {
                case ONLINE -> "jdte.screen.energy_transmitter.me_online_direct";
                case OFFLINE -> "jdte.screen.energy_transmitter.me_offline";
                case UNAVAILABLE -> "jdte.screen.energy_transmitter.me_unavailable";
            };
            ChatFormatting meColor = status == AdvancedEnergyTransmitterEnergySource.Status.ONLINE
                    ? ChatFormatting.GREEN
                    : status == AdvancedEnergyTransmitterEnergySource.Status.OFFLINE
                    ? ChatFormatting.RED : ChatFormatting.GRAY;
            tooltip.add(Component.translatable(meKey).withStyle(meColor));

            if (!data.getBooleanOr(TAG_PLAYER_BOUND, false)) {
                tooltip.add(Component.translatable("jdte.screen.energy_transmitter.player_unbound")
                        .withStyle(ChatFormatting.GRAY));
                return;
            }
            boolean online = data.getBooleanOr(TAG_PLAYER_ONLINE, false);
            tooltip.add(Component.translatable(online
                            ? "jdte.screen.energy_transmitter.player_online"
                            : "jdte.screen.energy_transmitter.player_offline",
                    data.getStringOr(TAG_PLAYER_NAME, "")).withStyle(
                    online ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        @Override
        public Identifier getUid() {
            return TRANSMITTER_STATUS_UID;
        }
    }

    private static class UpgradeDataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BaseMachineBE machine = resolveMachine(accessor);
            if (machine == null) {
                return;
            }

            List<ItemStack> upgrades = new ArrayList<>();
            collect(upgrades, UpgradeHelper.getUpgradeHandler(machine));
            if (machine instanceof BioCrusherBE crusher) {
                collect(upgrades, crusher.getLootingHandler());
                collect(upgrades, crusher.getSharpnessHandler());
            }
            if (upgrades.isEmpty()) {
                return;
            }

            ListTag serialized = new ListTag();
            ListTag patternStatuses = new ListTag();
            for (ItemStack upgrade : upgrades) {
                serialized.add(ItemStack.CODEC.encodeStart(accessor.getLevel().registryAccess()
                        .createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), upgrade).getOrThrow());
                CompoundTag statuses = new CompoundTag();
                var patterns = EssenceConversionUpgradeItem.patterns(upgrade);
                for (int index = 0; index < patterns.size(); index++) {
                    statuses.putBoolean(Integer.toString(index), patterns.get(index).resolve(accessor.getLevel()).isPresent());
                }
                patternStatuses.add(statuses);
            }
            data.put(TAG_UPGRADES, serialized);
            data.put(TAG_CONVERSION_PATTERNS, patternStatuses);
        }

        private static BaseMachineBE resolveMachine(BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof BaseMachineBE machine) {
                return machine;
            }
            if (accessor.getBlock() instanceof LargeGreenhousePartBlock greenhousePart) {
                return greenhousePart.getController(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
            }
            if (accessor.getBlock() instanceof LargeMineralExtractorPartBlock extractorPart) {
                return extractorPart.getController(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
            }
            return null;
        }

        @Override
        public Identifier getUid() {
            return UID;
        }

        private static void collect(List<ItemStack> upgrades, IItemHandler handler) {
            if (handler == null) return;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack existing = upgrades.stream()
                        .filter(candidate -> ItemStack.isSameItemSameComponents(candidate, stack))
                        .findFirst()
                        .orElse(null);
                if (existing == null) {
                    upgrades.add(stack.copy());
                } else {
                    existing.grow(stack.getCount());
                }
            }
        }
    }

    private static class UpgradeComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            ListTag serialized = accessor.getServerData().getListOrEmpty(TAG_UPGRADES);
            if (serialized.isEmpty()) {
                return;
            }

            tooltip.add(Component.translatable("jade.jdte.installed_upgrades").withStyle(ChatFormatting.GRAY));
            ListTag patternStatuses = accessor.getServerData().getListOrEmpty(TAG_CONVERSION_PATTERNS);
            for (int index = 0; index < serialized.size(); index++) {
                ItemStack upgrade = ItemStack.CODEC.parse(accessor.getLevel().registryAccess()
                        .createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), serialized.get(index)).result().orElse(ItemStack.EMPTY);
                if (upgrade.isEmpty()) {
                    continue;
                }
                Component label = Component.literal(" ")
                        .append(upgrade.getHoverName())
                        .append(Component.literal(" x" + upgrade.getCount()).withStyle(ChatFormatting.GRAY));
                tooltip.add(List.of(JadeUI.smallItem(upgrade.copyWithCount(1)), JadeUI.text(label)));
                if (upgrade.getItem() instanceof EssenceConversionUpgradeItem item) {
                    var patterns = EssenceConversionUpgradeItem.patterns(upgrade);
                    tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.patterns", patterns.size(), item.patternLimit())
                            .withStyle(ChatFormatting.AQUA));
                    CompoundTag statuses = index < patternStatuses.size() ? patternStatuses.getCompoundOrEmpty(index) : new CompoundTag();
                    int maxDisplay = Math.min(patterns.size(), 6);
                    for (int patternIndex = 0; patternIndex < maxDisplay; patternIndex++) {
                        var pattern = patterns.get(patternIndex);
                        boolean valid = statuses.getBooleanOr(Integer.toString(patternIndex), false);
                        Component patternLabel = Component.translatable("jdte.jade.essence_conversion.pattern",
                                patternIndex + 1, pattern.result().getHoverName(), pattern.result().getCount(),
                                Component.translatable(valid ? "jdte.jade.essence_conversion.valid" : "jdte.jade.essence_conversion.invalid"))
                                .withStyle(valid ? ChatFormatting.GREEN : ChatFormatting.RED);
                        tooltip.add(List.of(JadeUI.smallItem(pattern.result()), JadeUI.text(patternLabel)));
                    }
                    if (patterns.size() > maxDisplay) {
                        tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.more", patterns.size() - maxDisplay)
                                .withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }

    private static class GreenhouseStatusDataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BaseMachineBE machine = resolveGreenhouse(accessor);
            if (machine == null) {
                return;
            }

            boolean ultimateOverclock = UpgradeHelper.hasUltimateOverclock(machine);
            boolean ultimateCapacity = UpgradeHelper.hasUltimateCapacity(machine);
            boolean creative = UpgradeHelper.hasCreativeUpgrade(machine);
            if (ultimateOverclock && !ultimateCapacity && !creative) {
                data.putBoolean(TAG_GREENHOUSE_ULTIMATE_CAPACITY_WARNING, true);
            }
        }

        @Override
        public Identifier getUid() {
            return GREENHOUSE_STATUS_UID;
        }

        private static BaseMachineBE resolveGreenhouse(BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof GreenhouseBE greenhouse) {
                return greenhouse;
            }
            if (accessor.getBlockEntity() instanceof LargeGreenhouseBE largeGreenhouse) {
                return largeGreenhouse;
            }
            if (accessor.getBlock() instanceof LargeGreenhousePartBlock part) {
                return part.getController(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
            }
            return null;
        }
    }

    private static class GreenhouseStatusComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.getBooleanOr(TAG_GREENHOUSE_ULTIMATE_CAPACITY_WARNING, false)) {
                tooltip.add(Component.translatable("jade.jdte.greenhouse.ultimate_capacity_warning")
                        .withStyle(ChatFormatting.RED));
            }
        }

        @Override
        public Identifier getUid() {
            return GREENHOUSE_STATUS_UID;
        }
    }
}
