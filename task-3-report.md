# Task 3 report: large fuel canister model predicate

## Root cause

The client registered the large fuel canister fullness property as `jdte:fuel_fullness`, while the item model override used a custom predicate of the same name. JDT's fuel-canister model/property convention is `justdirethings:fullness`; the custom predicate therefore did not participate in the normal item model property lookup and filled canisters rendered with the missing/air model.

## Fix

- Registered `LargeFuelCanisterItem.getFullness` under `justdirethings:fullness`.
- Updated all four filled model overrides to the same predicate.
- Updated the model resource contract test so the empty and four filled states remain covered.

## Verification

`./gradlew.bat test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest --tests com.jdte.common.items.LargePortableContainerLogicTest --no-daemon` — `BUILD SUCCESSFUL in 49s`.
