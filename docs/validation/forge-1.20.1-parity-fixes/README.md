# Forge resource and tier-conversion parity corrections

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Starting checkpoint: `62a90b9`. Version remains **0.5.8-forge-1.20.1**.

## Generator checkpoint `a718716`

- **107 JUnit and 127 required GameTests passed**, including seven new policy tests and four actual native-generator tests. All prior 100/123 remain enabled. See `generator-selected.txt` for selected final task/result lines.
- Native fluid generation now preserves insufficient 0/1-mB supplies, requires full FE space, respects redstone and rolls back an unexpectedly short owned-buffer commit. Actual repeated attempts, BE save/load, exact/odd fuel and full/partial FE buffers are covered.
- Actual solid-generator tests exposed and fixed Forge-specific vanilla fuel resolution and the reserved Mixin-helper package. Two coal buy the source tripled pending FE; one coal remains untouched. Removing the fluid card retains native single-mB behavior.
- Initial failing runs and their fixes are recorded in the chronological development log. Generator Upgrade remains partial because its source Large Fuel Canister integration is absent; a native fix does not make the absent canister implemented.
- Combined production build/runtime, Extended Upgrade correction and portal-gun correction remain pending at this checkpoint. No graphical or multiplayer validation is claimed here.
