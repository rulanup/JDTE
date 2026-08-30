
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
