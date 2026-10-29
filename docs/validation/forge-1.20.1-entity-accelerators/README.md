# Entity Accelerator Forge acceptance

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`; Forge 47.4.10 / JDT Forge 1.1.4 / Java17 / Gradle8.6. Version remains 0.5.8.

## Current acceptance

Both machine tiers: scoped **V**, with 150 JUnit / 217 required GameTests, actual graphical acceptance and both packaged production JVMs passing. Entity Acceleration Upgrade: **V** at the later [wand acceptance checkpoint](../forge-1.20.1-ultimate-time-wand/README.md), including real wand/smithing/timed effects and independent-client/production lifecycle evidence. Earlier checkpoint paragraphs below retain their then-pending status; the final acceptance sections supersede those pending boundaries.

## Core regression checkpoint

**150 JUnit / 217 required GameTests**, complete build/main+probe reobfuscation and client-smoke compilation pass. All prior 146/202 tests remain enabled. [Build/GameTest output](core-build-summary.txt), [JUnit suite totals](junit-summary.json). Four pure math tests and fifteen new real Forge tests cover both tiers' requested work/costs and fractional debt, actual creature age advancement with AI/travel suppressed, source 32/64 bounds, 128/1024 Ultimate multipliers/ten ticker executions, exact empty/insufficient/short-debit behavior, filter cache/invalid targets, FE/Time Fluid ports/illegal water/capacity protection, save/load, conversion/Forge protection rollback and safe filled crafting prerequisites.

Actual entity removal, nested callback rejection, propagated tick exception/finally cleanup, real Forge Nether dimension transfer and off-thread invocation are covered. Overlapping machines retain source stacking: each synchronous batch pays separately; a global once-per-entity cap was not invented. No client entity receives server sub-ticks. Runtime GUI/production acceptance is still pending at this checkpoint, so both machines remain U.

Source defaults: Advanced 200,000 FE / 10,000 mB / four cards / default4x / adjustable1–64 / Overclock128 /32 targets. Extended500,000 FE / same10,000mB / eight cards / adjustable1–512 / Overclock1024 /64 targets. Per-batch extra work = (effective multiplier−1) × selected targets. FE is that work × native Time Wand FE cost. Fluid is work × native Time Wand fluid cost × configured global factor × tier2/5 ÷600. Fractional debt persists. Creative bypasses payment. Ultimate uses the source ten machine executions and overclock multiplier; standard Overclock uses that multiplier without generic extra machine executions. Requested batches reserve full costs before entity ticks, matching source; removal/dimension change stops the old reference without refunding unused reserved work.

Deliberate Forge safety fixes include per-entity synchronous AI suppression instead of a global flag for unrelated callbacks, recursion/ownership/thread/unload/stop guards, atomic resource payment rollback, capacity-safe card replacement/copy and preserving crafting preflight. Native ShapedRecipe inheritance retains JEI layout support. Source machine configs, recipes and textures are retained. Pinned Extended Upgrade lacks an entity-accelerator mapping; Forge provides an additional tested transactional conversion for this new family. Of the twenty pinned mappings, nineteen are present; Experience Holder remains absent, so Extended Upgrade stays P despite twenty total Forge routes.

## Historical partial dependency at the machine checkpoint

The Entity Acceleration Upgrade is a real source recipe ingredient used by the new machines, with one-time inventory attachment action stored in Forge NBT. **Ultimate Time Wand is still absent.** The source wand smithing recipe, timed target acceleration attachment and wand integration are not implemented/verified. The card stays P; this is not a claim of full wand functionality. No placeholder wand is registered.

## Failure chronology

Initial compile attempts corrected native Forge config names and typed screen accessors. The first GameTest structure name did not exist; reused mineral_empty. A simulated unload fixture left a still-placed machine's capabilities invalid for later world ticking; revive/load it before returning. A direct protected work entry must not reapply the native operation-counter gate; it independently requires nonempty resources. Prior failed-run creatures polluted a target-bound fixture; discard only prior local fixture creatures before creating the exact herd, retaining the strict32/64 requirement. All assertions and existing tests are retained; failed attempts are not counted as passes. Original logs: /tmp/jdte-entity-tests1.log through tests11.log and tests-final.log.

## Historical remaining boundary at the machine checkpoint

Graphical and packaged production validation follow separately. Long-running multiplayer, arbitrary mod creatures/data packs and external JDTE-AE whole-grid integration are unverified. Source wand integration remains partial regardless of machine acceptance.


## Real graphical acceptance

The complete graphical client run exits 0. [Stages/success](client/run-summary.txt), [Advanced GUI](client/jdte-entity-advanced.png), [Extended GUI](client/jdte-entity-extended.png), [JEI crafting layout](client/jdte-entity-jei.png), [Jade](client/jdte-entity-jade.png) and [natural production](client/jdte-entity-real-production.png) are retained. Both menus synchronize high-bit FE/Time Fluid, source multipliers and four/eight card slots. An out-of-range multiplier packet is clamped on the actual server and the widget tracks server values. The real JEI runtime lists both crafting recipes and displays the actual shaped layout. Jade receives real server multiplier/cost packets and has both new config translations (including the Bio disconnect fix). Actual natural creature acceleration spends exact batch FE and synchronizes resulting buffers; trying to sub-tick the corresponding client entity returns zero.

Dedicated packaged acceptance is still pending at this checkpoint. First probe sampled at a fixed startup tick before observed work; wait for the target actually ticking and establish an isolated accounting baseline, preserving the strict FE checks. The strengthened probe also reconciles natural entity.tickCount plus paid extra ticks against actual age, and whole mB plus fractional debt against exact requested work. The second attempt passed that production accounting but failed exact packing equality because the initial fixture omitted the placement-owner UUID. Seed the actual actor owner before taking the snapshot; retain full equality and use actual survival BlockItem placement/one-item consumption instead of only a placement callback. Neither failed attempt is recorded as passed.


## Packaged production acceptance completed

[Two real Forge production JVMs](production/run-summary.txt), full logs, markers and expected compressed NBT are retained. This minimal runtime includes JDT/GuideME/JDTE and the separately reobfuscated probe; **AE2, Curios, JEI and Jade are absent**. Once target world ticking starts, the probe clears prior local fixture cows and establishes an isolated accounting baseline. Natural world ticks advance real creature age by exactly natural `tickCount` plus paid extra ticks. FE is an exact multiple of source requested batch cost, and whole fluid spent plus fractional debt equals the exact requested Time Fluid cost for each tier.

An actual FakePlayer lifecycle actor with the seeded legitimate owner UUID fills its inventory, wrench-packs the machine into exactly one drop, and uses the actual survival BlockItem placement path, consuming that one packed item. Complete machine NBT remains equal. Real source chunk unload makes retained FE/fluid interfaces inert; reload preserves exact machine data and real cow identities. A second production JVM restores all FE/fluid/filter/cards/multiplier/fractional debt, then resumes naturally paid work; no sub-tick scope leaks. FakePlayer lifecycle actions are distinguished from the separate real graphical client GUI/packet acceptance.

The two rows now receive scoped V acceptance from combined complete regression, real graphical and packaged lifecycle evidence. Independent two-client operation specifically for Entity Accelerators, prolonged servers, arbitrary mod creatures/data packs and the external JDTE-AE backend remain unverified. The wand upgrade remains P; neither machine acceptance nor a successful build implies complete wand integration.

## Later wand integration acceptance

The previous paragraphs preserve the machine checkpoint's then-missing dependency. The Ultimate Time Wand and Entity Acceleration Upgrade are now implemented and scoped V with167/238 regression, real two-client interactions, JEI and optional-present/absent dedicated restart evidence. See [current wand acceptance](../forge-1.20.1-ultimate-time-wand/README.md). Independent two-client **machine menus**, prolonged operation and arbitrary mod creatures remain separate unverified boundaries.
