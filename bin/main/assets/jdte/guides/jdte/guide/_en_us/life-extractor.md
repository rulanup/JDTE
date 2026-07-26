---
navigation:
  title: Life Extractor
  icon: "jdte:advanced_life_extractor"
  position: 14
item_ids:
  - jdte:advanced_life_extractor
  - jdte:extended_life_extractor
  - jdte:life_fluid_bucket
---

# Life Extractor

Advanced and Extended Life Extractors cannot be destroyed by explosions, but can still be mined normally by players.

The Life Extractor kills mobs within its configured range and produces Life Fluid based on their current remaining health. Unlike the Bio Crusher, the Life Extractor does not produce drops — it converts the mob's life force directly into fluid.

## Variants

| Machine | Upgrade Slots | Energy Capacity | Fluid Capacity |
|---------|---------------|-----------------|----------------|
| Advanced Life Extractor | 4 | 100,000 FE | 16,000 mB |
| Extended Life Extractor | 8 | 200,000 FE | 16,000 mB |

## Features

### Life Fluid Generation

- Kills mobs within the configured range
- Produces Life Fluid from current remaining health: 0.1 mB per HP by default
- The server config `jdte.lifeExtractor.fluidPerHealth` controls the amount per HP
- The first 100 health uses the full rate. Each subsequent 100-health band uses 10% less marginal yield than the previous band by default
- `jdte.lifeExtractor.highHealthLossPercent` controls this marginal loss. Piecewise calculation prevents total output from dropping at a band boundary
- Targets are removed directly without normal death drops, experience, or loot events
- Fractional output from entity health and upgrade multipliers accumulates in the machine until it reaches 1 mB

### Low-Lag Batch Processing

| Machine / Mode | Scan Interval | Maximum Batch | Theoretical Throughput |
|----------------|---------------|---------------|------------------------|
| Advanced Normal | 20 ticks | 4 | 4 entities/s |
| Extended Normal | 20 ticks | 8 | 8 entities/s |
| Advanced Overclock/Creative | 5 ticks | 8 | 32 entities/s |
| Extended Overclock/Creative | 5 ticks | 16 | 64 entities/s |

Each range query processes several targets at once. Overclock scanning is limited to four queries per second instead of scanning a large area every tick. Underclock uses the normal batch size with a 40-tick interval.
- Fluid is stored in the internal tank and can be extracted with a bucket

### Modes

The Life Extractor supports 3 modes:

- **Hostile**: Only attacks hostile mobs (default)
- **Passive**: Only attacks passive mobs
- **All**: Attacks all mobs

When a Filter Upgrade is installed, the mode setting is ignored and the filter slots are used to select target entities instead.

### Range

- Base radius: 5 blocks (X/Y/Z each)
- Adjustable via the GUI
- Range Upgrade increases the configurable maximum
- Energy cost scales with the configured range

### Upgrade Effects

| Upgrade | Effect |
|---------|--------|
| Overclock | Processes a mob batch every 5 ticks, with 10% fluid loss |
| Underclock | Fluid yield increased to 1.5x |
| Creative | Uses the overclock batch rate, with no fluid loss or energy cost |
| Range | Increases configurable range maximum |
| Filter | Enables filter slots to select target entities by type |
| Capacity | Increases FE and fluid capacity |

## Energy

- Base energy cost: 300 FE x range scale factor
- Advanced Life Extractor: 100,000 FE capacity
- Extended Life Extractor: 200,000 FE capacity

## Crafting

### Advanced Life Extractor

<BlockImage id="jdte:advanced_life_extractor" scale="2" />

<RecipeFor id="jdte:advanced_life_extractor" />

### Extended Life Extractor

<BlockImage id="jdte:extended_life_extractor" scale="2" />

An extended version of the Advanced Life Extractor with 8 upgrade slots. Obtained by right-clicking an Advanced Life Extractor with an Extended Upgrade.

<RecipeFor id="jdte:extended_life_extractor" />
