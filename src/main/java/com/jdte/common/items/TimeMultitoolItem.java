package com.jdte.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.direwolf20.justdirethings.common.events.BlockEvents;
import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.Helpers;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.common.items.tools.EclipseAlloyPaxel;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.List;
import java.util.Set;

public class TimeMultitoolItem extends EclipseAlloyPaxel implements FluidContainingItem {
    public TimeMultitoolItem(Properties properties) {
        super(properties.pickaxe(com.direwolf20.justdirethings.common.items.tools.utils.GooTier.ECLIPSEALLOY.material(), 1.0F, -2.8F).fireResistant());
    }

    public static final int MAX_ENERGY = 500_000;
    public static final int MAX_TIME_FLUID = 1_000_000;

    @Override
    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public int getMaxMB() {
        return MAX_TIME_FLUID;
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return stack.getOrDefault(JustDireDataComponents.FORGE_ENERGY, 0) >= getBlockBreakFECost() && (super.canPerformAction(stack, itemAbility)
                || itemAbility == ItemAbilities.HOE_TILL);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return hasWorkingEnergy(stack) && super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float baseSpeed = super.getDestroySpeed(stack, state);
        if (baseSpeed <= 1.0F) {
            return baseSpeed;
        }

        return hasWorkingEnergy(stack) ? baseSpeed : 1.0F;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }

        TimeMultitoolSpeedMode nextMode = getSelectedSpeedMode(stack).next();
        if (!level.isClientSide()) {
            setSelectedSpeedMode(stack, nextMode);
            player.sendOverlayMessage(Component.translatable(
                    "message.jdte.time_multitool.speed", nextMode.multiplier()));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                openSettings(player);
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        ItemStack stack = context.getItemInHand();
        if (!hasWorkingEnergy(stack)) {
            return InteractionResult.FAIL;
        }

        InteractionResult hoeResult = tryTillSoil(context);
        if (hoeResult.consumesAction()) {
            return hoeResult;
        }
        return super.useOn(context);
    }

    /**
     * JDT routes every successful paxel block break through this method. Keeping the
     * normal helper loop preserves all installed JDT abilities while letting this
     * tool settle Time Fluid once, after each block was actually removed.
     */
    @Override
    public void mineBlocksAbility(ItemStack stack, Level level, BlockPos origin, LivingEntity entity) {
        BlockState originState = level.getBlockState(origin);
        Set<BlockPos> targets = getBreakBlockPositions(stack, level, origin, entity, originState);
        boolean instantBreak = canInstaBreak(stack, level, targets);
        int targetCount = (int) targets.stream().filter(pos -> !level.getBlockState(pos).isAir()).count();
        TimeMultitoolMiningPolicy.Decision batchDecision = miningBatchDecision(stack, targetCount);
        BlockEvents.spawnDropsAtPos = origin;
        try {
            for (BlockPos target : targets) {
                if (Helpers.testUseTool(stack) < 0) {
                    break;
                }

                boolean removed;
                TimeMultitoolBreakTracker.begin(target);
                try {
                    Helpers.breakBlocksNew(level, target, entity, stack, true, instantBreak);
                } finally {
                    removed = TimeMultitoolBreakTracker.finish();
                }
                if (!level.isClientSide() && removed && batchDecision.timeFluidCost() > 0) {
                    FluidContainingItem.consumeFluid(entity instanceof Player player ? player : null, stack, batchDecision.timeFluidCost());
                }
            }
        } finally {
            BlockEvents.spawnDropsAtPos = BlockPos.ZERO;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        int storedFluid = Math.max(0, FluidContainingItem.getAvailableFluid(stack));
        tooltip.accept(Component.translatable("tooltip.jdte.time_multitool.speed",
                getSelectedSpeedMode(stack).multiplier()).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable("tooltip.jdte.time_multitool.fluid",
                storedFluid, MAX_TIME_FLUID).withStyle(ChatFormatting.BLUE));
        tooltip.accept(Component.translatable("tooltip.jdte.time_multitool.controls")
                .withStyle(ChatFormatting.GRAY));
    }

    public static TimeMultitoolSpeedMode getSelectedSpeedMode(ItemStack stack) {
        return TimeMultitoolSpeedStorage.get(stack, JDTEDataComponents.TIME_MULTITOOL_SPEED_MODE.get());
    }

    private static void setSelectedSpeedMode(ItemStack stack, TimeMultitoolSpeedMode mode) {
        TimeMultitoolSpeedStorage.set(stack, JDTEDataComponents.TIME_MULTITOOL_SPEED_MODE.get(), mode);
    }

    private boolean hasWorkingEnergy(ItemStack stack) {
        return PoweredItem.hasEnoughEnergy(stack, getBlockBreakFECost());
    }

    TimeMultitoolMiningPolicy.Decision miningBatchDecision(ItemStack stack, int targetCount) {
        return TimeMultitoolMiningPolicy.decideBatch(
                getSelectedSpeedMode(stack),
                Math.max(0, FluidContainingItem.getAvailableFluid(stack)),
                Math.max(0, PoweredItem.getAvailableEnergy(stack)),
                getBlockBreakFECost(),
                targetCount);
    }

    private InteractionResult tryTillSoil(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() == net.minecraft.core.Direction.DOWN
                || !level.getBlockState(pos.above()).isAir()) {
            return InteractionResult.PASS;
        }

        BlockState current = level.getBlockState(pos);
        BlockState tilled = current.getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (tilled == null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!level.isClientSide()) {
            level.setBlock(pos, tilled, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, tilled));
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player,
                        context.getHand() == InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
