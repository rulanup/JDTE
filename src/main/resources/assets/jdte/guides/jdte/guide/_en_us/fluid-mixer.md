---
navigation:
  title: Fluid Mixer
  icon: "jdte:advanced_fluid_mixer"
  position: 15.5
item_ids:
  - jdte:advanced_fluid_mixer
  - jdte:extended_fluid_mixer
  - jdte:mixing_upgrade
---

# Fluid Mixer

The Fluid Mixer consumes FE to mix two input fluids (Tank A and Tank B) along with an optional item catalyst, outputting a new target fluid.

It natively supports custom datapack recipes (`jdte:fluid_mixer`). When the Mekanism mod is installed, it also automatically discovers and supports Chemical Infuser and Pigment Mixer recipes.

## Variants

| Machine | Standard Upgrade Slots | Dedicated Upgrade Slots | Energy Capacity | Fluid Tank Capacity |
|---------|------------------------|-------------------------|-----------------|---------------------|
| Advanced Fluid Mixer | 4 | 1 (Mixing Upgrade) | 100,000 FE | 3 × 8,000 mB |
| Extended Fluid Mixer | 8 | 1 (Mixing Upgrade) | 200,000 FE | 3 × 8,000 mB |

## Features & Layout

### Tank & Slot Structure

- **Input Tank A (Left)**: Base capacity of 8,000 mB for the first recipe fluid.
- **Input Tank B (Right)**: Base capacity of 8,000 mB for the second recipe fluid.
- **Item Catalyst Slot (Center)**: 1 slot for an optional item catalyst (such as a cactus for the Time Fluid recipe).
- **Output Tank (Bottom Center)**: Base capacity of 8,000 mB for the mixed output fluid. Does not accept external insertion; fluids can only be extracted or sent via Auto I/O.

### Workflow

1. The machine constantly monitors Tank A, Tank B, and the catalyst slot to match a valid fluid mixing recipe.
2. When ingredients and FE requirements are satisfied, processing begins. Upon completion, ingredients are consumed and output fluid is deposited.
3. Supports absolute-direction Auto I/O to pull fluids/items from or push fluids to adjacent inventories.
4. Supports redstone control (Ignored, Low, High).

## Mixing Upgrade & Parallel Processing

The Fluid Mixer features a dedicated upgrade slot that accepts the **Mixing Upgrade** (limit: 1).

- **Uninstalled**: Processes 1 standard recipe batch per cycle.
- **Installed**: Enables **Parallel Mixing Mode**. The machine calculates the maximum possible batches based on current inputs, remaining output tank space, and available FE, processing all ingredients in a single cycle.

## Upgrade Effects

| Upgrade | Effect |
|---------|--------|
| Capacity | Doubles FE energy capacity and all 3 fluid tanks' capacity (max 3) |
| Fluid | Doubles all 3 fluid tanks' capacity only (max 3) |
| Overclock | Locks speed to 1 tick, runs twice per tick, triples energy cost |
| Underclock | Locks speed to 40 ticks, reduces energy cost to 20% |
| Creative | Eliminates FE cost and applies Overclock speed |
| Mixing | Dedicated slot (max 1), enables parallel batch mixing |
| AE Crafting Read | Only operates when a bound AE2 network has active crafting jobs |
| AE Output | Automatically exports outputs to a bound AE2 network |

## Built-in Recipe Example: Time Fluid

| Input Fluid A | Input Fluid B | Item Catalyst | Output Fluid | Process Ticks | Energy |
|---------------|---------------|---------------|--------------|---------------|--------|
| Life Fluid 500 mB | Milk 500 mB | Cactus ×1 | Time Fluid 250 mB | 100 ticks (5s) | 5,000 FE |

## Mekanism Integration

When the **Mekanism** mod is present, the Fluid Mixer automatically supports:

- **Chemical Infuser Recipes**: Maps gas/fluid infusion reactions directly into the mixer.
- **Pigment Mixer Recipes**: Enables high-efficiency dye and pigment combining.

## Crafting

### Advanced Fluid Mixer

<BlockImage id="jdte:advanced_fluid_mixer" scale="2" />

<RecipeFor id="jdte:advanced_fluid_mixer" />

### Extended Fluid Mixer

<BlockImage id="jdte:extended_fluid_mixer" scale="2" />

Extended variant of the Advanced Fluid Mixer with 8 upgrade slots. Can be upgraded from an Advanced Fluid Mixer by right-clicking with an Extended Upgrade.

<RecipeFor id="jdte:extended_fluid_mixer" />

### Mixing Upgrade

<ItemImage id="jdte:mixing_upgrade" scale="2" />

<RecipeFor id="jdte:mixing_upgrade" />
