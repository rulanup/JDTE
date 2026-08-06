package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedUpgradeItem extends Item {
    private static final Map<Block, Block> UPGRADE_MAP = new HashMap<>();

    static {
        UPGRADE_MAP.put(Registration.ClickerT2.get(), JDTEBlocks.EXTENDED_CLICKER.get());
        UPGRADE_MAP.put(Registration.BlockBreakerT2.get(), JDTEBlocks.EXTENDED_BLOCK_BREAKER.get());
        UPGRADE_MAP.put(Registration.BlockPlacerT2.get(), JDTEBlocks.EXTENDED_BLOCK_PLACER.get());
        UPGRADE_MAP.put(Registration.BlockSwapperT2.get(), JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get());
        UPGRADE_MAP.put(Registration.DropperT2.get(), JDTEBlocks.EXTENDED_DROPPER.get());
        UPGRADE_MAP.put(Registration.SensorT2.get(), JDTEBlocks.EXTENDED_SENSOR.get());
        UPGRADE_MAP.put(Registration.FluidCollectorT2.get(), JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get());
        UPGRADE_MAP.put(Registration.FluidPlacerT2.get(), JDTEBlocks.EXTENDED_FLUID_PLACER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(), JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(), JDTEBlocks.EXTENDED_FLUID_STABILIZER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(), JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(), JDTEBlocks.EXTENDED_INFUSION_MACHINE.get());
    }

    public ExtendedUpgradeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        Block extendedBlock = UPGRADE_MAP.get(block);
        if (extendedBlock == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity oldBE = level.getBlockEntity(pos);
        CompoundTag data = null;
        if (oldBE != null) {
            data = oldBE.saveWithFullMetadata(level.registryAccess());
        }

        // Get facing direction before removing
        Direction facing = state.getValue(BlockStateProperties.FACING);

        // Remove old block without dropping items
        level.removeBlockEntity(pos);
        level.setBlock(pos, extendedBlock.defaultBlockState().setValue(BlockStateProperties.FACING, facing), Block.UPDATE_ALL);

        // Restore data to new block entity
        if (data != null) {
            BlockEntity newBE = level.getBlockEntity(pos);
            if (newBE != null) {
                newBE.loadCustomOnly(data, level.registryAccess());
            }
        }

        // Consume item
        context.getItemInHand().shrink(1);

        // Play sound
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.extended_upgrade").withStyle(ChatFormatting.GRAY));
    }
}
