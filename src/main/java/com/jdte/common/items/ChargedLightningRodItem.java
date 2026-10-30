package com.jdte.common.items;

import com.jdte.common.blockentities.ChargedLightningRodBE;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ChargedLightningRodItem extends BlockItem {
    public ChargedLightningRodItem(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.charged_lightning_rod.energy",
                ChargedLightningRodBE.ENERGY_PER_STRIKE).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.jdte.charged_lightning_rod.natural").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.jdte.charged_lightning_rod.storm").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("jdte.tooltip.charged_lightning_rod.overclock").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jdte.tooltip.charged_lightning_rod.ultimate").withStyle(ChatFormatting.GRAY));
    }
}
