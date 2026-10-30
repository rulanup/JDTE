# Large Pocket Generator validation

Pinned source ccf46472; Forge47.4.10/JDT1.1.4/Java17/Gradle8.6/version0.5.8. Core checkpoint is **U**, not runtime acceptance.

180 JUnit/298 required GameTests/full build/main+probe reobfuscation and client harness compilation pass (`core5.log.gz`). Original176/284 and24 mineral tests retained. Four pure math tests plus14 actual Forge tests cover source generation/fuel/FE, output-only capability/shared NBT, exact coal budget, full/partial buffer, prefetch credit, invalid/short/remainder fuels/both canisters/native consumer, menu/automation sharing, disabled/duplicate inventory ticks, receiver partial-accept refund, off-thread/malformed data, native transmitter and preserving recipe/wire.

Early failures retained: incorrect native item field, missing GameTest prefix annotation, mock player crafting hook requiring a real ServerPlayer (fixture now uses an explicit inert crafting menu), and a real native force-receive bug. Forge EnergyStorage.receiveEnergy checks virtual canReceive=false, so native PocketGenerator could never generate through its own internal force path. A narrow internal PocketGenerator adapter repairs that path while external receive remains disabled; actual native/large fuel multiplier test now passes. Large item has a dynamic native-compatible storage implementation.

Real graphical natural-work/Curios/JEI and paired minimal packaged-server unload/restart harnesses are implemented/compiled, but not yet run at this checkpoint. No multiplayer or full acceptance claim.
