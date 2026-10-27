---
navigation:
  title: Upgrade Storage
  icon: "jdte:upgrade_storage"
  position: 31
item_ids:
  - jdte:upgrade_storage
  - jdte:advanced_upgrade_storage
---

# Upgrade Storage

<ItemImage id="jdte:upgrade_storage" scale="2" />

Upgrade Storage holds twenty stacks of upgrades in a 4x5 grid. Each slot accepts up to 64 upgrades, including JDT upgrades that normally stack to one. Upgrades with different NBT remain separate.

## Usage

- Right-click while holding the storage in either hand to open it.
- Shift-click upgrades to move them into storage or back into your inventory. Unstackable upgrades return in legal player inventory stacks; any remainder stays in the bag.
- The open bag is locked against pickup, hotbar swaps and offhand swaps. Replacing the held bag invalidates its server menu.
- Hold Shift over the item tooltip to inspect stored upgrades and their counts.
- Forge item-handler integrations can insert and extract through the same persistent inventory. Contents remain with the item when it is saved, copied or transferred.

The bag accepts JDTE upgrade cards, Looting and Sharpness upgrades, Extended Upgrades, JDT ability upgrades and items in the `jdte:upgrades` item tag. It rejects ordinary items and nested upgrade storage bags.


## Crafting

<RecipeFor id="jdte:upgrade_storage" />

## Advanced Upgrade Storage

<ItemImage id="jdte:advanced_upgrade_storage" scale="2" />

The advanced case holds 36 stacks in a 6x6 grid, with the same 64-upgrade slot limit and Forge item interface. It uses the same persistent item inventory and protects the opened case against hotbar/offhand movement.

While a machine menu is open, its side panel finds advanced cases in your main hand, offhand, inventory and available Curios inventories. The page arrows switch between cases; the header collapses the panel. Green dots identify upgrades the current machine can accept.

- Left-click inserts one upgrade; Shift-left-click inserts as many as the machine accepts.
- Right-click takes one upgrade onto an empty cursor; Shift-right-click withdraws one legal stack into your inventory.
- With an upgrade on the cursor, left-click deposits the carried stack into that case slot; right-click deposits one.
- Machine limits, incompatible upgrades and dedicated Bio Crusher Looting/Sharpness slots are respected. Failed insertions leave the remainder in the case.

There is no automatic collection mode in the pinned 0.6.5.2 item implementation. Curios lookup uses slots supplied by the installed Curios configuration; this item does not create a new accessory slot.

<RecipeFor id="jdte:advanced_upgrade_storage" />
