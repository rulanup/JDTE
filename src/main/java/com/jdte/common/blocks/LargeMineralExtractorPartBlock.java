package com.jdte.common.blocks;

import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class LargeMineralExtractorPartBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<HorizontalPart> X = EnumProperty.create("x_part", HorizontalPart.class);
    public static final EnumProperty<DepthPart> DEPTH = EnumProperty.create("depth_part", DepthPart.class);
    public static final EnumProperty<LayerPart> LAYER = EnumProperty.create("layer_part", LayerPart.class);

    public LargeMineralExtractorPartBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(4.0F).noOcclusion()
                .isRedstoneConductor((state, level, pos) -> false));
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(X, HorizontalPart.CENTER)
                .setValue(DEPTH, DepthPart.FRONT)
                .setValue(LAYER, LayerPart.BASE));
    }

    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.jdte.common.blockentities.LargeMineralExtractorPartBE(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, X, DEPTH, LAYER);
    }

    public BlockPos getControllerPos(BlockPos pos, BlockState state) {
        return LargeMineralExtractorStructure.controllerPosition(pos, state);
    }

    public LargeMineralExtractorBE getController(Level level, BlockPos pos, BlockState state) {
        BlockPos controller = getControllerPos(pos, state);
        return level.hasChunkAt(controller) && level.getBlockEntity(controller) instanceof LargeMineralExtractorBE extractor
                ? extractor : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        BlockPos controllerPos = getControllerPos(pos, state);
        if (!level.hasChunkAt(controllerPos)) return InteractionResult.FAIL;
        BlockState controller = level.getBlockState(controllerPos);
        if (!controller.is(JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get())) return InteractionResult.FAIL;
        InteractionResult result = FluidContainerTransfer.useItemOn(player.getItemInHand(hand), state,
                level, pos, player, hand, hit);
        if (result != InteractionResult.PASS) return result;
        if (!level.isClientSide) ((LargeMineralExtractorBlock) controller.getBlock()).openMenu(player, controllerPos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            LargeMineralExtractorStructure.removeFromPart(level, pos, state, true);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public enum HorizontalPart implements StringRepresentable {
        LEFT(-1), CENTER(0), RIGHT(1);
        private final int offset;
        HorizontalPart(int offset) { this.offset = offset; }
        public int offset() { return offset; }
        public static HorizontalPart fromOffset(int offset) { return values()[offset + 1]; }
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }

    public enum DepthPart implements StringRepresentable {
        FRONT(0), MIDDLE(1), BACK(2);
        private final int offset;
        DepthPart(int offset) { this.offset = offset; }
        public int offset() { return offset; }
        public static DepthPart fromOffset(int offset) { return values()[offset]; }
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }

    public enum LayerPart implements StringRepresentable {
        BASE(0), TOP(1);
        private final int offset;
        LayerPart(int offset) { this.offset = offset; }
        public int offset() { return offset; }
        public static LayerPart fromOffset(int offset) { return values()[offset]; }
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }
}
