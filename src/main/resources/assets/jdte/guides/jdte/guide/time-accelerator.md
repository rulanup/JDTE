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

## 调度与倍率

三个等级共用统一调度器。标称倍率包含目标自身的原生 tick：一台标称 `X` 倍的加速器贡献 `X - 1` 个额外周期，多台重叠后的总倍率为 `1 + Σ(Xᵢ - 1)`。例如，两台 16x 加速器共同覆盖同一目标时得到 31x，而不是 32x。

普通方块实体和随机刻目标按区块发现，并使用已付费虚拟 tick 积压队列以及每 tick 固定的执行与扫描预算。服务器 MSPT 过高时不会停止加速，超出预算的普通工作会在贡献加速器保持启用时继续排队。FE 或时间流体不足时，本 tick 的整批请求会被拒绝，不执行也不扣费，不会静默降为较低倍率。

## AE2 Grid 加速

主模组 `jdte` 中 AE2 仍是可选依赖。未安装 JDTE-AE 附属时，AE 加速升级保留通过 `IGridTickable` 的逐设备回退，可继续兼容分子装配室等公开 Grid Tick 服务目标。

完整 Grid 加速要求客户端和服务端同时安装版本相互匹配的 JDTE-AE（`jdte-ae`）与 JDTE，以及 AE2 19.2.17+。加速范围命中任一在线且已启动的 AE 节点后，该节点所属的整个 Grid 都会获得完整倍率，范围外的合成 CPU、样板供应器、分子装配室以及遵循 AE 生命周期的附属设备会共同推进。同一 Grid 有多个节点落在范围内时，同一台加速器的贡献只计算一次。

完整 Grid 模式在每个额外周期同步执行 Server Start、Level Start、Level End 和 Server End。标称 1024x 因而会在每个真实 tick 执行 1023 个额外完整 Grid 生命周期。此工作不受普通目标默认 4096 次执行预算限制，可能显著提高服务器 MSPT；使用高倍率前应确认服务器有足够余量。

<RecipeFor id="jdte:extended_time_accelerator" />

## 时间流体催化剂

<ItemImage id="jdte:time_fluid_catalyst" scale="2" />

可直接触发源水到 JDT 时间流体的 FluidDrop 转换，也可放入流体稳定器的催化剂槽中使用。

<RecipeFor id="jdte:time_fluid_catalyst" />
