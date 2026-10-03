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

/**
 * 混合升级：仅流体混合器可用。
 * 插入后启用并行混合模式，根据当前输入的原料数量和输出/能量空间一次性批量混合。
 */
public class MixingUpgradeItem extends Item {
    public static final int MAX_LEVEL = 1;

    public MixingUpgradeItem() {
        super(new Item.Properties().stacksTo(MAX_LEVEL));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.mixing_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.jdte.max", MAX_LEVEL).withStyle(ChatFormatting.DARK_GRAY));
    }
}
