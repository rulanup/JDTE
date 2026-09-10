# AE Grid 虚拟 Tick 加速附属实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 修正三档时间加速器的严格倍率与资源准入，并新增独立 `jdte-ae` Jar，通过重复完整 AE2 Grid 生命周期让分子装配室、合成 CPU、样板供应器和遵循 AE 生命周期的附属设备获得真实 1024X 加速。

**架构：** 主 `jdte` 保持 AE2 可选依赖，在共享调度器前增加只含 JDTE/Minecraft/JDK 类型的单后端 SPI；普通目标继续使用有界积压队列，外部 Grid 在同一真实 tick 内收集、去重并整批执行。`jdte-ae` 强依赖 JDTE 与 AE2 19.2.17+，将命中的节点提升为整个 `appeng.me.Grid`，按同步虚拟轮次调用 Server Start、Level Start、Level End、Server End，并只在虚拟调用栈内覆盖 `TickHandler.getCurrentTick()`。

**技术栈：** Java 21、Minecraft 1.21.1、NeoForge 21.1.216+（开发环境 21.1.233）、ModDevGradle 2.0.137、AE2 19.2.17、Mixin、JUnit 5、Gradle 多项目构建。

---

## 实施前基线

- 保留当前未提交的 `ExtendedTimeAccelerationManager` 修复：AE2 `IGridTickable` 目标不能再因缺少原版 `BlockEntityTicker` 被排除。
- 保留 `AE2TimeAccelerationTargetTest`，它使用真实 `MolecularAssemblerBlockEntity` 验证上述回归。
- 不重写设计决策；实现必须逐项对照 `docs/superpowers/specs/2026-09-09-ae-grid-virtual-tick-acceleration-design.md`。
- 不执行 Git commit，除非用户在实施阶段明确授权。每个任务末尾保留可审查检查点；文中的提交命令仅在获得授权后执行。

## 文件职责

### 主模组新增文件

- `src/main/java/com/jdte/common/acceleration/ExternalTimeAccelerationBackend.java`
  - AE-neutral SPI、三态解析结果及不透明目标句柄。
- `src/main/java/com/jdte/common/acceleration/ExternalTimeAccelerationBackends.java`
  - 单后端注册、快照读取和可撤销注册句柄。
- `src/test/java/com/jdte/common/acceleration/ExternalTimeAccelerationBackendsTest.java`
  - 注册冲突、撤销和三态构造约束。

### 主模组修改文件

- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorTiming.java`
  - 严格 `X - 1` 附加周期、旧批量工作量和普通积压窗口数学。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicy.java`
  - 完整工作准入，不再返回缩小后的工作量。
- `src/main/java/com/jdte/common/blockentities/TimeAccelerationWorkQueue.java`
  - 按“目标 + 贡献者”限制普通工作积压，提供整组容量预检。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`
  - 持续时间加速器每真实 tick 只请求 `X - 1`，并暴露普通积压窗口。
- `src/main/java/com/jdte/common/blockentities/CrystalIncubatorBE.java`
  - 显式覆写批量工作量，保留现有生长尝试语义。
- `src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`
  - 外部目标三态路由、每加速器单次整批扣费、普通/外部目标分流及帧生命周期。
- `src/main/java/com/jdte/JDTE.java`
  - 将共享调度器注册为 `ServerTickEvent.Post` 的 `EventPriority.LOWEST`。
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java`
  - 1X/16X/1024X 严格倍率和积压窗口。
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java`
  - 成本只按本 tick 的附加周期计算。
- `src/test/java/com/jdte/common/blockentities/CrystalIncubatorAccelerationTest.java`
  - 水晶培育机继续按持续时间生成批量尝试。
- `src/test/java/com/jdte/common/blockentities/TimeAccelerationWorkQueueTest.java`
  - 每贡献者积压上限与整组准入。
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicyTest.java`
  - 完整接受/完整拒绝。
- `src/test/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManagerTest.java`
  - 外部三态、去重、一次扣费、回退与清理。
- `src/test/java/com/jdte/common/blockentities/AE2TimeAccelerationTargetTest.java`
  - 保留真实分子装配室回归，并补充未安装附属时的回退断言。

### 构建与附属新增文件

- `settings.gradle`
  - 包含 `jdte-ae` 子项目。
- `gradle.properties`
  - 附属 mod ID、显示名、归档名和严格 Minecraft 1.21.1 依赖范围。
- `jdte-ae/build.gradle`
  - 独立 ModDevGradle 模组、主项目源码依赖、AE2/JDT/ExtendedAE 测试运行时和 sources Jar。
- `jdte-ae/src/main/java/com/jdte/ae/JDTEAE.java`
  - `jdte_ae` 入口与后端注册日志。
- `jdte-ae/src/main/java/com/jdte/ae/AeGridAccelerationBackend.java`
  - 单帧句柄规范化、按 Grid/贡献者去重、贡献聚合、执行和清理。
- `jdte-ae/src/main/java/com/jdte/ae/AeGridResolver.java`
  - 从已加载位置解析节点主机、在线节点及具体 `Grid`。
- `jdte-ae/src/main/java/com/jdte/ae/AeGridHandle.java`
  - Grid 对象身份、锚点集合、有效性和相关已加载维度。
- `jdte-ae/src/main/java/com/jdte/ae/AeGridVirtualTickExecutor.java`
  - 稳定排序、同步虚拟轮次、完整生命周期、重入保护和崩溃报告。
- `jdte-ae/src/main/java/com/jdte/ae/AeVirtualTickContext.java`
  - 线程局部的当前虚拟 tick 覆盖值和自动清理作用域。
- `jdte-ae/src/main/java/com/jdte/ae/mixin/TickHandlerMixin.java`
  - 在 `TickHandler.getCurrentTick()` 返回点应用局部覆盖。
- `jdte-ae/src/main/resources/META-INF/neoforge.mods.toml`
  - `jdte`、AE2、Minecraft、NeoForge 必需依赖及 Mixin 声明。
- `jdte-ae/src/main/resources/mixins.jdte_ae.json`
  - `required: true` 的 AE2 Mixin 配置。
- `jdte-ae/src/test/java/com/jdte/ae/AddonMetadataTest.java`
  - 依赖范围、mod ID、Mixin 配置和独立 Jar 元数据。
- `jdte-ae/src/test/java/com/jdte/ae/AeGridResolverTest.java`
  - 三态解析、对象身份规范化、节点状态与跨维度发现。
- `jdte-ae/src/test/java/com/jdte/ae/AeGridAccelerationBackendTest.java`
  - 同 Grid/贡献者去重、重叠公式和多 Grid 完整贡献。
- `jdte-ae/src/test/java/com/jdte/ae/AeVirtualTickContextTest.java`
  - 覆盖值、溢出、异常恢复和嵌套拒绝。
- `jdte-ae/src/test/java/com/jdte/ae/TickHandlerMixinIntegrationTest.java`
  - 通过已变换的真实 `TickHandler` 验证 Mixin。
- `jdte-ae/src/test/java/com/jdte/ae/AeGridVirtualTickExecutorTest.java`
  - 生命周期顺序、1023 轮、跨 Grid 同步、失效、异常和重入。
- `jdte-ae/src/test/java/com/jdte/ae/AE2TickManagerIntegrationTest.java`
  - 真实 `Grid`/`TickManagerService` 与每次只推进 1 的探针设备。
- `jdte-ae/src/test/java/com/jdte/ae/ExtendedAECompatibilityTest.java`
  - ExtendedAE 运行时类和忽略 `ticksSinceLastCall` 的设备契约。

### 文档修改文件

- `README.md`
- `README_EN.md`
- `CHANGELOG.md`
- `AGENTS.md`
- `开发文档.md`
- `src/main/resources/assets/jdte/lang/zh_cn.json`
- `src/main/resources/assets/jdte/lang/en_us.json`
- `src/main/resources/assets/jdte/guides/jdte/guide/time-accelerator.md`
- `src/main/resources/assets/jdte/guides/jdte/guide/_en_us/time-accelerator.md`
- `src/main/resources/assets/jdte/guides/jdte/guide/upgrades.md`
- `src/main/resources/assets/jdte/guides/jdte/guide/_en_us/upgrades.md`

---

### 任务 1：锁定严格倍率数学并隔离水晶培育机

**文件：**
- 修改：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorTiming.java`
- 修改：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`
- 修改：`src/main/java/com/jdte/common/blockentities/CrystalIncubatorBE.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java`
- 测试：`src/test/java/com/jdte/common/blockentities/CrystalIncubatorAccelerationTest.java`

- [ ] **步骤 1：先把持续机器倍率测试改成严格语义**

在 `TimeAcceleratorTimingTest` 写入以下断言；旧实现会因为仍计算 `multiplier × seconds × 20` 而失败：

```java
@Test
void nominalMultiplierIncludesTheNativeTick() {
    assertEquals(0, TimeAcceleratorTiming.additionalCycles(1));
    assertEquals(15, TimeAcceleratorTiming.additionalCycles(16));
    assertEquals(1023, TimeAcceleratorTiming.additionalCycles(1024));
}

@Test
void durationOnlyBoundsOrdinaryPendingWork() {
    assertEquals(20_460L,
            TimeAcceleratorTiming.pendingWindowCycles(1024, 1, Long.MAX_VALUE));
    assertEquals(1_000L,
            TimeAcceleratorTiming.pendingWindowCycles(1024, 60, 1_000L));
}

@Test
void legacyBatchWorkRemainsAvailableForProductionMachines() {
    assertEquals(400, TimeAcceleratorTiming.batchWorkTicks(4, 5));
}
```

- [ ] **步骤 2：运行定向测试并确认红灯原因**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --no-daemon
```

预期：编译失败，明确缺少 `additionalCycles`、`pendingWindowCycles` 和 `batchWorkTicks`；不能以其他测试或依赖解析失败作为有效红灯。

- [ ] **步骤 3：实现饱和的倍率与窗口数学**

将 `TimeAcceleratorTiming` 固定为三个职责明确的方法：

```java
public static int additionalCycles(int nominalMultiplier) {
    return Math.max(0, nominalMultiplier - 1);
}

public static int batchWorkTicks(int multiplier, int durationSeconds) {
    long work = (long) Math.max(1, multiplier) * durationTicks(durationSeconds);
    return work >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) work;
}

public static long pendingWindowCycles(int nominalMultiplier, int durationSeconds,
                                       long configuredMaximum) {
    long cycles = additionalCycles(nominalMultiplier);
    long duration = durationTicks(durationSeconds);
    long window = cycles > Long.MAX_VALUE / duration
            ? Long.MAX_VALUE
            : cycles * duration;
    return Math.min(Math.max(0L, configuredMaximum), window);
}
```

保留 `durationTicks(int)`。删除或迁移所有 `workTicks` 调用，避免同名方法继续暗示持续机器会被持续时间放大。

- [ ] **步骤 4：让时间加速器提交 `X - 1`，让水晶培育机显式保留旧批量**

将 `TimeAcceleratorBE#getAccelerationWorkTicks` 改为：

```java
protected int getAccelerationWorkTicks(int effectiveMultiplier) {
    return TimeAcceleratorTiming.additionalCycles(effectiveMultiplier);
}

protected long getAccelerationPendingLimit(int effectiveMultiplier) {
    return TimeAcceleratorTiming.pendingWindowCycles(
            effectiveMultiplier,
            JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get(),
            JDTEConfig.COMMON.timeAcceleratorMaxPendingTicks.get());
}
```

在 `CrystalIncubatorBE` 增加唯一的生产机器覆写：

```java
@Override
protected int getAccelerationWorkTicks(int effectiveMultiplier) {
    return TimeAcceleratorTiming.batchWorkTicks(
            effectiveMultiplier,
            JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
}
```

这样高级/扩展加速器的 `getStandardEnergyCost()` 自动变为 `X - 1` 对应成本，而水晶培育机的成本和生长尝试仍按旧批量计算。

- [ ] **步骤 5：更新成本测试，证明持续时间不再放大当帧成本**

删除 `TimeAcceleratorCostMathTest#configuredDurationIsAppliedBeforeResourceCostCalculation` 的旧断言，替换为：

```java
@Test
void oneFrameCostUsesOnlyAdditionalCycles() {
    int workTicks = TimeAcceleratorTiming.additionalCycles(1024);
    assertEquals(1023, workTicks);
    assertEquals(102_300, TimeAcceleratorCostMath.energyCost(workTicks, 100));
}
```

扩展 `CrystalIncubatorAccelerationTest`，让记录子类调用真实覆写并断言倍率 4、持续时间 5 秒时仍得到 400 个工作 tick。

- [ ] **步骤 6：运行三组测试确认绿灯**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest --tests com.jdte.common.blockentities.CrystalIncubatorAccelerationTest --no-daemon
```

预期：全部 PASS；1024X 标准 FE 成本为 `102,300 FE/t`，1X 为 0 个附加周期。

- [ ] **步骤 7：任务检查点**

检查 `rg -n "TimeAcceleratorTiming\.workTicks" src` 无结果。若用户授权提交，使用：

```powershell
git add src/main/java/com/jdte/common/blockentities/TimeAcceleratorTiming.java src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java src/main/java/com/jdte/common/blockentities/CrystalIncubatorBE.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java src/test/java/com/jdte/common/blockentities/CrystalIncubatorAccelerationTest.java
git commit -m "fix: enforce strict time accelerator multipliers"
```

---

### 任务 2：普通队列按贡献者保留窗口并实行整批资源准入

**文件：**
- 修改：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicy.java`
- 修改：`src/main/java/com/jdte/common/blockentities/TimeAccelerationWorkQueue.java`
- 修改：`src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicyTest.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAccelerationWorkQueueTest.java`
- 测试：`src/test/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManagerTest.java`

- [ ] **步骤 1：写完整接受/拒绝与每贡献者窗口测试**

在 `TimeAcceleratorExecutionPolicyTest` 用以下测试替代会缩小请求的旧准入断言：

```java
@Test
void fullAdmissionNeverShrinksTheRequestedBatch() {
    assertTrue(TimeAcceleratorExecutionPolicy.canAdmitFullWork(40, 100L, 60L));
    assertFalse(TimeAcceleratorExecutionPolicy.canAdmitFullWork(40, 100L, 61L));
    assertFalse(TimeAcceleratorExecutionPolicy.canAdmitFullWork(0, 100L, 0L));
}
```

在 `TimeAccelerationWorkQueueTest` 增加：

```java
@Test
void pendingLimitIsPerTargetAndContributor() {
    TimeAccelerationWorkQueue<Object, String> queue = new TimeAccelerationWorkQueue<>();
    Object first = new Object();
    Object second = new Object();

    queue.enqueue("grid", first, 15, 16, 20);
    queue.enqueue("grid", second, 15, 16, 20);

    assertEquals(15, queue.pendingTicks("grid", first));
    assertEquals(15, queue.pendingTicks("grid", second));
    assertEquals(30, queue.pendingTicks("grid"));
    assertFalse(queue.canEnqueueAll(Set.of("grid"), first, 6, 20));
    assertTrue(queue.canEnqueueAll(Set.of("grid"), second, 5, 20));
}

@Test
void groupAdmissionRejectsWhenAnyOrdinaryTargetIsFull() {
    TimeAccelerationWorkQueue<Object, String> queue = new TimeAccelerationWorkQueue<>();
    Object source = new Object();
    queue.enqueue("full", source, 20, 16, 20);

    assertFalse(queue.canEnqueueAll(Set.of("full", "empty"), source, 15, 20));
    assertEquals(0, queue.pendingTicks("empty"));
}
```

- [ ] **步骤 2：运行测试确认旧总目标容量模型失败**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorExecutionPolicyTest --tests com.jdte.common.blockentities.TimeAccelerationWorkQueueTest --no-daemon
```

预期：缺少 `canAdmitFullWork`、`pendingTicks(target, source)` 和 `canEnqueueAll`，或旧 `admittedWorkTicks` 返回部分批次导致断言失败。

- [ ] **步骤 3：实现不缩批的执行策略**

在 `TimeAcceleratorExecutionPolicy` 新增并使用：

```java
public static boolean canAdmitFullWork(int requestedWorkTicks, long maxPendingTicks,
                                       long contributorPendingTicks) {
    if (requestedWorkTicks <= 0 || maxPendingTicks <= 0L) {
        return false;
    }
    long pending = Math.max(0L, contributorPendingTicks);
    return pending <= maxPendingTicks - Math.min(maxPendingTicks, (long) requestedWorkTicks)
            && (long) requestedWorkTicks <= maxPendingTicks;
}
```

删除持续机器调用中的 `admittedWorkTicks`；Ultimate Time Wand 保留一个独立的 `admittedWandWorkTicks`，继续允许其明确 `requestedTicks` 按队列剩余容量截断。

- [ ] **步骤 4：让工作队列暴露贡献者级容量预检**

在 `TimeAccelerationWorkQueue` 增加：

```java
long pendingTicks(T target, S source) {
    PendingTarget<S> work = pending.get(target);
    return work == null ? 0L : work.pendingTicks(source);
}

boolean canEnqueueAll(Collection<T> targets, S source, int workTicks, long maxPending) {
    if (targets.isEmpty() || workTicks <= 0) {
        return false;
    }
    for (T target : targets) {
        if (!TimeAcceleratorExecutionPolicy.canAdmitFullWork(
                workTicks, maxPending, pendingTicks(target, source))) {
            return false;
        }
    }
    return true;
}
```

`PendingTarget` 增加按身份读取贡献的方法：

```java
private long pendingTicks(S source) {
    Contribution contribution = contributions.get(source);
    return contribution == null ? 0L : contribution.virtualTicks;
}
```

`enqueue` 的容量断言也改为检查该贡献者，而不是目标总 `virtualTicks`；总待执行数仍用于轮询执行。

同一任务内迁移 `LevelPreparationAdapter`，避免删除旧准入签名后留下不能独立编译的中间状态：

```java
Optional<PreparedAcceleration> accept(TimeAcceleratorBE accelerator,
                                      AccelerationRequest request);

long pendingLimit(TimeAcceleratorBE accelerator, int displayMultiplier);
```

`LevelState.prepare` 对每个持续加速器先取得该机器的窗口并执行整组预检，再计算资源和扣费：

```java
long pendingLimit = adapter.pendingLimit(
        context.accelerator, context.request.displayMultiplier());
if (!workQueue.canEnqueueAll(context.targets, context.accelerator,
        context.request.workTicks(), pendingLimit)) {
    continue;
}
Optional<PreparedAcceleration> accepted =
        adapter.accept(context.accelerator, context.request);
if (accepted.isEmpty() || !adapter.pay(context.accelerator, accepted.get())) {
    continue;
}
PreparedAcceleration prepared = accepted.get();
enqueuePreparedTargets(workQueue, context.targets, context.accelerator,
        prepared.workTicks(), prepared.displayMultiplier(), pendingLimit);
```

生产适配器的 `pendingLimit` 委托任务 1 新增的 `TimeAcceleratorBE#getAccelerationPendingLimit`。测试适配器同步迁移；删除管理器对 `highestPendingTicks` 的持续机器调用。`fullTargetRejectsSubmissionBeforeAnyCostIsCalculatedOrPaid` 改为通过 `LevelState.prepare` 预填该贡献者窗口并断言 `accept`/`pay` 都未调用，不能继续直接调用已删除的旧签名。

- [ ] **步骤 5：先写资源不足整批拒绝回归**

在 `ExtendedTimeAccelerationManagerTest` 将旧的 `longDurationAdmitsTheLargestWorkBatchThatFitsMachineResources` 替换为：

```java
@Test
void unaffordableRequestIsRejectedInsteadOfSilentlyShrunk() throws Exception {
    ResourceLimitedAccelerator accelerator = newResourceLimitedAccelerator();
    ExtendedTimeAccelerationManager.AccelerationRequest request =
            new ExtendedTimeAccelerationManager.AccelerationRequest(1024, 1023);

    assertTrue(ExtendedTimeAccelerationManager
            .prepareAcceptedAcceleration(accelerator, request)
            .isEmpty());
    assertEquals(0, accelerator.consumedWorkTicks);
}
```

再增加资源刚好满足的对照：

```java
@Test
void fullyAffordableRequestKeepsItsExactSize() throws Exception {
    RecordingAccelerator accelerator = newRecordingAccelerator();
    ExtendedTimeAccelerationManager.AccelerationRequest request =
            new ExtendedTimeAccelerationManager.AccelerationRequest(16, 15);

    var prepared = ExtendedTimeAccelerationManager
            .prepareAcceptedAcceleration(accelerator, request)
            .orElseThrow();

    assertEquals(15, prepared.workTicks());
}
```

- [ ] **步骤 6：删除二分缩批并只准备完整请求**

将管理器方法收敛为：

```java
static Optional<PreparedAcceleration> prepareAcceptedAcceleration(
        TimeAcceleratorBE accelerator, AccelerationRequest request) {
    if (request.workTicks() <= 0) {
        return Optional.empty();
    }
    PreparedAcceleration prepared = prepareAcceleration(
            accelerator, request.displayMultiplier(), request.workTicks());
    return accelerator.hasResources(prepared.fluidCost(), prepared.energyCost())
            ? Optional.of(prepared)
            : Optional.empty();
}
```

完全删除 `largestAffordableWorkTicks`。`payForSubmission` 保留同线程提交前的最终模拟校验，然后一次扣除完整 `workTicks`。

- [ ] **步骤 7：运行队列、资源与旧 Wand 测试**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.blockentities.TimeAcceleratorExecutionPolicyTest --tests com.jdte.common.blockentities.TimeAccelerationWorkQueueTest --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --tests com.jdte.common.blockentities.UltimateTimeWandTargetRuntimeTest --no-daemon
```

预期：全部 PASS；普通持续机器不会部分接受，Wand 仍按其明确请求量保留余量。

- [ ] **步骤 8：任务检查点**

检查 `rg -n "largestAffordableWorkTicks|admittedWorkTicks" src/main/java` 只允许出现重命名后的 Wand 专用方法。若用户授权提交，使用：

```powershell
git add src/main/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicy.java src/main/java/com/jdte/common/blockentities/TimeAccelerationWorkQueue.java src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicyTest.java src/test/java/com/jdte/common/blockentities/TimeAccelerationWorkQueueTest.java src/test/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManagerTest.java
git commit -m "fix: reject partial time acceleration batches"
```

---

### 任务 3：建立不引用 AE2 的主模组外部后端 SPI

**文件：**
- 创建：`src/main/java/com/jdte/common/acceleration/ExternalTimeAccelerationBackend.java`
- 创建：`src/main/java/com/jdte/common/acceleration/ExternalTimeAccelerationBackends.java`
- 创建：`src/test/java/com/jdte/common/acceleration/ExternalTimeAccelerationBackendsTest.java`

- [ ] **步骤 1：写注册和三态约束测试**

测试必须覆盖：无后端时 `current()` 为空；注册后返回同一实例；第二个后端注册抛 `IllegalStateException`；关闭注册句柄只移除自己；`ACTIVE_EXTERNAL` 必须带句柄，其余状态不得带句柄。

```java
@Test
void onlyOneBackendCanBeRegistered() {
    StubBackend first = new StubBackend();
    StubBackend second = new StubBackend();
    try (ExternalTimeAccelerationBackends.Registration ignored =
                 ExternalTimeAccelerationBackends.register(first)) {
        assertSame(first, ExternalTimeAccelerationBackends.current().orElseThrow());
        assertThrows(IllegalStateException.class,
                () -> ExternalTimeAccelerationBackends.register(second));
    }
    assertTrue(ExternalTimeAccelerationBackends.current().isEmpty());
}

@Test
void activeResolutionRequiresAHandle() {
    ExternalTimeAccelerationBackend.TargetHandle handle = new StubHandle();
    assertSame(handle, ExternalTimeAccelerationBackend.TargetResolution.active(handle).handle());
    assertNull(ExternalTimeAccelerationBackend.TargetResolution.notExternal().handle());
    assertNull(ExternalTimeAccelerationBackend.TargetResolution.inactiveExternal().handle());
}
```

- [ ] **步骤 2：运行测试确认 SPI 尚不存在**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.acceleration.ExternalTimeAccelerationBackendsTest --no-daemon
```

预期：测试编译失败，缺少两个生产类型。

- [ ] **步骤 3：创建 AE-neutral 接口**

`ExternalTimeAccelerationBackend` 的公开签名固定为：

```java
public interface ExternalTimeAccelerationBackend {
    void beginFrame(MinecraftServer server);

    TargetResolution resolve(ServerLevel level, BlockPos pos);

    void submit(TargetHandle target, Object contributor, int additionalCycles);

    void executeFrame(MinecraftServer server);

    void clearFrame(MinecraftServer server);

    default void onLevelUnload(ServerLevel level) {
    }

    default void onServerStopped(MinecraftServer server) {
    }

    interface TargetHandle {
    }

    enum TargetState {
        NOT_EXTERNAL,
        INACTIVE_EXTERNAL,
        ACTIVE_EXTERNAL
    }

    record TargetResolution(TargetState state, TargetHandle handle) {
        public TargetResolution {
            Objects.requireNonNull(state, "state");
            if ((state == TargetState.ACTIVE_EXTERNAL) != (handle != null)) {
                throw new IllegalArgumentException("Only active external targets have handles");
            }
        }

        public static TargetResolution notExternal() {
            return new TargetResolution(TargetState.NOT_EXTERNAL, null);
        }

        public static TargetResolution inactiveExternal() {
            return new TargetResolution(TargetState.INACTIVE_EXTERNAL, null);
        }

        public static TargetResolution active(TargetHandle handle) {
            return new TargetResolution(TargetState.ACTIVE_EXTERNAL,
                    Objects.requireNonNull(handle, "handle"));
        }
    }
}
```

该文件不得 import `appeng.*`、ExtendedAE 或任何其他可选模组类型。

- [ ] **步骤 4：实现单后端注册点**

`ExternalTimeAccelerationBackends` 使用同步注册和 `volatile` 读取：

```java
private static volatile ExternalTimeAccelerationBackend backend;

public static Optional<ExternalTimeAccelerationBackend> current() {
    return Optional.ofNullable(backend);
}

public static synchronized Registration register(ExternalTimeAccelerationBackend candidate) {
    Objects.requireNonNull(candidate, "candidate");
    if (backend != null) {
        throw new IllegalStateException("A time acceleration backend is already registered");
    }
    backend = candidate;
    return () -> unregister(candidate);
}

private static synchronized void unregister(ExternalTimeAccelerationBackend candidate) {
    if (backend == candidate) {
        backend = null;
    }
}

@FunctionalInterface
public interface Registration extends AutoCloseable {
    @Override
    void close();
}
```

- [ ] **步骤 5：验证接口隔离和测试绿灯**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.acceleration.ExternalTimeAccelerationBackendsTest --no-daemon
rg -n "appeng\.|extendedae" src/main/java/com/jdte/common/acceleration
```

预期：JUnit PASS；`rg` 无输出。

- [ ] **步骤 6：任务检查点**

若用户授权提交，使用：

```powershell
git add src/main/java/com/jdte/common/acceleration src/test/java/com/jdte/common/acceleration
git commit -m "feat: add external acceleration backend SPI"
```

---

### 任务 4：在共享调度器中接入三态路由和一次扣费

**文件：**
- 修改：`src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`
- 修改：`src/main/java/com/jdte/JDTE.java`
- 测试：`src/test/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManagerTest.java`
- 测试：`src/test/java/com/jdte/common/blockentities/AE2TimeAccelerationTargetTest.java`

- [ ] **步骤 1：扩展测试适配器，先写路由红灯测试**

在管理器测试中创建 `RecordingExternalBackend`，记录 `beginFrame`、`resolve`、`submit`、`executeFrame`、`clearFrame` 的顺序和次数。添加以下行为断言：

```java
@Test
void inactiveExternalTargetNeverFallsBackToVanillaTicker() {
    var result = ExtendedTimeAccelerationManager.routeExternalTarget(
            ExternalTimeAccelerationBackend.TargetResolution.inactiveExternal(),
            () -> Optional.of(fakeQueuedTarget()));

    assertTrue(result.isEmpty());
}

@Test
void notExternalContinuesOrdinaryClassification() {
    var ordinary = fakeQueuedTarget();
    var result = ExtendedTimeAccelerationManager.routeExternalTarget(
            ExternalTimeAccelerationBackend.TargetResolution.notExternal(),
            () -> Optional.of(ordinary));

    assertEquals(ordinary, result.orElseThrow().queuedTarget());
}
```

再通过 `LevelState.prepare` 的测试适配器构造一个普通目标和两个位置相同句柄的外部目标，断言：

```java
assertEquals(1, adapter.paymentCount);
assertEquals(15, state.pendingTicksForTest(ordinaryTarget));
assertEquals(1, backend.submissions.size());
assertEquals(15, backend.submissions.getFirst().additionalCycles());
```

资源不足版本断言普通队列和后端提交都为 0。

- [ ] **步骤 2：运行管理器测试确认缺少外部路由**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --no-daemon
```

预期：缺少外部目标结构和路由方法，测试编译失败。

- [ ] **步骤 3：把发现结果拆成普通目标与外部目标**

在管理器内新增明确的内部类型：

```java
record TargetLocation(ServerLevel level, BlockPos pos) {
    TargetLocation {
        Objects.requireNonNull(level, "level");
        pos = Objects.requireNonNull(pos, "pos").immutable();
    }
}

record DiscoveredTarget(TargetLocation location, TargetKey queuedTarget,
                        ExternalTimeAccelerationBackend.TargetHandle externalHandle) {
    static DiscoveredTarget queued(TargetKey target) {
        return new DiscoveredTarget(
                new TargetLocation(target.targetLevel(), target.pos()), target, null);
    }

    static DiscoveredTarget external(TargetLocation location,
                                     ExternalTimeAccelerationBackend.TargetHandle handle) {
        return new DiscoveredTarget(location, null, Objects.requireNonNull(handle, "handle"));
    }

    boolean isExternal() {
        return externalHandle != null;
    }
}
```

`AcceleratorContext` 改为同时持有：

```java
private final Set<TargetKey> queuedTargets = new LinkedHashSet<>();
private final Map<ExternalTimeAccelerationBackend.TargetHandle, TargetLocation> externalTargets =
        new LinkedHashMap<>();
```

同一规范化句柄在一台加速器上下文中只保留一次；不同句柄全部保留。

在任务 2 已迁移的 `accept`/`pendingLimit` 基础上，把 `LevelPreparationAdapter` 的发现边界改成下列签名，确保测试适配器与生产控制流使用同一套“先解析位置和过滤、后分类”顺序：

```java
Optional<TimeAccelerationTarget> resolveLocation(ServerLevel level, BlockPos pos);

Optional<BlockState> loadedState(TargetLocation location);

Optional<TargetKey> classifyQueued(TargetLocation location, boolean ae2FallbackEnabled);

boolean filter(TimeAcceleratorBE accelerator, TargetLocation location, BlockState state);

```

生产适配器分别委托 `resolveTimeAccelerationTarget`、`getLoadedBlockState` 和普通目标分类；资源准入与窗口方法沿用任务 2 已建立的签名。`LevelState.prepare` 增加当前帧后端参数；测试适配器不得在 `resolveLocation` 阶段预先把目标压成 `TargetKey`。

- [ ] **步骤 4：按已批准顺序执行过滤和三态分类**

发现过程必须按以下实际控制流重构：

```java
Optional<TimeAccelerationTarget> resolved = adapter.resolveLocation(level, pos);
if (resolved.isEmpty()) {
    continue;
}
TargetLocation location = new TargetLocation(resolved.get().level(), resolved.get().pos());
Optional<BlockState> loadedState = adapter.loadedState(location);
if (loadedState.isEmpty()
        || !adapter.filter(context.accelerator, location, loadedState.get())) {
    continue;
}
Optional<DiscoveredTarget> target = classifyMachineTarget(
        location, context.ae2AccelerationEnabled, context.externalBackend,
        adapter::classifyQueued);
target.ifPresent(context::addTarget);
```

`classifyMachineTarget` 的三态行为固定为：

```java
if (ae2AccelerationEnabled && backend != null) {
    TargetResolution external = backend.resolve(location.level(), location.pos());
    if (external.state() == TargetState.ACTIVE_EXTERNAL) {
        return Optional.of(DiscoveredTarget.external(location, external.handle()));
    }
    if (external.state() == TargetState.INACTIVE_EXTERNAL) {
        return Optional.empty();
    }
}
return queuedClassifier.apply(location, ae2AccelerationEnabled && backend == null)
        .map(DiscoveredTarget::queued);
```

`NOT_EXTERNAL` 在附属存在时只能继续普通方块实体/随机刻分类，不能调用旧 AE2 逐设备路径。`INACTIVE_EXTERNAL` 直接停止，防止离线 AE 机器绕过网络状态。

- [ ] **步骤 5：实现每台加速器一次整批准入、扣费和扇出**

每个 `AcceleratorContext` 的提交顺序固定为：

```java
boolean hasAnyTarget = !context.queuedTargets.isEmpty()
        || !context.externalTargets.isEmpty();
if (!hasAnyTarget || context.request.workTicks() <= 0) {
    continue;
}
long pendingLimit = adapter.pendingLimit(context.accelerator,
        context.request.displayMultiplier());
if (!context.queuedTargets.isEmpty()
        && !workQueue.canEnqueueAll(context.queuedTargets, context.accelerator,
                context.request.workTicks(), pendingLimit)) {
    continue;
}
Optional<PreparedAcceleration> accepted =
        adapter.accept(context.accelerator, context.request);
if (accepted.isEmpty() || !adapter.pay(context.accelerator, accepted.get())) {
    continue;
}
PreparedAcceleration prepared = accepted.get();
enqueuePreparedTargets(workQueue, context.queuedTargets, context.accelerator,
        prepared.workTicks(), prepared.displayMultiplier(), pendingLimit);
for (var handle : context.externalTargets.keySet()) {
    context.externalBackend.submit(handle, context.accelerator, prepared.workTicks());
}
```

一台加速器命中 N 个普通目标和 M 个 Grid 仍只调用一次 `accept` 与一次 `pay`。任一普通目标的该贡献者窗口无法容纳完整批次时，本 tick 普通和外部贡献全部拒绝且不扣费。

- [ ] **步骤 6：在服务器 Post tick 外围管理完整外部帧**

`onServerTickPost` 在开始处理任何维度前快照一次注册后端。`beginFrame` 成功后才允许进入准备和执行；所有成功打开的帧都必须在 `finally` 中关闭：

```java
ExternalTimeAccelerationBackend backend =
        ExternalTimeAccelerationBackends.current().orElse(null);
boolean frameOpen = false;
try {
    if (backend != null) {
        backend.beginFrame(server);
        frameOpen = true;
    }
    prepareAllLevels(states, backend, scanBudget);
    executeAllOrdinaryLevels(states, executionBudget);
    if (backend != null) {
        backend.executeFrame(server);
    }
} finally {
    if (frameOpen) {
        backend.clearFrame(server);
    }
}
```

这里要先完成所有维度的 `prepare`，再执行普通队列和外部 Grid，不能继续使用“一个维度 prepare 后立刻 execute”的旧交错顺序。`onLevelUnload` 和 `onServerStopped` 同时转发到当前后端，且主管理器自身状态照旧清除。

- [ ] **步骤 7：保持 Wand 和无附属回退路径**

`submitWand` 与 Wand 执行重检继续调用不含外部后端的 `resolveTargetKey`，保留其明确 `requestedTicks` 和旧逐设备 AE2 路由。持续机器只有在后端未注册时才创建 `TargetKind.AE2_GRID` 队列目标。

扩展 `AE2TimeAccelerationTargetTest`：关闭任何临时注册后端，断言真实分子装配室仍解析为 `TargetKind.AE2_GRID`；注册返回 `INACTIVE_EXTERNAL` 的后端后，通过机器分类入口断言它不再成为普通目标。

- [ ] **步骤 8：将事件优先级降至最低**

把 `JDTE` 中的注册改成：

```java
NeoForge.EVENT_BUS.addListener(
        EventPriority.LOWEST,
        ExtendedTimeAccelerationManager::onServerTickPost);
```

这样 AE2 自己的原生 `ServerTickEvent.Post` 已先完成，附属读取到的 `TickHandler` 基准 tick 是本帧原生值。

- [ ] **步骤 9：运行主调度器完整定向测试**

运行：

```powershell
.\gradlew.bat test --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --tests com.jdte.common.blockentities.AE2TimeAccelerationTargetTest --tests com.jdte.common.blockentities.TimeAccelerationWorkQueueTest --tests com.jdte.common.blockentities.UltimateTimeWandTargetRuntimeTest --no-daemon
```

预期：全部 PASS；测试记录的帧顺序为 `begin -> resolve/submit -> execute -> clear`，扣费次数恰好为 1。

- [ ] **步骤 10：任务检查点**

若用户授权提交，使用：

```powershell
git add src/main/java/com/jdte/JDTE.java src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java src/test/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManagerTest.java src/test/java/com/jdte/common/blockentities/AE2TimeAccelerationTargetTest.java
git commit -m "feat: route time acceleration through external backends"
```

---

### 任务 5：创建独立 `jdte-ae` Gradle 子项目和模组入口

**文件：**
- 修改：`settings.gradle`
- 修改：`gradle.properties`
- 创建：`jdte-ae/build.gradle`
- 创建：`jdte-ae/src/main/java/com/jdte/ae/JDTEAE.java`
- 创建：`jdte-ae/src/main/resources/META-INF/neoforge.mods.toml`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/AddonMetadataTest.java`

- [ ] **步骤 1：先加入子项目声明和元数据测试**

在 `settings.gradle` 增加：

```groovy
include 'jdte-ae'
```

在 `gradle.properties` 增加：

```properties
ae_addon_mod_id=jdte_ae
ae_addon_mod_name=JDT Extras AE Acceleration
ae_addon_archive_name=jdte-ae
ae_addon_minecraft_version_range=[1.21.1,1.22)
```

创建最小 `jdte-ae/build.gradle` 以启用 JUnit，然后写 `AddonMetadataTest`。测试使用 NightConfig `TomlParser` 解析 classpath 中的 `META-INF/neoforge.mods.toml`，按 `mods` 和 `dependencies.jdte_ae` 结构读取值，不用字符串包含断言。先断言以下结构化值；Mixin 配置留到任务 7 在真实 Mixin 类一同加入：

```java
assertEquals("jdte_ae", onlyMod(modsToml).get("modId"));
assertDependency(modsToml, "jdte", "[0.6.0-pre6]");
assertDependency(modsToml, "ae2", "[19.2.17,20)");
assertDependency(modsToml, "minecraft", "[1.21.1,1.22)");
```

`onlyMod` 要求且只允许一个 `[[mods]]` 表；`assertDependency` 从 `dependencies.jdte_ae` 的表数组中按 `modId` 精确查找一项，并同时断言 `type="required"` 与给定范围。重复或缺失依赖都必须使测试失败。

- [ ] **步骤 2：运行附属元数据测试确认红灯**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AddonMetadataTest --no-daemon
```

预期：资源不存在或内容断言失败；子项目本身必须能被 Gradle 配置。

- [ ] **步骤 3：完成子项目 ModDevGradle 配置**

`jdte-ae/build.gradle` 使用以下结构，不把 AE2 或 JDTE 打进附属 Jar：

```groovy
plugins {
    id 'java-library'
    id 'eclipse'
    id 'idea'
    id 'net.neoforged.moddev' version '2.0.137'
}

version = rootProject.mod_version
group = rootProject.mod_group_id

base {
    archivesName = rootProject.ae_addon_archive_name
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

configurations {
    localRuntime
    runtimeClasspath.extendsFrom localRuntime
}

neoForge {
    version = rootProject.neo_version_dev
    parchment {
        mappingsVersion = rootProject.parchment_mappings_version
        minecraftVersion = rootProject.parchment_minecraft_version
    }
    mods {
        jdte {
            sourceSet rootProject.sourceSets.main
        }
        jdte_ae {
            sourceSet sourceSets.main
        }
    }
    runs {
        server {
            server()
            programArgument '--nogui'
            gameDirectory = file('runs/server')
        }
    }
    unitTest {
        enable()
        testedMod = mods.jdte_ae
    }
}

dependencies {
    implementation project(':')
    implementation "net.neoforged:neoforge:${rootProject.neo_version}"
    implementation "org.appliedenergistics:appliedenergistics2:${rootProject.ae2_version}"
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    localRuntime "curse.maven:just-dire-things-1002348:7463040"
    localRuntime "org.appliedenergistics:appliedenergistics2:${rootProject.ae2_version}"
    localRuntime "curse.maven:ex-pattern-provider-892005:${rootProject.extendedae_file}"
}

tasks.named('test', Test).configure {
    useJUnitPlatform()
}
```

复制根项目已有 Maven 仓库声明中实际需要的 NeoForge、Parchment、ModMaven 和 CurseMaven 仓库；不要复制与附属编译无关的前端或数据生成配置。`processResources` 展开 `mod_version`、`neo_version_range`、`ae_addon_minecraft_version_range`、`ae2_version` 和附属属性。`jar` manifest 与根项目保持同一字段集合。

- [ ] **步骤 4：写严格依赖元数据**

`neoforge.mods.toml` 至少包含下列内容。此任务不提前声明 Mixin；任务 7 会在真实 `TickHandlerMixin` 与配置文件同时存在时追加 `[[mixins]]`：

```toml
modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"

[[mods]]
modId="${ae_addon_mod_id}"
version="${mod_version}"
displayName="${ae_addon_mod_name}"
authors="${mod_authors}"
description='''Adds full-grid virtual tick acceleration for JDT Extras and AE2.'''

[[dependencies.${ae_addon_mod_id}]]
modId="jdte"
type="required"
versionRange="[${mod_version}]"
ordering="AFTER"
side="BOTH"

[[dependencies.${ae_addon_mod_id}]]
modId="ae2"
type="required"
versionRange="[19.2.17,20)"
ordering="AFTER"
side="BOTH"

[[dependencies.${ae_addon_mod_id}]]
modId="minecraft"
type="required"
versionRange="${ae_addon_minecraft_version_range}"
ordering="NONE"
side="BOTH"

[[dependencies.${ae_addon_mod_id}]]
modId="neoforge"
type="required"
versionRange="${neo_version_range}"
ordering="NONE"
side="BOTH"

```

其中 Minecraft 的 `versionRange` 必须使用 `${ae_addon_minecraft_version_range}`，不能复用主模组较宽的 `${minecraft_version_range}`。

- [ ] **步骤 5：创建可独立加载的附属入口**

入口类固定为：

```java
@Mod(JDTEAE.MOD_ID)
public final class JDTEAE {
    public static final String MOD_ID = "jdte_ae";
    private static final Logger LOGGER = LoggerFactory.getLogger(JDTEAE.class);

    public JDTEAE() {
        LOGGER.info("JDT Extras AE Acceleration loaded");
    }
}
```

此时不注册后端，也不创建空实现。任务 6 先交付真实的解析与聚合组件，任务 8 在完整执行器存在后才让该类实现 SPI 并由入口注册；因此每个检查点都不存在会被误用的临时生产路径。

- [ ] **步骤 6：运行 Gradle 项目与元数据验证**

运行：

```powershell
.\gradlew.bat projects :jdte-ae:test --tests com.jdte.ae.AddonMetadataTest --no-daemon
```

预期：项目列表包含 `:jdte-ae`，元数据测试 PASS。

- [ ] **步骤 7：任务检查点**

若用户授权提交，使用：

```powershell
git add settings.gradle gradle.properties jdte-ae
git commit -m "feat: scaffold JDTE AE acceleration addon"
```

---

### 任务 6：解析 AE Grid 并按 Grid/贡献者对象身份去重

**文件：**
- 创建：`jdte-ae/src/main/java/com/jdte/ae/AeGridResolver.java`
- 创建：`jdte-ae/src/main/java/com/jdte/ae/AeGridHandle.java`
- 创建：`jdte-ae/src/main/java/com/jdte/ae/AeGridAccelerationBackend.java`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/AeGridResolverTest.java`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/AeGridAccelerationBackendTest.java`

- [ ] **步骤 1：写解析三态和句柄规范化红灯测试**

通过可注入的 `NodeHostLookup` 返回代理 `IInWorldGridNodeHost`/`IGridNode`，覆盖：

```java
assertEquals(TargetState.NOT_EXTERNAL, resolver.resolve(level, plainPos).state());
assertEquals(TargetState.INACTIVE_EXTERNAL, resolver.resolve(level, offlinePos).state());
assertEquals(TargetState.INACTIVE_EXTERNAL, resolver.resolve(level, bootingPos).state());

var first = resolver.resolve(level, firstNodePos);
var second = resolver.resolve(level, secondNodeOnSameGridPos);
assertEquals(TargetState.ACTIVE_EXTERNAL, first.state());
assertSame(first.handle(), second.handle());
```

节点代理的有效条件必须同时为 `isOnline() == true`、`hasGridBooted() == true`、`getGrid() instanceof appeng.me.Grid`。同一主机六个方向返回同一节点时只登记一次锚点。

- [ ] **步骤 2：写贡献聚合红灯测试**

用两个对象身份不同的贡献者和两个 Grid 句柄断言：

```java
backend.beginFrame(server);
backend.submit(gridA, acceleratorOne, 15);
backend.submit(gridA, acceleratorOne, 15);
backend.submit(gridA, acceleratorTwo, 15);
backend.submit(gridB, acceleratorOne, 15);

assertEquals(30, backend.additionalCyclesForTest(gridA));
assertEquals(15, backend.additionalCyclesForTest(gridB));
assertEquals(31, backend.totalMultiplierForTest(gridA));
```

另加 `1023 + 1023 = 2046`、总倍率 2047 的断言，并测试饱和相加不会变为负数。

- [ ] **步骤 3：运行附属解析与聚合测试确认红灯**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AeGridResolverTest --tests com.jdte.ae.AeGridAccelerationBackendTest --no-daemon
```

预期：缺少解析器、句柄和真实聚合行为。

- [ ] **步骤 4：实现本帧规范化解析器**

`AeGridResolver` 每次 `beginFrame` 创建新的 `IdentityHashMap<Grid, AeGridHandle>`。生产查找器使用 `GridHelper::getNodeHost`；测试构造器接收函数接口：

```java
@FunctionalInterface
interface NodeHostLookup {
    IInWorldGridNodeHost find(ServerLevel level, BlockPos pos);
}
```

解析算法：

```java
IInWorldGridNodeHost host = lookup.find(level, pos);
if (host == null) {
    return TargetResolution.notExternal();
}
for (Direction side : Direction.values()) {
    IGridNode node = host.getGridNode(side);
    if (node == null || !seenNodes.add(node)) {
        continue;
    }
    if (node.isOnline() && node.hasGridBooted() && node.getGrid() instanceof Grid grid) {
        AeGridHandle handle = handles.computeIfAbsent(grid, AeGridHandle::new);
        handle.addAnchor(node, level, pos);
        activeHandles.add(handle);
    }
}
return activeHandles.isEmpty()
        ? TargetResolution.inactiveExternal()
        : TargetResolution.active(activeHandles.getFirst());
```

如果同一位置实际暴露多个活跃 Grid，解析器必须为每个 Grid 建立句柄。为避免 SPI 单句柄丢失，使用一个 `AeGridHandleGroup` 实现 `TargetHandle` 包装稳定顺序的多个 `AeGridHandle`；后端提交时展开该组。单 Grid 返回规范化 `AeGridHandle` 本身。

- [ ] **步骤 5：实现句柄有效性与相关维度发现**

`AeGridHandle` 的相等性只按 `grid == other.grid`，hash 使用 `System.identityHashCode(grid)`。它保留本帧解析到的锚点，不写 NBT。

```java
boolean isValid() {
    if (grid.isEmpty()) {
        return false;
    }
    return anchors.stream().anyMatch(anchor ->
            anchor.node().isOnline()
                    && anchor.node().hasGridBooted()
                    && anchor.node().getGrid() == grid);
}

List<ServerLevel> loadedLevels() {
    Set<ServerLevel> levels = Collections.newSetFromMap(new IdentityHashMap<>());
    for (IGridNode node : grid.getNodes()) {
        if (node.isOnline() && node.hasGridBooted() && node.getGrid() == grid
                && node.getLevel() != null) {
            levels.add(node.getLevel());
        }
    }
    return levels.stream()
            .sorted(Comparator.comparing(level -> level.dimension().location().toString()))
            .toList();
}
```

这里只读取 `IGridNode#getLevel()`，不得调用 `getChunk`、`getBlockEntity` 或维度加载 API。

- [ ] **步骤 6：实现按 Grid 和贡献者身份聚合**

本任务创建的 `AeGridAccelerationBackend` 先作为未注册的帧收集组件，完整实现 `beginFrame`、`resolve`、`submit`、查询测试结果和 `clearFrame`，但暂不声明 `implements ExternalTimeAccelerationBackend`，也不提供空的 `executeFrame`。任务 8 增加真实执行器后再接通 SPI。

后端帧状态使用：

```java
private final IdentityHashMap<Grid, GridContribution> contributions = new IdentityHashMap<>();

private static final class GridContribution {
    private final AeGridHandle handle;
    private final IdentityHashMap<Object, Integer> byContributor = new IdentityHashMap<>();

    private void add(Object contributor, int cycles) {
        byContributor.merge(contributor, cycles, Math::max);
    }

    private int totalCycles() {
        long total = 0L;
        for (int cycles : byContributor.values()) {
            total = Math.min(Integer.MAX_VALUE, total + cycles);
        }
        return (int) total;
    }
}
```

同一贡献者重复命中同一 Grid 取最大单次贡献而不是相加；不同贡献者相加；同一贡献者对不同 Grid 分别保留完整贡献。`additionalCycles <= 0` 不创建工作。

- [ ] **步骤 7：运行测试确认三态、去重和公式**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AeGridResolverTest --tests com.jdte.ae.AeGridAccelerationBackendTest --no-daemon
```

预期：全部 PASS；同 Grid 多节点不会重复贡献，跨维度节点仍归并到同一 Grid。

- [ ] **步骤 8：任务检查点**

若用户授权提交，使用：

```powershell
git add jdte-ae/src/main/java/com/jdte/ae/AeGridResolver.java jdte-ae/src/main/java/com/jdte/ae/AeGridHandle.java jdte-ae/src/main/java/com/jdte/ae/AeGridAccelerationBackend.java jdte-ae/src/test/java/com/jdte/ae/AeGridResolverTest.java jdte-ae/src/test/java/com/jdte/ae/AeGridAccelerationBackendTest.java
git commit -m "feat: resolve and deduplicate AE grids"
```

---

### 任务 7：局部覆盖 AE2 当前 tick，不改全局计数器

**文件：**
- 创建：`jdte-ae/src/main/java/com/jdte/ae/AeVirtualTickContext.java`
- 创建：`jdte-ae/src/main/java/com/jdte/ae/mixin/TickHandlerMixin.java`
- 创建：`jdte-ae/src/main/resources/mixins.jdte_ae.json`
- 修改：`jdte-ae/src/main/resources/META-INF/neoforge.mods.toml`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/AeVirtualTickContextTest.java`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/TickHandlerMixinIntegrationTest.java`
- 修改：`jdte-ae/src/test/java/com/jdte/ae/AddonMetadataTest.java`

- [ ] **步骤 1：写上下文进入、退出和异常恢复测试**

```java
@Test
void overrideExistsOnlyInsideItsScope() {
    assertEquals(100L, AeVirtualTickContext.override(100L));
    try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(100L, 5)) {
        assertEquals(105L, AeVirtualTickContext.override(100L));
        assertThrows(IllegalStateException.class,
                () -> AeVirtualTickContext.enter(100L, 6));
    }
    assertEquals(100L, AeVirtualTickContext.override(100L));
}

@Test
void scopeClearsAfterExceptionAndSaturatesOverflow() {
    assertThrows(TestFailure.class, () -> {
        try (AeVirtualTickContext.Scope ignored =
                     AeVirtualTickContext.enter(Long.MAX_VALUE - 1, 10)) {
            assertEquals(Long.MAX_VALUE,
                    AeVirtualTickContext.override(Long.MAX_VALUE - 1));
            throw new TestFailure();
        }
    });
    assertFalse(AeVirtualTickContext.isActive());
}
```

- [ ] **步骤 2：写真实 TickHandler Mixin 集成红灯测试**

```java
@Test
void transformedTickHandlerUsesTheScopedValueOnly() {
    TickHandler handler = TickHandler.instance();
    long nativeTick = handler.getCurrentTick();
    try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(nativeTick, 7)) {
        assertEquals(nativeTick + 7, handler.getCurrentTick());
    }
    assertEquals(nativeTick, handler.getCurrentTick());
}
```

旧代码会返回原生值，证明测试真正覆盖 Mixin，而不只是测试帮助类。

- [ ] **步骤 3：运行上下文与 Mixin 测试确认红灯**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AeVirtualTickContextTest --tests com.jdte.ae.TickHandlerMixinIntegrationTest --no-daemon
```

预期：帮助类或 Mixin 不存在；若纯上下文先通过，真实 `TickHandler` 集成测试仍必须失败后再实现注入。

- [ ] **步骤 4：实现线程局部作用域**

`AeVirtualTickContext` 位于普通 `com.jdte.ae` 包：

```java
private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

public static Scope enter(long nativeTick, int virtualRound) {
    if (virtualRound <= 0) {
        throw new IllegalArgumentException("virtualRound must be positive");
    }
    if (CURRENT.get() != null) {
        throw new IllegalStateException("AE virtual tick context is already active");
    }
    long override = nativeTick > Long.MAX_VALUE - virtualRound
            ? Long.MAX_VALUE
            : nativeTick + virtualRound;
    CURRENT.set(override);
    return () -> CURRENT.remove();
}

public static long override(long nativeTick) {
    Long current = CURRENT.get();
    return current == null ? nativeTick : current;
}

public static boolean isActive() {
    return CURRENT.get() != null;
}

@FunctionalInterface
public interface Scope extends AutoCloseable {
    @Override
    void close();
}
```

实现复核修正（2026-09-10）：上述 `nativeTick + virtualRound` 草案会让相邻帧复用时间戳，例如 `(100, 2)` 与 `(101, 1)` 都得到 `102`，并可能被 AE2 Crafting Service 的变更去重忽略。最终实现改为服务器线程局部的单调逻辑时钟：显式虚拟值仍只在作用域内生效，但启用后的原生 `getCurrentTick()` 观察也映射到下一逻辑值，保证整个观察序列严格递增；服务器停止时重置。Mixin 仍只修改返回值，不写 `TickHandler.tickCounter`。`AeVirtualTickContextTest` 和真实 `NetworkCraftingProviders` 集成测试覆盖相邻帧挂载/卸载 provider 的时间戳唯一性。

- [ ] **步骤 5：实现 required Mixin**

```java
@Mixin(value = TickHandler.class, remap = false)
public abstract class TickHandlerMixin {
    @Inject(
            method = "getCurrentTick",
            at = @At("RETURN"),
            cancellable = true,
            require = 1,
            remap = false)
    private void jdteAe$overrideCurrentTick(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(AeVirtualTickContext.override(cir.getReturnValue()));
    }
}
```

`mixins.jdte_ae.json` 固定为 Java 21、`required: true`，Mixin 包只包含转发类：

```json
{
  "required": true,
  "package": "com.jdte.ae.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "TickHandlerMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  },
  "minVersion": "0.8"
}
```

同时在 `neoforge.mods.toml` 追加：

```toml
[[mixins]]
config="mixins.jdte_ae.json"
```

`AddonMetadataTest` 使用 Gson 将 Mixin JSON 解析为对象，断言 `required` 为布尔值 `true`、`package` 精确等于 `com.jdte.ae.mixin`、`mixins` 数组精确包含 `TickHandlerMixin`，并从解析后的 TOML `mixins` 表数组断言只注册 `mixins.jdte_ae.json`。

- [ ] **步骤 6：运行测试并扫描 Mixin 包职责**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AeVirtualTickContextTest --tests com.jdte.ae.TickHandlerMixinIntegrationTest --tests com.jdte.ae.AddonMetadataTest --no-daemon
rg -n "class AeVirtualTickContext" jdte-ae/src/main/java/com/jdte/ae/mixin
```

预期：JUnit 全部 PASS；`rg` 无结果，帮助类不在保留 Mixin 包。

- [ ] **步骤 7：任务检查点**

若用户授权提交，使用：

```powershell
git add jdte-ae/src/main/java/com/jdte/ae/AeVirtualTickContext.java jdte-ae/src/main/java/com/jdte/ae/mixin/TickHandlerMixin.java jdte-ae/src/main/resources/META-INF/neoforge.mods.toml jdte-ae/src/main/resources/mixins.jdte_ae.json jdte-ae/src/test/java/com/jdte/ae/AeVirtualTickContextTest.java jdte-ae/src/test/java/com/jdte/ae/TickHandlerMixinIntegrationTest.java jdte-ae/src/test/java/com/jdte/ae/AddonMetadataTest.java
git commit -m "feat: scope AE virtual tick values"
```

---

### 任务 8：执行完整 Grid 生命周期并验证真实 TickManager

**文件：**
- 创建：`jdte-ae/src/main/java/com/jdte/ae/AeGridVirtualTickExecutor.java`
- 修改：`jdte-ae/src/main/java/com/jdte/ae/AeGridHandle.java`
- 修改：`jdte-ae/src/main/java/com/jdte/ae/AeGridAccelerationBackend.java`
- 修改：`jdte-ae/src/main/java/com/jdte/ae/JDTEAE.java`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/AeGridVirtualTickExecutorTest.java`
- 创建：`jdte-ae/src/test/java/com/jdte/ae/AE2TickManagerIntegrationTest.java`

- [ ] **步骤 1：写两 Grid 同步轮次和完整阶段顺序测试**

用记录型 `GridTickTarget` 构造 Grid A=2 个附加周期、Grid B=1 个附加周期，维度顺序为 overworld、nether。预期事件必须完全等于：

```java
assertEquals(List.of(
        "tick=101", "A:server-start", "B:server-start",
        "A:overworld-start", "B:overworld-start", "A:nether-start",
        "A:overworld-end", "B:overworld-end", "A:nether-end",
        "A:server-end", "B:server-end",
        "tick=102", "A:server-start", "A:overworld-start", "A:nether-start",
        "A:overworld-end", "A:nether-end", "A:server-end"
), events);
```

Grid 排序先按 `serialNumber`，维度排序按 `level.dimension().location().toString()`；第二轮只包含仍有剩余贡献的 Grid A。

- [ ] **步骤 2：写 1024X、派发接力和异常红灯测试**

```java
@Test
void nominal1024RunsExactly1023AdditionalLifecycles() {
    RecordingGrid grid = new RecordingGrid(7, 1023);
    executor.execute(100L, List.of(grid));
    assertEquals(1023, grid.serverStarts);
    assertEquals(1023, grid.levelStarts);
    assertEquals(1023, grid.levelEnds);
    assertEquals(1023, grid.serverEnds);
}

@Test
void workDispatchedAtServerEndIsProcessedByTheNextRound() {
    DispatchProbe grid = new DispatchProbe(2);
    executor.execute(100L, List.of(grid));
    assertEquals(1, grid.jobsProcessedAtLevelEnd);
}
```

`DispatchProbe.serverEnd` 在第一轮设置 `pendingJob=true`，下一轮 `levelEnd` 消费它。这证明不能把设备回调和 Crafting Service 只执行一次。

异常测试让第二轮 `levelEnd` 抛出，断言得到 `ReportedException`，崩溃报告文本包含 Grid serial、锚点、虚拟轮次和请求附加周期；随后断言 `AeVirtualTickContext.isActive() == false`，并再次执行一个健康 Grid 证明重入标记已恢复。

- [ ] **步骤 3：运行执行器测试确认红灯**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AeGridVirtualTickExecutorTest --no-daemon
```

预期：执行器不存在或阶段顺序不满足。

- [ ] **步骤 4：实现同步虚拟轮次执行器**

执行器输入为不可变工作项：

```java
record GridWork(AeGridHandle handle, int additionalCycles) {
    GridWork {
        Objects.requireNonNull(handle, "handle");
        if (additionalCycles <= 0) {
            throw new IllegalArgumentException("additionalCycles must be positive");
        }
    }
}
```

核心控制流：

```java
if (Boolean.TRUE.equals(RUNNING.get())) {
    throw new IllegalStateException("Recursive AE virtual grid ticking is not allowed");
}
RUNNING.set(true);
try {
    List<GridWork> ordered = work.stream()
            .sorted(Comparator.comparingInt(item -> item.handle().serialNumber()))
            .toList();
    int maxRounds = ordered.stream()
            .mapToInt(GridWork::additionalCycles)
            .max().orElse(0);
    for (int round = 1; round <= maxRounds; round++) {
        List<GridWork> active = validForRound(ordered, round);
        if (active.isEmpty()) {
            continue;
        }
        try (AeVirtualTickContext.Scope ignored =
                     AeVirtualTickContext.enter(nativeTick, round)) {
            active.forEach(item -> item.handle().onServerStartTick());
            Map<GridWork, List<ServerLevel>> levels = snapshotLevels(active);
            for (ServerLevel level : orderedLevels(levels)) {
                active.stream().filter(item -> levels.get(item).contains(level))
                        .forEach(item -> item.handle().onLevelStartTick(level));
            }
            for (ServerLevel level : orderedLevels(levels)) {
                active.stream().filter(item -> levels.get(item).contains(level))
                        .forEach(item -> item.handle().onLevelEndTick(level));
            }
            active.forEach(item -> item.handle().onServerEndTick());
        } catch (Throwable failure) {
            throw reportFailure(failure, active, round);
        }
    }
} finally {
    RUNNING.remove();
}
```

`validForRound` 在每轮开始重新调用句柄有效性。失效 Grid 只退出剩余轮次，其他 Grid 继续；任何生命周期方法抛错则终止整帧并抛带上下文的 `ReportedException`。

- [ ] **步骤 5：让后端执行并始终清理帧状态**

此时让 `AeGridAccelerationBackend` 正式 `implements ExternalTimeAccelerationBackend`，补齐真实 `executeFrame`、卸载和服务器停止清理路径；不得添加空实现或吞掉执行异常。

`AeGridAccelerationBackend#executeFrame` 获取一次：

```java
long nativeTick = TickHandler.instance().getCurrentTick();
List<GridWork> work = contributions.values().stream()
        .map(contribution -> new GridWork(
                contribution.handle, contribution.totalCycles()))
        .filter(item -> item.additionalCycles() > 0)
        .toList();
executor.execute(nativeTick, work);
```

`clearFrame` 必须清空 resolver 规范化句柄、贡献 Map 和当前服务器引用。`beginFrame` 在已有帧时抛出重入错误；`onServerStopped` 无条件清理匹配服务器的状态。

最后才在 `JDTEAE` 构造器注册完整后端，并保留一次启动日志：

```java
ExternalTimeAccelerationBackends.register(AeGridAccelerationBackend.INSTANCE);
LOGGER.info("JDT Extras AE Acceleration loaded; registered AE2 full-grid time acceleration backend");
```

- [ ] **步骤 6：写真实 AE2 TickManager 探针**

`AE2TickManagerIntegrationTest` 复用 AE2 自身测试思路：创建 `GridNode`，在 `markReady` 前添加一个 `IGridTickable` 服务；测试夹具可通过反射调用包私有 `markReady()`，并在 `finally` 调用 `node.destroy()` 从 `TickHandler` 移除 Grid。

探针固定为每周期只增加 1，故不会因 `ticksSinceLastCall=1023` 假装完成：

```java
AtomicInteger calls = new AtomicInteger();
node.addService(IGridTickable.class, new IGridTickable() {
    @Override
    public TickingRequest getTickingRequest(IGridNode gridNode) {
        return new TickingRequest(1, 1, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode gridNode, int ticksSinceLastCall) {
        calls.incrementAndGet();
        return TickRateModulation.URGENT;
    }
});
```

把该真实 `Grid` 包装为测试句柄，执行 1023 个附加周期并断言：

```java
assertEquals(1023, calls.get());
assertEquals(1023, observedTicksSinceLastCall.size());
assertTrue(observedTicksSinceLastCall.stream().allMatch(ticks -> ticks == 1));
```

- [ ] **步骤 7：运行执行器、真实 AE2 和 Mixin 测试**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AeGridVirtualTickExecutorTest --tests com.jdte.ae.AE2TickManagerIntegrationTest --tests com.jdte.ae.TickHandlerMixinIntegrationTest --no-daemon
```

预期：全部 PASS；1024X 精确产生 1023 次独立 TickManager 回调，`ticksSinceLastCall` 每次为 1。

- [ ] **步骤 8：任务检查点**

若用户授权提交，使用：

```powershell
git add jdte-ae/src/main/java/com/jdte/ae/AeGridVirtualTickExecutor.java jdte-ae/src/main/java/com/jdte/ae/AeGridHandle.java jdte-ae/src/main/java/com/jdte/ae/AeGridAccelerationBackend.java jdte-ae/src/main/java/com/jdte/ae/JDTEAE.java jdte-ae/src/test/java/com/jdte/ae/AeGridVirtualTickExecutorTest.java jdte-ae/src/test/java/com/jdte/ae/AE2TickManagerIntegrationTest.java
git commit -m "feat: execute complete AE grid virtual ticks"
```

---

### 任务 9：ExtendedAE 契约、双语文档和最终验证

**文件：**
- 创建：`jdte-ae/src/test/java/com/jdte/ae/ExtendedAECompatibilityTest.java`
- 修改：`README.md`
- 修改：`README_EN.md`
- 修改：`CHANGELOG.md`
- 修改：`AGENTS.md`
- 修改：`开发文档.md`
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/time-accelerator.md`
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/_en_us/time-accelerator.md`
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/upgrades.md`
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/_en_us/upgrades.md`

- [ ] **步骤 1：增加 ExtendedAE 运行时契约测试**

在测试运行时加载当前 `extendedae_file=8025439`，验证代表性设备存在并接入标准 Grid Tick 服务：

```java
@Test
void extendedAeAssemblersParticipateInGridTicking() throws Exception {
    assertTrue(IGridTickable.class.isAssignableFrom(Class.forName(
            "com.glodblock.github.extendedae.common.tileentities.TileExMolecularAssembler")));
    assertTrue(IGridTickable.class.isAssignableFrom(Class.forName(
            "com.glodblock.github.extendedae.common.tileentities.TileCrystalAssembler")));
    assertTrue(IGridTickable.class.isAssignableFrom(Class.forName(
            "com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter")));
}
```

再用 ASM 或类字节码扫描 `TileCrystalAssembler#tickingRequest(IGridNode,int)`，断言第二个参数槽未参与其生产调用；这项契约证明只传较大的 `ticksSinceLastCall` 不能兼容该设备，必须重复完整 Grid 周期。

- [ ] **步骤 2：运行兼容测试确认运行时依赖正确**

运行：

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.ExtendedAECompatibilityTest --no-daemon
```

预期：PASS；若类不存在，先修正 `jdte-ae` 的 `testRuntimeOnly`/`localRuntime`，不能把 ExtendedAE 改成发布硬依赖。

- [ ] **步骤 3：更新中文用户说明**

`README.md`、中文 GuideME 页面、`zh_cn.json` 和 `开发文档.md` 使用一致描述：

- 主 `jdte` 中 AE2 仍是可选依赖；不装附属时保留逐设备 `IGridTickable` 回退。
- 要获得完整 Grid 加速，客户端和服务端同时安装同版本 `jdte-ae`、JDTE 和 AE2 19.2.17+。
- 标称倍率包含原生 tick：单台贡献 `X - 1`，重叠为 `1 + Σ(Xᵢ - 1)`；两台 16X 为 31X。
- 范围内命中任一在线且已启动的 AE 节点后，该节点所属整个 Grid 获得完整倍率；同 Grid 多节点不重复计算。
- 1024X 每真实 tick 同步执行 1023 个完整 Grid 生命周期，不受普通 4096 预算限制，可能明显增加 MSPT。
- 资源不足时整批不执行、不扣费，不会静默变成较低倍率。

将升级 tooltip 压缩为一行可读文案，不在 tooltip 堆叠公式；完整公式放 README 和指南。

- [ ] **步骤 4：更新英文说明和 Changelog**

`README_EN.md`、英文 GuideME 页面和 `en_us.json` 与中文逐项对应。`CHANGELOG.md` 在当前版本顶部加入中英文条目，明确：

- 修复无原版 ticker 的 AE2 分子装配室识别。
- 修复持续机器错误按持续秒数放大请求以及 4096 预算导致的假 1024X。
- 新增独立 `jdte-ae` Jar 和完整 Grid 生命周期。
- 资源不足从部分缩批改为整批拒绝。
- ExtendedAE 是验证运行时，不是硬依赖。

不要声称已做手动整套合成测试，除非步骤 9 实际完成。

- [ ] **步骤 5：运行文档校验与文本契约检查**

运行：

```powershell
.\gradlew.bat validateDocs --no-daemon
rg -n "IGridTickable devices|IGridTickable 设备|20,480|20480" README.md README_EN.md 开发文档.md src/main/resources/assets/jdte/guides
```

预期：`validateDocs` PASS；旧的“只加速 IGridTickable 设备”用户文案和错误 20,480 请求描述不再残留在现行说明中。

- [ ] **步骤 6：运行主模组定向测试**

```powershell
.\gradlew.bat :test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest --tests com.jdte.common.blockentities.TimeAccelerationWorkQueueTest --tests com.jdte.common.blockentities.TimeAcceleratorExecutionPolicyTest --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --tests com.jdte.common.blockentities.AE2TimeAccelerationTargetTest --tests com.jdte.common.blockentities.CrystalIncubatorAccelerationTest --tests com.jdte.common.blockentities.UltimateTimeWandTargetRuntimeTest --no-daemon
```

预期：全部 PASS。

- [ ] **步骤 7：运行附属定向测试**

```powershell
.\gradlew.bat :jdte-ae:test --tests com.jdte.ae.AddonMetadataTest --tests com.jdte.ae.AeGridResolverTest --tests com.jdte.ae.AeGridAccelerationBackendTest --tests com.jdte.ae.AeVirtualTickContextTest --tests com.jdte.ae.TickHandlerMixinIntegrationTest --tests com.jdte.ae.AeGridVirtualTickExecutorTest --tests com.jdte.ae.AE2TickManagerIntegrationTest --tests com.jdte.ae.ExtendedAECompatibilityTest --no-daemon
```

预期：全部 PASS，零跳过。

- [ ] **步骤 8：运行全量测试、构建和产物检查**

```powershell
.\gradlew.bat test build --no-daemon
Get-ChildItem build/libs,jdte-ae/build/libs | Select-Object FullName,Length
jar tf jdte-ae/build/libs/jdte-ae-0.6.0-pre6.jar | Select-String "META-INF/neoforge.mods.toml|mixins.jdte_ae.json|TickHandlerMixin"
git diff --check
```

预期：根项目和子项目全部测试、构建通过；至少生成：

```text
build/libs/jdte-0.6.0-pre6.jar
build/libs/jdte-0.6.0-pre6-sources.jar
jdte-ae/build/libs/jdte-ae-0.6.0-pre6.jar
jdte-ae/build/libs/jdte-ae-0.6.0-pre6-sources.jar
```

`git diff --check` 无空白错误；Windows 的 LF/CRLF 提示可以记录，但不能有 trailing whitespace。

- [ ] **步骤 9：启动带 AE2 与 ExtendedAE 的开发专用服务器**

运行：

```powershell
.\gradlew.bat :jdte-ae:runServer --no-daemon
```

等待日志出现服务器启动完成，并确认：

```text
JDT Extras loaded
JDT Extras AE Acceleration loaded; registered AE2 full-grid time acceleration backend
```

同时确认没有 Mixin target failure、缺失 `jdte`/`ae2` 依赖、重复后端注册或 ExtendedAE 类加载错误。随后在同一终端输入 `stop`，等待 Gradle 进程正常退出；不能让服务器会话留在后台。

- [ ] **步骤 10：可执行时进行开发世界端到端检查**

在开发世界建立一个包含能源、存储、Crafting CPU、样板供应器和分子装配室的 AE2 Grid，再分别检查：

1. 单台 16X、单台 1024X 和两台重叠 16X 的完成时间趋势分别符合 16X、1024X、31X。
2. 加速器只覆盖同 Grid 任一在线节点时，范围外的 CPU、供应器和装配室仍共同推进。
3. 同 Grid 多个节点都在范围内时不会按节点数倍增。
4. 断电、网络启动中或拆桥后停止，恢复后下一真实 tick 重新解析。
5. FE 或时间流体不足时该帧完全不加速且不部分扣除。
6. ExtendedAE 的扩展分子装配室、晶体装配室或装配矩阵至少完成一条实际配方。

若当前环境无法稳定自动化这一步，交付说明必须写明“未执行手动端到端世界测试”，但步骤 6-9 的自动测试和启动检查仍需完成。

- [ ] **步骤 11：最终差异审查**

逐项确认：

```powershell
git status --short
git diff --stat
git diff -- src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java jdte-ae
rg -n "appeng\." src/main/java/com/jdte/common/acceleration
rg -n "tickCounter" jdte-ae/src/main/java
```

预期：

- 主 SPI 包无 `appeng.*` 引用。
- 附属不写 `TickHandler.tickCounter` 字段，只注入 `getCurrentTick()` 返回值。
- 现有分子装配室目标修复仍在。
- 没有逐 tick 日志、异步世界访问、强制区块加载或跨 tick Grid 句柄缓存。
- 所有临时帧和 ThreadLocal 都在 `finally` 清理。

- [ ] **步骤 12：任务检查点**

若用户授权提交，使用：

```powershell
git add README.md README_EN.md CHANGELOG.md AGENTS.md 开发文档.md src/main/resources/assets/jdte/lang src/main/resources/assets/jdte/guides jdte-ae/src/test/java/com/jdte/ae/ExtendedAECompatibilityTest.java
git commit -m "docs: document AE grid acceleration addon"
```

---

## 最终验收映射

| 设计要求 | 实现任务 | 自动证据 |
| --- | --- | --- |
| 分子装配室无原版 ticker 仍可识别 | 基线、任务 4 | `AE2TimeAccelerationTargetTest` |
| 1X/16X/1024X 分别为 0/15/1023 附加周期 | 任务 1 | `TimeAcceleratorTimingTest` |
| 持续时间只限制普通积压窗口 | 任务 1-2 | timing/queue tests |
| 资源不足整批拒绝 | 任务 2、4 | manager/policy tests |
| 普通目标与多个 Grid 只扣一次 | 任务 4 | manager adapter payment count |
| 无附属保留旧 AE2 回退 | 任务 4 | real assembler fallback test |
| 离线 AE 不回退普通 ticker | 任务 4、6 | tri-state tests |
| 同 Grid 多节点/同贡献者只算一次 | 任务 6 | backend identity tests |
| 多 Grid 各获完整贡献 | 任务 6 | backend multi-grid test |
| `1 + Σ(Xᵢ - 1)` 重叠 | 任务 6 | 31X/2047X assertions |
| 每轮完整 Server/Level 生命周期 | 任务 8 | lifecycle order test |
| 1024X 恰好执行 1023 轮 | 任务 8 | exact lifecycle counts |
| Crafting 派发可在下一轮被设备消费 | 任务 8 | dispatch probe |
| TickManager 对忽略批量参数的设备重复回调 | 任务 8 | real AE2 TickManager probe |
| 不写 `tickCounter`，逻辑时间戳跨帧单调且虚拟作用域不泄漏 | 任务 7-8、复核修正 | transformed TickHandler + real provider timestamp tests |
| 异常/重入不泄漏上下文 | 任务 7-8 | cleanup and retry tests |
| ExtendedAE 不成为硬依赖且可启动 | 任务 5、9 | metadata, class contract, server startup |
| 输出两个独立 Jar 和 sources Jar | 任务 5、9 | Gradle build artifact check |
