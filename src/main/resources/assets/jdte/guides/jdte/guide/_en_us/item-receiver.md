---
navigation:
  title: Item Receiver
  icon: "jdte:advanced_item_receiver"
  position: 11
item_ids:
  - jdte:basic_item_receiver
  - jdte:advanced_item_receiver
  - jdte:extended_item_receiver
---

# Item Receiver

The Item Receiver receives items from target containers within its configured range.

## Variants

| Machine | Upgrade Slots | Receive Amount | Requires FE |
|---------|--------------|----------------|-------------|
| Basic Item Receiver | 4 | 64 | No |
| Advanced Item Receiver | 4 | 64 | Yes |
| Extended Item Receiver | 8 | 64 | Yes |

## Features

- Receives items from containers within range into internal storage
- Supports redstone control, Range Upgrade, and Filter Upgrade
- With an Overclock or Creative Upgrade and Auto Output enabled, items move directly from ranged sources to Auto Output containers, up to 10,000 items per operation by default, without using internal slot capacity
- Speed controls and Underclock adjust the operation interval; batches are configurable

## Crafting

### Basic Item Receiver

<BlockImage id="jdte:basic_item_receiver" scale="2" />

<RecipeFor id="jdte:basic_item_receiver" />

### Advanced Item Receiver

<BlockImage id="jdte:advanced_item_receiver" scale="2" />

<RecipeFor id="jdte:advanced_item_receiver" />

### Extended Item Receiver

<BlockImage id="jdte:extended_item_receiver" scale="2" />

An extended version of the Advanced Item Receiver with 8 upgrade slots. Obtained by right-clicking an Advanced Item Receiver with an Extended Upgrade.

<RecipeFor id="jdte:extended_item_receiver" />
