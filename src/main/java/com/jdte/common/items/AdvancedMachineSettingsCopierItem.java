package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.items.machinesettings.MachineUpgradeHandlers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
        if (!stack.has(JustDireDataComponents.COPIED_MACHINE_DATA)
                || !hasCompatibleType(machine, stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag())) {
            return InteractionResult.PASS;
        }

        if (!consumeUpgradeCost(context.getPlayer(), level, machine, stack)) {
            return InteractionResult.PASS;
        }

        return super.useOn(context);
    }

    @Override
    public void saveSettings(Level level, BlockEntity blockEntity, ItemStack stack) {
        saveBaseSettings(level, blockEntity, stack);

        CompoundTag copiedData = stack.has(JustDireDataComponents.COPIED_MACHINE_DATA)
                ? stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag()
                : new CompoundTag();

        if (blockEntity instanceof BaseMachineBE machine) {
            ResourceLocation machineTypeId = getBlockEntityTypeId(machine);
            if (machineTypeId != null) {
                AdvancedMachineSettingsCopierData.writeMachineType(copiedData, machineTypeId);
            } else {
                AdvancedMachineSettingsCopierData.remove(copiedData);
            }

            saveUpgrades(level, machine, copiedData);

            if (hasConfigurableIo(machine)) {
                AdvancedMachineSettingsCopierData.writeMasks(copiedData,
                        getInputMask(machine),
                        getOutputMask(machine));
            } else {
                AdvancedMachineSettingsCopierData.clearMasks(copiedData);
            }
        } else {
            AdvancedMachineSettingsCopierData.remove(copiedData);
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

        CompoundTag copiedData = stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag();
        if (!hasCompatibleType(machine, copiedData)) {
            return;
        }

        loadBaseSettings(level, blockEntity, stack);
        loadUpgrades(level, machine, copiedData);
        if (!hasConfigurableIo(machine)) {
            return;
        }

        AdvancedMachineSettingsCopierData.readMasks(copiedData)
                .ifPresent(masks -> setMasks(machine, masks.inputMask(), masks.outputMask()));
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

        List<ItemStack> required = requiredResult.get();
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

        CompoundTag copiedData = copierStack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag();
        HolderLookup.Provider registries = getRegistryAccess(level);
        if (registries == null) {
            return Optional.empty();
        }

        return MachineUpgradeHandlers.prepare(copiedData, machine, registries)
                .map(MachineUpgradeHandlers.PreparedHandlers::requiredUpgrades);
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
}
