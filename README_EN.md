# JDT Extras

JDT Extras (`jdte`) is a NeoForge addon for [Just Dire Things](https://www.curseforge.com/minecraft/mc-mods/just-dire-things). It adds upgrade cards, more upgrade slots, time accelerators, and extra automation machines for JDT.

Current version: `0.3.0`

[中文 README](README.md)

## Features

### Upgrade Cards (9 Types)

Upgrade cards can be installed into supported JDT/JDTE machines. Standard machines have 4 upgrade slots, while extended machines have 8 upgrade slots.

| Upgrade | Effect | Max per Machine |
|---|---|---|
| Capacity | Doubles machine FE capacity and fluid capacity | 3 |
| Overclock | Forces 1 tick operation and runs twice per tick, tripling energy cost | 1 |
| Underclock | Forces 40 tick operation and reduces energy cost by 80% | 1 |
| Fluid | Doubles fluid capacity only | 3 |
| Fluid Storage | Clicker only; adds an internal fluid tank and automatically fills fluid containers in the clicker slot | 1 |
| Generator | Generator only; consumes twice the fuel input for triple generation | 1 |
| Range | Area machines only; doubles the configurable area limit | 2 |
| Filter | Adds extra filter slots to filterable machines; each card adds 9 slots | 2 |
| Creative | Machines operate without FE cost; time accelerators operate without Time Fluid cost; includes the overclock effect | 1 |

Overclock and underclock upgrades cannot be installed together.

### Time Accelerators

- **Basic Time Accelerator**: Uses JDT Time Fluid only. Runs at 4x by default, or 16x with overclock or creative upgrade.
- **Advanced Time Accelerator**: Uses JDT Time Fluid and FE. Adjustable from 1-128x, or 256x with overclock or creative upgrade.
- **Extended Time Accelerator**: Extended version of the advanced time accelerator with 8 upgrade slots.

The **Time Fluid Catalyst** can directly trigger a source-water to JDT Time Fluid FluidDrop conversion.

Time accelerators support area configuration, redstone control, and filters. They accelerate blocks and block entities that JDT's Time Wand can accelerate.

### Extended Advanced Machines

Use the **Extended Upgrade** on supported JDT T2 machines to convert them into JDTE extended versions with 8 upgrade slots.

- Extended Clicker T2
- Extended Block Breaker T2
- Extended Block Placer T2
- Extended Block Swapper T2
- Extended Dropper T2
- Extended Sensor T2
- Extended Fluid Collector T2
- Extended Fluid Placer T2
- Extended Time Accelerator

### Extra Automation Machines

- Glue Activator: basic, advanced, extended
- Gel Generator: advanced, extended
- Fluid Stabilizer: basic, advanced, extended; uses a catalyst slot to match JDT FluidDrop recipes and directly convert source fluids in its configured area
- Item Sender: basic, advanced, extended
- Fluid Sender: basic, advanced, extended
- Item Receiver: basic, advanced, extended
- Fluid Receiver: basic, advanced, extended
- Bio Crusher: advanced, extended; kills mobs to generate drops and experience fluid, supports looting and sharpness upgrades, can be placed above mob spawners to prevent spawning

### Bio Crusher

The Bio Crusher is a new machine that kills mobs in its range and generates drops and experience fluid.

- **Advanced Bio Crusher**: 4 standard upgrade slots + 2 dedicated upgrade slots
- **Extended Bio Crusher**: 8 standard upgrade slots + 2 dedicated upgrade slots

Dedicated upgrade slots support:
- **Looting Upgrade**: Up to level 6, each level adds 50% extra drop chance
- **Sharpness Upgrade**: Up to 6, each adds 5 damage

Special feature: Place above a mob spawner to prevent spawning and generate drops + experience fluid directly.

### Boss Essences

The Bio Crusher can crush bosses to generate unique essences:
- Wither Essence
- Ender Dragon Essence
- Elder Guardian Essence

## Installation

1. Install Minecraft `1.21.1`.
2. Install NeoForge `21.1.230+`.
3. Install Just Dire Things `1.5.7+`.
4. Place `jdte-x.x.x.jar` into the client and server `mods` folders.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.230+`
- Just Dire Things `1.5.7+`
- Java `21`

## Development

This project is built with Gradle. The development environment expects the JDT jar at:

```text
/home/guili/libs/justdirethings-1.5.7.jar
```

Common commands:

```bash
./gradlew compileJava
./gradlew jar
./gradlew runClient
./gradlew runServer
```

## Project Structure

- `src/main/java/com/jdte/common`: block entities, blocks, containers, items, upgrade system, and networking.
- `src/main/java/com/jdte/client`: client screens, rendering, and client setup.
- `src/main/java/com/jdte/mixin`: runtime injections into JDT and client screens.
- `src/main/resources/assets/jdte`: language files, models, textures, and GuideME docs.
- `src/main/resources/data/jdte`: recipes, loot tables, and other data files.

## License

MIT License
