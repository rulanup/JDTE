---
navigation:
  title: Entity Accelerators
  icon: "jdte:advanced_entity_accelerator"
  position: 24
item_ids:
  - jdte:advanced_entity_accelerator
  - jdte:extended_entity_accelerator
  - jdte:entity_acceleration_upgrade
---

# Entity Accelerators

Accelerate living creatures inside the configured area using FE and Time Fluid. Players, dead and removed creatures are excluded. Extra ticks advance creature timers; AI, travel and entity pushing are suppressed only for the creature currently receiving extra ticks. Ordinary world ticks still run normally.

Advanced has 200,000 FE, 10,000 mB, four upgrade slots, adjustable multiplier 1–64 (default 4), and at most 32 targets per execution. Extended has 500,000 FE, the same fluid capacity, eight slots, multiplier 1–512, and at most 64 targets. Standard Overclock/Creative selects 128x/1024x. Ultimate Overclock also selects that multiplier and executes ten times per real server tick, as in the source. Ordinary Overclock does not add generic extra machine executions because this is already a time accelerator.

Each selected target receives multiplier minus one extra ticks. FE cost is total requested extra ticks times JDT's Time Wand FE cost. Time Fluid cost is extra ticks times JDT's Time Wand fluid cost, divided by 600, times the configured global cost and tier factor (Advanced 2, Extended 5). Fractional mB debt is saved. No targets or multiplier 1 costs nothing. Insufficient FE/fluid does no work and spends neither resource; Creative bypasses payment. A target removed during a paid batch stops immediately, without refunding the reserved source batch cost.

Use spawn eggs in filters for allowlist/denylist selection. Capacity, Ultimate Capacity, Fluid, Range, Filter, Overclock, Ultimate Overclock, Underclock, Creative and AE Crafting Read are supported. Range changes the configurable area limit. Filled buffers prevent removal or configuration replacement of capacity cards that would discard resources. FE and Time Fluid support Forge automation. Save, wrench packing and Extended Upgrade conversion preserve state.

The Entity Acceleration Upgrade is a crafting ingredient for both machines and can also be attached to the [Ultimate Time Wand](ultimate-time-wand.md). Use inventory secondary-click, crafting, or smithing with one card as template, one wand as base and one Time Fluid Catalyst as addition. The wand retains its resources and other NBT. Its timed target effects stack with independently paid machine batches.

<RecipeFor id="jdte:entity_acceleration_upgrade" />
<RecipeFor id="jdte:advanced_entity_accelerator" />
<RecipeFor id="jdte:extended_entity_accelerator" />
