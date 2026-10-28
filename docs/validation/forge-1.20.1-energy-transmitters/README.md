# Energy transmitter recovery checkpoint

Recovered on 2026-10-28 from the interrupted workspace based on `fb64caa`.
This is a WIP checkpoint, not a full parity or runtime acceptance claim.

- Both transmitter implementations, registration, menus, client screens, networking, configuration, resources and core regression tests survived.
- The recovered full Gradle log records 151 required GameTests passing and a successful test/build/reobfuscation run. Retained JUnit XML reports 131 tests without failures. These are prior-run evidence, not a newly repeated run.
- Complete tracked binary diff, all untracked files, test XML, logs and SHA-256 checksums were backed up outside the repository at `/workspace/jdte-recovery-20261028` before staging. The Git checkpoint is the durable source recovery copy.
- Still pending: transmitter-specific real chunk unload, wrench transport, fresh production-server restart, installed optional integrations, graphical UI checks, guides, specialized Jade integration and audit reconciliation.
- Version remains 0.5.8. Existing acceptance classifications have not been promoted by this recovery checkpoint.

## Recovery follow-up regression

The new complete run (`build runGameTestServer`) passed **131 JUnit tests, zero failures/errors/skips; all 154 required GameTests; full build and both main/probe JAR reobfuscation**. The retained compressed log and aggregated JUnit XML are in this directory. All previous tests remain enabled.

New tests cover explicit unload and fresh deserialization, actual 50,000 FE delivery from a real Applied Flux disk on the same powered/channelled AE grid, and a deliberately failing refund: a receiver takes 40 FE of a 1,000 FE batch and removes the disk, leaving 960 FE owned by the transmitter. After snapshot, node teardown must not refund that saved credit. A fresh loaded BE reconnects and refunds exactly 960 once, conserving the original 1,000. This is an in-process network/NBT/teardown test, not a claim about an installed ME grid across two production JVMs.

The failed attempts file retains the original failures. Corrections preserve the assertions: a fresh BE replaces invalidated Forge attachments; native target discovery precedes network port reads; missing owned FE persistence was fixed in the Extended implementation; the AE fixture now bridges to the existing cell because the wireless access point exposes only its rear connection. A same-grid identity assertion prevents an independently powered empty grid from passing. Temporary extraction diagnostics have been removed.

Production lifecycle probes and graphical/player/Curios/Draconic acceptance are still pending at this checkpoint. Both transmitter rows stay U.
