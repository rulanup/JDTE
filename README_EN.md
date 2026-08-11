# JDT Extras

JDT Extras (`jdte`) is a NeoForge addon for [Just Dire Things](https://www.curseforge.com/minecraft/mc-mods/just-dire-things). It adds upgrade cards, extended machines, time acceleration, area control, and automation devices for JDT.

Current version: `0.5.9-alpha1`

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
| Looting | Dedicated to Bio Crushers and the Loot Fabricator | 6 |
| Sharpness | Bio Crusher only; adds five damage per card | 6 |

Overclock and Underclock cannot be installed together. The Creative Upgrade also provides relevant Overclock behavior.

### Time And Extended Machines

- Basic Time Accelerator: 16x by default or 32x with Overclock/Creative; consumes JDT Time Fluid only.
- Advanced Time Accelerator: adjustable from 1-64x or 128x with Overclock/Creative; consumes Time Fluid and FE at twice the Basic tier's Time Fluid rate.
- Extended Time Accelerator: an eight-slot tier adjustable from 1-512x or 1024x with Overclock/Creative; consumes Time Fluid at five times the Basic tier's rate.
- All three tiers share the managed scheduler. Overlapping multipliers fully stack while chunk target discovery, paid virtual-tick queues, and fixed per-tick execution and scan budgets reduce large-area and multi-machine overhead. Acceleration no longer stops when server MSPT is high, and AE2 `IGridTickable` devices remain supported.
- The Extended Upgrade converts JDT T2 Clickers, Block Breakers, Block Placers, Block Swappers, Droppers, Sensors, Fluid Collectors, and Fluid Placers into eight-slot variants while preserving machine data.

### Automation Machines

| Machine | Purpose |
|---|---|
| Advanced Item Collector | Inserts drops into its facing inventory before they enter the world; supports oversized-stack pre-transfer, AE2 `ME_STORAGE`, and ExtendedAE interfaces |
| Advanced Energy Transmitter | Fairly supplies every FE receiver in a configurable 3D area; fixed-budget demand batching raises overclocked throughput without more scans, AE2 plus Applied Flux enables direct ME-cable access to FE stored on energy disks, and an optional player binding prioritizes FE equipment in the online player's hotbar, hands, armor, and Curios slots across dimensions |
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

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.215+`
- Just Dire Things `1.5.7+`
- Java `21`

Place `jdte-x.x.x.jar` in both the client and server `mods` folders.

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
