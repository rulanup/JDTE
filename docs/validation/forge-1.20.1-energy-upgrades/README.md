# Energy upgrade port validation

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Audit baseline: `020109b`. Version stays **0.5.8-forge-1.20.1**.

## Implemented and tested

- Energy Brewing: source default 5000 FE for 20 fuel credits, configurable 0..1,000,000, Creative bypass, normal stage FE retained, no powder consumption while installed, reversible fuel-slot rejection. Fuel credits/progress and the card use existing saved data and canonical Forge capabilities.
- Energy Overload: source `Integer.MAX_VALUE` batch limit for native JDT transmitters, at most one card, no generated energy. Actual native world-capability delivery tests conserve 9000 FE, enforce the ordinary 1000-FE test rate before installation and restore that rate on removal. Advanced/Extended JDTE transmitters are absent and are not validated by this work.
- Both source recipes, textures/models, languages, creative entries, bags, typed copying, applicable-slot limits and shared Jade installed-upgrade collection are wired. A verified upgrade does not make its partially ported host machine fully source-complete.
- Real copier fix: new empty brewing templates encode as `{}`; earlier canonical Forge `air/Count:0` also decodes. Unknown IDs and positive-count air still reject. One bagged brewing card is consumed on paste; FE is not copied.

## Current results

| Check | Result |
| --- | --- |
| `test build runGameTestServer`, Java 17 / Forge 47.4.10 | Passed |
| JUnit | 100 passed, including 5 new transaction tests; all previous 95 retained |
| Forge GameTest | All 123 required tests passed, including 9 new integration tests; all previous 114 retained |
| Production and separate probe JAR reobfuscation | Passed |
| Production two-JVM save/restart probe | Pending at initial code checkpoint; final result will be appended after completion |
| `python scripts/validate_docs.py`, `git diff --check` | Passed before checkpoint |
| Fresh graphical client / new card visuals | Not run |
| New real two-client run | Not run; unaffected prior evidence retained |

The GameTests exercise actual brewer recipes/menus/capabilities and native transmitter transfer. Pure transaction unit tests exercise insufficient/zero/Creative/exact FE and short-commit rollback. Simulation tests alone are not presented as full gameplay proof.

Earlier failed attempts and corrections: the first unit test referenced JDT on a plain unit classpath (moved transaction logic to pure Java); a reused machine fixture retained Creative; a Shift assertion ignored legitimate movement to another allowed slot; the copier failed because of the real Forge empty-stack codec bug. Final results above refer to the corrected complete passing run. No baseline tests were removed or disabled.

## Reproduce

Reuse the configured Java 17 / Gradle 8.6 environment and existing caches, with the cloud proxy flags where required:

```bash
bash gradlew test build runGameTestServer --no-daemon --max-workers=2
python scripts/audit_forge_port.py --check
python scripts/validate_docs.py
```

The separate production probe is opt-in and excluded from the main mod JAR. It requires an **existing disposable** Forge 47.4.10 server marked `.jdte-lifecycle-fixture`, bound to `127.0.0.1` with `level-name=lifecycle-world`. Its two fixture positions are `(2040,90,2040)` and `(2044,90,2040)`. It replaces only those blocks when seeding. It does not install tools or mods, nor run against an unmarked server.

```bash
export JDTE_LIFECYCLE_SERVER=/path/to/disposable/forge-server
bash scripts/run_energy_upgrade_lifecycle_probe.sh
```

After a completed build, set `JDTE_SMOKE_PREBUILT=1` to reuse the current JARs. The script starts separate seed/verify JVMs, requires fresh success markers and exit 0, and leaves full logs/NBT in the fixture. The seed naturally brews to tick 175/400 and stops; the fresh JVM compares restored fields and naturally completes one potion. Its live Mixin limit is checked before shutdown and after loading/removal/reinsertion. This probe does not substitute for a two-client test.

## Unverified / out of scope

New graphical appearance/tooltips/Jade rendering have not been observed in a fresh client. The original source Advanced/Extended transmitters are absent. Advanced Potion Brewer coalesced accelerated processing/Ultimate support, the external installed JDTE-AE whole-grid backend, prolonged multiplayer uptime and complete operating reactor scenarios remain incomplete or unverified as described in the full feature matrix. No new two-client/AE-job/Mekanism runs were necessary for this bounded change.
