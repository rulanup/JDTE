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
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BaseMachineBlock.class, remap = false)
public abstract class BaseMachineBlockMixin {
    @Inject(remap = false, method = {"getDrops", "m_49635_"}, at = @At("RETURN"))
    private void jdte$removeMachineDataFromNormalDrops(BlockState state, LootParams.Builder builder,
                                                       CallbackInfoReturnable<java.util.List<ItemStack>> cir) {
        for (ItemStack stack : cir.getReturnValue()) {
            if (stack.is(state.getBlock().asItem())) {
                com.jdte.common.utils.MachineDropDataHelper.removeSeparatelyDroppedItems(stack);
            }
        }
    }

    @Inject(remap = false, method = {"getTicker", "m_142354_"}, at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void jdte$wrapServerTicker(Level level, BlockState state, BlockEntityType<T> type, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (level.isClientSide()) {
            return;
        }

        BlockEntityTicker<T> original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        cir.setReturnValue((tickLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof BaseMachineBE machine) {
                if (machine.isRemoved() || tickLevel.getBlockEntity(pos) != machine) return;
                com.jdte.common.manager.AEOutputManager.track(machine);
                boolean redstoneActive = true;
                if (machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE redstone) {
                    redstone.evaluateRedstone();
                    redstoneActive = redstone.isActiveRedstoneTestOnly();
                }
                int executions = redstoneActive && (machine instanceof com.jdte.common.blockentities.FluidMixerBE || machine instanceof com.jdte.common.blockentities.AdvancedEnergyTransmitterBE || machine instanceof com.jdte.common.blockentities.ExtendedEnergyTransmitterBE || UpgradeHelper.isExtendedGenerator(machine) || machine instanceof com.jdte.common.blockentities.BioExtractorBE || machine instanceof com.jdte.common.blockentities.AdvancedEntityAcceleratorBE) && UpgradeHelper.hasUltimateOverclock(machine)
                        ? 10 : UpgradeHelper.shouldRunOverclock(machine) ? 2 : 1;
                UpgradeHelper.runServerTicker(UpgradeHelper.usesCommonAeTickerGate(machine), redstoneActive,
                        () -> UpgradeHelper.mayRunWithUpgrades(machine), executions, () -> {
                    original.tick(tickLevel, pos, blockState, blockEntity);
                    if (UpgradeHelper.hasAdvancedAEOutputUpgrade(machine) && tickLevel instanceof net.minecraft.server.level.ServerLevel server)
                        com.jdte.common.manager.AEOutputManager.flush(server, machine);
                });
            } else original.tick(tickLevel, pos, blockState, blockEntity);
        });
    }

    @Inject(remap = false, method = {"onRemove", "m_6810_"}, at = @At("HEAD"))
    private void jdte$dropUpgradeCards(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (level.isClientSide() || newState.getBlock() == state.getBlock()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseMachineBE machine)) {
            return;
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(machine);
        if (machine instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator) {
            net.minecraftforge.items.ItemStackHandler customHandler = fabricator.getUpgradeHandler();
            for (int i = 0; i < customHandler.getSlots(); i++) {
                ItemStack stack = customHandler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                    customHandler.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            return;
        }
        if (handler == null) {
            return;
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
    @org.spongepowered.asm.mixin.injection.Redirect(method = {"onRemove", "m_6810_"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", remap = true), remap = false)
    private void jdte$dropMineralInventory(Level level, double x, double y, double z, ItemStack stack,
            BlockState oldState, Level oldLevel, BlockPos pos, BlockState newState, boolean moving) {
        if (oldState.getBlock() instanceof com.jdte.common.blocks.MineralExtractorBlock
                || oldState.getBlock() instanceof com.jdte.common.blocks.LargeMineralExtractorBlock) {
            if (stack.getCount() > stack.getMaxStackSize()) {
                level.addFreshEntity(new com.jdte.common.entities.BulkItemEntity(level, x + 0.5, y + 0.5, z + 0.5, stack));
                stack.setCount(0);
                return;
            }
        }
        Containers.dropItemStack(level, x, y, z, stack);
    }

}
