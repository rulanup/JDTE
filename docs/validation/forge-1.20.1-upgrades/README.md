# Crafting, conversion and AE upgrade validation

Pinned source: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Starting branch commit: `8563466`. Version: `0.5.8-forge-1.20.1`.

The final development suite passed **77 JUnit tests** and **85 required Forge GameTests**, including all earlier 58/57 tests. `test build runGameTestServer compileClientSmokeJava` completed successfully under the existing Java 17 / Gradle 8.6 setup. Both the production JAR and separate lifecycle probe JAR were reobfuscated.

See the [port checkpoint](../../forge-1.20.1-port.md#multiplayer-conversion-and-ae-upgrades-checkpoint-2026-10-27) for implementation details and test coverage. Runtime reruns are in progress and will be recorded here with their actual outcomes before this checkpoint is committed.

## Actual failure investigations

- Real AE network fixtures initially pointed the drive's non-connectable front toward the power source. Explicit AE2 orientation fixed connectivity. This did not require weakening network assertions.
- The FE fixture initially selected an item without an energy capability. It now uses an actual JDT Eclipse Alloy Pickaxe and an actual Applied Flux FE storage cell.
- Registry-dependent cache tests were moved to Forge GameTests after plain JUnit correctly reported that Minecraft was not bootstrapped. The final JUnit and GameTest counts exclude no failing assertion.
- A Factory Packer test exposed `ConcurrentModificationException` in Minecraft's mutable world-path cache, called from an asynchronous storage worker. Production code now passes a server-thread-captured path to asynchronous I/O. A new test performs eight concurrent write/claim/read/release/delete operations.
- A replacement-controller regression exposed an invalid-capability crash from a stale queued automatic output entry. Both adjacent-output and AE-output paths now verify the actual current loaded block entity. The test clears the old removed flag deliberately and verifies only the replacement can export the one live inventory.

- A stripped development client exposed premature AE2/Applied Flux class loading while Forge discovered GameTests. Optional integration tests now use conditional `RegisterGameTestsEvent` registration and separate AE2/Applied Flux/Curios classes. All 85 tests remain required in the full configured runtime; absent APIs are never loaded by discovery.
- The graphical screenshot exposed clipped English pattern-button labels; they now use the context-appropriate short labels “Save” and “Clear”.

## Limits

A genuinely submitted, running AE crafting CPU job controlling each supported machine family is not tested. Idle real-grid detection, CPU state decisions, five-tick caching and invalidation are tested separately. Full optional-mod combinations, long-running throughput and every equipment/recipe-pack combination remain unverified.
