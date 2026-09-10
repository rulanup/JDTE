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
  - jdte:ae_crafting_read_upgrade
  - jdte:ae_output_upgrade
  - jdte:ae_extraction_upgrade
  - jdte:essence_conversion_upgrade
  - jdte:seed_conversion_upgrade
  - jdte:looting_upgrade
  - jdte:sharpness_upgrade
  - jdte:energy_brewing_upgrade
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

仅限初级、高级和扩展高级时间加速器，每台机器最多安装 1 张。主模组 `jdte` 中 AE2 仍是可选依赖；未安装 JDTE-AE 附属时，此卡通过 `IGridTickable` 保留逐设备加速回退。

完整 Grid 加速要求客户端和服务端同时安装版本相互匹配的 JDTE-AE（`jdte-ae`）与 JDTE，以及 AE2 19.2.17+。范围内命中任一在线且已启动的节点即可加速其整个 Grid，同一 Grid 的多个命中节点不会重复计算同一台加速器。标称 `X` 倍包含原生 tick，因此单台贡献 `X - 1`，重叠总倍率为 `1 + Σ(Xᵢ - 1)`；1024x 每个真实 tick 执行 1023 个额外完整 Grid 生命周期。Grid 工作绕过普通 4096 次执行预算，可能显著提高 MSPT；资源不足时整批不执行且不扣费。

<RecipeFor id="jdte:ae_acceleration_upgrade" />

## AE 合成读取升级

<ItemImage id="jdte:ae_crafting_read_upgrade" scale="2" />

将升级卡放入 AE2 无线访问点界面的 linking 输入槽，再从输出槽取回已绑定的卡。把它安装到兼容机器的标准或扩展升级槽中，机器会读取绑定 AE2 网络的活动合成任务并自动运行。未绑定、无线访问点未加载或离线、网络正在启动，或网络没有进行中的合成任务时，机器会暂停；网络恢复并出现合成任务后会自动继续。每台机器最多安装 1 张。AE2 为可选依赖，未安装 AE2 时该升级不会启用。

## AE 输出升级

先将升级卡放入 AE2 无线访问点界面的绑定输入槽，并从输出槽取回已绑定的卡。随后把它安装到任意 JDT/JDTE 机器中，产物会按固定节奏（默认每 5 刻，`jdte.aeOutput.returnInterval` 可调）直接回传到绑定的 AE 网络，走真实 ME 存储写入——终端、总线和合成供应器都能即时看到回流物品。无线访问点必须已加载、在线且有频道；网络容量不足或离线时，未能写入的产物会安全保留在机器中。

每台机器最多安装 1 张；没有产物槽的机器装卡后不会回流。安装此卡的机器会停用常规自动输出，产物只走 AE。温室矩阵控制器（位于独立的 JDTE-Matrix 模组中）也支持此升级，并会回传所有受管理温室的产物。

<RecipeFor id="jdte:ae_output_upgrade" />

## AE 提取升级

<ItemImage id="jdte:ae_extraction_upgrade" scale="2" />

将无线接收器、输出总线和容量升级合成为此升级。先把它放入 AE2 无线访问点的输入槽进行绑定，再把绑定后的升级放入 JDT 或 JDTE 的锻造升级槽。绑定的升级会在玩家身上收集启用的 FE 与流体物品，并从对应 Applied Flux/AE2 网络补充资源；无线访问点必须保持加载、在线并连接到有可用存储的网络。

流体选择会优先保留储罐中已有的流体。空的通用储罐只有在候选流体恰好唯一时才会选择，不会在无法确定流体时误填。Applied Flux 是可选兼容；未安装时 FE 补充会安全跳过。

空通用容器不会被选作流体目标，只有已装流体或能明确接受唯一候选流体的专用储罐才会参与。锻造时会保留目标物品原有的能量、流体和其他组件。

<RecipeFor id="jdte:ae_extraction_upgrade_apply" />

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

## 能量酿造升级

<ItemImage id="jdte:energy_brewing_upgrade" scale="2" />

高级炼药机专用，最多安装 1 张。安装后酿造燃料改由 FE 支付：每次消耗 `energyPerBlazePowder` 配置的 FE（默认 5,000）获得相当于一个烈焰粉的 20 次酿造充能，烈焰粉槽随之停用，无需再外接烈焰粉。创造升级会豁免该费用。

<RecipeFor id="jdte:energy_brewing_upgrade" />
