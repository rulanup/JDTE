# Cainiaoyo Fumo validation checkpoint

Source `ccf46472`, target Forge47.4.10/JDT1.1.4, version0.5.8. Classification U pending runtime acceptance.

The Gradle build and main JAR reobfuscation passed;196 JUnit tests passed. The targeted GameTest exercises all horizontal facing shapes and the source self-drop through the real loot table.

The first complete GameTest run failed one existing Time Multitool silk-ice drop-count assertion. Its test world had been reused from earlier runs; that is a possible cause, not yet established. The full log is retained. The entire prior world was preserved at `runs/gameTestServer/world-before-fumo-8dd2a705ad41`, and a fresh-world suite was started without changing any prior assertion. Graphical/JEI and independent dedicated-server behavior remain unverified.

Documentation validation initially treated this hidden decorative block as a machine requiring a guide. The validator now explicitly excludes the decorative block, matching source behavior; existing machine checks are unchanged.

## Fresh-world result

All **325 required GameTests passed** in the fresh world, including the new Cainiaoyo facing/actual self-loot test and the existing Time Multitool test. The first run's isolated failure remains documented; the fresh-world result does not establish its exact cause. The original test world and both complete logs are retained. The configuration label and description now use the pinned source translations. Real graphical model rendering, creative visibility toggle, JEI visibility and independent dedicated-server save/load are still unverified, so the item remains U.

The final build after adding source configuration translations passed, including JAR reobfuscation and the JUnit suite (`build-final.log.gz`). The fresh-world GameTest was run before that translation-only configuration metadata change.

## Remaining Fumo and reused-world ice investigation

Maiqilin and Xingye were ported from `bfbbff0` following Cainiaoyo exactly: shared decorative block, registrations, pinned 0.6.5.2 models/textures, facing blockstates, self loot, translations and default-hidden creative/JEI visibility. GameTests for all three cover facing shapes and real self loot. Build and runtime validation are pending, so both remain U.

Time Multitool: the initial log's only failure is the silk-ice assertion in `realHarvestRetainsIceCallbackAndSilkTouchDrops`. That test scans an 11-wide box for all ice item entities, so pre-existing ice in a reused world can fail it without any tool defect, and the fresh-world pass is consistent with that explanation. The old world is gitignored and not present in this checkout, so no historical ice count is asserted here. The test now asserts exactly one additional silk ice over its starting count, with the water/air/FE/fluid assertions unchanged.

## Build and test validation (2026-11-01)

Local Gradle was fixed for this environment (IPv4-only Java loopback, current egress CA in the JDK trust store, fresh daemon, ForgeGradle certificate check disabled for the proxy-replaced certificates) and then: `compileJava` BUILD SUCCESSFUL in 10m 53s, `test` BUILD SUCCESSFUL in 2m 35s with 196 JUnit tests and no failures, `runGameTestServer` BUILD SUCCESSFUL in 5m 7s with all 327 required GameTests passing (325 baseline plus the two new Fumo tests; the first attempt failed only on five missing Minecraft asset downloads and was retried), and `jar` with `reobfJar` BUILD SUCCESSFUL in 24s producing `[Forge] jdte-0.5.8-forge-1.20.1.jar` (3.3M, verified to contain both new Fumo blockstates, models, textures and loot tables). The GameTest world was fresh, so the delta-count ice fix is verified in a fresh world, not against the original reused world. Real graphical model rendering, creative visibility toggle, JEI visibility and independent dedicated-server save/load are still unverified, so Maiqilin and Xingye remain U.
