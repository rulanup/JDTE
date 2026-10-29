# Bio Extractor Forge acceptance evidence

Reference: pinned JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`; Forge 47.4.10 / JDT 1.1.4 / Java 17; Mod stays 0.5.8.

## Core checkpoint (2026-10-29)

**146 JUnit / 198 required GameTests**, full build and main/probe reobfuscation pass. Original 140/183 tests/assertions are retained. Six new numeric tests cover scan-counter boundaries/coalescing/overflow. Fifteen real Forge tests cover all nine loaded recipes and network round trip, real cow milk/FE and shared cooldown, colored sheep/sheared exclusion, inventory/fluid backpressure, insufficient/short-debit rollback, tier/Overclock target limits, source timing/Ultimate ticker/physical buffers, spawn-egg filters, all capability faces/stale ownership/fresh NBT, capacity removal safety, actual adjacent automatic output, Shift output and actual conversion/protection rollback/packed crafting/wrench placement.

Initial failures are retained as checkpoint facts: processing item JSON was incorrectly adapted despite the shared Forge parser retaining the original `id` key; corrected to pinned schema. A full filter page existed only after menu creation; new machine filter storage now expands while preserving existing templates before any menu. Original source's output-only internal insertion dropped rolled products; internal staged commits bypass only external insertion validation. A real client exposed Forge native BaseMachineContainer's private `moveItemStackTo` name collision; use the existing mapped AbstractContainerMenu Invoker, with a real Shift regression test. No assertions were removed or weakened.

**Pending at this checkpoint:** graphical GUI/JEI/Jade and production-JVM full-inventory packing, actual unload and persistent creature cooldown/restart. Both rows remain U until those paths pass. A production probe and separate graphical harness are implemented; they are not shipped in the main Mod JAR.

## Source contracts and deliberate fixes

Advanced: 100k FE / 16k mB / 9 output slots / four cards / one target per scan. Extended: 200k / 32k / 18 / eight / four. Standard Overclock/Creative target caps two/eight; normal/Overclock/Underclock scan periods 20/5/40. Source Ultimate executes ten times with ×50 energy, retaining the source ordinary 20-counter scan period. Ultimate Capacity ×50 composes with ordinary cards. The source's initial radius 5/9 is clamped to the shared configured maximum (default 5); Range raises the permitted limit, not an automatic area expansion.

Products use real registered entity types, persistent shared absolute cooldown, chances and optional damage. Sheep color/shearing is preserved; babies are not newly excluded because the source does not exclude them. Forge enables its real milk fluid and the recipe serializer maps source `minecraft:milk` to Forge milk. No fake fluid/item is substituted. Existing Bio Factory/other partial systems are not declared fully accepted by this alias.

Fix resource loss/duplication risks: output staging, checked FE/fluid commit rollback, fixed-size inventory NBT expansion 9→18, stored normal/coalesced progress, invalid public ports, state-preserving Extended crafting/conversion, normal-drop duplicated item NBT removal, safe capacity-card changes and actual output-only item automation routes. The source tank permits external filling, including a foreign fluid that then blocks milk until drained; this is documented and tested rather than inventing output-only fluid semantics.

Unverified: independent two-player concurrency specifically for Bio Extractors, prolonged public servers, arbitrary data packs/mod combinations, external JDTE-AE whole-grid backend and complete reactor/fusion operation. Optional integrations use existing guarded bridges; selected new runtime checks follow below.
