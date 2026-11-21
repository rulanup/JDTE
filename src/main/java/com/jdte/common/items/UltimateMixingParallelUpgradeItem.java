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

public class UltimateMixingParallelUpgradeItem extends Item {
    public UltimateMixingParallelUpgradeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.jdte.ultimate_mixing_parallel_upgrade")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.max", 1).withStyle(ChatFormatting.DARK_GRAY));
    }
}
