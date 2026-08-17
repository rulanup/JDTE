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
  - jdte:ae_output_upgrade
  - jdte:essence_conversion_upgrade
  - jdte:seed_conversion_upgrade
  - jdte:looting_upgrade
  - jdte:sharpness_upgrade
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

Basic, Advanced, and Extended Time Accelerators only. Allows the accelerator containing this card to accelerate supported AE2 devices through AE2's public `IGridTickable` service. Limited to one per machine; when accelerators overlap, only those containing this card contribute acceleration to AE2 devices.

<RecipeFor id="jdte:ae_acceleration_upgrade" />

## AE Output Upgrade

Place the card in the linking input of an AE2 Wireless Access Point screen and retrieve the linked card from its output. Install it in a machine with item or fluid product outputs to return those products directly to the linked AE network. The access point must remain loaded, online, and have a channel. If the network is unavailable or full, unaccepted products safely remain in the machine.

Machines without a product-output use, such as Clickers, Placers, Droppers, and Senders, reject this upgrade. The limit is one per machine. A Greenhouse Matrix Controller (from the standalone JDTE-Matrix mod) can also use it to return products from every managed Greenhouse.

<RecipeFor id="jdte:ae_output_upgrade" />

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
