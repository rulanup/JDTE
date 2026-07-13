---
navigation:
  title: Entity Suppressor
  icon: "jdte:entity_suppressor"
  position: 16
---

# Entity Suppressor

The Entity Suppressor uses an Advanced Sensor-style interface and eight upgrade slots to control entities or particles in its configured area through event-driven indexes.

## Modes

- **Suppress Entities** cancels matching entity ticks, stopping AI, movement, projectiles, and entity timers.
- **Block Entities** rejects matching entities during spawn checks or before they join the level.
- **Disable Particles** rejects particles on the client before they enter the particle engine.

Entity modes support hostile mobs, passive mobs, all living entities, selected entity types, non-living entities, and all entity types. Put spawn eggs in filter slots to select entity types and choose allowlist or blacklist behavior.

Players and entities in vehicle/passenger relationships are always protected. Named, tamed, and boss entities are protected by default and can be configured. The machine supports Range, Filter, Capacity, and Creative Upgrades.

Suppress Entities freezes matching ticks on both logical sides and prevents frozen item entities from being picked up. Block Entities can also remove already existing matches every 20 ticks through the `removeExistingEntities` option, which is disabled by default.
