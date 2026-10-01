---
navigation:
  title: Upgrade Cards
  icon: "jdte:capacity_upgrade"
  position: 1
item_ids:
  - jdte:capacity_upgrade
  - jdte:overclock_upgrade
  - jdte:underclock_upgrade
  - jdte:fluid_upgrade
  - jdte:fluid_storage_upgrade
  - jdte:generator_upgrade
  - jdte:range_upgrade
  - jdte:filter_upgrade
  - jdte:creative_upgrade
  - jdte:fortune_upgrade
  - jdte:precision_upgrade
  - jdte:ae_acceleration_upgrade
  - jdte:ae_crafting_read_upgrade
  - jdte:ae_output_upgrade
  - jdte:ae_extraction_upgrade
  - jdte:entity_acceleration_upgrade
  - jdte:essence_conversion_upgrade
  - jdte:seed_conversion_upgrade
  - jdte:looting_upgrade
  - jdte:sharpness_upgrade
  - jdte:energy_brewing_upgrade
  - jdte:energy_overload_upgrade
  - jdte:upgrade_storage
  - jdte:advanced_upgrade_storage
---

# Upgrade Cards

Sneak-right-click a JDT or JDTE machine while holding Upgrade Cards to fill available upgrade slots until the type limit, slot capacity, or held stack is exhausted. Looting and Sharpness Upgrades are inserted into dedicated slots on supported machines. Machine compatibility, per-type limits, and Overclock/Underclock conflicts are still enforced. When FTB Ultimine is installed, hold its activation key while sneak-right-clicking to fill each eligible machine in the current selection in order.

Upgrade cards can be installed into JDT machines to enhance their functionality.

## Capacity Upgrade

<ItemImage id="jdte:capacity_upgrade" scale="2" />

Doubles the machine's FE capacity and fluid capacity. Stacks up to 3 times.

<RecipeFor id="jdte:capacity_upgrade" />

## Overclock Upgrade

<ItemImage id="jdte:overclock_upgrade" scale="2" />

Forces the machine to run at 1 tick intervals and perform two operations per tick. Energy consumption becomes 3x.

<RecipeFor id="jdte:overclock_upgrade" />

## Underclock Upgrade

<ItemImage id="jdte:underclock_upgrade" scale="2" />

Forces the machine to run at 40 tick intervals. Energy consumption is reduced by 80%.

<RecipeFor id="jdte:underclock_upgrade" />

## Fluid Upgrade

<ItemImage id="jdte:fluid_upgrade" scale="2" />

Doubles only the machine's fluid capacity. Stacks up to 3 times.

<RecipeFor id="jdte:fluid_upgrade" />

## Fluid Storage Upgrade

<ItemImage id="jdte:fluid_storage_upgrade" scale="2" />

Adds an internal fluid tank to the Clicker.

<RecipeFor id="jdte:fluid_storage_upgrade" />

## Generator Upgrade

<ItemImage id="jdte:generator_upgrade" scale="2" />

Consumes double fuel to output triple power.

<RecipeFor id="jdte:generator_upgrade" />

## Range Upgrade

<ItemImage id="jdte:range_upgrade" scale="2" />

Doubles the machine's configurable area limit. Stacks up to 2 times.

<RecipeFor id="jdte:range_upgrade" />

## Filter Upgrade

<ItemImage id="jdte:filter_upgrade" scale="2" />

Adds extra filter slots to the machine. Each upgrade adds one row (9 slots). Stacks up to 2 times.

**Limited to:** Machines with filter slots (e.g., Clicker T2, Sensor T2, etc.)

<RecipeFor id="jdte:filter_upgrade" />

## Creative Upgrade

<ItemImage id="jdte:creative_upgrade" scale="2" />

Waives FE consumption; time accelerators waive time fluid consumption; includes overclock effect.

<RecipeFor id="jdte:creative_upgrade" />

## Fortune Upgrade

<ItemImage id="jdte:fortune_upgrade" scale="2" />

Used by Gel Generators and Crystal Incubators. Gel Generators apply vanilla Fortune scaling to supported products; Crystal Incubators enchant their simulated harvesting tool with the installed Fortune level. Incubators accept up to eight and cannot combine Fortune with Precision.

<RecipeFor id="jdte:fortune_upgrade" />

## Precision Upgrade

<ItemImage id="jdte:precision_upgrade" scale="2" />

Crystal Incubator only. It applies vanilla Silk Touch to the simulated harvesting tool and lets the target block's own loot table determine the precise drop, preserving compatibility with mods that follow vanilla loot behavior. Limited to one and incompatible with Fortune.

<RecipeFor id="jdte:precision_upgrade" />

## AE Acceleration Upgrade

<ItemImage id="jdte:ae_acceleration_upgrade" scale="2" />

Basic, Advanced, and Extended Time Accelerators only, limited to one per machine. AE2 remains optional for the main `jdte` mod; without the JDTE-AE addon, this card retains per-device acceleration through `IGridTickable`.

Full Grid acceleration requires matching versions of JDTE-AE (`jdte-ae`) and JDTE plus AE2 19.2.17+ on both client and server. Reaching any online, fully booted node accelerates its entire Grid, while multiple covered nodes on that Grid do not duplicate one accelerator's contribution. Nominal `X` includes the native tick, so one accelerator contributes `X - 1` and overlaps produce `1 + Σ(Xᵢ - 1)`; nominal 1024x runs 1023 additional complete Grid lifecycles per real tick. Grid work bypasses the ordinary 4096-execution budget and can raise MSPT substantially; insufficient resources reject the whole batch without execution or charge.

<RecipeFor id="jdte:ae_acceleration_upgrade" />

## AE Crafting Read Upgrade

<ItemImage id="jdte:ae_crafting_read_upgrade" scale="2" />

Place the card in the linking input of an AE2 Wireless Access Point screen and retrieve the linked card from its output. Install it in a compatible machine's standard or extended upgrade slot. The machine reads active crafting tasks from the linked AE2 network and runs automatically while one is present. It pauses when unlinked, when the Wireless Access Point is unloaded or offline, while the network is booting, or when no crafting task is active; it resumes automatically after the network recovers and a task is available. Limited to one per machine. AE2 is an optional dependency, so the upgrade remains inactive when AE2 is not installed.

## AE Output Upgrade

Place the card in the linking input of an AE2 Wireless Access Point screen and retrieve the linked card from its output. Install it in ANY JDT/JDTE machine and products are returned to the linked AE network on a fixed cadence (every 5 ticks by default, configurable with `jdte.aeOutput.returnInterval`) through real ME storage writes — terminals, buses, and crafting providers see returned items immediately. The access point must remain loaded, online, and have a channel. If the network is unavailable or full, unaccepted products safely remain in the machine.

The limit is one per machine; machines without output slots simply never return anything. Installing this card disables the machine's regular automatic output so products only flow to AE. A Greenhouse Matrix Controller (from the standalone JDTE-Matrix mod) can also use it to return products from every managed Greenhouse.

<RecipeFor id="jdte:ae_output_upgrade" />

## AE Extraction Upgrade

<ItemImage id="jdte:ae_extraction_upgrade" scale="2" />

Craft this upgrade from a Wireless Receiver, Export Bus, and Capacity Upgrade. Put it into an AE2 Wireless Access Point input slot to bind it, then apply the bound upgrade in a JDT or JDTE smithing recipe. A bound upgrade collects enabled FE and fluid items carried by the player and refills them from the linked Applied Flux/AE2 network; the Wireless Access Point must stay loaded, online, and connected to storage with available resources.

Fluid selection prefers fluid already present in a tank. An empty universal tank is selected only when exactly one candidate fluid is valid, so an unknown fluid is never filled speculatively. Applied Flux is an optional integration; FE refilling safely does nothing when it is not installed.

Empty universal containers are not selected as fluid targets. Only tanks with existing contents or a dedicated tank that accepts exactly one candidate participate. Smithing preserves the target item's existing energy, fluid, and other components.

<RecipeFor id="jdte:ae_extraction_upgrade_apply" />

## Entity Acceleration Upgrade

<ItemImage id="jdte:entity_acceleration_upgrade" scale="2" />

Exclusive to the Ultimate Time Wand. Once installed, the wand can accelerate entities in addition to blocks. It can be installed in a smithing table, crafting table, or directly by right-clicking the upgrade onto the Ultimate Time Wand in the inventory.

While accelerated, the entity's AI is not accelerated, preventing excessive pathfinding lag, erratic movement, or rapid attacks. Entity life timers, growth (baby to adult), breeding cooldowns, potion effects, and production timers advance at the full accelerated rate.

<RecipeFor id="jdte:entity_acceleration_upgrade" />

## Essence Conversion Upgrade

<ItemImage id="jdte:essence_conversion_upgrade" scale="2" />

Greenhouse and Large Greenhouse only. For each harvested essence, the machine checks every crafting-table recipe that uses it. Conversion occurs only when exactly one such recipe exists and every non-empty ingredient in that recipe is that essence. The original ingredient and result counts are preserved; an incomplete recipe batch remains as essence and combines with later harvests. Essences with multiple recipes or recipes that need another ingredient remain unchanged.

<RecipeFor id="jdte:essence_conversion_upgrade" />

## Seed-to-Essence Upgrade

<ItemImage id="jdte:seed_conversion_upgrade" scale="2" />

Greenhouse and Large Greenhouse only. When harvesting a Mystical Agriculture crop, seed drops matching the planted template are replaced 1:1 with that crop's essence. If an Essence Conversion Upgrade is also installed, the added essence continues through its unique-recipe check and is converted to the final product when eligible. Other byproducts remain unchanged.

<RecipeFor id="jdte:seed_conversion_upgrade" />

## Looting Upgrade

<ItemImage id="jdte:looting_upgrade" scale="2" />

Exclusive to the Bio Crusher. Increases extra drop chance. Max level 6, with 50% chance per level for +1 drop.

<RecipeFor id="jdte:looting_upgrade" />

## Sharpness Upgrade

<ItemImage id="jdte:sharpness_upgrade" scale="2" />

Exclusive to the Bio Crusher. Increases attack damage. Each upgrade adds 5 damage, max 6, for up to 35 damage.

<RecipeFor id="jdte:sharpness_upgrade" />

## Energy Brewing Upgrade

<ItemImage id="jdte:energy_brewing_upgrade" scale="2" />

Advanced Potion Brewer only, limited to 1. Once installed, brewing fuel is paid with FE: each charge consumes the configured `energyPerBlazePowder` FE (default 5,000) and provides the same 20 brews as one Blaze Powder, disabling the Blaze Powder slot so no external Blaze Powder supply is needed. The Creative Upgrade waives the fee.

<RecipeFor id="jdte:energy_brewing_upgrade" />

## Energy Overload Upgrade

<ItemImage id="jdte:energy_overload_upgrade" scale="2" />

Energy Transmitters only, limited to 1. Once installed, removes all single-batch and per-tick transfer limits from the Energy Transmitter: including per-target limits, per-tick transfer budgets, and ME extraction limits, directly attempting all targets in range and fully charging bound player equipment in a single tick.

<RecipeFor id="jdte:energy_overload_upgrade" />

## Upgrade Storage

<ItemImage id="jdte:upgrade_storage" scale="2" />

A portable case dedicated to storing JDTE and JDT upgrades. Right-click to open a 4×5 grid of 20 upgrade slots. Upgrades stored inside can stack up to 64, bypassing the normal single-item stack limitation. Supports Shift-click fast transfer and automatically persists stored contents.

<RecipeFor id="jdte:upgrade_storage" />

## Advanced Upgrade Storage

<ItemImage id="jdte:advanced_upgrade_storage" scale="2" />

An advanced portable case for JDTE and JDT upgrades. Right-click to open a 6×6 grid of 36 upgrade slots, each supporting stacking up to 64. When carried by the player, opening any JDT or JDTE machine interface displays a dedicated side panel showing all stored upgrades. Players can directly transfer upgrades into the machine with a single click, pick them up to the cursor, or withdraw them to inventory, seamlessly managing machine upgrades without leaving the machine interface.

<RecipeFor id="jdte:advanced_upgrade_storage" />

