# Advanced transmitter dedicated acceptance

## Reproduce

Reuse the configured JDK/Gradle and installed runtime JARs; no toolchain installation is required. Build the test-only source sets, then run:

```bash
JAVA_HOME=/workspace/.jdks/jdk17 GRADLE_USER_HOME=/workspace/.gradle bash gradlew jar serverSmokeJar compileClientSmokeJava --no-daemon --max-workers=2
JAVA_HOME=/workspace/.jdks/jdk17 bash scripts/run_advanced_transmitter_acceptance.sh
```

The coordinator reuses the captured remote launch `/tmp/jdte-remote-launch.json`, existing libraries and installed mod JARs. It creates only marked disposable directories `/tmp/jdte-advanced-transmitter-server` and `/tmp/jdte-advanced-transmitter-control`, binds to loopback port 25579 and refuses unmarked reuse. A separate independent client connects to a production Forge server; no FakePlayer or mock receiver/network is used. The Curios slot is declared by the test-only probe/client datapack, validates a real JDT FE item and is excluded from the production mod.

## Actual results

1. Real player login creates four actual 1,000 FE gaps in hotbar, Eclipse boots, offhand and Curios. The transmitter spends exactly 4,000 FE to fill all four.
2. The actual client disconnects. The dedicated server remains running for 100 ticks with the bound UUID offline. An explicitly seeded 7,777 FE remains untouched; block outputs are filtered out to isolate player charging.
3. The source chunk really unloads after its force ticket is removed. A retained FE port rejects input and extraction. Reload restores 7,777 FE and the binding.
4. The same independent client reconnects using the same UUID. Saved actual Curios equipment is full; removing exactly 1,000 FE from it is refilled once, leaving source 6,777. The client then exits normally.
5. The source's remaining 6,777 FE is explicitly withdrawn. A real Applied Flux FE disk is seeded with 50,000 FE; an actual mixer buffer is prefilled with 60,000 of its 100,000 FE capacity. Enabling the source consumes only 40,000 FE from the disk and fills the receiver to 100,000, leaving 10,000 FE on disk. AP, drive and source identities are asserted to share the same powered/channelled grid.
6. The first production JVM saves and stops normally. The second production JVM restores the actual disk, source FE/long-credit/binding/capability fields and managed node. The operator remains offline. After an explicit 50,000 FE receiver withdrawal, the remaining disk 10,000 is delivered once, leaving target 60,000, disk/source/reserve zero. The conservation equation is 60,000 initial target + 50,000 seeded disk - 50,000 explicit withdrawal = 60,000 final.

Both production JVMs and the independent client exit 0. Full logs, markers, expected source NBT, actual saved player NBT and artifact hashes are retained. The final production JAR is byte-identical to the tested JAR; no production smoke classes or fixture slot JSON are packaged.

## Scope

This closes the dedicated logout/unload/rejoin and installed ME-grid JVM recovery gaps for Advanced. It does not certify prolonged multiplayer uptime, complete fusion recipes/reactors or the separately installed JDTE-AE whole-grid backend. The global full regression remains 131 JUnit / 164 required GameTests; only test-only source sets were added after that run. Final `test build`/reobfuscation passed again. Nonfatal upstream tag/auth/version-check and fresh flat-fixture generator warnings remain visible in the retained logs; they did not prevent startup or any assertion.
