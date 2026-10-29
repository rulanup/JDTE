# Ultimate Time Wand and Entity Acceleration Upgrade

Pinned source: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`.
Runtime: Forge47.4.10 / Minecraft1.20.1 / JDT Forge1.1.4 / Java17 / Gradle8.6. Mod version remains0.5.8.

## Acceptance at the core checkpoint

Both rows remain **U** while graphical/packaged runtime acceptance is in progress. The complete regression passes **167 JUnit /236 required GameTests**, preserving all original150/217 tests and their assertions. See [expanded core evidence](expanded-core-checkpoint.txt) and [initial complete regression](core-checkpoint.txt). Compilation alone is not acceptance.

## Pinned behavior implemented

- Shift-use in air cycles Normal/X2/X4/MAX. Block use, including shift-use, applies acceleration. Vanilla interactive blocks may open their menu first; sneak-use the wand to bypass that interaction. There is no invented standalone wand GUI or extra cooldown.
- Exponent steps are1/2/4; MAX selects exponent10, or15 with one Ultimate Overclock attachment (1024x/32768x). Reapplication restores half the elapsed time, truncated, without exceeding the existing total. Saturation neither charges nor refreshes.
- Default duration600 loaded natural ticks; block requests submit the full multiplier, while entity effects perform multiplier minus one extra ticks after a completed natural server tick. The source entity-expiration order is retained: the final countdown tick performs no extra batch.
- Each activation buys its **full resulting multiplier**, using native JDT Time Wand FE/fluid rates and the source configurable factors. FE defaults to100 times the multiplier; Time Fluid defaults to0.5 times the multiplier divided by600. Fractional mB debt persists on the wand; positive fractions require actual Time Fluid. Disabling fractional settlement rounds each activation up. Creative bypasses payment. Caps are10000000FE/800000mB by default.
- Source shapeless base recipe creates a fresh empty wand. Loaded ingredients are consumed as in the pinned source; use empty materials. Inventory secondary click and the special crafting recipe attach one Entity card and/or one Ultimate Overclock without losing wand NBT. Smithing uses exactly one Entity card as template, one wand as base and one Time Fluid Catalyst as addition; NBT is preserved and duplicate attachment rejected.
- Entity effects support eligible living/nonliving targets (including TNT), exclude players/effect entities, persist with their world-owned target and follow real dimension transfer. They do not require the buying player to stay online. Start-tracking and periodic bounded snapshots synchronize client display data. Clients never execute the extra ticks.
- Block requests use the **existing** area-acceleration scheduler, including bounded queues, source server/local budget switch, AE2 priority, ordinary/random ticking, public proxy chains, loaded-target/identity checks and coalesced Ultimate scaling. No second conflicting scheduler was introduced.

## Forge safety fixes

Staged item-NBT payment and optimistic commit roll back failed marker creation/resource mutation without partial fluid/FE charges. Failed entity settlement restores actual effect absence rather than leaving a ghost exponent-zero attachment. Server-only/thread/reach/permission/held-item guards, nested activation rejection, extra-tick AI/travel scope cleanup, loaded-chunk/identity checks, same-server-tick UUID deduplication across dimension transfer, and server-stop cleanup prevent duplicate/recursive work and stale target access. Random-tick batches refetch the current block state instead of repeatedly ticking a replaced block's stale state.

## Reproduction

Use the configured Java17 and Gradle cache. Run `test build runGameTestServer`, `serverSmokeJar compileClientSmokeJava`, and the audit scripts. The production probe is in a separate JAR and never included in the actual mod.

- `JDTE_REMOTE_SCENARIO=wand JDTE_LIFECYCLE_SERVER=<marked loopback 25579 fixture> JDTE_SMOKE_PREBUILT=1 bash scripts/run_mineral_multiplayer_smoke.sh` drives two independent client JVMs and two successive production server JVMs using the existing captured remote launch.
- `JDTE_LIFECYCLE_SERVER=<marked loopback optional-absence fixture> JDTE_SMOKE_PREBUILT=1 bash scripts/run_ultimate_time_wand_lifecycle_probe.sh` runs two production JVMs without AE2/Curios/JEI/Jade, using a real FakePlayer item action for setup. This is distinguished from remote-player packet acceptance.
- Runtime fixtures use the source-supported6000-tick duration to allow login, actual unload and restart. GameTests assert the default600-tick contract and exact short expiry boundaries separately. No tested production algorithm is replaced by a fake.

## Failure chronology

- The first source compile used the NeoForge RenderHelpers signature; adapted the Forge signature and compiled successfully.
- First combined suite passed all new cases but three legacy AE tests reflectively expected an `execute(ServerLevel,int)` entry. Restored a real int-to-long delegation without changing assertions. A strict Bio herd limit also failed in the reused previous GameTest world; preserved that world, reran with a new world and retained the exact target assertions. Full167/229 and then167/236 runs pass.
- First real remote run passed actual one-card inventory attachment on both clients, but failed the strict assertion that four block clicks create a single exponent-four request. It is not counted as passed. The second diagnostic run showed `shift=false` on both server events and actual native FurnaceMenu instances, with exact unchanged wand resources: unfocused automation did not deliver the key binding. Correct the test driver to send the ordinary press-shift/use-block/release-shift packet sequence; retain the strict shared-request and resource assertions.
- The minimal-production test-only fixture initially used the Java Optional no-argument `orElseThrow`; adapted it to Forge LazyOptional's supplier API. The probe/client source sets compile again. The first minimal server also queried the entity index inside ServerStarted before world indexing; capture the real EntityJoin request instead, retaining the actual activation/production assertions.

## Pending boundaries

Real remote completion, packaged unload/restart and optional-absence results follow in later checkpoint sections. Long-running public servers, arbitrary third-party entities/tickers/data packs and the separately installed external JDTE-AE whole-grid backend remain unverified; native AE2 Inscriber ticking is a distinct verified GameTest path. No unrelated machine family is claimed complete by this work.
