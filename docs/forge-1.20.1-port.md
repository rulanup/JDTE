# Forge 1.20.1 port — work in progress

Source: JDTE 0.6.5.2 (`ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`).
Target baseline: `ac1d813`, Forge 47.4.10, Just Dire Things Forge 1.1.4, Java 17, Gradle 8.6.

This is an ongoing port, not a completed 0.6.5.2 Forge release. The existing version remains 0.5.8 until feature migration and validation finish.

## Current Status

<!-- current-status:start -->
- Latest durable recovery checkpoint: **`a115219`** (WIP, pushed to `codex/recovery-energy-transmitters-20261028`), based on **`fb64caa`**. Both interrupted transmitter implementations survived. See [recovery evidence](validation/forge-1.20.1-energy-transmitters/README.md).
- Environment/version: Forge 47.4.10, JDT Forge 1.1.4, Java 17, Gradle 8.6; **0.5.8-forge-1.20.1**.
- Latest completed recovery regression: **131 JUnit / 154 required GameTests**, full build and production/probe reobfuscation passed. Real Applied Flux same-grid extraction and failed-refund/NBT/node recovery pass; production lifecycle validation is still running. Failed attempts remain recorded below.
- 120-item status: V **33**, U **21**, P **48**, N **18**; **102** source items registered in Forge. The [Feature Migration Matrix](forge-1.20.1-feature-matrix.md) is the authority for functional acceptance and scoped evidence.
- Priorities: finish transmitter chunk/wrench/restart and Applied Flux reserve-conservation checks, then review player/Curios, Draconic and graphical acceptance. Both newly registered transmitters remain U; no recovery-only V promotion. Four other Extended Upgrade targets and Large Fuel Canister remain absent.
- Still unverified: installed JDTE-AE whole-grid backend, prolonged multiplayer uptime and complete operating reactors. New graphical energy-card checks were not run.
<!-- current-status:end -->

This file is a chronological development log. Sections below preserve the implementation, limitations, failures and test results **at their recorded checkpoint**; old limitations are not current acceptance decisions. Consult Current Status and the matrix for the latest state. Earlier evidence retains its original scope and counts.

## Progress

- Selected the existing `forge-1.20.1` branch.
- Installed Java 17 and Gradle 8.6 and verified official SHA-256 checksums.
- Imported the environment proxy CA into the task JDK trust store, preserving TLS verification.
- Forge dependency resolution and compilation passed under Java 17.
- The two migrated mineral core test classes passed all 24 tests.
- Repair Talisman item, tick handler, energy capabilities, recipe, model, texture, config and translations compile.
- Dedicated-server validation found missing server-side Glodium and an unnecessary Mixin bootstrap JAR. Glodium is now available on both sides, and the standard ForgeGradle/Mixin launch path boots to `Done`.
- The server completed startup with the existing branch content and Repair Talisman; this is not validation of the still-unported machines.
- Integrated the mineral production engine, output planner and survey codecs into registered Forge machines, menus, capabilities and server datapack lifecycle hooks. See the verified mineral checkpoint below.

## Original registry-delta checklist

This historical checklist records the initial registration delta, not complete 0.6.5.2 behavior parity. The [full 120-item feature matrix](forge-1.20.1-feature-matrix.md) is authoritative for verified, unverified, partial and absent implementations.

- [x] `jdte:advanced_ae_output_upgrade` (implemented; see the latest validation checkpoint)
- [ ] `jdte:advanced_bio_extractor`
- [ ] `jdte:advanced_energy_transmitter`
- [ ] `jdte:advanced_entity_accelerator`
- [x] `jdte:advanced_essence_conversion_upgrade` (implemented; see the latest validation checkpoint)
- [x] `jdte:advanced_fluid_mixer` (implemented; see production-chain runtime evidence)
- [x] `jdte:advanced_machine_settings_copier` (typed Forge presets, upgrade exchange and storage sources implemented)
- [x] `jdte:advanced_upgrade_storage` (36 slots, machine actions, Curios lookup and Forge persistence implemented)
- [x] `jdte:ae_acceleration_upgrade` (three Time Accelerator tiers, per-device AE service and source external-backend contract implemented)
- [x] `jdte:ae_crafting_read_upgrade` (implemented; see the latest validation checkpoint)
- [x] `jdte:ae_extraction_upgrade` (implemented; see the latest validation checkpoint)
- [x] `jdte:ae_output_upgrade` (implemented; see the latest validation checkpoint)
- [ ] `jdte:cainiaoyo_fumo`
- [ ] `jdte:charged_lightning_rod`
- [x] `jdte:crafting_pattern` (implemented; see the latest validation checkpoint)
- [x] `jdte:energy_brewing_upgrade` (source fuel behavior and Forge integration tested; see energy evidence)
- [x] `jdte:energy_overload_upgrade` (native JDT transmitter tested; JDTE advanced/extended transmitters remain absent)
- [ ] `jdte:entity_acceleration_upgrade`
- [x] `jdte:essence_conversion_upgrade` (implemented; see the latest validation checkpoint)
- [ ] `jdte:extended_bio_extractor`
- [ ] `jdte:extended_energy_transmitter`
- [ ] `jdte:extended_entity_accelerator`
- [ ] `jdte:extended_experience_holder`
- [ ] `jdte:extended_fluid_generator`
- [x] `jdte:extended_fluid_mixer` (implemented; see production-chain runtime evidence)
- [ ] `jdte:extended_generator`
- [ ] `jdte:large_fuel_canister`
- [x] `jdte:large_mineral_extractor` (implemented; server integration tests passed; graphical integrated-client and dedicated-server checks passed; independent remote clients passed; see the latest checkpoint)
- [ ] `jdte:large_pocket_generator`
- [ ] `jdte:large_potion_canister`
- [ ] `jdte:maiqilin_fumo`
- [x] `jdte:mineral_extractor` (implemented; server integration tests passed; graphical integrated-client and dedicated-server checks passed; independent remote clients passed; see the latest checkpoint)
- [x] `jdte:mineral_survey` (implemented; server integration tests passed; graphical integrated-client and dedicated-server checks passed; independent remote clients passed; see the latest checkpoint)
- [x] `jdte:mixing_upgrade` (implemented; see production-chain runtime evidence)
- [x] `jdte:repair_talisman` (ported; runtime validation pending)
- [x] `jdte:seed_conversion_upgrade` (normal/large harvest paths, source recipe and NBT verified)
- [ ] `jdte:thunderstorm_upgrade`
- [ ] `jdte:time_multitool`
- [x] `jdte:ultimate_capacity_upgrade` (mineral machines and fluid mixers; other machine families remain pending)
- [x] `jdte:ultimate_mixing_parallel_upgrade` (implemented; see production-chain runtime evidence)
- [x] `jdte:ultimate_overclock_upgrade` (mineral machines and fluid mixers; other machine families remain pending)
- [ ] `jdte:ultimate_time_wand`
- [x] `jdte:upgrade_storage` (basic storage implemented; client and dedicated-server tests passed)
- [ ] `jdte:xingye_fumo`

## Validation required

- [x] Compile the Forge branch and migrated mineral core.
- [ ] Compile and test every remaining migrated subsystem (mineral core: 24 passed).
- [x] Clean build and production JAR reobfuscation completed.
- [x] Dedicated server booted to `Done` after a clean build.
- [ ] Exercise remaining new machine/upgrade registration and saved data after their migration.
- Validate optional dependencies and client-only screens separately.

## Historical validation summary (energy-upgrade checkpoint)

This retained summary describes that checkpoint. Current Status above contains the latest suite and acceptance counts.

- Latest Java 17 build: passed, including production/probe reobfuscation; 100 JUnit and 123 required GameTests passed. All earlier tests are retained.
- MineralProductionEngineTest: 16 passed.
- MineralOutputPlannerTest: 8 passed.
- Clean dedicated Forge server: reached Done and shut down with saved dimensions.
- Full 0.6.5.2 feature parity: not complete; unchecked registrations above are not implemented yet.

- The inherited cat-gift and chicken Bio Factory recipes used 1.21-style ingredient item lists. Both now use Forge 1.20.1 ingredient arrays; a subsequent dedicated-server restart reported no JDTE recipe parsing errors and reached Done.
- The environment start instructions were saved as an onboarding draft. Publishing that environment draft is separate from publishing a mod release.

## Mineral machines checkpoint (2026-10-26)

### Implemented

- Mineral Extractor: registered block/item, block entity, menu, screen and animated ore renderer. Runs the original batched weighted production and output-fitting algorithms, rather than placeholder processing.
- Large Mineral Extractor: original 3x3x2 structure, four reusable survey slots, 4x base work, 17 parts, placement checks, rollback and whole-structure dismantling. Forge requires a small block entity at each part for capability forwarding; these parts own no inventory, energy, fluids or production queue.
- Mineral Survey: right-click recording of the actual biome/dimension/index version, reusable machine input, NBT codec, recorded-survey inspection screen, tooltips and server-authoritative opening packets.
- Server mineral index: vanilla/modded world-generation feature analysis, bounded holder/codec traversal from the source #22 fix, deterministic profile fingerprints, biome/tag datapack overrides, rebuild on reload and cleanup on server stop. Forge's already-applied biome modifiers are read without applying them a second time.
- Original mineral behavior: 64 base work per regular tick, 20 work per cycle, 5,000 FE per completed cycle; optional XP Fluid consumes 25 mB per fortune cycle for a 100% yield bonus, and optional Time Fluid consumes 5 mB per additional cycle. Empty fluid tanks still permit base production. Settings remain configurable.
- Redstone, ore filters, JDT Smelter recipes and double smelting FE cost, Creative behavior, standard Capacity/Fluid/Filter/Overclock upgrades, transient accelerator coalescing, persistent pending-work backpressure and output-capacity planning are connected.
- Output supports 16-64 slots and the source limits of 64 / 2,048 / 4,096 / 8,192 items with ordinary Capacity cards, or 6,400,000 with Ultimate Capacity. Removing capacity retains occupied output slots. Oversized counts use the existing extended NBT serializer and a new count-safe menu snapshot packet; cursor pickup stays at vanilla stack size.
- The normal machine exposes restricted item automation, FE and two independently routed fluid inputs. Large-machine outer parts forward to the controller, without forcing unloaded chunks or retaining a detached controller. The large controller also exposes the same restricted storage capabilities, matching the source `MachineCapabilities` registration table. Absolute-direction automatic input/output and event-coalesced output are connected, including large-machine boundary neighbors.
- Ultimate Capacity and Ultimate Overclock are registered with recipes/assets and real mineral-machine behavior: 50x FE/fluid capacity and a default 640x selectable speed limit respectively. Speed cards conflict. Other machine families deliberately reject these cards until their respective behavior is ported; tooltips state this current scope.
- JEI category, mineral probabilities, paged biome recipes, non-consumed survey catalysts, optional-fluid explanations and a bounded chunked server catalog sync are implemented. Recipe reload and disconnect refresh/reset the client cache. Jade displays mineral status/source/count/speed and controller upgrades through both controller and part blocks.
- Source models, blockstates, textures, crafting recipes, block loot tables, pickaxe tag, English/Chinese translations and GuideME/Patchouli entries are installed. Newly added Markdown pages are English.

### Forge-specific corrections

- JDT Forge 1.1.4 registers source fluids as `justdirethings:xp_fluid` and `justdirethings:time_fluid`, rather than the 1.21 `_source` IDs. Recipe validation and all defaults use the real Forge IDs.
- Custom recipe serializers use 1.20.1 JSON/FriendlyByteBuf APIs; crafting results use `item`, with plural `recipes`, `loot_tables` and `tags/blocks` directories.
- Mineral NBT and packet codecs bound decoded list sizes and preserve long weights/versions. Invalid confidence strings produce codec errors; the optional field retains DFU's declared schema-default behavior.
- Fluid amounts/capacities are split across signed-short-safe container data fields. Fluid rendering uses long arithmetic and clamps the height to the GUI; empty tanks show the configured fluid as a ghost.
- Mineral energy, tank, inventory and pending-work mutations mark the controller dirty for saving. Filter/output page requests are clamped, and the output-page packet validates the current menu ID and player access.
- Optional metadata reflection catches expected reflection/runtime failures and logs diagnostics; it does not swallow `Throwable` or VM errors. The source's unsupported metadata fallbacks remain available.

### Validation performed

| Check | Result |
|---|---|
| Java 17 `test build runGameTestServer` | Passed, including production `reobfJar` |
| Existing mineral engine tests | 16 passed, preserved |
| Existing mineral output planner tests | 8 passed, preserved |
| New survey/container-data codec tests | 6 passed |
| Existing factory transform / life synthesis recipe tests | 10 + 7 passed |
| Real Forge mineral GameTests | All 18 required tests passed |
| Normal dedicated server | Reached `Done`; no JDTE recipe parsing errors; dimensions saved on SIGINT |
| Mineral model texture references | Six unique non-vanilla textures checked against project resources/JDT Forge JAR; none missing |
| Interactive client, GUI rendering, JEI/Jade and remote-player networking | Not run; this environment has no graphical display/Xvfb |

The 18 GameTests cover base FE-only production, paid optional fluid bonuses, full-output backpressure, stale surveys, real smelting recipes, 6.4-million-item inventory saving, large structure/relay teardown, oversized packet counts, vanilla and Mekanism mineral index entries, actual menu paging/Shift-click/cursor behavior, all six automatic output directions, pending-work reload, registered resource recipes/invalid flowing-fluid rejection, survey NBT/network round trips, blank-survey right-click recording, redstone/filter gating, bucket interaction through a large part and ultimate upgrade effects/conflicts.

Validation initially exposed the wrong JDT fluid IDs; those were corrected and the tests rerun successfully. Test harness setup also needed Forge's unqualified structure-template name and a mock player without a network connection for local menu operations. A codec assertion was corrected to distinguish a direct invalid confidence value from DFU's optional-field default. The final suite has no failing tests. The normal server's deliberate SIGINT shutdown yields Gradle exit 130 after successful world saves; this is separate from the successful build and GameTest tasks. Optional development mods still log unrelated missing integration recipes and outbound version/authentication connection failures.

### Unverified / still pending

- A real client must verify both machine screens, rotated models, large renderer culling, filter controls, tooltips, JEI refresh/bookmarks, Jade through parts and two-player page/snapshot synchronization. Packet round trips and dedicated-server isolation are covered; that is not an interactive client test.
- Full chunk unload/reload across a large-machine boundary and Factory Packer/wrench relocation need gameplay checks. NBT load/save and retained capability invalidation are tested, but large-storage destruction and long-running stress tests are not.
- AE Crafting Read/Output/Extraction, essence/seed conversion and other unchecked upgrades are not ported. The mineral machine runs with redstone gating and standard output; its missing AE/conversion extensions are not represented by fake always-successful adapters.
- Inventory Profiles Next annotations are not present in this Forge dependency set. JEI/Jade code compiles, but an optional-mod matrix without those clients was not launched.
- Ultimate card behavior outside mineral machines remains pending. The overall source-feature port is incomplete; the version is still `0.5.8-forge-1.20.1` and no Mod release was made.

### Saved work and next starting point

Development checkpoint branch: `codex/forge-1.20.1-mineral-machines`, based on the existing Forge work including `c54150d`, not a reset to `ac1d813`. Use this branch on `origin` as the continuation point; it preserves both the earlier foundation commit and the mineral implementation. No merge into `main` or Mod release is part of this task.

Next session:

1. Fetch and check out this checkpoint branch, then read this document and its Git status. Reuse the configured Java 17/Gradle 8.6 environment.
2. Run `./gradlew test build runGameTestServer` under Java 17. This cloud requires the existing proxy settings for Java dependency resolution.
3. Complete the real-client checks above before marking the mineral subsystem fully validated.
4. Continue with Upgrade Storage and the settings copier, then crafting patterns/essence conversion and AE output/read upgrades in dependency order. Extend Ultimate upgrades to each machine only together with its actual behavior/tests.
5. Keep version 0.5.8 until the full port and validation finish. The unchecked registration inventory remains the source of truth for the remaining item families.

## Mineral follow-up and Upgrade Storage checkpoint (2026-10-27)

### Actual code changes

- Corrected the mineral menus' inherited `MACHINE_SLOTS`: the normal menu has 17 visible machine slots and the large menu has 20, while their block entities still own all output pages. JDT 1.20.1 uses this field to route ghost-filter clicks. The old total-inventory value incorrectly blocked late hotbar slots and missed the real filter boundary.
- Connected Eclipse Alloy Wrench interactions through large-mineral parts to the controller, including static bulk-pickup/rotation entry points. Parts return `PASS` for wrench/settings-copier item interactions. A populated large machine can now be picked up through a part and placed again with its four survey slots, 6.4-million-item output, FE, upgrades and multiplier intact.
- Added the large mineral block entity's full 3x3x2 render bounds. All four orientations are covered by a server test; the actual client also renders the structure.
- Corrected the mineral screen's vertical origin for Forge JDT's title placement, including the existing window/GUI-scale refresh logic. Added compact oversized output labels and an exact-count tooltip. The private vanilla `renderSlot` method requires a client mixin on 1.20.1.
- Extended progress/process-length synchronization to signed-short-safe low/high halves, covering the configurable 72,000-tick process length. Mineral channel protocol is now `2`, so incompatible earlier WIP clients cannot exchange a different container-data layout under protocol `1`. Mod version remains unchanged.
- Added basic `jdte:upgrade_storage`: the source 4x5 GUI, 20 slots at up to 64 upgrades each, upgrade-only validation, native JDT unstackable-upgrade compression, Forge item-handler capability, persistent NBT, both-hand opening, Shift transfers, tooltip summaries and expanded contents, item model/texture, recipe, creative tab and English/Chinese translations.
- The storage handler replaces the source's component inventory with a live item-tag-backed Forge handler. Independently created wrappers observe the same saved state; simulated operations do not mutate NBT. Saved `Size` cannot resize the fixed inventory. Normal player inventory limits remain in force when extracting unstackable upgrades.
- The storage menu protects its held item against pickup, Shift movement, number-key swaps and offhand swaps. Server validity uses the bound stack identity; client validity tolerates the normal replacement of its inventory stack by server NBT packets. Dedicated-server tooltip execution never initializes client key classes.
- Corrected the mineral Patchouli addon category to `justdirethings:machines`, which actually exists in JDT Forge 1.1.4. Added GuideME and Patchouli Upgrade Storage entries. Updated the documentation validator's large-mineral alias/internal-part rules, and filled the inherited missing Time Freezer documentation from its actual Forge behavior; no new Time Freezer gameplay validation is claimed.

### Verification and reproducible commands

| Check | Result |
|---|---|
| Java 17 `test build runGameTestServer` | Passed, including production reobfuscation |
| JUnit tests | 51 passed, including all original 24 mineral core tests |
| Mineral server GameTests | 24 passed: original 18 plus hotbar/filter boundaries, independent viewers, populated wrench pickup/replacement, all-facing render bounds, recreated-controller capability routing and long process synchronization |
| Upgrade Storage server GameTests | 11 passed: capability/NBT round trip, whitelist/nesting, simulation, live wrappers, distinct NBT, bound item locks, offhand swap, Shift persistence/conservation, server-safe tooltip, legal unstackable extraction and bounded NBT size |
| Dedicated server | Reached `Done`, no JDTE recipe parsing errors, all dimensions saved on deliberate SIGINT; the launcher returns 130 for that manual stop |
| Graphical client | Passed with an actual Forge client and integrated server on existing Xorg dummy/llvmpipe; no JDK or Gradle reinstallation |
| Client network/UI checks | Normal/large menus, visible title, GUI-scale change, 80,000 mB fluid synchronization, server-acknowledged output page, 6.4M count snapshots, legal cursor pickup, recorded survey screen, synced biome catalog, actual JEI mineral category, Jade mineral server data through controllers and large-machine parts, and live Upgrade Storage item/menu/NBT updates |
| Documentation validator | Passed: English/Chinese key parity, registered block names and guide coverage |
| Production JAR isolation | The graphical harness is in `src/clientSmoke`, outside production main sources |

The final server suites have no failures. Initial smoke fixtures needed to wait for login/ground/chunk updates and keep both machines within player interaction reach. A removed-but-still-present entity block recreates its block entity on access; the capability test now checks that transfers reach that replacement rather than the detached instance. Fresh-world Jade checks use actual visible collision shapes when aiming at the machine. These fixture corrections are recorded separately from the menu/wrench/rendering fixes above.

The client harness is opt-in and never activates in normal gameplay or ships in the mod JAR. It creates an integrated-server test scene, sends real Forge/vanilla packets, checks server/client outcomes, saves screenshots and stops the client only after success. `scripts/run_mineral_client_smoke.sh` uses an existing display or starts an existing Xorg dummy driver. It copies a development world into an explicitly marked disposable test fixture, sets lightweight client options and refuses to replace an unmarked world. It does not install tools. Screenshots and the success marker are written under `runs/client`.

```bash
# Use the existing Java 17 installation and Gradle user home.
export JAVA_HOME=/workspace/.jdks/jdk17
export GRADLE_USER_HOME=/workspace/.gradle
bash gradlew -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 \
  -Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080 test build runGameTestServer --no-daemon --max-workers=2

# A development server test world must exist first. Override JDTE_SMOKE_SOURCE_WORLD if needed.
bash scripts/run_mineral_client_smoke.sh \
  -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 -Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080
python scripts/validate_docs.py
```

The older "no graphical display" entry describes the previous checkpoint. This session found the already-installed Xorg dummy driver and obtained the graphical coverage above. Client checks use one integrated client, not two independent remote clients. There is no full optional-mod absence matrix, long-running throughput benchmark, actual cross-chunk unload/reload test or Factory Packer relocation/rotation test. Ultimate-capacity inventory destruction also still needs a bounded-drop/load investigation before a production release; wrench pickup is the verified preservation path.

### Continuation

Keep using `codex/forge-1.20.1-mineral-machines` on `origin`; this session adds commits on top of `bf85f53` without resetting or merging the Forge baseline. Keep Mod version `0.5.8-forge-1.20.1` until the overall port is finished. No release or merge to `main` is authorized here.

Next: validate independent-client synchronization and the remaining large-machine lifecycle cases, then port Advanced Upgrade Storage (modes/actions/Curios) and the Advanced Machine Settings Copier against the now-working basic storage handler. Continue crafting patterns/essence conversion and AE read/output upgrades after those inventory dependencies. Unchecked registrations above remain unported; a working basic bag does not mark the advanced bag or copier complete.

## Lifecycle, advanced storage and settings copier checkpoint (2026-10-27)

This checkpoint continues `2bdf14e` on `codex/forge-1.20.1-mineral-machines`. The pinned reference remains `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`; the Mod version remains `0.5.8-forge-1.20.1`. Earlier limitations above describe earlier checkpoints.

### Lifecycle fixes and actual behavior

- Added `BulkItemEntity` for mineral output stacks larger than the item's normal stack limit. One full normal/large machine has 64 output entities rather than hundreds of thousands. Real counts use an integer entity-data field and integer NBT; the vanilla tracked item and saved `Item` node use a legal count. Vanilla/Forge pickup rules still govern inventory insertion, ownership, events and remainder handling. Full/partially full player inventories and entity NBT are covered by GameTests.
- Replaced broad removal of `JustDiresBEData` from ordinary machine loot with `MachineDropDataHelper`. The helper removes separately dropped machine inventories and upgrade handlers while retaining FE, fluids, filters, configuration and pending work. Mineral machines explicitly save non-default machine state when generating loot. Wrench pickup remains a whole-machine preservation path.
- Factory Packer now validates that every coordinate of a selected Large Mineral Extractor is inside the capture bounds. An orphan part or partial selection reports error 21 before world removal. Removal uses the moving-block flag for this structure, preventing part/controller teardown from destroying records the packer is still moving.
- Part capability relays now check their own loaded chunk and current block-entity identity, in addition to the controller. Even a detached object whose removed flag is cleared cannot write through its replacement. The probe distinguishes live chunk-object reuse from detached endpoints.
- Oversized inventory loading bounds `Size` to the actual fixed handler size before allocation and rejects non-positive integer count overrides. This shared loader also protects the already ported greenhouse inventories.
- Fixed a production-only startup failure: `mixins.jdte.json` now declares its generated refmap. JDT overrides of Minecraft methods use both development and SRG selectors where their JDT-target mixin deliberately disables automatic remapping. This was verified with a real reobfuscated JAR in a standalone production Forge server, not just `runServer`.

### Advanced Upgrade Storage

- Ported the pinned source's actual 6x6/36-slot advanced case, with up to 64 upgrades per slot, including compressed unstackable JDT upgrades. The live item-backed Forge handler, capability, fixed NBT size, both-hand menu opening, safe Shift transfers and bound-item swap protections are shared with the basic case.
- Ported the source machine side panel, case switching, collapse button, compatibility indicators, tooltips and all six actions: insert one/max, take one onto the cursor, withdraw a legal inventory stack, deposit all/one from the cursor. Bio Crusher Looting/Sharpness handlers are supported alongside standard handlers. The Bio Factory indicator uses the already ported Forge handler's actual limits.
- Server messages validate the active machine menu, its window ID, interaction validity, case index and slot before mutation. JEI excludes the panel area. The main channel protocol is now `2`; the mineral channel remains `2`.
- Case discovery checks main hand, offhand, inventory and optional Curios inventories, deduplicating the same stack by identity. No new Curios slot is created. Worn-case discovery and copier withdrawals are exercised against the real Curios capability in the server suite.
- Added the original item/GUI textures and model, adapted shaped recipe, creative tab, translations and guide content. The pinned source does **not** implement the “collection modes” described in an earlier continuation note; no collection mode is claimed here.

### Machine Settings Copiers

- The basic copier is the existing `justdirethings:machine_settings_copier` in JDT Forge 1.1.4. Its filter/redstone behavior is integration-tested against mineral machines; it is not duplicated under a second registry ID.
- Ported `AdvancedMachineSettingsCopierItem`, its typed/versioned snapshot data, machine codec registry, native/JDTE machine codecs, named upgrade handlers and planned upgrade exchange. Only machine families actually available in this Forge port/JDT 1.1.4 are registered; absent source families are still unported.
- Presets retain common tick/direction settings, supported custom options, auto-I/O masks and dedicated upgrade handlers. Native area/offset/filter/redstone option switches remain available. Advanced Potion Brewer recipe-lock templates apply as configuration without resetting an active brew or producing ingredients.
- Pasting reuses installed matching cards, consumes missing cards from the player/basic case/advanced case/Curios case, and refunds surplus cards to inventory or as player drops. Empty presets remove and refund old cards. Missing costs or invalid/type-mismatched/schema-mismatched presets reject the paste before mutation. Planned withdrawals support compressed unstackable upgrades and rollback.
- Both copier tiers resolve Large Mineral Extractor parts to their controller. Real machine inventories, FE, stored fluids, surveys and work queues are excluded from the configuration codecs. Removing a capacity card still applies the machine's ordinary capacity rules.
- Added source item resources/recipe and guides. The native Forge copier ingredient ID differs from the newer source and is adapted to `justdirethings:machine_settings_copier`.

### Verification and limits

- Java 17/Gradle 8.6 `test build serverSmokeJar runGameTestServer`: **58 JUnit tests passed**, including the original 24 mineral core tests; **57 required GameTests passed**. The server suite comprises 25 mineral interaction/production tests, 7 lifecycle/packer tests, 11 basic-case tests, 6 advanced-case tests and 8 copier tests.
- New lifecycle coverage includes all 409.6 million output items on destruction, bounded drops, entity save/partial pickup, malformed inventory size/count, full-player-inventory wrench pickup with both fluids/work queues, actual complete Factory Packer capture/place, horizontal rotation, and rejection of partial structure capture. Packer tests use a larger isolated template and a registered fake-owner lookup entry for permission checks, not a real multiplayer connection.
- `src/serverSmoke` is a separate test mod outside the production JAR. `serverSmokeJar` is separately reobfuscated with the main classes on its mapping classpath. `scripts/run_mineral_lifecycle_probe.sh` requires an explicitly marked, loopback-only disposable production Forge 47.4.10 server. It runs **two different JVMs**, checks success markers/exit codes, and retains per-phase logs.
- The production probe seeds normal and large machines with 64 full 6.4-million-item output slots, all survey slots, 50 million FE, both fluids, an Ultimate Capacity card, multiplier and both pending work queues. It releases all nearby test chunk tickets, observes an actual controller chunk unload, checks stale endpoint rejection, reloads the chunks and compares persisted fields. It then saves/shuts down normally. A fresh server process restores those fields from disk and resumes exactly seven fortune cycles into the final available output space, charging exactly 35,000 FE and 175 mB XP Fluid without overflowing the slot.
- Production runtime contains the configured optional-mod set. Some third-party missing optional recipe/tag references and update/authentication network failures occur in these cloud logs; they are not JDTE parsing failures. There is no full optional-mod absence matrix or claim that unrelated optional integrations have been validated.
- Graphical checks and exact retained evidence are listed in `docs/validation/forge-1.20.1-lifecycle/README.md`. The opt-in client harness remains outside the production JAR. These checks use a real client with an integrated server. **Two independent remote clients connected to a dedicated server remain unverified.**
- Earlier test attempts exposed fixture issues: neighbor forced tickets prevented unloading; a genuinely detached endpoint must stay invalid while a reused live chunk object may legitimately reconnect; repeated seeds needed fixture cleanup; fake networkless login could not run Forge login integrations; running capture fixtures needed explicit redstone pause; mock-player drops were located at the player's position. Each final assertion checks the intended behavior, and the final suites pass. One concurrent development compile invalidated a running test classpath; final validation uses completed compilation before starting the runtime checks.

### Next continuation

1. Continue from this branch's pushed checkpoint; retain all subsequent work rather than resetting to `ac1d813` or `2bdf14e`.
2. Complete two independent remote-client synchronization checks and longer-running production/automation tests before a release. Extend the packaged client checks if additional production-only mapping issues appear.
3. Continue crafting patterns and essence conversion (including Advanced Essence Conversion), then AE read/output upgrades against the now available case/copier inventory infrastructure. Add actual machine-specific behavior/tests before widening Ultimate upgrade compatibility.
4. The unchecked registration inventory at the start of this document still identifies unported machines, items and upgrade families. This checkpoint does not claim full 0.6.5.2 parity. Keep Mod version 0.5.8 until the overall port and validation finish. No release or merge into `main` was performed.


## Multiplayer, conversion and AE upgrades checkpoint (2026-10-27)

This checkpoint continues `8563466` on `codex/forge-1.20.1-mineral-machines`. The source is still pinned to `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`; version remains `0.5.8-forge-1.20.1`. Earlier limitations are historical. No release or merge is performed.

### Implemented and fixed

- Added opt-in remote-client and optional-client harnesses outside the production JAR. Two independent graphical clients connected to a real production dedicated server simultaneously, exercised shared output and independent pages, concurrent upgrade-panel and copier operations, both bag menus, logout with a carried stack, natural pickup after reconnect, observed chunk unload, saved shutdown and reconnection to a fresh server JVM. The first-phase evidence identifies its exact JAR separately from the later upgrade JAR.
- Fixed missing-Curios startup: `BigFluidTankItem` no longer directly implements an optional Curios interface. Curios-present behavior uses guarded item registration; the Forge fluid capability remains intact. Missing client block-entity upgrade queries now return no upgrades safely while a chunk arrives.
- Ported Crafting Pattern's held-item menu and screen, real 3x3 recipe validation, ghost ingredients, non-extractable result, save/clear buttons, both-hand bound-item protection, inventory synchronization, fixed-size version-independent Forge NBT, model, recipe and translations. Saved patterns revalidate recipe identity, input matching and actual output against server recipes.
- Ported basic/advanced Essence Conversion, including anvil binding of one/three distinct patterns, one-level/one-pattern costs, rename handling, native smithing NBT preservation, tooltips and Jade server-side validity status. Advanced patterns sharing an ingredient rotate fairly; different input groups craft independently. Machine NBT retains the rotation. Both greenhouses convert batch production and stored output, preserve crafting remainders, reserve incomplete inputs and reject conversions that cannot fit. Conversion runs before adjacent output and AE output. Unconfigured cards retain the source's unambiguous Mystical Agriculture essence-only conversion.
- Ported AE Crafting Read's Wireless Access Point binding and five-tick server/grid-scoped cache. A busy CPU must also have an active job. Unbound, unavailable, inactive, booting or unloaded targets fail closed. Normal and accelerated work boundaries, event-driven collectors/suppressors, freezers and factory operations use the gate; factory rollback remains possible. Mineral pending work pauses instead of being discarded. Only machine families already available in this Forge port are integrated.
- Ported AE Extraction's standalone linked template, dynamic two-input smithing recipe, equipment NBT/capability preservation, carried/Curios item discovery and capability identity deduplication. Matching existing fluids refill from ME; an empty tank requires a unique accepted fluid type. Applied Flux FE transfer is guarded by its own optional bridge. Extract/insert simulation and refund logic preserve resources on commit shortfalls.
- Ported basic/advanced AE Output with real output-only machine routes, conversion-before-export, oversized grouped long-count item transfers and rollback. Basic transfer defaults to five ticks (configurable 1–1200) and 64,000 mB; advanced output allows ten passes per tick, up to integer-max fluid per pass and a flush after actual machine ticker executions. Output tiers are mutually exclusive. Binding is persistent dimension/position NBT; it never forces the target chunk to load. Models, textures, recipes, creative entries, language keys, bag tag and English guide pages are included. The pinned source contains no survival recipe for Crafting Read; no replacement recipe is invented.
- Real concurrency testing exposed a preexisting Factory Packer race: asynchronous operations called Minecraft's mutable world-path cache. Packer write/read/claim/release/delete and package preview now capture the world root on the server thread and pass an immutable `Path` to asynchronous storage. Eight parallel complete package I/O operations are regression-tested.
- Optional integration GameTests register only when their own APIs are present, using separate AE2/Applied Flux/Curios classes. This fixes a development-client discovery crash without dropping tests from the full configured suite.
- Added current-world-object checks to queued adjacent output and AE output. A detached controller with its removed flag cleared cannot export a second copy or access invalidated capabilities after replacement. This fixes a server crash exposed by the new replacement test as well as preventing duplicate output.

### Verified in the final development suite

- Java 17 / Gradle 8.6 `test build runGameTestServer compileClientSmokeJava`: **77 JUnit tests passed**, preserving all previous 58 and the original 24 mineral core tests; **85 required Forge GameTests passed**, preserving all previous 57. Production and separate probe JAR reobfuscation passed.
- New JUnit coverage exercises grouped oversized/long-max item transfer, simulated acceptance, commit-shortfall refunds, resource transfer bounds and machine safety decisions. Tests requiring Minecraft registries run as GameTests rather than pretending a plain JUnit VM has bootstrapped Forge.
- New GameTests cover optional Curios registration, pattern ghost/menu/NBT validation, binding limits, fair rotation and save/load, output capacity and ingredient reservation, both greenhouse variants, native smithing pattern preservation, real AE2 link handlers, idle real-grid read rejection, paused mineral production, five-tick cache expiry/invalidation/server isolation, ordinary/advanced real-grid oversized output, conversion before real ME insertion, extraction equipment persistence, real ME fluid refill, real Applied Flux FE refill, empty-fluid ambiguity, discovery deduplication, stale-controller output rejection and concurrent factory storage.
- The AE fixtures use actual powered/channelled Wireless Access Points, drives and item/fluid/FE storage cells, not an always-successful network stub. Initial fixture failures came from a drive facing the wrong way, and a non-energy item chosen for an FE test; those fixtures were corrected. The packer path race and stale-output crash were actual defects and were fixed.
- `scripts/validate_docs.py` and `git diff --check` passed. Full configured development runtime logs contain unrelated third-party absent-item recipes/update checks; there are no JDTE recipe parsing errors in the final suite.

### Runtime scope and continuation

See `docs/validation/forge-1.20.1-network/README.md` and `docs/validation/forge-1.20.1-upgrades/README.md` for exact JAR checksums and runtime outcomes. The final JAR passed the full graphical integrated-client smoke, two simultaneous independent remote clients, a production server save/restart with those clients reconnecting, and both minimal server/client launches without AE2, Curios, JEI or Jade. The same remote clients and final full test suite also ran with Applied Flux absent/present respectively. All final test JVMs exited 0. The upgrade evidence records exact final checksums, inspected screenshots and selected log lines. Code commit `1588372` is pushed to the current development branch; subsequent evidence commits preserve this final runtime record.

Still unverified: an actually submitted nonempty AE crafting CPU job controlling every machine family, long-running public-server throughput/concurrency, all combinations of optional mods, full UI interaction for every AE2 equipment variant, and exhaustive Mystical Agriculture recipe-pack compatibility. Snapshot/cache tests do not claim those scenarios passed. Earlier unported registrations remain unchecked above.

Next: finish any runtime follow-up explicitly left pending in the evidence, then continue Seed Conversion / AE Acceleration and the remaining machine families according to dependencies. Keep the Forge version at 0.5.8 until the entire port and validation are finished. Continue from the pushed branch; do not reset to older baselines or publish a release.


## Real crafting jobs and production-chain follow-up (2026-10-27)

Starting checkpoint: `a36c6c5`, continuing the same development branch and version `0.5.8-forge-1.20.1`. Source remains pinned to JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`.

### AE job checkpoint

- Added two conditional AE2 GameTests using real crafting storage, a Pattern Provider, an encoded processing pattern, an actual asynchronous calculation and CPU submission. The completion case waits for a real CPU to finish; the cancellation case cancels the actual CPU. Neither uses a fake busy-CPU snapshot.
- A linked Mineral Extractor stays paused on the idle grid, runs when the submitted job is busy, pays FE and returns generated ore through its installed ordinary AE Output card. The standalone job finishes with its requested output in ME. Linked extraction equipment also refills from the same busy grid. Once the job completes or is canceled and the existing five-tick cache expires, FE and pending work remain unchanged during the paused observation interval.
- The processing pattern's cobblestone trigger is delivered to a neighboring chest. It is a fixture for an actual pending processing job; Mineral Extractor remains an independent ore producer and does not claim to process cobblestone. The source read upgrade gates on any active crafting job, rather than matching each machine to a recipe or limiting output to requested counts. A fast producer may generate extra items while a slow CPU/provider is still busy; those items legitimately remain in ME storage.
- Fixture corrections: asynchronous planners need a real network action host captured on the server thread; requesting a standalone job returns no requester link, so completion/cancellation is observed through the selected actual CPU. The fixture supplies sufficient FE for production throughout CPU scheduling. These were fixture changes; no production semantics were changed to satisfy the tests.
- Java 17 / Gradle 8.6 `test build runGameTestServer`: **77 JUnit tests and 87 required GameTests passed**, retaining all previous tests; JAR and separate probe reobfuscation passed. Selected evidence is in `docs/validation/forge-1.20.1-production-chain/ae-jobs.txt`.
- This verifies the native Mineral Extractor/ordinary AE Output/extraction path against real jobs. Every machine family, advanced-output job variants, active-job server restart and long-running multiplayer load are still separate validation work. Seed Conversion and the mixer family are the next implementation checkpoints.


### Seed Conversion checkpoint

- Registered the source one-per-machine Seed Conversion card, restricted to both greenhouse tiers. Newly harvested drops matching the planted seed are replaced one-for-one by the primary output only when that output has the Mystical Agriculture essence tag. Reusable planted templates, other drops and previously stored seeds remain untouched.
- Both real harvest paths perform seed replacement before fortune/capacity fitting and essence conversion. Converted amounts participate in capacity reservation; blocked output does not commit a harvest. The installed card uses the existing persisted upgrade handlers and generic bag/copier item rules.
- Added the pinned model/texture, Forge-conditional Mystical Agriculture crafting recipe, creative entry, translations and paired English guide pages. No optional MA API is referenced by the common upgrade implementation.
- Java 17 `test build runGameTestServer`: **77 JUnit and 93 required GameTests passed**, including both real AE-job tests and all prior tests. Six new tests cover upgrade compatibility/limit/NBT, non-essence crops, normal/large deterministic actual harvest boundaries, preserved planted templates/old stored seeds, converted-capacity backpressure and composition with recorded essence crafting. The MA-specific fixtures register only when MA is present.
- `seed-conversion.txt` retains selected actual suite output. A new graphical seed-specific check is not claimed here; mixer GUI/runtime validation is the next checkpoint.


### Fluid Mixer server checkpoint

- Ported both source registry tiers: `advanced_fluid_mixer` (100,000 FE/four upgrade slots) and `extended_fluid_mixer` (200,000 FE/eight slots). Both have two 8,000 mB inputs, an 8,000 mB output, a consumed catalyst slot, unordered inputs and optional second input. The original 100-tick/5,000-FE water/catalyst recipe produces 250 mB Time Fluid using Forge JDT 1.1.4's actual fluid ID.
- Ported source processing/accelerated batching, 8-batch Mixing Upgrade and the related Ultimate Mixing Parallel Upgrade bounded by real resources/output. Parallel tiers are mutually exclusive. Basic/advanced upgrade bags, Shift insertion and the typed copier accept the plain mixing items. Extended Upgrade conversion recognizes the mixer pair.
- Added full block/entity/menu/screen registrations, native models/loot/recipes/textures, translations, guide pages, JEI category/catalysts/click areas/stable recipe IDs and recipe-update refresh. A graphical mixer-specific check is still pending in this checkpoint.
- Forge-specific fixes: the JDT superclass intercepts fluid queries before attached capabilities, so the mixer explicitly exposes its combined three-tank capability. The machine controls its own recipe clock and does not apply JDT's inherited generic work delay again. All three tanks persist dirty state and resize/clamp together. Data slots split all tank quantities/capacity and progress/duration into 16-bit pieces, and rendering clamps to the visible bars.
- Normal/accelerated work respects AE read gating. Auto I/O accepts catalyst/inputs and exports only the product tank; AE output cannot extract input fluids/catalysts. Invalidated or detached combined endpoints cannot access a replacement machine. Ordinary destruction preserves FE, all fluids and progress but removes inventories/cards that drop separately.
- Ultimate Capacity and Ultimate Overclock now support this newly ported family in addition to mineral machines. Mixer Ultimate Overclock executes ten real ticks per server tick, one-tick recipes and fiftyfold FE per batch. Other previously unported Ultimate-compatible families remain restricted.
- Optional Mekanism integration adapts Forge 1.20's typed gas Chemical Infuser recipes to same-ID fluid equivalents, with the original 100 ticks/5,000 FE defaults. Unknown/unregistered equivalents are rejected; unexpected third-party recipe failures are logged rather than swallowed globally. Cache snapshots are scoped to recipe managers and reload generations. The configured runtime does not include Mekanism Extras; its specific liquid uranium/silicon fuel chain is not claimed as tested here.
- Java 17 `test build runGameTestServer`: **81 JUnit and 104 required GameTests passed**, preserving all prior 77/93. This adds four original parallel-limit unit tests and eleven game tests covering both tiers, resource costs, single/dual input, fresh-BE restoration, capability simulation/direction, high-bit menu values, all-tank capacity removal, codec validation, acceleration/read pause, Ultimate Overclock, ordinary-drop conservation, detached endpoints and bag/copier integration. Production and probe reobfuscation passed. Selected output is in `mixer-server.txt`.
- Next checkpoint: real processing-pattern fluid tasks with AE read/output, mixer GUI/JEI checks, optional-absence runtime checks and final evidence. No new server/client runtime pass is claimed before those checks run.

### Mixer AE processing-job checkpoint

- Added two real fluid processing-pattern jobs. The actual AE Pattern Provider pushes both the catalyst and 1,000 mB water directly into the real mixer capabilities; a real CPU waits for 250 mB Time Fluid. The Advanced mixer uses ordinary AE Output and the Extended mixer uses Advanced AE Output. Both finish, return exactly 250 mB, consume inputs once, pay exactly 5,000 FE and pause again when the CPU becomes idle.
- The full suite passed **81 JUnit and 106 required GameTests**, with all prior tests retained. `mixer-ae-jobs.txt` preserves selected output.
- Added compiled opt-in graphical checks for both mixer menus, high-bit fluid synchronization, Shift insertion of plain mixing cards, capacity removal and the actual JEI category. Added a separately reobfuscated production probe for both mixer tiers across actual chunk unload and a second server JVM. These runtime harnesses are outside the production JAR; execution results will be recorded by the next evidence checkpoint.

### Mixer runtime corrections checkpoint

- The first actual graphical run passed menu/data/JEI assertions, but screenshot inspection exposed Forge JDT's top-panel/menu-origin difference: the catalyst background and real clickable slot were offset by 30/26 pixels. Corrected the menu coordinates and extended the graphical probe to click the displayed catalyst slot through the actual screen and vanilla inventory packets. The corrected graphical run remains pending at this checkpoint.
- Added a real Extended Upgrade interaction regression. Forge stores separate four/eight-slot capability handlers; merely loading the old machine snapshot leaves installed cards in the inactive four-slot handler. Mixer tier conversion now migrates the tagged cards to the active eight-slot handler, clears the inactive copy and refreshes capacities/client state. The regression preserves all three tanks, FE, catalyst, 73 ticks of progress and the installed read gate.
- The full Java 17 `test build runGameTestServer compileClientSmokeJava` run passed **81 JUnit and 107 required GameTests**, including production/probe reobfuscation. No previous tests were removed.
- The production optional-absence seed JVM passed real unload/reload and exact snapshots. The first restart fixture checked resumed work after a fixed 35 startup ticks and failed its completion assertion even though its disk-restoration comparison passed. The rerun records the actual state at that point and waits, with a bounded timeout, for natural work completion. Final restart results are pending; no restart pass is claimed here.

### Capacity-safe mixer tier migration

- Expanded tier-conversion coverage to Ultimate Capacity in the last slot, with 4,000,000 FE and 120,000/130,000/140,000 mB stored fluids. Per-slot migration could temporarily expose base capacity before the last card arrived. Restoring the full eight-slot handler before any capacity refresh prevents this resource loss, and clears the inactive four-slot copy without intermediate callbacks.
- Full build/reobfuscation and the complete suite passed **81 JUnit and 108 required GameTests**. Earlier 107/81 results remain historical checkpoints. Final graphical and production runs are being repeated with the final production JAR.

### Mixer small-window presentation

- Clamped mixer GUI placement so Forge JDT's title, drawn above the inventory origin, remains visible at the actual 240-pixel minimum scaled height. Dynamic resize validation now recognizes the mixer origin, preventing repeated stale-layout rebuilds.
- The graphical catalyst fixture now sends both mouse press and release and separates cursor pickup from the displayed-slot click across server synchronization. Earlier click-fixture assertions failed; a diagnostic rerun passed the full previous graphical suite and real catalyst click before this final title adjustment. Final screenshots/results follow after the new runtime run.
- Repeated the complete suite after the GUI changes: **81 JUnit and 108 required GameTests passed**, with production/probe reobfuscation and compiled graphical harnesses.

### Final production-chain runtime checkpoint

- Final source checkpoint `b2466c5` passed the complete **81 JUnit / 108 required GameTest** suite, JAR/probe reobfuscation, the actual graphical client and two actual minimal production server JVMs. Final graphical/prod/optional client process exits were 0, 0/0 and 0. The production JAR checksum and selected logs are saved in [production-chain evidence](validation/forge-1.20.1-production-chain/README.md).
- The real client repeated all previous mineral/survey/storage/copier/pattern checks and passed both mixer menus, high-bit quantities, plain Mixing Shift insertion, real displayed catalyst mouse interaction, all-tank capacity clamping and JEI. Inspected screenshots show correctly aligned catalyst items and fully visible titles at the minimum scaled height.
- Both production mixers genuinely unloaded/reloaded with exact FE, all three fluids, catalyst, upgrade cards and progress. A fresh server JVM restored those fields and naturally completed the saved 73/100-tick recipe once with exact 1,000 mB water, catalyst, 5,000 FE and 250 mB Time Fluid accounting. AE2/Curios/JEI/Jade/Mekanism/Applied Flux were absent in that server; the actual optional-absence graphical title/registration check also passed.
- Earlier probe assertion failures and their final reruns are retained in the evidence README. A compiler pass is not presented as a runtime pass. New independent two-player mixer checks, active-AE-job restart, the Mekanism Extras liquid-fuel chain, exhaustive crop-pack behavior and long-running stress remain unverified.
- Seed Conversion, both source mixer tiers, Mixing and the related Ultimate Mixing Parallel are now implemented. Real AE CPU completion/cancellation and ordinary/advanced mixer-output jobs are verified. Unchecked registrations above remain unported; AE Acceleration and the remaining energy/entity/biological machinery are useful next checkpoints. Keep version 0.5.8, preserve this branch's commits and do not publish a release or merge main.

## AE acceleration and mixer concurrency follow-up (2026-10-28)

Starting commit: `5498124`; reference remains `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Version remains `0.5.8-forge-1.20.1`. This work is limited to AE Acceleration, mixer multiplayer, active-job restart and available Mekanism Extras fuel compatibility.

### AE Acceleration implementation checkpoint

- Added the source one-per-machine AE Acceleration card, restricted to Basic/Advanced/Extended Time Accelerators, including the AE2-conditional speed-card/overclock/time-crystal recipe, original model, creative entry, translations and paired English guide updates. Existing upgrade handlers provide NBT, menu, insertion, bag and copier integration.
- Each accelerator now needs its own card to discover native AE2 `IGridTickable` services. Removing the card or disabling AE2 acceleration removes its queued AE contribution; overlapping unupgraded accelerators cannot borrow another contributor's card. Native block-entity/random tick behavior and existing fluid-cost/budget settings remain in place.
- Ported the source external-backend registration contract and frame ordering for optional JDTE-AE integration. Active external handles receive submissions; inactive targets reject fallback and charging; backend-owned routing suppresses built-in per-device AE calls. Cleanup is performed after execution and on level/server shutdown. No third-party backend mod is bundled or claimed as runtime-tested.
- Corrected the older bridge's sleep accounting to match 0.6.5.2: a request that only makes endpoints sleep is not counted as useful work. Loaded-position checks prevent queued target lookups from forcing chunks.
- Java 17 `test build runGameTestServer`: **91 JUnit and 112 required GameTests passed**, retaining all previous 81/108. Ten original backend-registry tests and four new GameTests cover registration/limit/recipe/NBT, real powered Inscriber service calls and sided deduplication, per-contributor scheduler gating/backlog removal and external routing. The native Inscriber advances two processing units per one-tick service call; early fixture expectations of one unit were corrected without changing production behavior.
- Production and probe JAR reobfuscation passed. Real two-client mixer, active CPU restart and Mekanism Extras production checks are the next checkpoints; no new runtime pass is claimed here.

### Active-job restart and compiled runtime fixtures

- Two actual production Forge JVMs passed active real AE CPU save/shutdown/restart. A real provider feeds the Extended mixer; the first server saves partial work while the CPU remains busy. The next server restores the actual CPU job and exact machine NBT, completes exactly 500 mB through Advanced AE Output at 10,000 FE for two batches, then pauses. See [AE runtime evidence](validation/forge-1.20.1-ae-runtime/README.md).
- Added four unit tests for frame ordering and failure propagation/cleanup. Registry-dependent world discovery is separated from the pure frame runner. The complete build now passes **95 JUnit and 112 required GameTests**, retaining earlier 81/108; production/probe reobfuscation and remote-client compilation passed.
- Added compiled two-client mixer fixtures and opt-in actual Extras fuel probes. Their execution remains pending in this checkpoint. Extras 1.5.0 for Forge 1.20.1 was obtained from its public release metadata with a verified artifact hash; the exact historical uranium/silicon recipe is not present in its datapack resources.

### Mekanism Extras fuel checkpoint

- Actual Forge Extras 1.5.0 + Mekanism 10.4.16.80 production runtime passed both mixer tiers with the source chemical-infuser fuel recipe, reversed inputs, exact 8/25 batch output and 5,000 FE per batch. The real downstream rotary recipe accepts the output and returns the correct fuel gas. This tests the actual addon's recipe rather than a synthetic substitute.
- The registered output is `mekanism_extras:fluorinated_naquadah_uranium_fuel`, localized as 液态含氟硅岩铀燃料. The addon uses naquadah/硅岩; absence of an elemental `silicon` recipe name does not imply this fuel chain is absent. Exact IDs, release hashes, selected third-party warnings and reproduction are retained in [AE runtime evidence](validation/forge-1.20.1-ae-runtime/README.md). No full reactor cycle or all-Extras compatibility is claimed.
- A repeat two-JVM active crafting job test also passed with the later frame-helper refactor and AE2 present but Curios/JEI/Jade absent. Mixer multiplayer reruns exposed fixture-only logout pickup and delayed old-world entity loading; the baseline cleanup and natural pickup setup have been corrected. No passing full multiplayer result is claimed until its final rerun.

### Natural AE acceleration and fixture corrections

- Added a fifth AE Acceleration GameTest using actual natural server block ticks and the global scheduler, rather than manually driving a frame. The installed card finishes a real Inscriber silicon-print recipe with exactly one output and a reusable press, and records the source whole/fractional Time Fluid cost. Added explicit config-disable backlog invalidation to the existing per-contributor test.
- Full Java 17 build/reobfuscation and **95 JUnit / 113 required GameTests passed**, retaining all earlier tests. A subsequent resources/build pass corrected the guide's opening text: only linked AE cards need a Wireless Access Point; local AE Acceleration needs no binding.
- The early natural-cost assertion expected a whole-mB decrement too soon. The source retains fractional cost in `pendingFluidCost`, so the corrected test checks a whole debit or saved positive fractional debt. Production costs were not changed.
- Two early mixer multiplayer attempts are retained in the evidence folder: the first waited for a randomly tossed logout cursor stack outside stationary player reach; the second included 64 old world items loaded after ServerStarted, before any client interaction. The fixture now performs its one-time old-drop cleanup after client chunks are ready and before establishing the 128-catalyst/5-card baseline, then observes every subsequent transfer/drop without deleting items. Reconnected players are moved into natural pickup range. Control phases are written atomically. The full rerun is next; these attempts are not counted as passing validation.

### Submitted accelerator lifecycle guard

- Final review found a capability-lifetime risk when an accelerator is dismantled after submission but before END-phase processing. The scheduler now admits only nonremoved, loaded contributors belonging to the current level and current world block-entity object before querying upgrades or retaining backlog.
- Added a regression that queues real Inscriber work, dismantles the accelerator and clears the old object's removed flag. Neither invalidated capabilities nor the detached reference can execute queued work. Full build/reobfuscation and **95 JUnit / 114 required GameTests passed**, preserving original 81/108. No public-server crash is claimed; this risk was found during review.
- Previous final same-JAR multiplayer, active-job restart and fuel tests passed at the 113-test checkpoint. The final runtime rerun is being repeated with this guard; no other machine family is being expanded.

### Final bounded follow-up runtime checkpoint

- Final source checkpoint `9970bd6` passed **95 JUnit and 114 required GameTests**, full build/reobfuscation and the final same-JAR runtime checks. All original 81/108 tests remain. Final checksums, exact unit suites, logs, process exits and inspected screenshots are in [AE runtime evidence](validation/forge-1.20.1-ae-runtime/README.md).
- Two actual independent client JVMs passed simultaneous Advanced/Extended mixer catalyst races, Shift/bag upgrade insertion, concurrent copier pastes, authoritative menu snapshots, disconnect with a cursor, natural pickup, real chunk unload, saved shutdown and reconnection to a fresh dedicated server. All phases conserved 128 catalysts/five Mixing cards and exact three-tank/FE/progress state. Both servers and both clients exited 0.
- The final two-JVM active-job test restored the actual nonempty busy CPU and mixer state, resumed partial work, returned exactly 500 mB through Advanced AE Output at 10,000 FE and paused. No plan was recreated after loading. The final actual Extras fuel/downstream recipe test also passed against the same production JAR.
- The first multiplayer attempts failed at fixture-only pickup/pre-interaction contamination boundaries and are retained with their corrections. No item-bearing state was excluded from persistence comparisons, and no gameplay-phase drops were deleted to obtain a passing ledger.
- This follow-up implements AE Acceleration and verifies the requested mixer/AE persistence/fuel paths. Real third-party JDTE-AE whole-grid scheduling, prolonged public-server stress, full operating reactor cycles and all other Extras/mod combinations remain unverified. Other large machine families were not expanded. Keep 0.5.8, preserve the pushed checkpoints and do not merge main or publish a release.

## Complete inventory audit and bounded energy-upgrade work (2026-10-28)

Starting checkpoint: `020109b`. Rebuilt the complete pinned-source inventory rather than inferring completion from the initial registry delta. At audit start, source has **120** fixed item registrations, Forge **98**; **22** are absent. The initial classifications are **30 verified paths / 19 implemented but unverified / 49 partial / 22 absent**. The full English matrix, curated behavior notes and generated JSON include every item from the original checklist, concrete implementation paths, named BE/menu wiring and static resource signals. Run `python scripts/audit_forge_port.py --check` to detect drift without starting Minecraft.

Known gaps include Ultimate support on many already registered machines, newer Time Accelerator timing/proxy behavior, greenhouse matrix hooks, accelerated brewing, ordinary native tier migration and differing Ultimate Portal Gun manual-fluid costs. Missing method names alone were not treated as missing gameplay: inherited Forge methods are considered. Existing core/runtime tests remain valid for their stated paths but do not certify those missing behaviors.

Selected bounded implementation: **Energy Brewing Upgrade + Energy Overload Upgrade**, including real fuel/FE behavior, one-card compatibility, configuration, common bag/copier/menu persistence, recipes/resources and tooltips. Existing JDT Energy Transmitter is a source-supported overload target; the absent advanced/extended transmitter families remain absent and are not being implemented in this pass. The independent JDTE-AE backend, long-running multiplayer and complete reactor operation stay explicitly unverified. No expensive repeated multiplayer/reactor runs are planned for these changes.


## Full inventory and energy upgrade checkpoint (2026-10-28)

### Audit and bounded implementation

- Rechecked all **120** source item registrations at `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859` against actual Forge items, blocks, BE/menu registries, models, language entries, recipe names and concrete implementations. The [full matrix](forge-1.20.1-feature-matrix.md) replaces checkbox presence as the authoritative parity classification; JSON and a cheap `--check` generator are retained.
- Forge now registers **100** of the source items. The conservative matrix has **32 verified / 19 implemented-unverified / 49 partial / 20 absent** entries. Verified refers only to stated paths; a verified card does not complete all behavior of its host machine.
- Implemented Energy Brewing Upgrade and Energy Overload Upgrade as the source's single-card plain items, including original models/textures, Forge-shaped recipes, translations, creative-tab entries and paired English guide pages. Restored the source upgrade tag for currently registered items only, without dangling unported IDs.
- Energy Brewing buys the source **20 fuel credits / 5000 FE**, configurable from 0 to 1,000,000, with Creative/free-price bypass and the normal per-stage FE charge preserved. Purchases require valid ingredients and output room. Existing powder remains intact; manual/Shift/automation fuel insertion is rejected while installed and restored on removal. The canonical Forge upgrade capability persists the card alongside existing credits/progress, avoiding a second unsaved handler.
- Energy Overload returns `Integer.MAX_VALUE` from native JDT `EnergyTransmitterBE.fePerTick()`. Actual transfer still spends stored FE and respects recipients. It uses real native transfer, not simulated generation. Advanced/Extended JDTE transmitters remain unported and are explicitly excluded from this checkpoint.
- Both cards integrate with existing machine slots, Shift insertion, basic/advanced bags, typed copier validation, machine storage-panel limits and Jade's generic installed-card serialization. Graphical appearance of these new cards has not been run.
- The new copier regression exposed a real Forge data-format bug: unlocked/partly empty brewing templates serialize as `air/Count:0`, but the decoder only accepted `{}`. New saves use `{}` and both canonical empty formats decode; unknown items, positive-count air and malformed templates still reject. Paste spends exactly one bagged brewing card without copying FE.

### Validation and limits

- Java 17 / Gradle 8.6 `test build runGameTestServer`: **100 JUnit and 123 required GameTests passed**, including production and separate probe JAR reobfuscation. Original 95/114 and the original 24 mineral core tests remain intact. Added five pure transaction tests and nine actual Forge integration tests.
- New tests cover real twenty/next brews, exact costs, no-input/full-output/insufficient-FE behavior, retained powder, all six input faces, menu/Shift validation, mid-brew BE restoration, Creative/zero-price/legacy fuel, bags/copier costs, both empty template formats and actual native transmitter delivery/conservation/removal/persistence.
- Production reobfuscated JAR two-JVM saved-shutdown/restart verification **passed** with optional AE2/Curios/JEI/Jade absent: normal shutdown at live brew tick 175/400 restored exactly in a fresh JVM, finished one Awkward Potion for only 300 additional FE, retained powder/19 credits and preserved both cards. Live overload Mixin and removal/reinsertion passed; both processes exited 0. See [energy-upgrade evidence](validation/forge-1.20.1-energy-upgrades/README.md) for logs, checksums and the reproducible harness.
- Did not repeat the unaffected expensive two-client, real AE-job or Mekanism fuel runs. Their previous scoped evidence remains valid; this is not a new certification of those paths. Installed JDTE-AE whole-grid backend, prolonged multiplayer uptime and complete operating reactors remain **unverified**.
- First attempts failed on a JDT-only type accidentally referenced from the plain unit-test classpath, test fixtures that reused an existing brewer and assumed Shift-kept powder in its original player slot, and the real empty-template copier bug. The final full passing run uses a Minecraft-independent FE transaction boundary, fresh fixtures, item-conservation checks and the codec fix. No tests were deleted or disabled.

### Next development boundary

Use the matrix before choosing work. A bounded next energy slice is fixing the Generator Upgrade's one-mB drain edge case and validating its native generator paths, or adding one source energy-transmitter tier with its full settings/filter/energy contract. Do not silently expand Ultimate card support across all inherited machines: each host needs its own operation/capacity tests. Time Accelerator timing/proxy/cache additions, Greenhouse matrix support, brewing coalesced acceleration and the Ultimate Portal Gun numeric mismatch are recorded partial work. Keep version 0.5.8; no main merge or release.


## Generator Upgrade fuel correction (2026-10-28)

- Fixed upgraded fluid generation to simulate the complete two-mB fuel debit and full three-times-native FE credit before mutation. One leftover mB survives repeated attempts and BE reload. Full/partially full FE buffers and inactive redstone do not consume fluid. Unexpected short commits restore both owned buffers; integer-overflowing output is rejected.
- New native solid-generator coverage exposed two previously unexercised Forge port bugs: vanilla coal needs `ForgeHooks.getBurnTime`, and the non-Mixin helper cannot live in the reserved Mixin package. Corrected both and verified the two-coal/three-times-output behavior while retaining the insufficient-single-coal path and ordinary one-mB non-upgraded fluid behavior.
- **107 JUnit / 127 required GameTests passed**, retaining all previous 100/123. Added seven pure batch-policy tests and four actual native-generator tests. Initial solid-fuel runs failed on the raw burn-time hook and helper-package classloading restriction; the final rerun passed after actual fixes, without deleting or disabling tests.
- Generator Upgrade remains **P**: the native defect is fixed and tested, but the source Large Fuel Canister-specific path cannot be complete while that item remains unported. No broad Ultimate compatibility gate was removed. Final combined production JAR/dedicated-server checks will follow the other requested corrections.
- Documentation roles are now explicit: the complete matrix remains authoritative for functional acceptance; this file keeps chronological historical evidence. Current Status replaces the stale latest-checkpoint pointer, and `audit_forge_port.py --check` also checks its 120-item counts against the matrix. These documentation checks do not start Minecraft.


## Extended Upgrade interface and transaction correction (2026-10-28)

- Implemented real state migration for **all 15 currently available conversions**: eight native T2 variants, Advanced/Extended Time Accelerator, Time Freezer, Fluid Stabilizer, Life Extractor, Infusion Machine, Gel Generator and Fluid Mixer. Restored the omitted existing Gel Generator conversion map entry. The five source targets whose extended machines are absent remain unsupported and keep the upgrade's **P** classification.
- Preflight loads a prepared target with a complete four-to-eight-slot upgrade transfer. It preserves slot positions, NBT and counts, clears the inactive four-slot storage, checks the actual item/FE/fluid/filter interfaces and refuses occupied inactive data instead of erasing it. Capacity cards are installed as one complete state before capacity synchronization, preventing transient FE/fluid clamping.
- Native T2 fields and Forge attachment storage differ. Explicitly migrated actual FE and filter pages; corrected normal Capacity scaling on the seven extended classes inheriting T1 execution, kept the native T2 **32,000-mB** base for both extended fluid machines, and made Extended Dropper's real FE storage persistent. These are real capacity/interface fixes, not removal of Ultimate compatibility limits.
- Successful conversion clears the detached original item, upgrade, energy and fluid buffers. Invalidating a LazyOptional alone cannot revoke a handler already held by a caller. Failed replacement restores one original inventory and all actual FE, keeps the upgrade card, clears detached/staged copies, and logs the failure; rollback failure is propagated rather than swallowed.
- Added the Forge placement-protection path: the conversion owns its source snapshot while temporarily suspending the outer generic item placement snapshot capture. It posts the real placement event before accepting the transaction, restores capture state in `finally`, and rolls back a cancellation with source BE data intact. This avoids a generic snapshot captured after detachment restoring an empty original block.
- **112 JUnit / 133 required GameTests passed** with full production/probe build and reobfuscation. Added five pure slot-expansion tests and six Forge tests, including a test that loops through every available conversion with tagged inventory, all four cards, nearly full FE, applicable tanks, live automation, the eighth slot and a fresh BE reload. Dedicated tests exercise native filter pages, stale interfaces, denied/wrong replacements, occupied inactive storage, clicker auxiliary fluid/facing and real cancelled Forge placements for both native placer and dropper.
- Earlier runs uncovered actual missing inherited capacity scaling, Extended Dropper FE persistence and T1-versus-T2 reservoir shrinkage; all were fixed and the final run includes all 15 paths. Fixture corrections supplied Forge LazyOptional exception suppliers and injected replacement failures through the transaction boundary instead of constructing an unregistered Item after registry freeze. No failed path or baseline test was skipped.
- Actual production-JAR two-JVM save/restart checks remain pending at this code checkpoint. New graphical and independent multiplayer runs have not been performed. Installed JDTE-AE whole-grid scheduling, prolonged multiplayer uptime and full reactor operation remain unverified.


## Production tier restart and acceptance checkpoint (2026-10-28)

- Production gameplay checkpoint `b2d329e` and the separate probe passed **all 15 available real conversion paths** in one dedicated seed JVM and exact full-BE NBT restoration/live capability checks in a fresh JVM. Both final processes normally saved all dimensions and exited **0**, with AE2/Curios/JEI/Jade absent. This is new production disk-state evidence in addition to 112 JUnit/133 required GameTests and the full reobfuscated build.
- The seed checks physical inventory/card NBT, actual FE, applicable almost-full fluid tanks, native filter templates, empty inactive slots, detached endpoint conservation and a working eighth slot. It exercises an actual native-dropper Forge placement cancellation before successful conversion. The fresh process restores every tested BE and naturally ticks them before withdrawing through real item/energy capabilities.
- The production Generator conserves one-mB fuel during natural ticks, consumes a two-mB batch atomically, restores the odd remainder across a real saved shutdown and rejects 200 further incomplete attempts without FE generation or fuel loss.
- The first probe fixture failed by assuming native SensorT2 implements RedstoneControlledBE. The pause helper was corrected to inspect the actual interface; final full-state/live-interface checks include the sensor. No source path was dropped. Selected logs, first failure, checksums, process outcomes and reproduction are in [parity-fix evidence](validation/forge-1.20.1-parity-fixes/README.md).
- Acceptance remains **V 32 / U 19 / P 49 / N 20**, all 120 items accounted for. Generator Upgrade remains P because the source Large Fuel Canister path is absent. Extended Upgrade remains P because five source target machines remain absent. Eight extended machine rows retain P because broader Ultimate/operation parity is not proved by conversion tests. No item was promoted to V from compilation alone.
- No new graphical, independent multiplayer or stress runs were necessary for these server/interface corrections. Their absence is explicit; existing historical evidence is unchanged. Ultimate Portal Gun consumption, broader Ultimate hosts, installed JDTE-AE whole-grid scheduling, prolonged multiplayer and complete reactors remain the stated next work/unverified boundaries.


## Ultimate Portal Gun source consumption correction (2026-10-28)

- Restored the pinned source's actual manual prices: **500 mB** cross-dimension, **25 mB/block** in the same dimension, rounded up and capped at **25,000 mB**. The old Forge values were 1,000,000 / 1000 / 500,000. Zero-distance manual shots may use an empty fluid tank while still paying the normal native FE cost.
- Source firing uses JDT's `PORTAL_GUN_V2_RF_COST`; its Forge 1.20.1 counterpart is **`Config.PORTAL_GUN_FE_COST`**, as confirmed against the actual native `PortalGunV2` bytecode. The source JDTE `ultimatePortalGun.energyCost` field is unused; retain it for config compatibility and clarify it in the config comment/guide instead of incorrectly charging it.
- Restored separate pricing for recorded favorites and previous destinations. Holding Shift must call native JDT pricing even when a manual slot is selected. Source-compatible public calculation helpers now delegate to a pure tested policy. Whole FE/fluid checks precede firing; a cancelled native projectile addition no longer charges resources.
- Actual item capabilities retain the source default **1,000,000 mB / 1,000,000 FE**, configured through the existing JDTE capacity fields. Tests fill 30,000-mB/12,000-FE configured buffers, reject overfill/wrong fluid, save/reload the item and preserve resource amounts/manual flags. Added paired English GuideME pages and clarified native versus legacy energy settings.
- **120 JUnit / 140 required GameTests passed**, including all earlier 112/133, with full build and production/probe reobfuscation. Added eight pure pricing tests and seven actual Forge tests: native projectiles and exact debits, cross/same dimensions, previous/recorded/manual routing, rounding/cap/zero cost, configured capacities/item NBT, insufficient funds and real cancelled entity joins.
- A separate reobfuscated production server with JDT/GuideME and AE2/Curios/JEI/Jade absent passed real native shots, native 173-FE pricing despite a different legacy JDTE value, manual 500/250-mB debits, configured capacities, saved-item restoration, previous routing and the cap; normal saved shutdown and **exit 0**. The first test fixture assumed immediate spatial-query visibility in a newly loaded chunk and failed that assertion. The final fixture records actual per-owner Forge entity joins and requires accepted live native projectile objects; the GameTests retain their actual spatial-query checks. Initial NeoForge config-field naming also failed compilation and was corrected to the verified Forge API. No gameplay test was removed or disabled.
- The matrix promotes only Ultimate Portal Gun's verified stated paths from **P to V**. Current totals: **V 33 / U 19 / P 48 / N 20**, 120 items / 100 registrations. Fresh graphical portal-editor validation and a new independent multiplayer run were not performed. Generator and Extended Upgrade retain P for their source features blocked by absent items/targets.
- This bounded round completed the three requested resource/interface corrections. Broader Ultimate support was analyzed but not unconditionally enabled: common scaling helpers do not prove every host's capacity and per-operation behavior. Next work can start with a narrow extended-native Capacity group; Ultimate Overclock must also implement and verify host-specific batching and FE costs. Whole-grid JDTE-AE, prolonged multiplayer and full operating reactors remain unverified.


## 2026-10-28 — interrupted transmitter workspace recovered

The current workspace retained the full uncommitted transmitter port from `fb64caa`. Before changing source, a complete binary tracked diff, all 41 untracked files (including the pre-existing Python cache), prior logs, JUnit XML and SHA-256 checksums were saved at `/workspace/jdte-recovery-20261028`. Source and retained full successful core log were then committed as **`a115219`**, explicitly WIP, and pushed to independent branch `codex/recovery-energy-transmitters-20261028`. No reset, clean, main-branch merge or release was performed.

The prior log reports **151 required GameTests**, and retained XML totals **131 JUnit tests, zero failures/errors/skips**. These are recovered evidence, not a claim that recovery itself reran those checks. The previously unchecked native-to-Extended converter moves owned FE rather than a network aggregate. Advanced and Extended are separate source machine paths, not successive tiers.

Follow-up implementation adds explicit unloaded-state guards, source particle/player-binding copier codecs, source Jade network/player status and English guide pages. Dedicated optional tests now connect a real powered AE grid and Applied Flux disk. A separate production probe exercises two transmitter tiers with optional mods absent, full-inventory wrench transport, actual chunk unload and a fresh JVM.

Initial follow-up failures are retained and being corrected: Forge `LazyOptional.orElseThrow` requires a supplier; invoking `onLoad` on an explicitly unloaded object does not rebuild its invalidated attachment capabilities (use a fresh deserialized BE); native transmitter network FE is unavailable before native discovery; Forge FakePlayer suppresses dropped items (use an ordinary server-player actor for the full-inventory branch); the partial-receiver Applied Flux fixture needs further diagnosis. These failed attempts are not accepted validation.

The fresh-BE regression exposed a real Forge API gap: the inherited JDT transmitter save method does not persist its owned `TransmitterEnergyStorage` field. The Extended tier now explicitly writes/loads its local `energy`, clamped to actual capacity; this remains under regression validation. Early production-drop lookup also ran during `ServerStartedEvent`, before normal entity tracking; the transport phase now waits for running server ticks. No failing assertion was removed or relaxed.

### Follow-up regression checkpoint

The complete `build runGameTestServer` run now passes **131 JUnit / 154 required GameTests**, full build and both JAR reobfuscation tasks. The unload/fresh-load test confirms the Extended owned-FE persistence fix. The Applied Flux zero-extraction failure was a fixture topology error: the extra energy cell powered an isolated channelled grid beside the AP's unexposed side. Bridging through the existing energy cell and asserting grid identity makes real 50,000 FE disk delivery pass. A real 1,000 FE extraction/40 FE partial receive/960 FE failed refund now survives snapshot and node teardown, and a fresh BE refunds that credit exactly once after reconnecting. See retained compressed logs/XML and failure excerpts in [transmitter evidence](validation/forge-1.20.1-energy-transmitters/README.md). Production checks are separate and still pending at this checkpoint.
