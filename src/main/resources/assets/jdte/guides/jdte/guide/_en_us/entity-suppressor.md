---
navigation:
  title: Entity Suppressor
  icon: "jdte:entity_suppressor"
  position: 16
---

# Entity Suppressor

The Entity Suppressor uses an Advanced Sensor-style interface and eight upgrade slots to control entities, dynamic block entity rendering, or particles in its configured area through event-driven indexes.

## Modes

- **Suppress Entities** cancels matching entity ticks, stopping AI, movement, projectiles, and entity timers.
- **Prevent Entity Spawns** rejects matching entities during spawn checks or before they join the level.
- **Disable Particles** rejects particles on the client before they enter the particle engine.
- **Disable Entity Rendering** skips rendering matching entities in the area without changing their ticks, collisions, or server behavior.
- **Disable Block Entity Rendering** skips dynamic block entity renderers in the area for mods using the standard dispatcher while leaving ordinary baked block models visible.

Entity modes support hostile mobs, passive mobs, all living entities, selected entity types, non-living entities, and all entity types. Put spawn eggs in filter slots to select entity types and choose allowlist or blacklist behavior. Particles and block entities have no entity type, so the target and list controls are unavailable in those two modes.

Players and entities in vehicle/passenger relationships are always protected. Named, tamed, and boss entities are protected by default and can be configured. The machine supports Range, Filter, Capacity, and Creative Upgrades.

Suppress Entities freezes matching ticks on both logical sides and prevents frozen item entities from being picked up. Prevent Entity Spawns can also remove already existing matches every 20 ticks through the `removeExistingEntities` option, which is disabled by default.

Rendering modes reduce client rendering cost only; they do not stop server-side entity, block entity, or mod-network updates. Disabling block entity rendering suppresses the complete dynamic renderer, so chests, sign text, or machines whose full model is dynamic may look incomplete. Entity Suppressors themselves always remain rendered.
