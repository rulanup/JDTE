# Item sender validation checkpoint

Source `ccf46472` (JDTE 0.6.5.2), target Forge 47.4.10 / JDT 1.1.4, version 0.5.8.
Base branch `muse/forge-1.20.1-ultimate-item-sender` (local `56b5a734`, remote
snapshot `00844e1e`); this round is on `muse/forge-1.20.1-sender-validation`.

## What this round adds

The Ultimate pilot (11 GameTests) verified card installation, capacity values,
10 ticker executions, the 64-per-execution normal cap, the 25000 standard cost
and direct-path movement, but left real-client GUI/JEI/creative visibility and
packaged dedicated-server lifecycle untested, so all three senders stayed P.
This round closes exactly those gaps, and checks the sender family's other
functions for uncovered behaviour.

### Remaining-function GameTests (`ItemSenderValidationGameTests`, 9 tests)

- Redstone modes: HIGH without a signal blocks transfer; LOW without a signal
  allows it (one ticker tick without a speed card moves 64, not the full stock).
- Creative card: moves the full 576 stock with zero FE on all three tiers,
  standard cost 0, no FE spent.
- Underclock: measured, not assumed. With an Underclock card, nothing moves
  in the first 39 ticker ticks and one 64-item batch moves on the 40th tick,
  on all three tiers.
- Area settings: a widened area (radius 3) reaches a target at dx=2, and the
  area settings survive save/reload.
- Range upgrades: max area radius doubles per Range card (x2, x4) on all tiers.
- Filter parity, recorded honestly: the original 0.6.5.2 sender code never
  applies its filter to `sendItems`, and neither does the Forge port, so a
  Filter card installs and sending still works, but the filter does not
  filter what is sent. This is original behaviour, not a Forge bug, and it is
  not fixed here; no claim is made that filtering works.
- Normal-path throughput with refill: five refilled ticker ticks move 576
  per tick (2880 total) for both Ultimate Overclock and standard Overclock.
  The 640/tick (Ultimate) and 20000/tick (standard Overclock) figures are
  theoretical caps; the measured bottleneck is the 9 x 64 = 576 internal
  stock, and the test asserts the stock limit, not the theoretical figure.
- Direct-path throughput with refill: five refilled ticker ticks move 2304
  per tick (11520 total) with one adjacent source chest plus internal stock,
  far below the 100000/tick theoretical ceiling. A single direct execution
  with 2304 available moves exactly 2304, below both the 10000 cap and the
  far targets' combined capacity.
- Survival break: a FakePlayer in SURVIVAL breaks a non-default sender
  (Ultimate cards installed, stock present) on all three tiers, leaves air
  with no block entity, and drops exactly one self item per tier. This
  test reproduces the production-server crash found below; it failed
  before the fixes and passes after them.

Two fixture lessons from this round, kept in the tests: target chests
accumulate across refill ticks and must be cleared between ticks, and a
single execution's 64 items go to the first target in cache order, so a
far-target test must remove the adjacent target chest first.

### Real client (`SenderClientSmoke`, Xvfb + Mesa, opt-in, never in the JAR)

- Opens the real Basic, Advanced and Extended GUIs (screen classes
  `BasicItemSenderScreen`, `AdvancedItemSenderScreen`,
  `ExtendedItemSenderScreen`).
- Upgrade slot counts through the real containers: 4 / 4 / 8; machine slots 9.
- GUI shift-click installs both Ultimate cards on Basic, and the
  server-installed Ultimate cards show in the Advanced and Extended GUIs;
  the server confirms Basic kept its GUI-installed cards and GUI-inserted
  stone afterwards.
- Baked models are present (not the missing model, particle texture present)
  for all three placed blockstates, and the client block entities exist.
- Creative tab contents contain all three sender block items; JEI
  ingredients contain all three sender items and both Ultimate cards.
- Success marker: `client/sender-client-smoke-success.txt` (text only; no
  screenshots are uploaded to the repository). The full client log
  (`client/client.log.gz`) is retained locally only, because the snapshot
  push carries UTF-8 text only; the repository record relies on the success
  marker and the code. The first client attempt was killed (exit 137) during
  world load after JEI and advancements had loaded; a retry with no other
  build running passed cleanly, so the kill is recorded as transient
  environment pressure, not as a sender failure.

### Dedicated server (`SenderLifecycleProbe`, packaged reobfuscated JAR)

- Fresh Forge 47.4.10 server, two JVMs, probe excluded from the production JAR.
- Seed: places all three senders, live Basic transfer moves all 576 stone to
  an adjacent chest on the real server, saves, unloads and reloads the chunk
  with Ultimate cards and FE intact (Advanced 1000000/2500000, Extended
  1234567/5000000), breaks Extended in SURVIVAL with a loot-table drop of
  exactly one self item and destroy-to-air, saves and shuts down
  (`dedicated-server/sender-seed-success`).
- Verify: a fresh JVM restores Basic and Advanced with Ultimate cards, FE
  and inventory intact (Advanced dirt x7), Extended stays broken, breaks
  Advanced the same way (`dedicated-server/sender-verify-success`).
- Production ItemEntity counting is not used, for the same probe-environment
  reason recorded for Fumo (it yielded zero even for a vanilla stone
  control); production drop evidence is loot-table drop plus destroy-to-air,
  and ItemEntity counting for a real break passes on the GameTest server.
- Stage-0 timing, measured: the live-transfer check waits to tick 200, not
  tick 41. A stale initial area-target cache plus the sender's doubling
  failure backoff (10 -> 20 -> 40 ticks) can push the first successful
  retry well past tick 41, and early seed runs failed at tick 41 with the
  sender simply not having moved yet. At tick 200 the transfer is complete
  (chest 576, sender empty, target in cache), so the tick-41 failures are
  recorded as a probe timing issue, not a transfer bug.

### Real problem found and fixed in this round (minimal scope)

Breaking a non-default sender in SURVIVAL crashed the production server
twice, with two different capability errors from the same save path. Root
cause: vanilla survival `destroyBlock` removes the block entity
(invalidating its capabilities) BEFORE `dropResources`, and JDT `getDrops`
then calls `saveAdditional` on the removed block entity.

1. `Missing JDTE filter capability` (first crash): JDT
   `BaseMachineBE.saveAdditional` reads `getFilterHandler()` for every
   Filterable machine. The original 0.6.5.2 stores the filter as a data
   attachment, which survives removal; the Forge port looks it up through
   a capability, which throws after removal. Fix, sender-only:
   `ItemSenderBE` now caches its filter handler at construction (the same
   object the provider hands out, so capability reads and NBT stay
   consistent) and `getFilterHandler()` returns the cache. An earlier
   constructor-only `getFilterHandler()` call did not fix this, because it
   established storage but left the post-removal lookup throwing; the
   crash reproduced identically after that change, which is how the real
   mechanism was identified.
2. `Missing JDTE upgrade capability` (second crash, surfaced by the new
   GameTest): vanilla `BlockEntity.saveAdditional` serializes capabilities,
   and `JDTEAttachments.MachineDataProvider.serializeNBT` calls
   `initializeMachineEnergy()`, which reads `getMaxEnergy()`, which reads
   upgrade cards through the capability. A powered machine that was never
   serialized before removal throws here. Fix: `initializeMachineEnergy()`
   now skips the capacity sync when the provider is invalidated. The
   stored energy value is already correct from the machine's lifetime, so
   skipping the sync on a dead provider changes nothing during normal
   operation; it only removes the post-removal crash path. This guard
   lives in shared provider code but its behaviour change is confined to
   the post-removal path.

Both fixes are covered by the new survival-break GameTest (all three tiers)
and by the seed/verify probe runs, which break Extended and Advanced in
SURVIVAL on the production server. No transfer logic, energy logic, or
Ultimate behaviour was changed; the other 27 Ultimate-restricted families
are untouched, and the same latent post-removal save path likely affects
other Filterable/powered machines (their `getFilterHandler` and energy
init go through the same capabilities), which is recorded as a known
limitation for a later round, not fixed here.

## Limitations and open items

- Filter slots are present in the GUI and a Filter card installs, but the
  filter does not filter what is sent, in both the original and the port
  (see above). This is documented, not hidden, and not changed in this
  validation-focused round.
- PULSE redstone mode is not exercised with a real rising edge; HIGH, LOW
  and IGNORED (default) are covered.
- The 10000-per-execution direct cap is not saturated in a test: the
  largest measured single direct execution is 2304 (one adjacent source
  plus internal stock), and the 100000/tick ceiling is theoretical. The
  test asserts measured movement, not the ceiling.
- Production code changed in this round, and only for the real problem
  found: the cached filter handler in `ItemSenderBE` and the invalidated
  guard in `JDTEAttachments.initializeMachineEnergy()` (see above). No
  transfer logic, energy logic, or Ultimate behaviour was changed, and
  the other 27 Ultimate-restricted families are unchanged.
- The same post-removal save path likely affects other Filterable and
  powered machines (they share the capability lookups), but they are out
  of scope for this sender round and are not fixed here; only senders
  have survival-break evidence after the fixes.

## Promotion decision

All three senders meet the full validation standard after this round:
GameTest coverage of Ultimate behaviour plus the remaining functions,
real-client GUI/creative/JEI/model evidence, and packaged dedicated-server
lifecycle evidence (placement, live transfer, drops, saving, chunk unload
and reload, restart recovery, cards and FE persistence). All three move
P->V. Counts become V 58, U 19, P 43, N 0. Version stays 0.5.8, no main merge
or release.
