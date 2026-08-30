package com.jdte.common.items;

import com.jdte.common.integrations.ae2.AEExtractionNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class AEExtractionUpgradeItem extends Item {
    public AEExtractionUpgradeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        boolean linked = AEExtractionNetwork.isLinked(stack);
        tooltip.add(Component.translatable(linked
                        ? "tooltip.jdte.ae_extraction.linked"
                        : "tooltip.jdte.ae_extraction.unlinked")
                .withStyle(linked ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
