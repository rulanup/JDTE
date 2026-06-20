# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JDT Extras (`jdte`) is a NeoForge addon for **Just Dire Things** (JDT) that adds:
- 11 upgrade cards (capacity, overclock, underclock, fluid, fluid_storage, generator, range, filter, creative, looting, sharpness)
- 3 time accelerators (basic/advanced/extended)
- 8 extended JDT T2 machines with 8 upgrade slots each (up from JDT's default)
- An extended time accelerator with 8 upgrade slots
- Extra automation machine families: glue activator, gel generator, fluid stabilizer, item/fluid sender, item/fluid receiver
- Bio crusher (advanced/extended) with looting and sharpness upgrades, boss essences
- Life extractor and infusion machine
- An extended upgrade item to convert supported T2 machines or the advanced time accelerator into extended versions

Target: Minecraft 1.21.1, NeoForge 21.1.230+, JDT 1.5.7+, Java 21

## Build Commands

```bash
# Compile
./gradlew compileJava

# Build JAR
./gradlew jar

# Run client (dev)
./gradlew runClient

# Run server (dev)
./gradlew runServer
```

**Prerequisite**: JDT jar must exist at `/home/guili/libs/justdirethings-1.5.7.jar`. Build JDT first if missing.

## Architecture

### Dependency on JDT

JDT is a compile-only + runtime dependency (not a Gradle submodule). The mod extends JDT classes directly:
- JDT source reference: `~/idea-projects/JustDireThings-1.21.1`
- JDT package: `com.direwolf20.justdirethings`
- JDT registration: `com.direwolf20.justdirethings.setup.Registration`

### Core Patterns

**Extended Machines**: JDT-derived extended machines (e.g., `ExtendedClickerBE`) generally extend the matching JDT T1 BE class (e.g., `ClickerT1BE`) and implement additional interfaces such as `PoweredMachineBE`, `AreaAffectingBE`, `FilterableBE`, and `ExtendedUpgradeMachine`. `ExtendedTimeAcceleratorBE` extends `AdvancedTimeAcceleratorBE`. The `ExtendedUpgradeMachine` interface is a marker — it has no methods.

**Upgrade System**: `UpgradeType` enum defines upgrade types with serialization names and max-per-machine limits. `UpgradeItemStackHandler` (4 slots) and `ExtendedUpgradeItemStackHandler` (8 slots) manage upgrade inventories. `UpgradeHelper` provides static utility methods for capacity, cost, tick speed, range, filters, creative behavior, and Clicker fluid storage.

**Mixin Injection**: 20+ Mixins modify JDT base classes at runtime. Key conventions:
- Method prefixes: `jdte$methodName`
- Field prefixes: `jdte$fieldName`
- Use `@Shadow` for target class fields
- Use reflection when calling methods not on the target class
- Mixin classes must NOT extend any class (except Object)

### Registration Order

1. `JDTEBlocks` — block registration
2. `JDTEItems` — item registration (includes BlockItems for blocks)
3. `JDTEBlockEntities` — block entity types
4. `JDTEMenus` — container/menu types
5. `JDTEAttachments` — data attachment types
6. `JDTECreativeTabs` — creative mode tab
7. `JDTEEntities` — entity types

All registrations happen in `JDTE.java` constructor via `DeferredRegister` pattern.

### Capabilities

Energy, fluid, and item capabilities are registered in `JDTE.registerCapabilities()`. This includes Clicker fluid storage, time accelerators, extended JDT machines, glue activators, gel generators, the fluid stabilizer, item/fluid sender/receiver machines, bio crusher, life extractor, and infusion machine. Item handler (`Capabilities.ItemHandler.BLOCK`) is registered for all machines with item slots so external pipes (e.g. Mekanism) can insert/extract items.

### Client/Server Split

- `client/` — screens, renderers, entity renders (client-only)
- `common/` — block entities, blocks, containers, items, upgrades, network (both sides)
- `mixin/` — server mixins in `mixins` array, client mixins in `client` array of `mixins.jdte.json`

## Resource Files

- Block states: `assets/jdte/blockstates/{name}.json`
- Block models: `assets/jdte/models/block/{name}.json`
- Item models: `assets/jdte/models/item/{name}.json`
- Textures: `assets/jdte/textures/block/` and `assets/jdte/textures/item/`
- Recipes: `data/jdte/recipe/{name}.json`
- Loot tables: `data/jdte/loot_table/`
- Translations: `assets/jdte/lang/en_us.json` and `zh_cn.json`
- GuideME config: `assets/jdte/guideme_guides/guide.json`
- GuideME docs: `assets/jdte/guides/jdte/guide/`

## Common Pitfalls

- Mixin `@Shadow` field names must match the obfuscated/mapped names exactly. Use `javap` to verify.
- `stillValid()` in container classes must check for the correct block type, not just `BaseMachineBlock`.
- Don't inherit Mixin classes from anything except Object.
- Extended BE constructors must pass their own `BlockEntityType` from `JDTEBlockEntities`, not the parent's.
- When adding convertible extended machines, update `ExtendedUpgradeItem.UPGRADE_MAP`.
- Keep `README.md`, `README_EN.md`, `AGENTS.md`, `开发文档.md`, and GuideME pages in sync with gameplay changes.
