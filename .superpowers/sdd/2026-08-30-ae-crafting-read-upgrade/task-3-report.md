
## 审查修复追加（2026-08-30）

审查反馈已处理，最终提交：`a0d58cccf5618d3ccf03b9f0f091eeeaf005fc7d`。

### 修复内容

- 撤销 `canRun()` 返回值注入及 `AECraftingReadMachineMixin`。
  - `canRun()` 不是所有真实工作入口的共同边界，不能覆盖 LifeBreeder、MineralExtractor 的独立工作路径。
  - 不再把 AE deny 混入原始 `canRun=false`，因此不会触发 BioFactory/LootFabricator 的 `resetProgress()`。
  - `src/main/java/com/jdte/mixin/AECraftingReadMachineMixin.java` 已删除，mixins 配置已移除对应注册。
- 保留 `UpgradeHelper.mayRunWithUpgrades(BaseMachineBE)` 作为机器入口可调用的独立许可策略。
- 增加策略重载：
  - `mayRunWithUpgrades(UpgradeItemStackHandler, boolean)`，用于具体副作用边界在已查询网络状态后判定。
  - `mayRunWithUpgrades(UpgradeItemStackHandler, long, TargetResolver, Predicate<ItemStack>)`，用于注入绑定判定和网络 resolver 的测试/服务端边界。
- `BaseMachineBEMixin` 仅在许可通过时调用 `AutoIoTransferHelper.tick(machine)`；不短路 ticker，不改变红石 off/reset 流程。
- 测试新增独立策略覆盖：无升级允许、安装升级且无活动任务拒绝、活动任务允许、空 resolver 状态拒绝，并保留 AE facade/cache 测试与普通机器回归。

### 验证命令与输出

- `./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest`
  - 最终结果：`BUILD SUCCESSFUL`
- `./gradlew compileJava`
  - 最终结果：`BUILD SUCCESSFUL`
- `git diff --check`
  - 通过；仅有 Git 的 LF/CRLF 提示。
- 首次审查测试运行曾因测试替身未提供绑定目标而失败；随后改为显式注入绑定谓词，测试通过。
- 中间一次测试文件插入造成语法错误，已恢复并重新验证通过。
- 最终工作树干净。

### 当前边界/疑虑

- 任务4应在 Greenhouse、LifeSynthesis、Mineral、LifeBreeder、TimeFreezer 的具体工作入口调用公开 `UpgradeHelper.mayRunWithUpgrades(...)`，而不是依赖 `canRun()`。
- BaseMachine ticker 的 AutoIO 门禁已实现；具体机器副作用、资源扣除、进度推进需由任务4在各自真实入口组合该策略。
- AE2 仍通过现有 `AE2CraftingReadNetwork` facade 隔离，未引入直接 AE2 类型依赖。
- 未修改任务4专用机器文件。

## 第2轮 Critical 修复（2026-08-30）

### 根因与实际执行边界

- JDT 1.5.7 的 `BaseMachineBlock.getTicker()` 服务端 lambda 直接调用 `BaseMachineBE.tickServer()`；代表性 `BlockBreakerT1BE.tickServer()` 在调用基类后直接进入 `doBlockBreak()`，因此只 gate Auto I/O 无法阻止真实生产。
- `BaseMachineBE.tickServer()` 本身只执行 `handleTicks()`、保护缓存清理和 `RedstoneControlledBE.evaluateRedstone()`。在该方法 HEAD 取消会跳过红石状态维护；把 AE deny 注入 `canRun()` 又会把 AE 暂停误当成机器自身不可运行，并触发部分 JDTE 机器的 `resetProgress()`。

### 修复内容

- 在现有 `BaseMachineBlockMixin.getTicker()` wrapper 接入真实 ticker gate：
  - 先调用 `evaluateRedstone()` 刷新状态，再用不消费 pulse 的 `isActiveRedstoneTestOnly()` 判断红石许可。
  - 红石关闭时仍调用一次原 ticker，保留机器原有 off/reset 行为，并且不查询 AE 许可。
  - 红石开启但 AE deny 时不调用原 ticker，因此普通 JDT 机器不会推进计时、生产或消耗资源，已有进度保持不变。
  - AE allow 时调用原 ticker；Overclock/Creative 仍调用两次，但每个世界 tick 只查询一次 AE 许可。
  - pulse 在 AE deny 时不被消费，任务恢复后由原 ticker 的正常执行路径消费。
- 未恢复任何 `canRun()` mixin，避免 BioFactory/LootFabricator 等机器因 AE 暂停清空进度。
- `BaseMachineBEMixin` 的 Auto I/O gate 保持不变。
- 未修改 Greenhouse、LargeGreenhouse、LifeSynthesisVat、MineralExtractor 或 TimeFreezer 文件。

### 测试

- 新增 `BaseMachineTickerGateTest`，直接测试 mixin 所调用的真实 ticker 执行契约：
  - deny 不推进已有进度；
  - 活动任务恢复推进；
  - 无升级保持原 ticker 与 overclock 二次执行；
  - 红石关闭仍执行一次 reset 路径，且不读取 AE 状态。
- `AECraftingReadUpgradeTest` 新增 inactive resolver 返回 `null` 的拒绝测试，并补充未加载/非活动网络状态的目标级覆盖。
- focused tests：`BUILD SUCCESSFUL`。
- 全量 `./gradlew test`：`BUILD SUCCESSFUL`。
- `./gradlew compileJava`：`BUILD SUCCESSFUL`。
- `git diff --check`：通过，仅有 Git 的 LF/CRLF 提示。

## 第3轮 Critical 修复（2026-08-30）

### 根因

- 第2轮把通用 AE gate 放在 `BaseMachineBlock` 返回的整个服务端 ticker 外层。该边界适用于普通 JDT 生产机器，但不适用于 ticker 同时承担状态机维护、manager deactivate、预留资源归还或事务恢复的 JDTE 自有机器。
- 最直接的错误是 Time Freezer：AE deny 跳过整个 ticker 会阻止 `TimeFreezerManager.deactivate(this)`，导致维度冻结状态残留。任务4要求在具体 activate/生产副作用边界接入 AE gate，任务3必须让这些 ticker 继续执行。

### 修复内容

- 新增显式 `UpgradeHelper.usesCommonAeTickerGate(BaseMachineBE)` 类型策略。
- 通用 ticker gate 排除任务4状态机族：
  - `GreenhouseBE`、`LargeGreenhouseBE`；
  - `LifeSynthesisVatBE`；
  - `MineralExtractorBE`，因此同时覆盖继承它的 `LargeMineralExtractorBE`；
  - `LifeBreederBE`；
  - `TimeFreezerBE`，因此同时覆盖 `ExtendedTimeFreezerBE`。
- 代码复审后额外排除需要持续 maintenance/deactivate 的 JDTE 机器：
  - `TimeAcceleratorMachine` 全族，包括 Basic/Advanced/Extended Time Accelerator 和继承该族的 Crystal Incubator；
  - `EntitySuppressorBE`、`RangeBlockerBE`，其 ticker 维护 active 状态、过滤缓存和 manager/client 同步；
  - `AdvancedEnergyTransmitterBE`，其 ticker 在停止时归还网络预留能量并维护目标发现；
  - `FactoryPackerBE`，其 ticker 驱动事务阶段、恢复和 rollback。
- 排除机器始终恰好执行一次 original ticker，不读取 AE 许可，也不由通用 wrapper 追加 overclock；任务4在实际副作用边界实现 deny、暂停和 deactivate。
- 普通 JDT 机器继续使用第2轮通用 gate：红石关闭仍走原 reset 路径，Pulse 在 deny 时不被消费，AE deny 不执行 ticker，allow 后恢复，Overclock/Creative 仍可执行第二次且每 tick 只查询一次许可。
- `BaseMachineBEMixin` 的 Auto I/O gate 保持不变。
- 未修改任何任务4指定机器源码。

### 测试与验证

- 扩展 `BaseMachineTickerGateTest`：
  - 使用真实机器实例验证全部任务4族及额外维护型机器被排除；
  - 显式覆盖 `LargeMineralExtractorBE`、`ExtendedTimeFreezerBE` 的继承匹配；
  - 验证普通 JDT `BaseMachineBE` 仍使用通用 gate，AE deny 时 original ticker 执行次数为 0；
  - 验证排除状态机即使 AE deny 且传入 overclock，也只执行一次 original ticker且不读取许可。
- focused tests：`BUILD SUCCESSFUL`。
- 全量 `./gradlew test`：`BUILD SUCCESSFUL`。
- `./gradlew compileJava`：`BUILD SUCCESSFUL`。
- `git diff --check`：通过，仅有 Git 的 LF/CRLF 提示。
