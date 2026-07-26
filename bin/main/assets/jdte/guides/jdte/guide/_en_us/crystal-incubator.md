---
navigation:
  title: Crystal Incubator
  icon: "jdte:crystal_incubator"
  position: 18
item_ids:
  - jdte:crystal_incubator
---

# Crystal Incubator

<BlockImage id="jdte:crystal_incubator" scale="2" />

The Crystal Incubator finds budding blocks inside its configured area, consumes Time Fluid and FE to accelerate their growth, and automatically collects mature clusters into nine internal output slots.

## Operating Parameters

| Property | Behavior |
|----------|----------|
| Rate | Adjustable 1-512x; fixed at 1024x with Overclock or Creative |
| Base resources | Uses JDT Time Wand-equivalent Time Fluid and FE costs with configurable multipliers |
| Output | Nine product slots; clusters remain intact when all drops cannot fit |
| Auto I/O | Accepts Time Fluid and exports products through configured sides |
| Scanning | Builds the budding cache in batches and visits only six neighboring positions |

Ordinary budding blocks use AE2 Growth Accelerator-style random ticking: each equivalent accelerator calls the budding block's own `randomTick()` once every 10 ticks. By default, the 8x setting equals six AE2 Growth Accelerators and other rates scale proportionally. Calls accrue evenly and remain bounded by the per-tick operation budget to avoid concentrated work at high rates over large areas.

Growth stops without Time Fluid or FE. Creative follows the standard rule and removes the Incubator's own resource costs. For Just Dyna Things Echoing Budding, the Incubator reserves its current-tick operating cost and transfers the exact missing standard FE and Time Fluid into the target. The budding block still applies its own post-growth costs and configured probabilities.

## Harvest Upgrades

| Upgrade | Limit | Harvest behavior |
|---------|------:|------------------|
| Fortune | 8 | Applies the matching vanilla Fortune level to the simulated harvesting tool |
| Precision | 1 | Applies vanilla Silk Touch and lets the target loot table choose the drop |

Fortune and Precision cannot be installed together.

## Compatibility

- Common `c:budding_blocks` and `c:clusters` tags cover vanilla, JDT, AE2, Data Energistics, AE2 add-ons, and Just Dyna Things Echoing Budding.
- Modpacks can add custom budding and mature blocks to `jdte:crystal_incubator_budding_blocks` and `jdte:crystal_incubator_harvestable_crystals`.

Batched range scans and a budding-block cache limit server work to the six neighbors of known budding blocks. Clusters are left intact when all drops cannot fit.

## Crafting

<RecipeFor id="jdte:crystal_incubator" />
