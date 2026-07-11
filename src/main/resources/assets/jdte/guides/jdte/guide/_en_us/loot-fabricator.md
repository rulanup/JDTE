---
navigation:
  title: Loot Fabricator
  icon: "jdte:loot_fabricator"
  position: 18
item_ids:
  - jdte:loot_fabricator
---

# Loot Fabricator

The Loot Fabricator uses spawn eggs as reusable entity templates. It consumes Life Fluid, Time Fluid, and energy, then rolls the entity's currently loaded vanilla or modded loot table. Spawn eggs are not consumed and each of the four input slots holds at most one egg.

It starts with 16 output slots. Each Capacity Upgrade unlocks another 16-slot page, up to four pages. Its 18 dedicated upgrade slots accept Capacity Upgrades and up to three Looting Upgrades.

Each operation costs 1,000 mB of Life Fluid, 5,000 FE, and at least 10 mB of Time Fluid. Reducing the processing time below 20 ticks with the speed button increases Time Fluid consumption proportionally, reaching 200 mB at one tick.

Redstone control and automatic I/O are supported. Automation inserts only spawn eggs and matching fluids and extracts only from unlocked output slots.
