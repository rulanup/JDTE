---
navigation:
  title: Infusion Machine
  icon: "jdte:advanced_infusion_machine"
  position: 15
item_ids:
  - jdte:advanced_infusion_machine
  - jdte:extended_infusion_machine
  - jdte:life_apple
  - jdte:wither_spawn_egg
  - jdte:ender_dragon_spawn_egg
---

# Infusion Machine

The Infusion Machine consumes items and fluids to produce new items according to infusion recipes. It is the primary consumer of Life Fluid, combining ordinary items with Life Fluid to create special products.

## Variants

| Machine | Upgrade Slots | Energy Capacity | Fluid Capacity |
|---------|---------------|-----------------|----------------|
| Advanced Infusion Machine | 4 | 50,000 FE | 8,000 mB |
| Extended Infusion Machine | 8 | 100,000 FE | 8,000 mB |

## Features

### Workflow

1. Place the input item in the left input slot
2. Inject fluid (e.g. Life Fluid) into the internal tank
3. The machine automatically matches an infusion recipe and starts processing
4. The result appears in the right output slot when complete

### Slots

- Input slot: 1, for the item to be infused
- Output slot: 1, for the infusion product

### Processing Parameters

- Processing time: 20 ticks (1 second)
- Base energy cost: 500 FE per operation
- Accepts any fluid type; the recipe determines the required fluid

### Example Infusion Recipe

| Input | Fluid | Output | Energy |
|-------|-------|--------|--------|
| Apple | Life Fluid 1000 mB | Life Apple | 500 FE |
| Wither Essence | Life Fluid 64,000 mB | Wither Spawn Egg | 100,000 FE |
| Ender Dragon Essence | Life Fluid 64,000 mB | Ender Dragon Spawn Egg | 100,000 FE |

### Life Apple Progression

Life Apples immediately heal 20 health and permanently strengthen the player with no consumption cap. Progress survives death, relogging, and dimension changes.

| Condition | Maximum Health | Armor | Armor Toughness |
|-----------|----------------|-------|-----------------|
| Each apple | +0.01% | +0.01 | +0.01 |
| Every 10 | — | Extra +0.05 | Extra +0.05 |
| Every 50 | Extra +0.1% | — | — |
| Every 100 | Extra +0.2% | — | — |
| Every 1000 | Extra +100% | — | — |

All milestones repeat at every multiple, so 2000 apples grant the 1000-apple reward twice.

| Example | Maximum Health | Armor | Toughness | Life Fluid | Energy |
|---------|----------------|-------|-----------|------------|--------|
| 1000 apples | +114% | +15 | +15 | 1000 B | 500,000 FE |
| At least +10 armor (670) | Formula result | +10.05 | +10.05 | 670 B | 335,000 FE |
| At least +100 armor (6670) | Formula result | +100.05 | +100.05 | 6670 B | 3,335,000 FE |

### Universal Spawn Egg Infusion

The machine automatically discovers registered spawn eggs and entity loot tables from Minecraft and other mods. Infusing one full stack of a stackable mob drop with 64,000 mB (64 B) of Life Fluid and 100,000 FE creates the matching mob's spawn egg.

- The input count is the item's own maximum stack size, usually 64
- A recipe is available only when the drop uniquely identifies one spawn egg; shared drops are not guessed
- The tank must hold at least 64,000 mB, requiring at least three combined Capacity or Fluid Upgrade doublings
- Overclock, Underclock, and Creative Upgrades still modify or remove the 100,000 FE base cost
- JEI lists recipes discovered from the current resource packs, data packs, and loaded mods

### Upgrade Effects

| Upgrade | Effect |
|---------|--------|
| Overclock | Locks to 1 tick operation, 3x energy cost |
| Underclock | Locks to 40 tick operation, 20% energy cost |
| Creative | No energy cost, includes overclock effect |
| Capacity | Increases FE and fluid capacity |
| Fluid | Increases fluid capacity only |

## Crafting

### Advanced Infusion Machine

<BlockImage id="jdte:advanced_infusion_machine" scale="2" />

<RecipeFor id="jdte:advanced_infusion_machine" />

### Extended Infusion Machine

<BlockImage id="jdte:extended_infusion_machine" scale="2" />

An extended version of the Advanced Infusion Machine with 8 upgrade slots. Obtained by right-clicking an Advanced Infusion Machine with an Extended Upgrade.

<RecipeFor id="jdte:extended_infusion_machine" />
