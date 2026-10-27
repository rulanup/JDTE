package com.jdte.common.items;

import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public final class EssenceConversionUpgradeItem extends UpgradeCardItem {
    private final int patternLimit;

    public EssenceConversionUpgradeItem(int patternLimit) {
        super(UpgradeType.ESSENCE_CONVERSION);
        this.patternLimit = patternLimit;
    }

    public int patternLimit() { return patternLimit; }

    public static List<CraftingPatternData> patterns(ItemStack stack) {
        if (!(stack.getItem() instanceof EssenceConversionUpgradeItem item)) return List.of();
        List<CraftingPatternData> stored = stack.getOrDefault(JDTEDataComponents.ESSENCE_CONVERSION_PATTERNS.get(), List.of());
        return stored.stream().limit(item.patternLimit()).toList();
    }

    @Override
    public boolean isFoil(ItemStack stack) { return !patterns(stack).isEmpty(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<CraftingPatternData> patterns = patterns(stack);
        tooltip.add(Component.translatable(patterns.isEmpty() ? "jdte.tooltip.essence_conversion.automatic"
                : "jdte.tooltip.essence_conversion.configured").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.patterns", patterns.size(), patternLimit)
                .withStyle(ChatFormatting.AQUA));
        int maxDisplay = flag.isAdvanced() ? 10 : 5;
        int displayCount = Math.min(patterns.size(), maxDisplay);
        for (int index = 0; index < displayCount; index++) {
            appendPatternTooltip(tooltip, patterns.get(index), index + 1);
            if (context.level() != null && patterns.get(index).resolve(context.level()).isEmpty()) {
                tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.invalid", index + 1).withStyle(ChatFormatting.RED));
            }
        }
        if (patterns.size() > displayCount) {
            tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.more", patterns.size() - displayCount)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.anvil").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.reserved").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.exclusive").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static void appendPatternTooltip(List<Component> tooltip, CraftingPatternData pattern, int index) {
        tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.pattern", index,
                pattern.result().getHoverName(), pattern.result().getCount()).withStyle(ChatFormatting.GREEN));
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack ingredient : pattern.ingredients()) {
            if (ingredient.isEmpty()) continue;
            ItemStack existing = inputs.stream().filter(stack -> ItemStack.isSameItemSameComponents(stack, ingredient))
                    .findFirst().orElse(null);
            if (existing == null) inputs.add(ingredient.copy());
            else existing.grow(1);
        }
        for (ItemStack input : inputs) tooltip.add(Component.translatable("jdte.tooltip.essence_conversion.ingredient",
                input.getHoverName(), input.getCount()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
