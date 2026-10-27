# Production-chain follow-up validation

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Starting commit: `a36c6c5`. Version remains `0.5.8-forge-1.20.1`.

## Real AE crafting jobs

The full configured development runtime passed 77 JUnit tests and 87 required Forge GameTests. `ae-jobs.txt` contains selected actual output, including executable AE plans and the final suite result. Existing tests were retained.

New tests use a real powered AE network, crafting storage, a Pattern Provider, a processing pattern, asynchronous plan calculation, CPU submission, an installed read/output pair on a working Mineral Extractor and linked fluid extraction equipment. Both normal completion and cancellation pause the machine again after the source cache expires. Tests deliberately wait for actual completion rather than treating a busy flag as success.

The source read upgrade is a network-wide activity gate; it does not match the mineral producer to a processing recipe or cap its output to the job request. The fixture's cobblestone trigger enters a chest and ore is independently produced by the real mineral machine. Extra ore remains in ME while the CPU/provider schedules the outstanding work.

The check runs in Forge's dedicated GameTest server. It is not a new independent-client GUI test, an active-job persistence test or proof of all supported machine families. Those scenarios remain unverified in this checkpoint.
