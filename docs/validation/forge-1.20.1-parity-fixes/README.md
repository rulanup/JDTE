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
