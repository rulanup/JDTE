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
- Adapted mineral production/output planning and survey data codecs to Java 17 and FriendlyByteBuf; machine integration is pending.

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
- [ ] `jdte:large_mineral_extractor`
- [ ] `jdte:large_pocket_generator`
- [ ] `jdte:large_potion_canister`
- [ ] `jdte:maiqilin_fumo`
- [ ] `jdte:mineral_extractor`
- [ ] `jdte:mineral_survey`
- [ ] `jdte:mixing_upgrade`
- [x] `jdte:repair_talisman` (ported; runtime validation pending)
- [ ] `jdte:seed_conversion_upgrade`
- [ ] `jdte:thunderstorm_upgrade`
- [ ] `jdte:time_multitool`
- [ ] `jdte:ultimate_capacity_upgrade`
- [ ] `jdte:ultimate_mixing_parallel_upgrade`
- [ ] `jdte:ultimate_overclock_upgrade`
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
