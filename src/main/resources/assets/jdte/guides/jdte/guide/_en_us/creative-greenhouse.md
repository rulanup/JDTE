---
navigation:
  title: Creative Greenhouse
  icon: "jdte:creative_greenhouse"
  position: 19.6
item_ids:
  - jdte:creative_greenhouse
---

# Creative Greenhouse

<ItemImage id="jdte:creative_greenhouse" scale="2" />

The Creative Greenhouse supplies resources for creative-mode builds. It does not run the normal greenhouse production cycle: inserting a valid template immediately establishes an inexhaustible output catalog without FE, Time Fluid, fertilizer, or any other consumable.

## Templates and infinite products

The machine has four reusable seed or plant template slots. Each template uses the same crop resolution rules as a normal Greenhouse. Products with the same item and data components are merged into one catalog type.

Each catalog product is represented internally with a **`Long.MAX_VALUE`** amount. Player, pipe, and AE extraction never reduce that amount; the normal item capability still returns at most the item's legal stack size per request. Output slots are extraction-only and cannot be filled.

The screen displays 16 products per page. The base machine enables up to 16 types; each Capacity Upgrade adds 16, reaching the 64-type maximum with three cards. The screen reports overflow when the templates resolve to more types than the active limit.

## Upgrades and automation

The machine has eight standard upgrade slots and accepts Capacity, Overclock, Fortune, Seed Conversion, Essence Conversion, and AE Output upgrades.

- Capacity expands the number of active output types.
- Seed Conversion and Essence Conversion rewrite matching products when the catalog is built.
- AE Output can return products from the inexhaustible catalog to its bound network.
- Overclock and Fortune can be installed for normal Greenhouse configuration compatibility, but they do not multiply an already infinite amount.

Pipes can insert valid templates into the first four slots and extract from currently active output slots. The Creative Greenhouse has no survival recipe and is available only from the creative inventory or administrative commands.
