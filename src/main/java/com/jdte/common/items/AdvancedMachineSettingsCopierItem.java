package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdvancedMachineSettingsCopierItem extends MachineSettingsCopier {
    public AdvancedMachineSettingsCopierItem() {
        super();
    }

    @Override
    public void saveSettings(Level level, BlockEntity blockEntity, ItemStack stack) {
        super.saveSettings(level, blockEntity, stack);

        CompoundTag copiedData = stack.has(JustDireDataComponents.COPIED_MACHINE_DATA)
                ? stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag()
                : new CompoundTag();
        if (blockEntity instanceof BaseMachineBE machine && AutoIoConfigHelper.hasConfigurableIo(machine)) {
            AdvancedMachineSettingsCopierData.write(copiedData,
                    AutoIoConfigHelper.getInputMask(machine),
                    AutoIoConfigHelper.getOutputMask(machine));
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
        super.loadSettings(level, blockEntity, stack);

        if (!(blockEntity instanceof BaseMachineBE machine)
                || !AutoIoConfigHelper.hasConfigurableIo(machine)
                || !stack.has(JustDireDataComponents.COPIED_MACHINE_DATA)) {
            return;
        }

        CompoundTag copiedData = stack.get(JustDireDataComponents.COPIED_MACHINE_DATA).copyTag();
        AdvancedMachineSettingsCopierData.read(copiedData)
                .ifPresent(masks -> AutoIoConfigHelper.setMasks(machine, masks.inputMask(), masks.outputMask()));
    }
}
