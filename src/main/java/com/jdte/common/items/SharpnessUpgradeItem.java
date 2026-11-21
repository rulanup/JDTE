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

public class SharpnessUpgradeItem extends Item {
    public static final int MAX_COUNT = 6;
    public static final int DAMAGE_PER_UPGRADE = 5;

    public SharpnessUpgradeItem(Properties properties) {
        super(properties.stacksTo(MAX_COUNT));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.jdte.sharpness_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.max", MAX_COUNT).withStyle(ChatFormatting.DARK_GRAY));
    }
}
