# Fuel canister and extended generator validation

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Environment: Forge 47.4.10, JDT Forge 1.1.4, Java 17; version stays 0.5.8.

## Fuel core checkpoint (2026-10-28)

[Retained build summary](fuel-core-summary.txt): **138 JUnit / 169 required GameTests**, build and both JAR reobfuscations pass. All previous 131/164 tests and assertions remain enabled. Seven pure numeric tests cover whole-item filling and overflow. Five real Forge tests cover real fuels/rejected inputs, simulated/real fill and remainder ownership, capacity/NBT round-trip, normal/upgraded native generation, full/partial FE buffers, bound menu ownership and one-time remainder return, and the registered upgrade recipe.

The canister follows the source's capacity ×4, burn unit ×10, weighted stored burn multiplier and additional resolved ×10 multiplier. The recipe requires four occupied ingot cells; vanilla consumes one per occupied cell. It preserves source NBT/Forge capabilities and suppresses the source canister crafting remainder to prevent duplicating stored fuel. Filling copies the offered stack before synchronous handler callbacks mutate it. Native solid generation now debits accepted FE only, preserving prepaid fuel while blocked. Invalid/overflowing fuel budgets are rejected before consumption. Generator Upgrade requires both canister fuel units before its ×3 FE batch.

The source has no external item-handler capability on the canister: its menu slot is transient and unused solid fuel returns on close. Do not advertise an invented persistent staging inventory. The shared recipe only registers the implemented fuel pair; Large Pocket Generator and Large Potion Canister remain absent. Both extended generators are independent conversions of the two native JDT generators, not prerequisites of the canister.

**Not yet verified at this checkpoint:** graphical fuel GUI/Curios shortcut, dedicated production JVM recovery for this new item, extended generator registration/behavior and tier conversions. The canister remains U pending those acceptance checks. Previously recorded transmitter/server tests are not reused as proof for these new paths.

## Extended generator core checkpoint

[Build/test summary](extended-core-summary.txt): **138 JUnit / 178 required GameTests**, main/probe reobfuscation and graphical harness compilation pass. `ExtendedGeneratorGameTests` adds nine real Forge tests covering both machines and both native conversion paths, denied conversion rollback, whole-fuel/FE conservation, external validators and rates, actual ten-execution Ultimate ticker/physical capacity, packing/BlockItem placement, preserving crafting and invalidated/revived public capabilities. No simulated test is labeled a real chunk unload; production-JVM and graphical checks remain pending in this section.

The source generators are thin native subclasses with eight upgrade slots. Preserve their non-directional blockstate and native configured fuel/FE/tank ownership. Only JDT `RefinedFuel` is accepted by the liquid machine. Both native constructors create output-only energy storage; wrapper behavior follows the actual owned storage. FE output limits are per face, including native liquid export ×10. The port corrects short-acceptance loss and packed recipe NBT loss. Retained public ports reject detached/unloaded owners. The capability provider now recreates invalidated handles after Forge revival without recreating or copying their underlying inventories.

Source Extended Upgrade has 20 targets; 18 are now wired. Experience Holder and Advanced Bio Extractor are still absent. Other machines' missing Ultimate behaviors and the external JDTE-AE backend are not completed by this work.
