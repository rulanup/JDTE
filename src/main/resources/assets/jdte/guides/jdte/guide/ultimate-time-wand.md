---
navigation:
  title: Ultimate Time Wand
  icon: "jdte:ultimate_time_wand"
  position: 5.8
item_ids:
  - jdte:ultimate_time_wand
---

# Ultimate Time Wand

Sneak-right-click air to cycle **NORMAL / X2 / X4 / MAX**. Right-click a valid block to accelerate that position, including while sneaking. NORMAL adds one exponent, X2 adds two, X4 adds four, and MAX selects the maximum. Multipliers are powers of two: the default maximum is 1024x; attaching Ultimate Overclock increases it to 32768x. Invalid targets and already-saturated effects spend nothing and do not refresh the timer. There is no separate item cooldown.

A new effect lasts 600 loaded ticks by default. Reinforcement restores **half of the elapsed duration**, not the whole duration. Each successful activation pays the full new multiplier, not only the increase. FE = new multiplier × floor(JDT Time Wand FE cost × configured FE cost factor). Time Fluid = new multiplier × JDT Time Wand fluid cost × configured fluid factor / 600. Whole mB are drained while fractional debt is saved on the wand; a positive fractional settlement still requires some actual Time Fluid in the tank. Disabling fractional settlement rounds every activation up. Creative players bypass payment. Default capacities: **10,000,000 FE / 800,000 mB**, accepting Time Fluid only.

Block requests share the same rotating, bounded server queue as area Time Accelerators. They support ordinary tickable block entities, valid random-ticking blocks and native AE2 tickables, with AE2 taking priority. Coalesced JDTE machines accumulate work and flush once per frame. Queue pressure can defer work; the server's process-all setting removes the execution budget. Time Accelerator machines/request entities cannot recursively accelerate themselves. Unloaded targets are never loaded by the wand. Moving a block-request marker to another dimension cancels that request; unloading its chunk pauses the saved timer.

## Entity Acceleration Upgrade

Attach the card by inventory secondary-click, crafting the wand with one Entity Acceleration Upgrade, or smithing **one card as template + one wand as base + one Time Fluid Catalyst as addition**. Crafting can attach Entity Acceleration and Ultimate Overclock together. Existing resources, mode, fractional debt, name and other NBT are retained. Already-installed or duplicate cards are rejected. Ultimate Overclock can also be attached by inventory secondary-click or crafting.

With the entity card, right-click a living creature or another valid entity (for example a dropped item or primed TNT). Players and JDTE acceleration request/visual entities are excluded. The entity stores its own timed effect, independent of the player holding the wand. It receives multiplier minus one extra ticks per natural server tick, with AI/travel/pushing suppressed only for the current living target. Extra ticks supplied by a machine do not decrement or recursively run its wand timer. Separate machine batches still stack and pay their own costs. An entity moving dimensions retains its saved effect, with one wand application per real server tick. Unloading pauses the effect; logout does not cancel a loaded world-owned effect. As in the source, the final expiry tick removes the entity effect before running another extra batch.

Base crafting produces a fresh empty wand; resources stored in consumed crafting materials are not transferred. Use empty materials when making a new wand. Upgrade/smithing operations preserve the original wand state.

<RecipeFor id="jdte:ultimate_time_wand" />
