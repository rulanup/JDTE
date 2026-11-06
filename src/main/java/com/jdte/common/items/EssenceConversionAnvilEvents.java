package com.jdte.common.items;

import com.jdte.JDTE;


import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.AnvilUpdateEvent;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = JDTE.MODID)
public final class EssenceConversionAnvilEvents {
    private EssenceConversionAnvilEvents() {}

    @SubscribeEvent
    public static void bindPattern(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        if (!(left.getItem() instanceof EssenceConversionUpgradeItem upgrade)
                || !(event.getRight().getItem() instanceof CraftingPatternItem)) return;
        CraftingPatternData pattern = CraftingPatternItem.getPattern(event.getRight());
        var patterns = new ArrayList<>(EssenceConversionUpgradeItem.patterns(left));
        if (event.getPlayer()==null || left.getCount() != 1 || pattern == null || pattern.resolve(event.getPlayer().level()).isEmpty()
                || patterns.size() >= upgrade.patternLimit()
                || patterns.stream().anyMatch(pattern::equals)) {
            event.setCanceled(true);
            return;
        }
        patterns.add(pattern);
        ItemStack output = left.copyWithCount(1);
        EssenceConversionUpgradeItem.setPatterns(output,java.util.List.copyOf(patterns));
        if (event.getName() != null && !event.getName().isBlank()
                && !event.getName().equals(left.getHoverName().getString())) {
            output.setHoverName(Component.literal(event.getName()));
        }
        event.setOutput(output);
        event.setMaterialCost(1);
        event.setCost(1);
    }
}
