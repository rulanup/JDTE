---
navigation:
  title: Factory Packer
  icon: "jdte:factory_packer"
  position: 21
item_ids:
  - jdte:factory_packer
  - jdte:factory_package
---

# Factory Packer

<BlockImage id="jdte:factory_packer" scale="2" />

The Factory Packer stores selected blocks, block entities, non-player entities, and scheduled ticks in a portable package and restores them elsewhere. Inventories, energy, fluids, and most modded state travel with their block entities.

## Pack

1. Select an area with the range controls or Eclipse Alloy Wrench. The packer must remain outside it.
2. Insert an empty Factory Package and press Start.
3. Wait for scanning, persistence, permission checks, and bounded cutting to finish.
4. The filled package shows its dimensions, block count, and root entity count.

## Place

1. Hold the package to preview its real blocks. Use it on a block to anchor it; sneak-use to clear the anchor.
2. Hold the area modifier key (Left Alt by default) and scroll to rotate in 90° steps.
3. Make sure the entire destination is empty, insert the package into the destination packer, and start.
4. The package is consumed only after blocks, entities, and scheduled ticks are restored successfully.

An unanchored package uses the packer's area origin. The package, area, rotation, and machine block are locked during an operation. Failures roll back, and safely persisted jobs resume after a server restart.

## Limits and Safety

- Blacklisted blocks, unloaded chunks, and selections above configured limits are rejected with useful block names and coordinates.
- A Mekanism fission reactor must be selected completely. It is stopped before cutting and remains off after restoration until coolant and waste routes are checked.
- Internal absolute positions, block facing, entity transforms, and JDT area offsets rotate with the structure.
- Public move integrations cover AE2, Mekanism, Logistics Networks, and other known systems. Private state held only by unknown global managers cannot be guaranteed.
- Players and cross-dimension chunk tickets are never packed.

## Range and Upgrades

| Property | Default |
|----------|---------|
| X/Y/Z radius | 10; Range Upgrades raise it to 20/40 |
| Axis/volume limit | 128 blocks / 1,000,000 blocks |
| Work budget | 512 entries per tick; Overclock or Creative uses 4x |

Range, Capacity, Overclock, and Creative Upgrades are supported. Creative also removes FE costs. Compression and preview reads run on the I/O pool to avoid long server-tick stalls.

## Crafting

<RecipeFor id="jdte:factory_packer" />

<RecipeFor id="jdte:factory_package" />
