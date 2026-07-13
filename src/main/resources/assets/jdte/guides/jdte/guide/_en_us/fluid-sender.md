---
navigation:
  title: Fluid Sender
  icon: "jdte:advanced_fluid_sender"
  position: 10
item_ids:
  - jdte:basic_fluid_sender
  - jdte:advanced_fluid_sender
  - jdte:extended_fluid_sender
---

# Fluid Sender

The Fluid Sender sends internal fluids to target containers within its configured range. It does not place fluid source blocks in the world.

## Variants

| Machine | Upgrade Slots | Send Amount | Requires FE |
|---------|--------------|-------------|-------------|
| Basic Fluid Sender | 4 | All available fluid | No |
| Advanced Fluid Sender | 4 | All available fluid | Yes |
| Extended Fluid Sender | 8 | All available fluid | Yes |

## Features

- Sends fluids from the internal tank to fluid containers within range
- Distributes transfers across in-range containers in round-robin order so the nearest or front target cannot monopolize output
- Supports redstone control, Range Upgrade, and Filter Upgrade
- Unlimited batching is enabled by default and moves all available tank contents per operation; it can be disabled to use normal/overclock batch limits
- With an Overclock or Creative Upgrade and Auto Input enabled, fluid moves directly from Auto Input containers to ranged targets without using internal tank capacity
- Speed controls and Underclock adjust the operation interval, while Capacity Upgrades increase internal tank capacity

## Crafting

### Basic Fluid Sender

<BlockImage id="jdte:basic_fluid_sender" scale="2" />

<RecipeFor id="jdte:basic_fluid_sender" />

### Advanced Fluid Sender

<BlockImage id="jdte:advanced_fluid_sender" scale="2" />

<RecipeFor id="jdte:advanced_fluid_sender" />

### Extended Fluid Sender

<BlockImage id="jdte:extended_fluid_sender" scale="2" />

An extended version of the Advanced Fluid Sender with 8 upgrade slots. Obtained by right-clicking an Advanced Fluid Sender with an Extended Upgrade.

<RecipeFor id="jdte:extended_fluid_sender" />
