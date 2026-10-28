# Fuel canister and extended generator validation

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Environment: Forge 47.4.10, JDT Forge 1.1.4, Java 17; version stays 0.5.8.

## Fuel core checkpoint (2026-10-28)

[Retained build summary](fuel-core-summary.txt): **138 JUnit / 169 required GameTests**, build and both JAR reobfuscations pass. All previous 131/164 tests and assertions remain enabled. Seven pure numeric tests cover whole-item filling and overflow. Five real Forge tests cover real fuels/rejected inputs, simulated/real fill and remainder ownership, capacity/NBT round-trip, normal/upgraded native generation, full/partial FE buffers, bound menu ownership and one-time remainder return, and the registered upgrade recipe.

The canister follows the source's capacity ×4, burn unit ×10, weighted stored burn multiplier and additional resolved ×10 multiplier. The recipe requires four occupied ingot cells; vanilla consumes one per occupied cell. It preserves source NBT/Forge capabilities and suppresses the source canister crafting remainder to prevent duplicating stored fuel. Filling copies the offered stack before synchronous handler callbacks mutate it. Native solid generation now debits accepted FE only, preserving prepaid fuel while blocked. Invalid/overflowing fuel budgets are rejected before consumption. Generator Upgrade requires both canister fuel units before its ×3 FE batch.

The source has no external item-handler capability on the canister: its menu slot is transient and unused solid fuel returns on close. Do not advertise an invented persistent staging inventory. The shared recipe only registers the implemented fuel pair; Large Pocket Generator and Large Potion Canister remain absent. Both extended generators are independent conversions of the two native JDT generators, not prerequisites of the canister.

**Not yet verified at this checkpoint:** graphical fuel GUI/Curios shortcut, dedicated production JVM recovery for this new item, extended generator registration/behavior and tier conversions. The canister remains U pending those acceptance checks. Previously recorded transmitter/server tests are not reused as proof for these new paths.

## Extended generator core checkpoint

[Build/test summary](extended-core-summary.txt): **138 JUnit / 178 required GameTests**, main/probe reobfuscation and graphical harness compilation pass. `ExtendedGeneratorGameTests` adds nine real Forge tests covering both machines and both native conversion paths, denied conversion rollback, whole-fuel/FE conservation, external validators and rates, actual ten-execution Ultimate ticker/physical capacity, packing/BlockItem placement, preserving crafting and invalidated/revived public capabilities. No simulated test is labeled a real chunk unload; production-JVM and graphical checks remain pending in this section.

The source generators are thin native subclasses with eight upgrade slots. Preserve their non-directional blockstate and native configured fuel/FE/tank ownership. Only JDT `RefinedFuel` is accepted by the liquid machine. Both native constructors create output-only energy storage; wrapper behavior follows the actual owned storage. FE output limits are per face, including native liquid export ×10. The port corrects short-acceptance loss and packed recipe NBT loss. Retained public ports reject detached/unloaded owners. The capability provider now recreates invalidated handles after Forge revival without recreating or copying their underlying inventories.

Source Extended Upgrade has 20 targets; 18 are now wired. Experience Holder and Advanced Bio Extractor are still absent. Other machines' missing Ultimate behaviors and the external JDTE-AE backend are not completed by this work.

## Graphical and conservation follow-up

Actual Forge client run passed; [screenshots, server-checked assertions and summary](graphical/run-summary.txt), [success marker](graphical/success.txt). Handheld and Curios F-event canister GUI fills update real NBT; both generator screens synchronize >16-bit FE, burn and fluid properties. Eight real slots and card install/remove/transfer are verified. Harness does not substitute server menu mutation for player clicks. It uses real `MultiPlayerGameMode` inventory packets and the registered client key event.

[Full regression](conservation-summary.txt): **139 JUnit / 180 required GameTests**, build and both reobfuscations pass. Checked/saturated fuel arithmetic and card removal guards protect stored FE/fluid. Compared with source, unsafe generator capacity-card removal is rejected until buffers fit rather than clipping stored resources.

First production attempt failed; see [failure evidence](production-first-failure.txt). Production acceptance remains pending until a subsequent successful run is recorded below. Do not infer success from graphical or GameTest acceptance.

## Container-fuel and rendering follow-up

[Full regression](container-render-summary.txt): **140 JUnit / 181 required GameTests**, complete build and both reobfuscations pass. Actual Lava Bucket/remainder and Refined Fuel bucket/full versus partial headroom are covered. Source's one-container exception is preserved. Real GUI rerun also passes attempted unsafe capacity-card removal.

Screenshot inspection found native large-FE bar arithmetic overflow despite correct numeric synchronization. Only the two new generator hosts use the corrected wide/clamped bar in the native screen path; graphical verification of this change is pending here.

Production progress: full-inventory packed-drop and exact NBT restore pass after fixture warmup/initial placer correction. Actual unload still times out at FULL/level 33. Failed attempts are retained in `production-*-failure.txt`; do not label production recovery successful before a passing run below.

## Current acceptance (supersedes checkpoint limits above)

**140 JUnit / 183 required GameTests**, complete build and both reobfuscations pass ([summary](final-regression-summary.txt)). Existing 131/164 tests and assertions remain enabled. Added nine pure numeric, five canister and fourteen generator/native-lifecycle tests.

[Final real client evidence](graphical-final/run-summary.txt) includes the actual enlarged FE bar fix and full-FE capacity-card Shift-click rejection; earlier screenshots remain historical. The native screen's int multiplication overflow was confirmed visually and corrected with wide/clamped arithmetic only for these two hosts.

[Two actual production JVM success markers](production-first-pass.txt): full-inventory wrench drop/restore, actual chunk unloading/reloading, inactive stale item/FE/fluid ports, complete saved-state equality, shutdown/restart, exact resumed prepaid solid FE and two-mB liquid batches. Large canister name/speed/fuel, all eight-slot data, >16-bit fluid/burn values and owned FE survive. Runtime has only JDT, GuideME, JDTE and the separate probe mod: AE2, Curios, JEI and Jade are absent. Curios presence/F shortcut is verified separately in the real client.

The failed production attempts led to the actual native global-fluid-drop chunk pinning correction. [Captured stack and cause](production-sixth-failure.txt). `LoadedFluidDropLookup` reads an already loaded chunk without refreshing UNKNOWN tickets; no ordinary world entities are deleted to manufacture acceptance. Regression checks retain real native fluid-drop crafting. Probe-only ticket tracing is not shipped in the main mod.

### Source differences and limits

- Preserve source capacity ×4, unit ×10 and weighted/resolved canister speed (including the source's two-stage multiplier), native generator fuel types, native configured FE and fluid per-face export ×10, eight slots and actual Ultimate ×50/ten executions.
- Fix recipe resource errors: four occupied ingot cells; no returned old fuel canister; packed generator recipes retain machine state and migrate upgrade storage.
- Fix caller stack mutation, full/partial solid-buffer FE-credit loss, short neighbor acceptance loss, stale public ports, attached-capability revival and oversized numeric/rendering overflow.
- Reject insufficient second canister units, overflowing generation budgets and capacity-card changes that would discard FE/fluid. Preserve source's single-container fuel remainder exception.
- Large Pocket Generator/Large Potion Canister remain separate absent items. Extended Upgrade still lacks Experience Holder/Advanced Bio Extractor conversions. Production checks cover target lifecycle; native-to-Extended conversion/protection rollback uses real Forge GameTests, not a production player session.
- **Unverified:** two independent players concurrently using these new menus, long-duration public servers, arbitrary external fuel/recipe packs and every optional mod combination. External JDTE-AE whole-grid, complete reactors and full Draconic fusion remain unverified elsewhere. No release or main merge.

### Reproduce

Use the existing Java 17/Gradle environment. Run `gradlew test build runGameTestServer`; `build` includes separate main/probe reobfuscation. For graphics run `scripts/run_generator_client_smoke.sh`. For production set `JDTE_LIFECYCLE_SERVER` to a marked disposable loopback Forge 47.4.10 installation and run `scripts/run_generator_lifecycle_probe.sh`. The script copies both built/reobfuscated JARs and requires success markers from seed and verify JVMs. `JDTE_SMOKE_PREBUILT=1` uses a previously built pair. Never point the harness at an unmarked user world.
