---
navigation:
  title: Charged Lightning Rod
  icon: "jdte:charged_lightning_rod"
  position: 25
item_ids:
  - jdte:charged_lightning_rod
  - jdte:thunderstorm_upgrade
---

# Charged Lightning Rod

Supply FE and keep a vanilla lightning rod directly above the machine. If that space is air, the machine creates its own rod once. If you remove that rod, replace it manually. An existing player-placed rod is reused and is not removed with the machine.

The machine normally requires both rain and thunder. One **Thunderstorm Upgrade** bypasses the weather condition without changing world weather. Sky visibility is not required. Four slots accept Capacity, Ultimate Capacity, Overclock, Ultimate Overclock and Thunderstorm cards only; the two speed cards are mutually exclusive.

| Mode | FE per bolt | Maximum rate |
| --- | --- | --- |
| Normal | 10000 | One per200 ticks |
| Overclock | 40000 | Five per tick |
| Ultimate Overclock | 100000 | Fifty per tick |

Default capacity is10000000FE. Capacity cards increase it; removing one is rejected if stored FE would be lost. Each successfully admitted bolt costs once, partial batches keep the remainder, and time acceleration cannot multiply the same tick's batch. No fluid is required.

The bolts run full vanilla lightning behavior, including damage, transformations, fire rules, copper and rod power. They carry the original AE2LT natural-weather marker. This is a machine-created complete bolt, not a cosmetic flash or a change to random weather frequency. Optional AE2LT integration depends on a compatible installed version.

The menu has independent **render** and **sound** toggles. Hiding a bolt also disables its sky flash; silencing it leaves its effects active. The settings copier copies these controls without copying FE, work cooldown or ownership. Machine NBT retains energy, upgrades, cooldown and both controls through saving and transport.

<RecipeFor id="jdte:charged_lightning_rod" />
<RecipeFor id="jdte:thunderstorm_upgrade" />
