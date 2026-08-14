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
| Fortune role fluid (default: JDT Experience Fluid) | Raises that batch's yield by 100%; its amount remains configured by `experienceFluidPerCycle` (25 mB by default) |
| Acceleration role fluid (default: JDT Time Fluid) | Used only for accelerated batches; its amount remains configured by `timeFluidPerAcceleratedCycle` (5 mB by default), and the base 1x batch is free |
| Speed | 1x equals the former 64x throughput; adjustable up to 32x without Overclock and 64x with Overclock or Creative |
| Outputs | 16 slots by default; each Capacity Upgrade adds 16, up to 64 |
| Per-slot limit | 64, then 2048/4096/8192 with one/two/three Capacity Upgrades |

The machine settles as much queued work as the current output space can hold and preserves the remainder. Completely full outputs, insufficient energy, no candidates, or stale surveys do not charge resources or spill items. High multipliers use weighted batch settlement whose cost scales with mineral types instead of executing each production cycle separately.

## KubeJS Resource Fluids and Reloads

The standard and Large Mineral Extractors share one fixed resource recipe: `jdte:mineral_extractor_resources`. A KubeJS replacement must remove and recreate that same ID and use the same custom recipe type:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'jdte:mineral_extractor_resources' })
  event.custom({
    type: 'jdte:mineral_extractor_resources',
    fortune_fluid: 'minecraft:lava',
    acceleration_fluid: 'minecraft:water'
  }).id('jdte:mineral_extractor_resources')
})
```

- `fortune_fluid` and `acceleration_fluid` must name different fluids.
- Fluid amounts still come from the existing `experienceFluidPerCycle` and `timeFluidPerAcceleratedCycle` config keys; changing the recipe changes roles, not those costs.
- Keep the fixed recipe ID. If it is absent, JDTE falls back to the old default fluids and writes a warning to the log.
- `/reload` does not delete fluid already in a machine. The old role fluid can still be extracted, but it no longer produces; after the tank is emptied, the machine accepts the newly configured role fluid.

## Filtering and Automation

- Supports Capacity, Fluid, Filter, Overclock, and Creative Upgrades, plus at most one JDT Smelter Upgrade.
- Smelting results are cached when the mineral source, filters, or upgrade state changes; settlements do not query recipes per cycle.
- Allowlist and denylist entries match final ore blocks; remaining mineral weights are normalized again after filtering.
- Auto I/O and pipes can supply Mineral Surveys, the two role fluids (default: JDT Experience Fluid and JDT Time Fluid), and FE, and extract products from paged outputs.
- Modpacks can describe custom world-generation features through `data/<namespace>/jdte/mineral_sources/*.json` when automatic analysis cannot recognize them.

## Crafting

### Mineral Survey

<RecipeFor id="jdte:mineral_survey" />

### Mineral Extractor

<RecipeFor id="jdte:mineral_extractor" />

### Large Mineral Extractor

<RecipeFor id="jdte:large_mineral_extractor" />
