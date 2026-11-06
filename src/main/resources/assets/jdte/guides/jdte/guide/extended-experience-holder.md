---
navigation:
  title: Extended Experience Holder
  icon: "jdte:extended_experience_holder"
  position: 26
item_ids:
  - jdte:extended_experience_holder
---

# Extended Experience Holder

Stores up to **2147483647 XP points**, without FE or a separate fluid tank. Its fluid interface accepts only JDT Experience Fluid at **20 mB per XP**. Fill and drain operations transfer whole XP; sub-20 mB requests do nothing. The Forge fluid view saturates at the integer mB limit, while integer XP storage remains intact. Capacity and Fluid cards do not increase this fixed XP limit.

Use the native store/extract buttons for **one level**, **ten levels with Shift**, or **all XP with Ctrl**. A partial current level counts as the first level. Only XP that actually fits leaves a player. Owner-only restricts player XP actions and the holder-specific settings; pipes still use the normal fluid capability.

Set a target level (0–1000), area and redstone mode to balance players in the area. The source default is **Pulse**: a pulse acquires a player and finishes balancing that player. Invalid, disconnected, removed or out-of-area players are released. Optional orb collection runs at the configured operation interval independently of the redstone player trigger. Merged orb multiplicities count in full; overflow stays in the world. Show Particles controls the XP-flow visuals.

Eight upgrade slots use the shared storage bag and configuration copier system. Range expands the area. Overclock uses the source two executions at the locked delay; Ultimate Overclock uses ten executions with one-tick delay. Creative speeds operation but never creates free XP. AE Crafting Read gates work if installed. Unsupported machine-specific cards remain rejected.

Use an **Extended Upgrade** on the native JDT Experience Holder to preserve XP, owner, area, settings and installed cards while expanding four slots to eight. The shaped recipe also preserves packed machine state. Normal drops keep XP/settings and drop upgrades separately; the Eclipse Alloy Wrench packs the complete machine. The settings copier copies target/owner-only/collection/particles and common controls, never stored XP or ownership identity.

The source declares an adjacent-tank cache but never calls it; this machine does not invent automatic fluid export. Connect pipes or interact with fluid containers instead.

<RecipeFor id="jdte:extended_experience_holder" />
