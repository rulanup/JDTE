package com.jdte.client;

import com.mojang.blaze3d.platform.InputConstants;
/** Resolved only on the physical client after the optional Curios presence check. */
public final class LargePocketGeneratorCuriosKeys {
    private LargePocketGeneratorCuriosKeys() {}
    public static void consumeConflictingOpen(InputConstants.Key pressed) {
        var binding=top.theillusivec4.curios.client.KeyRegistry.openCurios;
        if(binding!=null&&binding.getKey().equals(pressed))while(binding.consumeClick()){}
    }
}
