package com.jdte.common.blocks;

import com.jdte.common.blockentities.CreativeEnergyCellBE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class CreativeEnergyCellBlock extends Block implements EntityBlock {
    public CreativeEnergyCellBlock(Properties properties) {
        super(properties.strength(3.0F).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeEnergyCellBE(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel server) || type != JDTEBlockEntities.CREATIVE_ENERGY_CELL.get()) return null;
        return (world, pos, blockState, cell) -> ((CreativeEnergyCellBE) cell).chargeNeighbors(server);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
        var receiver = ItemAccess.forPlayerSlot(player, slot).getCapability(Capabilities.Energy.ITEM);
        if (receiver == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) CreativeEnergyCellBE.charge(receiver);
        return InteractionResult.SUCCESS;
    }
}
