# AE2 提取升级设计规格

日期：2026-08-30

## 目标

新增锻造消耗品 `jdte:ae_extraction_upgrade`。玩家先通过 AE2 无线访问点把升级绑定到一个 ME 网络，再在锻造台中把它应用到任意实际具有 FE 或物品流体 capability 的 JDT/JDTE 物品。增强后的物品由在线玩家携带或装备时，会从绑定网络自动补满其 FE 和适用流体。

AE2 原生提供流体存储；FE 存储与提取依赖可选模组 Applied Flux。未安装 Applied Flux 时，增强物品仍可补充流体，但不补充 FE。

## 升级物品与绑定

`AEExtractionUpgradeItem` 是独立的锻造材料，不加入机器升级系统的 `UpgradeType`，也不能安装到机器升级槽。它使用 AE2 公共 `GridLinkables` 协议注册为可链接物品：玩家把升级放入无线访问点的链接输入槽，访问点将目标维度和坐标写入 `AEComponents.WIRELESS_LINK_TARGET`；解绑时移除该组件。

升级卡 tooltip 显示“未绑定”或“已绑定”。新增中英文名称、说明、指南内容、物品模型和一张与现有升级卡风格一致、以向内箭头区分“提取”的纹理。

## 锻造配方

新增自定义 `SmithingRecipe` 与序列化器，配方属于原版 `RecipeType.SMITHING`，并通过数据包 JSON 注册：

- 模板槽必须是已经包含 `AEComponents.WIRELESS_LINK_TARGET` 的 `jdte:ae_extraction_upgrade`。
- 基础槽物品的注册命名空间必须是 `justdirethings` 或 `jdte`。
- 基础槽物品必须在当前环境中实际暴露 `Capabilities.EnergyStorage.ITEM` 或 `Capabilities.FluidHandler.ITEM`。
- 附加材料槽必须为空。
- 已具有 `jdte:ae_extraction_enabled` 标记的物品不匹配，不能重复增强或借此更换链接。

组装时复制基础槽物品且数量固定为 1，保留其所有现有数据组件，包括自定义名称、附魔、耐久、FE、流体和物品内部库存；随后写入持久化、网络同步的布尔组件 `jdte:ae_extraction_enabled=true`，并把升级卡的 `AEComponents.WIRELESS_LINK_TARGET` 复制到成品。未绑定的升级、非 JDT/JDTE 物品、没有目标 capability 的物品和非空附加槽均不产生结果。

## 玩家物品发现

在服务端 `PlayerTickEvent.Post` 中运行补充服务。客户端、已移除玩家和死亡中的玩家直接跳过。每个 Tick 检查主背包、快捷栏、护甲、副手，并在 Curios 已安装时检查全部 Curios 槽。

发现流程先检查 `jdte:ae_extraction_enabled`，普通物品不查询 capability。增强物品按 `ItemStack` 和 capability 实例身份去重，防止同一对象通过主手、装备或 Curios 枚举路径被处理两次。Curios 调用隔离在可选集成类中，Curios 缺失时不得类加载其 API。

同一玩家本 Tick 内具有相同 `GlobalPos` 链接的物品分为一组。每组只解析一次目标维度、区块、方块实体、无线访问点和网格。访问点所在维度必须存在，目标区块必须已加载，目标方块实体必须实现 `IWirelessAccessPoint`，访问点必须 active 且网格非空；任一条件不满足时整组跳过。

链接采用现有 AE 输出升级的远程语义：不检查玩家和无线访问点之间的距离，不消耗无线终端能量；只要访问点保持已加载、在线并有频道，就允许跨维度补充。

## FE 补充

FE 桥接只在 AE2 与 Applied Flux 同时存在时启用，并使用 Applied Flux 公共 `FluxKey.of(EnergyType.FE)` 与 AE2 `MEStorage` API。操作来源使用 `IActionSource.ofPlayer(player, accessPoint)`，同时记录玩家和代理访问点。

对每个可接收 FE 的增强物品：

1. 使用物品 `IEnergyStorage.receiveEnergy(Integer.MAX_VALUE, true)` 模拟本次需求；需求为 0 时跳过。
2. 从 ME 存储模拟提取最多该需求的 FE；没有可用 FE 时跳过。
3. 实际提取可用量，再实际写入物品。
4. 如果实际写入少于实际提取，把差额立即插回同一 ME 网络。

单次 capability 调用受 `int` 上限约束；容量超过该上限的物品会在后续 Tick 继续补充，直至装满。Applied Flux 缺失或网络中没有 FE 时不修改物品。

## 流体补充

流体桥接使用 AE2 公共 `AEFluidKey` 和 `MEStorage` API，并按物品流体 handler 的储罐逐个判断：

- 非空储罐只尝试补充当前储存的同种流体及其组件。
- 空储罐通过 `IFluidHandlerItem.isFluidValid` 检查已注册的源流体。若该储罐只接受一种源流体，则可自动选择该流体；若接受零种或多种，则视为通用/不明确储罐，本次不自动选择。
- 空的通用容器必须由玩家先手动装入目标流体；后续 Tick 再按已有流体补满。
- 唯一可接受流体的判定按物品类型和储罐索引缓存，避免每 Tick 遍历流体注册表。实际写入前仍必须通过 handler 模拟，不能把缓存结果当作接收保证。

每次转移先模拟物品可填充量和 ME 可提取量，再实际从 ME 提取并填入物品。实际填入少于提取量时，把差额插回网络。网络资源不足时允许安全的部分补充；无可用资源时物品保持不变。

## 可选依赖与类加载边界

不引用 AE2 类型的公共门面负责事件入口、组件检查和可用性分发；AE2 实现类负责无线访问点解析、原生流体 ME 操作和 GridLinkables 注册；Applied Flux 实现类只负责 FE Key 与 FE 存储操作；Curios 实现类只负责饰品槽遍历。所有门面都先通过 `ModList` 判定再调用实现，保证 AE2、Applied Flux 或 Curios 缺失时 JDTE 仍能启动。

网络离线、访问点拆除、目标区块卸载、物品拒绝接收和资源不足属于正常无操作路径，不刷日志。只有实际提取后差额无法完整退回网络时才记录限频错误，包含资源类型、数量和绑定位置，便于定位潜在资源丢失。

## 玩家提示与文档

增强后的任意物品不能依靠自身 `Item` 子类覆盖 tooltip，因此通过全局物品 tooltip 事件在检测到 `jdte:ae_extraction_enabled` 时追加“AE2 提取已启用”。客户端不解析无线访问点，不显示可能过期的实时在线状态。

GuideME、Patchouli 镜像页面以及中英文语言文件说明完整流程：制作升级、在无线访问点绑定、在锻造台两件合成、携带时自动补满、通用空流体容器需先手动选择流体，以及 FE 需要 Applied Flux。

## 测试与验收

按测试先行实现以下行为：

- 锻造配方接受“已绑定升级 + 合法 capability 基础物品 + 空附加槽”，并拒绝未绑定、错误命名空间、无 capability、非空附加槽和已增强物品。
- 锻造结果完整保留基础物品组件，同时精确复制无线链接并写入启用标记。
- FE 计划与提交覆盖完整补充、部分可用、零需求、网络离线、实际接收缩小和差额回存。
- 流体选择覆盖已有流体、唯一专用流体、空通用容器、多储罐、部分可用和差额回存。
- 玩家物品发现覆盖主背包、快捷栏、护甲、副手、Curios 可选路径以及身份去重。
- 资源契约测试覆盖物品注册、配方 JSON、序列化器、模型、语言键和指南引用。
- 验证命令依次运行定向测试、完整 `test`、`compileJava` 和 `jar`，且所有命令必须以退出码 0 完成。

## 非目标

本功能不扫描箱子、机器或世界中的任意库存；不为纯 AE2 网络发明 FE 存储；不修改第三方物品 capability；不拦截每个物品的单次资源消耗调用；不允许未增强物品直接使用网络；不提供成品重新绑定、移除升级或实时网络状态 GUI。
