package com.jdte.setup;

import com.jdte.common.content.JDTEContentControl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentConfigTest {
    @Test
    void defaultsKeepExistingContentEnabledAndListsEmpty() {
        assertTrue(JDTEConfig.COMMON.content.greenhouseRecipeGenerationEnabled.get());
        assertTrue(JDTEConfig.COMMON.content.lootFabricatorRecipeGenerationEnabled.get());
        assertTrue(JDTEConfig.COMMON.content.bioFactoryRecipeGenerationEnabled.get());
        assertTrue(JDTEConfig.COMMON.content.disabledBlocks.get().isEmpty());
        assertTrue(JDTEConfig.COMMON.content.disabledRecipes.get().isEmpty());
        assertTrue(JDTEConfig.COMMON.content.timeAcceleratorEnabled.get());
        assertTrue(JDTEConfig.COMMON.content.timeFreezerEnabled.get());
        assertTrue(JDTEContentControl.current().isDynamicRecipeGenerationEnabled(
                JDTEContentControl.DynamicRecipeFamily.GREENHOUSE));
        assertEquals(List.of("jdte", "content", "disabledBlocks"),
                JDTEConfig.COMMON.content.disabledBlocks.getPath());
    }
}
