# AE acceleration and production runtime follow-up

Starting commit: `5498124`. Reference: JDTE 0.6.5.2 `ccf46472abaf3856fa9eebe0f5ebd25d8f28e859`. Version remains `0.5.8-forge-1.20.1`.

## Implementation and server suite

AE Acceleration is installed in all three existing Time Accelerators, using their current fluid-cost and bounded scheduler settings. Native AE service discovery requires each contributor's own card; pending AE work loses removed-card contributions. Optional whole-grid backends use the source registration/frame contract, with active/inactive routing and no duplicate native fallback. No actual JDTE-AE backend mod is claimed as tested.

The current full build passed **95 JUnit tests and 112 required GameTests**, preserving all earlier 81/108. New tests include ten original backend-registration tests, four frame-order/exception-cleanup unit tests and four real AE2 integration GameTests. The latter use a powered real Inscriber: AE's native one-tick request advances two processing units, so twelve requests advance 24 rather than twelve. Initial fixture expectations were corrected accordingly. The first attempt to unit-test the registry-backed scheduler directly failed during Minecraft tag bootstrap; the exact frame logic was isolated into a common helper and all four tests now run successfully without world bootstrap. No exception assertions were removed.

`suite.txt` retains selected final output. Production and separately packaged probe reobfuscation passed. Graphical harnesses compile and stay outside the production JAR.

## Active real crafting job, save and fresh JVM

`active-job-restart-first.txt` identifies the first production JAR and actual two-JVM results. A real Pattern Provider feeds the real Extended Fluid Mixer while a real AE CPU waits for 500 mB Time Fluid. Before the first batch completes, redstone pauses the mixer at partial progress while the actual CPU remains busy. The server saves, verifies the saved live job, then shuts down normally.

A second JVM compares exact mixer fields before grid boot, waits for the actual nonempty busy CPU to return, verifies the restored read binding and resumes native machine work. Advanced AE Output returns exactly 500 mB to ME, catalysts/water are consumed exactly once, total FE cost is exactly 10,000, and the completed job pauses the machine again. Both JVMs exit 0. These are real persisted CPU jobs, rather than a recreated plan or fake busy snapshot.

## Pending at this checkpoint

Two independent mixer clients and an actual Mekanism Extras runtime probe are compiled but have not run yet. The publicly available Forge 1.20.1 Extras 1.5.0 recipe set contains fluorinated naquadah uranium fuel; no silicon fuel recipe was found in that JAR's datapack resources. Final runtime evidence will distinguish actual tested recipes from unavailable version-specific content.
