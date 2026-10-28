---
navigation:
  title: Energy Conversion Upgrades
  icon: "jdte:energy_brewing_upgrade"
  position: 36
item_ids:
  - jdte:energy_brewing_upgrade
  - jdte:energy_overload_upgrade
---

# Energy Conversion Upgrades

## Energy Brewing

Install one Energy Brewing Upgrade in the Advanced Potion Brewer. It replaces Blaze Powder refills with one FE purchase per twenty brewing starts. The default charge is **5,000 FE**, configured by `advancedPotionBrewer.energyPerBlazePowder` from 0 to 1,000,000. Zero makes the fuel charge free; Creative also bypasses it. The normal FE cost for completing each ingredient stage remains in effect.

Existing fuel credits remain available. The upgrade blocks new Blaze Powder insertion through manual/Shift slots and automation, but does not delete powder already stored. Removing the card permits powder again and does not discard purchased credits. FE purchases and remaining credits survive saving, with the card stored in the canonical Forge upgrade capability. The machine needs valid ingredients and output capacity before purchasing fuel.

## Energy Overload

Install one Energy Overload Upgrade in a supported Energy Transmitter to remove its single-transfer rate cap. In this Forge checkpoint the native JDT Energy Transmitter is implemented and tested. The source advanced/extended transmitter machines remain unported. The card does not create FE, increase storage or make recipients accept more than their actual capacity/rate. Removing it restores the configured native cap.

Both cards use existing upgrade slots, Shift insertion, upgrade bags, machine-panel actions, typed copiers, tooltips and Jade's installed-upgrade display. They are not generic speed upgrades and reject unrelated machines.

<RecipeFor id="jdte:energy_brewing_upgrade" />
<RecipeFor id="jdte:energy_overload_upgrade" />
