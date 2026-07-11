# Changelog

### English

#### v0.5.3 (Current)
- **New**: Added the Loot Fabricator with four reusable spawn-egg inputs, vanilla/mod loot-table output, Life/Time Fluid and FE costs, speed-scaled Time Fluid use, 16-64 paged output slots, 18 dedicated upgrade slots, up to three Looting Upgrades, redstone control, and automatic I/O.
- **New**: Added the Elder Guardian Spawn Egg infusion recipe using one Elder Guardian Essence, 64 B of Life Fluid, and 100,000 FE; corrected the bilingual Boss Essence GuideME pages.
- **Fixed**: Bio Crusher forced kills once again roll the entity's complete loot table with a FakePlayer/player-kill context before removal. Boss Essences remain additional drops instead of replacing normal boss loot such as the Wither's Nether Star; the three Boss Essence JEI information pages now state this explicitly.
- **JEI**: Added information pages for Wither, Ender Dragon, and Elder Guardian Essences, including acquisition chances and current infusion uses.
- **Performance**: Life Extractors now increase throughput through larger batches instead of more range queries. Advanced/Extended process 4/8 targets every 20 ticks normally and 8/16 every 5 ticks with Overclock or Creative, reducing accelerated scans from 20 to 4 per second.
- **Changed**: Advanced/Extended Life Extractors and Bio Crushers now retain normal mining hardness but use Wither-proof explosion resistance. Wither Essence now has a 5% base drop chance; Looting affects quantity only after a successful roll.
- **Changed**: Life Extractors now remove targets directly without invoking Minecraft's death flow, so they produce only Life Fluid and no normal drops or experience. Current-health conversion defaults to 0.1 mB per health point and remains configurable as a decimal.
- **New**: Added a JDTE Ender Dragon Spawn Egg infusion recipe using one Ender Dragon Essence, 64 B of Life Fluid, and 100,000 FE. Ender Dragon Essence now has a 10% base drop chance; Looting affects quantity only after a successful roll.
- **Changed**: Life Apple tooltip text is now red, its JEI information page keeps only the short introductory sentence, and GuideME presents progression in tables.
- **New**: Life Apples now permanently and cumulatively increase maximum health, armor, and armor toughness with no consumption cap. Progress is stored on the player and reapplied after death/login; tooltip, JEI information, and GuideME document the repeating milestone formula.
- **New**: Added a real JDTE Wither Spawn Egg. Infusing one Wither Essence with 64 B of Life Fluid costs 100,000 FE and produces the egg; the generic mob-loot recipe scanner excludes the Wither to avoid a second Nether Star recipe.
- **Changed**: Life Extractor output now uses the target's current health instead of maximum health. The integer server config `jdte.lifeExtractor.fluidPerHealth` defaults to 1 mB per current-health point; fractional results caused by entity health or speed upgrades are accumulated and persisted without loss.
- **New**: Infusion Machines can automatically create mod-compatible spawn eggs from one full stack of a uniquely identifying mob drop, 64 B of Life Fluid, and 100,000 FE. Recipes are discovered from registered spawn eggs and current entity loot-table JSON, skip ambiguous shared drops, and are exposed through JEI.
- **Fixed**: Server-discovered spawn egg infusion recipes are now synchronized after login and data-pack reloads, so they appear in JEI even though JEI initializes before a client world exists. Gel Generator, Infusion Machine, and Potion Brewer recipes now expose stable recipe IDs, enabling JEI's recipe-bookmark button.
- **Fixed**: Spawn egg infusion recipes are now registered through one JEI path instead of both initial and runtime registration, removing duplicate entries. The visible Infusion Machine recipe list is rebuilt with spawn egg recipes first.
- **Fixed**: Extended Dropper now inherits JDT's complete T2 implementation, restoring configured offset targeting, filtered slot selection, correct 25 FE operation cost, and saved drop-count/pickup-delay values when reopening its GUI.
- **Fixed**: Extended Clicker settings now initialize through the JDT Clicker T2 screen that is actually opened by its inherited menu type, so saved non-Block targets no longer appear to reset after reopening the machine.
- **Fixed**: Added opt-in Bio Crusher compatibility for Draconic Evolution Chaos Guardian fights. The new server settings `allowDestroyChaosGuardianCrystals` and `allowInstantKillChaosGuardian` independently enable automatic outer-crystal destruction and a lethal FakePlayer-attributed guardian attack; both default to `false`. When enabled, DE's normal fight manager still settles the boss bar, Dragon Heart, central Chaos Crystal unlock, and delayed experience instead of raw entity removal.
- **Fixed**: Advanced Potion Brewer no longer crashes the integrated server when Placebo/Apotheosis synchronizes brewing mixes during login. A concurrent recipe update now pauses brewing for that tick and retries against the completed modded recipe list on the next tick.
- **Changed**: Extended Bio Crusher output inventory now scales with Capacity Upgrades. It starts with 18 output slots, opens 9 more per Capacity Upgrade by default, and uses a server-side multiplier of 2 for 18 slots per upgrade, reaching 72 slots with the default 3-upgrade limit. The GUI still shows 9 slots per page, auto-output only extracts active slots, and occupied high slots remain accessible if capacity is later reduced.
- **Fixed**: Advanced Bio Crusher drops are now emitted at the killed entity's position through Minecraft's standard item-drop path. The Advanced tier no longer loses captured drops despite having no output inventory; the Extended tier still stores drops in its output slots and only spills items above the machine when full.
- **Changed**: Bio Crusher experience fluid is now calculated from the entity's actual final experience reward after NeoForge and mod adjustments, including Apothic Spawners Echoing, instead of maximum health. The retained global config is now `experienceFluidPerPoint`, defaults to `1.0`, and multiplies the final XP amount in mB. Normal, forced, and virtual spawner kills all use the same conversion path without spawning experience orbs.
- **Fixed**: Bio Crusher spawner interception now honors Apothic Spawners' public dynamic stats, including modified spawn count, initial health, silent, no-AI, youthful, burning, and echoing. Echoing now contributes its additional loot through Apothic Spawners' own drop event, and the Apothic-specific delay method preserves modified min/max delays. Direct Sharpness kills now use `isDeadOrDying()` so they do not run the forced-loot path a second time.
- **Fixed**: Virtual Bio Crusher processing above vanilla and Apothic Spawners now loads the complete `SpawnData` entity NBT, runs the normal spawner finalization and equipment table, then captures the complete virtual death. Spawner-produced weapons, tools, armor, custom death drops, and player-kill loot conditions are now preserved without adding the entity to the world.
- **Fixed**: Sharpness upgrades no longer cause normally killed Bio Crusher targets to drop items into the world. Final drops are now captured at `LivingDropsEvent` after other mods finish modifying them, then routed into the Extended Bio Crusher output inventory. Looting upgrades now work for both live targets and virtual spawner processing with a real Looting weapon, a complete player-kill loot context, and the documented configurable 50% extra drop roll per level.
- **Changed**: Bio Crusher now performs a real cached-FakePlayer attack with an actual Looting weapon and captures the resulting mod-adjusted drops. If the target survives, force-kill remains enabled by default and can be disabled globally in `config/jdte/jdte.toml` with `jdte.bioCrusher.respectDamageRestrictions`; entity-type tags can fully blacklist targets or only blacklist forced killing. XP fluid and energy are now awarded only after a successful kill, and unknown modded entities no longer receive fabricated fallback drops.
- **Build system overhaul**: Refactored `build.gradle` — extracted `dependencies.gradle`, added `localRuntime`/`clientLocalRuntime` configurations, `clientRuntime` source set, `ExpandPropertiesAction`, sources jar, and jar manifest. All runtime dev dependencies from WCWT mod added.
- **JDT dependency**: Changed from local jar file to CurseMaven coordinate `curse.maven:just-dire-things-1002348:7463040`.
- **GUI Redesign Plan**: Created `GUI_REDESIGN.md` documenting all 37 machines with full component/upgrade analysis, overlap detection, and proposed embedded (popup-free) GUI layouts with calculated dimensions.
- **Upgrade panel rework**: All machines now use fixed embedded upgrade panels (no popups). 4-slot machines have a single panel right of the inventory (1×4 vertical). 8-slot machines have two panels — one right (slots 0–3) and one left of the inventory (slots 4–7). All slot positions are soft-coded in `assets/jdte/gui_layout.json`. Created `GuiUpgradeLayoutConfig` for runtime config loading.
- **Upgrade slot tooltip**: Empty upgrade slots now show available upgrade types for the machine with current count/max limits. Machine-incompatible upgrades and speed-upgrade conflicts (overclock/underclock) are dimmed.
- **Glue Activator GUI**: Advanced and Extended Glue Activator screens now use `extraWidth=60` (matching ClickerT2 screen size).
- **Gel Generator GUI final alignment**: GEL/FOOD slot X aligned with the first filter slot (x=8), input/output item columns at x=44/x=116, and separate input/output fluid bars at x=62/x=134. Progress arrow and speed button are centered between the item columns; Blacklist/MatchNBT/Redstone/Auto-balance buttons are moved to x=170. All coordinates are soft-coded through `assets/jdte/gui_layout.json` and `GuiUpgradeLayoutConfig`.
- **Gel Generator auto-balance tooltip**: Dynamic tooltip showing "Auto-balance: On"/"Auto-balance: Off" depending on state. Updated via `setTooltip()` in `toggleAutoBalance()`.
- **Fluid Sender GUI**: All three tiers (Basic/Advanced/Extended) now use `extraWidth=60` and `getFluidBarOffset()=204`, matching the Extended Time Accelerator layout for fluid tank and energy bar positioning.
- **Advanced/Extended Item Sender GUI**: Screen size matches Extended Time Accelerator (`extraWidth=60`). Blacklist/MatchNBT/Redstone/RenderArea buttons soft-coded via `item_sender_buttons` section in `gui_layout.json`, moved up 40px to y=42. Speed button X aligned 2px left of 5th filter slot (x=78), soft-coded, moved down 4px to y=44. 9 item storage slots soft-coded via `item_sender_slots`, moved up 18px to y=36, first slot X aligned with first filter slot (x=8). Area range (X/Y/Z radius) and offset adjustment buttons fully restored — `valueButtonsDoubleList`/`valueButtonsList` widgets properly registered via `addRenderableWidget()`. `addAreaButtons()` uses `UpgradeHelper.getMaxAreaRadius()` and `getMaxAreaOffset()` for dynamic max values.
- **Basic Item Sender GUI**: All buttons (Blacklist/MatchNBT/Redstone/RenderArea/Speed) and 9 item storage slots now use the same positions as Advanced/Extended, soft-coded via `basic_item_sender_buttons`/`basic_item_sender_slots` in `gui_layout.json`. Added `addMachineSlots()` override in container. Overrode `addFilterButtons()`, `addRedstoneButtons()`, `addAreaButtons()`, `addTickSpeedButton()` in screen.
- **Item slot tooltip**: All three tiers (Basic/Advanced/Extended Item Sender) now show "Item Slot" tooltip when hovering over empty machine storage slots, via `renderTooltip()` override.
- **Fluid Receiver GUI**: All three tiers (Basic/Advanced/Extended) now use `extraWidth=60` and `getFluidBarOffset()=204`, matching the Extended Time Accelerator layout for fluid tank and energy bar positioning.
- **Advanced/Extended Fluid Stabilizer GUI**: Screen size changed to `extraWidth=60`, energy bar positioned correctly.
- **Item Receiver GUI**: All three tiers (Basic/Advanced/Extended) now match their Item Sender counterparts. Buttons and slots soft-coded via `item_receiver_buttons`/`item_receiver_slots`/`basic_item_receiver_buttons`/`basic_item_receiver_slots` in `gui_layout.json`. Added "Item Slot" tooltip.
- **Life Extractor mode button**: Replaced sprite-sheet with JDT mob scanner/glowing/nightvision icons, added mode tooltips (hostile/friendly/all), soft-coded via `life_extractor_buttons` section in `gui_layout.json`. Fixed coordinate reference from `topSectionLeft` to `leftPos` for correct positioning.
- **Bio Crusher mode button**: Same redesign as Life Extractor — JDT icons, tooltips, soft-coded via `bio_crusher_buttons` section in `gui_layout.json`.
- **Fixed**: Extended Sensor GUI crash — MenuType passed JDT's `SensorT2_Container`; `instanceof SensorT2BE` check in `SensorT2Screen` caused NPE; API version mismatch corrected (JDT jar 1.5.7 uses old API).
- **Changed**: Extended Sensor filter slots increased to 9 (matching JDT Advanced Sensor).
- **New**: Gel Generator (Advanced/Extended) machine slot tooltips — shows slot type (Gel/Food/Input/Output) on hover.
- **Fixed**: Auto-balance button tooltip text changed from "Do not auto-balance" to "Auto-balance: Off".
- **Changed**: Filter pagination buttons scaled to match range button size (`PoseStack.scale 0.75`), added tooltips.
- **Fixed**: Filter page content not updating on page switch — `DynamicFilterSlot` overrides all `SlotItemHandler` methods using `this.index` to use `getSlotIndex()` instead.
- **Tweak**: Filter next button moved 4px left, both pagination buttons moved 2px down.
- **Tweak**: Filter page number text color changed to white. Later changed back to `0xFF404040` to match area label style.
- **Tweak**: Filter page number text moved from 14px to 2px right of the next button.
- **Fixed**: Removing FILTER upgrade while on a non-default filter page no longer causes filter slots to disappear — page auto-resets to 0 when current page exceeds max page.
- **New**: JEI recipe category for Advanced/Extended Gel Generator — dynamically reads JDT `GooSpreadRecipe`/`GooSpreadRecipeTag` entries and current goo-revive food tags. Recipes are split by gel tier while valid foods rotate in the food slot. The gel slot is marked as a non-consumed catalyst and now shows a "Not Consumed" tooltip. Item recipes draw the original four input/output slots with ingredients only in the first slot, move the progress arrow/output column left by 18px, add an animated energy-cost bar next to the output column, and use a compact page size. Fluid recipes use the same input/output/progress/energy column positions and also show the animated energy-cost bar.
- **Fixed**: Added JDT Fluid Placer-style right-click fluid container transfer for JDTE fluid machines. Buckets and compatible single-item fluid containers can now fill/drain Time Accelerators, Gel Generators, Fluid Senders/Receivers, Bio Crushers, Life Extractors, Infusion Machines, and Extended Fluid Collector/Placer blocks. Extended Fluid Collector/Placer also now expose `Capabilities.FluidHandler.BLOCK`.
- **New**: Added the first auto input/output side configuration panel for machines with item or fluid I/O. The toggleable panel uses JDT direction/phase/decoy/hammer icons, 12px buttons with dimmed inactive states, and the standard machine panel background. Side toggles now default to all disabled and are saved on the server via a serializable attachment.
- **Tweak**: Auto input/output side configuration now uses a fixed panel with its bottom-right corner docked to the upper machine configuration panel's bottom-left corner. Direction button spacing is tightened to 12px, the center button is used by the absolute Up side, disabled side buttons use a dark grayscale tint matching JDT grayscale buttons, JEI gets the panel/button extra areas to avoid bookmark overlap, and the button only appears for machines with actual item or fluid I/O.
- **Fixed**: Auto input/output config networking no longer registers the same `jdte:auto_io_config` payload for both directions. Server sync now uses `jdte:auto_io_config_sync`, fixing the startup crash during `RegisterPayloadHandlersEvent`.
- **New**: Auto input/output side toggles now perform server-side adjacent item/fluid transfers. JDTE machines use explicit input/output slot and tank semantics; supported JDT extended machines use conservative known input routes plus fluid collector/placer tank directions. Successful transfers can run every tick with up to 256 items and 16000 mB per tick; failed transfers back off up to 40 ticks to avoid repeated empty-neighbor scans.
- **Changed**: Auto input/output side buttons now map to absolute world directions (North/South/West/East/Up/Down) instead of machine-relative front/back/left/right directions.
- **Development runtime**: Added Logistics Networks (`curse.maven:logistics-network-1448257:8381956`) to the local runtime dependency set for performance comparison and future auto I/O design reference.
- **New**: Added the Eclipse Alloy Wrench for JDTE and JDT machines. It extends JDT's Ferricore Wrench interaction path so machine GUIs do not intercept the click, and right-clicking a supported machine picks it up as a block item with saved machine data, preserving internal materials, energy, fluids, upgrades, and configuration for transport. The temporary item model reuses JDT's Ferricore Wrench model.
- **Balance**: Time Accelerator Time Fluid consumption now matches JDT Time Wand cost by default, spread over the same 30-second duration instead of charging the full multiplier cost every tick. Added `timeAcceleratorFluidCostMultiplier` server config and `/jdte timeaccelerator fluidCostMultiplier <value>` command to tune the cost multiplier live.
- **Changed**: Eclipse Alloy Wrench now matches JDT Ferricore Wrench behavior for normal right-click machine rotation. Sneak right-click keeps the JDTE machine pickup behavior. Added optional FTB Ultimine right-click integration so the wrench can rotate or pick up multiple supported machines in one operation.
- **New**: Eclipse Alloy Wrench can now adjust area-machine X/Y/Z radius together with the mouse wheel while targeting a machine. When FTB Ultimine has an active block selection, the scroll action applies to all matching selected machines.
- **Changed**: Wrench scroll area adjustment now requires holding the **Wrench Area Modifier** key (default Left Alt, configurable in key bindings). Removed item tooltip — full documentation is in the GuideME guide (press G to open).
- **New**: Wrench scroll area adjustment now shows a green overlay message with the current/max range and plays a rotation sound when scrolling on a machine.
- **Fixed**: Reworked the Eclipse Alloy Wrench GuideME page to use plain Markdown only, avoiding unsupported keybind/image/recipe components. FTB Ultimine range scrolling now only uses the Ultimine selection while the Ultimine key is actively held; cached selections no longer affect normal range scrolling.
- **Fixed**: Wrench scroll area adjustment once again requires holding the Eclipse Alloy Wrench on both client and server; holding the area modifier key alone is no longer enough.
- **Development runtime**: Added FTB Ultimine (`dev.ftb.mods:ftb-ultimine-neoforge:${ftb_ultimine_version}`) and AppleSkin (`curse.maven:appleskin-248787:7854442`) to the local runtime dependency set.
- **New**: Infusion Machines can now automatically fill fluid containers using the standard NeoForge item fluid capability. Empty buckets and compatible modded containers can be infused with matching tank fluids without dedicated recipe JSON, and vanilla glass bottles can be infused with water to produce water bottles.
- **New**: Added a JEI recipe category for Advanced/Extended Infusion Machines. It shows `jdte:infusion` data recipes plus automatically discovered fluid-container filling recipes from current item/fluid capabilities, including vanilla water bottles.
- **New**: Infusion Machines can now infuse glass bottles with honey-like fluids to make honey bottles. Honey fluids are detected through common tags and honey-related fluid IDs/translation keys for broader mod compatibility.
- **New**: Clicking the progress arrow in Gel Generator and Infusion Machine GUIs now opens the matching JEI recipe category.
- **Performance**: Fixed severe machine GUI frame drops from embedded upgrade panels by replacing per-frame nine-sliced background sprite rendering with a fixed-size `upgrade_slot_panel.png` texture. Upgrade slot icons are still rendered normally.
- **Development runtime**: Added Industrial Foregoing (`curse.maven:industrial-foregoing-266515:8370717`) to local runtime dependencies.
- **Fixed**: Advanced Potion Brewer GUI now crops the vanilla brewing stand texture to the machine slot area with 2px padding, aligns bottle slots to the background slot art, and exposes the background crop, machine slots, and animated widget positions through `assets/jdte/gui_layout.json`.
- **Changed**: Advanced Potion Brewer layout now shifts the brewing background and original slots up by 10px, adds five soft-coded horizontal ingredient slots above the original ingredient slot, and adds three soft-coded vertical output slots on the right. Brewing now consumes bottle inputs and writes finished potions to the output slots; auto input/output routes recognize the new input/output slots.
- **Tweak**: Advanced Potion Brewer extra ingredient row now aligns to the top of the energy bar. The speed button is soft-coded to the right of the bottom output slot, the redstone button to the right of the middle output slot, output slots show a dim glass-bottle ghost, and slot validation now follows vanilla brewing-stand rules through the current `PotionBrewing` registry.
- **Fixed**: Advanced Potion Brewer brewing progress now advances every server tick like the vanilla brewing stand instead of being gated by JDT machine tick speed, so water bottle + nether wart recipes start visibly animating and complete in the expected 400 ticks.
- **Fixed**: Advanced Potion Brewer now renders the vanilla 1.21 brewing stand fuel, brew-progress, and bubble sprites instead of sampling obsolete texture-sheet coordinates, restoring the blaze-powder fuel bar and brewing animation.
- **New**: Advanced Potion Brewer now has a slot-lock button above redstone control using JDT's Water Breathing upgrade icon. Slot locking snapshots the current non-empty ingredient slots, restricts those slots to their captured items, and shows dim ghost items in empty locked ingredient slots. Bottle slots, output slots, empty ingredient slots, and blaze-powder fuel are not locked.
- **Fixed**: Advanced Potion Brewer slot-lock button no longer flashes back after clicking. The client only changes the button state after server confirmation, and the tooltip now says "Input Lock: On"/"Input Lock: Off".
- **Changed**: Advanced Potion Brewer ingredient slots now work as an ordered brewing chain: the original ingredient slot is step 1, and the five horizontal slots are steps 2-6 from left to right. Matching non-empty slots are brewed in order, empty or non-matching slots are skipped, intermediate potions stay in the bottle slots, and final results are moved to output slots only after the chain is exhausted.
- **New**: Advanced Potion Brewer now has two soft-coded fluid bars. The left water tank sits next to the energy bar and automatically fills glass bottles in bottle slots into water bottles; the right time-fluid tank uses the same position as the Infusion Machine fluid bar. Both tanks save, sync, expose fluid capability, work with auto input/output, and support right-click fluid container transfer.
- **Fixed**: Advanced Potion Brewer bottle/output slots now explicitly cap GUI stack size at 1, and Shift-click routing sends bottles, blaze powder, and brewing ingredients only to their matching slot groups.
- **Balance**: Advanced Potion Brewer speed is now the target tick duration per ingredient step. Time Fluid cost is based on JDT Time Wand efficiency across all powers-of-two rates: each full 400-tick brewing step costs `JDT Time Wand fluid cost * 2 / 3`, and the machine charges that amount proportionally to ticks saved by the current speed. Speed `1` charges the full saved-step cost; speed `400+` uses vanilla speed with no Time Fluid cost.
- **Fixed**: Advanced Potion Brewer Shift-click and Mouse Tweaks insertion no longer crash by calling JDT's private stack-transfer helper; the brewer now uses its own slot-limit-aware transfer path.
- **Fixed**: Advanced Potion Brewer automation now exposes a restricted item capability: external logistics can insert only into bottle/ingredient/fuel inputs and can extract only from the three product output slots. Auto input also prioritizes blaze powder into the fuel slot before ingredient slots.
- **Tweak**: Advanced Potion Brewer empty machine slots now show slot tooltips for bottle inputs, ordered ingredient steps, blaze powder, and product outputs. Locked empty ingredient slots also show the locked item name.
- **Docs**: Reworked the Advanced Potion Brewer GuideME page with a clearer hierarchy, a model preview at the top, and separate sections for layout, brewing flow, automation, input locking, Time Fluid cost, upgrades, and crafting.
- **New**: Added an Advanced Potion Brewer JEI recipe category. It discovers vanilla and NeoForge brewing recipes through the current `PotionBrewing` API, includes the machine's glass bottle + water filling recipe, renders the full machine slot layout with three bottle inputs, three outputs, water bar, energy bar, vanilla brewing background, fuel bar, bubbles, and progress arrow, registers the brewer as a catalyst, and makes the brewer progress arrow open the category.
- **Changed**: Advanced Potion Brewer JEI recipes now prefer complete brewing chains. Reachable potion outputs display the full original cost from three glass bottles and 750 mB water through up to six ordered ingredient steps, matching the machine's batch and material-slot behavior; one-step brewing recipes are kept only as fallback for custom recipes that cannot be traced back to glass bottles through `PotionBrewing`.
- **Fixed**: Advanced Potion Brewer JEI now groups equivalent brewing materials for the same input/output transition into rotating alternatives, so recipes like Mundane Potion show all valid vanilla and modded ingredient candidates instead of only the first discovered path.
- **Fixed**: Advanced Potion Brewer external item automation now hides the internal blaze-powder fuel stack from adjacent item handlers while still accepting fuel insertion. This prevents AE2 blocking-mode pattern providers from treating stocked fuel as leftover processing input and refusing to dispatch potion recipes.
- **Changed**: Advanced Potion Brewer crafting now uses 2 Time Crystals, 4 Eclipse Alloy Ingots, 2 JDT Fluid Canisters, and 1 Bucket.
- **Fixed**: Looting and Sharpness Bio Crusher upgrades are now shown in the JDTE creative tab; the items, recipes, tooltips, and Bio Crusher behavior already existed but were not added to the tab display list.
- **Fixed**: Extended Bio Crusher now implements the 8-slot extended upgrade panel instead of showing only four normal upgrade slots.
- **Changed**: Bio Crusher now exposes one visible Sharpness slot and one visible Looting slot next to the target mode button. The dedicated slots support stacked upgrades, show gray ghost icons, have empty-slot tooltips, and use JDT's Negate Fall Damage / Potion Arrow upgrade artwork.
- **Changed**: Bio Crusher target mode button and dedicated Sharpness/Looting slots are now moved above the output row while keeping their X positions. Only the Extended Bio Crusher has the 18-slot paged output inventory; the Advanced Bio Crusher drops generated items into the world as before, while the Extended Bio Crusher stores generated drops in its output slots when space is available.
- **Fixed**: Bio Crusher and Life Extractor hostile/friendly modes now classify targets by hostile entity type instead of the current `Mob.isAggressive()` AI state, so idle hostile mobs are no longer treated as friendly.
- **Fixed**: Bio Crusher spawner integration now intercepts the spawn cycle before vanilla creates entities. The crusher consumes the cycle only when it can generate drops/fluid successfully, then calls the matching vanilla or Apothic Spawners delay path to keep SpawnPotentials and modded spawn-count changes compatible; otherwise the original spawner logic continues.
- **Development runtime**: Added Apotheosis, Apothic Attributes/Enchanting/Spawners, Bookshelf, Enchantment Descriptions, Prickle, Placebo, and Hostile Neural Networks to the local runtime dependency set.
- **Build**: Updated NeoForge from `21.1.230` to `21.1.233` and raised the required NeoForge version range to `[21.1.233,)`.

#### v0.5.2
- **Fixed**: Extended Dropper GUI crash — `MACHINE_SLOTS` set to 9 in constructor, `getMachineHandler()` auto-expands old 1-slot handler to 9-slot.
- **Fixed**: Registered `Capabilities.ItemHandler.BLOCK` for all JDTE machines, enabling external pipe interaction (Mekanism, etc.).

#### v0.5.1
- **GuideME**: Fixed bilingual page display — default language pages moved to root, English pages moved to `_en_us` directory.
- **GuideME**: Added documentation for Life Extractor and Infusion Machine (EN/CN).
- **Fixed**: Missing blockstate/model warnings for `life_fluid_block`.
- **Fixed**: `basic_glue_activator` recipe pattern — undefined `L` symbol.
- **Fixed**: GuideME `item_ids` conflicts for 5 entries.
- **Fluid bar**: Unified all machine fluid bars to right side (offset=204).
- **Filter system**: Reverted filter slot position to JDT default (8,54); added pagination (prev/next buttons + page number) for FILTER upgrade.
- **Filter state**: Sync via `FilterPagePayload` network packet.
- **Fixed**: Time accelerator filter logic — empty filter slots no longer match all blocks in whitelist mode.
- **Mode button**: Replaced vanilla Button with custom texture-rendered button for hostile/passive/all modes.

#### v0.5.0
- **Fixed**: Global filter slot bug — one machine's config no longer causes all machines to lose filter slots.
- **Fixed**: Missing rotation transforms in blockstate JSON for 12 machines — wrench rotation now works visually.
- **GUI**: Added vanilla `container/slot` slot border rendering to upgrade and filter popups.
- **GuideME**: Added English translation (14 pages), Chinese pages moved to `zh_cn/` directory.
- **Fixed**: `fluid-stabilizer.md` navigation position conflict.

#### v0.4.0
- **New**: Bio Crusher (Advanced & Extended) — kills mobs for drops and XP fluid, supports Looting and Sharpness upgrades.
- **New**: Bio Crusher can be placed above mob spawners to intercept spawn logic.
- **New**: Boss essence items — Wither Essence, Ender Dragon Essence, Elder Guardian Essence.
- **New**: Looting upgrade and Sharpness upgrade cards.
- **New**: SpawnerMixin — intercepts spawner logic for Bio Crusher.
- **New**: Config system — game-accessible via Mods > Config menu.
- **New**: Life Extractor (Advanced & Extended) — extracts life fluid from entities in range.
- **New**: Infusion Machine (Advanced & Extended) — infusion processing using gel and items.

#### v0.3.0
- *(Details not recorded)*

#### v0.2.0
- **New**: 9 upgrade card types — extended to include Filter and Creative upgrades.
- **New**: 3 time accelerator tiers — Basic, Advanced, Extended Advanced.
- **New**: Extended Advanced Time Accelerator with 8 upgrade slots.
- **New**: 6 new automation machine families — Glue Activator, Gel Generator, Fluid Stabilizer, Item/Fluid Sender, Item/Fluid Receiver.
- **New**: Upgrade and filter popup windows — independently displayed, draggable, position-persistent.
- **New**: Dynamic filter slots — expandable based on FILTER upgrade count.
- **Docs**: README and AGENTS.md synced to current code.

#### v0.1.0
- **Initial release**: Core upgrade system.
- **New**: Basic/Advanced Time Accelerator.
- **New**: 8 Extended JDT T2 machines.
- **New**: GuideME in-game documentation pages.

---

### 中文

#### v0.5.3（当前）
- **修复**：生物粉碎机强制击杀现在执行带假玩家归因的真实死亡流程，不再只读取普通战利品表后直接移除实体。凋灵下界之星等 BOSS 自定义死亡掉落会与 JDTE 精华一起正常捕获和输出。
- **JEI**：为凋灵、末影龙和远古守卫者精华增加信息页，说明获取概率与当前灌注用途。
- **性能**：生命提取器改为通过扩大单次批处理提速，不再依赖增加范围扫描次数。高级/扩展普通模式每 20 tick 处理 4/8 个目标，超频或创造每 5 tick 处理 8/16 个，超频扫描从每秒 20 次降为 4 次。
- **调整**：高级/扩展生命提取器和生物粉碎机保留正常挖掘硬度，但改为凋灵级不可爆破。凋灵精华基础掉落概率改为 5%；抢夺只在成功触发后影响数量。
- **调整**：生命提取器改为直接移除目标，不再触发 Minecraft 正常死亡流程，因此只产生生命流体，不产生普通掉落物或经验。当前生命值转换倍率默认改为每点 0.1 mB，并保留小数配置。
- **新增**：加入末影龙刷怪蛋灌注配方，消耗 1 个末影龙精华、64 B 生命流体和 100,000 FE。末影龙精华基础掉落概率改为 10%；抢夺只在成功触发后影响数量。
- **调整**：生命苹果 tooltip 改为红色，JEI 信息页只保留简短介绍，GuideME 改用表格展示成长计算。
- **新增**：生命苹果现在可无上限永久累计提升最大生命值、护甲和护甲韧性。成长次数保存在玩家数据中，并在死亡或登录后重新应用；tooltip、JEI 信息页和 GuideME 已写明重复里程碑公式。
- **新增**：加入可实际生成原版凋灵的 JDTE 凋灵刷怪蛋。灌注 1 个凋灵精华与 64 B 生命流体并消耗 100,000 FE 可制作；通用生物掉落物扫描会排除凋灵，避免额外生成下界之星配方。
- **调整**：生命提取器改为依据目标当前剩余生命值产出生命流体。整数服务端配置 `jdte.lifeExtractor.fluidPerHealth` 默认每点当前生命值产出 1 mB；实体小数生命值和速度升级产生的小数结果会累计并持久化，不再丢失。
- **新增**：灌注机可使用某种能唯一标识生物的完整一组掉落物、64 B 生命流体和 100,000 FE 自动合成对应刷怪蛋。配方从已注册刷怪蛋和当前实体战利品表 JSON 自动发现，跳过共享掉落物造成的歧义，并显示在 JEI 中。
- **修复**：服务端发现的刷怪蛋灌注配方会在玩家登录和数据包重载后同步到客户端，解决 JEI 早于客户端世界初始化而看不到配方的问题。凝胶发生器、灌注机和炼药机配方增加稳定配方 ID，恢复 JEI 的整张配方书签按钮。
- **修复**：刷怪蛋灌注配方改为只通过一条 JEI 注册路径加入，消除完全相同的重复条目；灌注机可见配方列表会按刷怪蛋配方优先的顺序重建。
- **修复**：扩展高级投掷器改为继承 JDT 完整 T2 实现，恢复配置偏移落点、过滤槽选择、正确的每次 25 FE 消耗，以及重新打开 GUI 时投掷数量和拾取延迟设置的显示。
- **修复**：扩展高级点击器现在通过继承菜单实际打开的 JDT 高级点击器界面初始化设置，重新打开机器时不再把已保存的非“方块”目标显示为默认值。
- **修复**：为生物粉碎机新增可选的龙之研究混沌守卫兼容。服务端配置 `allowDestroyChaosGuardianCrystals` 与 `allowInstantKillChaosGuardian` 分别控制自动破坏外层水晶和带假玩家归因的守卫秒杀，两项默认均为 `false`。启用后仍由 DE 战斗管理器正常结算 Boss 血条、龙心、中央混沌水晶解锁及延迟经验，不直接删除实体。
- **修复**：Placebo/神化在进入世界时同步酿造配方期间，高级炼药机不再因并发修改酿造列表而使集成服务器崩溃；遇到同步窗口时机器会暂停当前 tick，并在下一 tick 使用同步完成后的模组配方继续处理。
- **变更**：扩展生物粉碎机输出库存现在随容量升级扩展。基础 18 格，每张容量升级额外开放 9 格，默认 3 张容量升级时最多 45 格。GUI 仍每页显示 9 格，自动输出只抽取当前有效槽位，容量降低后高位槽中已有物品仍保持可取出。
- **修复**：高级生物粉碎机现在通过原版物品掉落路径把战利品生成在被击杀实体原位置，不再因为自身没有输出库存而丢失已捕获的掉落物；扩展生物粉碎机仍优先写入输出槽，仅在槽位已满时从机器上方溢出物品。
- **变更**：生物粉碎机经验流体改为按实体经过 NeoForge 和其它模组修正后的实际最终经验计算，包含神化刷怪笼“回响”的经验加成，不再按最大生命值估算。保留全局配置并改为 `experienceFluidPerPoint`，默认 `1.0`，表示每点最终经验转换的 mB 数；普通击杀、强制击杀和刷怪笼虚拟击杀统一使用该换算且不会生成经验球。
- **修复**：生物粉碎机拦截刷怪笼时现会读取神化刷怪笼的公开动态统计，兼容修改后的生成数量、初始生命、静音、无 AI、幼年、燃烧和回响。回响会通过神化自己的掉落事件生成额外战利品，神化专用延迟方法也会保留修改后的最小/最大生成延迟。锋利直接击杀改用 `isDeadOrDying()` 判断，不再重复执行一次强制掉落流程。
- **修复**：生物粉碎机在原版及神话刷怪笼上方虚拟处理时，现会加载完整 `SpawnData` 实体 NBT，执行正常刷怪笼生成初始化和装备表，再捕获完整虚拟死亡结果。刷怪笼生成的武器、工具、盔甲、自定义死亡掉落和玩家击杀条件均会保留，且实体不会实际加入世界。
- **修复**：锋利升级使生物被正常伤害直接击杀时，掉落物不再生成到世界中；现在会在其它模组完成修改后的 `LivingDropsEvent` 阶段捕获最终掉落，并送入扩展生物粉碎机输出槽。抢夺升级现在对范围内实体和刷怪笼虚拟处理均生效，使用真实抢夺武器、完整玩家击杀上下文，并按配置执行文档所述的每级 50% 额外掉落判定。
- **变更**：生物粉碎机现在会使用缓存的假玩家和实际附有抢夺的武器进行攻击，并捕获经过其它模组调整后的掉落物。目标仍存活时默认继续强制击杀；可在全局 `config/jdte/jdte.toml` 中将 `jdte.bioCrusher.respectDamageRestrictions` 设为 `true` 来尊重假玩家/伤害限制。新增实体类型标签用于完全排除目标或仅禁止强制击杀；经验流体与能量只在成功击杀后结算，未知模组实体不再生成伪造的兜底掉落。
- **构建系统重构**：重写 `build.gradle`，提取 `dependencies.gradle` 分离依赖管理；新增 `localRuntime`/`clientLocalRuntime` 配置和 `clientRuntime` 源集；新增 `ExpandPropertiesAction` 处理资源替换；生成 sources jar 和 jar manifest。添加 WCWT 模组的所有开发运行时依赖。
- **JDT 依赖**：从本地 jar 文件改为 CurseMaven 坐标 `curse.maven:just-dire-things-1002348:7463040`。
- **GUI 重新设计方案**：创建 `GUI_REDESIGN.md`，详细分析全部 37 台机器的组件和升级配置，检测重叠问题，提出嵌入式的无弹窗 GUI 布局，含精确尺寸计算。
- **升级面板改造**：所有机器升级面板均改为嵌入式固定面板，去除弹窗。4 槽机器在物品栏右侧有一个纵向 1×4 面板。8 槽机器有两个面板——右侧（槽位 0–3）和物品栏左侧（槽位 4–7）。所有槽位坐标软编码到 `assets/jdte/gui_layout.json`，新增 `GuiUpgradeLayoutConfig` 运行时读取配置。
- **升级槽 tooltip**：空升级槽现在显示该机器可用的升级类型及当前限制（数量/最大数量），未兼容的升级和速度升级冲突（超频/降频）以灰色标记。
- **粘胶激活器 GUI**：高级和扩展粘胶激活器屏幕现在使用 `extraWidth=60`，与 ClickerT2 屏幕尺寸一致。
- **凝胶发生器 GUI 最终对齐**：GEL/FOOD 槽 X 坐标与第一个过滤槽对齐（x=8），输入/输出物品列位于 x=44/x=116，输入/输出流体条拆成两列并位于 x=62/x=134。进度箭头和速度按钮居中于物品列之间；黑名单/匹配NBT/红石控制/自动均分按钮移动到 x=170。所有坐标通过 `assets/jdte/gui_layout.json` 和 `GuiUpgradeLayoutConfig` 软编码。
- **凝胶发生器均分按钮 tooltip**：tooltip 根据开关状态动态显示"自动均分"/"不自动均分"，在 `toggleAutoBalance()` 中通过 `setTooltip()` 更新。
- **流体放置器 GUI**：三个等级（初级/高级/扩展）均使用 `extraWidth=60` 和 `getFluidBarOffset()=204`，与扩展时间加速器的流体罐和能量条布局对齐。
- **高级/扩展物品放置器 GUI**：界面大小匹配扩展时间加速器（`extraWidth=60`）。黑名单/匹配NBT/红石控制/渲染区域按钮通过 `item_sender_buttons` 节软编码，上移 40px 至 y=42。速度按钮软编码，相对第五个过滤槽左偏 2px（x=78），下移 4px 至 y=44。9 个物品存储槽通过 `item_sender_slots` 节软编码，上移 18px 至 y=36，第一个槽 X 坐标与第一个过滤槽对齐（x=8）。区域范围（X/Y/Z 半径）和偏移调节按钮完全恢复——`valueButtonsDoubleList`/`valueButtonsList` 控件通过 `addRenderableWidget()` 正确注册。`addAreaButtons()` 使用 `UpgradeHelper.getMaxAreaRadius()` 和 `getMaxAreaOffset()` 获取动态最大值。
- **初级物品放置器 GUI**：黑名单/匹配NBT/红石控制/渲染区域/速度按钮和 9 个物品存储槽均采用与高级/扩展版本相同的位置，通过 `basic_item_sender_buttons`/`basic_item_sender_slots` 节软编码。容器新增 `addMachineSlots()` 覆盖，屏幕覆盖 `addFilterButtons()`、`addRedstoneButtons()`、`addAreaButtons()`、`addTickSpeedButton()`。
- **物品槽 tooltip**：三个等级（初级/高级/扩展）的物品放置器在悬停于空的机器存储槽时显示"物品槽"提示，通过 `renderTooltip()` 覆盖实现。
- **流体接收器 GUI**：三个等级（初级/高级/扩展）均使用 `extraWidth=60` 和 `getFluidBarOffset()=204`，与扩展时间加速器的流体罐和能量条布局对齐。
- **高级/扩展流体稳定器 GUI**：界面大小改为 `extraWidth=60`，能量槽位置正确对齐。
- **物品接收器 GUI**：三个等级（初级/高级/扩展）现在与对应的物品放置器布局一致。按钮和槽位通过 `item_receiver_buttons`/`item_receiver_slots`/`basic_item_receiver_buttons`/`basic_item_receiver_slots` 软编码。增加"物品槽"tooltip。
- **生命提取器模式按钮**：将精灵表替换为 JDT 生物扫描/生物透视/夜视图标，增加模式 tooltip（敌对/友好/全部），通过 `life_extractor_buttons` 节软编码。修复坐标参考系问题（`topSectionLeft` → `leftPos`）以正确定位。
- **生物粉碎机模式按钮**：与生命提取器相同改造 — JDT 图标、tooltip、通过 `bio_crusher_buttons` 节软编码。
- **修复**：扩展传感器 GUI 崩溃 — MenuType 注册传入 JDT 的 `SensorT2_Container`；`SensorT2Screen` 的 `instanceof SensorT2BE` 检查导致 NPE；JDT jar 1.5.7 为旧版 API，修正 API 版本不匹配。
- **变更**：扩展传感器过滤槽改为 9 个（与 JDT 高级传感器一致）。
- **新增**：凝胶发生器（高级/扩展）机器槽位 tooltip — 鼠标悬停时显示槽位类型（凝胶/食物/输入/输出）。
- **修复**：自动均分按钮 tooltip 文本从"不自动均分"改为"取消均分"。
- **变更**：过滤翻页按钮缩放至与范围按钮相同大小（`PoseStack.scale 0.75`），并添加 tooltip。
- **修复**：翻页后过滤槽内容不更新 — `DynamicFilterSlot` 重写 `SlotItemHandler` 中所有使用 `this.index` 的方法，改为调用 `getSlotIndex()`。
- **调整**：过滤下一页按钮左移 4px，两翻页按钮下移 2px。
- **调整**：过滤页码文字颜色改为白色，后改为 `0xFF404040` 以匹配区域标签样式。
- **调整**：过滤页码文字从下一页按钮右侧 14px 处移至 2px 处。
- **修复**：移除过滤升级后非默认页的过滤槽不再消失 — 当前页超出最大页时自动重置到第 0 页。
- **新增**：高级/扩展凝胶发生器的 JEI 配方界面 — 动态读取 JDT `GooSpreadRecipe`/`GooSpreadRecipeTag` 和当前凝胶复活食物标签。配方按凝胶等级拆分，同一凝胶等级的可用食物在食物槽内轮换。凝胶槽标记为不消耗的催化剂，并显示“不消耗”tooltip；物品配方绘制原来的四个输入/输出槽，但只在第一个槽显示物品，并将进度箭头和输出列左移 18px，在输出列右侧增加动态能耗条，页面尺寸改为更紧凑；流体配方使用与物品页相同的输入/输出/进度/能量列位置，并同样显示动态能耗条。
- **修复**：为 JDTE 流体机器加入类似 JDT 流体放置器的右键流体容器转移逻辑。水桶和兼容的单物品流体容器现在可对时间加速器、凝胶发生器、流体放置/接收器、生物粉碎机、生命提取器、灌注机、扩展流体收集器/放置器进行填充或抽取。扩展流体收集器/放置器同步补充 `Capabilities.FluidHandler.BLOCK` 能力。
- **新增**：为含有物品或流体输入/输出的机器添加首版自动输入输出方向配置面板，可切换显示/隐藏。面板使用 JDT 的方向、穿墙、诱饵、重锤图标，按钮为 12px，关闭状态会变暗，并使用机器界面背景。方向默认全部关闭，并通过可序列化 attachment 在服务端保存。
- **调整**：自动输入输出方向配置改为固定面板，面板右下角挂靠在上方机器配置面板左下角；方向按钮间距收紧为 12px，正中间按钮改为绝对方向“上”，关闭的方向按钮改用与 JDT 灰度按钮一致的深色灰度显示；通过 JEI extra area 自动规避书签重叠；按钮只在真正有物品或流体 I/O 的机器上显示。
- **修复**：自动输入输出配置网络不再把同一个 `jdte:auto_io_config` payload 同时注册到双向通道。服务端同步改用 `jdte:auto_io_config_sync`，修复 `RegisterPayloadHandlersEvent` 阶段的启动崩溃。
- **新增**：自动输入输出方向开关现在会在服务端执行邻接物品/流体传输。JDTE 机器按明确的输入/输出槽位和储罐语义处理；支持的 JDT 扩展机器使用保守的已知输入路径，并区分流体收集器/放置器储罐方向。成功传输可每 tick 执行，单 tick 最高 256 个物品和 16000 mB；失败时最多退避到 40 tick，避免持续扫描空邻居。
- **变更**：自动输入输出方向按钮现在映射到绝对世界方向（北/南/西/东/上/下），不再使用依赖机器朝向的前/后/左/右方向。
- **开发运行时**：将 Logistics Networks（`curse.maven:logistics-network-1448257:8381956`）加入本地运行时依赖，用于性能对比和后续自动输入输出设计参考。
- **新增**：加入蚀空合金扳手，供 JDTE 和 JDT 机器搬运使用。该扳手继承 JDT 核源铁扳手的交互路径，避免右键时被机器 GUI 拦截；右键支持的机器会直接拆下为带保存数据的方块物品，保留内部材料、电量、流体、升级和配置；临时模型复用 JDT 的核源铁扳手模型。
- **平衡性**：时间加速器的时间流体消耗默认改为与 JDT 时间手杖一致，按同样的 30 秒持续时间折算，不再每 tick 收取完整倍率消耗。新增 `timeAcceleratorFluidCostMultiplier` 服务端配置和 `/jdte timeaccelerator fluidCostMultiplier <value>` 命令，可在线调整消耗倍率。
- **变更**：蚀空合金扳手普通右键现在与 JDT 核源铁扳手一致，用于调节机器方向；蹲下右键保留 JDTE 的机器拆卸搬运功能。新增可选 FTB Ultimine 右键集成，可批量调节方向或批量拆卸支持的机器。
- **新增**：手持蚀空合金扳手对准范围机器滚动鼠标滚轮时，可同时调节 X/Y/Z 作用范围。FTB Ultimine 存在有效方块选区时，会对选区内匹配的机器批量应用滚轮调节。
- **变更**：滚轮范围调节改为需要按住**扳手范围修饰**键（默认左 Alt，可在快捷键设置中更改）才会触发。移除了蚀空合金扳手的物品 tooltip，使用说明已移至 GuideME 游戏内指南（按 G 键打开）。
- **新增**：滚轮调节范围时在快捷栏上方绿色文字显示当前范围/最大范围，并播放旋转音效。
- **修复**：重写蚀空合金扳手 GuideME 页面为纯 Markdown，避免不支持的 keybind/image/recipe 组件导致指南格式错误。FTB Ultimine 范围滚轮现在只在主动按住连锁键时使用连锁选区，缓存选区不再影响普通范围调节。
- **修复**：扳手滚轮范围调节在客户端和服务端重新要求手持蚀空合金扳手，仅按住范围修饰键不再能触发调节。
- **开发运行时**：加入 FTB Ultimine（`dev.ftb.mods:ftb-ultimine-neoforge:${ftb_ultimine_version}`）和 AppleSkin（`curse.maven:appleskin-248787:7854442`）本地运行时依赖。
- **新增**：灌注机现在可以通过 NeoForge 标准物品流体能力自动处理流体容器填充。空桶和兼容的其它模组容器可直接使用机器流体槽中的对应流体灌注，不需要单独编写配方 JSON；原版玻璃瓶可用水灌注为水瓶。
- **新增**：为高级/扩展灌注机添加 JEI 配方分类。现在会显示 `jdte:infusion` 数据配方，并自动识别当前物品/流体能力支持的流体容器填充配方，包括原版水瓶。
- **新增**：灌注机现在可用玻璃瓶和蜂蜜类流体制作蜂蜜瓶。蜂蜜流体通过常见标签以及包含 honey 的流体 ID/翻译键识别，兼容更多模组的蜂蜜流体。
- **新增**：点击凝胶发生器和灌注机 GUI 的进度箭头，现在会打开对应 JEI 配方分类。
- **性能优化**：修复打开机器 GUI 后嵌入式升级面板导致严重掉帧的问题。升级面板背景不再每帧使用九宫格 sprite 平铺绘制，改为固定尺寸 `upgrade_slot_panel.png` 贴图；升级槽图标仍正常渲染。
- **开发运行时**：加入 Industrial Foregoing（`curse.maven:industrial-foregoing-266515:8370717`）本地运行时依赖。
- **修复**：炼药机 GUI 现在只裁切原版酿造台贴图中覆盖机器槽位并额外保留 2px 边距的区域，瓶子槽位已对齐背景槽位图案，背景裁切、机器槽位和动态组件坐标都已通过 `assets/jdte/gui_layout.json` 软编码。
- **变更**：炼药机背景和原有槽位整体上移 10px，在原材料输入槽上方新增 5 个软编码横向材料输入槽，并在右侧新增 3 个软编码纵向产物输出槽。炼药流程现在消耗瓶子输入并将成品写入输出槽，自动输入输出也会识别新的输入/输出槽位。
- **调整**：炼药机额外材料输入行现在与能量槽顶部对齐。速度按钮软编码到最下方输出槽右侧，红石按钮软编码到中间输出槽右侧；输出槽会显示灰色玻璃瓶底图，槽位限制改为通过当前 `PotionBrewing` 注册表遵循原版酿造台规则。
- **修复**：炼药机酿造进度现在像原版酿造台一样每个服务端 tick 推进，不再被 JDT 机器 tickSpeed 间隔卡住；水瓶 + 下界疣会立即出现动画，并按预期 400 tick 完成。
- **修复**：炼药机动态效果改为使用原版 1.21 酿造台的燃料、进度箭头和气泡 sprite，不再裁切旧版贴图坐标；烈焰粉燃料条和酿造动画会正常显示。
- **新增**：炼药机红石控制按钮上方新增锁定槽位按钮，使用 JDT 水下呼吸升级图标。锁定槽位会快照当前非空材料槽，限制这些材料槽只能放入对应物品，并在空锁定材料槽显示灰色 ghost 物品图标；瓶子槽、产物槽、空材料槽和烈焰粉燃料槽不锁定。
- **修复**：炼药机锁定槽位按钮点击后不再闪一下又回到关闭。客户端只在服务端确认后更新按钮状态，tooltip 已改为“锁定输入：开/锁定输入：关”。
- **变更**：炼药机材料槽现在作为有序酿造链使用：原材料槽是第 1 个消耗槽，顶部 5 个横向材料槽从左到右是第 2-6 个消耗槽。机器会按顺序检测非空且可用的材料槽，空槽或不匹配槽会跳过，中间产物保留在瓶子槽继续下一步，所有可用材料槽检测完成后才移动到产物槽。
- **新增**：炼药机新增两个软编码流体槽。左侧水槽位于能量条右侧，可将瓶子槽中的玻璃瓶自动消耗水转成水瓶；右侧时间流体槽使用与灌注机相同的右流体槽位置。两个流体槽都会保存、同步、暴露流体能力，支持自动输入输出和右键流体容器转移。
- **修复**：炼药机瓶子槽和产物槽现在在 GUI 槽位层面明确限制最大堆叠为 1，Shift-click 会把瓶子、烈焰粉、酿造材料分别送入对应槽位组，不再把一组玻璃瓶塞进单个瓶子槽。
- **平衡性**：炼药机速度现在表示每个材料步骤的目标 tick 数。时间流体按 JDT 时间手杖各档倍率的共同效率折算：每个完整 400 tick 酿造步骤费用为 `JDT时间手杖流体消耗 * 2 / 3`，机器再按当前速度节省的 tick 比例扣费。速度 `1` 收完整节省费用，速度 `400+` 为原版速度且不消耗时间流体。
- **修复**：炼药机 Shift-click 和 Mouse Tweaks 拖放物品不再因调用 JDT 私有物品转移方法而闪退；现在使用炼药机自己的槽位上限感知转移路径。
- **修复**：炼药机自动化物品能力现在会限制槽位语义：外部物流只能向瓶子/材料/燃料输入槽插入物品，只能从 3 个产物槽抽取物品。自动输入时也会优先把烈焰粉放入燃料槽，再尝试材料槽。
- **调整**：炼药机空机器槽现在会显示槽位 tooltip，覆盖瓶子输入、有序材料步骤、烈焰粉和产物槽。被锁定的空材料槽还会显示锁定的物品名称。
- **文档**：重写高级炼药机 GuideME 页面，改为更清晰的层级结构，并在页面顶部加入机器模型预览；内容拆分为界面结构、酿造流程、自动化、锁定输入、时间流体费用、升级和合成。
- **新增**：添加高级炼药机 JEI 配方分类。配方通过当前 `PotionBrewing` API 自动识别原版和 NeoForge 酿造配方，并包含机器的玻璃瓶 + 水自动装水配方；页面按机器界面显示三瓶输入、三格输出、水槽、能量条、原版酿造台背景、燃料条、气泡和进度箭头，不显示时间流体槽，注册炼药机为配方催化剂，点击机器进度箭头可打开该分类。
- **变更**：高级炼药机 JEI 配方现在优先显示完整酿造链路。可从玻璃瓶推导到的药水产物会显示从 3 个玻璃瓶、750 mB 水开始，到最多 6 个顺序材料步骤的完整消耗，匹配机器的一批 3 瓶和材料槽顺序；只有无法通过 `PotionBrewing` 反推到玻璃瓶的自定义配方才保留一步配方兜底。
- **修复**：高级炼药机 JEI 现在会把同一输入/输出阶段的等价酿造材料合并为轮换候选，因此平凡的药水这类配方会显示全部原版和模组新增的可用材料，而不是只显示第一条推导路径。
- **修复**：高级炼药机对外物品自动化现在会隐藏内部烈焰粉燃料堆叠，但仍允许外部插入燃料。这样 AE2 阻塞模式的样板供应器不会把常驻燃料误判为未清空的处理输入，从而拒绝发配炼药配方。
- **变更**：高级炼药机合成配方改为 2 个时间水晶、4 个蚀空合金锭、2 个 JDT 流体罐和 1 个桶。
- **修复**：抢夺升级和锋利升级现在会显示在 JDTE 创造标签页中；物品、配方、tooltip 和生物粉碎机效果原本已实现，但之前没有加入标签页显示列表。
- **修复**：扩展生物粉碎机现在正确使用 8 槽扩展升级面板，不再只显示 4 个普通升级槽。
- **变更**：生物粉碎机在目标模式按钮左右两侧分别显示锋利升级槽和抢夺升级槽。专用槽支持升级堆叠，空槽显示灰色 ghost 图标和 tooltip，并使用 JDT 的“抵消摔落”/“药箭”升级图标。
- **变更**：生物粉碎机目标模式按钮和锋利/抢夺专用槽移动到输出槽上方，X 坐标保持不变。只有扩展生物粉碎机拥有 18 格分页输出库存；高级生物粉碎机生成的物品仍直接掉落到世界中，扩展生物粉碎机在有空间时会把生成掉落物写入输出槽。
- **修复**：生物粉碎机和生命提取器的敌对/友好模式现在按敌对实体类型判断，不再使用当前 AI 攻击状态 `Mob.isAggressive()`，空闲敌对生物不会再被当成友好。
- **修复**：生物粉碎机刷怪笼集成现在会在生成实体之前接管刷怪周期。机器只有在能成功生成掉落物/经验流体时才消耗本轮刷怪，并调用原版或神化刷怪笼对应的延迟逻辑以兼容 SpawnPotentials 和其它模组修改的 spawnCount；处理失败时会继续走原刷怪逻辑。
- **开发运行时**：加入 Apotheosis、Apothic Attributes/Enchanting/Spawners、Bookshelf、Enchantment Descriptions、Prickle、Placebo 和 Hostile Neural Networks 本地运行时依赖。
- **构建**：NeoForge 从 `21.1.230` 更新到 `21.1.233`，最低版本范围同步提高到 `[21.1.233,)`。

#### v0.5.2
- **修复**：扩展投掷器 GUI 崩溃 — 构造函数设置 `MACHINE_SLOTS=9`，`getMachineHandler()` 自动扩展旧存档的 1 槽 handler 到 9 槽。
- **修复**：为所有 JDTE 机器注册 `Capabilities.ItemHandler.BLOCK`，使外部管道（如 Mekanism）可以交互。

#### v0.5.1
- **GuideME**：修复中英文双语页面显示 — 默认语言页面移至根目录，英文页面移至 `_en_us` 目录。
- **GuideME**：新增生命提取器和灌注机的中英文文档。
- **修复**：`life_fluid_block` 缺少 blockstate 和 model 导致的警告。
- **修复**：`basic_glue_activator` 配方中未定义的 `L` 符号。
- **修复**：GuideME 中 5 个 `item_ids` 冲突警告。
- **流体条**：统一所有机器的流体条到右侧（偏移量 204）。
- **过滤系统**：过滤槽位置恢复为 JDT 默认（8,54）；安装过滤升级后增加翻页功能（上一页/下一页按钮和页码显示）。
- **过滤状态**：通过 `FilterPagePayload` 网络包同步到服务端。
- **修复**：时间加速器过滤逻辑 — 空过滤槽时白名单模式不再加速所有方块。
- **模式按钮**：用自绘贴图按钮替换原版 Button，支持敌对/友好/全部三种模式。

#### v0.5.0
- **修复**：全局过滤槽 bug — 一台机器的配置不再导致所有机器的过滤槽消失。
- **修复**：12 台机器的 blockstate JSON 缺少旋转变换 — 扳手旋转现在视觉生效。
- **GUI**：升级弹窗和过滤弹窗增加原版 `container/slot` 槽位边框渲染。
- **GuideME**：新增英文翻译（14 页），中文页面移至 `zh_cn/` 目录。
- **修复**：`fluid-stabilizer.md` 导航位置冲突。

#### v0.4.0
- **新增**：生物粉碎机（高级和扩展） — 杀死生物产生掉落物和经验流体，支持抢夺和锋利升级。
- **新增**：生物粉碎机可放置在刷怪笼上方拦截刷怪逻辑。
- **新增**：BOSS 精华物品 — 凋灵精华、末影龙精华、远古守卫者精华。
- **新增**：抢夺升级和锋利升级卡。
- **新增**：SpawnerMixin — 拦截刷怪笼逻辑。
- **新增**：可配置系统 — 支持游戏内 Mods > Config 编辑。
- **新增**：生命提取器（高级和扩展） — 从范围内生物提取生命流体。
- **新增**：灌注机（高级和扩展） — 使用凝胶和物品进行灌注加工。

#### v0.3.0
- *（未记录详细内容）*

#### v0.2.0
- **新增**：9 种升级卡 — 扩展到包含过滤升级和创造升级。
- **新增**：3 种时间加速器等级 — 初级、高级、扩展高级。
- **新增**：扩展高级时间加速器支持 8 个升级槽。
- **新增**：6 种自动化机器族 — 粘胶激活器、凝胶发生器、流体稳定器、物品/流体放置器、物品/流体接收器。
- **新增**：升级和过滤悬浮弹窗 — 可独立显示、拖动、位置持久化。
- **新增**：动态过滤槽 — 根据过滤升级数量动态增加。
- **文档**：同步更新 README 和 AGENTS.md。

#### v0.1.0
- **初始发布**：核心升级系统。
- **新增**：初级/高级时间加速器。
- **新增**：8 种扩展 JDT T2 机器。
- **新增**：GuideME 游戏内文档页面。
