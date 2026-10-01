---
navigation:
  title: Entity Accelerator
  icon: "jdte:advanced_entity_accelerator"
  position: 2.3
item_ids:
  - jdte:advanced_entity_accelerator
  - jdte:extended_entity_accelerator
---

# Entity Accelerator

Entity Accelerators consume Time Fluid and FE energy to directly accelerate living entities (`LivingEntity`) within their configured area.

## Advanced Entity Accelerator

<BlockImage id="jdte:advanced_entity_accelerator" scale="2" />

The advanced tier entity accelerator powered by Time Fluid and FE energy.

- Adjustable multiplier: 1x - 64x
- With Overclock or Creative Upgrade: 128x
- Upgrade slots: 4 standard slots
- Supports ghost spawn egg filtering and automatically excludes players

<RecipeFor id="jdte:advanced_entity_accelerator" />

## Extended Entity Accelerator

<BlockImage id="jdte:extended_entity_accelerator" scale="2" />

An extended tier of the Advanced Entity Accelerator with 8 upgrade slots. Obtained by right-clicking an Advanced Entity Accelerator with an Extended Upgrade.

- Adjustable multiplier: 1x - 512x
- With Overclock or Creative Upgrade: 1024x
- Supports 8 upgrade slots for additional upgrade cards
- Higher entity throughput per tick

<RecipeFor id="jdte:extended_entity_accelerator" />

## Mechanics

- **AI and Movement Suppression**: During accelerated extra ticks, entity AI pathfinding, travel physics, and mob pushing collisions are automatically suppressed. This prevents glitchy high-speed movement while allowing biological and internal ticking (such as growth, breeding cooldowns, potion effects, etc.) to progress rapidly.
- **Entity Filtering**: Configure allowed or denied entity types by placing ghost spawn eggs in the filter slot. Supports allowlist and denylist toggles. Players are automatically excluded from acceleration.
- **Resource Consumption**: Consumes JDT Time Fluid and FE energy. If resources are insufficient to cover the whole batch, work is rejected without consumption.
- **Supported Upgrades**:
  - **Capacity Upgrade**: Increases energy and fluid capacity.
  - **Fluid Upgrade**: Multiplies Time Fluid storage.
  - **Range Upgrade**: Expands the operational bounding area.
  - **Filter Upgrade**: Adds extra filter pages for more entity types.
  - **Overclock / Creative Upgrade**: Unlocks peak acceleration multipliers; Creative waives both FE and Time Fluid costs.
  - **Underclock Upgrade**: Locks to low speed with reduced energy cost.
  - **AE Crafting Read Upgrade**: Operates only when the linked AE2 network has active crafting jobs.
