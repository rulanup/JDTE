---
navigation:
  title: Greenhouse Matrix
  icon: "jdte:greenhouse_matrix_controller"
  position: 19.7
item_ids:
  - jdte:greenhouse_matrix_controller
  - jdte:greenhouse_matrix_quick_install_upgrade
---

# Greenhouse Matrix

<ItemImage id="jdte:greenhouse_matrix_controller" scale="2" />

The Greenhouse Matrix is a variable closed cuboid, **5–18 blocks** along each axis. All six outer faces must consist entirely of Matrix Casings, exactly one controller, and at least one of every port type.

Matrix Casings connect to neighboring casings, controllers, and ports in all six directions. Internal glass seams disappear while metal frames remain around the outer edges of each continuous glass surface.

The interior accepts Greenhouses, Large Greenhouses (including their parts), Matrix Enhancers, and air. The controller validates every five seconds and whenever its management screen opens. The screen reports the structure and managed machines, pauses every internal Greenhouse together, and globally disables internal crop rendering.

## Ports

- Item Input inserts only reusable plant templates.
- Item Output extracts from the controller's central long-count product buffer.
- Time Fluid Input distributes Time Fluid.
- Energy Input distributes FE.

The controller exposes no automation capability; pipes must use the matching port.

Auto I/O is enabled by default in the management screen. Each port actively interacts with inventories or storage directly against its exterior face: Item Input pulls plant templates, Item Output pushes products, and the fluid and energy inputs pull their respective resources. Pipes can still connect directly and work alongside Auto I/O.

## Central Simulation

Once formed, internal Greenhouses no longer run recipe resolution, resource checks, and production independently every tick. The controller rebuilds their template, multiplier, and upgrade profiles in bounded batches, merges identical planting lanes into production groups, and advances those groups from real elapsed game ticks. Thousands of identically configured Greenhouses therefore settle only a small number of groups during stable operation.

FE and Time Fluid remain physically stored in the internal Greenhouses and form one pool across loaded members. New products enter a persistent controller-owned long-count buffer instead of the old internal output slots; items already present in those old slots are not deleted. The Item Output port, active Auto I/O, and a linked AE Output Upgrade all drain the central buffer, with AE using long-count batch uploads.

Real loot tables and dynamic crops use a bounded number of representative samples per group and scale the result to the group size. Seed and Essence conversions run before buffering. Paused matrices, unloaded members, and time while the game is closed do not receive catch-up production. Temporary structure invalidation does not clear buffered products.

## Block Enhancers

- Speed: +25% work per block, capped at +300%.
- Efficiency: -10% FE and Time Fluid cost per block, capped at -80%.
- Seed: grants the Seed-to-Essence effect to every managed Greenhouse.
- Essence: grants Essence Conversion; essences with multiple crafting recipes remain unchanged.

## Global Upgrade Installation

Like a normal machine, the controller has eight upgrade slots and no extra dedicated slot. The controller-only **Greenhouse Matrix Quick Install Upgrade** can occupy any of those eight slots. Once installed, it unlocks eight additional global slots that accept stacked upgrade cards. Cards placed there are transferred one at a time into the real eight-slot upgrade inventories of managed Greenhouses and Large Greenhouses; they are not virtual effects.

The controller distributes cards round-robin and respects each Greenhouse's normal limits and conflicts. Cards that currently cannot be installed remain queued. The Quick Install Upgrade cannot be removed until the queue is empty.

<RecipeFor id="jdte:greenhouse_matrix_quick_install_upgrade" />
<RecipeFor id="jdte:greenhouse_matrix_controller" />
<RecipeFor id="jdte:greenhouse_matrix_casing" />
