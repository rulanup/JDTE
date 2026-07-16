---
navigation:
  title: Range Blocker
  icon: "jdte:range_blocker"
  position: 17
item_ids:
  - jdte:range_blocker
---

# Range Blocker

<BlockImage id="jdte:range_blocker" scale="2" />

The Range Blocker reuses the Advanced Sensor-style interface and eight upgrade slots. It handles its configured area through chunk-indexed entity events instead of scanning the complete area every machine tick.

## Modes

- **Containment** keeps matching targets inside after they enter the configured area. Target modes cover hostile mobs, passive mobs, all living entities, selected entity types, non-living entities, and all entity types. The server and client correct only boundary crossings after the entity's own tick and preserve velocity on axes that did not cross the boundary. Players and vehicle chains are always protected; named, tamed, and boss protection is configurable. Non-player projectiles, including Wither Skulls, Dragon Fireballs, and most modded projectiles, match their owner's target category and are discarded before their next bounding box crosses the boundary; Non-living and All Types can also match projectiles directly. Player-fired projectiles remain unaffected.
- **Demagnetization** applies the conventional remote-movement prevention flags to item entities in range. Player magnets cannot attract them, while machine collectors remain allowed. Flags owned by JDTE are removed after an item leaves the area or the machine stops.
- **Silence** blocks positional sounds whose source is inside the configured area. Standard world sounds are cancelled on the server, sounds submitted directly by mods are rejected at the client sound engine, and a bounded rotating check stops loops that were already playing or move into the field. Relative sounds without a world position, including UI sounds and background music, remain unaffected.

Containment shares the Entity Suppressor's category and list semantics and interprets spawn eggs in filter slots as entity-type filters. Demagnetization interprets regular items as exact item filters; its target button is disabled while the allowlist/blacklist button remains available. Both controls are disabled in Silence mode. An empty filter matches every target in the selected category.

Simple Magnets and Sophisticated integrations use the `PreventRemoteMovement` and `AllowMachineRemoteMovement` flags. Mekanism's Magnetic Attraction Unit uses an optional narrow compatibility hook, enabled by default, that removes demagnetized items from its existing candidate list.

Projectile containment, ownerless-projectile containment, and projectile explosion clipping are independently configurable and enabled by default. When a contained projectile explodes inside the field, blocks and entities outside the field are removed from that explosion's affected lists. These checks run only during projectile ticks or actual explosions and never scan the complete area.

The machine supports Range, Filter, Capacity, and Creative Upgrades. Containment defaults to 250 FE per active tick and Demagnetization to 1 FE per active tick. Silence continuously consumes 1 FE/tick while enabled so client-only sounds are also paid for. All three costs are independently configurable, and Creative removes the energy cost.

## Crafting

<RecipeFor id="jdte:range_blocker" />
