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

<BlockImage id="jdte:time_freezer" scale="2" />

The Time Freezer consumes Time Fluid to freeze the day/night cycle and weather of its dimension. It is the automation device server administrators use to take control of the world's clock.

## Features

- **Freeze time**: While active, the dimension's day time is pinned to the moment it was activated; the sun and moon stop moving.
- **Freeze weather**: When active in the Overworld, the current weather state (clear/rain/thunder) is pinned and can no longer change naturally.
- **Time Fluid cost**: Consumes 100 mB of Time Fluid per tick (configurable). Freezing stops automatically once the tank runs dry.
- **Redstone control**: Supports Ignored, Low, High, and Pulse modes; defaults to Ignored (always active).
- **Upgrade slots**: The Time Freezer has 4 standard upgrade slots; the Extended Time Freezer has 8. Capacity and Fluid upgrades enlarge the tank; the Creative upgrade waives the fluid cost.

## Usage

- Feed Time Fluid through pipes or buckets (only Time Fluid is accepted).
- Multiple Time Freezers can run at once and share a single freeze target, so they never fight each other; every dimension has its own independent frozen time.
- Breaking the machine keeps its fluid (block entity data).

## Admin Command

Server operators (permission level 4) can run:

```
/jdte timefreezer list
```

This lists every loaded Time Freezer on the server: dimension, coordinates, whether it is currently freezing, and its remaining Time Fluid. The coordinates are clickable to teleport, which makes forgotten machines easy to track down.

The Extended Time Freezer is crafted in place by right-clicking a Time Freezer with an Extended Upgrade, gaining 8 upgrade slots.
