---
navigation:
  title: Time Multitool
  icon: "jdte:time_multitool"
  position: 6.7
item_ids:
  - jdte:time_multitool
---

# Time Multitool

<ItemImage id="jdte:time_multitool" scale="2" />

The Time Multitool is based on the Eclipse Alloy Paxel. Without changing tool forms, it performs pickaxe, shovel, axe, and hoe work and accepts compatible original JDT tool upgrades.

## Power and tool actions

The tool stores **500,000 FE**. Breaking blocks and performing tool actions require enough FE for one break. With insufficient power, mining falls back to hand speed and actions such as tilling or stripping do not run.

Normal right-click chooses the appropriate action for the target, such as tilling dirt or grass and stripping logs. Sneak-right-click a block to open JDT's tool ability settings.

## Clicks and continuous mining

Short attacks stay on the initial block. Hold attack for **250 ms** by default to continue onto additional blocks. Releasing the bound mouse button or keyboard key stops mining and resets the next click.

Adjust `jdte.timeMultitool.continuousMiningHoldDelayMillis` in the client config `jdte/time-accelerator-local.toml`, accessible from the main menu. Use `200` or `300` to tune the delay, or `0` to allow continuous mining immediately. Explicitly enabled JDT area-mining abilities retain their normal behavior.

The tool is eligible for vanilla **Fortune** and **Silk Touch** enchanted books through the mining-loot tag. Their vanilla mutual exclusion still applies.

## Time Fluid speed

The tool also stores **1000 B (1,000,000 mB) of Time Fluid**. Sneak-right-click air to cycle through six speed modes:

| Mode | Time Fluid per successfully broken block |
|---|---:|
| 1x | 0 mB |
| 2x | 2 mB |
| 4x | 4 mB |
| 16x | 16 mB |
| 256x | 256 mB |
| 1024x | 1024 mB |

For multi-block abilities such as Hammer or Tree Feller, the tool checks Time Fluid for the whole target batch before it starts. Insufficient fluid makes the entire batch fall back to 1x instead of accelerating the first block for free. FE is still checked through JDT's normal tool rules, and Time Fluid is settled only for blocks that were actually removed.

## Crafting

<RecipeFor id="jdte:time_multitool" />
