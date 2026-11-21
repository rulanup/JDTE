package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.integrations.ae2.AEExtractionNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class AEExtractionUpgradeItem extends Item {
    public AEExtractionUpgradeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        boolean linked = AEExtractionNetwork.isLinked(stack);
        tooltip.accept(Component.translatable(linked
                        ? "tooltip.jdte.ae_extraction.linked"
                        : "tooltip.jdte.ae_extraction.unlinked")
                .withStyle(linked ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
