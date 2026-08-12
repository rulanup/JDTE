package com.jdte.common.items;

import com.jdte.common.integrations.ae2.AEOutputNetwork;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AEOutputUpgradeItem extends UpgradeCardItem {
    public AEOutputUpgradeItem() {
        super(UpgradeType.AE_OUTPUT);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(AEOutputNetwork.isLinked(stack)
                        ? "tooltip.jdte.ae_output.linked"
                        : "tooltip.jdte.ae_output.unlinked")
                .withStyle(AEOutputNetwork.isLinked(stack) ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
