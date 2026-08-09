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

The Greenhouse retains crops, flowers, or saplings as reusable templates and consumes FE plus Time Fluid to generate their products. Displayed plants are client-only and no real crop blocks are placed or broken.

## Quick Start

1. Put up to four plant templates in the left slots. Templates are not consumed; stack size represents parallel plants.
2. Supply FE and Time Fluid, then select a `1-32x` speed.
3. Products enter paged output slots and can be sent through auto I/O or directly into adjacent inventories.

## Production and Capacity

| Property | Behavior |
|----------|----------|
| Base speed | 512 growth work per tick at 1x; settles in 20-tick batches by default |
| Per harvest | 10 FE; recipe Time Fluid divided by 100 and rounded up, minimum 1 mB |
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

## Upgrades and Automation

- Supports Capacity, Fluid, Fortune, Overclock, and Creative Upgrades.
- Each of up to three Fortune Upgrades adds 10% to average final output.
- Overclock or Creative locks production to 64x; Creative removes resource costs.
- Auto I/O accepts templates and Time Fluid and exports products. High-volume output is grouped and pushed to adjacent inventories at the end of the tick.

## Crafting

<RecipeFor id="jdte:greenhouse" />
