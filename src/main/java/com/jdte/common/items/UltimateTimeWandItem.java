package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MagicHelpers;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

/** Applies one server-owned, bounded Ultimate Time Wand request to the clicked target. */
public class UltimateTimeWandItem extends Item implements FluidContainingItem, PoweredItem {
    public enum AirUseAction {
        PASS,
        CYCLE_MODE
    }

    public enum UseOnAction {
        ACCELERATE
    }

    public UltimateTimeWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public int getMaxMB() {
        return configuredFluidCapacity();
    }

    @Override
    public int getMaxEnergy() {
        return configuredEnergyCapacity();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (resolveAirUseAction(player.isShiftKeyDown()) != AirUseAction.CYCLE_MODE) {
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
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (resolveUseOnAction(player.isShiftKeyDown()) != UseOnAction.ACCELERATE) {
            return InteractionResult.PASS;
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
                ? UltimateTimeWandData.initialState(pos, 0, configuredDuration())
                : existing.state();
        UltimateTimeWandData.Mode mode = getMode(stack);
        int finalExponent = UltimateTimeWandData.addStep(before.exponent(), mode);
        if (finalExponent <= before.exponent()) {
            return false;
        }

        int multiplier = UltimateTimeWandData.multiplierForExponent(finalExponent);
        UltimateTimeWandData.FluidSettlement fluidSettlement = UltimateTimeWandData.settleFluid(
                pendingFluid(stack), fluidCost(multiplier),
                keepsFractionalFluidSettlement());
        int energyCost = UltimateTimeWandData.saturatingEnergyCost(multiplier, scaledEnergyBaseCost());
        UltimateTimeWandData.OperationResult operation = planWithResources(
                player, stack, before, mode, fluidSettlement.drainMb(), energyCost);
        if (!operation.success()) {
            return false;
        }

        boolean creative = player.getAbilities().instabuild;
        ServerCommitPort commitPort = new ServerCommitPort(level, stack, pos, existing, before);
        if (!creative && !hasFluidForSettlement(stack, fluidSettlement)) {
            return false;
        }
        if (!commitIfTargetValid(true, creative, operation, commitPort).success()) {
            return false;
        }
        if (!creative) {
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

    static int configuredDuration() {
        return JDTEConfig.COMMON.ultimateTimeWandDuration.get();
    }

    static int configuredFluidCapacity() {
        return JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get();
    }

    static int configuredEnergyCapacity() {
        return JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get();
    }

    static boolean keepsFractionalFluidSettlement() {
        return JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        for (String line : resourceTooltipText(Math.max(0, FluidContainingItem.getAvailableFluid(stack)),
                configuredFluidCapacity(), Math.max(0, PoweredItem.getAvailableEnergy(stack)),
                configuredEnergyCapacity())) {
            tooltip.add(Component.literal(line));
        }
    }

    public static List<String> resourceTooltipText(int currentFluid, int maxFluid,
                                                   int currentEnergy, int maxEnergy) {
        return List.of(
                "Time Fluid: " + MagicHelpers.formatted(currentFluid) + " / " + MagicHelpers.formatted(maxFluid) + " mB",
                "FE: " + MagicHelpers.formatted(currentEnergy) + " / " + MagicHelpers.formatted(maxEnergy) + " FE");
    }

    static AirUseAction resolveAirUseAction(boolean shiftDown) {
        return shiftDown ? AirUseAction.CYCLE_MODE : AirUseAction.PASS;
    }

    static UseOnAction resolveUseOnAction(boolean shiftDown) {
        return UseOnAction.ACCELERATE;
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

    static CommitOutcome commitIfTargetValid(boolean targetValid, boolean creative,
                                             UltimateTimeWandData.OperationResult operation, CommitPort port) {
        if (!targetValid) {
            return CommitOutcome.TARGET_REJECTED;
        }
        if (!operation.success()) {
            return CommitOutcome.OPERATION_REJECTED;
        }
        if (!creative && !port.prepareResources(operation)) {
            return CommitOutcome.ROLLED_BACK;
        }
        if (!port.applyEntity(operation.state())) {
            return CommitOutcome.ENTITY_REJECTED;
        }
        if (creative) {
            return CommitOutcome.SUCCESS;
        }
        if (!port.commitResources()) {
            port.rollbackEntity();
            return CommitOutcome.ROLLED_BACK;
        }
        return CommitOutcome.SUCCESS;
    }

    enum CommitOutcome {
        SUCCESS,
        TARGET_REJECTED,
        OPERATION_REJECTED,
        ENTITY_REJECTED,
        ROLLED_BACK;

        boolean success() {
            return this == SUCCESS;
        }
    }

    interface CommitPort {
        boolean prepareResources(UltimateTimeWandData.OperationResult operation);

        boolean applyEntity(UltimateTimeWandEntity.WandState after);

        void rollbackEntity();

        boolean commitResources();
    }

    private static final class ServerCommitPort implements CommitPort {
        private final ServerLevel level;
        private final ItemStack stack;
        private final BlockPos target;
        private final UltimateTimeWandEntity existing;
        private final UltimateTimeWandEntity.WandState before;
        private UltimateTimeWandEntity created;
        private ItemStackComponentTransaction preparedResources;

        private ServerCommitPort(ServerLevel level, ItemStack stack, BlockPos target,
                                 UltimateTimeWandEntity existing, UltimateTimeWandEntity.WandState before) {
            this.level = level;
            this.stack = stack;
            this.target = target;
            this.existing = existing;
            this.before = before;
        }

        @Override
        public boolean prepareResources(UltimateTimeWandData.OperationResult operation) {
            preparedResources = ItemStackComponentTransaction.prepare(stack, staged -> {
                if (operation.fluidCost() > 0) {
                    IFluidHandlerItem fluid = staged.getCapability(Capabilities.FluidHandler.ITEM);
                    if (fluid == null || fluid.drain(operation.fluidCost(), IFluidHandler.FluidAction.EXECUTE).getAmount()
                            != operation.fluidCost()) {
                        return false;
                    }
                }
                if (operation.energyCost() > 0) {
                    IEnergyStorage energy = staged.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (energy == null || energy.extractEnergy(operation.energyCost(), false) != operation.energyCost()) {
                        return false;
                    }
                }
                return true;
            }).orElse(null);
            return preparedResources != null;
        }

        @Override
        public boolean applyEntity(UltimateTimeWandEntity.WandState after) {
            if (existing != null) {
                existing.applyWandState(after);
                return true;
            }
            created = new UltimateTimeWandEntity(level, target, after.exponent(), after.totalTime());
            return level.addFreshEntity(created);
        }

        @Override
        public void rollbackEntity() {
            if (existing != null) {
                existing.applyWandState(before);
            } else if (created != null) {
                created.discard();
            }
        }

        @Override
        public boolean commitResources() {
            return preparedResources != null && preparedResources.commit(stack);
        }
    }

    private static void cycleMode(Player player, ItemStack stack) {
        UltimateTimeWandData.Mode next = getMode(stack).next();
        stack.set(JDTEDataComponents.ULTIMATE_TIME_WAND_MODE.get(), next.serializedName());
        player.displayClientMessage(Component.translatable("message.jdte.ultimate_time_wand.mode", next.serializedName())
                .withStyle(ChatFormatting.AQUA), true);
    }
}
