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

The Life Breeder breeds animals or Villagers in its configured area and directly advances baby growth and adult breeding cooldowns. It uses each creature's native breeding path, never copies parent NBT, and does not repeat complete entity AI ticks.

## Quick Start

1. Configure the work area and mode.
2. Put breeding food for creatures in the area into the 2x2 input.
3. Supply FE and Life Fluid, then select a `1-32x` speed.
4. Existing item entities such as eggs are collected into the 4x2 output. Items that do not fit remain in the world.

## Modes

| Mode | Behavior |
|------|----------|
| Breeding and Growth | Pairs creatures while advancing babies and adult cooldowns |
| Breeding Only | Breeds only adults that can currently mate |
| Growth Only | Advances growth and cooldowns without consuming feed to breed |

A Villager pair needs 24 food points, such as six Bread or 24 Carrots, Potatoes, or Beetroot; mixed food works. No additional empty bed is required.

## Costs and Upgrades

- Life Fluid follows the biological ticks actually skipped. Defaults charge 10 mB per 20 growth ticks and about 3000 mB per breeding pair.
- Overclock completes the remaining baby age or adult cooldown for each processed creature and charges for the time actually skipped.
- Creative includes Overclock and removes FE and Life Fluid costs.
- Capacity, Fluid, Range, Filter, Overclock, and Creative Upgrades are supported, together with redstone control and absolute-side auto I/O.
- Filter slots use spawn eggs: allowlist processes only listed types, denylist excludes them, and an empty filter allows every compatible creature.

## Compatibility and Limits

- Modded creatures using Minecraft's standard `Animal` and `AgeableMob` breeding/growth APIs work automatically; their own sex, breed, and genetics rules remain authoritative.
- Villagers use a dedicated public-API adapter. Fully custom entities outside the standard systems are not force-bred.
- Only loaded chunks are queried, with separate budgets for scanning, pairing, growth, and item collection.
- Breeding pauses at 64 creatures of one type by default to prevent unbounded growth. Costs and budgets are server-configurable.

## Crafting

<RecipeFor id="jdte:life_breeder" />
