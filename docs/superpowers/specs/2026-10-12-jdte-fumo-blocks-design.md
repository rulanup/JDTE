# JDTE Fumo Decorative Blocks Design

**Date:** 2026-10-12  
**Status:** Draft for user review  
**Target:** Minecraft 1.21.1, NeoForge 21.1.216, Java 21

## Goal

Add three placeable decorative doll blocks to JDTE using the supplied Blockbench models and textures:

- `jdte:xingye_fumo` — 星野玩偶
- `jdte:cainiaoyo_fumo` — 菜鸟哟玩偶
- `jdte:maiqilin_fumo` — 麦麒麟玩偶

Each block is a simple decorative block with a corresponding block item. Players can obtain them from the JDTE creative tab, place them facing toward themselves, and recover the same block item when mined. They have no machine behavior or block entity.

## Scope and decisions

- Use the supplied block models and texture images without changing their geometry or pixels.
- Keep all three assets in the `jdte` namespace. The 麦麒麟 source model currently refers to `thaumcraft:block/maiqilin_fumo`; rewrite that texture reference to `jdte:block/maiqilin_fumo` and store its texture in JDTE so the mod has no Thaumcraft dependency.
- Give each block a horizontal facing state. Placement sets the facing opposite the player's horizontal look direction, and generated blockstate variants rotate the model accordingly.
- Use a compact collision/selection shape derived from the shared model bounds rather than the default full cube. The model extents are approximately x=3.9..12.2, y=-0.2..13.6, z=3.8..14.7, in sixteenths; the runtime shape will clamp the lower y bound to the block floor and rotate with facing.
- Register each block and block item through the existing JDTE DeferredRegister flow, and list all three block items in the JDTE creative tab.
- Each block drops itself. No crafting recipes are added because the supplied packs contain no recipe or ingredient design.
- Add Simplified Chinese and English display names.

## Resource generation

The project currently has no NeoForge data-generation entry point. Add a narrowly scoped datagen entry and providers for these blocks. Generate their blockstates, block-item models, loot tables, and English/Chinese translations into `src/generated/resources`. Keep the supplied custom block model JSON and PNG files as authored source assets in `src/main/resources`; they are user-provided geometry and texture art. Do not generate unrelated JDTE resources.

## Implementation boundaries

Expected changes are limited to:

- JDTE block and block-item registries and the existing creative tab.
- One small shared decorative block class for placement facing and rotated bounds.
- A client-independent datagen entry/provider set limited to the three doll blocks.
- Three supplied block model JSON files and three PNG files under JDTE assets, plus generated blockstates, item models, loot tables, and translations.
- Focused unit/resource checks for facing, drops, and generated references.

No new dependency, GUI, configuration, machine logic, or external-mod integration is required.

## Verification

- Run the configured JDTE data-generation task and inspect generated resources for all three IDs, all four horizontal model rotations, self-drop loot, and both language files.
- Run focused tests for the shared block behavior and resource references.
- Run `./gradlew build` on Linux with Java 21 and the project Wrapper.
- Confirm the built JAR contains all three models/textures and generated resources. A successful build does not replace an in-game placement/render check.

## Open implementation details

The exact NeoForge 1.21.1 datagen event/provider constructors must be confirmed from the project mappings and official 1.21.1 documentation during implementation. The legacy texture-pack exporter added in the preceding task remains unchanged.
