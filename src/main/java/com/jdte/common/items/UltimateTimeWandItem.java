package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Applies one server-owned, bounded Ultimate Time Wand request to the clicked target. */
public class UltimateTimeWandItem extends Item implements FluidContainingItem {
    public UltimateTimeWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public int getMaxMB() {
        return JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get();
    }

    public int getMaxEnergy() {
        return JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide()) {
            cycleMode(player, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                cycleMode(player, stack);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return applyToTarget(serverLevel, player, stack, context.getClickedPos())
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private boolean applyToTarget(ServerLevel level, Player player, ItemStack stack, BlockPos pos) {
        if (!isValidTarget(level, pos)) {
            return false;
        }

        UltimateTimeWandEntity existing = findExisting(level, pos);
        UltimateTimeWandEntity.WandState before = existing == null
                ? new UltimateTimeWandEntity.WandState(pos, 0, UltimateTimeWandEntity.DEFAULT_DURATION,
                        UltimateTimeWandEntity.DEFAULT_DURATION)
                : existing.state();
        UltimateTimeWandData.Mode mode = getMode(stack);
        int finalExponent = UltimateTimeWandData.addStep(before.exponent(), mode);
        if (finalExponent <= before.exponent()) {
            return false;
        }

        int multiplier = UltimateTimeWandData.multiplierForExponent(finalExponent);
        UltimateTimeWandData.FluidSettlement fluidSettlement = UltimateTimeWandData.settleFluid(
                pendingFluid(stack), fluidCost(multiplier));
        int energyCost = UltimateTimeWandData.saturatingEnergyCost(multiplier, scaledEnergyBaseCost());
        UltimateTimeWandData.OperationResult operation = planWithResources(
                player, stack, before, mode, fluidSettlement.drainMb(), energyCost);
        if (!operation.success()) {
            return false;
        }

        boolean creative = player.getAbilities().instabuild;
        if (!creative && !hasFluidForSettlement(stack, fluidSettlement)) {
            return false;
        }

        if (existing == null) {
            level.addFreshEntity(new UltimateTimeWandEntity(level, pos, operation.state().exponent()));
        } else {
            existing.merge(operation.state().exponent() - before.exponent());
        }
        if (!creative) {
            FluidContainingItem.consumeFluid(stack, operation.fluidCost());
            if (!PoweredItem.consumeEnergy(stack, operation.energyCost())) {
                return false;
            }
            stack.set(JDTEDataComponents.ULTIMATE_TIME_WAND_PENDING_FLUID.get(), fluidSettlement.remainingCost());
        }
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.2F);
        return true;
    }

    private static boolean isValidTarget(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return MiscTools.isValidTickAccelBlock(level, state, blockEntity)
                || ExtendedTimeAcceleratorAE2Integration.hasTickable(level, pos);
    }

    private static UltimateTimeWandEntity findExisting(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(UltimateTimeWandEntity.class, new AABB(pos),
                        entity -> pos.equals(entity.getTarget()))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static UltimateTimeWandData.Mode getMode(ItemStack stack) {
        return UltimateTimeWandData.Mode.fromName(stack.getOrDefault(
                JDTEDataComponents.ULTIMATE_TIME_WAND_MODE.get(), UltimateTimeWandData.Mode.NORMAL.serializedName()));
    }

    private static double pendingFluid(ItemStack stack) {
        return Math.max(0.0D, stack.getOrDefault(JDTEDataComponents.ULTIMATE_TIME_WAND_PENDING_FLUID.get(), 0.0D));
    }

    private static boolean hasFluidForSettlement(ItemStack stack, UltimateTimeWandData.FluidSettlement settlement) {
        int available = FluidContainingItem.getAvailableFluid(stack);
        return available >= settlement.drainMb() && (settlement.drainMb() > 0 || settlement.remainingCost() <= 0.0D || available > 0);
    }

    private static double fluidCost(int multiplier) {
        return multiplier * (double) Config.TIMEWAND_FLUID_COST.get()
                * JDTEConfig.COMMON.ultimateTimeWandBaseCostMultiplier.get() / 600.0D;
    }

    private static int scaledEnergyBaseCost() {
        double scaled = Math.max(0.0D, Config.TIMEWAND_RF_COST.get()
                * JDTEConfig.COMMON.ultimateTimeWandEnergyCostMultiplier.get());
        return scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    private static UltimateTimeWandData.OperationResult planWithResources(
            Player player, ItemStack stack, UltimateTimeWandEntity.WandState state, UltimateTimeWandData.Mode mode,
            int fluidCost, int energyCost) {
        if (player.getAbilities().instabuild) {
            return UltimateTimeWandData.planOperation(state, mode, fluidCost, energyCost);
        }
        return UltimateTimeWandData.applyIfAffordable(state, mode,
                FluidContainingItem.getAvailableFluid(stack), PoweredItem.getAvailableEnergy(stack), fluidCost, energyCost);
    }

    private static void cycleMode(Player player, ItemStack stack) {
        UltimateTimeWandData.Mode next = getMode(stack).next();
        stack.set(JDTEDataComponents.ULTIMATE_TIME_WAND_MODE.get(), next.serializedName());
        player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.mode", next.serializedName())
                .withStyle(ChatFormatting.AQUA), true);
    }
}
