---
navigation:
  title: Mineral Extractor
  icon: "jdte:mineral_extractor"
  position: 30
item_ids:
  - jdte:mineral_extractor
  - jdte:large_mineral_extractor
  - jdte:mineral_survey
---

# Mineral Extractor

<BlockImage id="jdte:mineral_extractor" scale="2" />

The Mineral Extractor produces ore blocks from normalized estimated weights derived from a biome's standard ore world-generation settings. It only queries the server mineral index built during data-pack loading; it does not scan chunks or generate new ones.

## Mineral Surveys

1. Hold a blank Mineral Survey and right-click in a target biome to record its mineral distribution.
2. Right-click a recorded survey to inspect minerals, estimated probabilities, heights, vein sizes, and recognition confidence.
3. Insert it into the left machine slot to use its recorded biome. Without a recorded survey, the machine uses its current biome.

A data-pack reload that changes the mineral index makes old surveys stale and stops production. Record the target biome again so changed generation settings cannot continue using outdated weights.

## Large Mineral Extractor

<ItemImage id="jdte:large_mineral_extractor" scale="2" />

The Large Mineral Extractor places as one 3×3×2 structure whose controller is at the front-center of the bottom layer. Before placement, all 18 positions must be loaded, inside the world bounds, and replaceable except for the controller position.

- It provides four Mineral Survey slots and merges candidates from every inserted survey. Weights for the same mineral are added together across surveys.
- The machine uses its local biome only when all four survey slots are empty. Any inserted stale survey stops the entire machine.
- Base work accumulation is four times that of the standard Mineral Extractor; multiplier, resource, filtering, smelting, and paged-output rules are otherwise shared.
- FE, fluid, and item capabilities are available from the controller and every structure part; auto I/O still operates only across the outer boundary.
- Breaking the controller or any structure part dismantles the whole machine and returns one Large Mineral Extractor.

## Production and Resources

| Property | Default behavior |
|----------|------------------|
| Base processing | 1x uses the former 64x throughput as its baseline: 64 work is added each tick, and each 20 work completes and immediately settles one batch |
| Energy | 5,000 FE per batch; 10,000 FE per batch with JDT's Smelter Upgrade installed |
| Smelting | Accepts at most one JDT Smelter Upgrade; ores with furnace recipes become their smelted results, while ores without recipes remain unchanged |
| Experience Fluid | 25 mB per batch raises that batch's yield by 100%; remaining batches retain normal yield |
| Time Fluid | 5 mB per accelerated batch; the base 1x batch is free |
| Speed | 1x equals the former 64x throughput; adjustable up to 32x without Overclock and 64x with Overclock or Creative |
| Outputs | 16 slots by default; each Capacity Upgrade adds 16, up to 64 |
| Per-slot limit | 64, then 2048/4096/8192 with one/two/three Capacity Upgrades |

The machine settles as much queued work as the current output space can hold and preserves the remainder. Completely full outputs, insufficient energy, no candidates, or stale surveys do not charge resources or spill items. High multipliers use weighted batch settlement whose cost scales with mineral types instead of executing each production cycle separately.

## Filtering and Automation

- Supports Capacity, Fluid, Filter, Overclock, and Creative Upgrades, plus at most one JDT Smelter Upgrade.
- Smelting results are cached when the mineral source, filters, or upgrade state changes; settlements do not query recipes per cycle.
- Allowlist and denylist entries match final ore blocks; remaining mineral weights are normalized again after filtering.
- Auto I/O and pipes can supply Mineral Surveys, Experience Fluid, Time Fluid, and FE, and extract products from paged outputs.
- Modpacks can describe custom world-generation features through `data/<namespace>/jdte/mineral_sources/*.json` when automatic analysis cannot recognize them.

## Crafting

### Mineral Survey

<RecipeFor id="jdte:mineral_survey" />

### Mineral Extractor

<RecipeFor id="jdte:mineral_extractor" />

### Large Mineral Extractor

<RecipeFor id="jdte:large_mineral_extractor" />