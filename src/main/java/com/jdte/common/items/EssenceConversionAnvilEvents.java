package com.jdte.common.items;

import com.jdte.JDTE;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = JDTE.MODID)
public final class EssenceConversionAnvilEvents {
    private EssenceConversionAnvilEvents() {}

    @SubscribeEvent
    public static void bindPattern(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        if (!(left.getItem() instanceof EssenceConversionUpgradeItem upgrade)
                || !(event.getRight().getItem() instanceof CraftingPatternItem)) return;
        CraftingPatternData pattern = event.getRight().get(JDTEDataComponents.CRAFTING_PATTERN);
        var patterns = new ArrayList<>(EssenceConversionUpgradeItem.patterns(left));
        if (left.getCount() != 1 || pattern == null || pattern.resolve(event.getPlayer().level()).isEmpty()
                || patterns.size() >= upgrade.patternLimit()
                || patterns.stream().anyMatch(pattern::equals)) {
            event.setCanceled(true);
            return;
        }
        patterns.add(pattern);
        ItemStack output = left.copyWithCount(1);
        output.set(JDTEDataComponents.ESSENCE_CONVERSION_PATTERNS, java.util.List.copyOf(patterns));
        if (event.getName() != null && !event.getName().isBlank()
                && !event.getName().equals(left.getHoverName().getString())) {
            output.set(DataComponents.CUSTOM_NAME, Component.literal(event.getName()));
        }
        event.setOutput(output);
        event.setMaterialCost(1);
        event.setCost(1);
    }
}
