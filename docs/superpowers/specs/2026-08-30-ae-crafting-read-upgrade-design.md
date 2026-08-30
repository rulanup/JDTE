# AE 合成读取升级设计规格

日期：2026-08-30

## 目标

新增通用标准升级“AE 合成读取升级”。升级安装到支持标准或扩展升级槽的机器后，机器只有在绑定的 AE2 网络存在正在执行的合成任务时才允许运行。没有合成任务时不推进工作、不消耗资源、不发送工作传输，也不激活外部管理器。未绑定、绑定目标失效、网络离线或 AE2 不可用均按“没有任务”处理。

## 绑定数据与交互

升级使用 AE2 公共 `GridLinkables` 协议注册为可链接物品。玩家把升级放入 AE2 无线接入点的 linking 输入槽，接入点菜单调用 handler，将目标维度和坐标以 `AEComponents.WIRELESS_LINK_TARGET` 的 `GlobalPos` 写入升级；解绑时移除该组件。升级不增加自定义右键逻辑或 GUI。该组件随 ItemStack 持久化并通过菜单同步。

## 升级系统

在 `UpgradeType` 增加 `AE_CRAFTING_READ`，序列化名为 `ae_crafting_read`，每台机器上限为 1。它作为普通 `UpgradeCardItem` 注册、加入创意物品列表，并沿用现有标准/扩展 handler、兼容性检查、槽位同步、保存加载和升级面板。补充物品模型、纹理、英文/中文名称与 tooltip。所有支持标准或扩展升级槽的机器允许该升级，不改变现有升级互斥规则。

## AE2 状态读取

AE2 代码放在可选集成门面/实现中，非 AE2 环境不得加载 AE2 类。服务端根据升级中的 `GlobalPos` 取得目标维度和已加载区块，确认目标是 `IWirelessAccessPoint`、接入点 active、网格存在且已启动。然后通过 `IGrid.getCraftingService().getCpus()` 遍历 CPU；任意 CPU `isBusy()` 且 `getJobStatus()` 有效，即判定网络存在合成任务。

读取失败、网络 booting、无 CPU、CPU 非 busy 或 job 状态无效都返回 false。读取结果按绑定位置/网络状态做短周期服务端缓存，减少同一网络上多台机器重复遍历 CPU；网格、接入点状态或绑定位置改变时必须失效，不能跨重启复用过期任务状态。所有 API 调用在服务端线程执行，不依赖 AE2 内部实现或客户端数据包。

## 运行许可接入

新增独立运行许可策略，不改变 JDT `RedstoneControlData`、`isActiveRedstone()` 或 `canRun()` 的原有语义。普通 JDT 机器在通用运行/工作副作用入口接入；JDTE 自有机器在资源消耗、进度结算、队列提交、传输和外部 manager 激活等副作用边界接入。必须覆盖自定义状态机和管理器路径，尤其是 Greenhouse、Life Synthesis Vat、Mineral Extractor、Time Freezer，以及普通生产/传输机器。

运行许可关闭时保留库存和已有进度，不清空状态；许可恢复后自动继续。自动 I/O 与机器运行许可一致，避免无任务时继续向机器输入或输出。原有红石关闭时的 reset/deactivate 行为仍需执行，不能用基类无条件跳过整个 ticker 代替业务判断。

## 测试

增加纯策略测试：未绑定、目标未加载、接入点 inactive、网络 booting、无任务、有任务和缓存失效。增加升级注册/上限/绑定组件持久化测试。增加代表性普通机器与 JDTE 状态机测试，验证无任务时不推进、不扣费、不传输，检测到任务后恢复；Time Freezer 需验证关闭时撤销 active manager。运行 `compileJava`、单元测试和必要的资源/JSON 校验。

## 非目标

本次不提交 AE2 合成任务、不追踪指定 crafting job ID、不新增独立无线终端或绑定 GUI、不修改 AE2 网络频道配置，也不改变机器原本的红石模式定义。
