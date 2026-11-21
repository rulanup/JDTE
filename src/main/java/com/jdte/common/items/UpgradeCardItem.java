package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.upgrades.UpgradeCardInsertionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class UpgradeCardItem extends Item {
    private final UpgradeType type;

    public UpgradeCardItem(Properties properties, UpgradeType type) {
        super(properties.stacksTo(64));
        this.type = type;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeCardInsertionHelper.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        String tooltipKey = type == UpgradeType.THUNDERSTORM ? "jdte.tooltip.thunderstorm_upgrade"
                : "tooltip.jdte." + type.getSerializedName();
        tooltip.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.max", type.getMaxPerMachine()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
