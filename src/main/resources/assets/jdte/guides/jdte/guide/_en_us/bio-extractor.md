---
navigation:
  title: Bio Extractors
  icon: "jdte:advanced_bio_extractor"
  position: 6.7
item_ids:
  - jdte:advanced_bio_extractor
  - jdte:extended_bio_extractor
---

# Bio Extractors

These machines collect products from nearby living creatures. They do not consume the creature. Default recipes milk cows/goats/mooshrooms, shear sheep in their actual wool color, and collect chicken, squid, glow squid, bee and sniffer products. JEI shows exact energy, cooldown and chance. Players, dead creatures and spectators are excluded.

| Tier | FE buffer | Fluid tank | Output slots | Creatures per scan |
| --- | --- | --- | --- | --- |
| Advanced | 100,000 | 16,000 mB | 9 | 1 |
| Extended | 200,000 | 32,000 mB | 18 | 4 |

Both scan every 20 counter ticks; Overclock uses 5, Underclock uses 40. Overclock/Creative increase the per-scan limit to 2/8. Ultimate Overclock follows the source's ten server executions and 50x recipe energy. Ultimate Capacity multiplies capacities by 50. Normal Capacity/Fluid cards stack with it. The Advanced machine has four upgrade slots; Extended has eight.

A creature's cooldown is shared between extractors and saved with the creature. Sheared sheep also need to regrow wool. A full output inventory/tank or insufficient FE prevents a successful harvest without consuming resources or setting a new cooldown. Failed chance rolls follow the recipe and still count as a harvest.

Use Range and spawn-egg Filter upgrades to select an area and creatures. The source initializes Advanced radius 5 and Extended radius 9, but clamps both against the shared area limit (default 5) on the first tick. Install Range or adjust that limit to retain a larger configured radius. Range increases the permitted limit; configure the area in the GUI.

Pipes can extract outputs. Items cannot be inserted. The source tank permits external filling of compatible fluid, but a different stored fluid blocks milk production until drained. Automatic I/O and AE Output treat both products as outputs. Capacity cards cannot be removed while the smaller buffers would discard FE/fluid.

Use Extended Upgrade on the placed Advanced machine for a transactional conversion, retaining outputs, FE, fluid, filters, upgrades and scan progress. Wrench transport also retains machine state.

## Crafting

Construction from a packed Advanced Life Extractor preserves stored FE, fluid and upgrades. Finish any pending fractional Life Fluid production before crafting. Settings copier presets apply to the same machine type; use Extended Upgrade for a tier conversion.

<RecipeFor id="jdte:advanced_bio_extractor" />
<RecipeFor id="jdte:extended_bio_extractor" />
