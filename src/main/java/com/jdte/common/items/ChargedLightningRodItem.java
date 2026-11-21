package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

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
    public ChargedLightningRodItem(Block block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.jdte.charged_lightning_rod.energy",
                ChargedLightningRodBE.ENERGY_PER_STRIKE).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.charged_lightning_rod.natural").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("tooltip.jdte.charged_lightning_rod.storm").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("jdte.tooltip.charged_lightning_rod.overclock").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("jdte.tooltip.charged_lightning_rod.ultimate").withStyle(ChatFormatting.GRAY));
    }
}
