# AE Grid 虚拟 Tick 加速附属设计

## 背景

JDTE 当前把安装了 AE 加速升级的时间加速器目标解析为单个方块位置，并直接重复调用该位置提供的 AE2 `IGridTickable.tickingRequest`。这条路径存在四个独立问题：

- AE2 分子装配室没有原版 `BlockEntityTicker`，旧的目标分类先检查原版 ticker，导致这类合法 `IGridTickable` 被排除。
- 时间加速器每个真实 tick 都提交 `倍率 × 配置持续秒数 × 20` 个虚拟 tick。默认持续时间为 1 秒时，1024X 实际提交 20,480 个 tick，而不是每个真实 tick 添加 1023 个 tick。
- 默认全局执行预算只有 4,096 tick，并由所有维度和目标共享；持续产生的工作大于预算时，队列无法追平标称倍率。
- 只调用设备的 `IGridTickable` 不会同步推进 AE2 Crafting Service。分子装配室完成一次加工并休眠后，Crafting CPU 和样板供应器仍要等待真实服务器 tick 才能派发下一次工作。

主模组中的目标分类修复保留：启用 AE 加速时，提供 `IGridTickable` 的方块不再因为缺少原版 ticker 而被拒绝。完整 1024X 由本设计新增的 AE 强依赖附属提供。

## 目标

- 新增独立 Jar `jdte-ae`，强依赖 JDTE 和 AE2，并锁定 Minecraft 1.21.1、NeoForge 与 AE2 19.2.x。
- 时间加速器范围内只要有一个合格 AE 节点，就推进该节点所属的整个 ME Grid。
- 让单台标称 1024X 的加速器产生恰好 1023 个附加 AE 虚拟周期，加上原生周期后总计 1024X。
- 同步推进 AE2 Tick Manager、Crafting Service、样板供应器、分子装配室及遵循 AE Grid 生命周期的第三方附属设备。
- 保持 JDTE 在没有 AE2 和没有 `jdte-ae` 时可以加载；保留主模组现有的可选 AE2 逐设备回退路径。
- AE Grid 在资源充足时不因普通目标执行预算而静默降低倍率；资源不足时整批拒绝。

## 非目标

- 不加速 Minecraft 世界时间、实体、随机刻或普通方块实体；附属只推进 AE Grid 生命周期。
- 不强制加载区块或维度。
- 不承诺服务器维持 20 TPS。完整执行 1023 个 Grid 周期可能显著延长一个服务器 tick。
- 不为每个 AE2 附属编写专用适配。附属必须通过 AE2 Grid 服务或 `IGridTickable` 接入生命周期才能自动受益。
- 不支持 AE2 20.x 或其他内部结构未经验证的版本。

## 工程结构与依赖

仓库改为 Gradle 多项目构建：

```text
JDTE/
|-- build.gradle                 # 现有 jdte 主模组
|-- settings.gradle
`-- jdte-ae/
    |-- build.gradle
    `-- src/
        |-- main/java/com/jdte/ae/
        |-- main/resources/META-INF/neoforge.mods.toml
        |-- main/resources/mixins.jdte_ae.json
        `-- test/java/com/jdte/ae/
```

附属模组 ID 为 `jdte_ae`，显示名为 `JDT Extras AE Acceleration`。它声明以下必需依赖：

- `jdte`：与当前构建版本一致；
- `ae2`：`[19.2.17,20)`；
- `minecraft`：`[1.21.1,1.22)`；
- `neoforge`：沿用主项目兼容范围。

ExtendedAE 不是硬依赖。开发与启动检查中保留当前 ExtendedAE 运行时依赖，用它验证基于 AE 生命周期的兼容性。

## 主模组扩展边界

主 `jdte` 增加一个不引用 AE2 类型的外部加速后端接口和单后端注册点。接口只使用 JDTE、Minecraft 和 JDK 类型，承担以下职责：

- 在服务器 tick 开始处理加速请求前打开一个收集帧；
- 判断一个已加载方块实体位置是普通目标、已识别但未激活的外部目标，还是可用的外部目标；
- 为可用外部目标返回具有稳定等价语义的不透明句柄；
- 接收已通过过滤和完整资源校验的 `(目标句柄, 加速器身份, 附加周期数)`；
- 在所有维度的请求收集完成后执行并关闭收集帧；
- 在异常、世界关闭或附属卸载路径中清空临时状态。

目标解析使用三态结果：

1. `NOT_EXTERNAL`：附属不认识该位置，主模组继续普通目标解析；
2. `INACTIVE_EXTERNAL`：位置属于 AE，但网络离线、启动中或没有有效 Grid，本 tick 不加速，也不回退到原版方块实体加速；
3. `ACTIVE_EXTERNAL(handle)`：由附属接管整个 Grid。

这样可避免离线 AE 机器被普通 ticker 路径绕过网络状态，也避免附属存在时同时执行完整 Grid 与旧逐设备路径。

未注册外部后端时，主模组继续使用现有 `ExtendedTimeAcceleratorAE2Integration`。因此主 JDTE 的 AE2 依赖保持可选，旧整合行为不会因为缺少新 Jar 而消失。

## 目标发现与去重

主调度器继续复用按已加载区块枚举方块实体的现有扫描，不增加全世界扫描。对每台加速器和每个范围内位置，处理顺序为：

1. 跳过被移除的方块实体和其他时间加速器；
2. 使用该实际位置的方块状态执行现有黑白名单过滤；
3. 安装 AE 加速升级且附属后端存在时，让后端解析该位置；
4. 活跃 AE 节点转换为 Grid 句柄，并从普通目标集合排除；
5. 非 AE 位置继续执行现有原版 ticker、代理目标及随机刻分类。

附属通过 `GridHelper.getNodeHost` 解析 `IInWorldGridNodeHost`，检查节点已在线、网络已启动且 `getGrid()` 非空。句柄以本服务器 tick 内的 `IGrid` 对象身份作为等价键，并保存一个用于再次验证的锚点节点。

后端按 Grid 和加速器双重去重：

- 同一加速器覆盖同一 Grid 的任意数量节点，只贡献一次；
- 同一加速器覆盖多个独立 Grid，每个 Grid 都获得完整贡献；
- 多台加速器覆盖同一 Grid，各自贡献一次；
- 跨维度 Grid 仍是一个目标，不能按维度重复计数。

句柄只存活一个收集帧，不写入存档。Grid 拆分、合并、量子桥断开或节点卸载会在下一真实 tick 重新解析。执行前再次确认锚点仍在线且仍属于同一 Grid；失效句柄直接拒绝本次执行，不加载其区块。

## 严格倍率语义

标称倍率表示包含原生 tick 的总速度：

```text
单台加速器附加周期 = max(0, 标称倍率 - 1)
Grid 总倍率 = 1 + Σ(每台有效加速器的附加周期)
```

示例：

| 加速器 | 每真实 tick 附加周期 | Grid 总倍率 |
| --- | ---: | ---: |
| 1X | 0 | 1X |
| 16X | 15 | 16X |
| 1024X | 1023 | 1024X |
| 两台 16X | 30 | 31X |
| 两台 1024X | 2046 | 2047X |

后端不受 `timeAcceleratorMaxExecutionsPerTick` 的普通目标预算限制，也不把未执行的 Grid 周期排队到以后。当前 tick 要么执行完整贡献，要么在资源准入阶段拒绝该加速器。运行中出现异常属于错误，不被解释为正常限流。

## 普通目标调度修正

持续运行的时间加速器不再每个真实 tick 提交 `倍率 × duration × 20`。主调度器每 tick 只提交 `倍率 - 1` 个附加工作。

`timeAcceleratorAccelerationDurationSeconds` 改为普通方块实体和随机刻目标的待执行工作保留窗口。每个贡献者在单个目标上的积压上限为：

```text
min(timeAcceleratorMaxPendingTicks,
    (倍率 - 1) × durationSeconds × 20)
```

它不再放大当前 tick 的工作量或资源消耗。普通目标仍受全局执行预算与轮询批量保护；预算不足时保留已支付工作，达到保留窗口后停止接收和扣除新工作。AE Grid 不使用该积压队列。

Ultimate Time Wand 的一次性 `requestedTicks` 保持其已有明确工作量，不套用持续机器的 `倍率 - 1` 换算。

`CrystalIncubatorBE` 虽然复用了 `TimeAcceleratorBE` 的资源方法，但它是独立生产机器，不进入共享范围调度器。它保留现有按配置持续时间生成一批生长尝试的语义，并通过显式覆写与三档范围时间加速器隔离。

## 资源准入与扣除

每台持续加速器每真实 tick 仅进行一次资源准入，成本按其完整 `倍率 - 1` 贡献计算。准入顺序为：

1. 完成范围扫描，确认至少存在一个普通目标或活跃外部 Grid；
2. 计算该 tick 的完整附加工作、Time Fluid 和 FE 成本；
3. 同时模拟检查全部资源；
4. 资源完整时一次扣除，并把同一份贡献发送给所有合格普通目标和 Grid；
5. 任一资源不足时整台加速器本 tick 不贡献、不扣费。

不再使用“二分查找当前可负担的最大工作量”静默缩小倍率。创造升级继续免除 FE 和 Time Fluid。1X 不产生附加工作，也不消耗加速资源。

一台加速器同时覆盖普通目标和一个或多个 Grid 时只扣一次资源；目标数量不参与成本乘法。这保留当前范围机器的计费模型。

高级和扩展时间加速器界面显示的标准 FE 成本改为每真实 tick 的完整 `倍率 - 1` 成本。默认 100 FE/虚拟 tick 时，1024X 显示并要求 102,300 FE/t，而不是错误的一秒预付 2,048,000 FE。

## AE 虚拟生命周期

附属在 JDTE 的 `ServerTickEvent.Post` 最低优先级处理阶段收集完所有请求后运行，确保普通 AE2 事件监听器已经完成原生周期。所有目标 Grid 按 AE2 `Grid.getSerialNumber()` 排序，以获得稳定执行顺序。

多个 Grid 按虚拟轮次同步推进，而不是先把一个 Grid 跑完 1023 次再处理另一个。每一轮执行：

1. 对本轮仍有剩余贡献的每个 Grid 调用 `Grid.onServerStartTick()`；
2. 按服务器维度顺序，对 Grid 中当前有已加载在线节点的维度调用 `Grid.onLevelStartTick(level)`；
3. 不模拟 Minecraft 世界 tick；
4. 以相同维度顺序调用 `Grid.onLevelEndTick(level)`；
5. 对参与本轮的 Grid 调用 `Grid.onServerEndTick()`；
6. 退出本轮的 Grid 局部虚拟 tick 上下文。

`Grid.onServerStartTick()` 会推进每个 Grid 自己的 `TickManagerService.currentTick`。Level End 阶段会按 AE2 队列调度 `IGridTickable`，Server End 阶段会推进 Crafting Service。因此 Crafting CPU 在一轮末尾派发的新工作可以在下一虚拟轮次被样板供应器和装配设备处理。

附属不修改 AE2 的全局 `TickHandler.tickCounter`，否则未加速 Grid 也会看到计数器跳跃。Mixin 在 `TickHandler.getCurrentTick()` 返回点应用服务器线程局部的单调逻辑时钟：每轮虚拟生命周期进入作用域时分配一个严格递增的时间戳，同一轮中的 Grid 服务看到一致值；作用域退出后不再暴露显式虚拟值，但后续原生观察仍映射到严格递增的逻辑时间。这样相邻真实帧不会因 `nativeTick + round` 复用同一值，也不会让多个 Crafting CPU 的最大变更时间戳回退。服务器停止时清除线程时钟。每个 Grid 自己的调度时间仍由 `Grid.onServerStartTick()` 实际增加。

虚拟 tick 上下文和运行时生命周期帮助类位于普通 `com.jdte.ae` 包，不放在 Mixin 包中。Mixin 类只负责把 `TickHandler.getCurrentTick()` 转发到该上下文。

## 执行安全与错误处理

- 所有收集和虚拟周期都在 Minecraft 服务器主线程执行，不创建异步世界任务。
- 一个线程本地重入标记包围整个合成帧；任何递归调用都被拒绝。
- 重入标记、当前轮次和临时 Grid 集合必须在 `finally` 中清理。
- 每轮开始前检查 Grid 非空、锚点在线且句柄仍指向该 Grid。失效 Grid 停止剩余轮次，不影响其他 Grid。
- 不尝试把已卸载节点所在维度加入轮次，也不通过 `getChunk` 触发加载。
- AE 服务抛出的异常转换为包含 Grid 序号、锚点、当前虚拟轮次和请求倍率的 `ReportedException`。不吞异常、不继续运行后续轮次，避免把部分执行伪装成完整倍率。
- Mixin 使用 `required: true`，并通过模组依赖上限约束 AE2 19.2.x。目标方法或字段不匹配时明确阻止附属加载。
- 初版不根据 AE2 核心服务推断“Grid 空闲”。第三方服务可能仍有工作，只要有效加速器绑定 Grid 就执行完整周期并正常耗能。

## 用户可见行为与文档

- AE 加速升级仍是启用入口，不新增方块、物品、菜单或网络包。
- 中英文升级说明改为：安装 `jdte-ae` 后，范围内任一合格 AE 节点会让整个 Grid 获得严格倍率加速。
- 主 README、英文 README、GuideME/Patchouli 页面和 CHANGELOG 说明两个 Jar 的安装关系、总倍率公式、跨范围 Grid 行为和服务器负载风险。
- 附属启动时记录一次后端注册信息；不逐 tick 输出日志。
- 资源不足不再显示标称倍率下的部分执行。现有机器状态同步用于显示无法运行；不新增诊断命令。

## 测试策略

实现遵循红灯、绿灯、重构顺序。每项生产行为先有能够因旧实现而失败的测试。

### 主模组单元测试

- `1X -> 0`、`16X -> 15`、`1024X -> 1023` 的持续机器附加工作计算；
- 持续提交不再乘以 20，持续时间只限制普通工作积压窗口；
- 完整资源准入、资源不足整批拒绝、创造升级免耗和 1X 零消耗；
- 同一加速器同时命中普通与外部目标时只扣一次；
- 后端缺失时继续走旧 AE2 回退路径；后端把 AE 节点标为未激活时不走普通 ticker；
- 保留真实 `MolecularAssemblerBlockEntity` 无原版 ticker 但可分类为 AE 目标的测试。

### 附属单元与集成测试

- 同一 Grid 多节点按对象身份去重，同一贡献者只计一次；
- 多 Grid、跨维度 Grid、多个贡献者以及 `1 + Σ(Xᵢ - 1)` 叠加公式；
- 离线、启动中、卸载、拆分和合并后的句柄校验；
- 虚拟轮次严格执行上下文进入、Server Start、Level Start、Level End、Server End、上下文退出的顺序；
- `TickHandler.getCurrentTick()` 只在当前虚拟轮次返回递增覆盖值，退出或异常后恢复原生值，且不改变未加速 Grid 的全局时间；
- 1024X 帧恰好运行 1023 轮；
- 使用真实 AE2 `TickManagerService` 和一个忽略 `ticksSinceLastCall`、每次回调只增加 1 的探针 `IGridTickable`，断言其获得独立重复回调；
- Crafting Service 探针验证一轮末尾的派发能在下一轮设备阶段处理；
- 一轮抛出异常后，重入状态在 `finally` 中恢复；
- Mixin 配置包含所需 accessor，且帮助类不位于保留的 Mixin 包。

### 构建与运行验证

- 主项目定向测试；
- `jdte-ae` 定向测试；
- 根项目完整 `test` 和 `build`，并确认生成两个普通 Jar 和对应 sources Jar；
- `git diff --check`；
- 使用现有开发运行时中的 AE2 与 ExtendedAE 启动专用服务器，确认依赖和 Mixin 正常加载；
- 条件允许时在开发世界中运行包含 Crafting CPU、样板供应器、AE2 分子装配室和 ExtendedAE 装配设备的端到端合成检查。无法自动断言的手动步骤必须在交付说明中明确列出，不能冒充自动测试通过。

## 验收标准

- 主 JDTE 在没有 AE2 和没有附属时仍能编译、测试和加载。
- 只安装主 JDTE 与 AE2 时，已修正的逐设备回退路径可识别无原版 ticker 的 AE2 分子装配室。
- 安装 `jdte-ae` 后，同一 Grid 不因范围内节点数量被重复加速。
- 单台 1024X 加速器每真实服务器 tick 对目标 Grid 执行恰好 1023 个完整附加生命周期。
- 合成 CPU、样板供应器和装配设备在连续虚拟轮次中能够相互推进，而不是每个真实 tick 只派发一次。
- ExtendedAE 中忽略 `ticksSinceLastCall` 的 `IGridTickable` 仍通过重复独立回调获得加速。
- 资源不足时倍率为 0 而非低于标称值；资源充足时不受普通 4,096 执行预算限制。
- 网络卸载、拆分、合并和异常路径不留下跨 tick 临时状态或强制加载区块。
