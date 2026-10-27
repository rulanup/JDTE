---
navigation:
  title: Machine Settings Copier
  icon: jdte:advanced_machine_settings_copier
  position: 32
item_ids:
  - jdte:advanced_machine_settings_copier
  - justdirethings:machine_settings_copier
---

# Machine Settings Copiers

The basic copier is the existing `justdirethings:machine_settings_copier`. Right-click in the air to choose whether area, offsets, filters and redstone controls are copied. Sneak-right-click a machine to record those settings, then right-click another machine to apply them. Basic copying does not exchange upgrade cards.

<ItemImage id="jdte:advanced_machine_settings_copier" scale="2" />

The advanced copier also records a typed machine preset: tick delay, direction, supported machine-specific options, automatic input/output sides and every upgrade handler. It accepts the exact recorded block entity type; normal and large machines are distinct types. Both tiers can address a Large Mineral Extractor through its structure parts.

A paste reuses matching installed upgrades, takes missing cards from your inventory or basic/advanced storage cases (including discovered Curios cases), and returns surplus cards to your inventory. If the inventory is full, returned cards drop at the player. An empty upgrade preset removes and returns the old cards. Missing cards or invalid presets leave inventories and the machine unchanged.

Real machine contents, FE, stored fluids, mineral surveys, output stacks and production progress are never part of the configuration preset. Reducing an installed capacity upgrade still applies the machine's normal capacity rules. Recipe-lock templates on the Advanced Potion Brewer are configuration only; copying them does not create ingredients or reset an active brew.

The Forge registry covers the available JDTE and native JDT 1.1.4 families. Machine families not yet ported are not registered as supported targets.

<RecipeFor id="jdte:advanced_machine_settings_copier" />
