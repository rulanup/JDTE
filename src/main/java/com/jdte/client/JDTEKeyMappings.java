package com.jdte.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class JDTEKeyMappings {
    public static final KeyMapping WRENCH_AREA_MODIFIER = new KeyMapping(
            "key.jdte.wrench_area_modifier",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.jdte"
    );
}
