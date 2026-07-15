---
navigation:
  title: Advanced Item Collector
  icon: "jdte:advanced_item_collector"
  position: 16
item_ids:
  - jdte:advanced_item_collector
---

# Advanced Item Collector

<BlockImage id="jdte:advanced_item_collector" scale="2" />

The Advanced Item Collector uses JDT's Item Collector model and machine interface and provides eight standard upgrade slots.

| Property | Behavior |
|----------|----------|
| Collection point | Before the drop joins the world |
| Destination | Adjacent inventory on the machine's facing side |
| Internal buffer | None |
| Supported upgrades | Range and Filter |
| Oversized threshold | 10,000,000 by default; configurable |

The machine does not scan its area every tick. When a dropped item is about to join the world, the server checks the loaded collectors indexed for that chunk. Matching drops of the same item created during one tick are aggregated into one inventory insertion. Fully accepted drops never enter the world, so they create no pickup particles, entity rendering, or continuous area scans; unaccepted portions retain their original entity metadata.

## Usage

1. Point the Advanced Item Collector at an inventory that exposes an item capability.
2. Configure its X/Y/Z area, offsets, filter, and redstone mode in the machine interface.
3. New drops inside the area are inserted directly into the adjacent inventory.
4. If the inventory cannot accept the complete stack, only the accepted portion is collected and the remainder spawns normally.

When a player breaks a container inside the configured area, any slot at or above the configured threshold (10,000,000 items by default) is transferred directly through item capabilities before drop entities are created. Every triggered slot must transfer completely; if the destination or filter cannot accept all of it, the break is cancelled and the player is notified. This protection is enabled by default and can be disabled or adjusted under `jdte.advancedItemCollector`.

When the facing target exposes AE2's `ME_STORAGE` capability, threshold-triggered stacks are inserted directly as long-count ME storage operations without using normal interface buffer slots or 64-item loops. This supports AE2 ME Interfaces plus ExtendedAE Extended and Oversize Interfaces, and can be disabled independently with `meDirectTransferEnabled` in the same config category.

The Advanced Item Collector accepts only Range and Filter Upgrades. Capacity, Fluid, speed, Creative, and other upgrades with no effect on event-driven collection cannot be inserted. The eight upgrade slots use JDTE's two-column layout. The machine has no internal item buffer and never creates item-flow particles.

## Crafting

<RecipeFor id="jdte:advanced_item_collector" />
