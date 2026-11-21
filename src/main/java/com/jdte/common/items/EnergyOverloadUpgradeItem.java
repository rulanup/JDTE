package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.upgrades.UpgradeCardInsertionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/**
 * 能量过载升级：仅能量传输器可用。放置后，取消能量传输器的一次性传输限制。
 */
public class EnergyOverloadUpgradeItem extends Item {
    public static final int MAX_LEVEL = 1;

    public EnergyOverloadUpgradeItem(Properties properties) {
        super(properties.stacksTo(MAX_LEVEL));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.jdte.energy_overload_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.max", MAX_LEVEL).withStyle(ChatFormatting.DARK_GRAY));
    }
}
