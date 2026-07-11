package com.jdte.common.jei.lootfabricator;

import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.jdte.client.LootFabricatorLootClientCache;
import com.jdte.common.utils.LootDropInfo;

import java.util.Comparator;
import java.util.List;

public record LootFabricatorJeiRecipe(ItemStack spawnEgg, List<DisplayDrop> possibleDrops, int outputPage) {
    public record DisplayDrop(ItemStack stack, int minCount, int maxCount, String chanceLabel) { }
    public ResourceLocation id() {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(spawnEgg.getItem());
        return ResourceLocation.fromNamespaceAndPath("jdte", "jei/loot_fabricator/" + itemId.getNamespace() + "/" + itemId.getPath() + "/" + outputPage);
    }

    public static List<LootFabricatorJeiRecipe> getRecipes() {
        return LootFabricatorLootClientCache.get().entrySet().stream()
                .map(entry -> {
                    ItemStack egg = BuiltInRegistries.ITEM.getOptional(entry.getKey()).map(ItemStack::new).orElse(ItemStack.EMPTY);
                    List<DisplayDrop> drops = entry.getValue().stream()
                            .map(drop -> toDisplayDrop(drop))
                            .flatMap(java.util.Optional::stream)
                            .toList();
                    return new LootFabricatorJeiRecipe(egg, drops, 0);
                })
                .filter(recipe -> !recipe.spawnEgg().isEmpty() && !recipe.possibleDrops().isEmpty())
                .flatMap(recipe -> java.util.stream.IntStream.range(0, (recipe.possibleDrops().size() + 15) / 16)
                        .mapToObj(page -> new LootFabricatorJeiRecipe(recipe.spawnEgg(),
                                List.copyOf(recipe.possibleDrops().subList(page * 16,
                                        Math.min(recipe.possibleDrops().size(), (page + 1) * 16))), page)))
                .sorted(Comparator.comparing(recipe -> BuiltInRegistries.ITEM.getKey(recipe.spawnEgg().getItem()).toString()))
                .toList();
    }

    private static java.util.Optional<DisplayDrop> toDisplayDrop(LootDropInfo drop) {
        return BuiltInRegistries.ITEM.getOptional(drop.itemId()).map(item -> {
            ItemStack stack = new ItemStack(item, Math.clamp(drop.maxCount(), 1, item.getDefaultMaxStackSize()));
            return new DisplayDrop(stack, drop.minCount(), drop.maxCount(), drop.chanceLabel());
        });
    }

    public static List<ItemStack> getMachines() {
        return List.of(new ItemStack(JDTEBlocks.LOOT_FABRICATOR.get()));
    }
}
