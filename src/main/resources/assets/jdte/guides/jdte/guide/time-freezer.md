---
navigation:
  title: Time Freezer
  icon: "jdte:time_freezer"
  position: 2.5
item_ids:
  - jdte:time_freezer
  - jdte:extended_time_freezer
---

# Time Freezer

The existing Forge Time Freezer can hold its dimension's time and weather. Its screen has independent time/weather switches and redstone control. Both switches start enabled.

Supply FE and JDT Time Fluid. The current Forge defaults are 100 FE and 100 mB per active tick, with a 200,000 FE buffer and an 8,000 mB tank. These values are configurable. Running out of either resource stops the freeze; a Creative Upgrade waives the costs.

Capacity and Fluid upgrades enlarge the corresponding storage. The basic machine has four standard upgrade slots; an Extended Upgrade converts it to the eight-slot Extended Time Freezer.

Use the Eclipse Alloy Wrench to pick up the machine while preserving its saved data. Operators with permission level 4 can list loaded freezers with:

```
/jdte timefreezer list
```

This page documents the existing Forge implementation. It does not claim new Time Freezer gameplay validation in the mineral-machine checkpoint.
