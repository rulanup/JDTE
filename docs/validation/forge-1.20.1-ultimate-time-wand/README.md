# Ultimate Time Wand and Entity Acceleration Upgrade

Pinned source: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`.
Runtime: Forge 47.4.10 / Minecraft 1.20.1 / JDT Forge 1.1.4 / Java 17 / Gradle 8.6. Mod version remains0.5.8.

## Current acceptance

Both items receive scoped **V**: all source gameplay is implemented and the complete **167 JUnit /238 required GameTests** pass, with all original 150/217 retained and no skips. The final runtime JAR passes both independent graphical clients, two production server JVMs, and another two production JVMs without AE2/Curios/JEI/Jade. Full build and both JAR reobfuscations pass. Earlier checkpoint paragraphs below retain their then-pending status.

- [Final real multiplayer run](multiplayer/run-summary.txt), [minimal production run](production/run-summary.txt), [exact aggregate resources](multiplayer/ledger-60), [natural work](multiplayer/natural-ledger).
- [Actual x16/countdown renderer and two visible wands](multiplayer/jdte-wand-active-Two-4.png), [restored renderer](multiplayer/jdte-wand-active-Two-10.png), [inventory attachment](multiplayer/jdte-wand-inventory-One.png), [JEI base crafting](multiplayer/jdte-wand-jei-crafting.png), [JEI source smithing](multiplayer/jdte-wand-jei-smithing.png).
- [Full Gradle/GameTest log](full-build-gametest.log.gz), [JUnit totals](junit-summary.json), [original JUnit XML](junit-results.zip), [artifact integrity](runtime-artifact-integrity.json). Complete seed/restart/client logs are retained as gzip in multiplayer/production; failures have their own folder.
- Only four in-game guide resources changed after those runtime runs, removing the stale unfinished-wand notice and clarifying sneak-use for interactive blocks. Final build/JUnit/reobfuscation passes; ZIP comparison proves **every class and all non-guide resources byte-identical**. Both runtime installed mod JAR hashes match the original build. Tests/probes remain separate from the production mod.

## Acceptance at the core checkpoint

Both rows remain **U** while graphical/packaged runtime acceptance is in progress. The complete regression passes **167 JUnit /236 required GameTests**, preserving all original 150/217 tests and their assertions. See [expanded core evidence](expanded-core-checkpoint.txt) and [initial complete regression](core-checkpoint.txt). Compilation alone is not acceptance.

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

Use the configured Java 17 and Gradle cache. Run `test build runGameTestServer`, `serverSmokeJar compileClientSmokeJava`, and the audit scripts. The production probe is in a separate JAR and never included in the actual mod.

- `JDTE_REMOTE_SCENARIO=wand JDTE_LIFECYCLE_SERVER=<marked loopback 25579 fixture> JDTE_SMOKE_PREBUILT=1 bash scripts/run_mineral_multiplayer_smoke.sh` drives two independent client JVMs and two successive production server JVMs using the existing captured remote launch.
- `JDTE_LIFECYCLE_SERVER=<marked loopback optional-absence fixture> JDTE_SMOKE_PREBUILT=1 bash scripts/run_ultimate_time_wand_lifecycle_probe.sh` runs two production JVMs without AE2/Curios/JEI/Jade, using a real FakePlayer item action for setup. This is distinguished from remote-player packet acceptance.
- Runtime fixtures use the source-supported 6000-tick duration to allow login, actual unload and restart. GameTests assert the default 600-tick contract and exact short expiry boundaries separately. No tested production algorithm is replaced by a fake.

## Failure chronology

- The first source compile used the NeoForge RenderHelpers signature; adapted the Forge signature and compiled successfully.
- First combined suite passed all new cases but three legacy AE tests reflectively expected an `execute(ServerLevel,int)` entry. Restored a real int-to-long delegation without changing assertions. A strict Bio herd limit also failed in the reused previous GameTest world; preserved that world, reran with a new world and retained the exact target assertions. Full167/229 and then167/236 runs pass.
- First real remote run passed actual one-card inventory attachment on both clients, but failed the strict assertion that four block clicks create a single exponent-four request. It is not counted as passed. The second diagnostic run showed `shift=false` on both server events and actual native FurnaceMenu instances, with exact unchanged wand resources: unfocused automation did not deliver the key binding. Correct the test driver to send the ordinary press-shift/use-block/release-shift packet sequence; retain the strict shared-request and resource assertions.
- The minimal-production test-only fixture initially used the Java Optional no-argument `orElseThrow`; adapted it to Forge LazyOptional's supplier API. The probe/client source sets compile again. The first minimal server also queried the entity index inside ServerStarted before world indexing; capture the real EntityJoin request instead, retaining the actual activation/production assertions.

## Historical pending boundaries at the core checkpoint

Real remote completion, packaged unload/restart and optional-absence results follow in later checkpoint sections. Long-running public servers, arbitrary third-party entities/tickers/data packs and the separately installed external JDTE-AE whole-grid backend remain unverified; native AE2 Inscriber ticking is a distinct verified GameTest path. No unrelated machine family is claimed complete by this work.

## Completed runtime checkpoint before the random-target correction

Both real client JVMs and both production JVMs completed the full remote scenario (fourth attempt). JEI actually displayed the base crafting and source smithing layouts. Minimal optional-absence seed/restart also passed exact restored NBT and resumed natural work. The test driver now uses normal crouch-command/use/release packets, counts biological work separately from vanilla natural tickCount, waits for real entity indexing and isolates every seed. Initial overlapping client stands obscured the block renderer screenshot; final evidence separates their positions.

The additional deterministic real sugar-cane test exposed Forge JDT1.1.4's unconditional null-BE rejection, differing from pinned1.5.7. An explicit random-target compatibility path now preserves deny tags and Forge regex blacklist rules. Current-state reads on each iteration avoid repeated stale growth. Full 167/238 passes after this fix; final same-JAR runtime acceptance is next.

## Final runtime details and remaining boundaries

Two independent development-mapped Forge clients connect to the actual separately reobfuscated dedicated-server mod; no integrated-server shortcut. Both survival players use real inventory/air/block/entity protocol paths, attach exactly one of two cards, echo all four modes, race four shared block activations and four living activations, and observe one exponent-four request/effect. Source costs aggregate to **19994000 FE remaining and0.05 mB Time Fluid spent including saved fractions**; loose/attached cards and custom wand NBT are conserved. Actual natural work over 42 ticks advances biological age 672 and decrements the effect 42, with no repeat payment. The furnace really smelts and conserves64 total raw/output items.

One original player disconnects/reconnects, then both leave; both boundary chunks actually unload. A second dedicated JVM checks the exact furnace state before natural ticks and the exact creature age/effect and request state during load, then both original clients restore exact inventory, FE, Time Fluid and fractional debt. The same final gameplay build passes isolated optional-absence two-JVM activation/natural work/unload/recovery; FakePlayer setup there is distinct from the real remote-player test. Source6000-tick configuration permits login/unload/restart; the default 600 behavior is separately tested.

Native AE2 Inscriber work, deterministic real sugar-cane growth and Forge blacklist rejection, shared area/wand/coalesced Ultimate work, generic TNT, true Nether transfer and same-tick UUID protection, exact source expiry order, permission/reach, real canceled/mutating Forge joins, held-item/valid-BE replacement rollback, recipe/NBT and actual SmithingMenu quick-move are GameTest paths. No original assertion or test was removed.

The new random-block test initially failed because Forge JDT1.1.4 rejects null BEs unconditionally. Pinned JDT1.5.7 bytecode permits random-only targets. The explicit compatibility path retains both deny tags and Forge regex blacklist, and all238 tests pass after the correction. All failed attempts remain failures in the chronology/logs.

Still unverified: prolonged public-server uptime, arbitrary external entities/tickers/data packs and the separately installed external JDTE-AE whole-grid backend. Independent two-client Entity Accelerator **machine menus**, full operating reactors and Draconic fusion are separate scopes; wand acceptance does not promote them. Source base-crafting fresh output and exact-one catalyst smithing behavior are deliberately retained.
