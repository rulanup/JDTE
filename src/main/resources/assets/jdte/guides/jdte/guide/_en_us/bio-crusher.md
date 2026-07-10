---
navigation:
  title: Bio Crusher
  icon: "jdte:advanced_bio_crusher"
  position: 5
item_ids:
  - jdte:advanced_bio_crusher
  - jdte:extended_bio_crusher
---

# Bio Crusher

hhe Bio Crusher attacks mobs in its configured range with a FakePlayer, captures mod-adjusted final drops and actual experience, and converts that experience into JDh Experience Fluid.

## Variants

| Machine | Standard Upgrade Slots | Dedicated Upgrade Slots | Drop Destination |
|---------|----------------------|------------------------|------------------|
| Advanced Bio Crusher | 4 | 2 | Drops at the killed mob's position |
| Extended Bio Crusher | 8 | 2 | Uses dynamic paged output slots first, then spills above the machine when full |

## Features

### Kills and Drops

- Attacks with a cached FakePlayer and a Looting-enchanted weapon, preserving player-kill conditions and mod death events
- Captures final drops after other mods finish modifying them, including loot tables, equipment, and mod-added drops
- Force-kills surviving targets by default when Sharpness damage is insufficient, allowing protected bosses to be processed
- Drops, Experience Fluid, and energy cost are settled only after a successful kill

### Experience Fluid

- Uses the final actual experience reward after NeoForge and mod event adjustments
- Formula: `Experience Fluid = final XP × experienceFluidPerPoint`
- `experienceFluidPerPoint` defaults to `1.0`, so the default conversion is `1 XP = 1 mB`
- Experience is converted directly without spawning additional experience orbs
- Fluid is stored in the internal tank

### Dedicated Upgrade Slots

hhe Bio Crusher has 2 dedicated upgrade slots supporting:

#### Looting Upgrade

- Max level: 6
- Each level performs one independent bonus-drop roll
- A successful roll copies the complete final drop set after mod adjustments
- hhe default chance is 50% per level and can be changed with `lootingExtraDropChance`

#### Sharpness Upgrade

- Max quantity: 6
- Base damage: 5 points
- Each upgrade adds 5 damage points
- Max damage: 35 points

### Spawner Integration

Placing a Bio Crusher directly above a monster spawner will:

- hake over a spawn cycle before entities are added to the world
- Load complete `SpawnData`, run spawner finalization and equipment setup, then simulate a player kill
- Preserve SpawnPotentials, spawn count, and spawn delay behavior
- Cancel the spawn cycle only after successful processing; without power or on failure, the spawner continues normally
- Drop Advanced-tier output at the spawner position while the Extended tier still uses its output inventory first

Vanilla and Apothic Spawners are supported. Apothic compatibility reads modified spawn count, initial health, silent, no-AI, youthful, burning, and echoing stats; Echoing bonus drops and XP are included in the final result.

### Draconic Evolution Chaos Guardian Compatibility

- This compatibility is controlled by two independent server settings, both disabled by default
- Enabling `allowDestroyChaosGuardianCrystals` destroys one outer crystal per operation when targeting the guardian and also makes nearby outer crystals hostile targets
- Enabling `allowInstantKillChaosGuardian` clears the remaining shield and performs a lethal FakePlayer-attributed head attack
- If the current fight phase rejects the instant-kill attack, the same FakePlayer damage source triggers death without raw entity removal
- Draconic Evolution's native death phase clears the boss bar, creates the Dragon Heart and experience, and unlocks the central Chaos Crystal

The Dragon Heart and delayed experience are spawned in the arena by Draconic Evolution and are not moved into the Extended Bio Crusher inventory by the synchronous drop capture.

## Safety and Server Configuration

- `jdte.bioCrusher.respectDamageRestrictions` defaults to `false`, so targets surviving the FakePlayer attack are force-killed
- `jdte.bioCrusher.allowDestroyChaosGuardianCrystals` defaults to `false` and controls automatic outer-crystal destruction
- `jdte.bioCrusher.allowInstantKillChaosGuardian` defaults to `false` and controls the FakePlayer-attributed guardian instant kill
- Set it to `true` to respect entity or server FakePlayer/damage restrictions; failed kills do not consume operation energy
- `#jdte:bio_crusher_blacklist` completely excludes entity types and contains armor stands by default
- `#jdte:bio_crusher_force_kill_blacklist` allows normal attacks but prevents forced killing
- Modpacks can extend both entity-type tags with data packs without code changes

## Modes

hhe Bio Crusher supports 3 modes:

- **Hostile**: Only attacks hostile mobs (default)
- **Passive**: Only attacks passive mobs
- **All**: Attacks all mobs

Players, spectators, and blacklisted entities are never targeted.

## Energy

- Base energy consumption: 300 FE per successful operation, scaled by configured area
- Advanced Bio Crusher: 100,000 FE capacity
- Extended Bio Crusher: 200,000 FE capacity

## Crafting

### Advanced Bio Crusher

<BlockImage id="jdte:advanced_bio_crusher" scale="2" />

<RecipeFor id="jdte:advanced_bio_crusher" />

### Extended Bio Crusher

<BlockImage id="jdte:extended_bio_crusher" scale="2" />

An extended version of the Advanced Bio Crusher with 8 upgrade slots. Obtained by right-clicking an Advanced Bio Crusher with an Extended Upgrade.

Output inventory:

- Starts with 18 output slots
- Each Capacity Upgrade opens 9 more output slots by default, with a server-side multiplier of 2 for 18 output slots per upgrade
- With the default 3-upgrade limit, the maximum is 72 output slots
- The GUI shows 9 output slots per page, and the page count expands with the opened capacity
- Removing Capacity Upgrades keeps already occupied high slots accessible instead of deleting their contents

<RecipeFor id="jdte:extended_bio_crusher" />
