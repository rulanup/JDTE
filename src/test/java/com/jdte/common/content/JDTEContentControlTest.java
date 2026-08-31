package com.jdte.common.content;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDTEContentControlTest {
    @Test
    void disabledBlocksAlsoDisableMatchingRecipesAndDescendants() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(ContentSelection.parse("greenhouse")),
                List.of(ContentSelection.parse("minecraft:barrel")),
                true,
                true,
                true
        );

        assertTrue(control.isBlockDisabled(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse")));
        assertTrue(control.isRecipeDisabled(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse")));
        assertTrue(control.isRecipeDisabled(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse/wheat")));
        assertTrue(control.isRecipeDisabled(ResourceLocation.fromNamespaceAndPath("minecraft", "barrel")));
        assertFalse(control.isBlockDisabled(ResourceLocation.fromNamespaceAndPath("jdte", "bio_factory")));
        assertFalse(control.isRecipeDisabled(ResourceLocation.fromNamespaceAndPath("jdte", "bio_factory")));
    }
}
