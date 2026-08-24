# Task 2 report: large portable generator coal-block fuel

## Root cause

JDT's pocket generator requires `ItemStack.getBurnTime(RecipeType.SMELTING())` before it initializes a burn. JDT coal blocks expose their custom burn-speed multiplier, but can lack the vanilla fuel tag/time used by that gate. The large generator also inherited JDT's private initialization path, so it could not provide a fallback.

## TDD evidence

- Red: the regression test for JDT and vanilla coal blocks failed before the large-generator resolver existed; the inherited burn path also failed to initialize the test fuel.
- Green: `./gradlew.bat test --tests com.jdte.common.items.LargePortableContainerLogicTest --no-daemon` — `BUILD SUCCESSFUL in 1m`.

## Implementation

- Added `PortableFuelBurnSpeedHelper.resolveBurnTime(...)` with vanilla and JDT coal/coal-block fallbacks.
- LargePocketGeneratorItem now owns its burn initialization path, using the resolved burn time and existing multiplier helper while preserving the JDT counter, max-counter, fuel consumption, and energy flow semantics.
- Added regression coverage for JDT and vanilla coal blocks and their burn-time equivalence.
