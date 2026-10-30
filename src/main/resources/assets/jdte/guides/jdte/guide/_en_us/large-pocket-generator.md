---
navigation:
  title: Large Pocket Generator
  icon: "jdte:large_pocket_generator"
  position: 28
item_ids:
  - jdte:large_pocket_generator
---

# Large Pocket Generator

Stores **four times** the native Pocket Generator's configured energy capacity (default **4,000,000 FE**). It burns smelting fuels, JDT coal and coal blocks, and both fuel canisters. Right-click in the **main hand** to open the persistent one-slot fuel inventory. Shift-use does not open it. Use the native JDT enabled/disabled control to pause both generation and charging.

While enabled in your player inventory, it burns one fuel into whole burn ticks and charges eligible inventory equipment at the native configured per-item rate. Like the pinned Neo parent, it also charges equipment you are actively using. The default base burn speed is4 and each fuel tick produces15 FE: ordinary coal therefore supplies400 generator ticks at60 FE/t,24,000 FE total. Fuel-specific multipliers change speed, with the source whole-tick rounding. A full energy buffer pauses burning. If only part of one tick fits, the source behavior consumes that tick and clips FE to the available space. At the last burn tick it preloads the next valid fuel and retains its full future credit.

The **Large Pocket Generator Curios slot** runs the same server-authoritative generation and inventory charging. Press **G** (configurable) to open that equipped generator. After the vanilla inventory, the pinned parent also charges actual equipped Curios items through their FE capabilities. Cosmetic slots are excluded. External FE **input is disabled**; FE extraction and native Energy Transmitter item-source use are supported. No fluid interface or machine upgrade slots are added.

Partial fuel, crafting remainders, enabled state, remaining burn credit, fuel speed and FE survive item movement and saves. The menu shares fuel state with natural production and automation, so it cannot restore already consumed fuel from an old view. Non-fuels are rejected; container fuels occupy one input position and return their real crafting remainder.

Craft one native Pocket Generator with **four Eclipse Alloy Ingots in four crafting slots** and one Time Fluid Catalyst. The upgrade preserves all native item NBT, including FE, burn progress, fuel, enabled state and custom name. The menu shows energy and remaining burn time; hover the energy bar for exact values with Shift.

<RecipeFor id="jdte:large_pocket_generator" />
