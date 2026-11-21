package com.jdte.client.renderers;

import com.direwolf20.justdirethings.client.blockentityrenders.baseber.AreaAffectingBER.AreaAffectingRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TimeAcceleratorBER<T extends BlockEntity> extends AreaAffectingBER<T, AreaAffectingRenderState> {
    public TimeAcceleratorBER(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
