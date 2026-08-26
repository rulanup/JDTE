# Task 3 BE Behavior Report

## Scope

- Baseline: `8765784` (parent `d17602d`)
- Production file: `src/main/java/com/jdte/common/blockentities/ExtendedExperienceHolderBE.java`
- Reference: local JDT `ExperienceHolderBE.java`

## Implementation

- Copied the JDT experience-holder behavior into the independent JDTE block entity while retaining `JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER` as its own `BlockEntityType`.
- Added the original settings, experience accounting, player deposit/withdrawal, level rounding, server/client ticks, orb collection, target-player selection, automatic experience balancing, and item-flow particle behavior.
- Restored the original default `PULSE` redstone mode and `isDefaultSettings()` behavior.
- Preserved the existing `AreaAffectingBE`, `FilterableBE`, and `RedstoneControlledBE` contracts, JDT-compatible NBT keys, and the independent XP fluid handler exposed by `getXpFluidHandler()`.
- The implementation neither extends nor instantiates JDT `ExperienceHolderBE`.

## Verification

- `.\gradlew.bat compileJava` — `BUILD SUCCESSFUL` (2 existing NeoForge deprecation warnings).
- `.\gradlew.bat test` — `BUILD SUCCESSFUL`.

No new test file was required for the copy slice: the requested full compile and existing test suite both pass, and no world behavior was removed for lack of a standalone world fixture.
