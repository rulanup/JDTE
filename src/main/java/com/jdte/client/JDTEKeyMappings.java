package com.jdte.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class JDTEKeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath("jdte", "main"));
    public static final KeyMapping WRENCH_AREA_MODIFIER = new KeyMapping(
            "key.jdte.wrench_area_modifier",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );
    public static final KeyMapping LARGE_POCKET_GENERATOR = new KeyMapping(
            "key.jdte.large_pocket_generator",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );
    public static final KeyMapping LARGE_POTION_CANISTER = new KeyMapping(
            "key.jdte.large_potion_canister",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            CATEGORY
    );
    public static final KeyMapping LARGE_FUEL_CANISTER = new KeyMapping(
            "key.jdte.large_fuel_canister",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            CATEGORY
    );
}
