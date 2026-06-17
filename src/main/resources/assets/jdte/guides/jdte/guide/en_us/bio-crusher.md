---
navigation:
  title: Bio Crusher
  icon: "jdte:advanced_bio_crusher"
  position: 5
item_ids:
  - jdte:advanced_bio_crusher
  - jdte:extended_bio_crusher
  - jdte:looting_upgrade
  - jdte:sharpness_upgrade
---

# Bio Crusher

The Bio Crusher kills mobs within its configured range, producing drops and experience fluid.

## Variants

| Machine | Standard Upgrade Slots | Dedicated Upgrade Slots |
|---------|----------------------|------------------------|
| Advanced Bio Crusher | 4 | 2 |
| Extended Bio Crusher | 8 | 2 |

## Features

### Drop Generation

- Kills mobs within the configured range
- Generates vanilla drops based on mob loot tables
- Supports Looting Upgrade for extra drops

### Experience Fluid

- Produces experience fluid based on mob health
- Fluid is stored in the internal tank

### Dedicated Upgrade Slots

The Bio Crusher has 2 dedicated upgrade slots supporting:

#### Looting Upgrade

- Max level: 6
- Each level adds 50% chance for an extra drop
- Level 1: 50% chance for +1 drop
- Level 2: 100% chance for +1 drop
- Level 3: 100% + 50% chance for another +1
- And so on...

#### Sharpness Upgrade

- Max quantity: 6
- Base damage: 5 points
- Each upgrade adds 5 damage points
- Max damage: 35 points

### Spawner Integration

Placing a Bio Crusher above a monster spawner will:

- Prevent the spawner from spawning mobs
- Generate drops directly from the spawner's entity type
- Produce experience fluid

This helps automate mob farms while avoiding lag from large numbers of entities.

## Modes

The Bio Crusher supports 3 modes:

- **Hostile**: Only attacks hostile mobs (default)
- **Passive**: Only attacks passive mobs
- **All**: Attacks all mobs

## Energy

- Base energy consumption: 300 FE per operation
- Advanced Bio Crusher: 100,000 FE capacity
- Extended Bio Crusher: 200,000 FE capacity

## Crafting

### Advanced Bio Crusher

<BlockImage id="jdte:advanced_bio_crusher" scale="2" />

<RecipeFor id="jdte:advanced_bio_crusher" />

### Extended Bio Crusher

<BlockImage id="jdte:extended_bio_crusher" scale="2" />

An extended version of the Advanced Bio Crusher with 8 upgrade slots. Obtained by right-clicking an Advanced Bio Crusher with an Extended Upgrade.

<RecipeFor id="jdte:extended_bio_crusher" />
