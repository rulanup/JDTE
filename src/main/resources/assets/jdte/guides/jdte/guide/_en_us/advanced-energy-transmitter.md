---
navigation:
  title: Advanced Energy Transmitter
  icon: "jdte:advanced_energy_transmitter"
  position: 29
item_ids:
  - jdte:advanced_energy_transmitter
---

# Advanced Energy Transmitter

<BlockImage id="jdte:advanced_energy_transmitter" scale="2" />

The Advanced Energy Transmitter supplies every FE receiver in its configured area and provides eight standard upgrade slots.

| Property | Default behavior |
|----------|------------------|
| Internal capacity | 2,000,000,000 FE |
| Initial area | One block above the transmitter (zero X/Y/Z radius and Y offset 1) |
| Target refresh | Wait 20 ticks after a scan completes before starting the next one |
| Scan budget | Inspect 512 block positions per server tick |
| Target budget | Attempt at most 512 cached targets per operation |
| Base transfer budget | 268,435,456 FE per operation; 8x while overclocked, capped by the integer limit |
| Supported upgrades | Range, Filter, Capacity, Overclock, and Creative |

## Operation

Target discovery and energy delivery are separate stages. The area is scanned in fixed-size batches while the previous complete target cache remains active. A newly discovered target list replaces the old list only after the scan finishes. Enlarging the area therefore cannot cause a full-area scan in one tick, and refreshing does not interrupt delivery to existing targets.

Delivery rotates through cached targets with a fair round-robin cursor. Full, invalid, and temporarily unavailable receivers still count toward the attempt budget, preventing large groups of bad targets from creating unbounded server work. The transmitter first tries the target side facing the transmitter, then checks the remaining sides. Other Advanced Energy Transmitters are excluded by default to prevent charging loops.

Filters match the target block item. Changing allowlist, denylist, or NBT comparison settings immediately discards the old cache and begins a new scan. Area and machine-facing changes also rebuild the cache. Unloaded chunks are never force-loaded; targets become discoverable during a later refresh after their chunks load.

## Upgrades And Interface

- **Range** raises radius and offset limits.
- **Filter** adds active filter slots.
- **Capacity** expands the internal FE buffer.
- **Overclock** shortens the interval between delivery operations and scales total transfer and ME extraction budgets by the configured multiplier.
- **Creative** supplies energy without an internal FE reserve and includes Overclock behavior.

Each operation simulates and aggregates real demand within the fixed target budget, batch-refills from the energy-item slot and available ME energy, then performs only one actual receive call per target. This does not increase area scan frequency or target attempts.

When AE2 and Applied Flux are installed, a normal ME cable can connect to any transmitter side. The transmitter consumes one channel and uses the public ME storage API to pull FE from energy disks on demand. It performs at most one batched ME extraction per regular block-delivery operation and never scans disks or storage cells. ME extraction fails fast when the grid is unpowered, lacks a channel, or either mod is absent; regular FE input remains available. Both the GUI and Jade show the ME integration state. A zero internal buffer does not mean the transmitter is offline because ME energy can be supplied on demand.

The player button to the right of Match NBT binds the transmitter to the current operator. While the transmitter chunk is loaded and that player is online, it can prioritize FE equipment in the player's hotbar, hands, armor, and Curios accessory slots across any distance or dimension. Player charging does not consume block scan or target-attempt budgets and has separate limits for inspected items and receive calls per item. After the first binding, only the bound player or a creative administrator can remove it.

The particle button controls visible energy flow from the transmitter to successful block targets and does not affect delivery. Particle targets have a separate server-side budget.

Energy capacity, operation delay, refresh interval, scan and target budgets, total transfer budget, overclock multiplier, ME extraction budget, per-target throughput, player equipment/call limits, transmitter-loop exclusion, and particle defaults/budget are configurable under `jdte.advancedEnergyTransmitter`.

## Crafting

<RecipeFor id="jdte:advanced_energy_transmitter" />