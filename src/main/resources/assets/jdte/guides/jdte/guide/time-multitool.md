---
navigation:
  title: Time Multitool
  icon: "jdte:time_multitool"
  position: 29
item_ids:
  - jdte:time_multitool
---

# Time Multitool

A powered Eclipse Alloy Paxel with **500,000 FE** and **1,000,000 mB of Time Fluid**. It mines pickaxe, shovel and axe blocks, tills soil, strips logs, scrapes oxidation, removes wax and douses campfires. Standard mining requires working FE even at1x; native tool costs and enchantment discounts apply (normally50 FE per mined block).

Sneak-use **air** to cycle **1,2,4,16,256,1024x**. Sneak-use a **block** to open the native JDT tool settings. The selected mode is saved on the tool and shown in its tooltip. Native enabled/disabled controls govern native abilities; selecting the mining speed is independent.

| Selected speed | Time Fluid per successfully mined block |
| --- | --- |
|1x|0mB|
|2x|2mB|
|4x|4mB|
|16x|16mB|
|256x|256mB|
|1024x|1024mB|

If the complete selected batch cannot afford its Time Fluid, it mines at1x with no partial fluid payment. Insufficient FE stops additional removals. Cancelled, replaced or unloaded targets consume no resources. Only successful blocks are charged; native hammer, vein/tree, falling-block, smelting and drop-teleport rules still apply. Leaf/lawn utility actions keep their native costs rather than gaining a mining Time Fluid fee. Axe/hoe/shovel transformations use the native one-durability FE rule and no Time Fluid.

Install the ten supported native ability upgrades in a smithing table, without a template. Each installation preserves the Time Multitool's FE, fluid, name, enchantments, mode and other native settings. The existing Large Fluid Tank can refill its Time Fluid in JDTE/all modes; FE equipment chargers can charge it normally.

The shapeless construction recipe consumes an Eclipse Alloy Paxel, Large Fluid Tank and Time Fluid Catalyst and creates a **fresh, uncharged tool**, as in the source recipe. Ingredient resources and installed upgrades are not transferred by that construction recipe.

Short attack presses stay on the first block. Holding attack allows another target after the configurable default **250ms**. Client configuration is `jdte/time-multitool-client.toml`, `timeMultitool.continuousMiningHoldDelayMillis` (0–2000); release, item-slot/world changes and menus reset the gate. This changes input timing, while the server validates mining and pays resources.

This is a mining tool, separate from Ultimate Time Wand's persistent tick acceleration. A native block breaker may use it as its tool; accelerated machine work still pays per actual removal. No wand/entity-acceleration upgrade or new scheduler is added to it.

<RecipeFor id="jdte:time_multitool" />
