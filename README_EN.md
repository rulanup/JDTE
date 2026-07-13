# JDT Extras

JDT Extras (`jdte`) is a NeoForge addon for [Just Dire Things](https://www.curseforge.com/minecraft/mc-mods/just-dire-things). It adds upgrade cards, extended machines, time acceleration, area control, and automation devices for JDT.

Current version: `0.5.4`

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
| Fortune | Gel Generator only; adds one vanilla ore Fortune level and 5% energy cost per card | 8 |
| Looting | Dedicated to Bio Crushers and the Loot Fabricator | 6 |
| Sharpness | Bio Crusher only; adds five damage per card | 6 |

Overclock and Underclock cannot be installed together. The Creative Upgrade also provides relevant Overclock behavior.

### Time And Extended Machines

- Basic Time Accelerator: 4x by default or 16x with Overclock/Creative; consumes JDT Time Fluid only.
- Advanced Time Accelerator: adjustable from 1-128x or 256x with Overclock/Creative; consumes Time Fluid and FE.
- Extended Time Accelerator: the eight-slot Advanced variant.
- The Extended Upgrade converts JDT T2 Clickers, Block Breakers, Block Placers, Block Swappers, Droppers, Sensors, Fluid Collectors, and Fluid Placers into eight-slot variants while preserving machine data.

### Automation Machines

| Machine | Purpose |
|---|---|
| Advanced Item Collector | Inserts drops into its facing inventory before they enter the world; supports oversized-stack pre-transfer, AE2 `ME_STORAGE`, and ExtendedAE interfaces |
| Entity Suppressor | Suppresses entity updates, prevents entity spawning, disables entity rendering, disables block entity rendering, or disables particles |
| Range Blocker | Contains mobs inside an area or prevents player magnets from attracting items within it |
| Glue Activator | Automates JDT glue operations |
| Gel Generator | Performs JDT goo-spread conversions; Fortune Upgrades increase JDT raw ore output |
| Fluid Stabilizer | Performs JDT FluidDrop conversions inside a configured area |
| Item/Fluid Senders | Send internal items or fluid to area targets |
| Item/Fluid Receivers | Pull items or fluid from area targets |
| Bio Crusher | Kills targets through a FakePlayer and produces loot and Experience Fluid; supports spawners and dedicated upgrades |
| Life Extractor | Converts target health into Life Fluid without normal drops or experience |
| Infusion Machine | Processes gel, item, and dynamic spawn-egg infusion recipes |
| Advanced Potion Brewer | Ordered six-step brewing with recipe locking, water and Time Fluid, auto I/O, and JEI brewing chains |
| Loot Fabricator | Uses spawn egg templates, Life Fluid, Time Fluid, and FE to manufacture mob loot |

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

- Jade displays icons, localized names, and aggregated counts for installed upgrades.
- JEI categories cover the Gel Generator, Infusion Machine, Advanced Potion Brewer, and Loot Fabricator.
- Optional integrations include FTB Ultimine, AE2/ExtendedAE, Mekanism, Apothic Spawners, and Draconic Evolution.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.233+`
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
