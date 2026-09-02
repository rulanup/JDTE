package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.items.machinesettings.MachineSettingsCodec;
import com.jdte.common.items.machinesettings.MachineSettingsCodecRegistry;
import com.jdte.common.items.machinesettings.MachineSettingsSnapshot;
import com.jdte.common.items.machinesettings.MachineUpgradeHandlers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Optional;

public class AdvancedMachineSettingsCopierItem extends MachineSettingsCopier {
    public AdvancedMachineSettingsCopierItem() {
        super();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof BaseMachineBE machine)) {
            return InteractionResult.PASS;
        }

        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return super.useOn(context);
        }

        ItemStack stack = context.getItemInHand();
        Optional<PreparedPaste> prepared = preparePaste(level, machine, stack);
        if (prepared.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!consumeUpgradeCost(context.getPlayer(), prepared.get().requiredUpgrades())) {
            return InteractionResult.PASS;
        }

        prepared.get().apply(this, level, blockEntity, machine, stack);
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable("justdirethings.settingspasted"), true);
            player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void saveSettings(Level level, BlockEntity blockEntity, ItemStack stack) {
        saveBaseSettings(level, blockEntity, stack);

        CompoundTag copiedData = stack.has(JustDireDataComponents.COPIED_MACHINE_DATA)
                ? stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag()
                : new CompoundTag();

        if (blockEntity instanceof BaseMachineBE machine) {
            ResourceLocation machineTypeId = getBlockEntityTypeId(machine);
            if (machineTypeId == null) {
                AdvancedMachineSettingsCopierData.remove(copiedData);
                AdvancedMachineSettingsCopierData.clearMachineSettings(copiedData);
                AdvancedMachineSettingsCopierData.clearUpgrades(copiedData);
                AdvancedMachineSettingsCopierData.clearUpgradeHandlers(copiedData);
            } else {
                AdvancedMachineSettingsCopierData.writeMachineType(copiedData, machineTypeId);
                saveMachineSettings(level, machine, machineTypeId, copiedData);
                saveUpgrades(level, machine, copiedData);

                if (hasConfigurableIo(machine)) {
                    AdvancedMachineSettingsCopierData.writeMasks(copiedData,
                            getInputMask(machine),
                            getOutputMask(machine));
                } else {
                    AdvancedMachineSettingsCopierData.clearMasks(copiedData);
                }
            }
        } else {
            AdvancedMachineSettingsCopierData.remove(copiedData);
            AdvancedMachineSettingsCopierData.clearMachineSettings(copiedData);
            AdvancedMachineSettingsCopierData.clearUpgrades(copiedData);
            AdvancedMachineSettingsCopierData.clearUpgradeHandlers(copiedData);
        }

        if (copiedData.isEmpty()) {
            stack.remove(JustDireDataComponents.COPIED_MACHINE_DATA.get());
        } else {
            stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));
        }
    }

    @Override
    public void loadSettings(Level level, BlockEntity blockEntity, ItemStack stack) {
        if (!(blockEntity instanceof BaseMachineBE machine) || !stack.has(JustDireDataComponents.COPIED_MACHINE_DATA)) {
            return;
        }

        Optional<PreparedPaste> prepared = preparePaste(level, machine, stack);
        if (prepared.isEmpty()) {
            return;
        }
        prepared.get().apply(this, level, blockEntity, machine, stack);
    }

    protected void saveBaseSettings(Level level, BlockEntity blockEntity, ItemStack stack) {
        super.saveSettings(level, blockEntity, stack);
    }

    protected void loadBaseSettings(Level level, BlockEntity blockEntity, ItemStack stack) {
        super.loadSettings(level, blockEntity, stack);
    }

    protected ResourceLocation getBlockEntityTypeId(BaseMachineBE machine) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType());
    }

    protected Optional<MachineSettingsCodec> getMachineSettingsCodec(ResourceLocation machineType,
                                                                       BaseMachineBE machine) {
        return MachineSettingsCodecRegistry.find(machineType, machine);
    }

    protected boolean hasConfigurableIo(BaseMachineBE machine) {
        return AutoIoConfigHelper.hasConfigurableIo(machine);
    }

    protected int getInputMask(BaseMachineBE machine) {
        return AutoIoConfigHelper.getInputMask(machine);
    }

    protected int getOutputMask(BaseMachineBE machine) {
        return AutoIoConfigHelper.getOutputMask(machine);
    }

    protected void setMasks(BaseMachineBE machine, int inputMask, int outputMask) {
        AutoIoConfigHelper.setMasks(machine, inputMask, outputMask);
    }

    protected HolderLookup.Provider getRegistryAccess(Level level) {
        return level == null ? null : level.registryAccess();
    }

    protected void saveUpgrades(Level level, BaseMachineBE machine, CompoundTag copiedData) {
        HolderLookup.Provider registries = getRegistryAccess(level);
        if (registries == null) {
            AdvancedMachineSettingsCopierData.clearUpgrades(copiedData);
            AdvancedMachineSettingsCopierData.clearUpgradeHandlers(copiedData);
            return;
        }
        MachineUpgradeHandlers.write(copiedData, machine, registries);
    }

    private void saveMachineSettings(Level level, BaseMachineBE machine, ResourceLocation machineType,
                                     CompoundTag copiedData) {
        HolderLookup.Provider registries = getRegistryAccess(level);
        if (registries == null) {
            AdvancedMachineSettingsCopierData.clearMachineSettings(copiedData);
            return;
        }

        Optional<MachineSettingsCodec> codec = getMachineSettingsCodec(machineType, machine);
        if (codec.isEmpty()) {
            AdvancedMachineSettingsCopierData.clearMachineSettings(copiedData);
            return;
        }

        try {
            MachineSettingsSnapshot.write(copiedData, machineType, machine.getTickSpeed(), machine.getDirection(),
                    codec.get().encode(machine, registries));
        } catch (RuntimeException ignored) {
            AdvancedMachineSettingsCopierData.clearMachineSettings(copiedData);
        }
    }

    protected void loadUpgrades(Level level, BaseMachineBE machine, CompoundTag copiedData) {
        HolderLookup.Provider registries = getRegistryAccess(level);
        if (registries == null) {
            return;
        }

        MachineUpgradeHandlers.prepare(copiedData, machine, registries).ifPresent(prepared -> {
            prepared.apply(machine);
        });
    }

    /**
     * Checks and atomically consumes one matching inventory item for every copied upgrade.
     * A failed check leaves the player's inventory untouched.
     */
    protected boolean consumeUpgradeCost(Player player, Level level, BaseMachineBE machine, ItemStack copierStack) {
        Optional<List<ItemStack>> requiredResult = readRequiredUpgrades(level, machine, copierStack);
        if (requiredResult.isEmpty()) {
            return false;
        }

        return consumeUpgradeCost(player, requiredResult.get());
    }

    private boolean consumeUpgradeCost(Player player, List<ItemStack> required) {
        if (required.isEmpty()) {
            return true;
        }
        Inventory inventory = getPlayerInventory(player);
        if (inventory == null) {
            return false;
        }

        int[] consumed = new int[inventory.getContainerSize()];
        for (ItemStack requirement : required) {
            int matchingSlot = -1;
            for (int slot = 0; slot < consumed.length; slot++) {
                ItemStack available = inventory.getItem(slot);
                if (available.getCount() > consumed[slot]
                        && ItemStack.isSameItemSameComponents(available, requirement)) {
                    matchingSlot = slot;
                    break;
                }
            }
            if (matchingSlot < 0) {
                return false;
            }
            consumed[matchingSlot]++;
        }

        for (int slot = 0; slot < consumed.length; slot++) {
            if (consumed[slot] > 0) {
                inventory.removeItem(slot, consumed[slot]);
            }
        }
        return true;
    }

    protected Inventory getPlayerInventory(Player player) {
        return player == null ? null : player.getInventory();
    }

    private Optional<List<ItemStack>> readRequiredUpgrades(Level level, BaseMachineBE machine, ItemStack copierStack) {
        if (!copierStack.has(JustDireDataComponents.COPIED_MACHINE_DATA)) {
            return Optional.of(List.of());
        }

        return preparePaste(level, machine, copierStack).map(PreparedPaste::requiredUpgrades);
    }

    private boolean hasCompatibleType(BaseMachineBE machine, CompoundTag copiedData) {
        ResourceLocation machineTypeId = getBlockEntityTypeId(machine);
        if (machineTypeId == null) {
            return false;
        }

        return AdvancedMachineSettingsCopierData.readMachineType(copiedData)
                .map(machineTypeId::equals)
                .orElse(false);
    }

    private Optional<PreparedPaste> preparePaste(Level level, BaseMachineBE machine, ItemStack copierStack) {
        if (!copierStack.has(JustDireDataComponents.COPIED_MACHINE_DATA)) {
            return Optional.empty();
        }

        CompoundTag copiedData = copierStack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag();
        ResourceLocation machineType = getBlockEntityTypeId(machine);
        HolderLookup.Provider registries = getRegistryAccess(level);
        if (machineType == null || registries == null || !hasCompatibleType(machine, copiedData)) {
            return Optional.empty();
        }

        MachineSettingsCodec.PreparedSettings settings = null;
        if (copiedData.contains(AdvancedMachineSettingsCopierData.MACHINE_SETTINGS_KEY)) {
            Optional<MachineSettingsSnapshot> snapshot = MachineSettingsSnapshot.read(copiedData);
            Optional<MachineSettingsCodec> codec = getMachineSettingsCodec(machineType, machine);
            if (snapshot.isEmpty() || codec.isEmpty() || !snapshot.get().machineType().equals(machineType)
                    || !hasMatchingCommonFields(snapshot.get())) {
                return Optional.empty();
            }
            Optional<MachineSettingsCodec.PreparedSettings> decoded = codec.get().decode(snapshot.get().custom(), registries);
            if (decoded.isEmpty()) {
                return Optional.empty();
            }
            settings = decoded.get();
        }

        Optional<MachineUpgradeHandlers.PreparedHandlers> upgrades =
                MachineUpgradeHandlers.prepare(copiedData, machine, registries);
        if (upgrades.isEmpty()) {
            return Optional.empty();
        }

        AdvancedMachineSettingsCopierData.Masks masks = hasConfigurableIo(machine)
                ? AdvancedMachineSettingsCopierData.readMasks(copiedData).orElse(null)
                : null;
        return Optional.of(new PreparedPaste(settings, upgrades.get(), masks));
    }

    private static boolean hasMatchingCommonFields(MachineSettingsSnapshot snapshot) {
        CompoundTag custom = snapshot.custom();
        return custom.contains("tickSpeed", Tag.TAG_INT)
                && custom.contains("direction", Tag.TAG_INT)
                && custom.getInt("tickSpeed") == snapshot.tickSpeed()
                && custom.getInt("direction") == snapshot.direction();
    }

    private record PreparedPaste(MachineSettingsCodec.PreparedSettings settings,
                                 MachineUpgradeHandlers.PreparedHandlers upgrades,
                                 AdvancedMachineSettingsCopierData.Masks masks) {
        private List<ItemStack> requiredUpgrades() {
            return upgrades.requiredUpgrades();
        }

        private void apply(AdvancedMachineSettingsCopierItem copier, Level level, BlockEntity blockEntity,
                           BaseMachineBE machine, ItemStack copierStack) {
            copier.loadBaseSettings(level, blockEntity, copierStack);
            upgrades.apply(machine);
            if (settings != null) {
                settings.apply(machine);
                if (MachineSettingsCopier.getCopyFilter(copierStack)) {
                    settings.applyFilterSettings(machine);
                }
            }
            if (masks != null) {
                copier.setMasks(machine, masks.inputMask(), masks.outputMask());
            }
            machine.markDirtyClient();
        }
    }
}
