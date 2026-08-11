package com.jdte.common.integrations.jade;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.JDTE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.SolarPanelBE;
import com.jdte.common.blockentities.GreenhouseMatrixControllerBE;
import com.jdte.common.blockentities.GreenhouseMatrixPortBE;
import com.jdte.common.blocks.AdvancedEnergyTransmitterBlock;
import com.jdte.common.blocks.GreenhouseMatrixControllerBlock;
import com.jdte.common.blocks.GreenhouseMatrixPortBlock;
import com.jdte.common.blocks.LargeMineralExtractorBlock;
import com.jdte.common.blocks.LargeMineralExtractorPartBlock;
import com.jdte.common.blocks.MineralExtractorBlock;
import com.jdte.common.blocks.SolarPanelBlock;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource;
import com.jdte.common.greenhouse.GreenhouseMatrixPortType;
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
    private static final ResourceLocation GREENHOUSE_MATRIX_STATUS_UID = JDTE.id("greenhouse_matrix_status");
    private static final ResourceLocation GREENHOUSE_MATRIX_ITEM_STORAGE_UID = JDTE.id("greenhouse_matrix_item_storage");
    private static final ResourceLocation GREENHOUSE_MATRIX_FLUID_STORAGE_UID = JDTE.id("greenhouse_matrix_fluid_storage");
    private static final ResourceLocation SOLAR_PANEL_STATUS_UID = JDTE.id("solar_panel_status");
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
    private static final String TAG_MATRIX_PORT_TYPE = "jdte_matrix_port_type";
    private static final String TAG_MATRIX_LINKED = "jdte_matrix_linked";
    private static final String TAG_MATRIX_FORMED = "jdte_matrix_formed";
    private static final String TAG_MATRIX_ENABLED = "jdte_matrix_enabled";
    private static final String TAG_MATRIX_RENDER = "jdte_matrix_render";
    private static final String TAG_MATRIX_AUTO_IO = "jdte_matrix_auto_io";
    private static final String TAG_MATRIX_GREENHOUSES = "jdte_matrix_greenhouses";
    private static final String TAG_MATRIX_SIZE_X = "jdte_matrix_size_x";
    private static final String TAG_MATRIX_SIZE_Y = "jdte_matrix_size_y";
    private static final String TAG_MATRIX_SIZE_Z = "jdte_matrix_size_z";
    private static final String TAG_MATRIX_SPEED = "jdte_matrix_speed";
    private static final String TAG_MATRIX_EFFICIENCY = "jdte_matrix_efficiency";
    private static final String TAG_MATRIX_SEED = "jdte_matrix_seed";
    private static final String TAG_MATRIX_ESSENCE = "jdte_matrix_essence";
    private static final String TAG_MATRIX_ERROR = "jdte_matrix_error";
    private static final String TAG_MATRIX_QUICK_INSTALL = "jdte_matrix_quick_install";
    private static final String TAG_MATRIX_QUEUED_UPGRADES = "jdte_matrix_queued_upgrades";
    private static final String TAG_MATRIX_GROUPS = "jdte_matrix_groups";
    private static final String TAG_MATRIX_REBUILDING = "jdte_matrix_rebuilding";
    private static final String TAG_MATRIX_BUFFER_TYPES = "jdte_matrix_buffer_types";
    private static final String TAG_MATRIX_BUFFER_ITEMS = "jdte_matrix_buffer_items";
    private static final String TAG_SOLAR_ACTIVE = "jdte_solar_active";
    private static final String TAG_SOLAR_CREATIVE = "jdte_solar_creative";
    private static final String TAG_SOLAR_BASE = "jdte_solar_base";
    private static final String TAG_SOLAR_GENERATION = "jdte_solar_generation";
    private static final String TAG_SOLAR_STORED = "jdte_solar_stored";
    private static final String TAG_SOLAR_CAPACITY = "jdte_solar_capacity";
    private static final UpgradeProvider UPGRADE_PROVIDER = new UpgradeProvider();
    private static final TransmitterStatusProvider TRANSMITTER_STATUS_PROVIDER =
            new TransmitterStatusProvider();
    private static final MineralExtractorStatusProvider MINERAL_EXTRACTOR_STATUS_PROVIDER =
            new MineralExtractorStatusProvider();
    private static final GreenhouseMatrixStatusProvider GREENHOUSE_MATRIX_STATUS_PROVIDER =
            new GreenhouseMatrixStatusProvider();
    private static final GreenhouseMatrixItemStorageProvider GREENHOUSE_MATRIX_ITEM_STORAGE_PROVIDER =
            new GreenhouseMatrixItemStorageProvider();
    private static final GreenhouseMatrixFluidStorageProvider GREENHOUSE_MATRIX_FLUID_STORAGE_PROVIDER =
            new GreenhouseMatrixFluidStorageProvider();
    private static final SolarPanelStatusProvider SOLAR_PANEL_STATUS_PROVIDER =
            new SolarPanelStatusProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(UPGRADE_PROVIDER, BaseMachineBE.class);
        registration.registerBlockDataProvider(
                TRANSMITTER_STATUS_PROVIDER, AdvancedEnergyTransmitterBE.class);
        registration.registerBlockDataProvider(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, MineralExtractorBE.class);
        registration.registerBlockDataProvider(
                MINERAL_EXTRACTOR_STATUS_PROVIDER, LargeMineralExtractorPartBlock.class);
        registration.registerBlockDataProvider(
                GREENHOUSE_MATRIX_STATUS_PROVIDER, GreenhouseMatrixControllerBE.class);
        registration.registerBlockDataProvider(
                GREENHOUSE_MATRIX_STATUS_PROVIDER, GreenhouseMatrixPortBE.class);
        registration.registerBlockDataProvider(SOLAR_PANEL_STATUS_PROVIDER, SolarPanelBE.class);
        registration.registerItemStorage(
                GREENHOUSE_MATRIX_ITEM_STORAGE_PROVIDER, GreenhouseMatrixPortBE.class);
        registration.registerFluidStorage(
                GREENHOUSE_MATRIX_FLUID_STORAGE_PROVIDER, GreenhouseMatrixPortBE.class);
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
        registration.registerBlockComponent(
                GREENHOUSE_MATRIX_STATUS_PROVIDER, GreenhouseMatrixControllerBlock.class);
        registration.registerBlockComponent(
                GREENHOUSE_MATRIX_STATUS_PROVIDER, GreenhouseMatrixPortBlock.class);
        registration.registerBlockComponent(SOLAR_PANEL_STATUS_PROVIDER, SolarPanelBlock.class);
        registration.registerFluidStorageClient(GREENHOUSE_MATRIX_FLUID_STORAGE_PROVIDER);
    }

    /** Prevents Jade's generic item provider from expanding every internal greenhouse slot. */
    private static class GreenhouseMatrixItemStorageProvider implements IServerExtensionProvider<ItemStack> {
        @Override
        public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
            return List.of();
        }

        @Override
        public ResourceLocation getUid() {
            return GREENHOUSE_MATRIX_ITEM_STORAGE_UID;
        }
    }

    /** Publishes the fluid input port as the controller's single aggregate virtual tank. */
    static class GreenhouseMatrixFluidStorageProvider implements IServerExtensionProvider<CompoundTag>,
            IClientExtensionProvider<CompoundTag, FluidView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
            if (!(accessor instanceof BlockAccessor blockAccessor)
                    || !(blockAccessor.getBlock() instanceof GreenhouseMatrixPortBlock portBlock)
                    || portBlock.portType() != GreenhouseMatrixPortType.FLUID_INPUT
                    || !(blockAccessor.getBlockEntity() instanceof GreenhouseMatrixPortBE port)) {
                return List.of();
            }
            GreenhouseMatrixControllerBE controller = port.controller();
            IFluidHandler handler = controller == null ? null : controller.getFluidHandler();
            if (handler == null || handler.getTanks() == 0) return List.of();
            FluidStack stored = handler.getFluidInTank(0);
            JadeFluidObject fluid = stored.isEmpty()
                    ? JadeFluidObject.empty()
                    : JadeFluidObject.of(stored.getFluid(), stored.getAmount(), stored.getComponentsPatch());
            CompoundTag view = FluidView.writeDefault(fluid, handler.getTankCapacity(0));
            return List.of(new ViewGroup<>(List.of(view)));
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor,
                                                                List<ViewGroup<CompoundTag>> groups) {
            return ClientViewGroup.map(groups, FluidView::readDefault, null);
        }

        @Override
        public ResourceLocation getUid() {
            return GREENHOUSE_MATRIX_FLUID_STORAGE_UID;
        }
    }

    private static class GreenhouseMatrixStatusProvider
            implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            int portType = accessor.getBlock() instanceof GreenhouseMatrixPortBlock port
                    ? port.portType().ordinal() : -1;
            data.putInt(TAG_MATRIX_PORT_TYPE, portType);

            GreenhouseMatrixControllerBE controller = accessor.getBlockEntity() instanceof GreenhouseMatrixControllerBE direct
                    ? direct
                    : accessor.getBlockEntity() instanceof GreenhouseMatrixPortBE port ? port.controller() : null;
            data.putBoolean(TAG_MATRIX_LINKED, controller != null);
            if (controller == null) return;

            var matrix = controller.getMatrixData();
            data.putBoolean(TAG_MATRIX_FORMED, controller.isFormed());
            data.putBoolean(TAG_MATRIX_ENABLED, controller.isEnabled());
            data.putBoolean(TAG_MATRIX_RENDER, controller.isRenderEnabled());
            data.putBoolean(TAG_MATRIX_AUTO_IO, controller.isAutoIoEnabled());
            data.putInt(TAG_MATRIX_GREENHOUSES, controller.getGreenhouseCount());
            data.putInt(TAG_MATRIX_SIZE_X, matrix.get(8));
            data.putInt(TAG_MATRIX_SIZE_Y, matrix.get(9));
            data.putInt(TAG_MATRIX_SIZE_Z, matrix.get(10));
            data.putInt(TAG_MATRIX_SPEED, matrix.get(4));
            data.putInt(TAG_MATRIX_EFFICIENCY, matrix.get(5));
            data.putInt(TAG_MATRIX_SEED, matrix.get(6));
            data.putInt(TAG_MATRIX_ESSENCE, matrix.get(7));
            data.putInt(TAG_MATRIX_ERROR, matrix.get(11));
            data.putBoolean(TAG_MATRIX_QUICK_INSTALL, controller.hasQuickInstallUpgrade());
            data.putInt(TAG_MATRIX_QUEUED_UPGRADES, controller.getQueuedUpgradeCount());
            data.putInt(TAG_MATRIX_GROUPS, controller.getSimulation().groupCount());
            data.putBoolean(TAG_MATRIX_REBUILDING, controller.getSimulation().rebuilding());
            data.putInt(TAG_MATRIX_BUFFER_TYPES, controller.getOutputBuffer().distinctTypes());
            data.putLong(TAG_MATRIX_BUFFER_ITEMS, controller.getOutputBuffer().totalCount());
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            int portOrdinal = data.getInt(TAG_MATRIX_PORT_TYPE);
            boolean port = portOrdinal >= 0;
            if (port) {
                GreenhouseMatrixPortType[] types = GreenhouseMatrixPortType.values();
                GreenhouseMatrixPortType type = types[Math.floorMod(portOrdinal, types.length)];
                tooltip.add(Component.translatable("jade.jdte.greenhouse_matrix.port",
                        Component.translatable("jade.jdte.greenhouse_matrix.port."
                                + type.name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.GRAY));
                boolean linked = data.getBoolean(TAG_MATRIX_LINKED);
                tooltip.add(Component.translatable(linked
                                ? "jade.jdte.greenhouse_matrix.linked"
                                : "jade.jdte.greenhouse_matrix.unlinked")
                        .withStyle(linked ? ChatFormatting.GREEN : ChatFormatting.RED));
                if (!linked) return;
            }

            boolean formed = data.getBoolean(TAG_MATRIX_FORMED);
            if (formed) {
                tooltip.add(Component.translatable("jade.jdte.greenhouse_matrix.formed",
                                data.getInt(TAG_MATRIX_GREENHOUSES))
                        .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("jade.jdte.greenhouse_matrix.size",
                                data.getInt(TAG_MATRIX_SIZE_X), data.getInt(TAG_MATRIX_SIZE_Y),
                                data.getInt(TAG_MATRIX_SIZE_Z))
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("jade.jdte.greenhouse_matrix.invalid",
                                Component.translatable("jdte.screen.greenhouse_matrix.error."
                                        + data.getInt(TAG_MATRIX_ERROR)))
                        .withStyle(ChatFormatting.RED));
            }

            tooltip.add(Component.translatable(data.getBoolean(TAG_MATRIX_ENABLED)
                            ? "jade.jdte.greenhouse_matrix.operation.on"
                            : "jade.jdte.greenhouse_matrix.operation.off")
                    .withStyle(data.getBoolean(TAG_MATRIX_ENABLED) ? ChatFormatting.GREEN : ChatFormatting.RED));
            tooltip.add(Component.translatable(data.getBoolean(TAG_MATRIX_AUTO_IO)
                            ? "jade.jdte.greenhouse_matrix.auto_io.on"
                            : "jade.jdte.greenhouse_matrix.auto_io.off")
                    .withStyle(data.getBoolean(TAG_MATRIX_AUTO_IO) ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.jdte.greenhouse_matrix.simulation",
                            data.getInt(TAG_MATRIX_GROUPS), data.getInt(TAG_MATRIX_BUFFER_TYPES),
                            data.getLong(TAG_MATRIX_BUFFER_ITEMS))
                    .withStyle(data.getBoolean(TAG_MATRIX_REBUILDING)
                            ? ChatFormatting.YELLOW : ChatFormatting.GRAY));

            if (!port) {
                tooltip.add(Component.translatable(data.getBoolean(TAG_MATRIX_RENDER)
                                ? "jade.jdte.greenhouse_matrix.render.on"
                                : "jade.jdte.greenhouse_matrix.render.off")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("jade.jdte.greenhouse_matrix.enhancements",
                                data.getInt(TAG_MATRIX_SPEED), data.getInt(TAG_MATRIX_EFFICIENCY),
                                data.getInt(TAG_MATRIX_SEED), data.getInt(TAG_MATRIX_ESSENCE))
                        .withStyle(ChatFormatting.GRAY));
                boolean quickInstall = data.getBoolean(TAG_MATRIX_QUICK_INSTALL);
                tooltip.add(Component.translatable(quickInstall
                                ? "jade.jdte.greenhouse_matrix.quick_install.on"
                                : "jade.jdte.greenhouse_matrix.quick_install.off",
                                data.getInt(TAG_MATRIX_QUEUED_UPGRADES))
                        .withStyle(quickInstall ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return GREENHOUSE_MATRIX_STATUS_UID;
        }
    }

    private static class SolarPanelStatusProvider
            implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof SolarPanelBE panel)) return;
            boolean creative = panel.tier().creative();
            data.putBoolean(TAG_SOLAR_ACTIVE, panel.canGenerate());
            data.putBoolean(TAG_SOLAR_CREATIVE, creative);
            data.putLong(TAG_SOLAR_BASE, creative ? Long.MAX_VALUE : panel.baseGeneration());
            data.putLong(TAG_SOLAR_GENERATION, creative ? Long.MAX_VALUE : panel.currentGeneration());
            data.putInt(TAG_SOLAR_STORED, panel.getEnergyStorage().getEnergyStored());
            data.putInt(TAG_SOLAR_CAPACITY, panel.getEnergyStorage().getMaxEnergyStored());
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.getBoolean(TAG_SOLAR_CREATIVE)) {
                tooltip.add(Component.translatable("jade.jdte.solar_panel.infinite")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                return;
            }
            tooltip.add(Component.translatable("jade.jdte.solar_panel.base_generation",
                    data.getLong(TAG_SOLAR_BASE)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.jdte.solar_panel.generation",
                    data.getLong(TAG_SOLAR_GENERATION)).withStyle(
                    data.getBoolean(TAG_SOLAR_ACTIVE) ? ChatFormatting.GREEN : ChatFormatting.RED));
            tooltip.add(Component.translatable("jade.jdte.solar_panel.storage",
                    data.getInt(TAG_SOLAR_STORED), data.getInt(TAG_SOLAR_CAPACITY))
                    .withStyle(ChatFormatting.GRAY));
            if (!data.getBoolean(TAG_SOLAR_ACTIVE)) {
                tooltip.add(Component.translatable("jade.jdte.solar_panel.inactive",
                        Component.translatable("jade.jdte.solar_panel.reason.no_sun")).withStyle(ChatFormatting.RED));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return SOLAR_PANEL_STATUS_UID;
        }
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
