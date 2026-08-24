# Task 2 Report

Date: 2026-08-24

Scope: large portable generator coal-block fuel resolution and burn-entry coverage only.

## Red

Command:

```powershell
./gradlew.bat test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

Result: `BUILD FAILED`

Evidence from the failing run:

- `LargePortableContainerLogicTest > largePocketGeneratorFuelResolverTreatsJdtAndVanillaCoalBlocksAsBurnableFuel()` failed at `LargePortableContainerLogicTest.java:247`
  - `expected: <true> but was: <false>`
- `LargePortableContainerLogicTest > largePocketGeneratorBurnsJdtAndVanillaCoalBlocksAndProducesEnergy()` failed at `LargePortableContainerLogicTest.java:362`
  - `java.lang.IllegalStateException: Cannot get config value before config is loaded.`
  - stack entered `com.direwolf20.justdirethings.common.items.PocketGenerator.tryBurn(...)`

Interpretation:

- The dedicated large-generator fuel-resolution contract was not yet satisfied.
- The actual burn path still depended on the base JDT config-backed path in pure unit tests.

## Green: Focused

Command:

```powershell
./gradlew.bat test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

Result:

```text
> Task :test

BUILD SUCCESSFUL in 34s
9 actionable tasks: 3 executed, 6 up-to-date
```

What this now proves:

- `LargePocketGeneratorItem.resolveFuelBurnTime(...)` returns burnable values for both `Registration.CoalBlock_T1_ITEM` and `Items.COAL_BLOCK`.
- `LargePocketGeneratorItem.tryBurn(...)` increases stored energy and updates burn counter / max burn while consuming the fuel slot for both coal-block inputs.

## Green: Full

Command:

```powershell
./gradlew.bat test
```

Result:

```text
> Task :test

BUILD SUCCESSFUL in 18s
9 actionable tasks: 2 executed, 7 up-to-date
```

## Review follow-up

- Moved the zero-tick guard before fuel consumption so an unusable burn multiplier cannot consume fuel and then report failure.
- Added a non-fuel regression assertion (`minecraft:dirt` resolves to zero burn time).
- Re-ran the focused suite after the follow-up: `BUILD SUCCESSFUL in 37s`.
