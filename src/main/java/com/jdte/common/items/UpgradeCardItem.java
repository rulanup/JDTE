package com.jdte.common.items;

import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class UpgradeCardItem extends Item {
    private final UpgradeType type;

    public UpgradeCardItem(UpgradeType type) {
        super(new Properties().stacksTo(64));
        this.type = type;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte." + type.getSerializedName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.jdte.max", type.getMaxPerMachine()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
