# Task 3 Report: Apply Configured Time Accelerator Duration

## Scope

Connected the configured acceleration duration to the single-accelerator resource and execution path in:

- `TimeAcceleratorBE`
- `AdvancedTimeAcceleratorBE`

This task intentionally did **not** update `ExtendedTimeAccelerationManager`'s old multiplier-based call sites. That follow-up remains task 4.

## RED Evidence

First I extended the focused contract tests before changing production code.

Command:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest
```

Observed failure:

```text
> Task :compileTestJava
TimeAcceleratorTimingTest.java:58: error: cannot find symbol
return getAccelerationWorkTicks(effectiveMultiplier);
       ^
symbol:   method getAccelerationWorkTicks(int)
```

That failure confirmed the new red test was pointed at the missing block-entity workload entrypoint rather than the already-green pure math helper.

## GREEN Evidence

After wiring `workTicks` into the BE resource path and adding a test-only loaded server config fixture, focused tests passed:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest
```

Output summary:

```text
> Task :test
BUILD SUCCESSFUL
```

The brief's required math and scheduler tests also passed:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest --tests com.jdte.common.blockentities.AdvancedEnergyTransmitterSchedulerTest
```

Output summary:

```text
> Task :test
BUILD SUCCESSFUL
```

Final full verification passed:

```bash
./gradlew.bat test
```

Output summary:

```text
> Task :test
BUILD SUCCESSFUL
```

## What Changed

- Added `TimeAcceleratorBE#getAccelerationWorkTicks(int)` and bound it to `JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds`.
- Changed the single-accelerator fallback path to:
  - compute `workTicks` once from effective multiplier and configured duration
  - use that same `workTicks` for fluid checks, FE checks, fluid settlement, and target execution
  - keep Creative as a full resource bypass
- Changed `AdvancedTimeAcceleratorBE` FE cost simulation/execution to use the same `workTicks` semantics.
- Preserved pending fractional fluid settlement via `TimeAcceleratorCostMath.settleFluid(...)`.
- Widened `TimeAcceleratorCostMath.fluidCost(...)` to accept the real JDT `Double` fluid config input without altering the formula.
- Added focused tests for:
  - BE workload entry uses server-configured duration
  - resource math consumes configured duration through `workTicks`

## Modified Files

- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`
- `src/main/java/com/jdte/common/blockentities/AdvancedTimeAcceleratorBE.java`
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorCostMath.java`
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java`
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java`

## Self-Review

- Verified the server config source is **only** `JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds`.
- Verified fluid and FE now both scale from the same actual workload: `effectiveMultiplier × durationSeconds × 20`.
- Verified `accelerateArea()` uses `workTicks` for every target execution loop, while visual effect display still uses the visible multiplier rather than confusing it with workload.
- Verified Creative still bypasses both fluid and FE checks/consumption.
- Verified pending fluid settlement still uses exact fractional carry and pure floor semantics.
- Verified Advanced FE preview and deduction stay aligned because both use the same precomputed `energyCost`.

## Concerns

- `ExtendedTimeAccelerationManager` still calls `getFluidDrainAmount(...)`, `getEnergyCost(...)`, and `consumeResources(...)` with old multiplier semantics. That mismatch is intentionally left for task 4 per brief, so current correctness is limited to the single-accelerator BE path and the updated local API semantics.
