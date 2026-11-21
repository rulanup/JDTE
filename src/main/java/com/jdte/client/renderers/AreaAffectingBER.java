package com.jdte.client.renderers;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.client.blockentityrenders.baseber.AreaAffectingBER.AreaAffectingRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class AreaAffectingBER<T extends BlockEntity, S extends AreaAffectingRenderState>
        extends com.direwolf20.justdirethings.client.blockentityrenders.baseber.AreaAffectingBER<T, S> {
    public AreaAffectingBER(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        if (blockEntity instanceof AreaAffectingBE areaAffectingBE) {
            BlockPos blockPos = blockEntity.getBlockPos();
            AABB bounds = areaAffectingBE.getAABB(BlockPos.ZERO).move(blockPos)
                    .minmax(new AABB(blockPos));
            if (areaAffectingBE.getAreaAffectingData().xRadius > 0
                    || areaAffectingBE.getAreaAffectingData().yRadius > 0
                    || areaAffectingBE.getAreaAffectingData().zRadius > 0) {
                bounds = bounds.minmax(areaAffectingBE.getAABBOffsetOnly(BlockPos.ZERO).move(blockPos));
            }
            return bounds.inflate(1.0D);
        }
        return new AABB(blockEntity.getBlockPos()).inflate(1.0D);
    }
}
