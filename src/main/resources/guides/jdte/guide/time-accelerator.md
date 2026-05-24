---
navigation:
  title: 时间加速器
  icon: jdte:basic_time_accelerator
  position: 2
item_ids:
  - jdte:basic_time_accelerator
  - jdte:advanced_time_accelerator
---

# 时间加速器

时间加速器可以加速区域内方块的运行速度，使用时间流体作为能源。

## 初级时间加速器

<BlockImage id="jdte:basic_time_accelerator" scale="2" />

仅消耗时间流体的简单加速器。

**属性：**
- 默认加速倍率：4x
- 安装超频升级后：16x
- 流体容量：1000 mB

**使用方法：**
1. 放置时间加速器
2. 用装有时间流体的桶右键填充
3. 在 GUI 中调整区域范围
4. 区域内的方块将被加速

**合成：**
<RecipeFor id="jdte:basic_time_accelerator" />

## 高级时间加速器

<BlockImage id="jdte:advanced_time_accelerator" scale="2" />

消耗时间流体和 FE 能量的高级加速器，支持更高的加速倍率。

**属性：**
- 可调节倍率：1x - 128x
- 安装超频升级后：256x
- 流体容量：1000 mB
- FE 容量：200,000 FE

**使用方法：**
1. 放置高级时间加速器
2. 用装有时间流体的桶右键填充
3. 连接 FE 能源
4. 在 GUI 中调整区域范围和加速倍率
5. 区域内的方块将被加速

**合成：**
<RecipeFor id="jdte:advanced_time_accelerator" />

## 升级支持

时间加速器支持以下升级：

-   <ItemLink id="jdte:overclock_upgrade" /> - 提高加速倍率
-   <ItemLink id="jdte:capacity_upgrade" /> - 增加流体/FE 容量
-   <ItemLink id="jdte:fluid_upgrade" /> - 增加流体容量

## 注意事项

- 时间加速器不能加速其他时间加速器
- 加速效果会消耗时间流体
- 高级时间加速器还会消耗 FE 能量
- 被加速的方块会显示粒子效果
