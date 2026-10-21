---
navigation:
  title: Crafting Patterns and Essence Conversion
  icon: "jdte:crafting_pattern"
  position: 39
item_ids:
  - jdte:crafting_pattern
  - jdte:advanced_essence_conversion_upgrade
---

# Crafting Patterns and Essence Conversion

<ItemImage id="jdte:crafting_pattern" scale="2" />

Right-click a Crafting Pattern to open its **3×3 ingredient grid and result preview**. Click a ghost slot with a carried ingredient, or shift-click an inventory item, to copy a sample. Shift-click a ghost slot to clear it. Samples are not consumed and preview results cannot be taken.

The screen indicates recipe validity. **Save Recipe** validates the current ordinary crafting-table recipe on the server before writing the pattern. **Clear Pattern** removes the stored recipe.

<RecipeFor id="jdte:crafting_pattern" />

## Binding in an anvil

Place **one Essence Conversion Upgrade** on the left and **one recorded Crafting Pattern** on the right. Binding consumes the pattern and **one experience level**. Duplicate recipes, invalid patterns and full cards produce no result.

- Normal Essence Conversion Upgrade: **one pattern**.
- Advanced Essence Conversion Upgrade: **three patterns**.
- Both tiers share the **one card per machine** limit and support the Greenhouse and Large Greenhouse.

<ItemImage id="jdte:advanced_essence_conversion_upgrade" scale="2" />

Smith a **Netherite Upgrade Smithing Template + normal Essence Conversion Upgrade + diamond** to obtain the advanced card. Existing bound patterns are retained.

<RecipeFor id="jdte:advanced_essence_conversion_upgrade" />

## Crafting and export

Recipes sharing ingredients run round-robin by craft count; independent ingredient groups craft separately. Rotation persists with the machine. Other essences retain unambiguous essence-only conversion. Ingredients match the recorded items and components; all ingredients must be present in the output buffer. Crafting respects input/output counts and crafting remainders. Insufficient output space leaves inputs untouched.

Conversion runs before every AE export. Incomplete ingredient batches remain buffered for later harvests; completed products export normally. Recipes are revalidated after reload and invalid patterns stop crafting.

Item tooltips list pattern counts, ingredients and results. Jade shows installed cards, pattern counts, each result and its recipe validity.

Remove the conversion card to withdraw reserved ingredients manually.
