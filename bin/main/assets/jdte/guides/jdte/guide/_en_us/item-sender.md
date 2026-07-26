---
navigation:
  title: Item Sender
  icon: "jdte:advanced_item_sender"
  position: 9
item_ids:
  - jdte:basic_item_sender
  - jdte:advanced_item_sender
  - jdte:extended_item_sender
---

# Item Sender

The Item Sender sends items to target containers within its configured range.

## Variants

| Machine | Upgrade Slots | Send Amount | Requires FE |
|---------|--------------|-------------|-------------|
| Basic Item Sender | 4 | 64 | No |
| Advanced Item Sender | 4 | 64 | Yes |
| Extended Item Sender | 8 | 64 | Yes |

## Features

- Sends items from internal storage to containers within range
- Supports redstone control, Range Upgrade, and Filter Upgrade
- With an Overclock or Creative Upgrade and Auto Input enabled, items move directly from Auto Input containers to ranged targets, up to 10,000 items per operation by default, without using internal slot capacity
- Speed controls and Underclock adjust the operation interval; batches are configurable

## Crafting

### Basic Item Sender

<BlockImage id="jdte:basic_item_sender" scale="2" />

<RecipeFor id="jdte:basic_item_sender" />

### Advanced Item Sender

<BlockImage id="jdte:advanced_item_sender" scale="2" />

<RecipeFor id="jdte:advanced_item_sender" />

### Extended Item Sender

<BlockImage id="jdte:extended_item_sender" scale="2" />

An extended version of the Advanced Item Sender with 8 upgrade slots. Obtained by right-clicking an Advanced Item Sender with an Extended Upgrade.

<RecipeFor id="jdte:extended_item_sender" />
