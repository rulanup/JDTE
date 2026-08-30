package com.jdte.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 修复护符：放在背包中即可持续修复全部受损装备与工具（含盔甲与副手），
 * 速度远快于等价交换的修复护符。速率与周期可在 jdte.repairTalisman 配置中调整。
 */
public class RepairTalismanItem extends Item {
    public RepairTalismanItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.repair_talisman").withStyle(ChatFormatting.GRAY));
    }
}
