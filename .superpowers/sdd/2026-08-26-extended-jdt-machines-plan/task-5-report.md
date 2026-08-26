# Task 5 Report

## Status

DONE

## Scope

- Worktree: `D:/MCDev/idea-projects/JDTE/.worktrees/extended-jdt-machines`
- Baseline: `c2392b7`
- Production change: `src/main/java/com/jdte/common/items/ExtendedUpgradeItem.java`
- Test change: `src/test/java/com/jdte/common/items/ExtendedUpgradeItemTest.java`
- No advanced energy transmitter implementation file was changed.

## Implementation

- Added the four ordinary JDT conversion sources:
  - `Registration.GeneratorT1` to `JDTEBlocks.EXTENDED_GENERATOR`
  - `Registration.GeneratorFluidT1` to `JDTEBlocks.EXTENDED_FLUID_GENERATOR`
  - `Registration.ExperienceHolder` to `JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER`
  - `Registration.EnergyTransmitter` to `JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER`
- Preserved every previously supported extended-upgrade mapping.
- Added package-visible `ExtendedUpgradeItem.targetFor(Block)` for direct mapping tests.
- Kept JDT T1 Clicker and Block Placer, plus JDTE's Advanced Energy Transmitter, outside the map.
- Hardened `useOn` into a guarded conversion transaction:
  - clients return `SUCCESS` only for a mapped source;
  - servers require a live source block entity whose state and type match the source block;
  - source data is captured with `saveWithFullMetadata`, and `FACING` is copied to the target state;
  - the target block factory and exact expected block entity type are validated before replacement;
  - after replacement, the actual block, block entity, exact type, and type/state binding are checked before `loadCustomOnly`;
  - the upgrade item is consumed and the sound is played only after a successful load;
  - failed replacement or load returns `FAIL` without consuming the item and attempts to restore the original block, state, and NBT.

## Tests

`ExtendedUpgradeItemTest` verifies:

- all four required ordinary JDT mappings;
- rejection of JDT T1 Clicker, JDT T1 Block Placer, and JDTE Advanced Energy Transmitter;
- each target block factory creates the corresponding JDTE block entity type;
- each of the four target block entity types accepts its own target state and rejects the other target/source states under test.

The tests avoid a synthetic real-server world fixture, keeping the Task 5 contract checks deterministic.

## TDD Evidence

Red phase:

```text
./gradlew test --tests com.jdte.common.items.ExtendedUpgradeItemTest
> Task :compileTestJava FAILED
错误: 找不到符号
  符号: 方法 targetFor(...)
BUILD FAILED
```

The new test failed for the intended missing API before production implementation.

Green phase after the final implementation and review adjustment:

```text
./gradlew test --tests com.jdte.common.items.ExtendedUpgradeItemTest
> Task :test
BUILD SUCCESSFUL in 32s
```

## Verification

- `./gradlew compileJava` — `BUILD SUCCESSFUL`
- `./gradlew test --tests com.jdte.common.items.ExtendedUpgradeItemTest` — `BUILD SUCCESSFUL`
- `./gradlew test` — `BUILD SUCCESSFUL`; 307 tests across 61 suites, 0 failures, 0 errors, 0 skipped
- `git diff --check` — no whitespace errors

Gradle emitted only existing NeoForge/Curios deprecation warnings.

## Review

- Scope review found no edits to `AdvancedEnergyTransmitterBE` or other advanced-transmitter implementation files.
- The rollback fallback was tightened so the old block entity is reattached only after the world has actually restored the exact original block state.
- No subagent reviewer was available in this environment, so the review was performed locally against the Task 5 checklist and final diff.
