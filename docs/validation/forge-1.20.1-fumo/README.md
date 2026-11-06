# Cainiaoyo Fumo validation checkpoint

Source `ccf46472`, target Forge47.4.10/JDT1.1.4, version0.5.8. Classification U pending runtime acceptance.

The Gradle build and main JAR reobfuscation passed;196 JUnit tests passed. The targeted GameTest exercises all horizontal facing shapes and the source self-drop through the real loot table.

The first complete GameTest run failed one existing Time Multitool silk-ice drop-count assertion. Its test world had been reused from earlier runs; that is a possible cause, not yet established. The full log is retained. The entire prior world was preserved at `runs/gameTestServer/world-before-fumo-8dd2a705ad41`, and a fresh-world suite was started without changing any prior assertion. Graphical/JEI and independent dedicated-server behavior remain unverified.

Documentation validation initially treated this hidden decorative block as a machine requiring a guide. The validator now explicitly excludes the decorative block, matching source behavior; existing machine checks are unchanged.

## Fresh-world result

All **325 required GameTests passed** in the fresh world, including the new Cainiaoyo facing/actual self-loot test and the existing Time Multitool test. The first run's isolated failure remains documented; the fresh-world result does not establish its exact cause. The original test world and both complete logs are retained. The configuration label and description now use the pinned source translations. Real graphical model rendering, creative visibility toggle, JEI visibility and independent dedicated-server save/load are still unverified, so the item remains U.

The final build after adding source configuration translations passed, including JAR reobfuscation and the JUnit suite (`build-final.log.gz`). The fresh-world GameTest was run before that translation-only configuration metadata change.
