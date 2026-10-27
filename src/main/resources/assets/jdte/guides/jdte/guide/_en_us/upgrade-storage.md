---
navigation:
  title: Upgrade Storage
  icon: "jdte:upgrade_storage"
  position: 31
item_ids:
  - jdte:upgrade_storage
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

This is the basic storage item. The Advanced Upgrade Storage's collection modes and Curios support remain part of the unfinished Forge port.

## Crafting

<RecipeFor id="jdte:upgrade_storage" />
