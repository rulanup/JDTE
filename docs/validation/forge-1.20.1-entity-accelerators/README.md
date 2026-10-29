# Entity Accelerator Forge acceptance

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`; Forge 47.4.10 / JDT Forge 1.1.4 / Java17 / Gradle8.6. Version remains 0.5.8.

## Core regression checkpoint

**150 JUnit / 217 required GameTests**, complete build/main+probe reobfuscation and client-smoke compilation pass. All prior 146/202 tests remain enabled. [Build/GameTest output](core-build-summary.txt), [JUnit suite totals](junit-summary.json). Four pure math tests and fifteen new real Forge tests cover both tiers' requested work/costs and fractional debt, actual creature age advancement with AI/travel suppressed, source 32/64 bounds, 128/1024 Ultimate multipliers/ten ticker executions, exact empty/insufficient/short-debit behavior, filter cache/invalid targets, FE/Time Fluid ports/illegal water/capacity protection, save/load, conversion/Forge protection rollback and safe filled crafting prerequisites.

Actual entity removal, nested callback rejection, propagated tick exception/finally cleanup, real Forge Nether dimension transfer and off-thread invocation are covered. Overlapping machines retain source stacking: each synchronous batch pays separately; a global once-per-entity cap was not invented. No client entity receives server sub-ticks. Runtime GUI/production acceptance is still pending at this checkpoint, so both machines remain U.

Source defaults: Advanced 200,000 FE / 10,000 mB / four cards / default4x / adjustable1–64 / Overclock128 /32 targets. Extended500,000 FE / same10,000mB / eight cards / adjustable1–512 / Overclock1024 /64 targets. Per-batch extra work = (effective multiplier−1) × selected targets. FE is that work × native Time Wand FE cost. Fluid is work × native Time Wand fluid cost × configured global factor × tier2/5 ÷600. Fractional debt persists. Creative bypasses payment. Ultimate uses the source ten machine executions and overclock multiplier; standard Overclock uses that multiplier without generic extra machine executions. Requested batches reserve full costs before entity ticks, matching source; removal/dimension change stops the old reference without refunding unused reserved work.

Deliberate Forge safety fixes include per-entity synchronous AI suppression instead of a global flag for unrelated callbacks, recursion/ownership/thread/unload/stop guards, atomic resource payment rollback, capacity-safe card replacement/copy and preserving crafting preflight. Native ShapedRecipe inheritance retains JEI layout support. Source machine configs, recipes and textures are retained. Pinned Extended Upgrade lacks an entity-accelerator mapping; Forge provides an additional tested transactional conversion for this new family. Of the twenty pinned mappings, nineteen are present; Experience Holder remains absent, so Extended Upgrade stays P despite twenty total Forge routes.

## Known partial dependency

The Entity Acceleration Upgrade is a real source recipe ingredient used by the new machines, with one-time inventory attachment action stored in Forge NBT. **Ultimate Time Wand is still absent.** The source wand smithing recipe, timed target acceleration attachment and wand integration are not implemented/verified. The card stays P; this is not a claim of full wand functionality. No placeholder wand is registered.

## Failure chronology

Initial compile attempts corrected native Forge config names and typed screen accessors. The first GameTest structure name did not exist; reused mineral_empty. A simulated unload fixture left a still-placed machine's capabilities invalid for later world ticking; revive/load it before returning. A direct protected work entry must not reapply the native operation-counter gate; it independently requires nonempty resources. Prior failed-run creatures polluted a target-bound fixture; discard only prior local fixture creatures before creating the exact herd, retaining the strict32/64 requirement. All assertions and existing tests are retained; failed attempts are not counted as passes. Original logs: /tmp/jdte-entity-tests1.log through tests11.log and tests-final.log.

## Remaining boundary

Graphical and packaged production validation follow separately. Long-running multiplayer, arbitrary mod creatures/data packs and external JDTE-AE whole-grid integration are unverified. Source wand integration remains partial regardless of machine acceptance.
