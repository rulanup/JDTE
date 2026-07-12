# JDT Extras Developer Guide

## Project Overview

JDT Extras (`jdte`) is a NeoForge extension for Just Dire Things (JDT). It adds upgrade cards, time accelerators, extended machines, automation devices, and optional cross-mod integrations.

| Property | Value |
|----------|-------|
| Mod ID | `jdte` |
| Mod name | `JDT Extras` |
| Current version | `0.5.3` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.233+` |
| Just Dire Things | `1.5.7+` |
| Java | `21` |

Major features:

- 11 upgrade items: Capacity, Overclock, Underclock, Fluid, Fluid Storage, Generator, Range, Filter, Creative, Looting, and Sharpness.
- Basic, Advanced, and Extended Advanced Time Accelerators.
- Eight extended variants of JDT T2 machines, each with eight standard upgrade slots.
- Glue Activators, Gel Generators, Fluid Stabilizers, and item/fluid sender and receiver families.
- Advanced and Extended Bio Crushers, Life Extractors, and Infusion Machines.
- Advanced Potion Brewer with ordered six-step brewing, recipe locking, auto I/O, and JEI brewing chains.
- Loot Fabricator using spawn egg templates, Life Fluid, Time Fluid, and FE to produce mob loot.
- Eclipse Alloy Wrench for rotation and NBT-preserving machine pickup, with optional FTB Ultimine bulk operations.
- Permanent Life Apple progression and JEI categories for machine recipes.
- Absolute-direction auto I/O configuration for machines with real item or fluid interfaces.
- Wither, Ender Dragon, and Elder Guardian essences.

## Build and Run

The project uses Gradle with NeoForge ModDev. JDT is loaded as a local jar rather than a Gradle submodule.

Expected local dependency:

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

If the build reports that the JDT jar is missing, build or copy JDT 1.5.7 to the configured path first. On Windows, update the path in the local Gradle configuration when necessary.

## Core Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| NeoForge | `21.1.233` | Mod loader and API |
| Just Dire Things | `1.5.7` | Base machines, interfaces, config, and Time Fluid |
| GuideME | `21.1.16` | In-game documentation |
| JEI | `19.27.0.340` | Recipe categories, catalysts, information pages, and GUI click areas |
| Apothic Spawners | CurseForge `7492121` | Optional spawner cycle and XP compatibility |
| Draconic Evolution | CurseForge `7584459` | Optional Chaos Guardian and dragon loot compatibility |
| FTB Ultimine | `2101.1.13` | Optional bulk wrench operations |
| Parchment | `2024.11.17` | Minecraft mappings |

Important JDT packages:

- `com.direwolf20.justdirethings`
- `com.direwolf20.justdirethings.setup.Registration`
- `com.direwolf20.justdirethings.setup.Config`

## Project Layout

```text
src/main/
|-- java/com/jdte/
|   |-- JDTE.java                         # Mod entry point, capabilities, networking hook
|   |-- client/                           # Client setup, screens, rendering, client caches
|   |-- common/
|   |   |-- autoioconfig/                 # Absolute-direction auto I/O state and transfers
|   |   |-- blockentities/                # Block entities and machine logic
|   |   |-- blocks/                       # Block definitions
|   |   |-- containers/                   # Menus, containers, and custom slots
|   |   |-- entities/                     # Time accelerator visual entities
|   |   |-- integrations/                 # Apothic, Draconic Evolution, FTB Ultimine
|   |   |-- items/                        # Upgrade cards, wrench, and conversion items
|   |   |-- jei/                          # JEI plugin, recipe wrappers, and categories
|   |   |-- network/                      # Payloads and packet handlers
|   |   |-- player/                       # Life Apple attachment and progression
|   |   |-- recipes/                      # Machine recipe models
|   |   |-- upgrades/                     # Upgrade system core
|   |   `-- utils/                        # Shared utilities
|   |-- mixin/                            # Mixins, accessors, and invokers
|   `-- setup/                            # DeferredRegister setup classes
`-- resources/
    |-- assets/jdte/
    |   |-- blockstates/
    |   |-- guideme_guides/
    |   |-- guides/jdte/guide/
    |   |-- lang/
    |   |-- models/
    |   |-- textures/
    |   `-- gui_layout.json               # Embedded upgrade panel and machine layouts
    |-- data/jdte/                        # Recipes, loot tables, and tags
    `-- META-INF/neoforge.mods.toml
```

## Registration Flow

Core registration is performed from the `JDTE` constructor in this order:

1. `JDTEBlocks`
2. `JDTEItems`
3. `JDTEBlockEntities`
4. `JDTEMenus`
5. `JDTEAttachments`
6. `JDTECreativeTabs`
7. `JDTEEntities`
8. `JDTE.registerCapabilities()`
9. `JDTEPacketHandler.registerNetworking()`

Adding a machine usually requires coordinated changes to `JDTEBlocks`, `JDTEItems`, `JDTEBlockEntities`, `JDTEMenus`, `JDTECreativeTabs`, `JDTEClientSetup`, and `JDTE.registerCapabilities()`.

## Upgrade System

### Upgrade Types

`UpgradeType` defines the serialized name and per-machine limit for standard upgrades.

| Type | Serialized name | Limit | Effect |
|------|-----------------|-------|--------|
| `CAPACITY` | `capacity` | 3 | Multiplies FE and fluid capacity by powers of two |
| `OVERCLOCK` | `overclock` | 1 | Locks operation to one tick, allows extra execution, and triples energy use |
| `UNDERCLOCK` | `underclock` | 1 | Locks operation to 40 ticks and reduces energy use to 20% |
| `FLUID` | `fluid` | 3 | Multiplies fluid capacity only |
| `FLUID_STORAGE` | `fluid_storage` | 1 | Adds an internal tank to Clickers |
| `GENERATOR` | `generator` | 1 | Uses twice the fuel input for three times the generation |
| `RANGE` | `range` | 2 | Raises configurable area limits |
| `FILTER` | `filter` | 2 | Adds nine filter slots per card |
| `CREATIVE` | `creative` | 1 | Removes FE cost, removes Time Fluid cost for accelerators, and includes overclock behavior |

Looting and Sharpness are dedicated upgrade items and are not members of `UpgradeType`. Bio Crushers accept up to six of each in dedicated slots. The Loot Fabricator uses `LootFabricatorUpgradeItemStackHandler` to allow up to three Looting Upgrades alongside eight standard slots.

### Upgrade Handlers

- `UpgradeItemStackHandler` provides four standard slots.
- `ExtendedUpgradeItemStackHandler` provides eight standard slots.
- Each upgrade slot has a stack limit of one.
- `UpgradeItemStackHandler.isItemValid()` checks item type, machine compatibility, aggregate limits, and Overclock/Underclock conflicts.
- `UpgradeHelper.getUpgradeHandler()` selects the standard or extended attachment through the `ExtendedUpgradeMachine` marker.
- `GuiUpgradeLayoutConfig` reads `assets/jdte/gui_layout.json`; machine screens use fixed embedded panels instead of draggable upgrade popups.

### Capacity and Cost

- `UpgradeHelper.adjustEnergyCapacity()` applies `CAPACITY`.
- `UpgradeHelper.adjustFluidCapacity()` applies both `CAPACITY` and `FLUID`.
- `UpgradeHelper.adjustEnergyCost()` applies Creative, Underclock, and Overclock rules.
- `UpgradeHelper.getEffectiveTickSpeed()` calculates locked machine speed.
- `UpgradeHelper.getMaxAreaRadius()` and `getMaxAreaOffset()` apply Range upgrades.
- `UpgradeHelper.getActiveFilterSlots()` calculates active paged filter slots.
- `UpgradeHelper.fillClickerItemFromTank()` transfers a Clicker's internal fluid into its slotted item.

## Time Accelerators

`TimeAcceleratorBE` extends JDT `BaseMachineBE` and implements `RedstoneControlledBE`, `AreaAffectingBE`, `FluidMachineBE`, `FilterableBE`, and `TimeAcceleratorMachine`.

Core behavior:

- The base tank holds `1000 mB` and accepts JDT Time Fluid only.
- Each server tick checks redstone state and resources before scanning the configured area.
- Other `TimeAcceleratorMachine` instances are skipped to prevent recursive acceleration.
- Only blocks accepted by `MiscTools.isValidTickAccelBlock()` are accelerated.
- Block entity tickers and random block ticks are invoked repeatedly according to the effective multiplier.
- FE and Time Fluid are consumed only when acceleration succeeds.
- Filter pages restrict eligible targets.

| Block entity | Inheritance | Default behavior |
|--------------|-------------|------------------|
| `BasicTimeAcceleratorBE` | `TimeAcceleratorBE` | 4x by default, 16x with Overclock/Creative, Time Fluid only |
| `AdvancedTimeAcceleratorBE` | `TimeAcceleratorBE`, `PoweredMachineBE` | Adjustable 1-128x, 256x with Overclock/Creative, Time Fluid and FE |
| `ExtendedTimeAcceleratorBE` | `AdvancedTimeAcceleratorBE`, `ExtendedUpgradeMachine` | Eight-slot Advanced variant |

Advanced defaults include `BASE_ENERGY_CAPACITY = 200000`, `MAX_MULTIPLIER = 128`, and `OVERCLOCK_MULTIPLIER = 256`. FE cost derives from `Config.TIMEWAND_RF_COST`; fluid cost derives from `Config.TIMEWAND_FLUID_COST` and `JDTEConfig.COMMON.timeAcceleratorFluidCostMultiplier`. The multiplier can be changed with `/jdte timeaccelerator fluidCostMultiplier <value>`.

## Extended Machines

`ExtendedUpgradeMachine` grants eight upgrade slots. Using `ExtendedUpgradeItem` on a supported JDT T2 machine converts it while preserving and restoring block entity NBT.

| Source | Extended block | Block entity | Base class |
|--------|----------------|--------------|------------|
| JDT Clicker T2 | `extended_clicker` | `ExtendedClickerBE` | `ClickerT1BE` |
| JDT Block Breaker T2 | `extended_block_breaker` | `ExtendedBlockBreakerBE` | `BlockBreakerT1BE` |
| JDT Block Placer T2 | `extended_block_placer` | `ExtendedBlockPlacerBE` | `BlockPlacerT1BE` |
| JDT Block Swapper T2 | `extended_block_swapper` | `ExtendedBlockSwapperBE` | `BlockSwapperT1BE` |
| JDT Dropper T2 | `extended_dropper` | `ExtendedDropperBE` | `DropperT1BE` |
| JDT Sensor T2 | `extended_sensor` | `ExtendedSensorBE` | `SensorT1BE` |
| JDT Fluid Collector T2 | `extended_fluid_collector` | `ExtendedFluidCollectorBE` | `FluidCollectorT1BE` |
| JDT Fluid Placer T2 | `extended_fluid_placer` | `ExtendedFluidPlacerBE` | `FluidPlacerT1BE` |
| JDTE Advanced Time Accelerator | `extended_time_accelerator` | `ExtendedTimeAcceleratorBE` | `AdvancedTimeAcceleratorBE` |

Rules:

- Constructors must pass their own `JDTEBlockEntities` type, not a parent type.
- Container `stillValid()` checks must recognize the actual extended block.
- Every required capability must be registered explicitly in `JDTE.registerCapabilities()`.
- Add conversion targets to `ExtendedUpgradeItem.UPGRADE_MAP`.

## Automation Machines

| Family | Tiers | Base block entity | Purpose |
|--------|-------|-------------------|---------|
| Glue Activator | Basic, Advanced, Extended | `GlueActivatorBE` | Automates JDT glue operations |
| Gel Generator | Advanced, Extended | `GelGeneratorBE` | Converts input into gel-related output using fluid and FE |
| Fluid Stabilizer | Basic, Advanced, Extended | `FluidStabilizerBE` | Applies JDT FluidDrop recipes to source fluids in an area |
| Item Sender | Basic, Advanced, Extended | `ItemSenderBE` | Sends items to configured targets |
| Fluid Sender | Basic, Advanced, Extended | `FluidSenderBE` | Sends internal fluid to configured targets |
| Item Receiver | Basic, Advanced, Extended | `ItemReceiverBE` | Pulls items from configured targets |
| Fluid Receiver | Basic, Advanced, Extended | `FluidReceiverBE` | Pulls fluid from configured targets |
| Bio Crusher | Advanced, Extended | `BioCrusherBE` | Produces mob loot and XP fluid, including spawner integration |
| Life Extractor | Advanced, Extended | `LifeExtractorBE` | Converts target health into Life Fluid without normal drops |
| Infusion Machine | Advanced, Extended | `InfusionMachineBE` | Performs gel/item and dynamic spawn egg infusion |
| Advanced Potion Brewer | Advanced | `AdvancedPotionBrewerBE` | Ordered brewing using water, Time Fluid, fuel, and FE |
| Loot Fabricator | Single eight-slot tier | `LootFabricatorBE` | Produces mob loot from reusable spawn egg templates |

Capability summary:

- Time Accelerators expose fluid; Advanced and Extended variants also expose energy.
- Extended JDT machines expose item and energy capabilities where supported.
- Glue Activators expose items; powered tiers also expose energy.
- Gel Generators, Bio Crushers, Infusion Machines, Potion Brewers, and Loot Fabricators expose energy, fluid, and item capabilities as appropriate.
- Fluid Stabilizers expose catalyst items and powered-tier energy, but no fluid capability.
- Item sender/receiver tiers expose items; powered tiers expose energy.
- Fluid sender/receiver tiers expose fluid; powered tiers expose energy.
- Life Extractors expose energy and fluid.
- JDT Clickers expose fluid when `FLUID_STORAGE` is installed.

Machines with real I/O persist absolute side settings through `AutoIoConfigData`; `AutoIoTransferHelper` executes transfers on the server. Any slot-layout change must also verify auto-I/O mappings, directional capability exposure, and the client cache.

## Mixins

Mixin configuration: `src/main/resources/mixins.jdte.json`.

Common/server mixins:

| Mixin | Purpose |
|-------|---------|
| `BaseMachineBEMixin` | Upgrade attachments, capacity synchronization, and persistence |
| `BaseMachineBEPopupMixin` | Legacy popup attachment data compatibility |
| `BaseMachineBlockMixin` | Overclock extra execution and wrench behavior hooks |
| `BaseMachineContainerMixin` | Injects standard upgrade slots into JDT menus |
| `BaseMachineContainerFilterMixin` | Provides dynamic filter slots and pagination |
| `ClickerFluidMixin` | Clicker tank upgrade behavior |
| `AreaAffectingBEMixin` | Applies Range upgrade limits |
| `EnergyCostMixin`, `ParadoxEnergyCostMixin` | Adjust machine energy costs |
| `FluidCapacityMixin` | Adjusts fluid capacity |
| `GeneratorT1UpgradeMixin`, `GeneratorFluidUpgradeMixin` | Generator upgrade behavior |
| `PoweredMachineDefaultMaxEnergyMixin`, `PoweredMachineOverrideMaxEnergyMixin` | Energy-capacity adjustment entry points |
| `SpawnerMixin` | Intercepts spawner cycles for Bio Crushers |
| `BaseSpawnerInvoker`, `ApothSpawnerInvoker` | Invoke vanilla and Apothic spawner internals |
| `TickSpeedPacketMixin` | Keeps tick-speed packets compatible with upgrades |
| Accessor mixins | Update JDT/NeoForge energy, fluid, filter, screen, and slot internals |

Client mixins:

| Mixin | Purpose |
|-------|---------|
| `BaseMachineScreenMixin` | Embedded upgrade panels, filter pages, auto I/O, fluid bars, and slot layout |
| `BaseMachineScreenAccessor` | Exposes JDT base screen state to custom screens |
| `ClickerT2ScreenMixin` | Fixes Advanced Clicker settings and embedded-panel layout |
| `ScreenMixin`, `AbstractContainerScreenMixin` | Expose screen layout fields |
| `SlotAccessor` | Repositions menu slots dynamically |

Mixin conventions:

- Do not explicitly extend the target class.
- Mark custom fields and methods `@Unique` and prefix them with `jdte$`.
- Prefer `@Shadow` for mapped target fields and verify names against the active JDT jar.
- Use reflection or interface checks when a method is not declared on the target type.
- Move GUI slots only when state or layout changes, not every frame.
- Declare new GUI layout in `gui_layout.json` and verify upgrade panels, filter pagination, auto I/O, and JEI click areas together.

## Resources

New items usually require an item model, texture, recipe, English and Chinese translations, and a GuideME entry. New blocks additionally require blockstate, block model, block texture, block loot table, and BlockItem model resources.

GuideME locations:

- Guide definition: `src/main/resources/assets/jdte/guideme_guides/guide.json`
- Pages: `src/main/resources/assets/jdte/guides/jdte/guide/`
- English translations: `src/main/resources/assets/jdte/guides/jdte/guide/_en_us/`
- Current feature pages include `advanced-potion-brewer.md`, `loot-fabricator.md`, and `eclipsealloy-wrench.md` in addition to the machine, upgrade, essence, and automation pages.

## Common Development Workflows

### Adding an Upgrade

1. Add its type, serialized name, and limit to `UpgradeType` when it is a standard upgrade.
2. Register the item in `JDTEItems`.
3. Add compatibility validation to `UpgradeItemStackHandler.isItemValid()` or a dedicated handler.
4. Implement the behavior in `UpgradeHelper`, the relevant mixin, or the block entity.
5. Add it to `JDTEItems.upgrades()` when it belongs in the creative tab.
6. Add model, texture, recipe, translations, and tooltip.
7. Update GuideME, README, changelog, and developer documentation.
8. Run `./gradlew compileJava`.

### Adding a Machine

1. Create the block, block entity, container, and any client screen or renderer.
2. Register it in `JDTEBlocks`, `JDTEItems`, `JDTEBlockEntities`, and `JDTEMenus`.
3. Add it to `JDTECreativeTabs` and register the screen in `JDTEClientSetup`.
4. Register all energy, fluid, and item capabilities in `JDTE.registerCapabilities()`.
5. Add blockstate, models, textures, translations, loot table, recipe, and GuideME page.
6. Implement `ExtendedUpgradeMachine` for eight-slot machines and update `ExtendedUpgradeItem.UPGRADE_MAP` when conversion is supported.
7. Define auto-I/O item/fluid semantics and test all six absolute directions when supported.
8. Update `JDTEJeiPlugin`, recipe categories, catalysts, and progress-arrow click areas when recipes are displayed. Dynamic recipes need stable IDs.
9. Add the menu layout to `gui_layout.json` and test standard, Filter, and Capacity upgrade states.
10. Run `./gradlew compileJava`, then use `./gradlew runClient` for in-game validation.

### Adding a Mixin

1. Create the class with `@Mixin(TargetClass.class)`.
2. Use the smallest practical `@Inject`, `@ModifyConstant`, accessor, or invoker.
3. Mark custom members `@Unique` with the `jdte$` prefix.
4. Register it under `mixins` or `client` in `mixins.jdte.json`.
5. Compile to validate mappings and refmap generation.
6. Run the client or server to verify that the mixin applies.

## Debugging and Validation

Recommended order:

```bash
./gradlew compileJava
./gradlew jar
./gradlew runClient
```

| Problem | Common cause | Check |
|---------|--------------|-------|
| `JDT jar not found` | Missing local dependency | Verify the configured JDT 1.5.7 jar path |
| Mixin apply failure | Target method or field changed | Inspect the active JDT jar with `javap` or source |
| `InvalidAccessor` | Wrong mapped field name or type | Compare the accessor with the actual target field |
| GUI crash | Invalid block check or slot injection | Inspect `stillValid()`, menu type, and slot indices |
| GUI panel misalignment | Missing or incorrect layout entry | Check `assets/jdte/gui_layout.json` and menu keys |
| Auto I/O uses the wrong side | World direction, facing, or slot mapping mismatch | Check `AutoIoConfigHelper` and capability exposure |
| JEI dynamic recipe cannot be bookmarked | Recipe has no stable UID | Check the recipe wrapper ID implementation |
| Capacity does not synchronize | Accessor or capability registration missing | Check `UpgradeHelper.syncCapacities()` and capability setup |
| Purple/black block model | Missing model, texture, or blockstate | Check asset paths and resource IDs |
| Recipe is unavailable | Invalid recipe path or JSON | Check `data/jdte/recipe/{name}.json` |

## Version History

### v0.5.3 (Current)

- Replaced draggable upgrade popups with fixed embedded panels. Four-slot machines use a right-side 1x4 panel; eight-slot machines use left and right 1x4 panels. `GuiUpgradeLayoutConfig` and `assets/jdte/gui_layout.json` centralize layout data and upgrade tooltips.
- Added absolute-direction auto input/output state, networking, client caching, and server transfer helpers.
- Added the Loot Fabricator with reusable spawn egg templates, parallel processing, paged output, Capacity and Looting upgrades, Boss multipliers, and Draconic Evolution compatibility.
- Reworked Bio Crusher loot capture, FakePlayer attribution, spawner integration, adjusted XP handling, paged Extended output, and optional Chaos Guardian support.
- Added dynamic spawn egg infusion, dedicated Wither/Dragon/Elder Guardian recipes, stable JEI IDs, and fluid-container filling.
- Added the Advanced Potion Brewer with ordered six-step chains, server-authoritative recipe locking, restricted automation, and JEI chain display.
- Added the Eclipse Alloy Wrench, NBT-preserving pickup, optional FTB Ultimine integration, and scroll-wheel range adjustment.
- Changed Life Apples to uncapped permanent health, armor, and armor-toughness progression stored in player attachments.
- Added JEI categories and progress-arrow navigation for the Gel Generator, Infusion Machine, Potion Brewer, and Loot Fabricator.
- Fixed filter pagination, Extended Dropper/Clicker initialization, Extended Sensor GUI startup, fluid-container interaction, and Life Extractor drops/XP.
- Balanced Time Accelerator fluid cost against the JDT Time Wand and added a runtime multiplier command.

### v0.5.2

- Fixed the Extended Dropper GUI crash by restoring nine machine slots and migrating old one-slot handlers.
- Registered item capabilities for JDTE machines so external transport systems can insert and extract items.
- Fixed Extended Sensor screen registration and compatibility with JDT 1.5.7, restored nine filter slots, and corrected dynamic filter indices and page controls.
- Added Gel Generator slot tooltips and refined filter pagination visuals.

### v0.5.1

- Corrected GuideME bilingual page layout and added Life Extractor and Infusion Machine pages.
- Fixed missing Life Fluid block assets, an invalid Glue Activator recipe symbol, and duplicate GuideME item IDs.
- Standardized fluid bar placement, added filter pagination and network synchronization, and fixed empty whitelist behavior.

### v0.5.0

- Fixed global filter-slot state leakage and missing rotation transforms for 12 machines.
- Added vanilla slot borders to upgrade/filter popups and English GuideME translations.

### v0.4.0

- Added Advanced and Extended Bio Crushers, Boss Essences, Looting and Sharpness upgrades, spawner interception, configuration support, Life Extractors, and Infusion Machines.

### v0.3.0

- Detailed changes were not recorded.

### v0.2.0

- Expanded the upgrade set, added all three Time Accelerator tiers and six automation families, introduced draggable upgrade/filter popups, dynamic filter slots, and synchronized documentation.

### v0.1.0

- Initial upgrade system, Basic and Advanced Time Accelerators, eight extended JDT T2 machines, and initial GuideME pages.

## Configuration

JDTE uses NeoForge `ModConfigSpec` with COMMON settings. The config is available through Mods > Config and takes effect after restart where required.

Config class: `src/main/java/com/jdte/setup/JDTEConfig.java`

| Category | Path | Purpose |
|----------|------|---------|
| Upgrade system | `jdte.upgrades` | Filter slots, energy multipliers, tick speed, and area limits |
| Time Accelerator | `jdte.timeAccelerator` | Fluid capacity, tier multipliers, energy, and fluid-cost multiplier |
| Bio Crusher | `jdte.bioCrusher` | Fluid, energy, damage, range, output scaling, and integrations |
| Life Extractor | `jdte.lifeExtractor` | Life Fluid capacity, health conversion, and batch size |
| Loot Fabricator | `jdte.lootFabricator` | Processing costs, Boss multipliers, Looting copies, and compatibility loot |
| Sender/Receiver | `jdte.senderReceiver` | Storage, transfer rates, delays, and energy |
| Gel Generator | `jdte.gelGenerator` | Slots, capacity, conversion, and fuel use |
| Generator upgrade | `jdte.generatorUpgrade` | Energy multiplier and fluid consumption |
| Upgrade items | `jdte.upgradeItems` | Limits and damage values |

Config translations use `config.jdte.<category>.<key>` in `en_us.json` and `zh_cn.json`.
