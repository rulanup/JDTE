---
navigation:
  title: Eclipse Alloy Wrench
  icon: "jdte:eclipsealloy_wrench"
  position: 6
item_ids:
  - jdte:eclipsealloy_wrench
---

# Eclipse Alloy Wrench

The Eclipse Alloy Wrench is a multi-purpose tool for working with JDTE and JDT machines.

## Usage

### Rotate Machine

Right-click any JDTE or JDT machine to rotate it by 90 degrees.

### Pickup Machine

Sneak + right-click a JDTE or JDT machine to pick it up as a block item, preserving stored items, energy, fluids, upgrades, and configuration.

### Adjust Machine Area

Hold the Eclipse Alloy Wrench, hold the **Wrench Area Modifier** key (default Left Alt, configurable in key bindings), and scroll the mouse wheel while looking at a JDTE or JDT machine with an adjustable area to increase or decrease the X/Y/Z range radius at the same time.

When FTB Ultimine is installed, the scroll adjustment only applies to the active Ultimine selection while holding the Eclipse Alloy Wrench, holding the Wrench Area Modifier key, holding the Ultimine key, and having a valid selection. If the Ultimine key is not held, only the machine under the crosshair is adjusted.

### Select an Area

Left-click a block with the Eclipse Alloy Wrench to set the first corner, then left-click another block to set the second corner; each corner plays a selection sound. The selection uses JDT's area preview effect and continuously shows its X/Y/Z dimensions below the crosshair. Once both corners are set, the selection is locked and its corners cannot be changed. Left-click adjustable-area machines to copy the exact selection into each one. Selections exceeding a machine's current radius or offset limits are rejected. Applying the selection does not clear it, allowing reuse across multiple machines; only Shift-left-click clears it and enables a new selection.

While held in Creative mode, the Eclipse Alloy Wrench cannot break blocks, preventing corner targets from being removed instantly during selection.
