package com.jdte.common.items;

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
    public UltimateMixingParallelUpgradeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.ultimate_mixing_parallel_upgrade")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.jdte.max", 1).withStyle(ChatFormatting.DARK_GRAY));
    }
}
