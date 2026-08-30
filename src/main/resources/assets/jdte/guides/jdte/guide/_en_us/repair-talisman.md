---
navigation:
  title: Repair Talisman
  icon: "jdte:repair_talisman"
  position: 6.8
item_ids:
  - jdte:repair_talisman
---

# Repair Talisman

<ItemImage id="jdte:repair_talisman" scale="2" />

The Repair Talisman works from anywhere in your inventory: as long as it sits in the main inventory, the armor slots, or the offhand, every damaged tool, weapon, and piece of armor on the player is continuously repaired — 5 durability per item per cycle by default (a cycle every 4 ticks, which is 25 durability per second, far faster than the Equivalent Exchange repair talisman). Repairs consume FE: every durability point costs 10,000 FE by default, drawn in slot order from charged energy items in the inventory (batteries, portable generators, and the like). When the available energy runs dry, repairs simply pause — nothing is ever repaired on credit. A fully charged Pocket Generator or large energy cell keeps the talisman running for a long time.

Everything is configurable under `jdte.repairTalisman`: `amountPerCycle` controls the durability repaired per item per cycle, `cycleInterval` the interval in ticks (0 disables the talisman), and `energyPerDurability` the FE cost per durability point (0 makes repairs free). Items with the Unbreakable component are left untouched.

The talisman carries an enchant glint so it is easy to spot in the inventory.

## Crafting

The recipe forms a pendant ring from an Eclipse Alloy block and Time Crystal blocks — a late-game purchase.

<RecipeFor id="jdte:repair_talisman" />
