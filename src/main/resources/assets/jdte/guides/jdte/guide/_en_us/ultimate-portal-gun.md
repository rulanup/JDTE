---
navigation:
  title: Ultimate Portal Gun
  icon: "jdte:ultimate_portal_gun"
  position: 37
item_ids:
  - jdte:ultimate_portal_gun
---

# Ultimate Portal Gun

The gun stores destinations in pages of twelve and supports manually edited coordinates. Manual coordinates cost **500 mB Portal Fluid across dimensions**, or **25 mB per block in the same dimension**, rounded up and capped at **25,000 mB**. Zero distance costs no fluid. Recorded favorites and the previous destination use JDT's native fluid pricing, even while a manual slot is selected.

Shots use the **JDT Portal Gun V2 energy-cost setting** (`PORTAL_GUN_FE_COST` in Forge 1.20.1). This is the Forge counterpart of the source's `PORTAL_GUN_V2_RF_COST`. The legacy JDTE `ultimatePortalGun.energyCost` option is retained for configuration compatibility and does not set the firing price, matching the source's actual behavior.

Default capacities are **1,000,000 mB Portal Fluid** and **1,000,000 FE**. JDTE's `ultimatePortalGun.fluidCapacity` and `ultimatePortalGun.energyCapacity` set the actual item capabilities. Other fluids are rejected. Destinations, manual flags and both resources survive item saves.

The gun requires the complete FE/fluid cost before firing. An insufficient balance or a rejected projectile does not spend either resource. Existing dimension blacklist and stay-open selection remain available. This port checkpoint has actual server projectile, resource and saved-item tests; it has not repeated graphical editor validation.
