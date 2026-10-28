package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.blockentities.ExtendedUpgradeMachine;
import com.jdte.common.blockentities.FluidMixerBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.upgrades.SlotExpansion;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEAttachments;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Prepares all state before replacement, then moves ownership or restores the original machine. */
final class MachineTierConversion {
    @FunctionalInterface
    interface Replacement { boolean replace(Level level, BlockPos pos, BlockState state); }

    private MachineTierConversion() {}

    static BlockState targetState(BlockState source, BlockState target) {
        boolean sourceFacing = source.hasProperty(BlockStateProperties.FACING);
        if (sourceFacing != target.hasProperty(BlockStateProperties.FACING)) return null;
        return sourceFacing ? target.setValue(BlockStateProperties.FACING, source.getValue(BlockStateProperties.FACING)) : target;
    }

    static InteractionResult convert(UseOnContext context, Block targetBlock, Replacement replacement) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState sourceState = level.getBlockState(pos);
        if (!(level.getBlockEntity(pos) instanceof BaseMachineBE source) || source.isRemoved()
                || !source.getType().isValid(sourceState)) return InteractionResult.FAIL;
        BlockState targetState = targetState(sourceState, targetBlock.defaultBlockState());
        if (targetState == null || !(targetBlock instanceof EntityBlock factory)) return InteractionResult.FAIL;
        if (!(factory.newBlockEntity(pos, targetState) instanceof BaseMachineBE prepared)
                || !(prepared instanceof ExtendedUpgradeMachine) || !prepared.getType().isValid(targetState)) return InteractionResult.FAIL;
        prepared.setLevel(level);

        State old;
        State staged;
        try {
            old = new State(source);
            CompoundTag data = old.expandedData(prepared);
            prepared.load(data);
            // Native T2 classes own FE/filter fields; several extended classes use Forge attachments.
            // Copy the real interfaces explicitly after loading, rather than their inactive placeholder caps.
            if (old.energy != null && prepared instanceof PoweredMachineBE powered) {
                if (old.fe > powered.getMaxEnergy()) throw new IllegalArgumentException("Target cannot hold all source FE");
                powered.getEnergyStorage().setEnergy(old.fe);
            }
            if (old.tank != null && prepared instanceof FluidMachineBE fluid) {
                if (old.fluid.getAmount() > fluid.getMaxMB()) throw new IllegalArgumentException("Target cannot hold all source fluid");
                fluid.getFluidTank().setFluid(old.fluid.copy());
            }
            UpgradeHelper.syncCapacities(prepared);
            if (prepared instanceof FluidMixerBE mixer) mixer.syncTankCapacities();
            if (prepared instanceof GelGeneratorBE gel) gel.getOutputFluidTank().setCapacity(gel.getMaxMB());
            old.verify(prepared);
            staged = new State(prepared);
        } catch (RuntimeException failure) {
            LogUtils.getLogger().warn("JDTE tier conversion rejected at {} before replacing the source", pos, failure);
            return InteractionResult.FAIL;
        }

        // ItemStack.useOn normally captures placement snapshots. Capturing after the old BE
        // is detached would omit its inventory. Own this transaction and its protection event.
        BlockSnapshot originalBlock = BlockSnapshot.create(level.dimension(), level, pos, Block.UPDATE_ALL);
        boolean outerCapture = level.captureBlockSnapshots;
        level.captureBlockSnapshots = false;
        try {
            level.removeBlockEntity(pos); // Block removal must not scatter the still-owned source inventory.
            if (!replacement.replace(level, pos, targetState)
                    || level.getBlockState(pos).getBlock() != targetBlock
                    || level.getBlockEntity(pos) == null
                    || level.getBlockEntity(pos).getType() != prepared.getType())
                throw new IllegalStateException("Tier replacement did not create the expected block entity");
            level.setBlockEntity(prepared);
            if (level.getBlockEntity(pos) != prepared || prepared.isRemoved())
                throw new IllegalStateException("Prepared replacement was not installed");
            // Invalidating LazyOptionals alone does not revoke an IItemHandler already held by a caller.
            old.clearOwnedBuffers();
            if (ForgeEventFactory.onBlockPlace(context.getPlayer(), originalBlock, context.getClickedFace()))
                throw new IllegalStateException("Tier replacement was denied by a placement event");
            prepared.setChanged();
            level.sendBlockUpdated(pos, sourceState, targetState, Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, targetBlock);
        } catch (RuntimeException failure) {
            try {
                level.removeBlockEntity(pos);
                if (level.getBlockState(pos) != sourceState) level.setBlock(pos, sourceState, Block.UPDATE_ALL);
                if (level.getBlockState(pos) != sourceState) throw new IllegalStateException("Cannot restore source block");
                BlockEntity restored = BlockEntity.loadStatic(pos, sourceState, old.nbt);
                if (restored == null || restored.getType() != source.getType()) throw new IllegalStateException("Cannot reload source data");
                level.setBlockEntity(restored);
                // Some native classes omit their owned FE field from NBT entirely.
                if (old.energy != null && restored instanceof PoweredMachineBE powered)
                    powered.getEnergyStorage().setEnergy(old.fe);
                old.verify((BaseMachineBE) restored);
                restored.setChanged();
                level.sendBlockUpdated(pos, targetState, sourceState, Block.UPDATE_ALL);
                old.clearOwnedBuffers();
                staged.clearOwnedBuffers();
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
                throw new IllegalStateException("JDTE tier conversion and rollback failed at " + pos, failure);
            }
            LogUtils.getLogger().warn("JDTE tier conversion failed at {}; restored the original machine without spending the card", pos, failure);
            return InteractionResult.FAIL;
        } finally {
            level.captureBlockSnapshots = outerCapture;
        }
        context.getItemInHand().shrink(1);
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static List<ItemStack> stacks(ItemStackHandler handler) {
        var result = new ArrayList<ItemStack>(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); i++) result.add(handler.getStackInSlot(i).copy());
        return result;
    }

    private static void sameStacks(List<ItemStack> expected, ItemStackHandler actual, String label) {
        if (actual.getSlots() < expected.size()) throw new IllegalStateException(label + " lost slots");
        for (int i = 0; i < actual.getSlots(); i++) {
            ItemStack wanted = i < expected.size() ? expected.get(i) : ItemStack.EMPTY;
            if (!ItemStack.matches(wanted, actual.getStackInSlot(i))) throw new IllegalStateException(label + " differs at slot " + i);
        }
    }

    private static final class State {
        final CompoundTag nbt;
        final ItemStackHandler inventoryHandler, active, standard, extended, filter;
        final List<ItemStack> inventory, cards, filters;
        final MachineEnergyStorage energy, attachedEnergy;
        final int fe;
        final FluidTank tank, clickerTank;
        final FluidStack fluid;
        final List<FluidTank> extraTanks = new ArrayList<>();

        State(BaseMachineBE machine) {
            inventoryHandler = machine.getMachineHandler();
            active = UpgradeHelper.getUpgradeHandler(machine);
            standard = JDTEAttachments.upgrades(machine);
            extended = JDTEAttachments.extendedUpgrades(machine);
            filter = machine instanceof FilterableBE filterable ? filterable.getFilterHandler() : null;
            inventory = stacks(inventoryHandler); cards = stacks(active);
            filters = filter == null ? List.of() : stacks(filter);
            energy = machine instanceof PoweredMachineBE powered ? powered.getEnergyStorage() : null;
            attachedEnergy = JDTEAttachments.energy(machine);
            fe = energy == null ? 0 : energy.getEnergyStored();
            tank = machine instanceof FluidMachineBE fluidMachine ? fluidMachine.getFluidTank() : null;
            fluid = tank == null ? FluidStack.EMPTY : tank.getFluid().copy();
            clickerTank = JDTEAttachments.clickerFluidTank(machine);
            if (machine instanceof FluidMixerBE mixer) { extraTanks.add(mixer.inputTankB); extraTanks.add(mixer.outputTank); }
            if (machine instanceof GelGeneratorBE gel) extraTanks.add(gel.getOutputFluidTank());
            nbt = machine.saveWithFullMetadata();
        }

        CompoundTag expandedData(BaseMachineBE target) {
            var inactive = active == standard ? extended : standard;
            var plan = SlotExpansion.plan(cards, stacks(inactive), ItemStack::isEmpty, ItemStack::copy);
            if (plan.size() != UpgradeHelper.getUpgradeHandler(target).getSlots())
                throw new IllegalArgumentException("Unexpected target upgrade slot count");
            var next = new ItemStackHandler(plan.size());
            for (int i = 0; i < plan.size(); i++) next.setStackInSlot(i, plan.get(i));
            CompoundTag result = nbt.copy();
            result.putString("id", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(target.getType()).toString());
            CompoundTag caps = result.getCompound("ForgeCaps").getCompound("jdte:machine_data");
            caps.put("extended_upgrade_handler", next.serializeNBT());
            var empty = new CompoundTag(); empty.putInt("Size", 4);
            caps.put("upgrade_handler", empty);
            if (filter != null) caps.put("filter_handler", filter.serializeNBT());
            if (target instanceof PoweredMachineBE powered)
                caps.putInt("machine_energy", powered.getEnergyStorage() == JDTEAttachments.energy(target) ? fe : 0);
            return result;
        }

        void verify(BaseMachineBE target) {
            sameStacks(inventory, target.getMachineHandler(), "Machine inventory");
            sameStacks(cards, UpgradeHelper.getUpgradeHandler(target), "Active upgrade inventory");
            if (filter != null && target instanceof FilterableBE filterable) sameStacks(filters, filterable.getFilterHandler(), "Filter templates");
            if (energy != null && (!(target instanceof PoweredMachineBE powered) || powered.getEnergyStorage().getEnergyStored() != fe))
                throw new IllegalStateException("FE differs after conversion");
            if (tank != null && (!(target instanceof FluidMachineBE fluidMachine) || !fluid.isFluidStackIdentical(fluidMachine.getFluidStack())))
                throw new IllegalStateException("Fluid differs after conversion");
        }

        void clearOwnedBuffers() {
            Set<ItemStackHandler> handlers = Collections.newSetFromMap(new IdentityHashMap<>());
            Collections.addAll(handlers, inventoryHandler, active, standard, extended);
            if (filter != null) handlers.add(filter);
            for (var handler : handlers) { var empty = new CompoundTag(); empty.putInt("Size", handler.getSlots()); handler.deserializeNBT(empty); }
            if (energy != null) energy.setEnergy(0);
            attachedEnergy.setEnergy(0);
            if (tank != null) tank.setFluid(FluidStack.EMPTY);
            clickerTank.setFluid(FluidStack.EMPTY);
            for (var extra : extraTanks) extra.setFluid(FluidStack.EMPTY);
        }
    }
}
