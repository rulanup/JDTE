package com.jdte.client.renderers;

import net.minecraft.world.item.Items;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ExtendedExperienceHolderBER extends RotatingMachineItemRenderer<ExtendedExperienceHolderBE> {
    public ExtendedExperienceHolderBER(BlockEntityRendererProvider.Context context) {
        super(context, Items.EXPERIENCE_BOTTLE);
    }
}
