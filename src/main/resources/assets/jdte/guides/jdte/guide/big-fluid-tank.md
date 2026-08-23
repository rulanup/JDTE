---
navigation:
  title: 大型流体储罐
  icon: "jdte:big_fluid_tank"
  position: 6.6
item_ids:
  - jdte:big_fluid_tank
  - jdte:large_pocket_generator
  - jdte:large_potion_canister
  - jdte:large_fuel_canister
---

# 大型流体储罐

<ItemImage id="jdte:big_fluid_tank" scale="2" />

大型流体储罐是 JDT 流体罐的 1000 B 版本。它只保存一种流体，最大容量为 **1,000,000 mB**，可以手持使用，也可以放入 Curios 的专用 `big_fluid_tank` 槽位。

## 世界交互

- 对着流体源普通右键：优先拾取 1000 mB；不能拾取时尝试放置储罐中的流体。
- 对着方块潜行右键：直接尝试放置 1000 mB。
- 对着空气潜行右键：切换自动填充模式。

储罐只能混装同一种流体，并遵守目标方块、流体容器与维度的原版放置规则。

## 自动填充模式

储罐位于玩家物品栏或 Curios 槽位时，可以把内部流体自动送入其他兼容的流体物品，每次检查对单个物品最多传输 100 mB。

- **None**：不自动填充任何物品。
- **JDT**：只填充 Just Dire Things 的物品。
- **JDTE**：只填充 JDT Extras 的物品。
- **All**：填充所有具有兼容流体能力的物品。

## 相关便携容器

- `jdte:large_pocket_generator`：4 倍容量的便携发电机，可放入独立 Curios 槽位并通过快捷键打开界面。
- `jdte:large_potion_canister`：4,000 mB 的大型药水罐，每批消耗 4 瓶相同药水并增加 1,000 mB。
- `jdte:large_fuel_canister`：40,000,000 燃料上限的大型燃料罐，最小消耗与燃烧倍率均按原版规则放大 10 倍。

## 合成

<RecipeFor id="jdte:big_fluid_tank" />
