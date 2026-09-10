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

## Scheduling and Multipliers

All three tiers use the same managed scheduler. A nominal multiplier includes the target's native tick: one nominal `X` accelerator contributes `X - 1` additional cycles, and overlaps produce `1 + Σ(Xᵢ - 1)`. For example, two 16x accelerators covering one target produce 31x, not 32x.

Ordinary block entities and random-tick targets are discovered by chunk and use a paid virtual-tick queue with fixed per-tick execution and scan budgets. High server MSPT does not stop acceleration; ordinary work above the budget remains queued while its contributing accelerator stays active. If FE or Time Fluid is insufficient, the whole request for that tick is rejected without execution or charge instead of silently using a lower multiplier.

## AE2 Grid Acceleration

AE2 remains optional for the main `jdte` mod. Without the JDTE-AE addon, the AE Acceleration Upgrade retains its per-device fallback through `IGridTickable`, including public Grid Tick service targets such as Molecular Assemblers.

Full Grid acceleration requires matching versions of JDTE-AE (`jdte-ae`) and JDTE plus AE2 19.2.17+ on both client and server. When the accelerator reaches any online, fully booted AE node, that node's entire Grid receives the full multiplier. Crafting CPUs, Pattern Providers, Molecular Assemblers, and addon devices that follow the AE lifecycle advance together even when they are outside the accelerator's area. Covering several nodes on the same Grid counts one accelerator's contribution only once.

Full Grid mode synchronously runs Server Start, Level Start, Level End, and Server End for every additional cycle. Nominal 1024x therefore runs 1023 additional complete Grid lifecycles per real tick. This work bypasses the ordinary target budget of 4096 executions and can raise server MSPT substantially, so reserve sufficient server headroom before using high multipliers.

<RecipeFor id="jdte:extended_time_accelerator" />

## Time Fluid Catalyst

<ItemImage id="jdte:time_fluid_catalyst" scale="2" />

Directly triggers the water source to JDT time fluid FluidDrop conversion. Can also be placed in the Fluid Stabilizer's catalyst slot.

<RecipeFor id="jdte:time_fluid_catalyst" />
