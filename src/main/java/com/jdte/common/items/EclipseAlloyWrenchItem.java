package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.direwolf20.justdirethings.common.items.FerricoreWrench;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.JDTE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EclipseAlloyWrenchItem extends FerricoreWrench {
    private static final String JDT_MODID = "justdirethings";

    public EclipseAlloyWrenchItem() {
        super();
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.getAbilities().instabuild && super.canAttackBlock(state, level, pos, player);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Player player = context.getPlayer();

        if (player == null || !player.mayBuild()) {
            return InteractionResult.FAIL;
        }
        if (!canHandleMachine(state)) {
            return super.useOn(context);
        }

        if (!player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            return rotateMachine(level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return pickupMachine(level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public static boolean canHandleMachine(BlockState state) {
        return state.getBlock() instanceof BaseMachineBlock && isMovableMachineBlock(state.getBlock());
    }

    public static boolean rotateMachine(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!canHandleMachine(state)) {
            return false;
        }

        BlockState rotatedState = ((BaseMachineBlock) state.getBlock()).direRotate(state, level, pos, Rotation.CLOCKWISE_90);
        if (rotatedState.equals(state)) {
            return false;
        }
        level.setBlock(pos, rotatedState, 3);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public static boolean pickupMachine(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!canHandleMachine(state)) {
            return false;
        }

        Block block = state.getBlock();
        ItemStack pickedStack = new ItemStack(block.asItem());
        if (pickedStack.is(Items.AIR)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            CompoundTag tag = blockEntity.saveCustomOnly(level.registryAccess());
            if (!tag.isEmpty()) {
                pickedStack.set(JustDireDataComponents.CUSTOM_DATA_1, CustomData.of(tag));
            }
        }

        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);

        if (!player.getInventory().add(pickedStack)) {
            player.drop(pickedStack, false);
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0f, 0.9f);
        return true;
    }

    private static boolean isMovableMachineBlock(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null && (JDTE.MODID.equals(id.getNamespace()) || JDT_MODID.equals(id.getNamespace()));
    }
}
