# Fuel canister and extended generator validation

Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Environment: Forge 47.4.10, JDT Forge 1.1.4, Java 17; version stays 0.5.8.

## Fuel core checkpoint (2026-10-28)

[Retained build summary](fuel-core-summary.txt): **138 JUnit / 169 required GameTests**, build and both JAR reobfuscations pass. All previous 131/164 tests and assertions remain enabled. Seven pure numeric tests cover whole-item filling and overflow. Five real Forge tests cover real fuels/rejected inputs, simulated/real fill and remainder ownership, capacity/NBT round-trip, normal/upgraded native generation, full/partial FE buffers, bound menu ownership and one-time remainder return, and the registered upgrade recipe.

The canister follows the source's capacity ×4, burn unit ×10, weighted stored burn multiplier and additional resolved ×10 multiplier. The recipe requires four occupied ingot cells; vanilla consumes one per occupied cell. It preserves source NBT/Forge capabilities and suppresses the source canister crafting remainder to prevent duplicating stored fuel. Filling copies the offered stack before synchronous handler callbacks mutate it. Native solid generation now debits accepted FE only, preserving prepaid fuel while blocked. Invalid/overflowing fuel budgets are rejected before consumption. Generator Upgrade requires both canister fuel units before its ×3 FE batch.

The source has no external item-handler capability on the canister: its menu slot is transient and unused solid fuel returns on close. Do not advertise an invented persistent staging inventory. The shared recipe only registers the implemented fuel pair; Large Pocket Generator and Large Potion Canister remain absent. Both extended generators are independent conversions of the two native JDT generators, not prerequisites of the canister.

**Not yet verified at this checkpoint:** graphical fuel GUI/Curios shortcut, dedicated production JVM recovery for this new item, extended generator registration/behavior and tier conversions. The canister remains U pending those acceptance checks. Previously recorded transmitter/server tests are not reused as proof for these new paths.
