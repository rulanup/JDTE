package com.jdte.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SharpnessUpgradeItem extends Item {
    public static final int MAX_COUNT = 6;
    public static final int DAMAGE_PER_UPGRADE = 5;

    public SharpnessUpgradeItem() {
        super(new Item.Properties().stacksTo(MAX_COUNT));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.sharpness_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.jdte.max", MAX_COUNT).withStyle(ChatFormatting.DARK_GRAY));
    }
}
