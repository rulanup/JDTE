# Task 5 Report — Large Portable Containers

Date: 2026-08-23

Worktree: `D:\MCDev\idea-projects\JDTE\.worktrees\large-portable-containers`

Branch: `codex/large-portable-containers`

## Scope

Executed Task 5 from `.superpowers/sdd/2026-08-23-large-portable-containers-plan/task-5-brief.md` in the feature worktree only. Reviewed Task 1-4 implementation, ran the requested verification commands serially, inspected git status/diff, and performed a static/manual acceptance audit against the large portable container requirements. This report also includes the follow-up runtime/UI repair pass requested after the initial Task 5 commit.

## Exact verification commands and results

Authoritative latest serial verification run after the live-screen and large-item-hover repair:

1. `./gradlew test --tests com.jdte.client.screens.LargeCanisterScreenLiveRefreshTest --tests com.jdte.common.items.LargeCanisterTooltipTest --tests com.jdte.client.screens.LargePocketGeneratorScreenTest --tests com.jdte.common.items.LargePortableContainerLogicTest`
   - Result: `BUILD SUCCESSFUL in 28s` (36 tests)

Full serial verification:

1. `./gradlew test`
   - Result: `BUILD SUCCESSFUL in 23s`

2. `./gradlew compileJava`
   - Result: `BUILD SUCCESSFUL in 1s`

3. `./gradlew validateDocs`
   - Result: `FAILED`
   - Remaining failures are limited to the pre-existing Patchouli validator mismatch:
     - missing generated resources under `src/main/resources/data/jdte/patchouli_books/jdte_guide/...`
     - missing generated resources under `src/main/resources/assets/jdte/patchouli_books/jdte_guide/...`
     - missing/stale translations for `item.jdte.patchouli_guide.name` and `.landing`
   - Important negative result: the follow-up branch-specific docs regression (`category mapping mismatch; extra=['large-fuel-canister', 'large-pocket-generator', 'large-potion-canister']`) was fixed by updating the generator category map and tests. The validator now fails only in the baseline “expected generated jdte_guide resources are not checked in / landing translations stale” shape.

Supporting inspection commands used during the audit:

- `git status --short`
- `git diff --stat`
- `javap -classpath D:\MCDev\.gradle\caches\modules-2\files-2.1\curse.maven\just-dire-things-1002348\7463040\f0327d7d2f389020063849200a4128ac9dcb52da\just-dire-things-1002348-7463040.jar -c com.direwolf20.justdirethings.common.items.PocketGenerator`
- `javap -classpath D:\MCDev\.gradle\caches\modules-2\files-2.1\curse.maven\just-dire-things-1002348\7463040\f0327d7d2f389020063849200a4128ac9dcb52da\just-dire-things-1002348-7463040.jar -c com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE`
- `javap -classpath D:\MCDev\.gradle\caches\modules-2\files-2.1\curse.maven\just-dire-things-1002348\7463040\f0327d7d2f389020063849200a4128ac9dcb52da\just-dire-things-1002348-7463040.jar -c com.direwolf20.justdirethings.client.screens.GeneratorT1Screen`

## Real defects found and fixed

1. Large pocket generator did not receive Curios tick behavior while equipped in a Curios slot.
   - Impact: acceptance item “Curios 中自动给背包和 Curios 中的可充能物品供能” was not satisfied, because JDT charging happens in `PocketGenerator.inventoryTick(...)`.
   - Fix:
     - registered an optional Curios behavior in `com.jdte.common.integrations.curios.BigFluidTankCuriosIntegration.registerCurioBehaviors()`
     - invoked it from `JDTE.commonSetup(...)`
   - Reasoning: Curios docs state slot tags assign equipment slots, but Curios behavior while slotted requires `ICurio`/`ICurioItem` behavior. JDT `PocketGenerator` charging logic lives in `inventoryTick(...)`.

2. Normal JDT generators did not apply the large fuel canister’s x10 burn multiplier.
   - Impact: acceptance item “JDT 发电机识别大型燃料倍率 ×10” failed outside the generator-upgrade helper path.
   - Fix:
     - added a `@Redirect` in `com.jdte.mixin.GeneratorT1UpgradeMixin` so `GeneratorT1BE.doBurn(...)` uses `LargeFuelCanisterItem.getBurnSpeedMultiplier(...)` for large fuel canisters.

3. Large pocket generator fuel tooltip used base JDT fuel canister multiplier instead of the large-canister x10 value.
   - Impact: UI evidence contradicted actual intended burn scaling.
   - Fix:
     - updated `LargePocketGeneratorScreen.burnSpeedMultiplierTooltipValue(...)`
     - updated `LargePocketGeneratorScreenTest` to assert `20` instead of `2` for a representative large canister case.

4. Large pocket generator burn initialization still stored the base JDT fuel canister multiplier for `LargeFuelCanisterItem`.
   - Impact: runtime and UI drift — the large pocket generator could display x20 fuel tooltip text while persisting only the base x2 multiplier into `POCKETGEN_FUELMULT`, causing actual burn/init behavior to stay on the plain-fuel path.
   - Fix:
     - added `com.jdte.common.items.PortableFuelBurnSpeedHelper` as the shared source of truth
     - added `com.jdte.mixin.PocketGeneratorFuelMultiplierMixin` to redirect the inherited JDT `PocketGenerator.initBurn(...)` `FuelCanister.getBurnSpeedMultiplier(...)` call through that shared resolver
     - added regression coverage for the resolved large-canister multiplier and the stored pocket-generator multiplier state

5. Normal JDT `GeneratorT1Screen` tooltip still displayed the base fuel-canister multiplier for `LargeFuelCanisterItem`.
   - Impact: normal generator runtime and tooltip drift — runtime burn used the large x10 path after the earlier mixin fix, but the screen still rendered the plain x2 tooltip branch.
   - Fix:
     - added `com.jdte.mixin.GeneratorT1ScreenFuelTooltipMixin` to redirect the tooltip’s `FuelCanister.getBurnSpeedMultiplier(...)` call through the shared resolver
     - updated the large pocket generator screen to use the same shared resolver
     - updated regression coverage so both plain and large canister branches are asserted

6. New large container items initially piggybacked on the Big Fluid Tank GuideME page instead of having dedicated entries.
   - Impact: documentation coverage existed, but it was not ideal for direct item lookup/review.
   - Fix:
     - created dedicated minimal guide pages for:
       - `large-pocket-generator`
       - `large-potion-canister`
       - `large-fuel-canister`
       in both `zh_cn` and `en_us`
     - removed their `item_ids` mapping from the Big Fluid Tank page
     - updated `scripts/generate_patchouli_book.py` category ownership and `scripts/test_generate_patchouli_book.py` expected counts/category assertions so the dedicated pages are first-class Patchouli entries

7. Final rereview found that the last cleanup had removed the base `GeneratorT1BE.doBurn()` runtime redirect.
   - Impact: normal generator runtime would again treat `LargeFuelCanisterItem` as a plain `FuelCanister`, even though pocket-generator burn init and both tooltip paths had already moved to the shared resolver.
   - Fix:
     - restored a single `@Redirect` in `com.jdte.mixin.GeneratorT1UpgradeMixin` for the base `doBurn()` `FuelCanister.getBurnSpeedMultiplier(...)` invoke
     - routed that invoke through `PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(...)`
     - preserved generator-upgrade behavior, which still uses `GeneratorUpgradeHelper.burnSpeedMultiplier(...)` and the early-cancel path
     - added `GeneratorFuelRuntimeHookTest` as a focused guard that checks the live mixin source still contains the base `doBurn()` redirect to the shared resolver

8. Large potion/fuel canister screens read a stale bound snapshot for live display state.
   - Impact: after server synchronization or source-stack component changes, potion bars/tooltips and fuel labels could remain on the decoded menu snapshot instead of the current hand/Curios stack.
   - Fix:
     - added `getCurrentStack()` to `LargePotionCanisterContainer` and `LargeFuelCanisterContainer`
     - changed `LargePotionCanisterScreen` bar and tooltip reads to use `menu.getCurrentStack()`
     - changed `LargeFuelCanisterScreen` labels to use `menu.getCurrentStack()`
     - added `LargeCanisterScreenLiveRefreshTest` guards for both screens so a future `getBoundStack()` regression fails

9. Large potion/fuel canisters inherited JDT hover text sized for the original canisters.
   - Impact: potion hover text could report `1000 mB`, and fuel item-equivalent hover text divided by the original `200` ticks instead of the large canister’s `4000 mB` and `2000` tick minimum.
   - Fix:
     - overrode `appendHoverText(...)` in `LargePotionCanisterItem` using the live potion contents/amount and `getPotionCapacityMb()`
     - overrode `appendHoverText(...)` in `LargeFuelCanisterItem` preserving JDT’s shift/raw-fuel branch and using `getMinimumFuelConsumed()` for the item-equivalent branch
     - added `LargeCanisterTooltipTest` for `4000 mB`, `2000` ticks, and inherited-hover replacement guards
   - JDT bytecode evidence inspected before implementation:
     - `PotionCanister.appendHoverText(...)` uses static `getMaxMB() = 1000`
     - `FuelCanister.appendHoverText(...)` divides by literal `200.0f`

## Acceptance audit

This was a static/manual acceptance audit backed by source inspection, existing Task 1-4 tests, the serial Gradle run above, and JDT/Curios API inspection. No live `runClient` playthrough was performed in Task 5.

### 1. Capacities

Status: pass

Evidence:

- `LargePortableContainerLogic.pocketGeneratorCapacity(...)` and tests verify 4x pocket generator scaling with overflow saturation.
- `LargePortableContainerLogic.potionCapacity()` returns `4_000`.
- `LargeFuelCanisterItem.getMaxFuelLevel()` scales configured JDT fuel capacity by 4.
- `LargePortableContainerLogicTest` covers these values.

### 2. Potion 4x1000 behavior

Status: pass

Evidence:

- `LargePortableContainerLogic.POTION_BATCH_MB = 1_000`
- `LargePortableContainerLogic.canFillPotionBatch(...)`
- `LargePotionCanisterItem.tryFillBatch(...)`
- `LargePortableContainerLogicTest` verifies:
  - four matching potions add `1_000 mB`
  - three potions do not fill
  - mixed potion contents do not consume input
  - total capacity is `4_000 mB`

### 3. Fuel 40M and x10 burn/minimum rules

Status: pass after fix

Evidence:

- `LargeFuelCanisterItem` scales:
  - maximum fuel to `40,000,000`
  - minimum fuel consumed by x10
  - burn multiplier by x10
- `LargePortableContainerLogicTest` verifies x10 scaling and saturation behavior.
- `GeneratorT1UpgradeMixin` now redirects JDT generator burn-multiplier lookup for large fuel canisters.
- `PocketGeneratorFuelMultiplierMixin` now redirects JDT pocket-generator burn initialization to store the resolved large-canister multiplier into `POCKETGEN_FUELMULT`.
- `PortableFuelBurnSpeedHelper` is now the shared resolver used by:
  - generator-upgrade runtime
  - base generator `doBurn()` runtime
  - inherited pocket-generator burn initialization
  - large pocket generator fuel tooltip
  - normal JDT generator tooltip
- Decompiled `GeneratorT1BE` confirmed the base JDT generator path calls `FuelCanister.getBurnSpeedMultiplier(ItemStack)`, which was the real missing integration point.
- Decompiled `PocketGenerator.initBurn(...)` confirmed the inherited pocket-generator path also hardcoded `FuelCanister.getBurnSpeedMultiplier(ItemStack)`, which was the true runtime cause of the follow-up review failure.

### 4. Simultaneous Curios slots

Status: pass by static resource and test audit

Evidence:

- Separate Curios item tags exist for:
  - `large_pocket_generator`
  - `large_potion_canister`
  - `large_fuel_canister`
- Separate Curios slot definitions exist under `data/curios/curios/slots/...`
- Separate player slot bindings exist under `data/jdte/curios/entities/...`
- `LargePortableContainerRecipeTest.curiosSlotAndEntityResourcesExposeThreeIndependentSingleSlots()` verifies the three slots remain distinct.
- `BigFluidTankCuriosIntegration` ensures existing players receive all four JDTE Curios slots on login.

### 5. Hotkeys, menu invalidation, no item loss

Status: pass by static test audit

Evidence:

- `LargePortableContainerClientEvents.collectOpenPayloads(...)` only emits payloads for pressed keys and suppresses requests when there is no player or another screen is open.
- `LargePortableContainerFlowTest` covers:
  - correct hotkey payload collection
  - exact live Curios slot resolution
  - main-hand precedence
  - safe behavior when Curios is unavailable
- `LargePortableContainerBinding.isStillValid()` requires the exact same live `ItemStack` instance.
- `LargePortableContainerLogicTest.largePortableContainerBindingRequiresTheSameResolvedItemStackInstance()` verifies menu invalidation when the bound item is replaced.
- Container classes call `binding.isStillValid()`, so removing/replacing the bound Curio or handheld item invalidates the menu instead of continuing against stale state.

### 6. Generator charging from Curios

Status: pass after fix, by static integration audit

Evidence:

- Curios behavior is now registered for `jdte:large_pocket_generator` in `BigFluidTankCuriosIntegration.registerCurioBehaviors()`.
- That Curios callback delegates to `LargePocketGeneratorItem.inventoryTick(...)`.
- Decompiled JDT `PocketGenerator` confirmed that charging both inventory items and Curios-equipped chargeable items happens in `inventoryTick(...)`.
- Therefore the large pocket generator now reuses JDT’s existing charging path while equipped in its Curios slot.

### 7. Recipes / models / lang

Status: pass with dedicated pages

Evidence:

- `LargePortableContainerRecipeTest` verifies:
  - recipe ids and serializer wiring
  - Curios item tags
  - Curios slot resources
  - model override resources
  - English and Chinese names plus Curios slot labels
- dedicated GuideME pages now exist for each large portable container in both languages
- `validateDocs` no longer reports a branch-specific undocumented-item or category-mapping mismatch for the three large items; it only reports the pre-existing generated-book baseline drift
- `GeneratorFuelRuntimeHookTest` guards the presence of the base `doBurn()` redirect so the normal-generator runtime hook is not accidentally removed again during cleanup

## Git inspection after the live-screen and hover repair

Fresh `git status --short --untracked-files=all` after the live-screen and hover repair showed only the expected scoped delta for this last commit:

- `M .superpowers/sdd/2026-08-23-large-portable-containers-plan/task-5-report.md`
- `M src/main/java/com/jdte/client/screens/LargeFuelCanisterScreen.java`
- `M src/main/java/com/jdte/client/screens/LargePotionCanisterScreen.java`
- `M src/main/java/com/jdte/common/containers/LargeFuelCanisterContainer.java`
- `M src/main/java/com/jdte/common/containers/LargePotionCanisterContainer.java`
- `M src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java`
- `M src/main/java/com/jdte/common/items/LargePotionCanisterItem.java`
- `?? src/test/java/com/jdte/client/screens/LargeCanisterScreenLiveRefreshTest.java`
- `?? src/test/java/com/jdte/common/items/LargeCanisterTooltipTest.java`

This matched the intended final scope:

- refresh both canister screens from the source metadata each frame
- replace inherited fixed-size canister hover text with large-container semantics
- add focused regression coverage for both repairs
- update the Task 5 report with the exact fresh verification evidence

## Remaining limitations

1. `./gradlew validateDocs` is still blocked by a pre-existing Patchouli validation mismatch unrelated to this feature branch:
   - validator expects generated `jdte_guide` resources
   - repository content is already on the migrated `justdirethingsbook` layout
   - after the follow-up docs work, the validator also expects generated entries for the three new dedicated large-container guide pages, which is consistent with the same baseline “generated jdte_guide output is not checked in” issue rather than a new feature-specific mismatch

2. Task 5 was completed as a static/manual acceptance audit.
   - No live in-game GUI playthrough was performed in this task.
   - Confidence is high because the relevant code paths, resource wiring, and regression tests were all inspected and the requested serial Gradle verification was rerun after the fixes.

## Conclusion

Task 5 and its follow-up reviews found eight real feature-scope runtime/UI integration defects plus one documentation-coverage gap. All were corrected in the feature worktree. The latest serial verification ends in:

- `test`: pass
- `compileJava`: pass
- `validateDocs`: fail only for the known baseline Patchouli validator mismatch

The large portable container feature is otherwise integration-complete based on the Task 5 audit evidence above.
