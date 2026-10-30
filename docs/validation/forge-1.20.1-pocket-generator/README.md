# Large Pocket Generator validation

Pinned source ccf46472; Forge47.4.10/JDT1.1.4/Java17/Gradle8.6/version0.5.8. Core checkpoint is **U**, not runtime acceptance.

180 JUnit/298 required GameTests/full build/main+probe reobfuscation and client harness compilation pass (`core5.log.gz`). Original176/284 and24 mineral tests retained. Four pure math tests plus14 actual Forge tests cover source generation/fuel/FE, output-only capability/shared NBT, exact coal budget, full/partial buffer, prefetch credit, invalid/short/remainder fuels/both canisters/native consumer, menu/automation sharing, disabled/duplicate inventory ticks, receiver partial-accept refund, off-thread/malformed data, native transmitter and preserving recipe/wire.

Early failures retained: incorrect native item field, missing GameTest prefix annotation, mock player crafting hook requiring a real ServerPlayer (fixture now uses an explicit inert crafting menu), and a real native force-receive bug. Forge EnergyStorage.receiveEnergy checks virtual canReceive=false, so native PocketGenerator could never generate through its own internal force path. A narrow internal PocketGenerator adapter repairs that path while external receive remains disabled; actual native/large fuel multiplier test now passes. Large item has a dynamic native-compatible storage implementation.

Real graphical natural-work/Curios/JEI and paired minimal packaged-server unload/restart harnesses are implemented/compiled, but not yet run at this checkpoint. No multiplayer or full acceptance claim.

## Neo parent recipient parity and key handling checkpoint

Core `f8e7437` pushed. Further native Neo1.5.7 parent inspection confirms it also charges actual equipped Curios recipients and does not skip a currently used inventory item; Forge1.1.4 differs. Large now follows the pinned Neo behavior on both points. Optional Curios access remains isolated and cosmetic slots are excluded. A real Curios handler test, maximum configured FE/capacity runtime test and throwing-receiver refund test raise the complete suite to **180 JUnit/301 required GameTests**, all passing with full build/main+probe reobfuscation/client compilation (`curios-regression.log.gz`).

First graphical run passes natural inventory charging/menu moves and equipped generation, then times out opening G. Key dispatch now directly matches the pressed configured key instead of depending on a shared-key click lookup; final rerun includes event diagnostics and unchanged real menu assertions. First minimal production pair passes both JVMs before these recipient/key changes; final matching-artifact pair and actual graphical rerun are running. U retained.

## Actual key packet and native short-credit correction

Attempt2 identifies actual CuriosScreenV2 overriding the G request; attempt3 has no screen because the new packet callback was copied with the potion target. Both failures are retained. Matched equipped-generator input now drains the conflicting Curios click; the corrected C2S callback opens the actual pocket slot. Actual attempt4 passes all graphical paths, native toggle and normal unequipped Curios shortcut, then real disconnect/save/new integrated-server/rejoin and exact whole item/inventory recovery plus natural resumed charging (423 ledger samples/126 worn).

A native zero-counter/high-speed fuel guard prevents the repaired internal force path from producing oversized unpaid FE. `native-guard-regression.log.gz`:180 JUnit/302 required GameTests/full build/main+probe reobfuscation/client compile pass. Final matching-JAR production/graphical replay pending; U retained until completion.
