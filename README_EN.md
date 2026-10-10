# JDT Extras

JDT Extras (`jdte`) is a NeoForge addon for [Just Dire Things](https://www.curseforge.com/minecraft/mc-mods/just-dire-things). It adds upgrade cards, extended machines, time acceleration, area control, and automation devices for JDT.

Current version: `0.6.3.3`

## What's new in 0.6.3.3

Feature & maintenance release:
- **Bio Factory Stacking Expanded to 6,400,000 (`jdte:bio_factory`)**: The Ultimate Capacity Upgrade (`jdte:ultimate_capacity_upgrade`) now scales output slot stacking in the Bio Factory up to **6,400,000** items per slot with compact slot count rendering, preventing output stalls under high productivity and time acceleration.
- **Bio Factory Ultimate Overclock GUI & Multiplier Alignment**: Aligned the Bio Factory's speed multiplier and GUI button with Greenhouse standards. Installing an Ultimate Overclock Upgrade locks `getMultiplier()` and `getMaxSelectableMultiplier()` to 640 (standard Overclock locks to 64), resolving the discrepancy where the GUI previously remained at 1~64 while internal processing ran at 640x.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.3.3.md).

## What's new in 0.6.3.2

Feature & maintenance release:
- **Advanced AE Output Upgrade (`jdte:advanced_ae_output_upgrade`)**: Brand new upgrade card (limit 1, mutually exclusive with standard AE Output Upgrade). Binds to an AE2 Wireless Access Point to dump items and fluids directly into the ME network. Elevates single fluid extraction limit to 2.147 billion mB (`Integer.MAX_VALUE`), runs up to 10 extraction cycles per tick (1T 10 extractions), and interleaves flush operations between Ultimate Overclock machine executions to completely eliminate output buffer blockage under extreme speeds.
- **Greenhouse & Large Greenhouse Stacking Expanded to 6,400,000**: The Ultimate Capacity Upgrade (`jdte:ultimate_capacity_upgrade`) now drastically expands single-slot product stacking in the Greenhouse and Large Greenhouse up to **6,400,000**, providing unlimited room for ultra-high-throughput agricultural automation.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.3.2.md).

## What's new in 0.6.3.1

Feature & maintenance release:
- **Native 32768X & Extreme Wand Acceleration Optimization for JDTE Machines**: Overhauled and expanded `CoalescedAcceleratedMachine` batch execution across all JDTE production and processing machines (Bio Crushers, Bio Extractors, Bio Factory, Fluid Mixer, Gel Generator, Infusion Machines, Life Breeder, Life Extractor, Loot Fabricator, Advanced Potion Brewer, and Crystal Incubator). When receiving extreme acceleration from the Ultimate Time Wand (up to 32768X) or time accelerators, machines batch operations, pre-check energy and fluids, probe output capacity, and smoothly consume resources without frame drops or CPU lag; machines with Ultimate Overclock Upgrade smoothly receive 1-tick / 10-iteration acceleration benefits.
- **Crystal Incubator Time Acceleration Fix**: Fixed a bug where the Crystal Incubator could not be accelerated by Time Accelerators or Time Wands. The anti-nesting `TimeAcceleratorMachine` marker has been precisely scoped to real time accelerators, while the Crystal Incubator now implements `CoalescedAcceleratedMachine` with up to 64 cycles/tick batch growth and harvest and 10x growth acceleration under Ultimate Overclock.
- **Greenhouse & Large Greenhouse Jade Fluid Warning**: When a Greenhouse or Large Greenhouse has an Ultimate Overclock Upgrade installed without sufficient capacity upgrades, Jade displays a clear warning status explaining that Time Fluid capacity is insufficient to support Ultimate Overclock fluid consumption.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.3.1.md).

## What's new in 0.6.3.0

Feature & maintenance release:
- **Mineral Extractor JEI Integration & Network Sync**: Added a dedicated JEI recipe category for the Mineral Extractor and Large Mineral Extractor, displaying extractable ores, base chances, and smelting outputs for every biome. The server-side biome mineral survey index is automatically synchronized to clients on connect and `/reload`.
- **Modded Ore Feature Detection & Compatibility**: Overhauled the world-generation feature analyzer to recognize modded ores including Mekanism (`ResizableOreFeatureConfig`), BiomeModifiers, and JSON Codec fallbacks, ensuring all modded ore veins are correctly indexed and extractable.
- **Ultimate Overclock Upgrade (`jdte:ultimate_overclock_upgrade`)**: High-tier overclock upgrade (limit 1). Locks machine delay to 1 tick, executes 10 times per tick at 50x power consumption; can also be applied to the Ultimate Time Wand to increase maximum acceleration up to 32768X.
- **Ultimate Capacity Upgrade (`jdte:ultimate_capacity_upgrade`)**: High-tier capacity upgrade (limit 1). Provides a 50x capacity upgrade multiplier, massively scaling machine FE capacity, fluid capacity, and unlocking maximum output inventory space.
- **GuideME & Patchouli In-Game Documentation**: Added comprehensive documentation in both English and Simplified Chinese for new upgrade cards and the Ultimate Time Wand.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.3.0.md).

## What's new in 0.6.2.0

Feature & maintenance release:
- **Advanced & Extended Bio Extractors (`jdte:advanced_bio_extractor`, `jdte:extended_bio_extractor`)**: Machines that consume FE to extract biological fluids and item drops (e.g. cow milking into internal tanks, sheep shearing with accurate dye colors, chicken eggs, squid ink) from living entities in a configured area. Includes entity cooldown management, ghost spawn egg filtering, and in-world tier conversion via Extended Upgrade cards.
- **Fluid Mixer UI & Recipe Overhaul (`jdte:fluid_mixer`)**: Redesigned machine GUI with streamlined central progress arrow (`mixer_arrow.png`), relocated catalyst slot to `(94, 14)` for clean vertical alignment, explicit tooltips for Tank A/B/Output, fully aligned JEI recipe displays, and complete Patchouli & GuideME documentation.
- **Advanced Upgrade Storage Multi-Storage Pagination**: When carrying multiple Advanced Upgrade Storages across inventory and Curios slots, the docked machine side-panel now features footer pagination controls `[ < ]  X / Y  [ > ]` for quick switching; Shift-clicking from machine slots cascades smoothly into subsequent storages when the current one is full.
- **Ultimate Time Wand DYNA Decoupling Fix**: Resolved an issue where the Ultimate Time Wand failed to function properly when JustDynaThings was not installed.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.2.0.md).

## What's new in 0.6.1.0

Feature release:
- **Advanced Upgrade Storage (`jdte:advanced_upgrade_storage`)**: Enhanced 6×6 (36 slot) upgrade case with 64-stacking per slot and Curios support. While carried in player inventory or equipped in Curios, opening any JDT/JDTE machine interface automatically docks a live interactive 6×6 upgrade panel, allowing direct 1-click and Shift-click transfers between storage and machine slots.
- **Entity Acceleration Upgrade (`jdte:entity_acceleration_upgrade`)**: Dedicated Ultimate Time Wand smithing/crafting upgrade allowing players to right-click living entities to accelerate them directly while automatically suspending target AI and navigation.
- **Advanced & Extended Entity Accelerators (`jdte:advanced_entity_accelerator`, `jdte:extended_entity_accelerator`)**: Machines consuming Time Fluid and FE to accelerate living entities (`LivingEntity`) in a configured area with 1-64x speed (1-512x for Extended, up to 1024x overclocked). Automatically suspends entity AI, pathfinding, and mob pushing collisions during accelerated ticks to ensure stable biological ticking without erratic movement; supports ghost spawn egg filtering and automatically excludes players.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.1.0.md).

## What's new in 0.6.0.3

Feature release:
- **Upgrade Storage (`jdte:upgrade_storage`)**: Portable storage case dedicated to JDTE and JDT upgrades with a 4×5 (20 slot) grid. Supports stacking upgrades up to 64 per slot, intuitive Shift-click fast transfers, real-time data persistence, and an `ItemHandler.ITEM` capability.
- **Energy Overload Upgrade (`jdte:energy_overload_upgrade`)**: Dedicated Energy Transmitter upgrade (limit 1) removing all single-batch, per-tick transfer budgets, and ME energy extraction limits to charge all range targets and bound player inventories in a single tick.
- **Greenhouse Crop Scaling Fix**: Resolved growth scaling discrepancies between vanilla and Mystical Agriculture crops in the Greenhouse under varying upgrade configurations.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.0.3.md).

## What's new in 0.6.0.2-fix1

Hotfix release: fixes Extended Experience Holder UI display issues. Removed the erroneously implemented `FilterableBE` interface and redundant filter data from `ExtendedExperienceHolderBE`, eliminating overlapping allowlist/NBT buttons with the owner-only and target-level buttons, as well as ghost filter slots that intersected with the experience progress bar.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.0.2-fix1.md).

## What's new in 0.6.0.2

Performance and stability optimization release: Time Accelerators now feature resource-readiness guards preventing empty idle work and scans when out of fluid or power; machine targets are cached in `MachineTargetCache` with periodic refreshes, cutting over 95% of per-tick chunk iterations; unfiltered accelerators pass blocks in O(1) time without fake players, while configured filters use a local `BlockState` cache; visual effect entities are updated locally by clients to eliminate per-tick packet spam, backed by a `timeAcceleratorEffectsEnabled` toggle and disconnect safety guards.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.0.2.md).

## What's new in 0.6.0.1

Naming-normalization release: event-driven classes now follow unified semantic suffixes (`*Events` / `*Manager` / `*Helper` / `*Handler`), runtime managers moved into a dedicated `common/manager` package, the JEI and Jade plugins moved under `client/`, the `jdte-ae` addon aligned on the uppercase `AE` abbreviation, screen lang keys unified under `jdte.screen.*`, `fluidbar.png` renamed to `fluid_bar.png`, and the mod description refreshed. Registry IDs and config field names are unchanged, so worlds, datapacks, and player configs stay fully compatible.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.0.1.md).

## What's new in 0.6.0-fix1

Hotfix release: the JEI greenhouse category now caches the Botany Pots crop enumeration per RecipeManager (world joins and `/reload` no longer re-resolve every crop); the Draconic Evolution stabilized spawner interception mixin no longer fails to apply and crash on spawner placement due to an exact-type accessor mismatch; and the hidden `2i` easter egg slot texture is upgraded to the full 512x512 artwork with a CPU mip chain and trilinear filtering.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.0-fix1.md).

## What's new in 0.6.0

This release includes the full-grid AE2 acceleration addon, machine-settings and automation stability fixes, transactional Bio Factory output handling, Gel Generator extended conversion support, Loot Fabricator temporary-entity cleanup, and a paginated Ultimate Portal Gun dimension picker.

See the [detailed release notes and upgrade guidance](docs/releases/0.6.0.md).

[中文 README](README.md)

## Main Features

### Upgrade System

Standard machines have four upgrade slots and extended machines have eight. Empty slots show supported upgrade types, installed counts, and limits. Sneak-right-click a machine with an Upgrade Card to insert it directly; FTB Ultimine can insert cards into selected machines in bulk.

| Upgrade | Primary effect | Limit |
|---|---|---:|
| Capacity | Multiplies FE and fluid capacity by powers of two | 3 |
| Overclock | Increases operation speed and transfer batches at a higher energy cost | 1 |
| Underclock | Reduces operation speed and energy cost | 1 |
| Fluid | Increases fluid capacity only | 3 |
| Fluid Storage | Adds an internal tank to JDT Clickers | 1 |
| Generator | Uses more fuel for increased generation | 1 |
| Range | Raises area radius and offset limits | 2 |
| Filter | Adds nine filter slots per card | 2 |
| Creative | Removes FE cost and includes overclock behavior | 1 |
| Fortune | Adds one vanilla Fortune level per card in Gel Generators and Crystal Incubators; adds 10% average output per card in both Greenhouses | 8 (3 in Greenhouse) |
| Precision | Crystal Incubator only; harvests through vanilla Silk Touch loot logic and conflicts with Fortune | 1 |
| AE Acceleration | Time Accelerator tiers only; accelerates individual AE2 devices by default or a whole Grid with JDTE-AE | 1 |
| AE Crafting Read | Lets compatible machines read active crafting tasks from a linked AE2 network; pauses when unlinked, offline, or idle | 1 |
| AE Output | Installable in any JDTE machine; once bound in a Wireless Access Point it returns item or fluid products directly to that AE network | 1 |
| Essence Conversion | Greenhouses only; converts harvested essences when they have exactly one essence-only crafting recipe | 1 |
| Seed Conversion | Greenhouses only; converts harvested copies of the planted Mystical Agriculture seed into its essence | 1 |
| AE Extraction | Binds to a Wireless Access Point and refills carried JDT/JDTE FE and fluid items; supports Applied Flux | 1 |
| Looting | Dedicated to Bio Crushers, the Loot Fabricator, and the Bio Factory | 6 / 3 / 4 |
| Sharpness | Bio Crusher only; adds five damage per card | 6 |
| Energy Brewing | Advanced Potion Brewer only; replaces Blaze Powder fuel with FE and disables the fuel slot while installed | 1 |

Overclock and Underclock cannot be installed together. The Creative Upgrade also provides relevant Overclock behavior. The first sixteen rows above are standard upgrade cards backed by the `UpgradeType` enum; Looting, Sharpness, Energy Brewing, and AE Extraction are dedicated upgrade items whose installation slots and limits are defined by their own machines. The AE Crafting Read Upgrade must first be bound by placing it in an AE2 Wireless Access Point's linking input, then installed in a compatible machine's standard or extended upgrade slot. The machine pauses when unbound, when the access point is unloaded or offline, while the network is booting, or with no crafting task; it runs automatically when a task is available after recovery. AE2 remains optional for the main `jdte` mod; without the JDTE-AE addon, the AE Acceleration Upgrade retains its per-device fallback through `IGridTickable`.

### Time And Extended Machines

- Basic Time Accelerator: 16x by default or 32x with Overclock/Creative; consumes JDT Time Fluid only.
- Advanced Time Accelerator: adjustable from 1-64x or 128x with Overclock/Creative; consumes Time Fluid and FE at twice the Basic tier's Time Fluid rate.
- Extended Time Accelerator: an eight-slot tier adjustable from 1-512x or 1024x with Overclock/Creative; consumes Time Fluid at five times the Basic tier's rate.
- All three tiers share the managed scheduler. A nominal multiplier includes the target's native tick: one nominal `X` accelerator contributes `X - 1` additional cycles, and overlaps produce `1 + Σ(Xᵢ - 1)`, so two 16x accelerators produce 31x. Ordinary block entities and random-tick targets use chunk discovery, paid pending queues, and fixed per-tick execution and scan budgets; high server MSPT does not pause them, and excess work remains queued.
- Advanced & Extended Entity Accelerators: machines consuming Time Fluid and FE to directly accelerate living entities (`LivingEntity`) in a configurable area with adjustable multiplier 1-64x (Extended tier 1-512x, up to 1024x overclocked). Automatically suspends entity AI pathfinding, navigation, and pushing physics during accelerated extra ticks to ensure stable biological ticking without erratic movement; supports ghost spawn egg filtering and automatically excludes players.
- Full AE Grid acceleration requires matching versions of JDTE-AE (`jdte-ae`) and JDTE plus AE2 19.2.17+ on both client and server. Covering any online, fully booted node gives its entire Grid the full multiplier; covering multiple nodes on the same Grid does not count one accelerator more than once.
- AE Grid work synchronously runs complete Server Start, Level Start, Level End, and Server End lifecycles. Nominal 1024x runs 1023 additional Grid lifecycles per real tick and bypasses the ordinary target budget of 4096 executions, so it can raise server MSPT substantially. Insufficient FE or Time Fluid rejects the whole batch without execution or charge instead of silently reducing the multiplier.
- The Extended Upgrade converts JDT T2 Clickers, Block Breakers, Block Placers, Block Swappers, Droppers, Sensors, Fluid Collectors, and Fluid Placers into eight-slot variants while preserving machine data.

### Automation Machines

| Machine | Purpose |
|---|---|
| Advanced Item Collector | Inserts drops into its facing inventory before they enter the world; supports oversized-stack pre-transfer, AE2 `ME_STORAGE`, and ExtendedAE interfaces |
| Advanced Energy Transmitter | Fairly supplies every FE receiver in a configurable 3D area; fixed-budget demand batching raises overclocked throughput without more scans, AE2 plus Applied Flux enables direct ME-cable access to FE stored on energy disks, and an optional player binding prioritizes FE equipment in the online player's hotbar, hands, armor, and Curios slots across dimensions |
| AE Extraction Upgrade | Bind it in an AE2 Wireless Access Point, then smith it onto a JDT/JDTE FE or fluid item to refill carried resources; Applied Flux is optional and empty universal fluid containers are never selected speculatively |
| Entity Suppressor | Suppresses entity updates, prevents entity spawning, disables entity rendering, disables block entity rendering, or disables particles |
| Range Blocker | Contains mobs inside an area or prevents player magnets from attracting items within it |
| Glue Activator | Automates JDT glue operations |
| Gel Generator | Performs JDT goo-spread conversions; Fortune Upgrades increase JDT raw ore output |
| Fluid Stabilizer | Performs JDT FluidDrop conversions inside a configured area |
| Item/Fluid Senders | Send internal items or fluid to area targets |
| Item/Fluid Receivers | Pull items or fluid from area targets |
| Crystal Incubator | Consumes Time Fluid and FE to accelerate conventional budding blocks at an adjustable 1-512x or 1024x when overclocked, auto-outputs mature clusters, and supports Fortune or Precision harvesting |
| Greenhouse | Original horizontally connectable machine with four stackable plant templates, 1-4 pages of high-capacity internal output, Fortune, JEI, real-tick-coalesced acceleration, and bounded Auto I/O |
| Large Greenhouse | Places as one 3×3×2 machine with its sole controller at the front-center of the bottom layer, nine stackable plant templates, and up to 64 unified output slots; all base-layer faces accept input/output |
| Greenhouse Matrix, Tiered Solar Panels | Moved to the standalone **JDTE-Matrix** addon (`jdte_matrix`); JDTE keeps only the member-side API its greenhouses need, while the matrix controller, structure blocks, and all five solar panel tiers ship in the new mod |
| Bio Factory | Uses reusable spawn eggs or Productive Bees cages, food/flowers, FE, and separate Life/Time/culture/product fluids; supports the Life Fluid Bee flowering on Life Extractors, adjustable 1-32x or 64x operation, auto I/O, eight default outputs, loaded bee JEI recipes, and all four Productivity Upgrade tiers |
| Life Breeder | Automatically feeds and pairs standard animals or Villagers in a configured area, advances baby growth and breeding cooldowns at 1-32x, completes them with Overclock/Creative, supports spawn-egg allowlist/denylist filters and time-derived Life Fluid costs, and collects bounded batches of real drops into 4x2 outputs |
| Life Synthesis Vat | Places as one 3×3×2 culture vat that grows tissue from organic media, Water, and FE and distills it into Life Fluid; plant/protein/enriched recipe tiers, Time Fluid doubling boost, direct-neighbor distillation priority, and a progress-tracking red viewport liquid column |
| Factory Packer | Transactionally relocates blocks, populated block entities, non-player entities, and scheduled ticks with live-source recapture, safe Mekanism reactor and radioactive-transmitter handling, dependent multiblock teardown support, cached previews, rotation, AE2 move strategies, asynchronous I/O, rollback, and restart recovery |
| Bio Crusher | Kills targets through a FakePlayer and produces loot and Experience Fluid; supports spawners and dedicated upgrades |
| Life Extractor | Converts target health into Life Fluid without normal drops or experience |
| Infusion Machine | Processes gel, item, and dynamic spawn-egg infusion recipes; with Productive Bees, one Egg, 64 B of Life Fluid, and 1,000,000 FE create a Life Fluid Bee |
| Advanced Potion Brewer | Ordered six-step brewing with recipe locking, water and Time Fluid, auto I/O, a separate external Blaze Powder input toggle, and JEI brewing chains |
| Loot Fabricator | Uses spawn egg templates, Life Fluid, Time Fluid, and FE to manufacture mob loot |
| Mineral Extractor | Produces weighted ore batches from its local biome or a Mineral Survey; supports Experience/Time Fluid, smelting, filtering, paged outputs, auto I/O, and adjustable operation up to 64x |
| Large Mineral Extractor | A 3×3×2 multiblock with its controller at the front-center of the bottom layer; merges up to four Mineral Surveys, has four times the standard extractor's base throughput, and exposes capabilities and auto I/O only through outer structure parts |

### Automatic I/O

Machines with real item or fluid interfaces can configure each absolute world direction:

`Disabled -> Auto Input/Output -> Auto Input (orange) -> Auto Output (blue) -> Disabled`

Unsupported modes are skipped. Senders expose Auto Input only and Receivers expose Auto Output only. Auto I/O defaults to batches of 10,000 items or 1,000,000 mB. Senders and Receivers default to 64 items or 20,000 mB and gain higher throughput with an Overclock or Creative Upgrade.

### Time Multitool

- Automatically combines pickaxe, shovel, axe, and hoe behavior. Normal use tills soil, strips logs, flattens paths, or extinguishes campfires without switching tools.
- Reuses the JDT Eclipse Alloy Paxel ability and upgrade system and stores 500,000 FE plus 1,000,000 mB of Time Fluid.
- Sneak-use in air cycles 1x/2x/4x/16x/256x/1024x mining; sneak-use on a block opens the JDT tool settings.
- 1x costs no Time Fluid. Faster modes consume 2/4/16/256/1024 mB per successfully broken block and fall back to 1x without partial draining when fluid is insufficient.

### Eclipse Alloy Wrench

- Right-click rotates compatible machines; sneak-right-click picks up supported machines while preserving NBT.
- Standard wrench tags allow mods such as AE2 to use their native rotation and dismantling behavior.
- Two left-clicked corners define an area with a JDT-style preview and live dimensions; left-click an area machine to write the selection.
- Applied selections remain locked for reuse across multiple machines; Shift-left-click clears them.
- Supports FTB Ultimine bulk operations and prevents accidental Creative-mode block breaking while held.

### Compatibility And Information

- Jade displays icons, localized names, and aggregated counts for installed upgrades, plus ME network and bound-player charging status for the Advanced Energy Transmitter.
- JEI categories cover the Gel Generator, Infusion Machine, Advanced Potion Brewer, and Loot Fabricator.
- Optional integrations include FTB Ultimine, AE2/ExtendedAE, Mekanism, Apothic Spawners, Draconic Evolution, and Productive Bees; the latter adds a non-self-breeding Life Fluid Bee, Life Extractor flowering, and 250 mB centrifuge output per comb.

### Modpack Content Controls

`config/jdte/jdte.toml` can disable JDTE content without removing registry IDs. Disabled blocks are hidden from Creative tabs and cannot be newly placed, opened, ticked, or accessed through automation capabilities. Existing blocks remain in old worlds and can still be removed, avoiding missing-registry save damage. `disabledRecipes` accepts exact recipe IDs and directory prefixes; for example, `greenhouse` matches both `jdte:greenhouse` and `jdte:greenhouse/*`.

```toml
[jdte.content]
disabledBlocks = ["advanced_item_collector", "bio_factory"]
disabledRecipes = ["greenhouse"]
timeAcceleratorEnabled = false
timeFreezerEnabled = false

[jdte.greenhouse]
recipeGenerationEnabled = false
```

The Greenhouse, Loot Fabricator, and Bio Factory `recipeGenerationEnabled` switches only stop dynamic JEI recipe generation; data-pack/KubeJS recipes and the machines remain usable. Add the block ID to `disabledBlocks` to disable the machine itself. See [KUBEJS_EN.md](KUBEJS_EN.md) for complete English KubeJS recipe replacement and content-control examples.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.216+` (development builds use `21.1.233`)
- Just Dire Things `1.5.7+`
- Java `21`

Place `jdte-x.x.x.jar` in both the client and server `mods` folders. Full AE Grid acceleration additionally requires the matching `jdte-ae-x.x.x.jar` and AE2 19.2.17+ on both sides; the main mod alone does not require AE2.

## Development Build

```bash
./gradlew compileJava
./gradlew jar
./gradlew runClient
./gradlew runServer
```

See [AGENTS.md](AGENTS.md) and [开发文档.md](开发文档.md) for architecture and development workflows. See [CHANGELOG.md](CHANGELOG.md) for release history.

## License

MIT License
