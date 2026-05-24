package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMachineBlock.class)
public abstract class BaseMachineBlockMixin {
    @Inject(method = "getTicker", at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void jdte$wrapServerTicker(Level level, BlockState state, BlockEntityType<T> type, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (level.isClientSide()) {
            return;
        }

        BlockEntityTicker<T> original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        cir.setReturnValue((tickLevel, pos, blockState, blockEntity) -> {
            original.tick(tickLevel, pos, blockState, blockEntity);
            if (blockEntity instanceof BaseMachineBE machine && UpgradeHelper.shouldRunOverclock(machine)) {
                original.tick(tickLevel, pos, blockState, blockEntity);
            }
        });
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void jdte$dropUpgradeCards(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (level.isClientSide() || newState.getBlock() == state.getBlock()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseMachineBE machine)) {
            return;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(machine);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
