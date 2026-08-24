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
    public static Result execute(ServerLevel level, BlockPos pos, int requestedTicks, long remainingBudget) {
        Result result = execute(requestedTicks, JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize.get(), remainingBudget,
                new ServerTargetExecutor(level, pos));
        if (result.coalescedTarget() != null) {
            result.coalescedTarget().flushAcceleratedTicks();
        }
        return result;
    }

    static Result execute(int requestedTicks, int batchSize, long remainingBudget, TargetExecutor target) {
        int admittedTicks = admit(requestedTicks, batchSize, remainingBudget);
        if (admittedTicks <= 0) {
            return Result.noWork();
        }

        if (target.hasAe2Tickable()) {
            return target.executeAe2(admittedTicks);
        }
        return target.executeOrdinary(admittedTicks);
    }

    static Result executeOrdinary(ServerLevel level, BlockPos pos, int requestedTicks, long remainingBudget) {
        int admittedTicks = admit(requestedTicks, JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize.get(), remainingBudget);
        if (admittedTicks <= 0) {
            return Result.noWork();
        }
        return executeOrdinaryTarget(level, pos, admittedTicks);
    }

    private static Result executeOrdinaryTarget(ServerLevel level, BlockPos pos, int requestedTicks) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            return executeBlockEntity(level, pos, blockEntity, requestedTicks);
        }
        return executeRandomTicks(level, pos, requestedTicks);
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

    interface TargetExecutor {
        boolean hasAe2Tickable();

        Result executeAe2(int requestedTicks);

        Result executeOrdinary(int requestedTicks);
    }

    private record ServerTargetExecutor(ServerLevel level, BlockPos pos) implements TargetExecutor {
        @Override
        public boolean hasAe2Tickable() {
            return ExtendedTimeAcceleratorAE2Integration.hasTickable(level, pos);
        }

        @Override
        public Result executeAe2(int requestedTicks) {
            ExtendedTimeAcceleratorAE2Integration.Result result =
                    ExtendedTimeAcceleratorAE2Integration.accelerate(level, pos, requestedTicks);
            return new Result(result.executed(), result.valid(), result.idle(), null);
        }

        @Override
        public Result executeOrdinary(int requestedTicks) {
            return executeOrdinaryTarget(level, pos, requestedTicks);
        }
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
