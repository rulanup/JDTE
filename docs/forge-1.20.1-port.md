# Forge 1.20.1 port — work in progress

Source: JDTE 0.6.5.2 (`ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`).
Target baseline: `ac1d813`, Forge 47.4.10, Just Dire Things Forge 1.1.4, Java 17, Gradle 8.6.

This is an ongoing port, not a completed 0.6.5.2 Forge release. The existing version remains 0.5.8 until feature migration and validation finish.

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
- [ ] `jdte:advanced_machine_settings_copier`
- [ ] `jdte:advanced_upgrade_storage`
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
- [x] `jdte:large_mineral_extractor` (implemented; server integration tests passed; interactive client checks pending)
- [ ] `jdte:large_pocket_generator`
- [ ] `jdte:large_potion_canister`
- [ ] `jdte:maiqilin_fumo`
- [x] `jdte:mineral_extractor` (implemented; server integration tests passed; interactive client checks pending)
- [x] `jdte:mineral_survey` (implemented; server integration tests passed; interactive client checks pending)
- [ ] `jdte:mixing_upgrade`
- [x] `jdte:repair_talisman` (ported; runtime validation pending)
- [ ] `jdte:seed_conversion_upgrade`
- [ ] `jdte:thunderstorm_upgrade`
- [ ] `jdte:time_multitool`
- [x] `jdte:ultimate_capacity_upgrade` (mineral machines only; other machine families remain pending)
- [ ] `jdte:ultimate_mixing_parallel_upgrade`
- [x] `jdte:ultimate_overclock_upgrade` (mineral machines only; other machine families remain pending)
- [ ] `jdte:ultimate_time_wand`
- [ ] `jdte:upgrade_storage`
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
