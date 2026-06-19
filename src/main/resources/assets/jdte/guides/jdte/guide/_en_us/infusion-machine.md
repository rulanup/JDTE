---
navigation:
  title: Infusion Machine
  icon: "jdte:advanced_infusion_machine"
  position: 15
item_ids:
  - jdte:advanced_infusion_machine
  - jdte:extended_infusion_machine
  - jdte:life_apple
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
