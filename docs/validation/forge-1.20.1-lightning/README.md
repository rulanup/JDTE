# Charged Lightning Rod / Thunderstorm Upgrade

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Target: Minecraft 1.20.1, Forge 47.4.10, JDT Forge 1.1.4, Java 17/Gradle 8.6. Mod remains 0.5.8.

## Current acceptance

Both items receive scoped **V** for the implemented source machine, upgrade and vanilla/marker contract. **170 JUnit /252 required GameTests** pass with all prior 167/238 retained, no skips or relaxed existing assertions. Full build, both JAR reobfuscations and client/probe compilation pass.

- [Complete Gradle/GameTest log](complete-build-gametest.log.gz), [JUnit totals](junit-summary.json), [original XML](junit-results.zip).
- [Actual client result](client/run-summary.txt), [waiting for weather](client/jdte-lightning-waiting.png), [hidden/silent controls](client/jdte-lightning-hidden.png), [actual JEI](client/jdte-lightning-jei.png), [visible storm bolt](client/jdte-lightning-visible-storm.png), [full client log](client/runtime.log.gz).
- [Two production JVMs](production/run-summary.txt), [seed](production/lightning-seed.log.gz), [restart](production/lightning-verify.log.gz), [expected complete NBT](production/lightning-expected.nbt), [same-JAR hashes](artifact-integrity.json).

## Pinned behavior

| Mode | FE per bolt | Maximum batch | Interval |
| --- | --- | --- | --- |
| Normal | 10000 | 1 | 200 loaded ticks |
| Overclock | 40000 | 5 | 1 tick |
| Ultimate Overclock | 100000 | 50 | 1 tick |

Base FE capacity 10M, increased by source Capacity/Ultimate Capacity rules. Four upgrade slots accept only Capacity, Ultimate Capacity, Overclock, Ultimate Overclock and Thunderstorm; speed cards remain mutually exclusive and Thunderstorm is limited to one copy. No fluid, redstone, creative or arbitrary extra upgrades were added. The source has no configurable cost values or sky-access requirement; these constants are retained.

Both rain and thunder are required unless the card bypasses weather. The card never changes world weather. An existing vanilla rod above the controller is reused without ownership; air is filled with one owned rod. Mining that rod requires manually replacing it. Breaking/transporting the controller removes its owned rod without a drop and relocation restores that ownership credit once; player rods survive controller removal.

Real `LightningBolt` instances run with `visualOnly=false`, null player cause and `ae2lt.natural_weather_lightning=true`. This is the pinned source's complete machine-created lightning/marker contract, not a claim that the random vanilla weather-spawn engine generated the entity. Actual hidden/silent bolts power a vanilla rod and transform a real pig in GameTests. The client-only renderer/sky-flash flag is synchronized; native silent metadata independently suppresses both thunder/impact calls. Neither toggle disables server effects. The settings codec copies only presentation/configuration, not FE/cooldown/ownership.

## Actual verification scope

GameTests cover the real server-weather flags, clear/rain rejection, one-card restriction, exact5/50 batches and resource-short remainders, same-game-time deduplication, Forge canceled/mutating joins, independent/persistent flags, real vanilla rod/pig effects, FE bounds/capacity-card removal, stale ports/menu actions, foreign rod preservation, unloaded targets and real survival wrench/BlockItem relocation. Negative/INT_MAX NBT FE is bounded; a legitimate 15M FE save with a 20M upgraded capacity remains 15M after loading.

The graphical mapped client opens the real menu and verifies 9000001 FE, real inventory packets installing exactly one of two cards, one 10000 FE charge, both toggles, card removal and restored weather gating. A client-only menu view also receives all eight data properties and the same four upgrade slots without a cached target chunk; no target chunk is loaded or ticked by this view. Hidden/silent bolts produce no sky flash or lightning sound event, while re-enabled bolts produce both a visible sky flash and actual client sound events. Audio is observed at the client event layer, not certified by a human listening to an output device. Both recipes are found and displayed by the actual JEI runtime.

Two **separate reobfuscated production JVMs** use only JDT/GuideME/JDTE/probe; AE2/Curios/JEI/Jade/AE2LT are absent. The first runs natural machine ticks in deterministically configured server thunderstorm conditions, checks exact bolt/FE accounting and same-time invocation suppression, then uses an actual survival FakePlayer/full-inventory wrench and actual BlockItem placement across a chunk boundary. It checks owned-rod cleanup, one package, no extra rod item and preserved resources/controls/cooldown; the weather card resumes clear-weather work. Actual chunk unload/reload retains complete machine NBT and makes old FE interfaces inert. A real resumed strike prepares a **nonzero 200-tick cooldown** before shutdown. A destination with an existing player rod never inherits the origin ownership claim; an actual wrench/BlockItem regression verifies that rod survives breaking the moved controller. The second JVM checks exact complete NBT before natural ticks, then waits for a real resumed bolt and reconciles its exact 10000 FE debit. FakePlayer setup here is distinct from the graphical real-player GUI run.

Weather in GameTests and runtime is **set deterministically using the real level/server APIs**. We did not wait for a randomly arriving thunderstorm, use a fake lightning entity, or replace real behavior with a simulated production algorithm.

## Fixes and failure chronology

- Initial source adaptation fixed registration declaration order and old/new Forge signatures. First core suite's stale-menu fixture reinserted an invalidated BE during cleanup; revive it before cleanup. All prior assertions stayed enabled.
- First graphical attempt sent an interaction before the target BE reached the client, exposing a real data-slot mismatch. The menu now has an explicit client data/inventory view for this arrival order; the harness also waits for real physical click targets. A separate missing-chunk regression initially used ClientLevel.hasChunkAt, which does not report actual cache presence; use getChunk(FULL,false) instead.
- First packaged run found the wrench removing the BE before block.onRemove could clean its owned rod. The shared Eclipse pickup helper cleans only a Charged Rod's owned block after snapshot and before BE removal.
- Real Forge BlockItem placement can restore NBT after onLoad. Ownership relocation is therefore reconciled after load as well as onLoad; the updated real placement regression supplies no artificial onLoad call.
- Fixed-startup-tick production samples failed before the forced remote chunk had enough actual block-ticking frames. Failed restart had cooldown 177 after 210 global ticks, proving only 23 actual machine frames. Fixtures now wait for observed first/resumed natural lightning with the same strict one-bolt/exact-FE assertions and bounded timeout. They do not manually tick the machine to obtain a passing production result.
- Forge's raw energy setter accepts negative/over-capacity integers; sanitize incoming NBT against the restored upgrade layout. Regression170/252 and final client/paired server runs pass after this correction.

Failed attempts are retained under `failures/` and the initial/guard build logs alongside the final passing evidence. They are not counted as successes.

## Destination ownership correction

Final review added a guard for moving an owned machine under a pre-existing player rod. Relocation clears old ownership before deciding whether air can be filled; existing destination blocks are never claimed. A real survival placement regression was added without changing prior assertions. Final 170/252, the actual client and both packaged production JVMs pass on the guarded JAR; its installed and built hashes match.

## Optional integration boundary

[Uploaded AE2LT metadata/reader evidence](ae2lt-contract.json) confirms the same marker key in its transformation handler, weather mixin and debug rod. That supplied 2.1.2-beta3 JAR requires NeoForge 21, Minecraft 1.21.1, AE2 19, Thunderbolt 2 and class major 65 (Java 21). It cannot run in this Forge1.20.1/Java 17 target. **Actual AE2LT transformation integration remains unverified**; the real vanilla effects and exact tag contract are separately verified. No compatible Forge AE2LT dependency was fabricated or substituted.

Cross-dimensional controller transport/operation, independent multiplayer interaction with this new machine, prolonged uptime, arbitrary third-party lightning hooks and random weather-arrival timing remain unverified. Existing external JDTE-AE whole-grid/reactor limitations remain unchanged.

## Reproduction

Use the existing configured Java 17/Gradle 8.6 environment. Run `test build runGameTestServer serverSmokeJar compileClientSmokeJava`. `scripts/run_lightning_client_smoke.sh` reuses Xorg/software GL, captures a configured launch and selects the small real-mod client profile. It replaces only a marked disposable copied client world.

`JDTE_LIFECYCLE_SERVER=<marked loopback fixture> JDTE_SMOKE_PREBUILT=1 bash scripts/run_lightning_lifecycle_probe.sh` runs seed/verify production JVMs with the separate probe JAR. The fixture requires `level-name=lightning-lifecycle-world` and preserves earlier worlds by renaming them before each isolated seed. No test/probe classes ship in the main JAR. Run both audit generation and `--check` plus documentation validation before saving the checkpoint.
