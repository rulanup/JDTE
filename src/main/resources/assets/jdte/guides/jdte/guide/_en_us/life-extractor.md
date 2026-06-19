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

The Life Extractor kills mobs within its configured range and produces Life Fluid based on their max health. Unlike the Bio Crusher, the Life Extractor does not produce drops — it converts the mob's life force directly into fluid.

## Variants

| Machine | Upgrade Slots | Energy Capacity | Fluid Capacity |
|---------|---------------|-----------------|----------------|
| Advanced Life Extractor | 4 | 100,000 FE | 16,000 mB |
| Extended Life Extractor | 8 | 200,000 FE | 16,000 mB |

## Features

### Life Fluid Generation

- Kills mobs within the configured range
- Produces Life Fluid based on the mob's max health: 100 mB per HP
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
| Overclock | Processes 2 mobs per tick, but with 10% fluid loss |
| Underclock | Fluid yield increased to 1.5x |
| Creative | Processes 2 mobs per tick, no fluid loss, no energy cost |
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
