# Forge 1.20.1 port — work in progress

Source: JDTE 0.6.5.2 (`ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`).
Target baseline: `ac1d813`, Forge 47.4.10, Just Dire Things Forge 1.1.4, Java 17, Gradle 8.6.

This is an ongoing port, not a completed 0.6.5.2 Forge release. The existing version remains 0.5.8 until feature migration and validation finish.

Latest checkpoint: [2026-10-27 follow-up](#mineral-follow-up-and-upgrade-storage-checkpoint-2026-10-27), with [graphical evidence](validation/forge-1.20.1-minerals/README.md).

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

## Item registrations requiring migration

- [ ] `jdte:advanced_ae_output_upgrade`
- [ ] `jdte:advanced_bio_extractor`
- [ ] `jdte:advanced_energy_transmitter`
- [ ] `jdte:advanced_entity_accelerator`
- [ ] `jdte:advanced_essence_conversion_upgrade`
- [ ] `jdte:advanced_fluid_mixer`
- [x] `jdte:advanced_machine_settings_copier` (typed Forge presets, upgrade exchange and storage sources implemented)
- [x] `jdte:advanced_upgrade_storage` (36 slots, machine actions, Curios lookup and Forge persistence implemented)
- [ ] `jdte:ae_acceleration_upgrade`
- [ ] `jdte:ae_crafting_read_upgrade`
- [ ] `jdte:ae_extraction_upgrade`
- [ ] `jdte:ae_output_upgrade`
- [ ] `jdte:cainiaoyo_fumo`
- [ ] `jdte:charged_lightning_rod`
- [ ] `jdte:crafting_pattern`
- [ ] `jdte:energy_brewing_upgrade`
- [ ] `jdte:energy_overload_upgrade`
- [ ] `jdte:entity_acceleration_upgrade`
- [ ] `jdte:essence_conversion_upgrade`
- [ ] `jdte:extended_bio_extractor`
- [ ] `jdte:extended_energy_transmitter`
- [ ] `jdte:extended_entity_accelerator`
- [ ] `jdte:extended_experience_holder`
- [ ] `jdte:extended_fluid_generator`
- [ ] `jdte:extended_fluid_mixer`
- [ ] `jdte:extended_generator`
- [ ] `jdte:large_fuel_canister`
- [x] `jdte:large_mineral_extractor` (implemented; server integration tests passed; graphical integrated-client and dedicated-server checks passed; independent remote clients pending)
- [ ] `jdte:large_pocket_generator`
- [ ] `jdte:large_potion_canister`
- [ ] `jdte:maiqilin_fumo`
- [x] `jdte:mineral_extractor` (implemented; server integration tests passed; graphical integrated-client and dedicated-server checks passed; independent remote clients pending)
- [x] `jdte:mineral_survey` (implemented; server integration tests passed; graphical integrated-client and dedicated-server checks passed; independent remote clients pending)
- [ ] `jdte:mixing_upgrade`
- [x] `jdte:repair_talisman` (ported; runtime validation pending)
- [ ] `jdte:seed_conversion_upgrade`
- [ ] `jdte:thunderstorm_upgrade`
- [ ] `jdte:time_multitool`
- [x] `jdte:ultimate_capacity_upgrade` (mineral machines only; other machine families remain pending)
- [ ] `jdte:ultimate_mixing_parallel_upgrade`
- [x] `jdte:ultimate_overclock_upgrade` (mineral machines only; other machine families remain pending)
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

## Current validation evidence

- Clean Java 17 build: passed, including reobfJar.
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
