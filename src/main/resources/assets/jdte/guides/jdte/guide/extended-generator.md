---
navigation:
  title: Extended Generators
  icon: "jdte:extended_generator"
  position: 6.64
item_ids:
  - jdte:extended_generator
  - jdte:extended_fluid_generator
---

# Extended Generators

<ItemImage id="jdte:extended_generator" scale="2" />
<ItemImage id="jdte:extended_fluid_generator" scale="2" />

These upgrade the native JDT solid and fluid generators to **eight upgrade slots**. Use Extended Upgrade on the placed generator, or craft it with the generator. Both methods retain inventory, energy, fluid, upgrades and paid generation progress. The Eclipse Alloy Wrench packs the machine for transport.

The solid generator burns furnace fuel, including the normal and Large Fuel Canisters. The fluid generator accepts JDT **Refined Fuel** fluids and supported fuel containers. Its tank can also be filled and drained with pipes. Other liquids, such as water, are rejected. Both export FE to neighboring receivers on all six sides, using the native configured per-face limits. A full energy buffer stops spending fuel. Capacity upgrades cannot be removed while their stored FE or fluid would exceed the smaller buffer; drain or spend the excess first.

Generator Upgrade spends two solid fuel inputs or two mB of fluid per upgraded batch, producing three times the native fuel credit. Capacity and Fluid upgrades enlarge their corresponding real buffers. Ultimate Capacity multiplies capacity by 50; Ultimate Overclock executes the machine ten times per tick. Overclock and Underclock cannot be combined with Ultimate Overclock. Filter upgrades require a filtering machine and cannot be installed here.

Redstone controls new fuel generation. Already purchased solid-fuel work can finish when redstone is disabled, following native behavior. Stored FE can still be exported while generation is disabled.

## Crafting

<RecipeFor id="jdte:extended_generator" />
<RecipeFor id="jdte:extended_fluid_generator" />
