package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.direwolf20.justdirethings.common.items.FerricoreWrench;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Consumer;

/**
 * Restores the menu hooks that JDTE's 1.21 JDT baseline exposed on its base
 * machine block. JDT 1.20.1 opens menus directly from each concrete block.
 */
public abstract class JDTEMachineBlock extends BaseMachineBlock {
    protected JDTEMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return openMachineMenu(level, pos, player, hand);
    }

    protected final InteractionResult useWithFluidContainer(ItemStack stack, BlockState state, Level level,
                                                             BlockPos pos, Player player,
                                                             InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = FluidContainerTransfer.useItemOn(stack, state, level, pos, player, hand, hit);
        return result == InteractionResult.PASS ? openMachineMenu(level, pos, player, hand) : result;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING,
                context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    private InteractionResult openMachineMenu(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof FerricoreWrench || heldItem.getItem() instanceof MachineSettingsCopier) {
            return InteractionResult.PASS;
        }
        if (!isValidBE(level.getBlockEntity(pos))) {
            return InteractionResult.FAIL;
        }

        openMenu(player, pos);
        return InteractionResult.SUCCESS;
    }

    protected final void openScreen(Player player, MenuProvider provider, Consumer<FriendlyByteBuf> extraDataWriter) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, provider, extraDataWriter);
        }
    }

    /**
     * JDT 1.20.1 does not expose the newer rotation hook. Keeping it here
     * preserves the custom wrench contract for JDTE machines.
     */
    public BlockState direRotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            return state.setValue(BlockStateProperties.FACING, rotation.rotate(facing));
        }
        return state.rotate(rotation);
    }

    public abstract void openMenu(Player player, BlockPos pos);

    public abstract boolean isValidBE(BlockEntity blockEntity);
}
