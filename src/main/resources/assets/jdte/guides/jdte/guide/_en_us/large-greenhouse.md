---
navigation:
  title: Large Greenhouse
  icon: "jdte:large_greenhouse"
  position: 19.5
item_ids:
  - jdte:large_greenhouse
---

# Large Greenhouse

<ItemImage id="jdte:large_greenhouse" scale="2" />

The Large Greenhouse is a single-placement **3×3×2** machine. It provides nine independent production lanes, reuses every plant template, and consumes only Time Fluid and FE.

## Quick Start

1. Clear a complete 3×3×2 area and place the machine.
2. Insert up to nine seeds, flowers, or saplings; template stack size represents parallel plants.
3. Supply FE and Time Fluid, then select a speed from 1-32x.
4. Attach pipes to any base-layer face or configure Auto I/O in the screen.

## Core Values

| Property | Value |
|----------|-------|
| Speed | 1-32x; Overclock or Creative locks it to 64x |
| Base cost | 10 FE per harvest; Time Fluid depends on the plant recipe |
| Large-structure bonus | Each lane runs at 9x production speed; batched Time Fluid cost is divided by nine |
| Output inventory | 16 slots by default; +16 per Capacity Upgrade, up to 64 |
| Per-slot limit | 64, then 2048/4096/8192 with one/two/three Capacity Upgrades |
| Fortune | Up to three cards; each adds 10% long-run average output |

Every batch costs at least 1 mB, so very small or slow batches may not receive the full ninefold fluid efficiency. Removing a Capacity Upgrade never deletes an oversized stack, but the slot cannot accept more until it falls below its current limit.

## Upgrades

- Supported: Capacity, Fluid, Overclock, Creative, and Fortune.
- Unsupported: Range, Filter, Underclock, and Precision.

## Plants and Recipes

- Includes common vanilla crops, saplings, flowers, and similar plants.
- Automatically discovers Mystical Agriculture, Mystical Agradditions, and Botany Pots crops.
- Generically recognizes mod crops with a mature age and preserves secondary mature-block drops.
- Modpacks can add shared `jdte:greenhouse` data recipes.

JEI shows the template, base FE, Time Fluid, and preview outputs. Clicking the growing-bed progress area also opens the category.

## KubeJS: Configured Fluids

Large Greenhouses use the shared `jdte:greenhouse` recipe format. For example, this custom carrot recipe consumes water rather than the default Time Fluid:

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:greenhouse',
    seed: { item: 'minecraft:carrot' },
    outputs: [{ id: 'minecraft:carrot', count: 2 }],
    display_block: 'minecraft:carrots',
    growth_work: 20,
    fluid: 'minecraft:water',
    time_fluid: 100
  }).id('kubejs:watered_carrot')
})
```

Omit `fluid` to use `justdirethings:time_fluid_source`. `time_fluid` is still the per-harvest amount. The machine's one tank does not mix fluids; after `/reload`, old contents remain extractable but can operate only a recipe whose required fluid still matches.

## Automation and Acceleration

Products enter internal storage first and are batch-exported at the end of the real server tick; a blocked destination applies backpressure. Time Accelerator virtual ticks are coalesced and cannot repeatedly hammer inventories. Dynamic providers such as Botany Pots default to 128 exact calls per Greenhouse per real tick, with excess work retained for later ticks.

## Placement and Removal

The controller occupies the front-center block of the bottom layer; the other 17 blocks are structure parts. Every base-layer face accepts templates, Time Fluid, and FE and exposes product extraction. Using any part opens the controller. Breaking any part removes the whole structure and returns exactly one Large Greenhouse item.

## Crafting

<RecipeFor id="jdte:large_greenhouse" />
