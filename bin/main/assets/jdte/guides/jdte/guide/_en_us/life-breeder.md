---
navigation:
  title: Life Breeder
  icon: "jdte:life_breeder"
  position: 22
item_ids:
  - jdte:life_breeder
---

# Life Breeder

<BlockImage id="jdte:life_breeder" scale="2" />

The Life Breeder automatically pairs breedable animals or Villagers in its configured area and directly advances baby growth and adult breeding cooldowns. Standard animals use their native breeding path. Villagers use their public offspring logic and machine-supplied food without requiring an extra bed. Parent NBT is never copied and entity AI is not repeatedly ticked.

## Modes

| Mode | Behavior |
|------|----------|
| Breeding and Growth | Feeds compatible pairs while accelerating babies and adult breeding cooldowns |
| Breeding Only | Pairs only adults that can currently breed |
| Growth Only | Advances babies and adult cooldowns without consuming feed to breed |

The 2x2 inputs can hold breeding food for different animals. A Villager pair requires 24 food points: six Bread or 24 Carrots, Potatoes, or Beetroot, with mixed inputs supported. Successful breeding also consumes FE and Life Fluid. The 4x2 outputs collect bounded batches of item entities that actually exist in the area, such as eggs or modded animal products. Items that do not fit remain safely in the world.

## Speed and Upgrades

- Normal speed is adjustable from `1-32x`. Processing is batched every 20 ticks by default; the multiplier advances biological age without repeating AI or complete entity ticks.
- Overclock completes the remaining baby age or adult cooldown for each processed animal, while charging for the biological ticks actually skipped.
- Creative includes Overclock behavior and removes FE and Life Fluid costs.
- Eight upgrade slots accept Capacity, Fluid, Range, Filter, Overclock, and Creative Upgrades.
- Entity filters use spawn eggs. Allowlist mode processes only listed types, denylist mode excludes them, and an empty filter permits every supported creature.
- Redstone control, area settings, and absolute-side auto I/O are supported. Automatic input reaches only feed and Life Fluid, while automatic output extracts only collected products.

Life Fluid is derived from the biological ticks actually skipped and uses a configurable 10x default factor. Breeding is priced from the standard 6000-tick cooldown or the base breeding cost, whichever is larger, resulting in 3000 mB per pair with default settings. Growth cost scales linearly with the age or cooldown time advanced in that settlement; advancing 20 biological ticks costs 10 mB by default.

## Compatibility and Performance

Modded creatures that extend Minecraft `Animal` and implement the standard `isFood`, `canMate`, and offspring paths work automatically. Sex, breed, and genetics restrictions remain controlled by the creature. Villagers have a dedicated public-API adapter; fully custom creatures outside the `Animal` or `AgeableMob` systems are not force-bred.

The machine queries entity sections only in loaded chunks and has separate per-cycle limits for inspection, pairing, growth, and item collection. Breeding pauses at 64 animals of one type by default. All budgets and costs are server-configurable.

## Crafting

<RecipeFor id="jdte:life_breeder" />
