## 任务 4：JDTE 自有状态机与管理器接入

### 实现

已在真实副作用边界接入 `UpgradeHelper.mayRunWithUpgrades(this)`：

- `GreenhouseBE` / `LargeGreenhouseBE`
  - 普通结算与加速 flush 均受 AE Crafting Read 许可控制。
  - deny 时清除客户端 active mask，但保留生产进度和加速待处理 tick，不扣流体/能量、不生成输出。
- `LifeSynthesisVatBE`
  - 普通 production advance、settle 以及 accelerated flush 均受许可控制。
  - deny 时保留 settlement ticker、culture work、pending Life Fluid、输入和资源。
- `MineralExtractorBE`（包含 `LargeMineralExtractorBE`）
  - transient work 推进、transient settle、普通 base work 推进和 accelerated flush 均受许可控制。
  - deny 时不推进、不 settle、不 flush，并跳过 transient 清理，保留 transient/pending work，恢复后继续。
- `LifeBreederBE`
  - 许可检查位于 cycle ticker 增长、掉落收集、实体扫描、年龄推进、繁殖和资源扣除之前。
  - deny 不累积暂停期间的周期时间，也不执行自动 I/O 或实体副作用。
- `TimeFreezerBE`（继承的 `ExtendedTimeFreezerBE` 同样覆盖）
  - AE deny 时不会扣除 FE/Time Fluid，也不会 activate。
  - 原有 ticker 继续执行 else 分支，确保 `TimeFreezerManager.deactivate(this)` 始终发生。

任务3排除的 `TimeAccelerator` 全族（含 `CrystalIncubator`）、`EntitySuppressor`、`RangeBlocker`、`AdvancedEnergyTransmitter`、`FactoryPacker` 已检查；它们仍需要持续 ticker maintenance/deactivate/资源归还/事务恢复，因此保持 `usesCommonAeTickerGate` 排除，不重复添加机器级 gate。

### 测试

新增：

- `src/test/java/com/jdte/common/upgrades/AECraftingReadMachineBehaviorTest.java`
  - 覆盖各目标状态机的真实入口门控契约。
  - 验证 deny 不丢失 pending/transient 状态、不提前推进 ticker。
  - 验证 Time Freezer deny 仍保留 deactivate 路径。
  - 验证维护型额外排除仍留在通用 ticker gate 之外。

### 验证结果

- `./gradlew test --tests com.jdte.common.upgrades.AECraftingReadMachineBehaviorTest`：通过
- `./gradlew test`：通过
- `./gradlew compileJava`：通过
- `git diff --check`：通过

提交：`feat: gate JDTE state machines on AE crafting`

## 审查修复追加（2026-08-30）

- 修复 Greenhouse/LargeGreenhouse 的 tickServer 顺序：先取得本次入口的 AE 许可，再执行 Essence conversion；deny 时不改变库存。
- `captureMatrixProfiles` 两个独立入口均在解析配方和构建 profile 前检查许可，deny 返回空列表。
- Greenhouse/LargeGreenhouse production tick 与 accelerated flush 复用入口许可快照；MineralExtractor 单次 ticker 复用一次 `allowed`，避免普通 tick 中重复查询。
- 新增生产实际调用的纯策略 `AECraftingReadMachinePolicy`，统一表达状态机推进和 Time Freezer 的 activate/deactivate、资源扣除决策。
- `AECraftingReadMachineBehaviorTest` 增加可执行策略测试：deny 保留 pending/progress、allow 恢复推进，Time Freezer active->deny 触发 deactivate 且不扣费；同时保留入口契约覆盖。

审查修复验证：

- `./gradlew test --tests com.jdte.common.upgrades.AECraftingReadMachineBehaviorTest`：通过
- `./gradlew test`：通过
- `./gradlew compileJava`：通过
- `git diff --check`：通过

## 第 2 轮审查修复追加（2026-08-30）

- 删除测试专用死 API `mayAdvance`，并重命名决策 record 为 `MachineWorkDecision`；`runWork`、`deactivate`、`consumeResources` 分别表达实际含义，不再把 active 状态命名为 allowed。
- `AECraftingReadMachinePolicy.production(...)` 已被 Greenhouse、LargeGreenhouse、LifeSynthesisVat、MineralExtractor、LifeBreeder 的真实生产入口调用；`freezer(...)` 被 TimeFreezer 的 activate/deactivate 和资源扣费边界调用。
- 测试不再用纯整数 API 作为行为结论；直接执行生产实际使用的 decision，并验证 deny 保留 pending、allow 恢复推进，deny 的 Time Freezer 返回 deactivate 且 `consumeResources=false`。少量源码断言仅确认各真实入口接入统一 API。

第 2 轮修复验证：

- `./gradlew test --tests com.jdte.common.upgrades.AECraftingReadMachineBehaviorTest`：通过
- `./gradlew test`：通过
- `./gradlew compileJava`：通过
- `git diff --check`：通过

## 第 3 轮定向修复追加（2026-08-30）

- Greenhouse/LargeGreenhouse `tickServer` 现在先计算 `production(allowed, isActiveRedstone(), canRun())`，只有 `decision.runWork()` 时才执行 Essence conversion 和生产推进；红石关闭或 AE deny 均不会 conversion。
- LargeGreenhouse `flushAcceleratedTicks` 改为使用同一三条件 production policy，且本次入口只读取一次 AE 许可；`canRun`、红石和 AE 语义与 tick/profile 入口一致。
- `AECraftingReadMachineBehaviorTest` 增加 conversion 前策略顺序及 Large flush 三条件策略接线契约，并保留可执行 deny/allow 状态决策测试。

第 3 轮验证：

- `./gradlew test --tests com.jdte.common.upgrades.AECraftingReadMachineBehaviorTest`：通过
- `./gradlew test`：通过
- `./gradlew compileJava`：通过
- `git diff --check`：通过
