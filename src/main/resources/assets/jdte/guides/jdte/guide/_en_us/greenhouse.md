---
navigation:
  title: Greenhouse
  icon: "jdte:greenhouse"
  position: 19
item_ids:
  - jdte:greenhouse
---

# Greenhouse

<BlockImage id="jdte:greenhouse" scale="2" />

The Greenhouse retains crops, flowers, or saplings as reusable templates and consumes FE plus each recipe's required fluid (Time Fluid by default) to generate their products. Displayed plants are client-only and no real crop blocks are placed or broken.

## Quick Start

1. Put up to four plant templates in the left slots. Templates are not consumed; stack size represents parallel plants.
2. Supply FE and the recipe's required fluid (Time Fluid by default), then select a `1-32x` speed.
3. Products enter paged output slots and can be sent through auto I/O or directly into adjacent inventories.

## Production and Capacity

| Property | Behavior |
|----------|----------|
| Base speed | 512 growth work per tick at 1x; settles in 20-tick batches by default |
| Per harvest | 10 FE; recipe fluid cost divided by 100 and rounded up, minimum 1 mB |
| Template stacks | Up to half the item's stack limit uses 1x fluid; larger stacks use 2x |
| Outputs | 16 slots by default; each Capacity Upgrade adds 16, up to 64 |
| Slot limit | 64 by default; 2048/4096/8192 with one/two/three Capacity Upgrades |

Production pauses safely when outputs are blocked. Horizontally adjacent Greenhouses connect visually but keep separate inventories and upgrades.

## Plant Compatibility

Resolution order is: JDTE data recipe → dedicated Mystical Agriculture integration → Botany Pots → generic plant detection.

- Built-in recipes cover common vanilla crops, Nether Wart, Cocoa Beans, and saplings.
- Mystical Agriculture and Mystical Agradditions use the public Crop Registry and their mature crop drops.
- Every loaded Botany Pots crop recipe is enumerated directly, including Spore Blossoms and plants added by mods or datapacks.
- Other crops with an `age` property and common flowers, saplings, or mushrooms are detected when possible.
- Modpacks can add `jdte:greenhouse` data recipes with explicit templates, products, and fluid costs.

JEI shows the template, base costs, and preview products. Click the growing-bed progress area to open the category.

## KubeJS: Configured Fluids

Use a `jdte:greenhouse` recipe to replace a plant's recipe. This same recipe format is used by the Greenhouse, Large Greenhouse, and the Greenhouse Matrix (from the standalone JDTE-Matrix mod).

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'jdte:greenhouse/wheat' })
  event.custom({
    type: 'jdte:greenhouse',
    seed: { item: 'minecraft:wheat_seeds' },
    outputs: [{ id: 'minecraft:wheat', count: 2 }],
    display_block: 'minecraft:wheat',
    growth_work: 20,
    fluid: 'minecraft:water',
    time_fluid: 100
  }).id('jdte:greenhouse/wheat')
})
```

`fluid` is optional and defaults to `justdirethings:time_fluid_source`. `time_fluid` remains the amount consumed per harvest; it is not a fluid ID. A Greenhouse has one tank, so it never mixes fluids. After `/reload`, its previous fluid can still be extracted, but it powers only recipes that still require that fluid.

## Upgrades and Automation

- Supports Capacity, Fluid, Fortune, Overclock, and Creative Upgrades.
- Each of up to three Fortune Upgrades adds 10% to average final output.
- Overclock or Creative locks production to 64x; Creative removes resource costs.
- Auto I/O accepts templates and recipe-required fluid (Time Fluid by default) and exports products. High-volume output is grouped and pushed to adjacent inventories at the end of the tick.

## Crafting

<RecipeFor id="jdte:greenhouse" />
