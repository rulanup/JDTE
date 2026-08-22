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

- Historical concern before the review repair: `ExtendedTimeAccelerationManager` called the resource methods with old multiplier semantics. The live-path mismatch was fixed in the Review Repair section below; task-4 global budget and parallel behavior remains out of scope.

## Review Repair

The review identified that the live path is `TimeAcceleratorBE.tickServer()` → `ExtendedTimeAccelerationManager.submit(this)`. The manager still prepared resource costs from the display multiplier, passed fluid cost into `consumeResources(...)`, and enqueued only the multiplier. This bypassed the configured duration and could treat mB as work ticks during the second cost calculation.

The repair keeps the existing global execution, scan, pending, and parallel-budget logic unchanged, but fixes the value flow at the manager boundary:

- `prepareAcceleration(...)` calculates `displayMultiplier`, then one `workTicks` value from the server duration, and derives both fluid and FE costs from that workload.
- `AcceleratorContext` stores both `workTicks` and `displayMultiplier` separately.
- Resource consumption receives `context.workTicks`; the enqueue path accepts `context.workTicks` while retaining `displayMultiplier` only for contribution display/effects.
- The base fallback and Advanced FE path continue to use the same work amount for checks, execution, and settlement.

### Repair RED

Before the manager change, the new manager contract test was run with:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest
```

It failed during `compileTestJava` because `ExtendedTimeAccelerationManager.PreparedAcceleration` and `prepareAcceleration(...)` did not exist. This was the expected feature-missing failure for the manager contract.

### Repair GREEN and verification

The manager-focused contract passed after the minimal wiring:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest
```

Output: `BUILD SUCCESSFUL`.

The focused manager/time-accelerator regression set passed:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest --tests com.jdte.common.blockentities.AdvancedEnergyTransmitterSchedulerTest
```

Output: `BUILD SUCCESSFUL`.

The complete test suite passed:

```bash
./gradlew.bat test
```

Output: `BUILD SUCCESSFUL`.

### Repair self-review

- The manager contract verifies that a 5-second configured duration produces 400 work ticks and passes 400—not the 7 mB test cost or the old multiplier 4—to both resource-cost entry points; a 2-second configuration produces 160.
- The configured duration source remains exactly `JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds`.
- Fluid and FE costs, resource simulation/execution, and target execution all use the same workload. Creative still bypasses resource checks and consumption.
- Pending fractional fluid settlement, tier multipliers, the base fluid formula, Advanced simulation/execute consistency, display multiplier, and visual effects remain preserved.
- No task-4 global budget or parallel scheduling policy was changed.

### Remaining concern

The manager contract test isolates the value flow with a stub accelerator rather than constructing a full world and target-discovery graph. The full test suite is green, and the manager call sites were audited to confirm the live path now uses the same work ticks for resource consumption and enqueueing. The task-4 global budget/parallel changes remain intentionally out of scope.

## Follow-up Review Repair

The previous manager test only called `prepareAcceleration(...)`, so it could pass even if `LevelState.prepare()` later connected resource checks and payment incorrectly. The test coverage was tightened with the smallest reusable seam:

- `payForSubmission(accelerator, prepared)` is package-private and performs `hasResources(prepared.fluidCost(), prepared.energyCost())`, then—only on success—calls `consumeResources(prepared.workTicks(), prepared.energyCost())`.
- `LevelState.prepare()` calls this seam on its real post-discovery payment path before the existing enqueue loop. Discovery, queueing, execution, and all budget calculations remain unchanged.
- `ExtendedTimeAccelerationManagerTest` reuses its `RecordingAccelerator` and asserts a 5-second configuration produces 400 work ticks, resource simulation receives the stub's 7 mB / 11 FE costs, resource consumption receives 400 work ticks / 11 FE, and 7 mB is not passed as work ticks.

### Follow-up RED

After adding the seam contract test before production changes:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest
```

Output: `compileTestJava FAILED` because `ExtendedTimeAccelerationManager.payForSubmission(...)` did not yet exist. This was the expected missing-seam failure.

### Follow-up GREEN and verification

The manager/time-accelerator focused suite passed:

```bash
./gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest --tests com.jdte.common.blockentities.AdvancedEnergyTransmitterSchedulerTest
```

Output: `BUILD SUCCESSFUL`.

The complete test suite passed:

```bash
./gradlew.bat test
```

Output: `BUILD SUCCESSFUL`.

This follow-up changes only the tested payment seam and its real `LevelState.prepare()` call site; task-4 global budget and parallel scheduling behavior is still untouched.
