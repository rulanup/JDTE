package com.jdte.common.integrations.jade;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.JDTE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blocks.AdvancedEnergyTransmitterBlock;
import com.jdte.common.blocks.LargeMineralExtractorBlock;
import com.jdte.common.blocks.LargeMineralExtractorPartBlock;
import com.jdte.common.blocks.MineralExtractorBlock;
import com.jdte.common.blocks.LargeMineralExtractorBlock;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WailaPlugin(JDTE.MODID)
public class JDTEJadePlugin implements IWailaPlugin {
    private static final ResourceLocation UID = JDTE.id("installed_upgrades");
    private static final ResourceLocation TRANSMITTER_STATUS_UID = JDTE.id("advanced_energy_transmitter_status");
    private static final ResourceLocation MINERAL_EXTRACTOR_STATUS_UID = JDTE.id("mineral_extractor_status");
    private static final String TAG_UPGRADES = "jdte_upgrades";
    private static final String TAG_ME_STATUS = "jdte_me_status";
    private static final String TAG_PLAYER_BOUND = "jdte_player_bound";
    private static final String TAG_PLAYER_ONLINE = "jdte_player_online";
    private static final String TAG_PLAYER_NAME = "jdte_player_name";
    private static final String TAG_EXTRACTOR_STATE = "jdte_extractor_state";
    private static final String TAG_EXTRACTOR_SURVEY = "jdte_extractor_survey";
    private static final String TAG_EXTRACTOR_MINERALS = "jdte_extractor_minerals";
    private static final String TAG_EXTRACTOR_MULTIPLIER = "jdte_extractor_multiplier";
    private static final String TAG_EXTRACTOR_MAX_MULTIPLIER = "jdte_extractor_max_multiplier";
    private static final UpgradeProvider UPGRADE_PROVIDER = new UpgradeProvider();
    private static final TransmitterStatusProvider TRANSMITTER_STATUS_PROVIDER =
            new TransmitterStatusProvider();
    private static final MineralExtractorStatusProvider MINERAL_EXTRACTOR_STATUS_PROVIDER =
            new MineralExtractorStatusProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(UPGRADE_PROVIDER, BaseMachineBE.class);
        registration.registerBlockDataProvider(
                TRANSMITTER_STATUS_PROVIDER, AdvancedEnergyTransmitterBE.class);
        registration.registerBlockDataProvider(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, MineralExtractorBE.class);
        registration.registerBlockDataProvider(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, LargeMineralExtractorPartBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(UPGRADE_PROVIDER, BaseMachineBlock.class);
        registration.registerBlockComponent(
                TRANSMITTER_STATUS_PROVIDER, AdvancedEnergyTransmitterBlock.class);
        registration.registerBlockComponent(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, MineralExtractorBlock.class);
        registration.registerBlockComponent(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, LargeMineralExtractorBlock.class);
        registration.registerBlockComponent(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, LargeMineralExtractorPartBlock.class);
    }

    private static class MineralExtractorStatusProvider
            implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
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
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            MineralExtractorBE.State[] states = MineralExtractorBE.State.values();
            MineralExtractorBE.State state = states[Math.floorMod(data.getInt(TAG_EXTRACTOR_STATE), states.length)];
            ChatFormatting stateColor = state == MineralExtractorBE.State.RUNNING
                    ? ChatFormatting.GREEN
                    : state == MineralExtractorBE.State.IDLE ? ChatFormatting.GRAY : ChatFormatting.RED;
            tooltip.add(Component.translatable("jdte.screen.mineral_extractor.state."
                    + state.name().toLowerCase(Locale.ROOT)).withStyle(stateColor));
            tooltip.add(Component.translatable(data.getBoolean(TAG_EXTRACTOR_SURVEY)
                    ? "jdte.screen.mineral_extractor.source.survey"
                    : "jdte.screen.mineral_extractor.source.local").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jdte.screen.mineral_extractor.minerals",
                    data.getInt(TAG_EXTRACTOR_MINERALS)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.jdte.mineral_extractor.speed",
                    data.getInt(TAG_EXTRACTOR_MULTIPLIER), data.getInt(TAG_EXTRACTOR_MAX_MULTIPLIER))
                    .withStyle(ChatFormatting.GRAY));
        }

        @Override
        public ResourceLocation getUid() {
            return MINERAL_EXTRACTOR_STATUS_UID;
        }
    }

    private static class TransmitterStatusProvider
            implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
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
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            AdvancedEnergyTransmitterEnergySource.Status[] statuses =
                    AdvancedEnergyTransmitterEnergySource.Status.values();
            int ordinal = Math.floorMod(data.getInt(TAG_ME_STATUS), statuses.length);
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

            if (!data.getBoolean(TAG_PLAYER_BOUND)) {
                tooltip.add(Component.translatable("jdte.screen.energy_transmitter.player_unbound")
                        .withStyle(ChatFormatting.GRAY));
                return;
            }
            boolean online = data.getBoolean(TAG_PLAYER_ONLINE);
            tooltip.add(Component.translatable(online
                            ? "jdte.screen.energy_transmitter.player_online"
                            : "jdte.screen.energy_transmitter.player_offline",
                    data.getString(TAG_PLAYER_NAME)).withStyle(
                    online ? ChatFormatting.GREEN : ChatFormatting.RED));
        }

        @Override
        public ResourceLocation getUid() {
            return TRANSMITTER_STATUS_UID;
        }
    }

    private static class UpgradeProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof BaseMachineBE machine)) {
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
            for (ItemStack upgrade : upgrades) {
                serialized.add(upgrade.save(accessor.getLevel().registryAccess()));
            }
            data.put(TAG_UPGRADES, serialized);
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            ListTag serialized = accessor.getServerData().getList(TAG_UPGRADES, Tag.TAG_COMPOUND);
            if (serialized.isEmpty()) {
                return;
            }

            tooltip.add(Component.translatable("jade.jdte.installed_upgrades").withStyle(ChatFormatting.GRAY));
            IElementHelper elements = IElementHelper.get();
            for (int index = 0; index < serialized.size(); index++) {
                ItemStack upgrade = ItemStack.parseOptional(
                        accessor.getLevel().registryAccess(), serialized.getCompound(index));
                if (upgrade.isEmpty()) {
                    continue;
                }
                Component label = Component.literal(" ")
                        .append(upgrade.getHoverName())
                        .append(Component.literal(" x" + upgrade.getCount()).withStyle(ChatFormatting.GRAY));
                tooltip.add(List.of(elements.smallItem(upgrade.copyWithCount(1)), elements.text(label)));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        private static void collect(List<ItemStack> upgrades, IItemHandler handler) {
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
}
