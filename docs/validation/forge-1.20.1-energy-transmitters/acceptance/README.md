# Transmitter remaining acceptance

## Completed

- Full **131 JUnit / 164 required GameTests**, build and main/probe reobfuscation passed. All prior tests remain enabled.
- Actual graphical Forge client (1280x720, GUI scale 2, Xorg/llvmpipe): both registered menus, 1.9B FE synchronization, filter allowlist/NBT packets, ghost template conservation, all eight upgrade slots, real card installation/removal, particles/player binding and actual Jade online HUD passed.
- Actual online operator hotbar, Eclipse boots, offhand and validated Curios slot each had a 1,000 FE gap; all four filled while the source paid exactly 4,000.
- The client really disconnected, unloading the integrated world and stopping its server, then reopened the saved world. The old capability was inert, the old operator resolved offline, UUID binding and actual Curios inventory restored, and a further 1,000 FE Curios refill cost exactly 1,000 FE. This is not a dedicated server remaining online during logout.
- Curios uses a test-only datapack slot with a tag validator accepting the actual JDT FE item. This exercises real Curios handlers, valid insertion and serialization, without inventing a mock FE capability or changing production accessory registrations.
- Real registered Draconic injector demand/receive fields pass the 6,500 + 3,500 = 10,000 conservation test. A complete fusion crafting recipe is not claimed.
- Extended Ultimate Capacity/Overclock installation/removal changes the physical buffer and actual native ticker: ten 1,000 FE executions versus one after removal. All six faces, held rotated ports and absent unsided FE pass.
- Native/JDTE mixed-network production checks and prior real unload/wrench probes retain their separate logs in the parent directory.

## Final acceptance

Both Advanced and Extended are V for the stated source contracts. [Dedicated Advanced evidence](dedicated/README.md) now also verifies a real independent client leaving a still-running dedicated server, 100 offline ticks, actual source chunk unload/reload, same-UUID reconnect and exact Curios refill, followed by real installed AE2/Applied Flux disk/node recovery across two production JVMs. Earlier limits below are historical; prolonged multiplayer, complete fusion/reactor operation and the separately installed JDTE-AE whole-grid backend remain unverified.

## Fixture failures retained as failures

Initial attempts used the ordinary inventory-click path for a ghost slot, an unforwarded Xorg cursor callback and a stale ghost hover for subsequent button dispatch. The final harness uses the actual native ghost-screen path and actual rendered button callbacks, verifies rendered hover and full carried-stack conservation, and propagates asynchronous server assertions into client failure. Native logout also requires `ClientLevel.disconnect()` before `Minecraft.clearLevel()`; omitting the first step waits forever for a server that was never told to stop. Interrupted attempts are not passes. The complete successful log, marker and six screenshots are retained here.

No test-only slot JSON, client smoke classes or production probe classes were found in the final production mod JAR. Version remains 0.5.8.
