package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.containers.CraftingPatternContainer;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CraftingPatternItem extends Item {
    public CraftingPatternItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new CraftingPatternContainer(id, inventory, hand),
                    Component.translatable("jdte.screen.crafting_pattern.title")), buffer -> buffer.writeEnum(hand));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) { return stack.has(JDTEDataComponents.CRAFTING_PATTERN); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        CraftingPatternData pattern = stack.get(JDTEDataComponents.CRAFTING_PATTERN);
        tooltip.accept(Component.translatable("jdte.tooltip.crafting_pattern.open").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("jdte.tooltip.crafting_pattern.ghost").withStyle(ChatFormatting.DARK_GRAY));
        if (pattern == null) tooltip.accept(Component.translatable("jdte.tooltip.crafting_pattern.blank").withStyle(ChatFormatting.YELLOW));
        else {
            EssenceConversionUpgradeItem.appendPatternTooltip(tooltip, pattern, 1);
            if (context.level() != null && pattern.resolve(context.level()).isEmpty()) {
                tooltip.accept(Component.translatable("jdte.tooltip.essence_conversion.invalid", 1).withStyle(ChatFormatting.RED));
            }
        }
        tooltip.accept(Component.translatable("jdte.tooltip.crafting_pattern.anvil").withStyle(ChatFormatting.GOLD));
    }
}
