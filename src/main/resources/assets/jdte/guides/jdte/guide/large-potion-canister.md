---
navigation:
  title: Large Potion Canister
  icon: "jdte:large_potion_canister"
  position: 27
item_ids:
  - jdte:large_potion_canister
---

# Large Potion Canister

Stores **4000 mB** of one potion composition. Four identical potion items produce **1000 mB** and **four glass bottles**. The input slot holds up to four potions or bottles. Shift-click four individual potions into it, or place them one at a time; remove the returned bottles before the next batch. A partial batch stays inside the canister when its menu closes or the item is saved.

Base potion, custom effects (duration, amplifier and display flags) and custom color all identify the stored composition. Different contents do not mix. Invalid, insufficient or over-capacity batches remain unprocessed and can be recovered. Emptying the stored liquid clears its composition. No FE or Forge fluid capability is required; the displayed mB are native potion units, not arbitrary fluid.

Use the canister in your **main hand** to open its menu. Alternatively equip the dedicated **Large Potion Canister Curios slot** and press **P** (configurable). An offhand use follows the source main-hand resolution. The menu is bound to the actual item; moving or replacing it closes the old view and cannot redirect actions into another item. The input item interface and menu share the persistent slot, with simulation leaving contents unchanged.

Install it in a **native JDT bow potion-canister slot**. Potion Arrow costs **25 mB**, Splash another **25 mB**, Lingering another **50 mB** per shot and per usable canister. Normal ability upgrades, FE/durability and enabled controls still apply. With all three abilities, each usable canister costs **100 mB**. Custom effects reach the actual arrow, while the stored remainder can stay above1000 mB. A water batch is consumed according to the source rules but contributes no potion effects.

The portable upgrade recipe preserves the native canister's potion amount, base type, name, pending slot and other NBT. It consumes one native canister, four Eclipse Alloy Ingots in four crafting slots, and one Time Fluid Catalyst. The tooltip and menu bar show remaining amount out of4000 and stored effects.

<RecipeFor id="jdte:large_potion_canister" />
