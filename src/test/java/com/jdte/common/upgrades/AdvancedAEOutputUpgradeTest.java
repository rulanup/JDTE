package com.jdte.common.upgrades;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedAEOutputUpgradeTest {

    @Test
    void upgradeTypeConfiguredCorrectly() {
        assertEquals("advanced_ae_output", UpgradeType.ADVANCED_AE_OUTPUT.getSerializedName());
        assertEquals(1, UpgradeType.ADVANCED_AE_OUTPUT.getMaxPerMachine());
        assertTrue(UpgradeType.ADVANCED_AE_OUTPUT.isAeOutputUpgrade());
        assertTrue(UpgradeType.AE_OUTPUT.isAeOutputUpgrade());
    }

    @Test
    void aeOutputConflictsBetweenStandardAndAdvanced() {
        AdvancedEnergyTransmitterBE machine = new AdvancedEnergyTransmitterBE(
                BlockPos.ZERO, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());
        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(machine);
        assertNotNull(handler);

        ItemStack standard = new ItemStack(JDTEItems.AE_OUTPUT_UPGRADE.get());
        ItemStack advanced = new ItemStack(JDTEItems.ADVANCED_AE_OUTPUT_UPGRADE.get());

        // Initially both are valid
        assertTrue(handler.isItemValid(0, standard));
        assertTrue(handler.isItemValid(0, advanced));

        // Insert standard into slot 0
        handler.setStackInSlot(0, standard);
        assertTrue(UpgradeHelper.hasAEOutputUpgrade(machine));
        assertFalse(UpgradeHelper.hasAdvancedAEOutputUpgrade(machine));
        assertTrue(ItemStack.isSameItemSameComponents(standard, UpgradeHelper.getAEOutputUpgrade(machine)));

        // Now advanced cannot be inserted into slot 1 due to conflict
        assertFalse(handler.isItemValid(1, advanced));

        // Remove standard and insert advanced into slot 0
        handler.setStackInSlot(0, advanced);
        assertTrue(UpgradeHelper.hasAEOutputUpgrade(machine));
        assertTrue(UpgradeHelper.hasAdvancedAEOutputUpgrade(machine));
        assertTrue(ItemStack.isSameItemSameComponents(advanced, UpgradeHelper.getAEOutputUpgrade(machine)));

        // Now standard cannot be inserted into slot 1 due to conflict
        assertFalse(handler.isItemValid(1, standard));
    }

    @Test
    void resourcesAndTextureExist() throws IOException {
        ClassLoader cl = getClass().getClassLoader();

        String recipePath = "data/jdte/recipe/advanced_ae_output_upgrade.json";
        try (InputStream in = cl.getResourceAsStream(recipePath)) {
            assertNotNull(in, "Recipe resource must exist on classpath: " + recipePath);
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("jdte:advanced_ae_output_upgrade", json.getAsJsonObject("result").get("id").getAsString());
            }
        }

        String modelPath = "assets/jdte/models/item/advanced_ae_output_upgrade.json";
        try (InputStream in = cl.getResourceAsStream(modelPath)) {
            assertNotNull(in, "Model resource must exist on classpath: " + modelPath);
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("jdte:item/advanced_ae_output_upgrade",
                        json.getAsJsonObject("textures").get("layer0").getAsString());
            }
        }

        String texturePath = "assets/jdte/textures/item/advanced_ae_output_upgrade.png";
        try (InputStream in = cl.getResourceAsStream(texturePath)) {
            assertNotNull(in, "Texture resource must exist on classpath: " + texturePath);
            assertNotNull(ImageIO.read(in), "Texture must be a valid readable image");
        }
    }
}
