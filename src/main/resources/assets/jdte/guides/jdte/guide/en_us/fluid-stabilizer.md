---
navigation:
  title: Fluid Stabilizer
  icon: "jdte:advanced_fluid_stabilizer"
  position: 13
item_ids:
  - jdte:basic_fluid_stabilizer
  - jdte:advanced_fluid_stabilizer
  - jdte:extended_fluid_stabilizer
  - jdte:time_fluid_catalyst
---

# Fluid Stabilizer

The Fluid Stabilizer scans source fluid blocks within its configured range and matches them against the JDT FluidDrop recipes using the item in the catalyst slot.

- Converts up to 1 source fluid block per operation.
- Consumes 1 catalyst per successful conversion.
- Creative Upgrade waives both FE and catalyst consumption.
- Filter slots use fluid buckets as filter items.
- Supports redstone control, Range Upgrade, Filter Upgrade, Overclock, Underclock, and Capacity Upgrades.

For example, placing JDT's Polymorph Catalyst converts water sources to polymorph fluid; placing JDTE's Time Fluid Catalyst converts water sources to time fluid.

## Basic Fluid Stabilizer

<BlockImage id="jdte:basic_fluid_stabilizer" scale="2" />

Does not require FE and only consumes catalysts. Longer operation interval (40 ticks).

<RecipeFor id="jdte:basic_fluid_stabilizer" />

## Advanced Fluid Stabilizer

<BlockImage id="jdte:advanced_fluid_stabilizer" scale="2" />

Requires both FE and catalysts. Operation interval is 20 ticks, with FE consumption scaling with configured range.

<RecipeFor id="jdte:advanced_fluid_stabilizer" />

## Extended Fluid Stabilizer

<BlockImage id="jdte:extended_fluid_stabilizer" scale="2" />

An extended version of the Advanced Fluid Stabilizer with 8 upgrade slots. Obtained by right-clicking an Advanced Fluid Stabilizer with an Extended Upgrade.

<RecipeFor id="jdte:extended_fluid_stabilizer" />
