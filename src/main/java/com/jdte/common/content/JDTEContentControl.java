package com.jdte.common.content;

import com.jdte.setup.JDTEConfig;
import com.jdte.setup.config.ContentConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record JDTEContentControl(
        Collection<ContentSelection> disabledBlocks,
        Collection<ContentSelection> disabledRecipes,
        boolean greenhouseRecipeGenerationEnabled,
        boolean lootFabricatorRecipeGenerationEnabled,
        boolean bioFactoryRecipeGenerationEnabled,
        boolean timeAcceleratorEnabled,
        boolean timeFreezerEnabled) {

    private static volatile Snapshot CURRENT;

    public enum DynamicRecipeFamily {
        GREENHOUSE,
        LOOT_FABRICATOR,
        BIO_FACTORY
    }

    public JDTEContentControl {
        disabledBlocks = Set.copyOf(disabledBlocks);
        disabledRecipes = Set.copyOf(disabledRecipes);
    }

    /** Compatibility constructor for callers that only model dynamic recipe families. */
    public JDTEContentControl(Collection<ContentSelection> disabledBlocks,
                              Collection<ContentSelection> disabledRecipes,
                              boolean greenhouseRecipeGenerationEnabled,
                              boolean lootFabricatorRecipeGenerationEnabled,
                              boolean bioFactoryRecipeGenerationEnabled) {
        this(disabledBlocks, disabledRecipes, greenhouseRecipeGenerationEnabled,
                lootFabricatorRecipeGenerationEnabled, bioFactoryRecipeGenerationEnabled, true, true);
    }

    public static JDTEContentControl from(ContentConfig config) {
        return new JDTEContentControl(
                parseSelections(config.disabledBlocks.get()),
                parseSelections(config.disabledRecipes.get()),
                config.greenhouseRecipeGenerationEnabled.get(),
                config.lootFabricatorRecipeGenerationEnabled.get(),
                config.bioFactoryRecipeGenerationEnabled.get(),
                config.timeAcceleratorEnabled.get(),
                config.timeFreezerEnabled.get());
    }

    /**
     * Returns the configuration snapshot used by runtime guards.  Config values
     * can be reloaded while the client is at the title screen, so the snapshot
     * is rebuilt whenever one of its source values changes.
     */
    public static JDTEContentControl current() {
        ContentConfig config = JDTEConfig.COMMON.content;
        List<? extends String> blocks = config.disabledBlocks.get();
        List<? extends String> recipes = config.disabledRecipes.get();
        boolean greenhouse = config.greenhouseRecipeGenerationEnabled.get();
        boolean lootFabricator = config.lootFabricatorRecipeGenerationEnabled.get();
        boolean bioFactory = config.bioFactoryRecipeGenerationEnabled.get();
        boolean timeAccelerator = config.timeAcceleratorEnabled.get();
        boolean timeFreezer = config.timeFreezerEnabled.get();

        Snapshot snapshot = CURRENT;
        if (snapshot != null && snapshot.matches(blocks, recipes, greenhouse, lootFabricator, bioFactory,
                timeAccelerator, timeFreezer)) {
            return snapshot.control();
        }
        synchronized (JDTEContentControl.class) {
            snapshot = CURRENT;
            if (snapshot == null || !snapshot.matches(blocks, recipes, greenhouse, lootFabricator, bioFactory,
                    timeAccelerator, timeFreezer)) {
                snapshot = new Snapshot(copyValues(blocks), copyValues(recipes), greenhouse, lootFabricator, bioFactory,
                        timeAccelerator, timeFreezer,
                        new JDTEContentControl(parseSelections(copyValues(blocks)), parseSelections(copyValues(recipes)),
                                greenhouse, lootFabricator, bioFactory, timeAccelerator, timeFreezer));
                CURRENT = snapshot;
            }
            return snapshot.control();
        }
    }

    public boolean isBlockEnabled(ResourceLocation id) {
        return !isBlockDisabled(id);
    }

    public boolean isBlockEnabled(Block block) {
        return isBlockEnabled(BuiltInRegistries.BLOCK.getKey(block));
    }

    public boolean isItemEnabled(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        return !(stack.getItem() instanceof BlockItem blockItem) || isBlockEnabled(blockItem.getBlock());
    }

    public boolean isBlockDisabled(ResourceLocation id) {
        if (matchesAny(disabledBlocks, id) || controllerDisabledForPart(id)) {
            return true;
        }
        if (id == null || !"jdte".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return (!timeAcceleratorEnabled && isTimeAcceleratorBlock(path))
                || (!timeFreezerEnabled && isTimeFreezerBlock(path));
    }

    public boolean isRecipeEnabled(ResourceLocation id) {
        return !isRecipeDisabled(id);
    }

    public boolean isRecipeDisabled(ResourceLocation id) {
        return matchesAny(disabledRecipes, id)
                || matchesAny(disabledBlocks, id)
                || matchesGeneratedFamilyRoot(disabledRecipes, id)
                || isDisabledTimeRecipe(id);
    }

    public boolean isDynamicRecipeGenerationEnabled(DynamicRecipeFamily family) {
        boolean configured = switch (family) {
            case GREENHOUSE -> greenhouseRecipeGenerationEnabled;
            case LOOT_FABRICATOR -> lootFabricatorRecipeGenerationEnabled;
            case BIO_FACTORY -> bioFactoryRecipeGenerationEnabled;
        };
        if (!configured) {
            return false;
        }
        ResourceLocation root = switch (family) {
            case GREENHOUSE -> ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse");
            case LOOT_FABRICATOR -> ResourceLocation.fromNamespaceAndPath("jdte", "loot_fabricator");
            case BIO_FACTORY -> ResourceLocation.fromNamespaceAndPath("jdte", "bio_factory");
        };
        boolean blockAvailable = switch (family) {
            case GREENHOUSE -> isBlockEnabled(root)
                    || isBlockEnabled(ResourceLocation.fromNamespaceAndPath("jdte", "large_greenhouse"));
            case LOOT_FABRICATOR, BIO_FACTORY -> isBlockEnabled(root);
        };
        ResourceLocation generatedRoot = ResourceLocation.fromNamespaceAndPath("jdte", "jei/" + root.getPath());
        return blockAvailable && !matchesAny(disabledRecipes, root)
                && !matchesAny(disabledRecipes, generatedRoot);
    }

    private static boolean matchesAny(Collection<ContentSelection> selections, ResourceLocation id) {
        for (ContentSelection selection : selections) {
            if (selection.matches(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDisabledTimeRecipe(ResourceLocation id) {
        if (id == null || !"jdte".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return (!timeAcceleratorEnabled && isTimeAcceleratorBlock(path))
                || (!timeFreezerEnabled && isTimeFreezerBlock(path));
    }

    private static boolean isTimeAcceleratorBlock(String path) {
        return path.equals("basic_time_accelerator") || path.equals("advanced_time_accelerator")
                || path.equals("extended_time_accelerator");
    }

    private static boolean isTimeFreezerBlock(String path) {
        return path.equals("time_freezer") || path.equals("extended_time_freezer");
    }

    private static boolean matchesGeneratedFamilyRoot(Collection<ContentSelection> selections,
                                                      ResourceLocation id) {
        if (id == null || !"jdte".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        String family = path.startsWith("jei/greenhouse/") ? "greenhouse"
                : path.startsWith("jei/loot_fabricator/") ? "loot_fabricator"
                : path.startsWith("jei/bio_factory/") ? "bio_factory" : null;
        return family != null && matchesAny(selections,
                ResourceLocation.fromNamespaceAndPath("jdte", family));
    }

    private boolean controllerDisabledForPart(ResourceLocation id) {
        if (id == null || !"jdte".equals(id.getNamespace())) {
            return false;
        }
        return switch (id.getPath()) {
            case "large_greenhouse_part" -> matchesAny(disabledBlocks,
                    ResourceLocation.fromNamespaceAndPath("jdte", "large_greenhouse"));
            case "life_synthesis_part" -> matchesAny(disabledBlocks,
                    ResourceLocation.fromNamespaceAndPath("jdte", "life_synthesis_vat"));
            case "large_mineral_extractor_part" -> matchesAny(disabledBlocks,
                    ResourceLocation.fromNamespaceAndPath("jdte", "large_mineral_extractor"));
            default -> false;
        };
    }

    private static Set<ContentSelection> parseSelections(List<? extends String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .filter(ContentSelection::isValid)
                .map(ContentSelection::parse)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static List<String> copyValues(List<? extends String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private record Snapshot(List<String> blocks, List<String> recipes, boolean greenhouse,
                            boolean lootFabricator, boolean bioFactory, boolean timeAccelerator,
                            boolean timeFreezer, JDTEContentControl control) {
        private boolean matches(List<? extends String> blocks, List<? extends String> recipes, boolean greenhouse,
                                boolean lootFabricator, boolean bioFactory, boolean timeAccelerator,
                                boolean timeFreezer) {
            return this.blocks.equals(blocks) && this.recipes.equals(recipes)
                    && this.greenhouse == greenhouse && this.lootFabricator == lootFabricator
                    && this.bioFactory == bioFactory && this.timeAccelerator == timeAccelerator
                    && this.timeFreezer == timeFreezer;
        }
    }
}
