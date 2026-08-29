# AE2 生态机器时间加速设计

## 目标

当 Basic、Advanced 或 Extended Time Accelerator 安装 AE Acceleration Upgrade，且
`timeAcceleratorAE2Enabled` 开启时，支持加速 Data Energistics（`data_energistics`）与
AE2 Lightning Tech（`ae2lt`）的机器方块实体。没有升级卡时，这两类机器不应被时间加速器
的普通机器路径加速。

已经通过 AE2 公共 `IGridTickable` 服务工作的 AE2 Part 继续保持现有行为。

## 当前上下文

`ExtendedTimeAccelerationManager` 目前将目标分为普通方块实体、随机刻方块和
`IGridTickable` AE2 节点。Data Energistics 的部分 Part 已提供 `IGridTickable`，可以继续
使用现有节点服务路径；两个目标模组的主要机器方块实体则提供自己的服务器 ticker，而不是
`IGridTickable` 服务。

现有普通方块实体执行器已经能安全地重复调用目标方块状态提供的服务器 ticker，因此新兼容
只需要补充目标分类与升级卡门控，不需要直接依赖两个可选模组的 Java 类，也不需要修改它们
的内部状态或注入 Mixin。

## 方案

### 目标识别

在 `ExtendedTimeAccelerationManager` 中增加 AE2 生态机器目标类别。使用
`BuiltInRegistries.BLOCK_ENTITY_TYPE` 查询目标方块实体类型的 `ResourceLocation`，仅将
命名空间为 `data_energistics` 或 `ae2lt` 的、拥有有效服务器 ticker 的方块实体视为该类别。

该规则不依赖可选模组的编译期类，因此两个模组缺失时不会触发类加载问题；后续同一模组
新增机器，只要注册为带服务器 ticker 的方块实体即可自动覆盖。

若一个目标同时拥有 `IGridTickable` 服务和方块实体 ticker，`IGridTickable` 路径优先，避免
同一个目标被执行两次。普通 AE2 方块与其他外部模组方块不改变现有分类。

### 调度与执行

新增的 AE2 生态机器目标使用独立的 `TargetKind`，这样队列保留逻辑可以把它与 AE2
`IGridTickable` 目标一起按“需要 AE 升级卡”的贡献者处理。

发现阶段按以下优先级加入队列：

1. 有 `IGridTickable` 服务的目标加入现有 AE2 网格目标；只有安装 AE 升级时加入。
2. 没有该服务、但属于两个目标命名空间且有服务器 ticker 的目标加入 AE2 生态机器目标；
   只有安装 AE 升级时加入。
3. 其他有效方块实体仍加入普通方块实体目标，不受本次改动影响。

执行阶段复用当前的方块实体 ticker 执行器，并继续使用现有的批量大小、全局执行预算、
待处理 tick 上限、资源预付费和无效目标清理逻辑。AE2 生态机器目标不再额外调用普通
`IGridTickable` 路径；AE2 Part 仍由现有公开节点服务执行。

移除升级卡后，调度器下一次提交会清除该加速器对 AE2 生态机器目标的保留工作；其他普通
机器目标的保留工作不受影响。

### 可选模组与兼容性

不新增 `compileOnly` 模组依赖，不在代码中引用目标模组的实现类、私有字段或反射 API。
AE2 生态识别只依赖 Minecraft 的方块实体注册表和现有服务器 ticker。命名空间规则作为
小型纯函数集中定义，便于单元测试并避免字符串判断散落在调度器中。

## 错误处理与安全边界

- 方块实体类型没有注册表 key、没有有效服务器 ticker、已移除或命中 JDT 的禁止加速标签时，
  不会进入 AE2 生态机器队列。
- 目标模组未加载时，不会有对应命名空间的已加载方块实体，现有 AE2 与普通路径保持不变。
- ticker 执行过程中目标被移除时，沿用现有执行结果和队列清理逻辑。
- 不改变目标机器自身的能量、流体、物品或 AE 网络扣除规则；时间加速器只重复调用其已
  注册的服务器 tick。
- 同一位置最多生成一种目标类别，AE2 服务目标优先于 AE2 生态机器目标，确保不重复执行。

## 测试策略

先为命名空间策略与目标分类边界编写失败测试，再实现最小改动使其通过：

- `data_energistics` 与 `ae2lt` 被识别为 AE2 生态命名空间。
- `ae2`、`jdte` 和任意其他命名空间不被识别。
- AE2 服务目标仍优先于普通 ticker 目标，避免双重加速。
- AE2 生态目标需要 AE 升级门控，普通机器仍走原有路径。
- 现有时间加速器调度器、工作队列和资源结算测试全部保持通过。

验证命令包括目标单元测试、完整 `./gradlew test` 和 `./gradlew compileJava`；若 Gradle
测试环境可加载目标运行模组，再补充一次开发服务器/客户端中的实际机器验证。

## 不在本次范围内

- 不为目标模组的每个机器类建立硬编码清单。
- 不修改 AE2、Data Energistics 或 AE2 Lightning Tech 的内部 ticker/网络实现。
- 不把普通 AE2 方块或其他模组的所有方块实体泛化为 AE 升级目标。
- 不通过 Mixin 强制给目标机器增加 `IGridTickable` 服务。
