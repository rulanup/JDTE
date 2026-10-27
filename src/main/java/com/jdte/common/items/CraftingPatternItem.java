package com.jdte.common.items;

import com.jdte.common.containers.CraftingPatternContainer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CraftingPatternItem extends Item {
    public CraftingPatternItem() { super(new Properties().stacksTo(1)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,new SimpleMenuProvider(
                    (id, inventory, owner) -> new CraftingPatternContainer(id, inventory, hand),
                    Component.translatable("jdte.screen.crafting_pattern.title")), buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    public static CraftingPatternData getPattern(ItemStack stack) {
        return stack.hasTag()?CraftingPatternData.load(stack.getTag().getCompound("JDTECraftingPattern")).orElse(null):null;
    }
    public static void setPattern(ItemStack stack,CraftingPatternData pattern) {
        if(pattern==null)stack.getOrCreateTag().remove("JDTECraftingPattern");else stack.getOrCreateTag().put("JDTECraftingPattern",pattern.save());
    }
    @Override
    public boolean isFoil(ItemStack stack) { return getPattern(stack) != null; }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CraftingPatternData pattern = getPattern(stack);
        tooltip.add(Component.translatable("jdte.tooltip.crafting_pattern.open").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jdte.tooltip.crafting_pattern.ghost").withStyle(ChatFormatting.DARK_GRAY));
        if (pattern == null) tooltip.add(Component.translatable("jdte.tooltip.crafting_pattern.blank").withStyle(ChatFormatting.YELLOW));
        else {
            EssenceConversionUpgradeItem.appendPatternTooltip(tooltip, pattern, 1);
            if (level != null && pattern.resolve(level).isEmpty()) {
                tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.invalid", 1).withStyle(ChatFormatting.RED));
            }
        }
        tooltip.add(Component.translatable("jdte.tooltip.crafting_pattern.anvil").withStyle(ChatFormatting.GOLD));
    }
}
