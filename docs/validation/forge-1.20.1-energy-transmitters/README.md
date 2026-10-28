# Energy transmitter recovery checkpoint

Recovered on 2026-10-28 from the interrupted workspace based on `fb64caa`.
The initial recovery was a WIP checkpoint; it was not a full parity or runtime acceptance claim.

## Current validation status

- **131 JUnit / 154 required GameTests passed**, full build and both reobfuscated JARs passed.
- Both production JVMs exited **0** with AE2, Applied Flux, Curios, JEI, Jade and Draconic absent. The opt-in probe passed full-inventory wrench transport, actual unload/reload, saved shutdown/restart and live FE access after restart for both registered tiers.
- Both items remain **U**. Pending acceptance includes graphical screens/Jade presentation, online bound-player/Curios charging, Draconic injectors, complex native peer-network accounting and installed ME-grid recovery across production JVMs. Prolonged multiplayer/global JDTE-AE backend/reactor operation remains unverified.

## Initial recovery (historical)

- Both transmitter implementations, registration, menus, client screens, networking, configuration, resources and core regression tests survived.
- The recovered full Gradle log records 151 required GameTests passing and a successful test/build/reobfuscation run. Retained JUnit XML reports 131 tests without failures. These are prior-run evidence, not a newly repeated run.
- Complete tracked binary diff, all untracked files, test XML, logs and SHA-256 checksums were backed up outside the repository at `/workspace/jdte-recovery-20261028` before staging. The Git checkpoint is the durable source recovery copy.
- At this initial WIP checkpoint, lifecycle, optional-integration, graphical checks, guides, specialized Jade and audit reconciliation were pending. Later sections record the follow-up work.
- Version remains 0.5.8. Existing acceptance classifications have not been promoted by this recovery checkpoint.

## Recovery follow-up regression

The new complete run (`build runGameTestServer`) passed **131 JUnit tests, zero failures/errors/skips; all 154 required GameTests; full build and both main/probe JAR reobfuscation**. The retained compressed log and aggregated JUnit XML are in this directory. All previous tests remain enabled.

New tests cover explicit unload and fresh deserialization, actual 50,000 FE delivery from a real Applied Flux disk on the same powered/channelled AE grid, and a deliberately failing refund: a receiver takes 40 FE of a 1,000 FE batch and removes the disk, leaving 960 FE owned by the transmitter. After snapshot, node teardown must not refund that saved credit. A fresh loaded BE reconnects and refunds exactly 960 once, conserving the original 1,000. This is an in-process network/NBT/teardown test, not a claim about an installed ME grid across two production JVMs.

The failed attempts file retains the original failures. Corrections preserve the assertions: a fresh BE replaces invalidated Forge attachments; native target discovery precedes network port reads; missing owned FE persistence was fixed in the Extended implementation; the AE fixture now bridges to the existing cell because the wireless access point exposes only its rear connection. A same-grid identity assertion prevents an independently powered empty grid from passing. Temporary extraction diagnostics have been removed.

At the regression checkpoint, production lifecycle and graphical/player/Curios/Draconic acceptance were pending. Production follow-up is recorded below; both transmitter rows stay U.

## Production lifecycle evidence

`JAVA_HOME=/workspace/.jdks/jdk17 JDTE_LIFECYCLE_SERVER=/tmp/jdte-optional-none JDTE_SMOKE_PREBUILT=1 bash scripts/run_transmitter_lifecycle_probe.sh` launches two independent production Forge JVMs using the actual reobfuscated main/probe JARs. The server is opt-in, marked disposable, bound to loopback and restricted to `lifecycle-world`. The probe is excluded from the production mod JAR.

Both tiers use non-default area bounds/offset/render setting, active native/Forge filter interfaces with allowlist and NBT comparison, a charged energy item, overload in slot 0 and capacity in slot 7. Advanced starts with 1,900,000,000 owned FE, a synthetic 5,000,000,000 FE long reserve, particles enabled and an offline UUID/name. Extended starts with 30,000 owned FE and particles disabled. Complete saved machine NBT is compared except identity/position and placement-owner metadata.

The seed JVM waits for real server ticks, fills an ordinary server player's inventory, invokes the actual wrench helper and requires exactly one packed drop per machine. Replacing through `setPlacedBy` restores the full saved state. Retained old ports reject FE access. The server saves, removes force tickets, observes both chunks truly absent, verifies held FE/inventory endpoints reject operations, reloads both chunks and compares state exactly. It then shuts down normally.

The second JVM loads the on-disk world, compares the expected NBT, verifies paused state stability, spends exactly 123 FE through each restored real capability and confirms the long reserve remains intact. The native tier rebuilds its transmitter network discovery before network-wide FE queries. This does not prove arbitrary native peer-network accounting or an installed ME network across a JVM restart. The 5B reserve in this probe is intentionally synthetic; the separate GameTest uses real ME-extracted credit.

Evidence files: `production-summary.txt`, compressed seed/verify logs, success markers, expected binary NBT and `artifact-sha256.json`. The final strengthened probe compiles and reobfuscates separately; the production main JAR is the same one used by the complete successful regression. Ordinary upstream tag/auth/version-check warnings are retained in the full logs; both startups, probes and orderly shutdowns completed.

## Continuation

Keep version 0.5.8 and retain both recovery and development branch checkpoints. Start with the unverified player/Curios and complex native peer-network FE paths before considering V acceptance. The graphical transmitter UI and Jade status have implementation/build coverage only in this round. Do not rerun unrelated prolonged multiplayer or reactor tests just to change counts.

## Final packaging check

A final `test build` after the guide additions passed in 14 seconds. The produced JAR and the production-tested JAR were compared entry by entry: only the two Advanced Energy Transmitter Markdown guide pages differ; all runtime classes and all other resources are byte-identical. `artifact-sha256.json` records the production-tested artifacts; `final-artifact-equivalence.json` records final artifact hashes and exact differences. Expensive Minecraft runs were not repeated for these documentation-only resource changes.
