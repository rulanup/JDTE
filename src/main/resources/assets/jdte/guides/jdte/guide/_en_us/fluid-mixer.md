---
navigation:
  title: Fluid Mixers
  icon: "jdte:advanced_fluid_mixer"
  position: 36
item_ids:
  - jdte:advanced_fluid_mixer
  - jdte:extended_fluid_mixer
  - jdte:mixing_upgrade
  - jdte:ultimate_mixing_parallel_upgrade
---

# Fluid Mixers

The Advanced Fluid Mixer stores 100,000 FE and has four upgrade slots. The Extended tier stores 200,000 FE and has eight slots. Both have two 8,000 mB input tanks, one 8,000 mB product tank and an optional consumable catalyst slot. Capacity/Fluid upgrades apply to all three tanks. The Extended Upgrade converts the Advanced tier while preserving its machine data.

Input order is interchangeable. Single-fluid recipes require the unused input tank to be empty. External fluid fills enter the matching or empty input tank; external drains extract only product. Auto I/O routes the catalyst inward and product fluid outward. AE Output exports only product fluid, and AE Crafting Read gates normal and accelerated work.

The built-in recipe consumes 1,000 mB water, one Time Fluid Catalyst and 5,000 FE for 250 mB Time Fluid after 100 ticks. Recipes are data-driven and synchronized to clients. Optional Mekanism integration adapts Chemical Infuser recipes only where each gas has a same-ID registered fluid equivalent; gas-only chemicals are not invented as fluids.

## Parallel and speed upgrades

Without a Mixing Upgrade, each completion performs one batch. Mixing Upgrade allows up to eight batches per completion; Ultimate Mixing Parallel removes that upgrade limit. Actual input fluids, catalyst count, FE and output room still bound each batch. The two mixing cards cannot coexist.

Overclock completes a recipe in one tick, uses three times the FE per batch and runs twice per server tick. Underclock doubles recipe duration and uses 20% of the FE. Creative removes input and FE consumption while retaining the source matching rules. Ultimate Overclock completes recipes in one tick, runs ten times per server tick and uses fifty times the FE. Ultimate Capacity multiplies base FE/fluid capacity by fifty before ordinary capacity multipliers.

Fluid quantities and processing duration use split network values to retain counts above 32,767/65,535. Tank and arrow rendering clamps to the visible bar. On capacity removal, all tanks follow the machine's ordinary capacity-clamping rules.

<RecipeFor id="jdte:advanced_fluid_mixer" />
<RecipeFor id="jdte:extended_fluid_mixer" />
<RecipeFor id="jdte:mixing_upgrade" />
<RecipeFor id="jdte:ultimate_mixing_parallel_upgrade" />
