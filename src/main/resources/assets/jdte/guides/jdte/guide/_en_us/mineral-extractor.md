---
navigation:
  title: Mineral Extractor
  icon: "jdte:mineral_extractor"
  position: 30
item_ids:
  - jdte:mineral_extractor
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

## Production and Resources

| Property | Default behavior |
|----------|------------------|
| Base processing | Accumulates one batch per 20 ticks and settles every 20 ticks |
| Energy | 5,000 FE per batch |
| Experience Fluid | 25 mB per batch raises that batch's yield by 100%; remaining batches retain normal yield |
| Time Fluid | 5 mB per accelerated batch; the base 1x batch is free |
| Speed | Adjustable 1-32x; Overclock or Creative locks it to 1024x |
| Outputs | 16 slots by default; each Capacity Upgrade adds 16, up to 64 |

The machine simulates the complete output before committing a settlement. Full outputs, insufficient energy, no candidates, or stale surveys do not charge resources or spill items. High multipliers use weighted batch settlement whose cost scales with mineral types instead of running 1,024 production loops.

## Filtering and Automation

- Supports Capacity, Fluid, Filter, Overclock, and Creative Upgrades.
- Allowlist and denylist entries match final ore blocks; remaining mineral weights are normalized again after filtering.
- Auto I/O and pipes can supply Mineral Surveys, Experience Fluid, Time Fluid, and FE, and extract products from paged outputs.
- Modpacks can describe custom world-generation features through `data/<namespace>/jdte/mineral_sources/*.json` when automatic analysis cannot recognize them.

## Crafting

<RecipeFor id="jdte:mineral_extractor" />