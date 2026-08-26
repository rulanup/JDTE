# Task 4 Report

## Status

DONE

## Summary

Implemented the extended ordinary JDT energy transmitter vertical slice in
`D:/MCDev/idea-projects/JDTE/.worktrees/extended-jdt-machines`.

- Added `ExtendedEnergyTransmitterBE` with `EnergyTransmitterBE` as its direct parent and
  `ExtendedUpgradeMachine` as its upgrade marker.
- Added a dedicated six-facing metal block, JDTE menu/container, ordinary-transmitter screen,
  JDTE settings payload, and server handler.
- Registered the block, item, block entity type, menu, screen, ordinary JDT energy transmitter
  renderer, creative-tab entry, network payload, and capabilities.
- Kept the implementation isolated from `AdvancedEnergyTransmitterBE`, the advanced block/menu/screen,
  AE2 integration, player charging, and advanced transmitter configuration.

## Behavior and Registration

- `ExtendedEnergyTransmitterBE` calls
  `super(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), pos, state)` and therefore reuses the
  ordinary JDT transmitter's target discovery, energy transfer and balancing, filters, area settings,
  redstone settings, facing behavior, persistence, and particles.
- `ExtendedEnergyTransmitterBlock` mirrors the ordinary JDT transmitter's metal properties, six voxel
  shapes, `FACING` placement, occlusion/skylight behavior, menu opening, and block-entity validation.
- `ExtendedEnergyTransmitterContainer` retains the ordinary container's player slots, quick move, and
  removal behavior while binding only the JDTE menu and block.
- `ExtendedEnergyTransmitterScreen` retains the ordinary show-particles button and inherited common
  redstone/area/filter controls, but sends `ExtendedEnergyTransmitterSettingPayload` instead of JDT's
  ordinary payload.
- The JDTE packet handler accepts the setting only while the player has an
  `ExtendedEnergyTransmitterContainer` backed by `ExtendedEnergyTransmitterBE` open.
- The energy capability uses a dedicated extended-transmitter provider and is exposed only on the
  block state's `FACING` side. The item capability uses the same machine-handler provider as the JDT
  ordinary transmitter. It does not use the all-sided powered-machine provider.

## Tests

`ExtendedJdtMachineBehaviorTest` now verifies:

- direct parent class is JDT `EnergyTransmitterBE`
- the extended instance is not `AdvancedEnergyTransmitterBE`
- eight upgrade slots and the `ExtendedUpgradeMachine` marker
- its own block entity type, block ID, item ID, and menu ID
- the existing advanced block still creates `AdvancedEnergyTransmitterBE`
- the new source slice contains no `AdvancedEnergyTransmitter`, `AE2`, or `PlayerCharger` references

The first targeted run reached the new production code but failed during test compilation because Java
rejects a statically impossible sibling-class `instanceof`. Casting the test subject to `Object` retained
the required runtime isolation assertion. The next targeted run passed.

## Verification

- `./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest` —
  `BUILD SUCCESSFUL`
- `./gradlew compileJava` — `BUILD SUCCESSFUL`
- `git diff --check` — no whitespace errors

Gradle reported only existing deprecation warnings from NeoForge event-bus APIs and optional Curios APIs.
Resources and recipes were intentionally left for the later resource slice, as allowed by Task 4.
