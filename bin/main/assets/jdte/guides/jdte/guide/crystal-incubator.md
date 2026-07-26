---
navigation:
  title: 水晶培育机
  icon: "jdte:crystal_incubator"
  position: 18
item_ids:
  - jdte:crystal_incubator
---

# 水晶培育机

<BlockImage id="jdte:crystal_incubator" scale="2" />

水晶培育机识别配置范围内的母岩，消耗时间流体和 FE 催生晶体，并将成熟晶簇自动回收到内部 9 个输出槽。

## 运行参数

| 项目 | 行为 |
|------|------|
| 倍率 | 可调 1-512x；超频或创造升级固定为 1024x |
| 基础资源 | 按 JDT 时间手杖等效成本消耗时间流体和 FE，倍率可配置 |
| 输出 | 9 个产物槽；空间不足时不会破坏晶簇 |
| 自动 I/O | 允许从配置面输入时间流体，并向配置面输出产物 |
| 扫描 | 分批建立母岩缓存，只轮询母岩相邻六面 |

普通母岩采用 AE2 晶体催生器式随机刻催生：每个等效催生器每 10 Tick 调用一次母岩自身的 `randomTick()`。默认 8x 档位等效 6 个 AE2 晶体催生器，其他倍率按比例换算；调用会均匀累计并受每 Tick 操作预算限制，避免大范围高倍率产生集中卡顿。

没有时间流体或 FE 时不会催生；创造升级按通用规则免除培育机自身资源消耗。对于 Just Dyna Things Echoing Budding，培育机会预留自身当前 Tick 的运行成本，再将母岩缺少的一次标准 FE 和时间流体精确补入目标。母岩仍按自身配置决定成功生长后的实际扣除与概率，不会绕过激活成本。

## 采收升级

| 升级 | 上限 | 采收行为 |
|------|-----:|----------|
| 时运 | 8 | 向自动采收工具附加对应等级的原版时运 |
| 精准 | 1 | 向自动采收工具附加原版精准采集，由目标战利品表决定掉落 |

时运与精准不能同时安装。

## 兼容范围

- 支持 `c:budding_blocks` 和 `c:clusters` 通用标签，以及原版、JDT、AE2、Data Energistics、AE2 附属和 Just Dyna Things Echoing Budding。
- 整合包可向 `jdte:crystal_incubator_budding_blocks` 与 `jdte:crystal_incubator_harvestable_crystals` 标签添加自定义母岩和成熟晶簇。

机器使用分批范围扫描和母岩缓存，只检查已缓存母岩相邻的六个方块。输出空间不足时不会破坏晶簇。

## 合成

<RecipeFor id="jdte:crystal_incubator" />
