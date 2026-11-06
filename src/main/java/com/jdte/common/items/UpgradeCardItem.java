package com.jdte.common.items;

import net.minecraft.world.level.Level;
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

    public UpgradeCardItem(UpgradeType type) {
        super(new Properties().stacksTo(64));
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte." + type.getSerializedName()).withStyle(ChatFormatting.GRAY));
        if (type == UpgradeType.ULTIMATE_CAPACITY || type == UpgradeType.ULTIMATE_OVERCLOCK)
            tooltip.add(Component.translatable("tooltip.jdte.forge_mineral_upgrade_only").withStyle(ChatFormatting.DARK_GRAY));
        if (type == UpgradeType.AE_CRAFTING_READ) {
            boolean linked = com.jdte.common.integrations.ae2.AEWirelessLinkData.get(stack) != null;
            tooltip.add(Component.translatable(linked ? "tooltip.jdte.ae_crafting_read.linked" : "tooltip.jdte.ae_crafting_read.unlinked")
                    .withStyle(linked ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("tooltip.jdte.max", type.getMaxPerMachine()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
