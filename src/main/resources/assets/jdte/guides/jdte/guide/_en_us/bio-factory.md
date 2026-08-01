---
navigation:
  title: Bio Factory
  icon: "jdte:bio_factory"
  position: 20
item_ids:
  - jdte:bio_factory
---

# Bio Factory

<BlockImage id="jdte:bio_factory" scale="2" />

The Bio Factory uses a reusable spawn egg or Productive Bees cage as a specimen and converts materials, fluids, and FE into biological products. The displayed creature is client-only; no real entity is spawned or ticked.

## Quick Start

1. Put a spawn egg or filled bee cage in the specimen slot.
2. Follow JEI and place food, flowering inputs, or consumed materials in the three unordered material slots.
3. Supply FE. Life Fluid raises yield, Time Fluid enables speed control, and culture fluid serves fluid-flowering recipes.
4. Extract item products and product fluid through their separate outputs.

## Slots and Resources

| Area | Purpose |
|------|---------|
| One specimen slot | Reusable spawn egg or bee cage |
| Three material slots | Unordered; each recipe decides whether an input is consumed |
| Eight output slots | Each Capacity Upgrade adds eight, up to 32 |
| Life Fluid | Doubles cycle yield by default when sufficient |
| Time Fluid | Enables `1-32x`; Overclock or Creative uses 64x |
| Culture/product fluids | Culture fluid is an input; milk and other fluid products have a separate output |

The machine has a fixed 5x base work rate without increasing cycle costs. FE, fluids, and consumed materials are charged only when every product fits. It still runs at base yield and speed without Life or Time Fluid.

## Recipe Compatibility

- Built-in recipes cover common animal products, all wool colors, Honeycomb, Snowballs, Froglights, Goat Horns, Suspicious Stew, Cat gifts, Panda Slimeballs, and more.
- Modpacks can add `jdte:bio_factory` recipes with up to three unordered inputs. `count: 0` is reusable; a positive count is consumed.
- Productive Bees specimens use the loaded Advanced Beehive products, exact flowering item or fluid, environmental conditions, and Productivity gene.
- Alpha, Beta, Gamma, and Omega Productivity Upgrades share a four-card limit; Omega can produce comb blocks.
- JDTE's Life Fluid Bee flowers on an Advanced or Extended Life Extractor, and its comb centrifuges into Life Fluid plus Wax.

JEI displays exact inputs, chance products, fluids, FE, and processing time.

## Upgrades and Automation

- Supports Capacity, Fluid, Overclock, Creative, and recipe-specific dedicated upgrades.
- Built-in JDTE recipes accept up to four Looting Upgrades; dynamic Productive Bees recipes do not use Looting.
- Creative includes Overclock and removes machine resource costs.
- Auto I/O keeps all three input fluids separate from product fluid so input pipes cannot drain fluid products.

## Crafting

<RecipeFor id="jdte:bio_factory" />
