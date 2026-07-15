# JDT Extras Developer Guide

## Project Overview

JDT Extras (`jdte`) is a NeoForge extension for Just Dire Things (JDT). It adds upgrade cards, time accelerators, extended machines, automation devices, and optional cross-mod integrations.

| Property | Value |
|----------|-------|
| Mod ID | `jdte` |
| Mod name | `JDT Extras` |
| Current version | `0.5.5` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.233+` |
| Just Dire Things | `1.5.7+` |
| Java | `21` |

Major features:

- 13 upgrade items: Capacity, Overclock, Underclock, Fluid, Fluid Storage, Generator, Range, Filter, Creative, Fortune, Precision, Looting, and Sharpness.
- Basic, Advanced, and Extended Advanced Time Accelerators.
- Eight extended variants of JDT T2 machines, each with eight standard upgrade slots.
- Glue Activators, Gel Generators, Fluid Stabilizers, and item/fluid sender and receiver families.
- Advanced Item Collector with eight upgrade slots and event-driven pre-spawn collection without item-flow particles.
- Entity Suppressor with entity-tick suppression, entity spawn/join blocking, entity and block entity rendering suppression, client particle suppression, and six entity target modes.
- Range Blocker with event-driven living-entity containment and player-magnet suppression.
- Crystal Incubator with generic budding-block discovery, Time Fluid growth acceleration, Fortune harvesting, and batched area caching.
- Glass-framed Greenhouse with four client-rendered plants, four reusable stackable plant templates, paged output, bounded batch production, generic plant support, and public-API Mystical Agriculture/Agradditions support.
- Advanced and Extended Bio Crushers, Life Extractors, and Infusion Machines.
- Advanced Potion Brewer with ordered six-step brewing, recipe locking, auto I/O, and JEI brewing chains.
- Loot Fabricator using spawn egg templates, Life Fluid, Time Fluid, and FE to produce mob loot.
- Eclipse Alloy Wrench for rotation and NBT-preserving machine pickup, with optional FTB Ultimine bulk operations.
- Permanent Life Apple progression and JEI categories for machine recipes.
- Absolute-direction auto I/O configuration for machines with real item or fluid interfaces.
- Wither, Ender Dragon, and Elder Guardian essences.

## Build and Run

The project uses Gradle with NeoForge ModDev. JDT is resolved through CurseMaven file `7463040`; no machine-specific local jar path is required.

Common commands:

```bash
./gradlew compileJava
./gradlew jar
./gradlew runClient
./gradlew runServer
```

If dependency resolution fails, verify CurseMaven access and the coordinates in `dependencies.gradle` before changing source code.

## Core Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| NeoForge | `21.1.233` | Mod loader and API |
| Just Dire Things | `1.5.7` | Base machines, interfaces, config, and Time Fluid |
| GuideME | `21.1.16` | In-game documentation |
| JEI | `19.27.0.340` | Recipe categories, catalysts, information pages, and GUI click areas |
| Apothic Spawners | CurseForge `7492121` | Optional spawner cycle and XP compatibility |
| Draconic Evolution | CurseForge `7584459` | Optional Chaos Guardian and dragon loot compatibility |
| FTB Ultimine | CurseForge `8231400` | Optional bulk wrench and Upgrade Card operations |
| Mystical Agriculture | CurseForge `8344249` | Optional Greenhouse crop registry integration |
| Mystical Agradditions | CurseForge `7802027` | Optional high-tier Greenhouse crop integration through the shared registry |
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
8. `JDTEFluids` fluid types, fluids, blocks, and buckets
9. `JDTERecipes` recipe types and serializers
10. `JDTE.registerCapabilities()`
11. `JDTEPacketHandler.registerNetworking()`

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
| `FORTUNE` | `fortune` | 8 | Gel Generator, Crystal Incubator, and Greenhouse; Greenhouse uses a machine-specific limit of 3 |
| `PRECISION` | `precision` | 1 | Crystal Incubator only; applies vanilla Silk Touch loot behavior and conflicts with Fortune |

Fortune and Precision are standard `UpgradeType` values restricted to supported production machines; they conflict on the Crystal Incubator like vanilla Fortune and Silk Touch. Looting and Sharpness are dedicated upgrade items and are not members of `UpgradeType`. Bio Crushers accept up to six of each in dedicated slots. The Loot Fabricator uses `LootFabricatorUpgradeItemStackHandler` to allow up to three Looting Upgrades alongside eight standard slots.

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
- Each server tick checks redstone state and machine state before submitting work to the shared scheduler.
- Other `TimeAcceleratorMachine` instances are skipped to prevent recursive acceleration.
- Only blocks accepted by `MiscTools.isValidTickAccelBlock()` are accelerated.
- Block entity tickers and random block ticks are invoked repeatedly according to the effective multiplier.
- FE and Time Fluid are consumed only when acceleration succeeds.
- Filter pages restrict eligible targets.

| Block entity | Inheritance | Default behavior |
|--------------|-------------|------------------|
| `BasicTimeAcceleratorBE` | `TimeAcceleratorBE` | 16x by default, 32x with Overclock/Creative, base Time Fluid cost |
| `AdvancedTimeAcceleratorBE` | `TimeAcceleratorBE`, `PoweredMachineBE` | Adjustable 1-64x, 128x with Overclock/Creative, Time Fluid at 2x the Basic rate plus FE |
| `ExtendedTimeAcceleratorBE` | `AdvancedTimeAcceleratorBE`, `ExtendedUpgradeMachine` | Managed eight-slot tier, adjustable 1-512x and 1024x with Overclock/Creative, Time Fluid at 5x the Basic rate |

Advanced defaults include `BASE_ENERGY_CAPACITY = 200000`, `MAX_MULTIPLIER = 64`, and `OVERCLOCK_MULTIPLIER = 128`. FE cost derives from `Config.TIMEWAND_RF_COST`; fluid cost derives from `Config.TIMEWAND_FLUID_COST` and `JDTEConfig.COMMON.timeAcceleratorFluidCostMultiplier`, then applies fixed Basic/Advanced/Extended tier factors of 1x/2x/5x. The base fluid multiplier can be changed with `/jdte timeaccelerator fluidCostMultiplier <value>`.

All three tiers use the shared `ExtendedTimeAccelerationManager`. Active accelerators submit work to a server-post-tick scheduler that discovers loaded block entities through chunk maps, sums overlapping multipliers without discarding contributions, rotates bounded target batches inside configurable MSPT headroom, and retains paid virtual ticks while their contributing accelerators remain active. Random-ticking targets use a periodically refreshed cache. Optional AE2 support resolves public in-world grid nodes and invokes their `IGridTickable` services without reflection or mixins. `TimeAcceleratorBE.accelerateArea()` remains as the fallback/reference implementation.

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
| Advanced Item Collector | Single eight-slot tier | `AdvancedItemCollectorBE` | Intercepts item entities before world insertion and sends them to its facing inventory |
| Entity Suppressor | Single eight-slot tier | `EntitySuppressorBE` | Suppresses entity ticks or client rendering, blocks entity creation, or disables particles in a filtered area |
| Range Blocker | Single eight-slot tier | `RangeBlockerBE` | Contains living entities or prevents player magnets from moving item entities in a filtered area |
| Crystal Incubator | Single eight-slot tier | `CrystalIncubatorBE` | Accelerates tagged budding blocks with Time Fluid and FE, then Fortune- or Precision-harvests mature neighboring clusters |
| Greenhouse | Single eight-slot tier | `GreenhouseBE` | Runs four stackable crop/flower/sapling templates at adjustable 1-32x or forced 64x with Overclock/Creative, samples or explicitly defines multi-output harvests, and generates new output directly into adjacent item handlers |
| Bio Crusher | Advanced, Extended | `BioCrusherBE` | Produces mob loot and XP fluid, including spawner integration |
| Life Extractor | Advanced, Extended | `LifeExtractorBE` | Converts target health into Life Fluid without normal drops |
| Infusion Machine | Advanced, Extended | `InfusionMachineBE` | Performs gel/item and dynamic spawn egg infusion |
| Advanced Potion Brewer | Advanced | `AdvancedPotionBrewerBE` | Ordered brewing using water, Time Fluid, fuel, and FE |
| Loot Fabricator | Single eight-slot tier | `LootFabricatorBE` | Produces mob loot from reusable spawn egg templates |

Capability summary:

- Time Accelerators expose fluid; Advanced and Extended variants also expose energy.
- The Crystal Incubator exposes energy, Time Fluid input, and an extraction-only nine-slot item inventory; Auto I/O supports fluid input and item output.
- The Greenhouse exposes energy, Time Fluid input, four plant-template insertion slots, and 16-64 extraction-only paged output slots; its renderer displays four cached plant states without real world crops. Harvest settlement prefers the last successful adjacent item handler and routes newly generated stacks there first. Only remainders use internal output slots, and bounded snapshots preflight both destinations without a per-tick inventory-transfer loop.
- Extended JDT machines expose item and energy capabilities where supported.
- Glue Activators expose items; powered tiers also expose energy.
- Gel Generators, Bio Crushers, Infusion Machines, Potion Brewers, and Loot Fabricators expose energy, fluid, and item capabilities as appropriate.
- Fluid Stabilizers expose catalyst items and powered-tier energy, but no fluid capability.
- Item sender/receiver tiers expose items; powered tiers expose energy.
- Fluid sender/receiver tiers expose fluid; powered tiers expose energy.
- Life Extractors expose energy and fluid.
- JDT Clickers expose fluid when `FLUID_STORAGE` is installed.
- The Advanced Item Collector exposes no internal item capability; it writes directly to the adjacent inventory on its facing side.

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
| `ParticleEngineMixin` | Rejects particles at the final client particle-engine insertion point for Entity Suppressor areas |
| `ItemEntityRendererMixin` | Freezes render interpolation for suppressed item entities |
| `EntityRenderDispatcherMixin` | Skips filtered entity rendering in active Entity Suppressor areas |
| `BlockEntityRenderDispatcherMixin` | Skips dynamic block entity rendering in active Entity Suppressor areas while preserving baked block models and suppressor rendering |
| `MekanismMagneticAttractionMixin` | Optionally excludes demagnetized items from Mekanism's existing attraction candidates |

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

Greenhouse data recipes live under `data/jdte/recipe/greenhouse/`. Each recipe declares a reusable `seed` ingredient, an `outputs` list for JEI and explicit harvest results, `display_block`, `growth_work`, and the unreduced base `time_fluid` cost. Optional `harvest_block` selects the mature state used for loot simulation. Set `use_loot_table` to `true` to harvest that state through its real loot table, or to `false` to use `outputs` directly. Runtime applies the configured 100x fluid divisor and the input stack-density multiplier.

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

### v0.5.5 (Current)

- Reworked all Time Accelerators to use a shared managed stacking scheduler with retained virtual ticks, chunk-based target discovery, dynamic MSPT headroom, and AE2 `IGridTickable` support.
- Balanced tier limits and Time Fluid costs: Basic runs at 16x or 32x with Overclock/Creative, Advanced is adjustable to 64x or runs at 128x with Overclock/Creative, Extended remains adjustable to 512x or runs at 1024x, and Basic/Advanced/Extended use 1x/2x/5x Time Fluid cost rates.
- Added the Crystal Incubator with adjustable 1-512x or overclocked 1024x Time Fluid growth acceleration, nine-slot automatic mature-cluster harvesting, Fortune VIII, common/extension tags, batched caching, and public-API Just Dyna Things support.
- Added Crystal Incubator FE usage, automatic item output, AE2 Growth Accelerator-style ordinary budding ticks with six equivalent accelerators at 8x, exact Dyna target FE/Time Fluid provisioning and target-side charging, and the mutually exclusive Precision Upgrade backed by vanilla Silk Touch loot behavior.
- Added the Greenhouse with a transparent Eclipse Alloy frame and Shadowpulse Soil base, four client-rendered plants, four reusable stackable plant templates, 16-64 paged output slots, a former-512-work 1x baseline adjustable to 32x or overclocked 64x, fair bounded settlements, mature crop loot sampling, generic crops/flowers/saplings, Fortune III, JEI recipes, and public Crop Registry support for Mystical Agriculture and Mystical Agradditions.
- Replaced the Advanced Potion Brewer's directional fuel setting with a binary external Blaze Powder input toggle while preserving the configurable AE2 pattern-provider guard.
- Added AE2 Crystal Science file `8112039`, AE2 Lightning Tech, and Data Energistics to the local runtime test environment.

### v0.5.4

- Added the Eclipse Alloy Wrench crafting recipe.
- Added the Gel Generator Fortune Upgrade.
- Added the Entity Suppressor with entity update, spawn, entity-render, block-entity-render, and particle suppression modes plus six entity target modes.
- Added the Range Blocker with Containment and Demagnetization modes.
- Added the Advanced Item Collector with eight standard slots, pre-spawn collection, oversized Sophisticated Storage handling, and direct AE2/ExtendedAE interface insertion.
- Added direct and FTB Ultimine bulk Upgrade Card insertion.
- Added reusable two-corner Eclipse Alloy Wrench area selection with JDT-style preview and live dimensions.
- Added Jade display for installed standard and dedicated upgrades.
- Registered the Eclipse Alloy Wrench through conventional wrench tags for native rotation and dismantling support.
- Added sided Mekanism capability interaction for Item Senders and Receivers.
- Expanded absolute-direction Auto I/O to Disabled, Input/Output, Input, and Output states while skipping unsupported routes.
- Increased Auto I/O and Sender/Receiver transfer batches, including Overclock and Creative behavior.
- Renamed the Fluid Placer automation family to Fluid Sender.
- Restricted Advanced Item Collector upgrade slots to Range and Filter Upgrades.
- Batched machine area previews to reduce client rendering cost.
- Updated FTB Ultimine compatibility for Eclipse Alloy Wrench range adjustment.

### v0.5.3

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
| Time Accelerator | `jdte.timeAccelerator` | Fluid capacity, tier multipliers, energy/fluid costs, and shared scheduler MSPT, queue, batch, refresh, and AE2 controls |
| Bio Crusher | `jdte.bioCrusher` | Fluid, energy, damage, range, output scaling, and integrations |
| Life Extractor | `jdte.lifeExtractor` | Life Fluid capacity, health conversion, and batch size |
| Loot Fabricator | `jdte.lootFabricator` | Processing costs, Boss multipliers, Looting copies, and compatibility loot |
| Sender/Receiver | `jdte.senderReceiver` | Storage, transfer rates, delays, and energy |
| Advanced Item Collector | `jdte.advancedItemCollector` | Pre-break oversized-container transfer, per-slot threshold, and direct AE2 ME transfer toggle |
| Entity Suppressor | `jdte.entitySuppressor` | Energy use, named/tamed/Boss protection, and optional removal of existing blocked entities |
| Range Blocker | `jdte.rangeBlocker` | Separate mode energy costs, entity safety, projectile/ownerless projectile containment, explosion clipping, and optional Mekanism compatibility |
| Crystal Incubator | `jdte.crystalIncubator` | FE/Time Fluid capacity and cost, 512x/1024x rates, cache scanning, bounded growth/harvest batches, and Dyna growth attempts |
| Greenhouse | `jdte.greenhouse` | FE/Time Fluid capacity, 1-32x/64x speed, 10 FE harvest cost, 100x fluid divisor, settlement interval, generic/Mystical costs, and batch cap |
| Advanced Potion Brewer | `jdte.advancedPotionBrewer` | Optional rejection of adjacent AE2 crafting providers for Blaze Powder automation |
| Gel Generator | `jdte.gelGenerator` | Slots, capacity, conversion, and fuel use |
| Generator upgrade | `jdte.generatorUpgrade` | Energy multiplier and fluid consumption |
| Upgrade items | `jdte.upgradeItems` | Limits and damage values |

Config translations use `config.jdte.<category>.<key>` in `en_us.json` and `zh_cn.json`.
