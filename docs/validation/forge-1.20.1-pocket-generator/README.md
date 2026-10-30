# Large Pocket Generator acceptance

Reference JDTE0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`, including the inherited JDT Neo1.5.7 generator behavior. Target Forge47.4.10/JDT Forge1.1.4/Java17/Gradle8.6. Version remains0.5.8. Core checkpoints `f8e7437`, `fbdac7b`, final implementation **`c30ced4`** are pushed. Historical intermediate U checkpoints/failures remain in the chronological port log. Current classification: **scoped V** for the behavior below.

## Source contract and adaptation

- Configured native capacity×4, default4,000,000FE; default15FE per fuel tick, base burn speed4 and5000FE/t per eligible item. Native charging threshold is configured item rate/10. Smelting fuels, JDT coal/blocks and both fuel canisters resolve original burn time and speed. A coal gives400 whole generator ticks×60FE=24,000FE. The source rounds burn duration down to whole ticks, clips a partially fitting final FE tick, pauses on a full buffer and preloads the next fuel after the last tick while retaining its whole future credit.
- Native-format `Energy`, `FuelInventory`, `counter`, `maxburn`, `fuelmultiplier`, `enabled` and unknown item NBT are preserved. Neo components become one shared persistent Forge fuel slot and dynamic item-owned FE storage. Whole fuels/remainders are staged before commit; too-short or invalid input does not alter multiplier, fuel or FE. A remainder-producing fuel occupies one slot item, preventing loss of stacked buckets.
- Real no-receive FE capability permits extraction and internal generation only; no fluid interface or machine upgrade slots are invented. Native-compatible storage type avoids stale cached FE while reading full stack NBT. Negative requests, overflow, full/self/rejecting targets and simulated operations are guarded. A smaller actual receiver acceptance is refunded; an exception before acceptance is refunded and rethrown, never swallowed.
- Enabled player inventory and dedicated Curios ticks generate once per physical item/server tick and charge inventory **and actual equipped Curios recipients**. Cosmetic entries are excluded. Unlike Forge1.1.4's native parent, the pinned Neo parent charges active inventory tools too; the Large implementation follows Neo here. A weak server/item tick stamp avoids duplicate wearable callbacks without retaining entities; direct native transmitter calls retain their source machine semantics.
- Bound main-hand and equipped menus share the real fuel view with production/automation. Main-hand resolution, shift-use, native toggle, G binding, input/output moves, capacity/rate/progress, exact/suffixed hover values, models/enabled property, texture, English/Chinese names, preserving recipe, creative tab and English guide are wired. G conflicts with default Curios G: the matched equipped-generator request consumes that pending Curios click; unequipped Curios opening still works.
- The preserving upgrade consumes one native Pocket Generator, four ingots in four occupied crafting slots and a Time Fluid Catalyst, retaining complete native item data. JEI sees both IDs over the actual recipe wire. Source has no dedicated Jade item provider; normal item tooltip is used in the tested Jade-present client.

## Native Forge compatibility repairs

The actual Forge1.1.4 internal force-receive path calls EnergyStorage.receiveEnergy, which rejects virtual canReceive=false; the original Pocket Generator generated zero. A narrow PocketGenerator internal adapter now produces FE while public input remains disabled. Its pinned native large-canister multiplier redirect also works. Before native fuel initialization, whole-tick credit and one remainder container are validated: extremely fast fuel cannot consume a unit for zero burn ticks and emit an oversized unpaid tick. Native vanilla coal-block special speed remains separate from Large source rules.

Native EnergyTransmitterBE casts portable item storage to an unrelated machine class before tryBurn. A **Large-item-only** interception calls the actual generator and conserving FE transfer. This acceptance does not claim to repair every unrelated native JDT behavior or every native-machine item combination.

## Complete automated suite

`native-guard-regression.log.gz`, `final-junit-summary.json`, `junit-xml.zip`: **180 JUnit/302 required GameTests**, zero failures/errors/skips. Previous176/284 assertions and original24 mineral core tests remain. Four new numeric tests and18 Forge tests cover real generation/source whole-tick credit, bounds/full/partial/prefetch, sim/caller copies, native-format save, both fuel canisters/remainders/custom coal blocks, too-short fuel, menu identity and shared production/automation, disabled/duplicate wearable ticks, actual Curios recipient, partial-accept/throw-before-mutation refunds, wrong-thread/malformed FE, actual max-int config, native generator/high-speed rejection, actual native transmitter and preserving recipe/network. Full build, main/probe reobfuscation and client harness compilation pass.

## Real graphical client, natural work and actual player recovery

`final-client-launch.log.gz`, `final-client-runtime.log.gz`, `client-success.txt`, `client-expected.nbt` and eight screenshots: a real graphical Forge **development client**, with actual menus/C2S packets and naturally scheduled inventory/Curios production. Materials, initial charging gaps, plane and item equipment moves are explicitly controlled setup, not claimed human actions. A test-only datapack validates a real native pickaxe as an actual Curios recipient.

Verified disabled eight-coal insertion, native enabled toggle packet, real production and inventory/Curios charging, repeated fuel extraction/reinsertion while producing, no self-item client pickup prediction, worn generation/G menu and normal unequipped Curios key. The ledger counts loose/input/carried coal plus remaining burn credit against actual source+inventory+Curios FE; **406 final samples,123 worn** pass. Explicit fixture fill3456789FE checks full-width NBT/menu synchronization, not claimed fuel generation. JEI opens all three preserving portable recipes. Screenshots visually inspected for fuel/FE layout and restored state.

The actual player disconnects, the world/integrated server saves and stops, then the same client rejoins a **new integrated-server instance**. Complete vanilla inventory and equipped generator/recipient item NBT must match the saved expectation before work. G reopens the disabled3456789FE generator. A controlled1000FE receiver gap then naturally refills after enabling, under a new exact credit/FE ledger. This is real player/world recovery, **not two independent clients**.

## Reobfuscated production servers and optional absence

`final-production-*.log.gz`, both production success markers, `production-expected.nbt`: two separate Java17 **packaged dedicated-server JVMs**, with matching reobfuscated main/probe JARs. Only JDT and GuideME are present besides the test probe; AE2/Curios/JEI/Jade are absent. Explicit FakePlayer method calls exercise actual generation/charging; they are **not** reported as real remote-client input.

Twenty-one source production ticks retain600 buffer FE+660 actual inventory charge,379 remaining burn ticks and two coal. A full4M/disabled/sixteen-coal item is an explicit capacity fixture. Actual chest inventory relocation removes exactly one item; an actual ChunkEvent.Unload and saved reload into a **different chest BE** must preserve exact whole inventory NBT. Saved shutdown/new JVM restores exact NBT before work, then one genuine resumed method tick charges660FE (1320 total receiver), leaves378 credit/two coal and rejects a duplicate wearable callback. Full disabled item stays unchanged. Both processes exit0. `artifact-sha256.txt` proves final build/installed main and probe bytes match; test-only runtime harnesses are excluded from the main JAR.

## Preserved failures

- Early compile: native registry uses Fuel_Canister. GameTest fixture needed PrefixGameTestTemplate(false); the mock player inventory menu triggered a vanilla crafting hook requiring ServerPlayer, so recipe fixture now uses an explicit inert menu. Resource assertions remain intact.
- Actual native generation originally yielded zero because of the internal no-receive bug; repaired/tested as above.
- Graphical attempts1/2/3 retained: G timed out; diagnostics then exposed actual CuriosScreenV2 overriding the request, followed by no screen after draining that click. The new payload's copied callback still resolved the potion menu. Actual pocket resolution and conditional key handling repair both; final real opening/reopening passes.
- No old test was removed, skipped or weakened. Intermediate passing production logs are retained separately from final-artifact evidence.

## Reproduce and boundaries

Use the existing configured JDK17/Gradle and marked disposable fixtures; no tool reinstall is required. Run `gradlew test runGameTestServer build serverSmokeJar compileClientSmokeJava`, then `scripts/run_pocket_client_smoke.sh` and `JDTE_LIFECYCLE_SERVER=/tmp/jdte-pocket-production JDTE_SMOKE_PREBUILT=1 scripts/run_pocket_lifecycle_probe.sh`. Production is loopback25591 with pocket-lifecycle-world; scripts guard fixture markers and preserve previous worlds. See runtime-profiles.json and retained logs for exact profiles.

Unverified: independent two-client contention, prolonged public-server uptime, cross-dimensional carrying, abrupt unsaved crash recovery, arbitrary third-party fuels/FE receivers/events and every optional-mod combination. Tests distinguish actual client packets/natural ticks from explicit setup and FakePlayer calls. No other four N items or broad Ultimate subsystem was started. No full-mod parity or published release is claimed.
