package com.jdte.client.renderers;

import com.jdte.common.greenhouse.GreenhouseMatrixRenderRegistry;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.function.IntFunction;

public final class GreenhouseCropRenderState extends BlockEntityRenderState {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    final BlockModelRenderState[] crops;
    Direction facing = Direction.NORTH;

    GreenhouseCropRenderState(int slots) {
        crops = new BlockModelRenderState[slots];
        Arrays.setAll(crops, index -> new BlockModelRenderState());
    }

    void extractCrops(BlockEntity machine, IntFunction<BlockState> displayCrop, BlockModelResolver resolver) {
        boolean visible = machine.getLevel() != null
                && GreenhouseMatrixRenderRegistry.shouldRender(machine.getLevel(), machine.getBlockPos());
        for (int slot = 0; slot < crops.length; slot++) {
            BlockState crop = visible ? displayCrop.apply(slot) : null;
            if (crop == null) crops[slot].clear();
            else resolver.update(crops[slot], crop, DISPLAY_CONTEXT);
        }
    }
}
