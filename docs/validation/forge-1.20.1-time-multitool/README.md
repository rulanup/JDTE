# Time Multitool acceptance

Reference ccf46472, including inherited Neo JDT1.5.7 behavior; Forge47.4.10/JDT Forge1.1.4/Java17/Gradle8.6/version0.5.8. Historical core classification was **U**; the final acceptance below supersedes the pending statements in earlier checkpoints.

## Current acceptance — 2026-10-31

**Scoped V**. Implementation/runtime checkpoints `a73f98b`, `cd01c50`, `ef4abe7` and **`3852fe8`** are pushed. Final **196 JUnit /324 required GameTests**, full build/main+probe reobfuscation, real graphical replay and two matching-JAR production JVMs pass. All previous180/302 and original24 mineral tests remain;16 pure and22 Forge tests were added. [Final unit summary](final-junit-summary.json), `final-regression.log.gz`, `client-final.log.gz`, `production-seed.log.gz` and `production-verify.log.gz` retain evidence. Historical attempts and failures below are preserved.

### Implemented source behavior

- Powered Eclipse Alloy Paxel:500,000FE/1,000,000mB Time Fluid, six1/2/4/16/256/1024x modes; selected multiplier mB per actually removed block except1x=0. A whole proposed batch must afford the fluid or falls back1x; FE exhaustion stops further removals. Native costs/enchantment discounts, tool actions, supported targets and10 native abilities are preserved. Air sneak-use cycles; block sneak-use opens existing JDT settings. Mode/FE/fluid/native settings/name/enchantments/unknown NBT persist. No new scheduler/menu/network protocol or wand/entity upgrade is invented.
- Forge lacks Neo's breakBlocksNew/BasePaxel/BlockDropsEvent layer. Scoped game-mode and native-breaker adapters own one real event/removal/receipt/resource transaction, with loaded-only source target discovery, exact refund/recursive guards and scoped native playerDestroy/block-pop capture. Native harvest callbacks, silk/statistics/exhaustion, doTileDrops/snapshot rules and exactly one Forge XP settlement are preserved. Passive smelt→real bound-inventory processing uses native fees.
- Source construction is an ordinary fresh shapeless result. Ten native exact-base smithing variants make the inherited abilities installable on a Forge subtype, preserving complete item data and rejecting duplicates. This is an explicit usability adaptation, not a fabricated preserving construction or Time Wand upgrade. Forge NBT replaces Neo data components; client hold-delay config is separate from server acceleration settings. Recipes/model/creative tab/en+zh tooltip/English guide are wired; the source has no dedicated Jade item provider.

### Real graphical coverage

Actual rendered Forge client plus its real integrated server uses controller C2S actions for sneak-air1→2→4→16, target-gate/controller stone mining, native tool settings opened by sneak-block and native Hammer GUI7→off3→on3→5→7. Exact resources synchronize, source recipe renders in JEI, all10 native smithing recipes decode on the client. Full player inventory/tool NBT survives actual disconnect, saved old-server shutdown and new integrated-server rejoin. Actual resumed256x removal ends **4850FE/9712mB**, with Hammer/native settings/name/mode retained. Four screenshots are included.

Materials/native recipe assembly are controlled fixture setup. Input events are generated and their cancellation is honored before actual controller packets; this is not a claim of physical human input, GUI smithing or independent-client multiplayer. Native JDT's public JEI smithing extension accepts AbilityRecipe without a JDTE namespace exclusion; only source crafting display/client recipe decoding were directly exercised here.

### Packaged production coverage

Loopback-only disposable Forge47.4.10 server, **only JDT1.1.4/GuideME/JDTE plus the separate probe**; AE2/Curios/JEI/Jade are absent. Both installed main/probe hashes match final reobfuscated build outputs. Main JAR excludes smoke classes/test Curios data. Real game-mode/native machine actions, vanilla ice callback, exactly one diamond/7XP, actual inventory move, observed unload/absent chunk/fresh BE/full NBT, saved stop/new JVM/exact restoration and resumed production pass. FakePlayer method calls are explicitly server fixture actions.

The persistent tool's first9-block16x hammer spends450FE/144mB, native breaker spends50FE/16mB, leaving19500FE/9840mB and10 cobblestone. Restarted mode256 spends450FE/2304mB on9 more blocks, leaving19050FE/7536mB and19 actual cobblestone. A separate controlled1000FE/100mB tool verifies ice and one ore cost100FE/4mB with exactly7XP/one diamond.

### Reproduction and remaining boundary

Use configured Java17/Gradle8.6 and existing dependencies:

```bash
bash gradlew test runGameTestServer build serverSmokeJar compileClientSmokeJava --no-daemon --max-workers=2
bash scripts/run_multitool_client_smoke.sh
JDTE_LIFECYCLE_SERVER=/path/to/disposable-server bash scripts/run_multitool_lifecycle_probe.sh
python scripts/audit_forge_port.py
python scripts/audit_forge_port.py --check
python scripts/validate_docs.py
```

The production script requires the explicit fixture marker, loopback bind, dedicated world name and installed Forge libraries. It preserves prior production fixture worlds; the graphical script only refreshes its explicitly marked disposable world. No existing recovery backup/branch is removed.

**Not verified:** independent two-client competing mining, long-running public servers, cross-dimensional transport of this item, abrupt unsaved crashes, every third-party block override/protection mod/custom recipe pack, visual use of each ability binding and every JEI smithing layout. Existing external JDTE-AE whole-grid, reactor and matrix boundaries are unchanged. V is scoped behavior acceptance, not complete0.6.5.2 parity. Fumo remains3N; [source investigation](../../forge-1.20.1-fumo-investigation.md) is read-only.

## Historical checkpoints and failures

196 JUnit/321 required GameTests, full build/main reobfuscation pass. Previous180/302 and24 mineral tests retained.16 new pure tests preserve six modes/batch policy/tracker/input gate and add overflow coverage;19 Forge tests cover actual game-mode removal/last FE harvest, fallback/modes/save, hammer complete-fluid/partial-FE budgets, cancellation/replacement/held identity/recursion, reservation rollback, speed event, caps/simulation/off-thread, actual tank refill, unloaded/adventure targets, hoe/Neo paxel actions/Forge place rollback, fresh source recipe, all ten actual native smithing recipes, leaf/lawn utility and native breaker/stale owner.

Port differences are explicit: Forge has no Neo breakBlocksNew/BasePaxel layer. A scoped Minecraft game-mode interception owns one native-style removal/event/resource transaction, preserving actual removal receipt/drops/XP. Loaded-only target search retains vein/tree/hammer/sky algorithms; per-call leases refund failure and lock concurrent resource callbacks. Forge native breaker legacy second drop/removal pass is intercepted only for this item. Neo inherited axe/shovel functions are implemented, with native1.20 campfire douse API replacing unavailable SHOVEL_DOUSE. Native exact-base smithing recipes do not recognize a new item subtype; ten explicit native ability recipes make inherited ability support usable, preserving tool NBT. Source construction remains a fresh vanilla shapeless result. No wand scheduler/entity upgrade/placeholder added.

Failures retained: first Forge run's duplicate assertion assumed every nearby drop was the one primary cobblestone. It now requires exact before/after equality of the whole nearby drop list, retaining the exact one-cobblestone/last-FE assertion. Next compile found missing SHOVEL_DOUSE, adapted through actual1.20 CampfireBlock.dowse. No previous test or assertion removed. At the core checkpoint, actual graphical and packaged-server lifecycle were not yet executed.

## Runtime checkpoint after a73f98b

Real rendered Forge client passed actual controller C2S sneak-air modes1→2→4→16, source250ms target-switch event gating, two real stone removals with exactly100FE/32mB, native settings opened through sneak-block C2S, actual JEI source crafting and all10 client-decoded native smithing recipes. Real player disconnect/save/old integrated server stop/new server/rejoin preserves the complete inventory/tool NBT;256x post-rejoin mining finishes4850FE/9712mB. Screenshots and logs are retained. Fixture materials/native recipe assembly are controlled; generated input events are honored before actual controller packets, not physical human input or a claim of GUI smithing. Native GUI button changes are the next small acceptance check.

The independently reobfuscated main/probe JARs passed two actual production Forge JVMs with only JDT/GuideME (AE2/Curios/JEI/Jade absent). Actual game-mode hammer9 blocks costs450FE/144mB; native breaker costs50FE/16mB; real inventory split/move, observed ChunkEvent.Unload, absent chunk and fresh BE preserve exact full chest NBT. New JVM restores19500FE/9840mB/mode16/native upgrade/name/unknown tags, cycles256 and mines9 more for450FE/2304mB, leaving19050FE/7536mB and19 actual cobblestone. Server actions use explicit FakePlayer fixture calls, not real graphical input. Both JVMs save/shut down normally.

The initial production fixture failed by asserting inventory split returns the same Java object. Actual ContainerHelper splitting copies the stack; the assertion now requires one returned item, an empty original slot and exact complete NBT equality. Failure log retained; no game code was changed for this fixture repair.

## Harvest repair regression checkpoint

196 JUnit/**324 required GameTests**, full build/main+probe reobfuscation pass (`harvest-regression-pass.log.gz`). Real playerDestroy and scoped vanilla block-pop capture now retain ice callbacks, silk loot, stats/exhaustion, doTileDrops and native passive ability processing. Forge BreakEvent XP is the single XP settlement. Three added Forge regressions cover exact resource/drop/merged-orb XP conservation and actual smelt→loaded bound-chest output. The first XP assertion failure is retained; final assertion uses complete exact before/after ledgers including merged Count.

Two supplementary native GUI fixtures initially assumed false/default3; actual JDT source defaults true/maximum7, so the test now requires7→off3→on3→5→7 through real screen clicks and C2S sync. Both failed logs are retained. The preceding runtime results describe the earlier JAR; final harvest-JAR runtime replay is pending.

## Final harvest-JAR dedicated acceptance

Final196/324 regression and full build/reobfuscation pass. Native popExperience also requires doTileDrops and no snapshot restoration; the exact XP test now includes disabled-drop ore with an explicit7XP event, preserving previous enabled-drop/FE/fluid assertions.

Both final-JAR production JVMs pass, now including actual native ice callback plus exactly one diamond/seven XP through the scoped harvest capture, alongside the retained19-cobblestone/FE/fluid/unload/save/restart ledger. The first expanded probe queried newly queued entities during the same ServerTickEvent.END callback and observed0/0; verifying them on the following tick passes. Failure log retained; native-tier/actual mining checks remain in the fixture. `production-artifacts.json` matches **both** installed main/probe bytes to final build outputs. Final graphical replay pending at this checkpoint.

## Final replay result

The expanded fixture now clears every possible7x7 native Hammer plane. It passes the original exact one-removal4850FE/9712mB assertion, all native button/C2S checks and saved rejoin/resume on final code. Final regression includes native XP doTileDrops/snapshot guards. Earlier pending statements above describe those historical checkpoints only. Final artifact hash/recipe/smoke-exclusion checks pass; no further game code changed after runtime acceptance.
