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
| Collection point | Before world insertion, plus bounded existing-drop scans |
| Destination | Adjacent inventory on the machine's facing side |
| Internal buffer | None |
| Supported upgrades | Range and Filter |
| Oversized threshold | 10,000,000 by default; configurable |

New drops use an event-driven path rather than an area scan. The server checks only collectors indexed for the drop's chunk, and matching drops created during one tick are aggregated into one insertion. Existing drops are handled by a separate bounded round-robin scan: at most one due collector is scanned per server tick, every collector waits at least 10 ticks by default, and one scan processes at most 256 item entities. All three limits are configurable.

## Usage

1. Point the Advanced Item Collector at an inventory that exposes an item capability.
2. Configure its X/Y/Z area, offsets, filter, and redstone mode in the machine interface.
3. New and already existing drops inside the area are inserted into the adjacent inventory.
4. If the inventory cannot accept the complete stack, only the accepted portion is collected and the remainder spawns normally.

When a player breaks a container inside the configured area, any slot at or above the configured threshold (10,000,000 items by default) is transferred directly through item capabilities before drop entities are created. Every triggered slot must transfer completely; if the destination or filter cannot accept all of it, the break is cancelled and the player is notified. This protection is enabled by default and can be disabled or adjusted under `jdte.advancedItemCollector`.

The oversized threshold applies only to pre-break container protection. For an AE2 ME Interface or an ExtendedAE Extended/Oversize Interface, ordinary and oversized collection first simulates the normal item handler. If that handler cannot accept the complete stack, the collector checks `ME_STORAGE` once and performs one atomic long-count direct insertion when the network has enough capacity. If direct insertion cannot accept everything, normal partial insertion is used and the remainder stays in the world. This path is independent of item count and can be disabled with `meDirectTransferEnabled`.

The Advanced Item Collector accepts only Range and Filter Upgrades. Capacity, Fluid, speed, Creative, and other upgrades with no effect on event-driven collection cannot be inserted. The eight upgrade slots use JDTE's two-column layout. The machine has no internal item buffer and never creates item-flow particles.

## Crafting

<RecipeFor id="jdte:advanced_item_collector" />
