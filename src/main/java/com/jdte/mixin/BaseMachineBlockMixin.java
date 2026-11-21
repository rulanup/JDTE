package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.ChargedLightningRodBE;
import com.jdte.common.manager.ExtendedTimeAccelerationManager;
import com.jdte.common.blockentities.TimeAcceleratorBE;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.manager.TimeFreezerManager;
import com.jdte.common.content.JDTEContentControl;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.MachineDropDataHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMachineBlock.class)
public abstract class BaseMachineBlockMixin {
    @Inject(method = "getDrops", at = @At("RETURN"))
    private void jdte$removeSeparatelyDroppedItems(BlockState state, LootParams.Builder builder,
                                                    CallbackInfoReturnable<java.util.List<ItemStack>> cir) {
        for (ItemStack stack : cir.getReturnValue()) {
            if (stack.is(state.getBlock().asItem())) {
                MachineDropDataHelper.removeSeparatelyDroppedItems(stack);
            }
        }
    }

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
            if (!JDTEContentControl.current().isBlockEnabled(blockState.getBlock())) {
                if (blockEntity instanceof TimeAcceleratorBE accelerator) {
                    ExtendedTimeAccelerationManager.deactivate(accelerator);
                }
                if (blockEntity instanceof TimeFreezerBE freezer) {
                    TimeFreezerManager.deactivate(freezer);
                }
                return;
            }

            if (!(blockEntity instanceof BaseMachineBE machine)) {
                original.tick(tickLevel, pos, blockState, blockEntity);
                return;
            }

            boolean redstoneActive = true;
            if (machine instanceof RedstoneControlledBE redstoneControlled) {
                redstoneControlled.evaluateRedstone();
                redstoneActive = redstoneControlled.isActiveRedstoneTestOnly();
            }
            boolean ultimateOverclock = redstoneActive && UpgradeHelper.hasUltimateOverclock(machine);
            boolean overclock = redstoneActive && UpgradeHelper.shouldRunOverclock(machine);
            int executions = machine instanceof ChargedLightningRodBE ? 1 : (ultimateOverclock ? 10 : (overclock ? 2 : 1));
            UpgradeHelper.runServerTicker(UpgradeHelper.usesCommonAeTickerGate(machine), redstoneActive,
                    () -> UpgradeHelper.mayRunWithUpgrades(machine), executions,
                    () -> {
                        original.tick(tickLevel, pos, blockState, blockEntity);
                        if (UpgradeHelper.hasAdvancedAEOutputUpgrade(machine) && tickLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            com.jdte.common.manager.AEOutputManager.flush(serverLevel, machine);
                        }
                    });
        });
    }

}
