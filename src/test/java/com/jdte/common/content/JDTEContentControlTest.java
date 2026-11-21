package com.jdte.common.content;

import net.minecraft.resources.Identifier;
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

        assertTrue(control.isBlockDisabled(Identifier.fromNamespaceAndPath("jdte", "greenhouse")));
        assertTrue(control.isRecipeDisabled(Identifier.fromNamespaceAndPath("jdte", "greenhouse")));
        assertTrue(control.isRecipeDisabled(Identifier.fromNamespaceAndPath("jdte", "greenhouse/wheat")));
        assertTrue(control.isRecipeDisabled(Identifier.fromNamespaceAndPath("minecraft", "barrel")));
        assertFalse(control.isBlockDisabled(Identifier.fromNamespaceAndPath("jdte", "bio_factory")));
        assertFalse(control.isRecipeDisabled(Identifier.fromNamespaceAndPath("jdte", "bio_factory")));
    }
}
