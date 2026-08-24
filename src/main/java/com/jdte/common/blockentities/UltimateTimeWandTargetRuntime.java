package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Executes one bounded Time Accelerator target request for the Ultimate Time Wand and the
 * shared accelerator scheduler.
 */
public final class UltimateTimeWandTargetRuntime {
    private UltimateTimeWandTargetRuntime() {
    }

    public static int admit(int requestedTicks, int batchSize, long remainingBudget) {
        return TimeAcceleratorExecutionPolicy.requestedTicks(requestedTicks, batchSize, remainingBudget);
    }

    public static Route route(boolean ae2Available, boolean hasTickable, boolean ordinaryAvailable) {
        if (ae2Available && hasTickable) {
            return Route.AE2;
        }
        return ordinaryAvailable ? Route.ORDINARY : Route.NONE;
    }

    /**
     * Executes one direct wand request. AE2 targets have priority and never also receive an
     * ordinary block-entity or random-tick execution from the same request.
     */
    public static Result execute(ServerLevel level, BlockPos pos, int requestedTicks) {
        int admittedTicks = admit(requestedTicks, JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize.get(), Long.MAX_VALUE);
        if (admittedTicks <= 0) {
            return Result.noWork();
        }

        boolean hasTickable = ExtendedTimeAcceleratorAE2Integration.hasTickable(level, pos);
        if (route(true, hasTickable, true) == Route.AE2) {
            ExtendedTimeAcceleratorAE2Integration.Result result =
                    ExtendedTimeAcceleratorAE2Integration.accelerate(level, pos, admittedTicks);
            return new Result(result.executed(), result.valid(), result.idle(), null);
        }
        Result result = executeOrdinary(level, pos, admittedTicks);
        if (result.coalescedTarget() != null) {
            result.coalescedTarget().flushAcceleratedTicks();
        }
        return result;
    }

    static Result executeOrdinary(ServerLevel level, BlockPos pos, int requestedTicks) {
        int admittedTicks = admit(requestedTicks, JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize.get(), Long.MAX_VALUE);
        if (admittedTicks <= 0) {
            return Result.noWork();
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            return executeBlockEntity(level, pos, blockEntity, admittedTicks);
        }
        return executeRandomTicks(level, pos, admittedTicks);
    }

    @SuppressWarnings("unchecked")
    private static Result executeBlockEntity(ServerLevel level, BlockPos pos, BlockEntity blockEntity, int requestedTicks) {
        if (blockEntity.isRemoved() || blockEntity instanceof TimeAcceleratorMachine) {
            return Result.invalid();
        }
        BlockState state = blockEntity.getBlockState();
        BlockEntityTicker<BlockEntity> ticker = state.getTicker(level, (BlockEntityType<BlockEntity>) blockEntity.getType());
        if (ticker == null || !MiscTools.isValidTickAccelBlock(level, state, blockEntity)) {
            return Result.invalid();
        }
        if (blockEntity instanceof CoalescedAcceleratedMachine coalesced) {
            coalesced.accumulateAcceleratedTicks(requestedTicks);
            return new Result(requestedTicks, true, false, coalesced);
        }

        int executed = 0;
        for (; executed < requestedTicks && !blockEntity.isRemoved(); executed++) {
            ticker.tick(level, pos, blockEntity.getBlockState(), blockEntity);
        }
        return new Result(executed, true, false, null);
    }

    private static Result executeRandomTicks(ServerLevel level, BlockPos pos, int requestedTicks) {
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity() || !state.isRandomlyTicking()
                || !MiscTools.isValidTickAccelBlock(level, state, null)) {
            return Result.invalid();
        }
        for (int executed = 0; executed < requestedTicks; executed++) {
            state.randomTick(level, pos, level.random);
        }
        return new Result(requestedTicks, true, false, null);
    }

    public enum Route {
        AE2,
        ORDINARY,
        NONE
    }

    public static record Result(int executed, boolean valid, boolean idle,
                                CoalescedAcceleratedMachine coalescedTarget) {
        private static Result invalid() {
            return new Result(0, false, true, null);
        }

        private static Result noWork() {
            return new Result(0, true, true, null);
        }
    }
}
