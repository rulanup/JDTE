---
navigation:
  title: 时间加速器
  icon: "jdte:basic_time_accelerator"
  position: 2
item_ids:
  - jdte:basic_time_accelerator
  - jdte:advanced_time_accelerator
  - jdte:extended_time_accelerator
---

# 时间加速器

时间加速器可以加速区域内方块的运行速度。

## 初级时间加速器

<BlockImage id="jdte:basic_time_accelerator" scale="2" />

仅消耗时间流体的简单加速器。

- 默认加速倍率：16x
- 安装超频或创造升级后：32x
- 使用基础时间流体消耗倍率

<RecipeFor id="jdte:basic_time_accelerator" />

## 高级时间加速器

<BlockImage id="jdte:advanced_time_accelerator" scale="2" />

消耗时间流体和 FE 能量的高级加速器。

- 可调节倍率：1x - 64x
- 安装超频或创造升级后：128x
- 时间流体消耗为初级版的 2 倍

<RecipeFor id="jdte:advanced_time_accelerator" />

## 扩展高级时间加速器

<BlockImage id="jdte:extended_time_accelerator" scale="2" />

高级时间加速器的扩展版本，拥有 8 个升级槽。可通过扩展升级右键高级时间加速器获得。

- 可调节倍率：1x - 512x
- 安装超频或创造升级后：1024x
- 时间流体消耗为初级版的 5 倍
- 支持 8 个升级槽，可安装更多升级卡

三个等级共用统一调度器。多个时间加速器覆盖同一目标时倍率会完整叠加，范围内已加载方块实体按区块统一发现，当前服务器 tick 未完成的已付费虚拟 tick 会在贡献加速器保持启用时继续执行。调度器使用可配置的 MSPT 余量，并可加速通过标准 `IGridTickable` 服务运行的 AE2 设备。

<RecipeFor id="jdte:extended_time_accelerator" />

## 时间流体催化剂

<ItemImage id="jdte:time_fluid_catalyst" scale="2" />

可直接触发源水到 JDT 时间流体的 FluidDrop 转换，也可放入流体稳定器的催化剂槽中使用。

<RecipeFor id="jdte:time_fluid_catalyst" />
