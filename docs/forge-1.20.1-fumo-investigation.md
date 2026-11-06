# Remaining Fumo source investigation

Read-only investigation against JDTE0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. At the investigation checkpoint all three items were **N** in the authoritative [feature matrix](forge-1.20.1-feature-matrix.md). No Fumo registrations or gameplay were added in the Time Multitool task. Cainiaoyo has since been implemented; consult the current matrix for its status.

## Actual shared behavior

`src/main/java/com/jdte/common/blocks/DecorativeFumoBlock.java` is a horizontal decorative block, with wool sounds, strength0.2 and no occlusion. Placement faces opposite the player's horizontal direction. Four explicitly supplied voxel shapes select collision/outline bounds by facing. The source has no block entity, machine tick, container, energy, fluid, XP, item capability, upgrade, interaction menu or external gameplay integration. It does not override vanilla rotate/mirror methods; do not invent extra source behavior from its name.

`JDTEBlocks` registers all three with the same class. `JDTEItems` registers ordinary `BlockItem`s. The NeoForge `decorative_fumo` MapCodec block-type registration has no equivalent requirement in Forge1.20.1; a normal horizontal-block constructor/state definition is the appropriate adaptation.

| Source item/block ID | Block/item model | Local source texture | Shared dependencies |
|---|---|---|---|
| `jdte:cainiaoyo_fumo` | `jdte:block/cainiaoyo_fumo` | `jdte:block/cainiaoyo` (`cainiaoyo.png`, **not** `cainiaoyo_fumo.png`) | Vanilla block/item APIs and existing JDTE content visibility setting |
| `jdte:maiqilin_fumo` | `jdte:block/maiqilin_fumo` | `jdte:block/maiqilin_fumo` | Same |
| `jdte:xingye_fumo` | `jdte:block/xingye_fumo` | `jdte:block/xingye_fumo` | Same |

All models contain12 elements, local JDTE textures and `cutout` render type. No other mod's model or texture namespace is referenced. JEI is an optional visibility integration, not a required gameplay dependency.

## Source registration, visibility and resources

- `setup/config/ContentConfig.java`: `showFumoDolls` defaults **false**. Registrations still exist when hidden.
- `setup/JDTECreativeTabs.java`: conditional inclusion of all three based on that setting.
- `client/jei/JDTEJeiPlugin.java:onRuntimeAvailable`: removes the three ingredients from JEI at runtime when disabled.
- `datagen/JDTEFumoBlockStateProvider.java`: four horizontal facing model variants/rotations; `JDTEFumoItemModelProvider`: items inherit their respective block model.
- `datagen/JDTEFumoLootTableProvider.java`: each drops itself. Generated Neo resources use `data/jdte/loot_table/blocks`; Forge requires **`loot_tables/blocks`**.
- Both source language files name the three blocks. No recipe referencing these IDs exists in the pinned main/generated recipe resources or recipe generators checked by `git grep`; do not fabricate an acquisition recipe.
- Source regression classes `DecorativeFumoBlockTest` and `JDTEFumoResourcesTest` cover placement, all shape bounds, all four blockstate rotations, item parents, self drops, both languages and actual texture existence.

## Suggested next bounded subsystem

Port all three together: shared Forge block, three block/item registrations, source geometry/model/texture/language and self loot, existing content-setting/creative/optional-JEI visibility. Adapt the source shape/resource tests and add actual Forge placement/drop/save tests plus graphical model checks. Preserve hidden-but-registered semantics and absent optional-JEI loading. This document is investigation evidence, **not implementation or runtime acceptance**.
