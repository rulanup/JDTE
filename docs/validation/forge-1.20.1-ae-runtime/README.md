# AE acceleration and production runtime follow-up

Starting commit: `5498124`. Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Version remains `0.5.8-forge-1.20.1`.

## Implementation and server suite

AE Acceleration is installed in all three existing Time Accelerators, using their current fluid-cost and bounded scheduler settings. Native AE service discovery requires each contributor's own card; pending AE work loses removed-card contributions. Optional whole-grid backends use the source registration/frame contract, with active/inactive routing and no duplicate native fallback. No actual JDTE-AE backend mod is claimed as tested.

The current full build passed **95 JUnit tests and 112 required GameTests**, preserving all earlier 81/108. New tests include ten original backend-registration tests, four frame-order/exception-cleanup unit tests and four real AE2 integration GameTests. The latter use a powered real Inscriber: AE's native one-tick request advances two processing units, so twelve requests advance 24 rather than twelve. Initial fixture expectations were corrected accordingly. The first attempt to unit-test the registry-backed scheduler directly failed during Minecraft tag bootstrap; the exact frame logic was isolated into a common helper and all four tests now run successfully without world bootstrap. No exception assertions were removed.

`suite.txt` retains selected final output. Production and separately packaged probe reobfuscation passed. Graphical harnesses compile and stay outside the production JAR.

## Active real crafting job, save and fresh JVM

`active-job-restart-first.txt` identifies the first production JAR and actual two-JVM results. A real Pattern Provider feeds the real Extended Fluid Mixer while a real AE CPU waits for 500 mB Time Fluid. Before the first batch completes, redstone pauses the mixer at partial progress while the actual CPU remains busy. The server saves, verifies the saved live job, then shuts down normally.

A second JVM compares exact mixer fields before grid boot, waits for the actual nonempty busy CPU to return, verifies the restored read binding and resumes native machine work. Advanced AE Output returns exactly 500 mB to ME, catalysts/water are consumed exactly once, total FE cost is exactly 10,000, and the completed job pauses the machine again. Both JVMs exit 0. These are real persisted CPU jobs, rather than a recreated plan or fake busy snapshot.

## Earlier compiled-fixture checkpoint

Two independent mixer clients and an actual Mekanism Extras runtime probe are compiled but have not run yet. The publicly available Forge 1.20.1 Extras 1.5.0 recipe set contains fluorinated naquadah uranium fuel; no silicon fuel recipe was found in that JAR's datapack resources. Final runtime evidence will distinguish actual tested recipes from unavailable version-specific content.

## Verified Mekanism Extras liquid fuel

Mekanism Extras **1.5.0 for Forge 1.20.1** was downloaded through its public Modrinth release metadata; the published SHA-512 was verified. `mekanism-artifact.json` records the exact release URL/hashes, dependency versions and tested JDTE JAR hash. No third-party JAR is committed or bundled.

The actual installed recipe `mekanism_extras:chemical_infusing/fluorinated_naquadah_uranium_fuel` mixes liquid `mekanism:uranium_hexafluoride` with `mekanism_extras:naquadah_hexafluoride` into real `mekanism_extras:fluorinated_naquadah_uranium_fuel` at 1:1:2. The packaged Chinese localization calls the output **液态含氟硅岩铀燃料**. Naquadah is 硅岩 in this addon; an English search for elemental `silicon` alone does not identify this chain. The earlier metadata-only observation above must not be read as absence of the naquadah fuel recipe.

`mekanism-fuel.txt` records actual reobfuscated production-JAR execution with Mekanism 10.4.16.80 and Extras 1.5.0 installed. Both mixer tiers run 100 native recipe work calls with deliberately reversed inputs. Ordinary Mixing completes eight batches; Ultimate Mixing Parallel completes 25. Fluid quantities and 5,000 FE per batch are exact. The output is the real registered liquid rather than empty/air, and the addon's actual downstream RotaryRecipe accepts it and produces the correct nonempty fuel gas. No complete reactor cycle or operating downstream rotary machine is claimed; downstream recipe acceptance is the checked boundary.

This fixture also runs with AE2/Curios/JEI/Jade absent. Extras itself logs missing optional-generator scan data and unrelated loot/tag entries; selected warnings are retained in `mekanism-third-party-warnings.txt`. They were not suppressed or patched, and successful mixer fuel verification does not claim every Extras machine or optional combination is valid.

Reproduce with an existing marked disposable Forge installation, the verified Extras JAR and matching Mekanism JAR:

```bash
export JDTE_LIFECYCLE_SERVER=/path/to/disposable/forge-server
export JDTE_MEKE_JAR=/path/to/mekanism_extras-1.20.1-1.5.0.jar
export JDTE_MEKANISM_JAR=/path/to/Mekanism-1.20.1-10.4.16.80.jar
bash scripts/run_mekanism_fuel_probe.sh
```
