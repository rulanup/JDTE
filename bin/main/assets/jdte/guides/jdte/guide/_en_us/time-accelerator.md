---
navigation:
  title: Time Accelerator
  icon: "jdte:basic_time_accelerator"
  position: 2
item_ids:
  - jdte:basic_time_accelerator
  - jdte:advanced_time_accelerator
  - jdte:extended_time_accelerator
---

# Time Accelerator

Time accelerators speed up the operation of blocks within their configured area.

## Basic Time Accelerator

<BlockImage id="jdte:basic_time_accelerator" scale="2" />

A simple accelerator that only consumes time fluid.

- Default acceleration multiplier: 16x
- With Overclock or Creative Upgrade: 32x
- Uses the base Time Fluid cost rate

<RecipeFor id="jdte:basic_time_accelerator" />

## Advanced Time Accelerator

<BlockImage id="jdte:advanced_time_accelerator" scale="2" />

An advanced accelerator that consumes both time fluid and FE energy.

- Adjustable multiplier: 1x - 64x
- With Overclock or Creative Upgrade: 128x
- Uses twice the Basic tier's Time Fluid cost rate

<RecipeFor id="jdte:advanced_time_accelerator" />

## Extended Time Accelerator

<BlockImage id="jdte:extended_time_accelerator" scale="2" />

An extended version of the Advanced Time Accelerator with 8 upgrade slots. Obtained by right-clicking an Advanced Time Accelerator with an Extended Upgrade.

- Adjustable multiplier: 1x - 512x
- With Overclock or Creative Upgrade: 1024x
- Uses five times the Basic tier's Time Fluid cost rate
- Supports 8 upgrade slots for more upgrade cards

All three tiers use the same managed scheduler. Overlapping accelerators fully add their multipliers, loaded block entities are discovered once per chunk, and unfinished paid virtual ticks continue while their contributing accelerators remain active. The scheduler uses configurable MSPT headroom and can accelerate AE2 devices that expose the standard `IGridTickable` service.

<RecipeFor id="jdte:extended_time_accelerator" />

## Time Fluid Catalyst

<ItemImage id="jdte:time_fluid_catalyst" scale="2" />

Directly triggers the water source to JDT time fluid FluidDrop conversion. Can also be placed in the Fluid Stabilizer's catalyst slot.

<RecipeFor id="jdte:time_fluid_catalyst" />
