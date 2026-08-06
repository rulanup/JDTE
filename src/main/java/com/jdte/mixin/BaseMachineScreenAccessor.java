package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseMachineScreen.class)
public interface BaseMachineScreenAccessor {
    @Accessor("baseMachineBE")
    BaseMachineBE jdte$getBaseMachineBE();
}
