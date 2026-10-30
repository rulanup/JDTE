# Extended Experience Holder validation

Reference: JDTE0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Forge47.4.10/JDT Forge1.1.4/Java17/Gradle8.6; unchanged version0.5.8. Implementation checkpoints: `114f578`, `63153c4`, `257adc4`, **`804fed9`**.

## Current acceptance

**Scoped V** for `extended_experience_holder` and the completed `extended_upgrade` mapping. Full inventory: **V49/U19/P46/N6**,114/120 items registered. This does not certify arbitrary mod combinations or prolonged public-server operation.

| Verification | Actual result | Retained evidence |
| --- | --- | --- |
| JUnit |173, zero failures/errors/skips; original170 retained | `junit-summary.json`, `junit-results.zip` |
| Forge GameTest |270 required pass; original252 and original24 mineral tests unchanged | `pulse-regression.log.gz` |
| Build | Full build, main/probe reobfuscation, client compilation pass | Same complete Gradle log |
| Real graphical client | Native mouse controls,32-bit XP/settings, eight slots, actual JEI/Jade, real player XP/target work pass | `client-launch.log.gz`, `client-runtime.log.gz`, `client-success.txt`, five PNGs |
| Production JVM1 | Conversion, merged orbs, full-inventory survival transport, actual unload/new BE, complete saved state pass | `production-seed.log.gz`, `experience-seed-success` |
| Production JVM2 | Exact NBT before work; naturally resumed33XP pass | `production-verify.log.gz`, `experience-verify-success`, `experience-expected.nbt` |
| Artifact | Built main SHA256 matches installed production JAR; client/probe classes excluded | `artifact-integrity.json` |
| Audit/resources | Regeneration and `--check` pass;991 language keys/68 blocks/50 guides validate | Matrix/JSON and chronological port log |

Runtime scripts assert successful Java exits and success markers before printing PASS. Test fixtures are separate from the shipping main JAR.

## Source contract and implementation

Read the pinned standalone BE/block/menu/screen/renderer/packets/copier/recipes, plus actual native Forge XP utilities/capabilities/block/menu. Potion Canister is independent and investigated without registration.

- Integer XP is canonical, capacity2147483647XP, no FE or separate fluid buffer. Only JDT XP fluid is accepted at20mB/XP. Forge's int mB projection saturates; fill/drain transfer whole XP without overflow. Capacity/Fluid cards do not raise the fixed XP limit.
- Manual controls transfer1 level, Shift10, Ctrl all, with a partial current level counted first. Target0–1000, source default Pulse acquisition/continuing balance, area, owner-only player/settings access, independently periodic orb collection and particles are preserved.
- Active Overclock uses2 sub-ticks, Ultimate10 with one-tick delay. After Pulse ends the acquired-player continuation uses one settlement per world tick, matching source. Creative never invents XP.
- The pinned standalone class omitted the extended marker. Forge explicitly implements the eight-slot contract, bag/copier support, full-width properties and a client-only missing-chunk menu view. No server gameplay placeholder is used.
- Fluid endpoints reject stale generations, detached/removed/unloaded owners, wrong thread and stopped servers. Player references are checked and released after disconnect/removal/world/area changes; load/unload discards transient selection.
- Typed copying excludes stored XP and ownership identity. Source-shaped crafting uses the existing preserving serializer. Native tier conversion/rollback now verifies XP and clears detached native XP ownership alongside existing buffers/cards.
- Normal drops retain XP/settings and drop cards separately once. The Eclipse wrench packs the whole machine. Forge FluidUtil preserves stacked buckets/returned containers.
- Pinned models/recipe ingredients/translations, rotating bottle/area renderer, native GUI, centralized layout, actual JEI recipe and exact Jade XP/target packets are wired. Exact XP is shown in the XP-bar tooltip without overlapping area controls.
- The source adjacent-tank cache helper is never called. No automatic export was invented: use pipes or containers.

Intentional conservation fixes: account merged-orb multiplicity; leave overflow in the world; remove only accepted player XP; bound negative/oversized data and arithmetic. Vanilla absolute ServerPlayer XP setters refresh synchronization without synthetic `giveExperiencePoints(0)` events that bonus mods could rewrite into unpaid gain. A real Forge listener regression rewrites gains to100 and verifies exact transactions without invoking gain events.

## Automated behavior

Three new numeric tests and18 actual GameTests cover whole-XP fluid/simulation/invalid inputs/int limits;1/10/all player operations/partial levels/near-full/owner-only settings; merged orbs/remainders/removal; invalidated/revived/replaced/off-thread/unloaded endpoints; menu/packet position/distance/current-BE/range; interleaved two-menu repeated withdrawals/toggles; native XP/card migration/eighth slot/failed replacement rollback; actual preserving recipe/survival placement/late NBT/wrench/normal drops; stacked XP buckets; settings-only copying/bag/eighth-slot persistence; maximum player XP; area-player work and reference release; actual Ultimate ten-level160XP ticker; normal/Ultimate Pulse2/10 then1; and gain-callback isolation for Player/ServerPlayer.

Interleaved menus are server logic tests, **not two independent clients**. All previous assertions remain enabled.

## Graphical client

`scripts/run_experience_client_smoke.sh` reuses existing Java/Gradle/Xorg and opt-in `-PjdteExperienceSmoke`. One actual mapped graphical client/integrated server uses JDT/GuideME/AE2/Curios/JEI/Jade/ModernFix and a marked copied disposable world.

Initial ledger:1234567890 holder XP +400 player XP. Actual mouse deposit/extract transfers6 then47XP at native boundaries. Target37/owner/collection/particles synchronize. Vanilla repeated quick-move installs both legal Range cards in separate one-item slots, then recovers both. All deposit yields1234568290 holder XP/zero player XP. JEI displays the actual preserving recipe; Jade packets contain exact XP/target. A missing-chunk view accepts all ten short properties and44 slots without loading its distant chunk.

A deliberately configured real source pulse and area then let ordinary world ticks balance the actual connected player to37. Reopened menu and player agree with the vanilla XP curve and unchanged1234568290XP combined ledger. This is natural work after deterministic setup, not random-trigger or independent-multiplayer testing. Five screenshots were retained and visually inspected; initial text overlap is repaired.

## Independent production lifecycle

`scripts/run_experience_lifecycle_probe.sh` requires a marked loopback-only disposable Forge installation and exact `experience-lifecycle-world` level. Previous fixture worlds are renamed/preserved. Two actual Java17 JVMs use reobfuscated main/probe with only Forge/JDT/GuideME; AE2/Curios/JEI/Jade are absent.

JVM1 converts a native holder with500000XP/four cards, clearing old XP ownership. Five merged37XP orb units collected by natural ticks add185XP. A real survival FakePlayer with full inventory uses the wrench, creating exactly one package; actual crosschunk placement retains complete NBT. A35XP orb resumes work. Slot7 holds the fifth card, Ultimate Capacity, without generating XP.

The probe removes tickets, waits for actual **ChunkEvent.Unload**, completes pending save and requires a **different** reloaded BE with exact complete NBT; captured old endpoints stay inert. It writes500220XP expected NBT and performs saved shutdown. JVM2 verifies exact XP/owner/area/settings/eight-slot state before work, then collects three11XP units, ending500253XP with slot7 intact. Installed main hash matches final build.

## Historical failures and repairs

Failures are preserved and never counted as passing:

1. Initial compile had four native API mismatches, corrected before first passing regression.
2. Client attempt1 expected one quick-moved Range card; vanilla correctly repeats into two legal one-item slots. Fixture now requires both installed/zero loose, then exactly two returned. No assertion disabled.
3. Production attempts1/2 found `hasChunk=false` while Forge still cached the pending-unload BE. NBT was equal but required fresh identity failed (`sameOwner=true`). Actual unload event/completed save now precede the still-required identity/equality/stale-port assertions.
4. Visual inspection found raw XP text overlapping native area controls; changed to a tooltip and inspected final screenshots.
5. Source review found ungated fallback double-ticks after Pulse ended. Common wrapper now matches source redstone gating; all retained tests plus exact2/10-then1 regression pass.

`core-*`, `container-*`, `ultimate-*`, `transaction-*`, `attempt*`, `native-controls-*`, `unload-event-*` and `first-*` retain historical scope. Current acceptance uses `pulse-regression` and final client/production files.

## Unverified and next subsystem

- No two independent XP clients, genuine disconnect/rejoin mid-balance, cross-dimensional transport or prolonged workload this round.
- Source active-player reference is transient; no Player object is falsely persisted. Interrupted-pulse reacquisition after real reconnect/restart remains unverified.
- Arbitrary third-party XP hooks/entities/data packs are not certified by the targeted gain-event test.
- Prior external JDTE-AE whole-grid, operating-reactor, full fusion and lightning integration boundaries remain unverified.
- Large Potion Canister remains **N**. Read-only source inspection establishes4000mB, four matching potions→1000mB/four bottles, bound handheld/Curios menus, full PotionContents and preserving recipe. Native bows have two canister slots and25/25/50mB per enabled Potion Arrow/Splash/Lingering. Native static setter clamps to1000mB: subclass-only registration would lose contents on first consumption. Full NBT/custom effects/color, consumers, binding and current fuel-only recipe serializer need a separate port/test stage. It is not a passive player-buff item.

## Reproduction

Use existing Java17/Gradle8.6 and configured proxy arguments where required:

```bash
bash gradlew test runGameTestServer build serverSmokeJar compileClientSmokeJava --no-daemon --max-workers=2
bash scripts/run_experience_client_smoke.sh
JDTE_LIFECYCLE_SERVER=/path/to/marked/disposable/forge-server JDTE_SMOKE_PREBUILT=1 bash scripts/run_experience_lifecycle_probe.sh
python scripts/audit_forge_port.py
python scripts/audit_forge_port.py --check
python scripts/validate_docs.py
```

Version/main/release untouched; original energy recovery branch and all backup hashes retained.
