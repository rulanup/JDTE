---
navigation:
  title: Tiered Solar Panels
  icon: "jdte:concentrated_solar_panel"
  position: 2.8
item_ids:
  - jdte:concentrated_solar_panel
  - jdte:singularity_solar_panel
  - jdte:stellar_fusion_solar_panel
  - jdte:dimensional_collapse_solar_panel
  - jdte:creative_solar_panel
---

# Tiered Solar Panels

<ItemGrid>
  <ItemIcon id="jdte:concentrated_solar_panel" />
  <ItemIcon id="jdte:singularity_solar_panel" />
  <ItemIcon id="jdte:stellar_fusion_solar_panel" />
  <ItemIcon id="jdte:dimensional_collapse_solar_panel" />
  <ItemIcon id="jdte:creative_solar_panel" />
</ItemGrid>

JDT Extras solar panels cost more than JDT's base panel and provide progressively larger generation rates and internal buffers. Server configuration can change the survival-tier defaults.

## Tier comparison

| Tier | Default base generation | Internal capacity |
|---|---:|---:|
| Concentrated Solar Panel | 46,080 FE/t | 4,608,000 FE |
| Singularity Solar Panel | 184,320 FE/t | 18,432,000 FE |
| Stellar Fusion Solar Panel | 737,280 FE/t | 73,728,000 FE |
| Dimensional Collapse Solar Panel | 2,949,120 FE/t | 294,912,000 FE |
| Creative Solar Panel | Inexhaustible source | `Integer.MAX_VALUE` FE capability view |

The Creative Solar Panel never runs out. NeoForge's FE capability uses a 32-bit amount for each request, so it can supply up to **2,147,483,647 FE per capability request** and continue supplying later requests.

## Generation conditions and arrays

Survival tiers generate only during daytime when the block directly above is air and the panel can see the sky. Height also changes actual production: generation approaches its full multiplier farther from the world's vertical midpoint and is lowest near that midpoint.

If at least seven panels of the same tier occupy the surrounding positions in the same-height 3×3 area, the adjacency multiplier becomes 2x. Different tiers do not boost one another.

Every panel actively rotates output across all six directions and sends energy to adjacent blocks that can receive FE. The Creative Solar Panel ignores daylight, sky, and height conditions.

## Crafting

<RecipeFor id="jdte:concentrated_solar_panel" />
<RecipeFor id="jdte:singularity_solar_panel" />
<RecipeFor id="jdte:stellar_fusion_solar_panel" />
<RecipeFor id="jdte:dimensional_collapse_solar_panel" />

The Creative Solar Panel is available only from the creative inventory or administrative commands and has no survival recipe.
