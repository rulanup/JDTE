---
navigation:
  title: Bio Crusher
  icon: "jdte:advanced_bio_crusher"
  position: 5
---

# Bio Crusher

The Bio Crusher is a machine that kills mobs in its range and generates drops and experience fluid.

## Variants

| Machine | Standard Upgrade Slots | Dedicated Upgrade Slots |
|---------|------------------------|-------------------------|
| Advanced Bio Crusher | 4 | 2 |
| Extended Bio Crusher | 8 | 2 |

## Features

### Drop Generation

- Kills mobs in configured range
- Generates vanilla drops based on entity loot tables
- Supports looting upgrades for extra drops

### Experience Fluid

- Produces life fluid based on mob health
- Fluid stored in internal tank

### Dedicated Upgrades

The Bio Crusher has 2 dedicated upgrade slots that support:

#### Looting Upgrade

- Max level: 6
- Each level adds 50% chance for extra drop
- Level 1: 50% chance for +1 drop
- Level 2: 100% chance for +1 drop
- Level 3: 100% + 50% for +1 more
- And so on...

#### Sharpness Upgrade

- Max count: 6
- Base damage: 5 points
- Each upgrade adds 5 damage
- Max damage: 35 points

### Spawner Integration

Place the Bio Crusher above a mob spawner to:

- Prevent the spawner from spawning mobs
- Generate drops directly from the spawner's entity type
- Generate experience fluid

This is useful for automating mob farms without creating lag from many entities.

## Modes

The Bio Crusher supports 3 modes:

- **Hostile**: Only targets hostile mobs (default)
- **Friendly**: Only targets friendly mobs
- **All**: Targets all mobs

## Energy

- Base energy cost: 300 FE per operation
- Advanced Bio Crusher: 100,000 FE capacity
- Extended Bio Crusher: 200,000 FE capacity

## Crafting

### Advanced Bio Crusher

```
ABA
CDC
ABA
```

- A: Diamond
- B: Gold Ingot
- C: Iron Ingot
- D: Nether Star

### Extended Bio Crusher

```
ABA
CDC
ABA
```

- A: Diamond
- B: Netherite Ingot
- C: Ender Pearl
- D: Advanced Bio Crusher
