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
 * 能量酿造升级：仅高级炼药机可用。插入后酿造燃料改由 FE 支付
 * （每次充能替代一个烈焰粉，费率可配置），无需再外接烈焰粉。
 */
public class EnergyBrewingUpgradeItem extends Item {
    public static final int MAX_LEVEL = 1;

    public EnergyBrewingUpgradeItem(Properties properties) {
        super(properties.stacksTo(MAX_LEVEL));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.jdte.energy_brewing_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.max", MAX_LEVEL).withStyle(ChatFormatting.DARK_GRAY));
    }
}
