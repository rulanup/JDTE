package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

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
