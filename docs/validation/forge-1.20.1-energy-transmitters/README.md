# Energy transmitter recovery checkpoint

Recovered on 2026-10-28 from the interrupted workspace based on `fb64caa`.
This is a WIP checkpoint, not a full parity or runtime acceptance claim.

- Both transmitter implementations, registration, menus, client screens, networking, configuration, resources and core regression tests survived.
- The recovered full Gradle log records 151 required GameTests passing and a successful test/build/reobfuscation run. Retained JUnit XML reports 131 tests without failures. These are prior-run evidence, not a newly repeated run.
- Complete tracked binary diff, all untracked files, test XML, logs and SHA-256 checksums were backed up outside the repository at `/workspace/jdte-recovery-20261028` before staging. The Git checkpoint is the durable source recovery copy.
- Still pending: transmitter-specific real chunk unload, wrench transport, fresh production-server restart, installed optional integrations, graphical UI checks, guides, specialized Jade integration and audit reconciliation.
- Version remains 0.5.8. Existing acceptance classifications have not been promoted by this recovery checkpoint.
