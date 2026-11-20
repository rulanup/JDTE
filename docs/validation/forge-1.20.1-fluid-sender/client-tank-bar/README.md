Client tank-bar confirmation (2026-11-20)

Question carried by the B1 fluid-sender round: on the powered tiers the tank bar
looked empty, because JDT sizes it with integer arithmetic
((amount * 70) / maxMB). With Ultimate Capacity the tank is 400,000 mB, so the
4,000 mB fixture fill computes to 0 pixels.

Root cause (verified in source, not inferred):
- JDT BaseMachineScreen renders the fluid bar itself; the port MachineBarMath
  minimum-one-pixel rule only reached the screens that draw their own bar
  (Mineral Extractor, Fluid Mixer) and the Clicker hook. The JDTE fluid sender
  and receiver screens inherit JDT renderBg, so the rule never applied.
- Fix: BaseMachineScreenMixin now paints the missing bottom row from the same
  renderBg tail hook, scoped to FluidSenderContainerBase and
  FluidReceiverContainerBase, and only when JDT produced zero pixels while the
  tank is not empty.

Run: Forge 1.20.1 Headless Client, run 44673884793, harness jdteFluidSenderSmoke
(real Forge 1.20.1 client under Xvfb with Mesa). Screenshot 1280x720, guiScale 2,
so one logical GUI pixel is two screenshot pixels.

Fixture and measurement (screenshot column x=826..851, bar interior x=826..845):
- fluid-sender-advanced-gui (Ultimate Capacity, tank 400,000 mB, fill 4,000 mB):
  fluid rows are y=292 and y=293 -> exactly 1 logical pixel, sitting directly
  above the bar frame at y=294..295. Before the fix these rows had no fluid.
- fluid-sender-extended-gui: identical, y=292..293 -> 1 logical pixel.
- fluid-sender-basic-gui (tank 8,000 mB, fill 4,000 mB): fluid spans y=224..293,
  i.e. about half the 70-pixel interior, which is the expected normal bar and
  shows the sliver rule did not disturb ordinary fills.

Files:
- advanced-tank-bar-8x.png / extended-tank-bar-8x.png: the bar column cropped and
  magnified 8x; the single blue row is visible at the bottom of the tank.
- basic-tank-bar-8x.png: the same crop for the half-full Basic tank.
- advanced-full-gui.png / extended-full-gui.png / basic-full-gui.png: the full
  1280x720 screenshots the crops came from.

Scope: this confirms the render arithmetic and the shipped pixels for the two
powered sender tiers with the Ultimate Capacity fixture. It does not re-run the
dedicated-server lifecycle round (that evidence is unchanged from B1).