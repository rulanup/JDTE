# JDT Extras

A powerful expansion for Just Dire Things that takes your automation to the next level.

## What is JDT Extras?

JDT Extras adds new upgrade cards, machines, time acceleration, and automation options to Just Dire Things. If you love JDT and want more control over your machines, this mod is for you.

## Upgrade Cards

Install upgrade cards into your machines to boost their performance. Standard machines have 4 slots, extended machines have 8. Empty slots show which upgrades the machine accepts, and you can sneak-right-click a machine with a card to insert it directly — or use FTB Ultimine to fill whole selections of machines at once.

**Standard cards:**

- **Capacity Upgrade** - Double your energy and fluid storage per card (up to 3)
- **Overclock Upgrade** - Make machines run faster at triple the energy cost
- **Underclock Upgrade** - Slow machines down to save 80% energy
- **Fluid Upgrade** - Double fluid storage only (up to 3)
- **Fluid Storage Upgrade** - Add a built-in fluid tank to Clicker machines
- **Generator Upgrade** - Triple generator output for double fuel cost
- **Range Upgrade** - Expand the working area of range-based machines (up to 2)
- **Filter Upgrade** - Add 9 extra filter slots per card (up to 2)
- **Creative Upgrade** - Free operation with no FE cost, and includes Overclock behavior
- **Fortune Upgrade** - +1 Fortune level per card in Gel Generators and Crystal Incubators, +10% average output per card in Greenhouses (up to 8, or 3 in Greenhouses)
- **Precision Upgrade** - Crystal Incubator only: harvest crystals with Silk Touch, mutually exclusive with Fortune
- **AE Acceleration Upgrade** - Time Accelerators only: accelerates individual AE2 devices by default, or entire AE2 grids with the JDTE-AE addon
- **AE Crafting Read Upgrade** - Compatible machines run only while their linked AE2 network has an active crafting job
- **AE Output Upgrade** - Returns item and fluid products straight into a bound AE2 network
- **AE Extraction Upgrade** - Bound to a Wireless Access Point: automatically refills the JDT/JDTE energy and fluid items you carry
- **Essence & Seed Conversion Upgrades** - Greenhouse upgrades for Mystical Agriculture-style essence automation

**Dedicated cards:**

- **Looting Upgrade** - Bio Crusher, Loot Fabricator, and Bio Factory
- **Sharpness Upgrade** - Bio Crusher only, +5 damage per card
- **Energy Brewing Upgrade** - Advanced Potion Brewer only: pay brewing fuel with FE instead of Blaze Powder

Overclock and Underclock cannot be installed together.

## Time Accelerators

Speed up any machine, crop, or random-tick process in your base.

- **Basic Time Accelerator** - 16x speed (32x with Overclock), uses Time Fluid only
- **Advanced Time Accelerator** - Adjustable 1-64x speed (128x with Overclock), uses Time Fluid and FE
- **Extended Advanced Time Accelerator** - Same as Advanced but with 8 upgrade slots, adjustable 1-512x (1024x with Overclock)

Accelerator multipliers overlap additively: two 16x accelerators covering the same area produce 31x. With the matching **JDTE-AE** addon installed on both sides, covered AE2 networks get the same treatment — Molecular Assemblers, crafting CPUs, and pattern providers run through complete grid lifecycles at up to 1024x.

## Extended Machines

Upgrade your JDT T2 machines to extended versions with 8 upgrade slots instead of 4. Works with Clicker, Block Breaker, Block Placer, Block Swapper, Dropper, Sensor, Fluid Collector, and Fluid Placer, preserving all machine data. JDTE's own machines add extended tiers too, including the Coal Generator, Fluid Generator, Experience Holder, Energy Transmitter, Time Freezer, and every machine family below.

## Machines

JDT Extras adds a large lineup of machines for advanced automation:

**Mob farming & life resources**

- **Bio Crusher** - A mob farming machine that kills mobs and collects drops and experience fluid automatically
- **Life Extractor** - Converts mob health directly into Life Fluid
- **Loot Fabricator** - Manufactures mob loot from spawn-egg templates, Life Fluid, Time Fluid, and FE
- **Bio Factory** - Turns reusable spawn eggs or bee cages, food, and fluids into animal products
- **Life Breeder** - Automates animal and villager breeding and baby growth in an area
- **Life Synthesis Vat** - A 3×3×2 multiblock that grows tissue and distills it into Life Fluid

**Crops & crystals**

- **Greenhouse & Large Greenhouse** - Stackable plant templates, Fortune support, and large paged outputs
- **Crystal Incubator** - Accelerates budding blocks at 1-512x (1024x overclocked) with Fortune or Silk Touch harvesting
- **Mineral Extractor & Large Mineral Extractor** - Produce weighted ore batches from biome data or Mineral Surveys, up to 64x

**Processing & logistics**

- **Glue Activator, Gel Generator, Fluid Stabilizer** - Automate JDT's glue, goo-spread, and FluidDrop conversions
- **Infusion Machine** - Gel infusion crafting, including automatic spawn-egg production
- **Advanced Potion Brewer** - Ordered six-step brewing with recipe locking and optional FE fuel
- **Item/Fluid Senders & Receivers** - Push and pull items and fluids over an area
- **Advanced Item Collector** - Catches drops before they enter the world
- **Advanced Energy Transmitter** - Fairly distributes FE across a 3D area and wirelessly charges bound players across dimensions

**World control**

- **Entity Suppressor** - Suppress entity updates, spawning, or rendering in an area
- **Range Blocker** - Contain mobs or block player magnets
- **Time Freezer** - Freeze the day/night cycle and weather
- **Factory Packer** - Transactionally pack, move, and unpack entire areas, including entities, block entities, and scheduled ticks

**Tools**

- **Time Multitool** - Pickaxe, shovel, axe, and hoe in one, with up to 1024x mining speed
- **Eclipse Alloy Wrench** - Rotate and pick up machines with their data, and box-select areas to write into range machines
- **Ultimate Portal Gun** - Unlimited paginated teleport slots across all registered dimensions
- **Big Fluid Tank** - A wearable 1000 B fluid tank with its own Curios slot
- **Repair Talisman** - Repairs everything in your inventory for FE

Related addons: **JDTE-AE** for full AE2 grid acceleration, and **JDTE-Matrix** for the Greenhouse Matrix and tiered solar panels.

## Bio Crusher

A mob farming machine that kills mobs and collects drops and experience fluid automatically.

- Supports Looting upgrades for extra drops
- Supports Sharpness upgrades for more damage
- Place it above a mob spawner to stop spawning and farm directly — works with vanilla, Apothic Spawners, and Draconic Evolution stabilized spawners

Crush bosses like the Wither, Ender Dragon, and Elder Guardian to collect rare essences.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.216+
- Just Dire Things 1.5.7+
- Java 21

Optional: Applied Energistics 2 19.2.17+ together with the JDTE-AE addon for full grid acceleration.
