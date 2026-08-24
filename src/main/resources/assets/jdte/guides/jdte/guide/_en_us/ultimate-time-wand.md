---
navigation:
  title: Ultimate Time Wand
  icon: "jdte:ultimate_time_wand"
  position: 6.6
item_ids:
  - jdte:ultimate_time_wand
---

# Ultimate Time Wand

<ItemImage id="jdte:ultimate_time_wand" scale="2" />

The Ultimate Time Wand creates a time-limited, server-owned acceleration request for one target. By default, it stores **800,000 mB of Time Fluid** and **10,000,000 FE**. The capacities, duration, and cost multipliers are configurable by the server.

## Modes and stacking

Sneak-right-click air or a block to cycle through four modes:

| Mode | Exponent added per use |
|---|---:|
| Normal | 1 |
| 2x | 2 |
| 4x | 4 |
| Max | 10 |

Normal right-click on a valid target creates a request; right-clicking a target with an existing request adds its exponent. The effect is capped at **1024x**, and reaching the cap does not consume resources. The wand accelerates JDT-approved tickable blocks and directly accelerates AE2 devices that expose the `IGridTickable` service.

## Resources and server safety

For each stack, the server validates the target and calculates resource costs before it writes the new request state. Insufficient Time Fluid or FE, an invalid target, a target already at the maximum multiplier, or a failed server commit never consumes resources.

Requests execute only on the server and are bounded by the per-target batch limit. AE2 targets take the direct acceleration route and never also receive an ordinary block-entity or random-tick execution from the same request. Time accelerators themselves are excluded as targets to prevent recursive acceleration.

## Crafting

<RecipeFor id="jdte:ultimate_time_wand" />
