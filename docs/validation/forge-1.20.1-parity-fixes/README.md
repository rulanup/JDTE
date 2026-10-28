# Forge resource and tier-conversion parity corrections

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Starting checkpoint: `62a90b9`. Version remains **0.5.8-forge-1.20.1**.

## Generator checkpoint `a718716`

- **107 JUnit and 127 required GameTests passed**, including seven new policy tests and four actual native-generator tests. All prior 100/123 remain enabled. See `generator-selected.txt` for selected final task/result lines.
- Native fluid generation now preserves insufficient 0/1-mB supplies, requires full FE space, respects redstone and rolls back an unexpectedly short owned-buffer commit. Actual repeated attempts, BE save/load, exact/odd fuel and full/partial FE buffers are covered.
- Actual solid-generator tests exposed and fixed Forge-specific vanilla fuel resolution and the reserved Mixin-helper package. Two coal buy the source tripled pending FE; one coal remains untouched. Removing the fluid card retains native single-mB behavior.
- Initial failing runs and their fixes are recorded in the chronological development log. Generator Upgrade remains partial because its source Large Fuel Canister integration is absent; a native fix does not make the absent canister implemented.
- Combined production build/runtime, Extended Upgrade correction and portal-gun correction remain pending at this checkpoint. No graphical or multiplayer validation is claimed here.


## Extended Upgrade checkpoint `b2d329e`

- **112 JUnit / 133 required GameTests** and full build/production/probe reobfuscation passed. Prior 107/127 remain enabled. Added five pure migration-planning tests and six actual Forge tests; selected final results are in `tier-selected.txt`.
- Every one of the **15 available** source/target routes uses real item interaction, four tagged cards, actual physical inventories, nearly full FE, applicable fluid tanks, the live item interface, a usable eighth upgrade slot and a fresh-BE save/load check. Native T2 filter pages and actual FE reach the target's effective storage interfaces.
- Failure tests cover wrong/denied replacement, nonempty inactive storage and actual Forge placement event cancellations for native placer and dropper. They conserve the source NBT/actual FE and held card, with one restored inventory and no second inventory/FE remaining in retained old references.
- Inherited normal FE capacity, the extended dropper's persistent actual FE and both fluid machines' source-T2 32,000-mB base are corrected. Source/destination preparation precedes mutation and source buffers are cleared only as part of a successful move or restored ownership after rollback. Unexpected rollback errors propagate.
- Five source conversion targets are absent. Ultimate cards are still restricted outside their currently verified hosts; a tier-conversion test is not proof of full machine operation or Ultimate parity. Production two-JVM checks are next; no new real graphical/multiplayer result is claimed at this checkpoint.


## Final production checkpoint `f9c4a57`

The exact reobfuscated production JAR from gameplay checkpoint `b2d329e` passed **two actual dedicated-server JVMs**, with JDT Forge 1.1.4 and GuideME only (AE2, Curios, JEI and Jade absent). Both final processes reached `Done`, normally saved all dimensions and exited **0**. `summary.json` records the production/probe hashes, retained suite counts and explicit limits. Selected actual logs and success markers are adjacent.

The seed performs **all 15 conversions through real item use**, with physical tagged inventories, four tagged upgrades, almost-full FE, applicable almost-full tanks and actual native filter templates. It checks the active eight-slot inventory, the empty inactive four-slot handler, a usable eighth slot, and emptied detached original interfaces. A real Forge event cancels a native dropper replacement; exact source NBT/actual owned FE and the upgrade card are retained, then the real conversion succeeds. The fluid generator naturally retains its last one mB, consumes only one complete two-mB batch from three mB and saves its one-mB remainder.

The fresh JVM compares **complete saved NBT for every one of the 15 converted BEs and the generator**, then compares it again after natural ticks. Actual live Forge energy/item capabilities withdraw from the one saved inventory, and all eighth slots remain active. Two hundred further attempts with a one-mB remainder produce no FE and consume no fuel. This tests actual disk shutdown/restart, not only in-memory `loadStatic`.

The first production fixture incorrectly cast SensorT2 to RedstoneControlledBE. It is not redstone-controlled; the test-only pause helper now checks the interface, and the final sensor participates in the same full-state reload/live-capability checks as every other machine. The failed attempt is recorded separately and is not counted as successful. No gameplay change or skipped sensor path was used to pass.

### Reproduce production checks

Reuse an existing disposable Forge 47.4.10 server marked `.jdte-lifecycle-fixture`, bound to `127.0.0.1`, with `level-name=lifecycle-world`. The seed replaces its own fifteen fixture positions from `(3104,90,3104)` to `(3120,90,3112)` plus the generator at `(3128,90,3104)`. The script refuses an unmarked server and installs no tools or dependencies.

```bash
export JDTE_LIFECYCLE_SERVER=/path/to/disposable/forge-server
bash scripts/run_tier_conversion_lifecycle_probe.sh
```

Use `JDTE_SMOKE_PREBUILT=1` after a completed build to avoid rebuilding unchanged artifacts. Full logs, expected NBT and the disposable world stay outside Git. New graphical/two-client tests, absent conversion targets, whole-grid JDTE-AE, prolonged multiplayer and full reactor operation remain unverified or unimplemented. Generator/Extended Upgrade remain P for the source features still dependent on absent items/machines; tested native fixes do not imply full source parity.
