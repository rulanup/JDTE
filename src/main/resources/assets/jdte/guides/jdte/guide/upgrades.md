---
navigation:
  title: 升级卡
  icon: "jdte:capacity_upgrade"
  position: 1
item_ids:
  - jdte:capacity_upgrade
  - jdte:overclock_upgrade
  - jdte:underclock_upgrade
  - jdte:fluid_upgrade
  - jdte:fluid_storage_upgrade
  - jdte:generator_upgrade
  - jdte:range_upgrade
  - jdte:filter_upgrade
  - jdte:creative_upgrade
  - jdte:fortune_upgrade
  - jdte:precision_upgrade
  - jdte:ae_acceleration_upgrade
  - jdte:ae_output_upgrade
  - jdte:essence_conversion_upgrade
  - jdte:seed_conversion_upgrade
  - jdte:looting_upgrade
  - jdte:sharpness_upgrade
---

# 升级卡

手持升级卡蹲下右键 JDT 或 JDTE 机器，可将手中的同类升级卡连续插入可用升级槽，直到达到类型上限、槽位用完或手中卡耗尽。抢夺和锋利升级会写入支持机器的专用升级槽。机器兼容性、数量上限和超频/降频互斥规则仍会正常检查。安装 FTB Ultimine 时，按住连锁键蹲下右键可依次为当前选区中的机器尽量插满该升级卡。

升级卡可以安装到 JDT 机器上，增强其功能。

## 容量升级

<ItemImage id="jdte:capacity_upgrade" scale="2" />

使机器的 FE 容量和流体容量翻倍。最多可叠加 3 次。

<RecipeFor id="jdte:capacity_upgrade" />

## 超频升级

<ItemImage id="jdte:overclock_upgrade" scale="2" />

强制机器以 1 tick 间隔运行，并且每 tick 执行两次操作。耗电量变为 3 倍。

<RecipeFor id="jdte:overclock_upgrade" />

## 降频升级

<ItemImage id="jdte:underclock_upgrade" scale="2" />

强制机器以 40 tick 间隔运行，耗电降低 80%。

<RecipeFor id="jdte:underclock_upgrade" />

## 流体升级

<ItemImage id="jdte:fluid_upgrade" scale="2" />

仅使机器的流体容量翻倍。最多可叠加 3 次。

<RecipeFor id="jdte:fluid_upgrade" />

## 流体存储升级

<ItemImage id="jdte:fluid_storage_upgrade" scale="2" />

为 Clicker 添加内部流体储罐。

<RecipeFor id="jdte:fluid_storage_upgrade" />

## 发电机升级

<ItemImage id="jdte:generator_upgrade" scale="2" />

消耗双倍燃料，产出三倍电量。

<RecipeFor id="jdte:generator_upgrade" />

## 范围升级

<ItemImage id="jdte:range_upgrade" scale="2" />

使机器的可配置区域上限翻倍。最多可叠加 2 次。

<RecipeFor id="jdte:range_upgrade" />

## 过滤升级

<ItemImage id="jdte:filter_upgrade" scale="2" />

为机器添加额外的过滤槽。每个升级增加一排（9个槽位），最多可叠加 2 次。

**仅限：** 有过滤槽的机器（如 Clicker T2、传感器 T2 等）

<RecipeFor id="jdte:filter_upgrade" />

## 创造升级

<ItemImage id="jdte:creative_upgrade" scale="2" />

免除 FE 消耗；时间加速器免除时间流体消耗；包含超频效果。

<RecipeFor id="jdte:creative_upgrade" />

## 时运升级

<ItemImage id="jdte:fortune_upgrade" scale="2" />

凝胶发生器和水晶培育机专用。凝胶发生器按每级原版时运提高支持产物的数量；水晶培育机把对应等级的时运附加到自动采收工具。水晶培育机最多安装 8 个，且不能与精准升级同时安装。

<RecipeFor id="jdte:fortune_upgrade" />

## 精准升级

<ItemImage id="jdte:precision_upgrade" scale="2" />

水晶培育机专用。自动采收时向模拟工具附加原版精准采集，由目标方块自身的战利品表决定产物，适合兼容遵循原版精准采集规则的模组晶体。最多安装 1 个，且不能与时运升级同时安装。

<RecipeFor id="jdte:precision_upgrade" />

## AE 加速升级

<ItemImage id="jdte:ae_acceleration_upgrade" scale="2" />

仅限初级、高级和扩展高级时间加速器。允许安装此卡的时间加速器通过 AE2 公开的 `IGridTickable` 服务加速支持的 AE2 设备。每台机器最多安装 1 张；重叠范围内只有安装此卡的加速器会向 AE2 设备贡献倍率。

<RecipeFor id="jdte:ae_acceleration_upgrade" />

## AE 输出升级

先将升级卡放入 AE2 无线访问点界面的绑定输入槽，并从输出槽取回已绑定的卡。随后把它安装到具有物品或流体产物槽的机器中，产物会优先直接回传到绑定的 AE 网络。无线访问点必须已加载、在线且有频道；网络容量不足或离线时，未能写入的产物会安全保留在机器中。

点击器、放置器、投掷器、发送器等没有产物输出用途的机器不能安装此升级。每台机器最多安装 1 张。温室矩阵控制器也支持此升级，并会回传所有受管理温室的产物。

<RecipeFor id="jdte:ae_output_upgrade" />

## 精华转化升级

<ItemImage id="jdte:essence_conversion_upgrade" scale="2" />

仅限普通温室和大型温室。安装后，机器会查找每种收获精华参与的工作台合成配方：只有该精华恰好只有一个配方，并且配方的所有非空材料槽都只使用该精华时，才会按原配方的消耗与产出数量自动转化。数量不足一个配方的精华会暂时保持原样，并与后续收获继续合并；精华存在多个配方或配方还需要其他材料时不会转化。

<RecipeFor id="jdte:essence_conversion_upgrade" />

## 种子转化升级

<ItemImage id="jdte:seed_conversion_upgrade" scale="2" />

仅限普通温室和大型温室。收获神秘农业作物时，与当前种植模板相同的种子掉落不再直接输出，而是按数量 1:1 替换为该作物的精华。若同时安装精华转化升级，这些精华会继续参与唯一配方判定，并在满足条件时按配方比例转化为最终产物。其他副产物保持不变。

<RecipeFor id="jdte:seed_conversion_upgrade" />

## 抢夺升级

<ItemImage id="jdte:looting_upgrade" scale="2" />

生物粉碎机专用，增加额外掉落概率。最大等级 6，每级 50% 概率获得 +1 掉落。

<RecipeFor id="jdte:looting_upgrade" />

## 锋利升级

<ItemImage id="jdte:sharpness_upgrade" scale="2" />

生物粉碎机专用，增加攻击伤害。每个升级增加 5 点伤害，最多 6 个，最大 35 点伤害。

<RecipeFor id="jdte:sharpness_upgrade" />
