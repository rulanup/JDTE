---
navigation:
  title: 升级卡
  icon: jdte:capacity_upgrade
  position: 1
item_ids:
  - jdte:capacity_upgrade
  - jdte:overclock_upgrade
  - jdte:underclock_upgrade
  - jdte:fluid_upgrade
  - jdte:fluid_storage_upgrade
  - jdte:generator_upgrade
  - jdte:range_upgrade
---

# 升级卡

升级卡可以安装到 JDT 机器上，增强其功能。每台机器最多可安装 4 个升级槽（扩展机器 8 个）。

## 容量升级

<ItemImage id="jdte:capacity_upgrade" scale="2" />

使机器的 FE 容量和流体容量翻倍。最多可叠加 3 次。

**效果：**
- FE 容量 × 2^n（n 为升级数量）
- 流体容量 × 2^n

**合成：**
<RecipeFor id="jdte:capacity_upgrade" />

## 超频升级

<ItemImage id="jdte:overclock_upgrade" scale="2" />

强制机器以 1 tick 间隔运行，并且每 tick 执行两次操作。

**效果：**
- tick 间隔锁定为 1
- 每 tick 执行两次操作
- 耗电量变为 3 倍

**注意：** 不能与降频升级同时使用。

**合成：**
<RecipeFor id="jdte:overclock_upgrade" />

## 降频升级

<ItemImage id="jdte:underclock_upgrade" scale="2" />

强制机器以 40 tick 间隔运行，大幅降低能耗。

**效果：**
- tick 间隔锁定为 40
- 耗电量降低 80%

**注意：** 不能与超频升级同时使用。

**合成：**
<RecipeFor id="jdte:underclock_upgrade" />

## 流体升级

<ItemImage id="jdte:fluid_upgrade" scale="2" />

仅使机器的流体容量翻倍。最多可叠加 3 次。

**合成：**
<RecipeFor id="jdte:fluid_upgrade" />

## 流体存储升级

<ItemImage id="jdte:fluid_storage_upgrade" scale="2" />

为 Clicker 添加内部流体储罐，自动给槽位内可填充流体的物品填充。

**效果：**
- 添加 8000 mB 内部流体储罐
- 自动填充槽位内的流体容器

**仅限：** Clicker T1/T2

**合成：**
<RecipeFor id="jdte:fluid_storage_upgrade" />

## 发电机升级

<ItemImage id="jdte:generator_upgrade" scale="2" />

消耗双倍燃料，产出三倍电量。

**效果：**
- 燃料消耗 × 2
- 发电量 × 3

**仅限：** 发电机 T1

**合成：**
<RecipeFor id="jdte:generator_upgrade" />

## 范围升级

<ItemImage id="jdte:range_upgrade" scale="2" />

使机器的可配置区域上限翻倍。最多可叠加 2 次。

**效果：**
- 区域半径上限 × 2^n
- 区域偏移上限 × 2^n

**仅限：** 区域机器（如 Clicker T2、传感器 T2 等）

**合成：**
<RecipeFor id="jdte:range_upgrade" />
