# Large Potion Canister acceptance

Pinned source `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`; Forge47.4.10/JDT1.1.4/Java17/Gradle8.6. Version remains0.5.8. Main implementation checkpoints: `f1f0fad`, `0da09ae`, final gameplay guard `3ff5008`. Historical interim U/failures remain in the chronological port log. Current decision: **scoped V** from the following behavioral evidence, not registration/build alone.

## Source contract and Forge adaptation

- 4000mB; four identical PotionItem inputs produce1000mB and four glass bottles. Partial inputs, mismatched composition and a full tank remain recoverable in the persistent slot. No energy or Forge fluid capability is invented. Native JDT bow Potion Arrow/Splash/Lingering costs25/25/50mB per enabled usable ability per canister, including the source water/no-effect rule.
- Neo components map to native Forge `PotionAmt`/`StoredPotionId`/`PotionSlot` and `JDTEPotionContents`. Canonical base potion, every custom-effect flag/duration/amplifier and custom color survive full item saves. Native static amount/base/color reads and bow consumers are adapted only for the large item; small native canisters keep1000mB behavior.
- Identity-bound handheld and dedicated Curios P-key menus use one canonical server item handler. Server ownership/active-menu checks reject detached or transferred items; client selected-slot prediction is also guarded. Empty C2S opening payload resolves the actual server Curios slot. Native source GUI/fullness models/texture, translations, creative tab, preserving recipe, JEI and English guide are wired. Source has no dedicated Jade item provider; the normal tooltip works in the tested Jade-present client profile.
- Deliberate conservation guards: slot max4 prevents oversized source batches returning too few bottles; malformed/empty composition is rejected without spending inputs; arithmetic is bounded; matching effects normalize equivalent optional encodings. Forge additionally exposes the same persistent ITEM_HANDLER used by the menu for automation. Simulated operations and caller stacks remain unchanged. Recipe consumes four occupied ingot slots and preserves complete input item NBT. No new passive effects, FE use or fluids were added.

## Complete automated regression

`delivery-regression.log.gz`, `delivery-junit-summary.json`, `junit-xml.zip`: **176 JUnit,284 required GameTests, zero failures/errors/skips**; all original173/270 tests and original24 mineral tests retained. Full build, main/probe JAR reobfuscation and client harness compilation pass. New three pure tests and14 real Forge tests exercise batches/bottles, full/invalid/mismatching inputs, full custom composition/save roundtrip, shared views/simulation/caller isolation, native static APIs, real native bow method and release, disabled/insufficient/water behavior, stale/physical item handoff, quick move/full inventory, preserving recipe/network, malformed data, off-thread access and semantic defaults. No old assertion was removed or weakened.

## Actual graphical client

`final-client-launch.log.gz`, `final-client-runtime.log.gz`, `client-success.txt` and six `jdte-potion-*.png` screenshots: a real Forge client with software rendering, native menus and actual C2S clicks/draw/release. Verified four individually unstackable potions,1000mB/four bottles,4000mB/16 bottles, full pending input recovery, immediate no-ghost self-item click, dedicated Curios/P-key, native full base/color reads, JEI potion and existing fuel preserving recipes. Actual native bow draw/release admits a custom-effect arrow, spends100mB/one arrow and retains3900mB. Controlled server materials/Curios equip are fixture setup, not claimed player actions. Screenshots visually inspected for layout and synchronized partial/full states.

## Packaged production lifecycle, optional dependencies absent

`final-production-*.log.gz`, both production success files and `production-expected.nbt`: two separate production server JVMs load the reobfuscated main/probe JARs, JDT and GuideME only. AE2/Curios/JEI/Jade are absent. Actual native bow method calls use an explicitly identified FakePlayer; they are **not** reported as independent client input. The actual arrow admission/effects, ammo and100mB are asserted.

The first JVM stores a4000mB canister with pending3, a bow-embedded3900mB canister with pending2 and32 returned bottles in a real chest. Inventory relocation, actual ChunkEvent.Unload, saved reload into a different chest BE and exact whole NBT are required. Saved shutdown followed by a fresh JVM restores exact NBT before work. Ten real native method shots spend exactly1000mB/ten ammo while preserving pending3; adding one actual matching potion resumes1000mB/four bottles exactly once (36 bottles total).

## Two independent clients and separate production restart

`final-remote-*.log.gz`, `remote-*-success.txt`, `remote-expected.nbt`, two remote screenshots: **two actual client JVMs and two successive independent dedicated-server JVMs**, with AE2/Curios/JEI/Jade installed. Actual packets concurrently fill separate portable menus, recover outputs repeatedly, reject the old container ID after a controlled physical server handoff and complete the new owner's fourth potion exactly once. Handoff/material setup is instrumented, not claimed human drag/drop.

A client really disconnects while holding one potion on its cursor. Vanilla drops that cursor into the world; the ledger includes inventory, cursor and real ItemEntity locations. The observer is moved away and the returning owner is positioned at the actual drop for **normal world-tick pickup**, with no synthetic inventory grant. Both clients reconnect and require empty cursor, pending1, loose1, two canisters,3000mB and twelve bottles. A saved shutdown/new server JVM compares exact complete inventories before work; both clients reconnect again and observe the correct full effects/color/pending menu. All JVMs exit0.

The old coordinator's generic seed label says "chunk unload" in the retained remote launch log. That remote scenario itself tests reconnect/saved shutdown, not a separate asserted unload. Actual fresh-BE unload evidence is the independent minimal production pair above; the potion coordinator label is now corrected.

## Failures preserved and repaired

- First graphical attempt: driver did not hold vanilla use key, causing automatic release. Repaired driver holds then releases; actual arrow/cost assertion passes.
- First production attempt: ServerStarted visibility query preceded entity-section visibility. Replaced with actual uncanceled join, admitted entity/effects and unchanged exact debit/ammo requirements; final diagnostics explicitly show admitted1/visible0/added=true when appropriate.
- Remote attempt1 omitted world drops from the disconnect ledger. Attempt2 reached reconnect but timed out before pickup/menu synchronization. Both full failure/control logs retained. Final driver waits for held inventory sync and actual pickup; no resource assertion relaxed.
- Actual gameplay fixes include native full-color/base getters, malformed legacy-ID safety, capacity clamp avoidance, exact bottle batches and client held-slot prediction protection.

## Reproduce and boundaries

Reuse the configured Java17/Gradle/Forge runtime. Run `gradlew test runGameTestServer build serverSmokeJar compileClientSmokeJava`, `scripts/run_potion_client_smoke.sh`, `scripts/run_potion_lifecycle_probe.sh`, and `JDTE_REMOTE_SCENARIO=potion scripts/run_mineral_multiplayer_smoke.sh` with the existing **marked disposable** fixtures and configured JAVA_HOME/GRADLE_USER_HOME. The remote fixture uses loopback25579 and captured `/tmp/jdte-remote-launch.json`; production25589. Existing scripts retain fixture safeguards. Test-only client/server harnesses are excluded from the main JAR. `artifact-sha256.txt` proves both packaged profiles used the same final build/probe bytes; `runtime-profiles.json` lists installed files.

Unverified: cross-dimensional item movement, prolonged public-server load, abrupt crash recovery without a completed save, arbitrary potion-effect/event mods and recipe packs, malicious out-of-protocol clients beyond the tested stale packet, and every Curios/optional-mod combination. No full-mod parity, real human play session or arbitrary third-party compatibility claim is made. No unrelated absent subsystem was implemented.
