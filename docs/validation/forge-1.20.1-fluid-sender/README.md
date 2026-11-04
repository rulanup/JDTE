# Fluid Sender Validation (B1)

Branch: `muse/forge-1.20.1-fluid-sender-validation`, from
`muse/forge-1.20.1-ultimate-27-machines` (`856a12fa`). Mod version 0.5.8.
No main merge, no release. Per-tier promotion decision at the end of this
record: Basic P -> V (scoped), Advanced and Extended stay P.

## Scope

Per-tier validation (Basic / Advanced / Extended fluid senders) of the
Ultimate-card behaviour restored by the 27-family implementation round,
plus a regression check on the two already-verified (V) machines affected
by the generalization: the mineral extractor and the fluid mixer.

## V-machine regression (VRegressionUltimateGameTests, 7 tests)

- Mineral extractor + Ultimate Overclock: real block ticker runs
  10 executions (was 1 before the generalization). A single ticker tick
  settles identically to the no-card baseline (output 3, FE -15,000 from
  2,000,000), because executions 2-10 within one game tick only
  accumulate transient work. Over 100 real world ticks: Ultimate output
  400 vs baseline 320, FE 0 vs 400,000 remaining from 2,000,000 — the
  Ultimate machine produced more and hit the energy wall first.
- Fluid mixer + Ultimate Overclock: real ticker still runs 10 executions
  (mixer was already whitelisted). Correction to the validation audit:
  `OverclockDirectTransferHelper.isEnabled` gates on machine type
  (senders/receivers only), so the mixer does NOT gain AutoIO direct
  transfer; `transfer(mixer)` returns 0. The `hasOverclock` change affects
  only the tick-speed packet sync condition (value-neutral). Energy cost
  remains x50 (pre-existing, 700 -> 35,000). Work test: one ticker tick
  completes 2 batches (energy-bound at 100,000 FE), output 100 mB time
  fluid, lava 1000 -> 800, catalyst 10 -> 8, FE 100,000 -> 30,000.

## Fluid sender GameTests (FluidSenderValidationGameTests, 22 tests)

Full suite: 408 GameTests passed (379 + 7 V-regression + 22 fluid),
196 JUnit. All figures below are measured, not theoretical, except where
marked theoretical.

- Install/limits/capacity per tier: a second Ultimate Capacity or
  Ultimate Overclock is rejected; standard Overclock and Underclock are
  rejected while Ultimate Overclock is installed. Tank with Ultimate
  Capacity + 3 Capacity cards: 3,200,000 mB all tiers; FE maxima
  20,000,000 (Advanced) / 40,000,000 (Extended).
- Unlimited ON (default): whole tank moves in one ticker tick on all
  tiers (Basic/Advanced 8,000; Advanced deducts exactly one standard cost
  of 500; Extended with both Ultimates deducts 25,000).
- Unlimited OFF: base rate exactly 20,000 mB/tick with a 500 FE deduction;
  standard Overclock drains a 400,000 mB tank in one tick with a 1,500 FE
  deduction; Ultimate Overclock moves exactly 200,000 mB/tick (10 x
  20,000) on all three tiers with a 250,000 FE deduction on powered
  tiers. The original quirk stands: Ultimate is slower per tick than
  standard Overclock (200,000 vs 2,000,000 mB/tick theoretical).
- Real ticker count: 10 executions on all tiers (counting subclasses).
  Tick speed: Ultimate 1, Underclock 40 (nothing in 39 ticker ticks,
  whole tank on the 40th).
- Direct transfer: Ultimate direct path moves the whole 400,000 mB tank
  (fluidLimit 1,000,000, tank-limited) with one 25,000 FE deduction;
  without an AutoIO input mask it falls back to the normal path
  (200,000, -250,000); it pulls 8,000 mB from an adjacent source tank;
  a direct execution that moves nothing deducts nothing.
- Energy gating: zero FE blocks transfer with no deduction.
- Redstone with a real redstone block above the sender: HIGH blocks
  without a signal and allows with one; LOW is the reverse. PULSE:
  five-step rising-edge sequence (no signal 0; rising edge moves; held
  signal 0; falling edge 0; second rising edge moves).
- No-target backoff: 10 -> fail -> 20 -> success, no fluid lost.
- Area: radius 3 reaches a dx=2 target; Range upgrades double the max
  radius per card (x2, x4).
- Filter: installs but does not change transfer (parity with the
  original, same as item senders).
- Fluid type: lava preserved through transfer.
- Save/reload: tank fluid + amount (water 12,345), FE (1,000,000 /
  1,234,567), both cards, area, redstone mode and filter stone survive.
- Extended 8 slots: all 8 cards installed (Ultimate Capacity, Ultimate
  Overclock, 3 Capacity, 2 Range, Filter), tank 3,200,000, all survive
  save/reload.
- Survival break with both Ultimate cards, all 3 tiers: FakePlayer
  SURVIVAL + iron pickaxe, ItemEntity before/after delta == 1, air, no
  block entity.
- Sustained throughput (refill loop, 5 iterations): Ultimate 1,000,000
  mB (5 x 200,000); baseline 100,000 mB (5 x 20,000); standard Overclock
  2,000,000 mB (5 x 400,000, tank-limited). Per-tick theoretical
  ceilings that exceed one tank (200,000/tick Ultimate is reachable;
  2,000,000/tick standard Overclock is tank-limited, not execution-
  limited) are recorded as such.

## Real client (Xvfb + Mesa)

Run: `scripts/run_fluid_sender_client_smoke.sh` (property
`jdte.fluidSenderSmoke`; first attempt died to transient environment
pressure, same class as the item-sender round's exit 137; the retry
passed). Marker: `client/fluid-sender-client-smoke-success.txt`.
Screenshots are local evidence only and are NOT committed.

- Real Forge client opened the Basic, Advanced and Extended fluid sender
  GUIs (upgrade slots 4/4/8, programmatic count plus visible columns;
  Extended shows 4 + 4 in two columns).
- GUI shift-click installed both Ultimate cards on Basic (menu +
  server-side confirmation; Basic kept its 4,000 mB tank).
- Server-installed Ultimate cards show in the Advanced/Extended GUIs;
  powered tiers' FE bars read full.
- Baked models present for all three tiers; creative tab and JEI
  contain all three sender items and both Ultimate cards.
- Screenshots visually inspected: all three GUIs open with correct
  titles and Rad/Off area controls; Basic's tank bar shows about half
  blue water (4,000 / 8,000 mB). Advanced/Extended tank bars appear
  EMPTY in their screenshots despite the identical server-side 4,000 mB
  fill. This is recorded as a pending client display question, NOT as
  proof of tank data loss: the dedicated-server evidence independently
  confirms powered-tier tank contents survive unload/reload/restart
  (12,345 / 23,456 mB). Preliminary read-only localization (see
  "Limitations carried forward") points to JDT's bar-render arithmetic
  rather than a sync failure; it is not yet live-confirmed, and
  Advanced/Extended therefore stay P.

## Dedicated server

Server: packaged Forge 47.4.10, port 25582, world
`fluid-sender-lifecycle-world`. Probe: `FluidSenderLifecycleProbe`
(`-Djdte.fluidSenderLifecycle=seed|verify`). Markers:
`dedicated-server/fluid-sender-seed-success` and
`dedicated-server/fluid-sender-verify-success`.

Seed (fresh JVM, probe stages 0-4):

- Three tiers placed; Basic live transfer moved all 8,000 mB water to an
  adjacent target tank; saved; chunk unloaded and reloaded with
  Ultimate cards, FE, tanks and Extended 8-slot state intact (Advanced
  1,000,000/2,500,000 FE + 12,345 mB, Extended 1,234,567/40,000,000 FE +
  23,456 mB, filter stone intact).
- Extended survival break (FakePlayer SURVIVAL + iron pickaxe): loot
  dropped exactly one self item, block became air, block entity
  removed.
- ItemEntity-counting evidence for the seed Extended break: NOT
  OBTAINED. The probe's before/after ItemEntity delta was 0 for the
  sender AND 0 for a vanilla stone control broken the same way in the
  same chunk (probe-environment anomaly, same class as the Fumo round).
  The seed drop evidence is therefore loot-table drop plus
  destroy-to-air ONLY; it is NOT described anywhere in this record as a
  measured entity drop of exactly 1. This fallback standard was
  explicitly accepted by the user on 2026-11-05, with this annotation
  required. GameTest-server survival breaks counted delta == 1 for all
  three tiers.

Verify (fresh JVM, probe stages 10-13):

- Basic and Advanced restored with Ultimate cards, FE and tanks intact;
  Extended stayed broken (air) as seeded.
- Advanced survival break: loot exactly one self item AND ItemEntity
  before/after delta == 1, air, no block entity (log stage 11).
- Basic survival break: loot exactly one self item AND ItemEntity
  before/after delta == 1, air, no block entity (log stage 12).
- The verify phase therefore meets the strict delta standard for both
  restored tiers; the seed anomaly applies to the seed Extended break
  only.

## Limitations carried forward

- The direct-path 1,000,000 mB/execution cap is not saturated (largest
  measured single direct execution: 400,000, tank-limited).
- PULSE was exercised with a real redstone block edge, not with a
  redstone circuit pulse (repeater/piston edge).
- Seed-phase production ItemEntity counting: not obtained (see
  "Dedicated server" — seed anomaly confirmed by the stone control;
  seed drop evidence is loot + destroy-to-air only). Verify-phase
  deltas (Advanced 1, Basic 1) are measured and recorded as passed.
- Client tank-bar display on powered tiers (preliminary, read-only,
  NOT live-confirmed): JDT's `BaseMachineScreen` renders the fluid bar
  from the menu's `ContainerData` (`FluidMachineBE.getFluidContainerData`,
  same code path for all tiers — no JDTE container override) as
  `amount * 70 / maxMB` integer pixels. The smoke filled 4,000 mB on
  every tier. Basic: 4,000 * 70 / 8,000 = 35 px (visible; pixel scan of
  the Basic screenshot finds 384 blue pixels). Advanced/Extended with
  Ultimate Capacity: 4,000 * 70 / 400,000 = 0 px (pixel scans find 0
  blue pixels). Any fill below ~5,714 mB renders as zero pixels on a
  400,000 mB tank by this arithmetic alone, so the empty-looking bars
  are consistent with a render-scale fixture artifact, not with sync
  loss or tank data loss. This does NOT yet close the question: the
  next round must live-confirm it (fill a powered tier well above the
  ~5,714 mB threshold and re-screenshot) and must not change shared
  sync mechanisms on the basis of this preliminary finding. The
  Advanced/Extended promotion hold stands until then.

## Promotion decision

Per-tier, not bundled (user decision 2026-11-05):

- Basic fluid sender: P -> V (scoped). Client GUI, creative/JEI, real
  transfer (GameTest + dedicated-server live 8,000 mB) and all
  necessary tests pass, with no other unresolved functional gap: its
  tank bar visibly shows water (4,000 / 8,000 mB), and its production
  drop evidence is complete (verify-phase entity delta 1 plus
  GameTest-server delta 1). Scope note: the seed-phase ItemEntity
  anomaly applies to the seed Extended break only, not to Basic's
  verify break; the PULSE-circuit and direct-cap limitations above
  still apply.
- Advanced fluid sender: stays P. Server tank storage is proven
  correct, but the client tank-bar display question is not yet
  live-confirmed; it is listed as a pending client display/sync
  investigation, not dismissed as a harmless screenshot artifact.
- Extended fluid sender: stays P, same reason as Advanced. Its seed
  survival-break drop evidence is loot + destroy-to-air only (entity
  counting not obtained in the seed phase); its verify-phase break was
  not re-tested because it stayed broken by design.

Counts after this round: V 59 / U 19 / P 42 / N 0, version 0.5.8, no
main merge, no release.
