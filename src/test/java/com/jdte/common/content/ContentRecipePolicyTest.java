package com.jdte.common.content;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentRecipePolicyTest {
    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    @Test
    void dynamicRecipeSwitchesAreIndependentAndFilterTheirRecipeFamilies() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(), List.of(), false, true, false);

        assertFalse(control.isDynamicRecipeGenerationEnabled(JDTEContentControl.DynamicRecipeFamily.GREENHOUSE));
        assertTrue(control.isDynamicRecipeGenerationEnabled(JDTEContentControl.DynamicRecipeFamily.LOOT_FABRICATOR));
        assertFalse(control.isDynamicRecipeGenerationEnabled(JDTEContentControl.DynamicRecipeFamily.BIO_FACTORY));
        // The generation switches only control the expensive dynamic JEI enumeration;
        // a pack may still provide a custom greenhouse recipe through a data pack.
        assertTrue(control.isRecipeEnabled(id("jdte:greenhouse/wheat")));
        assertTrue(control.isRecipeEnabled(id("jdte:loot_fabricator")));
        assertTrue(control.isRecipeEnabled(id("jdte:bio_factory/cow")));
    }

    @Test
    void disabledRecipeIdsMatchDirectoryDescendantsWithoutMatchingSimilarNames() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(), List.of(ContentSelection.parse("jdte:machine")), true, true, true);

        assertFalse(control.isRecipeEnabled(id("jdte:machine")));
        assertFalse(control.isRecipeEnabled(id("jdte:machine/variant")));
        assertTrue(control.isRecipeEnabled(id("jdte:machine2")));
    }

    @Test
    void disablingAMultiblockControllerAlsoDisablesItsStructurePart() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(ContentSelection.parse("jdte:large_greenhouse")), List.of(),
                true, true, true);

        assertFalse(control.isBlockEnabled(id("jdte:large_greenhouse_part")));
    }

    @Test
    void disablingARecipeFamilyAlsoHidesItsGeneratedJeiEntries() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(), List.of(ContentSelection.parse("greenhouse")),
                true, true, true);

        assertFalse(control.isRecipeEnabled(id("jdte:jei/greenhouse/minecraft/wheat")));
        assertTrue(control.isRecipeEnabled(id("jdte:jei/bio_factory/minecraft/cow")));
    }

    @Test
    void aDisabledFamilyRootSkipsExpensiveDynamicGeneration() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(), List.of(ContentSelection.parse("bio_factory")),
                true, true, true);

        assertFalse(control.isDynamicRecipeGenerationEnabled(
                JDTEContentControl.DynamicRecipeFamily.BIO_FACTORY));
        assertTrue(control.isDynamicRecipeGenerationEnabled(
                JDTEContentControl.DynamicRecipeFamily.GREENHOUSE));
    }

    @Test
    void disablingOnlyTheSmallGreenhouseKeepsLargeGreenhouseJeiGeneration() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(ContentSelection.parse("greenhouse")), List.of(),
                true, true, true);

        assertTrue(control.isDynamicRecipeGenerationEnabled(
                JDTEContentControl.DynamicRecipeFamily.GREENHOUSE));
    }

    @Test
    void disablingTimeFamiliesBlocksTheirRegistryIdsAndBundledRecipes() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(), List.of(), true, true, true, false, false);

        assertFalse(control.isBlockEnabled(id("jdte:basic_time_accelerator")));
        assertFalse(control.isRecipeEnabled(id("jdte:advanced_time_accelerator")));
        assertFalse(control.isBlockEnabled(id("jdte:extended_time_freezer")));
        assertTrue(control.isBlockEnabled(id("jdte:extended_clicker")));
    }
}
