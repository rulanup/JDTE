package com.jdte.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GreenhouseMatrixQuickInstallUpgradeItem extends Item {
    public GreenhouseMatrixQuickInstallUpgradeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.greenhouse_matrix_quick_install_upgrade")
                .withStyle(ChatFormatting.GRAY));
    }
}
