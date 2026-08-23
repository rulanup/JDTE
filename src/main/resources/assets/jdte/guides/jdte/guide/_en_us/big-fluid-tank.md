---
navigation:
  title: Big Fluid Tank
  icon: "jdte:big_fluid_tank"
  position: 6.6
item_ids:
  - jdte:big_fluid_tank
  - jdte:large_pocket_generator
  - jdte:large_potion_canister
  - jdte:large_fuel_canister
---

# Big Fluid Tank

<ItemImage id="jdte:big_fluid_tank" scale="2" />

The Big Fluid Tank is a 1000 B version of JDT's Fluid Canister. It stores one fluid up to **1,000,000 mB**, works while held, and can also be equipped in the dedicated Curios `big_fluid_tank` slot.

## World interaction

- Normal right-click on a fluid source: collect 1000 mB first; if collection is impossible, try placing the stored fluid.
- Sneak-right-click a block: directly try to place 1000 mB.
- Sneak-right-click air: cycle the automatic filling mode.

The tank can only contain one fluid type at a time and respects vanilla placement, container, and dimension rules.

## Automatic filling modes

While the tank is in the player's inventory or Curios slot, it can feed its fluid into other compatible fluid items. Each inventory check transfers at most 100 mB to one item.

- **None**: do not fill items automatically.
- **JDT**: fill Just Dire Things items only.
- **JDTE**: fill JDT Extras items only.
- **All**: fill every item with a compatible fluid capability.

## Related portable containers

- `jdte:large_pocket_generator`: a 4x-capacity pocket generator with its own Curios slot and hotkey-opened menu.
- `jdte:large_potion_canister`: a 4,000 mB potion canister that consumes 4 matching potions per 1,000 mB batch.
- `jdte:large_fuel_canister`: a 40,000,000-fuel canister whose minimum consumption and burn multiplier are both scaled by 10.

## Crafting

<RecipeFor id="jdte:big_fluid_tank" />
