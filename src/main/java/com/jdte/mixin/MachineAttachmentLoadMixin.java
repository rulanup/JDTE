package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.utils.LegacyMachineAttachmentHelper;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AttachmentHolder.class)
public abstract class MachineAttachmentLoadMixin {
    @ModifyVariable(method = "deserializeAttachments", at = @At("HEAD"), argsOnly = true)
    private ValueInput jdte$readLegacyAttachments(ValueInput input) {
        if (!((Object) this instanceof BaseMachineBE) || !(input instanceof TagValueInputAccessor view)) return input;
        var original = view.jdte$input();
        var converted = LegacyMachineAttachmentHelper.upgrade(original);
        return converted == original ? input : TagValueInput.create(view.jdte$reporter(), input.lookup(), converted);
    }
}
