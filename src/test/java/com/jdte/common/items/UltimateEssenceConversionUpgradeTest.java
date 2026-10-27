package com.jdte.common.items;

import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UltimateEssenceConversionUpgradeTest {

    @Test
    void ultimateEssenceConversionUpgradeHasMaxIntegerPatternLimitAndCorrectUpgradeType() {
        EssenceConversionUpgradeItem item = JDTEItems.ULTIMATE_ESSENCE_CONVERSION_UPGRADE.get();
        assertNotNull(item);
        assertEquals(Integer.MAX_VALUE, item.patternLimit());
        assertEquals(UpgradeType.ESSENCE_CONVERSION, item.getType());
        assertTrue(JDTEItems.upgrades().contains(JDTEItems.ULTIMATE_ESSENCE_CONVERSION_UPGRADE));
    }

    private static Path resolveProjectFile(String relativePath) {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) {
            current = current.getParent();
        }
        assertNotNull(current, "Could not locate project root from test runtime path");
        return current.resolve(relativePath);
    }

    @Test
    void upgradesTagContainsUltimateEssenceConversionUpgrade() throws IOException {
        Path path = resolveProjectFile("src/main/resources/data/jdte/tags/item/upgrades.json");
        String content = Files.readString(path);
        assertTrue(content.contains("\"jdte:ultimate_essence_conversion_upgrade\""));
    }

    @Test
    void smithingRecipeExistsAndTransformsFromAdvancedUpgrade() throws IOException {
        Path path = resolveProjectFile("src/main/resources/data/jdte/recipe/ultimate_essence_conversion_upgrade.json");
        assertTrue(Files.exists(path));
        String content = Files.readString(path);
        assertTrue(content.contains("\"minecraft:smithing_transform\""));
        assertTrue(content.contains("\"jdte:advanced_essence_conversion_upgrade\""));
        assertTrue(content.contains("\"justdirethings:time_crystal\""));
        assertTrue(content.contains("\"jdte:ultimate_essence_conversion_upgrade\""));
    }

    @Test
    void itemModelExists() {
        Path path = resolveProjectFile("src/main/resources/assets/jdte/models/item/ultimate_essence_conversion_upgrade.json");
        assertTrue(Files.exists(path));
    }

    @Test
    void langFilesContainRequiredTranslations() throws IOException {
        Path zhCn = resolveProjectFile("src/main/resources/assets/jdte/lang/zh_cn.json");
        String zhContent = Files.readString(zhCn);
        assertTrue(zhContent.contains("\"item.jdte.ultimate_essence_conversion_upgrade\": \"顶级精华转化升级\""));
        assertTrue(zhContent.contains("\"jdte.tooltip.essence_conversion.more\""));

        Path enUs = resolveProjectFile("src/main/resources/assets/jdte/lang/en_us.json");
        String enContent = Files.readString(enUs);
        assertTrue(enContent.contains("\"item.jdte.ultimate_essence_conversion_upgrade\": \"Ultimate Essence Conversion Upgrade\""));
        assertTrue(enContent.contains("\"jdte.tooltip.essence_conversion.more\""));
    }

    @Test
    void tooltipTruncatesAndAppendsMoreWhenExceedingDisplayLimit() {
        EssenceConversionUpgradeItem item = JDTEItems.ULTIMATE_ESSENCE_CONVERSION_UPGRADE.get();
        ItemStack stack = new ItemStack(item);

        List<ItemStack> nineWheat = Collections.nCopies(9, new ItemStack(Items.WHEAT));
        List<CraftingPatternData> patterns = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            patterns.add(new CraftingPatternData(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "bread_" + i),
                    nineWheat,
                    new ItemStack(Items.BREAD, i + 1)
            ));
        }

        stack.set(JDTEDataComponents.ESSENCE_CONVERSION_PATTERNS.get(), patterns);

        List<Component> normalTooltip = new ArrayList<>();
        item.appendHoverText(stack, Item.TooltipContext.EMPTY, normalTooltip, TooltipFlag.NORMAL);

        // Max 5 patterns displayed under NORMAL flag, plus the 'more' component for remaining 3 patterns
        boolean foundMore = normalTooltip.stream().anyMatch(c ->
                c.getContents() instanceof TranslatableContents tc
                        && tc.getKey().equals("jdte.tooltip.essence_conversion.more")
                        && tc.getArgs().length == 1
                        && tc.getArgs()[0].equals(3));
        assertTrue(foundMore, "Should display '... and 3 more patterns' in normal tooltip");

        List<Component> advancedTooltip = new ArrayList<>();
        item.appendHoverText(stack, Item.TooltipContext.EMPTY, advancedTooltip, TooltipFlag.ADVANCED);

        // Under ADVANCED flag, limit is 10, so 8 patterns should all be displayed without 'more'
        boolean foundMoreAdvanced = advancedTooltip.stream().anyMatch(c ->
                c.getContents() instanceof TranslatableContents tc
                        && tc.getKey().equals("jdte.tooltip.essence_conversion.more"));
        assertTrue(!foundMoreAdvanced, "Under advanced tooltip, 8 patterns should not be truncated");
    }
}
