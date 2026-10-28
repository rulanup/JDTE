---
navigation:
  title: AE Network Upgrades
  icon: "jdte:ae_output_upgrade"
  position: 34
item_ids:
  - jdte:ae_acceleration_upgrade
  - jdte:ae_crafting_read_upgrade
  - jdte:ae_extraction_upgrade
  - jdte:ae_output_upgrade
  - jdte:advanced_ae_output_upgrade
---

# AE Network Upgrades

Bind each upgrade in an AE2 Wireless Access Point's item slot. The target must remain loaded and active. Binding does not keep chunks loaded. Items retain their target dimension and block position when saved or moved.

## Crafting Read

Install one Crafting Read Upgrade to run a compatible machine only while the linked grid has a busy crafting CPU with an active job. An unbound, unloaded, inactive or booting target pauses production. Pending work remains available for resumption. The activity cache lasts five ticks and is isolated by server and grid identity. Factory Packer rollback remains available while paused.

The pinned 0.6.5.2 source has no survival crafting recipe for this card. This port retains that source behavior.

## Output

A basic AE Output Upgrade returns actual machine output slots and output fluids to the linked network. Inputs, surveys, fuel and input fluids are excluded. The default successful transfer interval is five ticks, configurable from 1 to 1200 ticks. Its fluid limit is 64,000 mB per transfer.

Advanced AE Output performs up to ten passes per server tick and can transfer up to 2,147,483,647 mB per pass. It also flushes after actual machine ticker executions. Install only one output card of either tier. Batched item output supports oversized mineral and greenhouse stacks and refunds commit shortfalls. Essence Conversion runs before ME insertion and retains incomplete recipe inputs.

## Extraction

Bind an AE Extraction Upgrade, then use it as the smithing template with a JDT/JDTE item exposing energy or fluid storage. Leave the addition slot empty. The resulting equipment retains its existing NBT, capabilities and wireless binding; the template is consumed by normal smithing rules.

Enabled carried equipment and optional Curios equipment refill from that grid. Existing fluid types are refilled; an empty tank is filled only when it accepts one unambiguous fluid type. Energy refill additionally requires Applied Flux and stored FE. Inventory and Curios discoveries are deduplicated. Failed or partial transfers refund their unused resource to ME storage.

AE2, Applied Flux, Curios, JEI and Jade remain optional. The cards' network operations are inactive when AE2 is absent.

<RecipeFor id="jdte:ae_output_upgrade" />
<RecipeFor id="jdte:advanced_ae_output_upgrade" />
<RecipeFor id="jdte:ae_extraction_upgrade" />

## AE Acceleration

Install one AE Acceleration Upgrade in any of the three Time Accelerator tiers. This card needs no wireless binding: it discovers filtered targets in the configured loaded area. With AE2 enabled in config, it invokes individual AE2 `IGridTickable` services using the accelerator's existing multiplier, Time Fluid costs and bounded shared execution budget. Overlapping accelerators contribute to the same queued target; removing the card discards its pending AE contribution. Idle sleeping endpoints stop work without counting their sleep-only call as an executed acceleration tick.

The source external-backend registration API is available for JDTE-AE whole-grid scheduling. A registered backend owns active/inactive external targets; the built-in per-device AE fallback is disabled while that backend owns routing, preventing duplicate acceleration. No third-party backend is bundled. Without AE2 the card stays saved but performs no AE work, and its survival recipe is disabled.

<RecipeFor id="jdte:ae_acceleration_upgrade" />
