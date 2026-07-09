---
navigation:
  title: Advanced Potion Brewer
  icon: "jdte:advanced_potion_brewer"
  position: 16
item_ids:
  - jdte:advanced_potion_brewer
---

# Advanced Potion Brewer

<BlockImage id="jdte:advanced_potion_brewer" scale="2" />

The Advanced Potion Brewer is an automated version of the vanilla brewing stand. It uses the current `PotionBrewing` registry directly for bottle inputs, brewing ingredients, and potion conversions, so brewing recipes added by other mods can be detected automatically.

## Role

- Designed for batch brewing multi-step potions.
- Can automatically fill glass bottles with water.
- Can consume Time Fluid to brew faster, falling back to vanilla speed when Time Fluid is missing.
- Supports auto input/output, with products extracted only from the three output slots.

## Layout

### Item Slots

- Bottle input slots: 3 slots, each capped to 1 glass bottle or potion.
- Ingredient slots: 6 slots, representing brewing steps 1-6 in order.
- Blaze powder slot: 1 slot, accepts blaze powder only.
- Product output slots: 3 slots, each capped to 1 final potion.

Ingredient order:

1. The vanilla-style ingredient slot is step 1.
2. The 5 top horizontal ingredient slots are steps 2-6 from left to right.

### Fluid Tanks

- Left water tank: accepts water.
- Right Time Fluid tank: accepts JDT Time Fluid.

The water tank can automatically fill glass bottles in the bottle input slots into water bottles, consuming 250 mB water per bottle.

### Controls

- Speed button: sets the tick duration for each ingredient step.
- Redstone button: controls whether the machine obeys redstone.
- Input lock button: locks the current non-empty ingredient slots to their captured items.
- Auto I/O button: configures which world directions the machine uses for automatic input/output.

## Brewing Flow

1. Place glass bottles or potions in the three bottle input slots.
2. Place blaze powder in the blaze powder slot.
3. Place ingredients in order across ingredient steps 1-6.
4. The machine checks ingredients starting from step 1.
5. Empty ingredient slots are skipped; ingredients that do not apply to the current potion are skipped.
6. Valid ingredients continue transforming the intermediate potions in the bottle slots.
7. After all usable ingredients have been checked, final potions move to the three output slots on the right.

## Input Lock

Input lock records the items currently present in non-empty ingredient slots. While locked:

- Captured ingredient slots only accept their captured item.
- Empty locked ingredient slots show a dim ghost item.
- Hovering a locked ingredient slot shows the locked item name.
- Bottle slots, output slots, and the blaze powder slot are not locked.

This is useful for fixed brewing chains, such as "water bottle -> awkward potion -> upgraded potion -> extended potion".

## Automation

### Item Automation

- Auto input recognizes bottle slots, the blaze powder slot, and ingredient slots.
- Blaze powder is inserted into the blaze powder slot before ingredient slots.
- Auto output extracts only from the three product output slots. It will not extract bottles, ingredients, or blaze powder.

### Fluid Automation

- The water tank can receive water automatically.
- The Time Fluid tank can receive Time Fluid automatically.
- Both tanks also support right-click fluid container transfer.

## Speed And Time Fluid

### Vanilla Speed Relation

A vanilla brewing stand takes 400 ticks per ingredient step. The Advanced Potion Brewer speed button is the tick duration for each ingredient step:

- `1`: fastest setting.
- `400`: vanilla speed.
- Above `400`: treated as vanilla speed.

Time Fluid is charged only for ticks saved relative to vanilla speed. Without enough Time Fluid, the machine falls back to vanilla 400-tick speed.

### Cost Derivation

The cost is derived from a JDT Time Wand accelerating a vanilla brewing stand:

- A 256x JDT Time Wand effect lasts 600 ticks.
- 256x adds 256 extra block-entity ticks per tick.
- Total extra ticks: 600 × 256 = 153,600.
- One vanilla brewing step takes 400 ticks.
- 153,600 extra ticks are equivalent to 384 full brewing steps.

Other Time Wand rates have the same efficiency:

| Time Wand rate | Extra ticks in 30 seconds | Theoretical full brewing steps | Time Fluid cost |
|----------------|---------------------------|--------------------------------|-----------------|
| 2x | 1,200 | 3 | 2 × JDT Time Wand fluid cost |
| 4x | 2,400 | 6 | 4 × JDT Time Wand fluid cost |
| 8x | 4,800 | 12 | 8 × JDT Time Wand fluid cost |
| 16x | 9,600 | 24 | 16 × JDT Time Wand fluid cost |
| 32x | 19,200 | 48 | 32 × JDT Time Wand fluid cost |
| 64x | 38,400 | 96 | 64 × JDT Time Wand fluid cost |
| 128x | 76,800 | 192 | 128 × JDT Time Wand fluid cost |
| 256x | 153,600 | 384 | 256 × JDT Time Wand fluid cost |

Therefore each full 400-tick brewing step costs:

`JDT Time Wand fluid cost × 2 ÷ 3`

The machine then charges only for ticks saved by the current speed:

`cost = full 400-tick cost × saved ticks ÷ 400`

At `1 tick`, it charges the full saved-step cost, equivalent to:

`256 × JDT Time Wand fluid cost ÷ 384`

## Upgrade Effects

| Upgrade | Effect |
|---------|--------|
| Creative | Removes FE and Time Fluid cost |
| Capacity | Increases FE and fluid capacity |
| Fluid | Increases fluid capacity only |

## Crafting

<RecipeFor id="jdte:advanced_potion_brewer" />
