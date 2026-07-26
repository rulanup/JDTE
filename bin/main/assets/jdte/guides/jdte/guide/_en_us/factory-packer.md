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

The Factory Packer cuts blocks, block entities, non-player entities, and scheduled ticks into a portable package and restores them elsewhere. Inventories, energy, fluids, upgrades, and most modded state use complete block entity serialization.

## Packing

1. Configure the area through the area controls or the Eclipse Alloy Wrench. The packer itself must remain outside the area.
2. Insert an empty Factory Package and press Start.
3. The machine scans the selection. A fully selected Mekanism fission reactor is stopped through public APIs; a partial selection is rejected with the complete reactor bounds.
4. After shutdown, an authoritative snapshot is recaptured and safely stored. Source topology changes receive up to three bounded recapture attempts instead of failing immediately.
5. After saved entities are detached, block permissions are checked in bounded batches before supporting blocks are cut. A denied permission restores machines and entities.
6. The completed package tooltip shows its dimensions, non-air block count, and root entity count.

## Unpacking

Holding a filled package displays its bounds and a capped preview rendered from the actual block models. Before deployment it follows the targeted block face; use the package on a block to anchor it, or use it while sneaking to clear the anchor. Hold the area modifier key (Left Alt by default) and scroll to rotate horizontally in 90-degree steps. A destination packer reads the package position, dimensions, and rotation automatically.

The complete destination volume must be empty. The package file is exclusively claimed before rotated block states, block entities, entity trees, scheduled ticks, lighting, and neighbor updates are restored in bounded batches. A successful operation consumes the package. An unanchored package uses the configured machine area's origin.

The package slot, area, rotation, and machine block are locked while an operation runs. Failed cuts and placements are rolled back in batches. Safely persisted cut or placement stages resume after a server restart.

A fission reactor must be completely inside the selection. The packer stops it and waits for burning to cease before touching coolant pipes, waste routes, or reactor casing, preventing a staged cut from causing a meltdown. Restored reactors remain inactive so their coolant and waste networks can be checked before manual restart.

Internal absolute-coordinate links are remapped only when they point inside the source bounds, including source/target dimensions. Block facing plus entity positions and yaw follow structure rotation. Every JDT `AreaAffectingBE` also rotates its X/Z radius and offset. AE2 block entities use its public move strategies; AE2 Crystal Science broadcasters leave their runtime band and remove the old declaration before cutting, then register the restored position; Logistics Networks nodes rebuild their attachment from the transformed entity position before registry insertion. Other networks rebuild through normal load and neighbor updates after complete NBT restoration. Players, cross-dimension chunk tickets, and private state held only by unknown global managers without a public move API are not guessed or rewritten.

Blacklisted blocks, unloaded chunks, and areas above configured limits are rejected. When blacklist entries are found, chat lists their localized names, registry IDs, and actual coordinates; unpacking a rotated package reports transformed destination coordinates. At most 16 entries are expanded, followed by an omitted count to avoid chat spam. Players with teleport command permission can click a coordinate to teleport above that block. Datapacks can extend the `#jdte:factory_packer_blacklist` block tag.

The machine supports Range, Capacity, Overclock, and Creative Upgrades. Its default X/Y/Z radius is 10, with Range Upgrades extending it to 20/40; new default limits allow 128 blocks per axis and a 1,000,000-block volume. Overclock and Creative use four times the per-tick operation budget, while Creative also removes the FE cost. Defaults process at most 512 entries per tick and retry source changes up to three times; compressed I/O and preview reads run on the I/O worker pool.

## Crafting

<RecipeFor id="jdte:factory_packer" />

<RecipeFor id="jdte:factory_package" />
