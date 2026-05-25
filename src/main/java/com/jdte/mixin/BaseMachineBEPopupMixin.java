package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradePositionHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseMachineBE.class)
public abstract class BaseMachineBEPopupMixin implements UpgradePositionHolder {
    @Unique private int jdte$popupX = 0;
    @Unique private int jdte$popupY = 0;
    @Unique private boolean jdte$popupOpen = false;

    @Override
    public int jdte$getPopupX() { return jdte$popupX; }

    @Override
    public int jdte$getPopupY() { return jdte$popupY; }

    @Override
    public boolean jdte$isPopupOpen() { return jdte$popupOpen; }

    @Override
    public void jdte$setPopupX(int x) { jdte$popupX = x; }

    @Override
    public void jdte$setPopupY(int y) { jdte$popupY = y; }

    @Override
    public void jdte$setPopupOpen(boolean open) { jdte$popupOpen = open; }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void jdte$savePopup(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        tag.putInt("jdte$popupX", jdte$popupX);
        tag.putInt("jdte$popupY", jdte$popupY);
        tag.putBoolean("jdte$popupOpen", jdte$popupOpen);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void jdte$loadPopup(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (tag.contains("jdte$popupX")) jdte$popupX = tag.getInt("jdte$popupX");
        if (tag.contains("jdte$popupY")) jdte$popupY = tag.getInt("jdte$popupY");
        if (tag.contains("jdte$popupOpen")) jdte$popupOpen = tag.getBoolean("jdte$popupOpen");
    }
}
