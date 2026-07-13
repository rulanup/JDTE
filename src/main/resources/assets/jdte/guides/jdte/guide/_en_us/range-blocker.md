---
navigation:
  title: Range Blocker
  icon: "jdte:range_blocker"
  position: 17
---

# Range Blocker

The Range Blocker reuses the Advanced Sensor-style interface and eight upgrade slots. It handles its configured area through chunk-indexed entity events instead of scanning the complete area every machine tick.

## Modes

- **Containment** keeps living entities inside after they enter the configured area. The server and client correct only boundary crossings after the entity's own tick and preserve velocity on axes that did not cross the boundary. Players and vehicle chains are always protected; named, tamed, and boss protection is configurable. Non-player projectiles, including Wither Skulls, Dragon Fireballs, and most modded projectiles, bind to a field by their own or their owner's position and are discarded before their next bounding box crosses the boundary. Player-fired projectiles remain unaffected.
- **Demagnetization** applies the conventional remote-movement prevention flags to item entities in range. Player magnets cannot attract them, while machine collectors remain allowed. Flags owned by JDTE are removed after an item leaves the area or the machine stops.

Containment interprets spawn eggs in filter slots as entity-type filters. Demagnetization interprets regular items as exact item filters. An empty filter matches every target; the list button switches between allowlist and blacklist behavior.

Simple Magnets and Sophisticated integrations use the `PreventRemoteMovement` and `AllowMachineRemoteMovement` flags. Mekanism's Magnetic Attraction Unit uses an optional narrow compatibility hook, enabled by default, that removes demagnetized items from its existing candidate list.

Projectile containment, ownerless-projectile containment, and projectile explosion clipping are independently configurable and enabled by default. When a contained projectile explodes inside the field, blocks and entities outside the field are removed from that explosion's affected lists. These checks run only during projectile ticks or actual explosions and never scan the complete area.

The machine supports Range, Filter, Capacity, and Creative Upgrades. It consumes no operating energy while idle and settles energy at most once per machine tick while matching targets are present. Containment defaults to 250 FE per active tick. Demagnetization defaults to an average of 0.25 FE per active tick, accumulated as 1 FE every four active ticks; both costs are independently configurable.
