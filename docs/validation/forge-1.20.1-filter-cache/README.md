# Post-removal filter-cache fix

Source `ccf46472` (JDTE 0.6.5.2), target Forge 47.4.10 / JDT 1.1.4, version 0.5.8.
Base branch `muse/forge-1.20.1-sender-validation` (local `a03fec99`, remote
snapshot `fc079887`); this round is on `muse/forge-1.20.1-filter-cache`.

## The hazard

The sender-validation round found and fixed a production-server crash:
vanilla survival `destroyBlock` removes the block entity (invalidating its
Forge capabilities) BEFORE `dropResources`, and JDT `getDrops` then calls
`saveAdditional`, which reads `getFilterHandler()` for every `FilterableBE`.
In the Forge port, that method delegated to the `JDTEAttachments.filter`
capability lookup, which throws `Missing JDTE filter capability` once the
block entity is removed. The trigger is any non-default machine: JDT
`isDefaultSettings()` returns false as soon as any filter slot holds an
item, and also when area, redstone, tick-speed, or stored-energy settings
differ from defaults.

A second throw on the same path (provider energy initialization reading
the upgrade capability during capability serialization) was already fixed
globally in the sender round by skipping initialization on an invalidated
provider, so this round fixes only the filter side.

## Audit: exactly 22 vulnerable root files

Every `getFilterHandler()` implementation that delegates to
`JDTEAttachments.filter` was enumerated; the list below is complete for
the current tree. Fixing a root covers its whole family because tier
shells inherit the root constructor and getter.

| Root file | Family covered by the fix |
|---|---|
| `AdvancedEnergyTransmitterBE` | Advanced energy transmitter (single) |
| `AdvancedEntityAcceleratorBE` | Advanced + Extended entity accelerators |
| `AdvancedItemCollectorBE` | Advanced item collector (single) |
| `BioExtractorBE` | Advanced + Extended bio extractors |
| `EntitySuppressorBE` | Entity suppressor (single) |
| `ExtendedBlockBreakerBE` | Extended block breaker |
| `ExtendedBlockPlacerBE` | Extended block placer |
| `ExtendedBlockSwapperBE` | Extended block swapper |
| `ExtendedClickerBE` | Extended clicker |
| `ExtendedFluidCollectorBE` | Extended fluid collector |
| `ExtendedFluidPlacerBE` | Extended fluid placer |
| `FluidReceiverBE` | Basic + Powered fluid receivers |
| `FluidSenderBE` | Basic + Powered fluid senders |
| `FluidStabilizerBE` | Basic + Powered fluid stabilizers |
| `GelGeneratorBE` | Advanced + Extended gel generators |
| `GlueActivatorBE` | Basic + Powered glue activators |
| `ItemReceiverBE` | Basic + Powered item receivers |
| `LifeBreederBE` | Life breeder (single) |
| `LifeExtractorBE` | Advanced + Extended life extractors |
| `MineralExtractorBE` | Mineral + Large mineral extractors |
| `RangeBlockerBE` | Range blocker (single) |
| `TimeAcceleratorBE` | Basic + Advanced + Extended time accelerators, Crystal Incubator |

Each root's `saveAdditional` was also checked: after the `super` call they
only serialize fields (inventories, tanks), so the filter handler was the
only capability touch on the post-removal save path.

Deliberately NOT changed:

- `ItemSenderBE`: already fixed in the sender-validation round.
- `ExtendedSensorBE`: its `getFilterHandler()` delegates to its JDT parent
  `SensorT1BE`, which returns a field handler (verified by bytecode), so it
  never touches a capability on this path.
- `ExtendedEnergyTransmitterBE`: not a `FilterableBE`; it is not on this
  save path at all.
- Any machine that is not `FilterableBE`: JDT `saveAdditional` only reads
  the filter handler for `FilterableBE` instances.

## The fix

The same minimal pattern as the sender fix, applied per root:

1. A `private final FilterBasicHandler cachedFilterHandler` field.
2. Assignment in the constructor, immediately after `super(...)`, from
   `JDTEAttachments.filter(this)`. For the two roots whose constructors
   previously called `getFilterHandler()` to "establish" storage
   (`BioExtractorBE`, `AdvancedEntityAcceleratorBE`), that call is kept
   after the assignment: their getters do more than fetch, they resize
   the handler to the maximum filter slots via a serialize/deserialize
   round-trip on every call, and pre-existing tests depend on the handler
   being sized at construction. Replacing the call with the bare
   assignment broke two pre-existing tests, which is how the side effect
   was found. Retaining the handler object is what survives removal;
   the getter call alone does not.
3. `getFilterHandler()` returns the cache, falling back to the capability
   lookup only if called before the constructor assignment (defensive; no
   such call exists in the current code).

The cached handler is the same object the provider hands out, so
capability reads, GUI edits, and NBT serialization/deserialization during
the machine's lifetime are unchanged. No transfer, energy, upgrade, or
Ultimate behaviour is touched, and no JDT code is modified.

## GameTests (`FilterCacheSurvivalBreakGameTests`, 23 tests)

One test per fixed root family. Each test places one representative block,
puts a stone stack in filter slot 0 (the simplest universal non-default
trigger, independent of upgrade compatibility), breaks it with a survival
FakePlayer via `gameMode.destroyBlock`, and asserts the break succeeds,
leaves air with no block entity, and drops exactly one self item. A 23rd
test (`lifeBreederDropLogicYieldsSelfItem`) covers the life breeder's
drop logic directly: `getDrops` yields exactly one self item, and
`level.destroyBlock` drops exactly one new self item.

Fixture note: the FakePlayer holds an iron pickaxe. `ServerPlayerGameMode.
destroyBlock` only calls `playerDestroy` (and therefore drops anything)
when `canHarvestBlock` is true, and the life breeder is the only one of
the 22 representatives with `requiresCorrectToolForDrops()`. A
bare-handed break correctly removes the block but drops nothing, which
is vanilla behaviour, not a missing drop; this was confirmed by bytecode
inspection of `destroyBlock` and by the direct-`getDrops` and
`level.destroyBlock` paths both yielding one self item. The pickaxe does
not change any other family's drops.

## Tag gap found and fixed (Life Breeder mineable tag)

While diagnosing the life breeder's GameMode-path drop count, the
survival test showed that even an iron pickaxe is not a correct tool
for it in this environment: the loaded `mineable/pickaxe` tag (2298
entries) contained none of the port's singular-path entries. In this
Forge 1.20.1 setup the effective block-tag path is the plural
`data/minecraft/tags/blocks/mineable/pickaxe.json`; the singular
1.21-style `data/minecraft/tags/block/mineable/pickaxe.json` (which does
list `jdte:life_breeder`) is not loaded. The plural file listed only 7
JDTE entries and lacked `life_breeder`, while the original JDTE tag
lists it. With user approval, this round adds only `jdte:life_breeder`
to the plural file, restoring original drop behaviour for survival
pickaxe breaks. This data fix alone promotes no machine's status, and
the dedicated-server spot check below verifies the tag on the packaged
server directly (loaded-tag membership plus `hasCorrectToolForDrops`).
Other machines from the original tag that are absent from the plural
file (for example time accelerators and entity accelerators) do not
carry `requiresCorrectToolForDrops()`, so their absence has no drop
effect and is left for a later data-completeness pass, not fixed here.

Representatives: advanced energy transmitter, advanced entity accelerator,
advanced item collector, advanced bio extractor, entity suppressor,
extended block breaker / placer / swapper / clicker / fluid collector /
fluid placer, basic fluid receiver / sender / stabilizer, advanced gel
generator, basic glue activator, basic item receiver, life breeder,
advanced life extractor, mineral extractor, range blocker, basic time
accelerator.

## Regression and status

- Full regression: compileJava, 196 JUnit, 373 GameTests (350 + 22 + 1),
  jar/reobfJar all passed; final JAR `[Forge] jdte-0.5.8-forge-1.20.1.jar`
  = 3,466,511 bytes. The main sources in the committed tree are identical
  to the sources that passed this regression; only documentation and the
  serverSmoke probe changed afterwards, and the probe itself was rebuilt
  and exercised by the dedicated-server runs below.
- Dedicated-server spot check (`FilterCacheLifecycleProbe`, seed/verify):
  both markers pass and are committed under
  `dedicated-server/filter-seed-success` and
  `dedicated-server/filter-verify-success`. Seed placed Advanced
  BioExtractor, Basic Time Accelerator, Extended Block Breaker and Life
  Breeder non-default (stone in filter slot 0), verified, and saved.
  A fresh JVM restored all four with filter stone intact, confirmed
  Life Breeder in the loaded `mineable/pickaxe` tag with an iron pickaxe
  as its correct tool, and survival-broke the Block Breaker and the Life
  Breeder, each leaving air with no block entity and no crash.
- No migration-matrix status changes come from this round alone: fixing a
  crash does not count as completing a machine's validation, so every
  affected item keeps the status it had before this round. Counts stay
  V 58 / U 19 / P 43 / N 0.

## Limitations

- The GameTest survival break uses the GameTest server's FakePlayer path,
  which is the same vanilla `destroyBlock` -> `dropResources` ->
  `getDrops` -> `saveAdditional` code path as production; the dedicated
  server spot check above exists to confirm it on a packaged server.
- Filter contents themselves are not asserted as drops: the tests count
  only the self block item, matching the sender test's convention.
- The packaged-server probe originally waited for a natural chunk
  unload/reload between seed and break, but the probe chunk never
  unloaded within 7200 ticks despite being unforced and absent from
  the world's forced-chunk list (verified in `chunks.dat`); only the
  shutdown-time unload fired. The probe therefore uses the strictly
  stronger save/restart round-trip instead, which exercises the same
  `saveAdditional` serialization path and keeps the survival break
  after reload as the production shape of the original crash. Chunk
  unload timing in this probe environment is an open anomaly, not a
  product claim; the sender round's server probe did observe a chunk
  unload/reload for senders in the same world.
- The remaining 27 Ultimate-restricted machines are untouched by this
  round and remain a separate, user-approved-later topic.
