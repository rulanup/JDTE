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

The Greenhouse retains up to four crops, flowers, or saplings as reusable planting templates and consumes Time Fluid and FE to produce plant products quickly. Its transparent chamber renders all four matching plants without placing, random-ticking, or breaking real blocks in the world.

## Operating Parameters

| Property | Behavior |
|----------|----------|
| 1x baseline | Accumulates 512 growth work each tick |
| Multiplier | Speed button adjusts 1-32x; Overclock or Creative unlocks 64x |
| Energy per harvest | 10 FE with no additional speed-level penalty |
| Fluid per harvest | Recipe cost divided by 100 and rounded up, with a 1 mB minimum |
| Settlement | Combines production once every 20 ticks by default |
| Input | Four stackable plant templates plus Time Fluid; stack count represents parallel plants |
| Stack fluid | 1x through half the item's maximum stack, 2x above half |
| Output | 16 paged product slots by default; each Capacity Upgrade adds 16, up to 64 |
| Auto I/O | Supports seed and Time Fluid input plus product output |

Capacity, Fluid, Overclock, Creative, and Fortune Upgrades are supported. Up to three Fortune Upgrades apply vanilla Fortune III; Range, Filter, Underclock, and Precision are unavailable.

## Plant Compatibility

- Built-in recipes cover wheat, carrots, potatoes, beetroot, pumpkins, melons, nether wart, and cocoa beans.
- Every enabled Mystical Agriculture crop is discovered through its public Crop Registry.
- Mystical Agradditions registers its crops in the same registry and is covered automatically.
- Other mod seeds linked to a crop block with an `age` property are discovered generically.
- Flowers, saplings, mushrooms, and similar mod plants implemented as `BushBlock` are supported generically and default to their own loot tables.
- Built-in vanilla sapling recipes produce matching logs and return saplings. Mod trees whose full products cannot be derived through a stable public API can define multiple explicit outputs in data packs.
- Mature crop loot tables provide the harvest, so wheat includes wheat and seeds while mod crops retain their secondary and Fortune drops.
- Mystical crop Time Fluid cost grows with the square of crop tier.
- Modpacks can add more plants with `jdte:greenhouse` recipes and choose explicit `outputs` or mature-block loot with `use_loot_table`.

The JEI category displays the plant template, base Time Fluid, base FE, and preview outputs. Clicking the growing-soil progress area in the Greenhouse GUI opens the category.

The Greenhouse caches only its four current plant-template definitions. All four lanes share one bounded settlement cap and rotate resource priority. Each plant uses at most four real loot samples per settlement before scaling them as a batch; it creates no item entities, performs no area scans, and never loops loot tables at the theoretical harvest count.

Overclock and Creative Upgrades both lock production to 64x. The growing-bed progress advances only while at least one lane has its required resources and output space. New harvests are generated directly into an adjacent item container, preferring the most recently successful side; only remainders fall back to the Greenhouse's internal output slots.

Greenhouses connect visually when placed next to one another horizontally. Shared glass walls, posts, and roof rails disappear, while the glass roof extends across each joined edge. Connections are visual only; every Greenhouse keeps its own inventory, resources, and upgrades.

## Crafting

<RecipeFor id="jdte:greenhouse" />
