---
navigation:
  title: Loot Fabricator
  icon: "jdte:loot_fabricator"
  position: 18
item_ids:
  - jdte:loot_fabricator
---

# Loot Fabricator

<BlockImage id="jdte:loot_fabricator" scale="2" />

The Loot Fabricator uses spawn eggs as reusable entity templates. It consumes Life Fluid, Time Fluid, and energy, then rolls the entity's currently loaded vanilla or modded loot table. Spawn eggs are not consumed and each of the four input slots holds at most one egg.

| Property | Base value |
|----------|------------|
| Template slots | Four spawn egg slots processed in parallel |
| Output slots | 16; each Capacity Upgrade unlocks another 16 |
| Cost per share | 100 mB Life Fluid, 5,000 FE, and at least 1 mB Time Fluid |
| Boss multiplier | Wither/Elder Guardian 10x, Ender Dragon 100x |

It starts with 16 output slots. Each Capacity Upgrade unlocks another 16-slot page, up to four pages. Its standard eight-slot upgrade area has one four-slot column on each side and accepts Capacity, Overclock, Underclock, Fluid, Creative, and up to three Looting Upgrades.

Looting Upgrades affect fabrication results like they do for Bio Crushers: each level uses the `lootingExtraDropChance` config to try one extra copy of the base drop list for that roll, defaulting to 50% per level. Installed Looting Upgrades also increase Life Fluid and Time Fluid costs by 50% per card by default, configurable with `lootingFluidCostIncreasePercent`.

All four spawn-egg inputs run in parallel on the same progress bar. When multiple eggs are present, the machine first checks that Life Fluid, Time Fluid, and FE can cover every participating input slot. On completion, each successful loot output consumes one full cost share, so two successful eggs consume 2x fluid and FE, and four consume 4x.

By default, each successful output share costs 100 mB of Life Fluid, 5,000 FE, and at least 1 mB of Time Fluid. Reducing the processing time below 20 ticks with the speed button increases Time Fluid consumption proportionally, reaching 20 mB per share at one tick. The Life Fluid cost and base Time Fluid cost are both integer server-config values. If a loot-table roll produces no items, that share does not consume fluid or FE; if every roll is empty, the machine resets progress.

Boss spawn eggs have higher fluid costs: Wither and Elder Guardian eggs consume 10x Life Fluid and Time Fluid, while Ender Dragon eggs consume 100x Life Fluid and Time Fluid. Wither eggs fabricate the Wither's core loot, including the Nether Star.

Redstone control and automatic I/O are supported. Automation inserts only spawn eggs and matching fluids and extracts only from unlocked output slots.

## Crafting

<RecipeFor id="jdte:loot_fabricator" />
