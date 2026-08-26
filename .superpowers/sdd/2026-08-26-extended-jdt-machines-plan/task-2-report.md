# Task 2 Report

## Status

DONE

## Summary

Implemented the extended fluid generator vertical slice in `D:/MCDev/idea-projects/JDTE/.worktrees/extended-jdt-machines` by:

- adding `ExtendedFluidGeneratorBE` as a typed `GeneratorFluidT1BE` subclass that implements `ExtendedUpgradeMachine`
- adding `ExtendedFluidGeneratorBlock`, `ExtendedFluidGeneratorContainer`, and `ExtendedFluidGeneratorScreen`
- registering the block, item, block entity type, and menu type under `jdte:extended_fluid_generator`
- wiring the screen registration, capability exposure, and `ExtendedUpgradeItem` conversion mapping
- extending `ExtendedJdtMachineBehaviorTest` with a red/green regression for the new machine contract

## Files Changed

- `src/main/java/com/jdte/common/blockentities/ExtendedFluidGeneratorBE.java`
- `src/main/java/com/jdte/common/blocks/ExtendedFluidGeneratorBlock.java`
- `src/main/java/com/jdte/common/containers/ExtendedFluidGeneratorContainer.java`
- `src/main/java/com/jdte/client/screens/ExtendedFluidGeneratorScreen.java`
- `src/main/java/com/jdte/setup/JDTEBlocks.java`
- `src/main/java/com/jdte/setup/JDTEItems.java`
- `src/main/java/com/jdte/setup/JDTEBlockEntities.java`
- `src/main/java/com/jdte/setup/JDTEMenus.java`
- `src/main/java/com/jdte/client/JDTEClientSetup.java`
- `src/main/java/com/jdte/common/capabilities/MachineCapabilities.java`
- `src/main/java/com/jdte/common/items/ExtendedUpgradeItem.java`
- `src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`

## Red Test

Command:

```bash
./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest.extendedFluidGeneratorKeepsJdtFluidGeneratorContract
```

Observed result:

```text
> Task :compileTestJava FAILED
...
错误: 找不到符号
  符号:   类 ExtendedFluidGeneratorBE
...
错误: 找不到符号
  符号:   变量 EXTENDED_FLUID_GENERATOR
  位置: 类 JDTEBlocks
...
错误: 找不到符号
  符号:   变量 EXTENDED_FLUID_GENERATOR
  位置: 类 JDTEBlockEntities
...
错误: 找不到符号
  符号:   变量 EXTENDED_FLUID_GENERATOR
  位置: 类 JDTEMenus
...
BUILD FAILED in 4s
```

Interpretation: the test failed for the intended reason before implementation existed: missing class plus missing JDTE registrations.

## Green Test

Command:

```bash
./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest
```

Observed result:

```text
> Task :test

BUILD SUCCESSFUL in 27s
9 actionable tasks: 4 executed, 5 up-to-date
```

Warnings emitted during compilation:

```text
D:\MCDev\idea-projects\JDTE\.worktrees\extended-jdt-machines\src\main\java\com\jdte\client\JDTEClientSetup.java:35: 警告: [removal] EventBusSubscriber 中的 bus() 已过时
D:\MCDev\idea-projects\JDTE\.worktrees\extended-jdt-machines\src\main\java\com\jdte\common\integrations\curios\BigFluidTankCuriosIntegration.java:51: 警告: [removal] Curios API 相关调用已过时
```

These warnings were pre-existing and unrelated to task 2; they did not block the targeted test class.

## Self Review

- `ExtendedFluidGeneratorBE` uses the typed constructor with `JDTEBlockEntities.EXTENDED_FLUID_GENERATOR`, satisfying the block-entity binding requirement.
- `JDTEBlockEntities.EXTENDED_FLUID_GENERATOR` binds only `JDTEBlocks.EXTENDED_FLUID_GENERATOR`.
- `ExtendedFluidGeneratorBlock` inherits JDT `GeneratorFluidT1`, so the machine reuses JDT fluid handling, burn logic, FE generation, redstone behavior, tank behavior, and the existing `GeneratorFluidUpgradeMixin` target type.
- `ExtendedFluidGeneratorContainer` uses its own `JDTEMenus.EXTENDED_FLUID_GENERATOR` and `stillValid` checks only `JDTEBlocks.EXTENDED_FLUID_GENERATOR`.
- `MachineCapabilities` exposes energy, fluid, and insert-only item access so automation matches the original fuel-fluid generator contract.
- `JDTEClientSetup` registers only the new menu screen; no changes were made to `AdvancedEnergyTransmitterBE`.
- `ExtendedUpgradeItem` was updated so the existing extended-upgrade workflow can convert JDT's base fluid generator into the extended tier, which is necessary for a usable vertical slice.

## Concerns

- No client runtime/manual GUI verification was performed beyond compile + targeted tests, so the new screen/layout was validated by source parity with JDT rather than by launching `runClient`.

---

## Fix Round 1 - 2026-08-26

### Status

DONE

### Findings Addressed

- Removed the incorrect follow-up attempt that invented `getBurnRemaining()` / `getMaxBurn()` on `ExtendedFluidGeneratorContainer` and a fake burn bar in `ExtendedFluidGeneratorScreen`.
- Restored task 2 to match the real `just-dire-things-1002348:7463040` API surface:
  - `ExtendedFluidGeneratorContainer` now mirrors JDT's public method shape with `addMachineSlots`, `stillValid`, `quickMoveStack`, and `removed`, while still binding `JDTEMenus.EXTENDED_FLUID_GENERATOR`.
  - `ExtendedFluidGeneratorScreen` now mirrors JDT's real screen contract with `init`, `setTopSection`, `addTickSpeedButton`, `addRedstoneButtons`, and `powerBarTooltip`, while inheriting fluid bar rendering/tooltips from `BaseMachineScreen`.
- Added regression coverage in `ExtendedJdtMachineBehaviorTest` for:
  - extended fluid generator typed BE and registration contract
  - container method shape and `stillValid` restricted to `JDTEBlocks.EXTENDED_FLUID_GENERATOR`
  - screen method shape and inherited `BaseMachineScreen` fluid display path instead of custom burn UI

### Javap / Source Evidence

Controller-provided `javap` for dependency `7463040` matched the local JDT source checkout and showed that the real API is thinner than the review text suggested:

```text
GeneratorFluidT1Container: constructors, addMachineSlots, stillValid, quickMoveStack, removed
GeneratorFluidT1Screen: constructor, init, setTopSection, addTickSpeedButton, addRedstoneButtons, powerBarTooltip
```

Local source parity references used during this round:

- `D:/MCDev/idea-projects/JustDireThings-1.21.1/JustDireThings-1.21.1/src/main/java/com/direwolf20/justdirethings/common/containers/GeneratorFluidT1Container.java`
- `D:/MCDev/idea-projects/JustDireThings-1.21.1/JustDireThings-1.21.1/src/main/java/com/direwolf20/justdirethings/client/screens/GeneratorFluidT1Screen.java`
- `D:/MCDev/idea-projects/JustDireThings-1.21.1/JustDireThings-1.21.1/src/main/java/com/direwolf20/justdirethings/common/containers/basecontainers/BaseMachineContainer.java`
- `D:/MCDev/idea-projects/JustDireThings-1.21.1/JustDireThings-1.21.1/src/main/java/com/direwolf20/justdirethings/client/screens/basescreens/BaseMachineScreen.java`

Judgment: the original review correctly required parity with JDT behavior, but its mention of dedicated burn/fluid sync getters and standalone `renderBg` / `renderTooltip` methods conflicts with the actual dependency API for `7463040`. The correct fix is to preserve the real JDT inheritance/data path, not to fabricate extra sync semantics.

### Files Changed In This Round

- `src/main/java/com/jdte/common/containers/ExtendedFluidGeneratorContainer.java`
- `src/main/java/com/jdte/client/screens/ExtendedFluidGeneratorScreen.java`
- `src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`

### Red Test During This Round

Command:

```bash
./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest.extendedFluidGeneratorContainerExposesGeneratorSyncAccessors --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest.extendedFluidGeneratorScreenRetainsDedicatedFuelAndFluidDisplayHooks
```

Observed result:

```text
> Task :compileTestJava
.../ExtendedJdtMachineBehaviorTest.java:94: 错误: 找不到符号
assertEquals(18, ExtendedFluidGeneratorScreen.fuelBarHeight(4000, 4000));
符号:   方法 fuelBarHeight(int,int)
位置: 类 ExtendedFluidGeneratorScreen
.../ExtendedJdtMachineBehaviorTest.java:95: 错误: 找不到符号
assertEquals(0, ExtendedFluidGeneratorScreen.fuelBarHeight(0, 4000));
符号:   方法 fuelBarHeight(int,int)
位置: 类 ExtendedFluidGeneratorScreen
> Task :compileTestJava FAILED
BUILD FAILED in 7s
```

Interpretation: the intentionally failing regression exposed that the prior fix-round attempt depended on nonexistent dedicated burn UI APIs.

### Green Test During This Round

Command:

```bash
./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest
```

Observed result:

```text
> Task :test

BUILD SUCCESSFUL in 20s
9 actionable tasks: 3 executed, 6 up-to-date
```

Compilation warnings observed:

```text
D:\MCDev\idea-projects\JDTE\.worktrees\extended-jdt-machines\src\main\java\com\jdte\client\JDTEClientSetup.java:35: 警告: [removal] EventBusSubscriber 中的 bus() 已过时, 且标记为待删除
D:\MCDev\idea-projects\JDTE\.worktrees\extended-jdt-machines\src\main\java\com\jdte\client\JDTEClientSetup.java:35: 警告: [removal] EventBusSubscriber 中的 Bus 已过时, 且标记为待删除
```

### Self Review

- `ExtendedFluidGeneratorContainer` keeps the real JDT slot/data contract and does not invent fuel-progress accessors.
- `ExtendedFluidGeneratorScreen` keeps only the methods actually present in JDT `7463040`; fluid bar rendering and fluid tooltip remain inherited from `BaseMachineScreen`.
- The fix is scoped to task 2 files only; no changes were made to `AdvancedEnergyTransmitterBE`, task 1's coal generator work, or unrelated user edits.
