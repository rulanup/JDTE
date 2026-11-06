# Forge 1.20.1 graphical validation — 2026-10-27

These screenshots come from the real Forge 47.4.10 client and integrated server using Java 17, JDT Forge 1.1.4, the existing Xorg dummy display and Mesa software rendering. They are captured after packet/UI assertions, rather than mock renders. Mod version stays 0.5.8.

The opt-in harness lives in `src/clientSmoke` and is excluded from production JARs. Run `scripts/run_mineral_client_smoke.sh` with the configured Java/Gradle environment and proxy arguments described in [port tracking](../../forge-1.20.1-port.md).

| Screenshot | Verified path |
|---|---|
| [Normal Mineral Extractor](jdte-mineral-normal.png) | Visible title, compact count, real page acknowledgement, 6.4M count synchronization and legal cursor extraction |
| [Large Mineral Extractor](jdte-mineral-large.png) | Four survey slots, correct visible-slot boundary and paged output synchronization |
| [Mineral Survey](jdte-mineral-survey.png) | Recorded-survey screen opened by the server packet |
| [JEI mineral category](jdte-mineral-jei.png) | Synchronized biome recipes, candidates, paged outputs, catalysts and optional fluid inputs |
| [Jade controller](jdte-mineral-jade.png) | Actual server-supplied mineral status and installed upgrades |
| [Jade large-machine part](jdte-mineral-jade-part.png) | Controller status and upgrades resolved through a structure part |
| [Upgrade Storage](jdte-upgrade-storage.png) | Real menu/held-item NBT synchronization after extracting upgrades; native JDT upgrades remain compressed in storage |

The harness also changes GUI scale and checks that the mineral screen rebuilds at the expected origin. A success marker is written to `runs/client/mineral-smoke-success.txt` only after all stages pass. The final run completed with `JDTE_MINERAL_CLIENT_SMOKE_PASS` and Gradle success.

This is a single integrated-client test. Independent remote clients, optional-mod absence combinations, actual cross-chunk unload/reload, Factory Packer relocation/rotation and ultimate-capacity destruction/load behavior remain outside this validation.
