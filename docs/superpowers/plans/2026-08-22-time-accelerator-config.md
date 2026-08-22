# 时间加速器服务器配置与并行加速实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 为时间加速器增加服务器权威的“加速所有机器”和“单次加速时长（秒）”配置，按时长准确扣除时间流体与 FE，并让并行模式跳过全局执行上限。

**架构：** 两个新增值放入独立 NeoForge `SERVER` 配置，服务器/存档配置是运行时唯一来源。JDTE 配置界面始终显示这两个字段：主菜单中可编辑，进入单人存档或连接多人服务器后由客户端过滤器置灰。时间加速器把“倍率 × 秒 × 20”作为一次提交的虚拟工作量；关闭并行时保留现有全局执行预算，开启并行时只保留单目标批量大小并排空当前队列。

**技术栈：** Java 21、NeoForge 21.1.233、`ModConfigSpec`、NeoForge `ConfigurationScreen`、JUnit 5、Gradle。

---

## 文件结构

### 新建文件

- `src/main/java/com/jdte/setup/config/TimeAcceleratorServerConfig.java`：定义两个 `SERVER` 配置值、默认值、范围、配置文件注释和翻译键。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorTiming.java`：把秒转换为 Minecraft tick，并计算一次提交的虚拟工作量，集中处理整数溢出边界。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorCostMath.java`：计算时间流体/FE 成本，并提供小数时间流体结算结果。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicy.java`：把并行开关转换为全局执行预算，并计算单目标请求批量。
- `src/main/java/com/jdte/client/TimeAcceleratorConfigScreenPolicy.java`：定义配置界面在主菜单和活动世界中的锁定规则。
- `src/test/java/com/jdte/setup/TimeAcceleratorServerConfigTest.java`：验证服务器配置默认值与范围入口。
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java`：验证秒到 tick、倍率到工作量和边界乘法。
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java`：验证流体/FE 成本线性关系和小数结算。
- `src/test/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicyTest.java`：验证关闭时受全局预算限制、开启时使用无限预算。
- `src/test/java/com/jdte/client/TimeAcceleratorConfigScreenPolicyTest.java`：验证主菜单可编辑、活动世界锁定。

### 修改文件

- `src/main/java/com/jdte/setup/JDTEConfig.java`：增加 `SERVER_SPEC`、`SERVER` 和服务器配置包装对象；不把两个新值加入 `COMMON`。
- `src/main/java/com/jdte/JDTE.java`：注册独立 `ModConfig.Type.SERVER` 配置文件。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`：统一读取服务器时长配置，按虚拟工作量检查/扣除资源，并让回退的 `accelerateArea()` 使用相同工作量。
- `src/main/java/com/jdte/common/blockentities/AdvancedTimeAcceleratorBE.java`：让 FE 检查和扣除接受实际虚拟工作量。
- `src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`：使用时长工作量入队；按并行开关决定是否跳过全局执行预算。
- `src/main/java/com/jdte/client/JDTEClientMod.java`：注册配置界面过滤器，活动世界中置灰 `SERVER` 配置字段。
- `src/main/resources/assets/jdte/lang/zh_cn.json`：新增服务器配置的中文名称、描述、只读提示和卡顿警告。
- `src/main/resources/assets/jdte/lang/en_us.json`：新增对应英文翻译。

---

### 任务 1：建立服务器权威配置

**文件：**

- 创建：`src/main/java/com/jdte/setup/config/TimeAcceleratorServerConfig.java`
- 修改：`src/main/java/com/jdte/setup/JDTEConfig.java`
- 修改：`src/main/java/com/jdte/JDTE.java`
- 测试：`src/test/java/com/jdte/setup/TimeAcceleratorServerConfigTest.java`

- [ ] **步骤 1：编写失败的配置默认值测试**

```java
package com.jdte.setup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TimeAcceleratorServerConfigTest {

    @Test
    void serverSettingsUseTheApprovedDefaults() {
        assertFalse(JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.get());
        assertEquals(1, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
    }
}
```

- [ ] **步骤 2：运行测试确认它因配置入口缺失而失败**

运行：`./gradlew test --tests com.jdte.setup.TimeAcceleratorServerConfigTest`

预期：编译失败，指出 `JDTEConfig.SERVER` 或两个服务器配置字段尚未定义；不能把测试改成读取 `COMMON` 配置来绕过失败。

- [ ] **步骤 3：实现独立 SERVER 配置**

在 `TimeAcceleratorServerConfig` 中注册以下值，配置路径保持在 `jdte.timeAccelerator` 分组：

```java
public final ModConfigSpec.BooleanValue timeAcceleratorAccelerateAllMachines;
public final ModConfigSpec.IntValue timeAcceleratorAccelerationDurationSeconds;

public TimeAcceleratorServerConfig(ModConfigSpec.Builder builder) {
    builder.comment("Time Accelerator Server Settings")
            .translation("config.jdte.jdte.serverTimeAccelerator")
            .push("timeAccelerator");
    timeAcceleratorAccelerateAllMachines = builder
            .comment("Accelerate all discovered machines in one scheduler pass; may cause server lag")
            .translation("config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerateAllMachines")
            .define("timeAcceleratorAccelerateAllMachines", false);
    timeAcceleratorAccelerationDurationSeconds = builder
            .comment("Acceleration duration per submission in seconds; valid range 1-60")
            .translation("config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerationDurationSeconds")
            .defineInRange("timeAcceleratorAccelerationDurationSeconds", 1, 1, 60);
    builder.pop();
}
```

在 `JDTEConfig` 中新增 `SERVER_SPEC`/`SERVER` 静态初始化块及 `Server` 包装类；在 `JDTE` 构造函数中保留原有 COMMON 注册，并追加：

```java
modContainer.registerConfig(
        ModConfig.Type.SERVER,
        JDTEConfig.SERVER_SPEC,
        JDTE.MODID + "/time-accelerator-server.toml");
```

服务器配置值的访问入口固定为 `JDTEConfig.SERVER.timeAccelerator.<field>`，实现代码不得从客户端缓存或 `JDTEConfig.COMMON` 读取这两个值。

- [ ] **步骤 4：运行配置测试确认通过**

运行：`./gradlew test --tests com.jdte.setup.TimeAcceleratorServerConfigTest`

预期：测试通过，默认并行关闭、默认时长为 1 秒；配置 spec 的 `defineInRange` 接受 1–60 秒。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/setup/config/TimeAcceleratorServerConfig.java src/main/java/com/jdte/setup/JDTEConfig.java src/main/java/com/jdte/JDTE.java src/test/java/com/jdte/setup/TimeAcceleratorServerConfigTest.java
git commit -m "feat: add server time accelerator settings"
```

### 任务 2：实现时长与资源成本的可测试数学边界

**文件：**

- 创建：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorTiming.java`
- 创建：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorCostMath.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java`

- [ ] **步骤 1：编写失败的换算和成本测试**

```java
package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAcceleratorTimingTest {

    @Test
    void durationSecondsBecomeMinecraftTicks() {
        assertEquals(20, TimeAcceleratorTiming.durationTicks(1));
        assertEquals(1200, TimeAcceleratorTiming.durationTicks(60));
    }

    @Test
    void effectiveMultiplierScalesOneSubmission() {
        assertEquals(960, TimeAcceleratorTiming.workTicks(16, 3));
    }
}
```

```java
package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAcceleratorCostMathTest {

    @Test
    void fluidCostScalesWithVirtualTicks() {
        double oneSecond = TimeAcceleratorCostMath.fluidCost(20, 600, 1.0D, 1.0D);
        double threeSeconds = TimeAcceleratorCostMath.fluidCost(60, 600, 1.0D, 1.0D);
        assertEquals(oneSecond * 3.0D, threeSeconds, 1.0E-9D);
    }

    @Test
    void fractionalFluidSettlementDoesNotLoseCost() {
        double pending = 0.0D;
        int drained = 0;
        for (int i = 0; i < 5; i++) {
            TimeAcceleratorCostMath.Settlement settlement =
                    TimeAcceleratorCostMath.settleFluid(pending, 0.2D);
            drained += settlement.drainMb();
            pending = settlement.remainingCost();
        }
        assertEquals(1, drained);
        assertEquals(0.0D, pending, 1.0E-9D);
    }
}
```

- [ ] **步骤 2：运行测试确认它们因辅助 API 缺失而失败**

运行：`./gradlew test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest`

预期：编译失败，指出 `TimeAcceleratorTiming`、`TimeAcceleratorCostMath` 或对应方法不存在。

- [ ] **步骤 3：实现最小换算和成本辅助类**

实现以下契约：

```java
public final class TimeAcceleratorTiming {
    public static int durationTicks(int seconds) {
        long ticks = Math.max(1L, seconds) * 20L;
        return ticks >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ticks;
    }

    public static int workTicks(int effectiveMultiplier, int durationSeconds) {
        long work = (long) Math.max(1, effectiveMultiplier) * durationTicks(durationSeconds);
        return work >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) work;
    }
}
```

`TimeAcceleratorCostMath.fluidCost` 必须等价于现有公式 `virtualTicks * timeWandFluidCost * configMultiplier * tierMultiplier / 600.0`；FE 成本必须使用饱和整数乘法；`settleFluid` 返回本次整数扣除量和剩余小数成本。

- [ ] **步骤 4：运行数学测试确认通过**

运行：`./gradlew test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest`

预期：所有换算、比例、1–60 秒边界和小数结算测试通过。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/blockentities/TimeAcceleratorTiming.java src/main/java/com/jdte/common/blockentities/TimeAcceleratorCostMath.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorCostMathTest.java
git commit -m "feat: add time accelerator duration cost math"
```

### 任务 3：把实际工作量接入时间加速器资源管线

**文件：**

- 修改：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`
- 修改：`src/main/java/com/jdte/common/blockentities/AdvancedTimeAcceleratorBE.java`
- 测试：复用 `TimeAcceleratorCostMathTest.java`，并扩展 `TimeAcceleratorTimingTest.java` 验证实际工作量 API

- [ ] **步骤 1：先扩展失败测试，锁定基类工作量契约**

增加一个不依赖 Minecraft 世界的契约测试，要求工作量由倍率和服务器时长共同决定：

```java
@Test
void configuredDurationIsAppliedBeforeResourceCostCalculation() {
    int workTicks = TimeAcceleratorTiming.workTicks(4, 5);
    assertEquals(400, workTicks);
    assertEquals(
            TimeAcceleratorCostMath.fluidCost(400, 600, 1.0D, 2.0D),
            TimeAcceleratorCostMath.fluidCost(workTicks, 600, 1.0D, 2.0D),
            1.0E-9D);
}
```

- [ ] **步骤 2：运行测试确认新契约先失败或暴露旧路径**

运行：`./gradlew test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest`

预期：在基类接入前，新增的工作量入口不存在或仍只使用单 tick 倍率；失败原因必须指向新契约，而不是测试语法错误。

- [ ] **步骤 3：接入基类和高级档位的资源计算**

在 `TimeAcceleratorBE` 中增加统一入口：

```java
protected int getAccelerationWorkTicks(int effectiveMultiplier) {
    return TimeAcceleratorTiming.workTicks(
            effectiveMultiplier,
            JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
}
```

将 `getFluidDrainAmount`、`getFluidCostPerTick` 和 `getEnergyCost` 的参数语义改为“本次虚拟工作量”，并让 `consumeResources` 接收该工作量，以便用同一精确成本结算 `pendingFluidCost`。`AdvancedTimeAcceleratorBE` 的 FE 模拟/执行扣除必须使用同一个工作量参数。

`accelerateArea()` 必须先计算工作量，再使用该工作量进行资源检查和每个目标的 ticker/random tick 执行；Creative 仍绕过资源检查和扣除。

- [ ] **步骤 4：运行时间加速器数学和现有单元测试**

运行：`./gradlew test --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest --tests com.jdte.common.blockentities.TimeAcceleratorCostMathTest --tests com.jdte.common.blockentities.AdvancedEnergyTransmitterSchedulerTest`

预期：新测试通过，旧的调度/资源相关测试不回归；时长为 1 秒时每次提交使用 20 个基础虚拟 tick，时长为 3 秒时使用 60 个基础虚拟 tick。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java src/main/java/com/jdte/common/blockentities/AdvancedTimeAcceleratorBE.java src/main/java/com/jdte/common/blockentities/BasicTimeAcceleratorBE.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorTimingTest.java
git commit -m "feat: apply configured time accelerator duration"
```

### 任务 4：实现并行模式的无限全局执行预算

**文件：**

- 创建：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicy.java`
- 修改：`src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`
- 测试：`src/test/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicyTest.java`

- [ ] **步骤 1：编写失败的执行预算测试**

```java
package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAcceleratorExecutionPolicyTest {

    @Test
    void disabledParallelModeKeepsConfiguredGlobalBudget() {
        assertEquals(4096L, TimeAcceleratorExecutionPolicy.globalBudget(false, 4096));
    }

    @Test
    void enabledParallelModeRemovesOnlyTheGlobalBudget() {
        assertEquals(Long.MAX_VALUE, TimeAcceleratorExecutionPolicy.globalBudget(true, 4096));
    }

    @Test
    void perTargetBatchSizeStillLimitsOneTickerRequest() {
        assertEquals(64, TimeAcceleratorExecutionPolicy.requestedTicks(1000L, 64, Long.MAX_VALUE));
        assertEquals(12, TimeAcceleratorExecutionPolicy.requestedTicks(1000L, 64, 12L));
    }
}
```

- [ ] **步骤 2：运行测试确认它因策略类缺失而失败**

运行：`./gradlew test --tests com.jdte.common.blockentities.TimeAcceleratorExecutionPolicyTest`

预期：编译失败，指出 `TimeAcceleratorExecutionPolicy` 不存在。

- [ ] **步骤 3：实现策略并接入管理器**

策略类提供以下确定性 API：

```java
public static long globalBudget(boolean accelerateAllMachines, int configuredBudget) {
    return accelerateAllMachines ? Long.MAX_VALUE : Math.max(0L, configuredBudget);
}

public static int requestedTicks(long pendingTicks, int batchSize, long remainingBudget) {
    long request = Math.min(pendingTicks, Math.min((long) Math.max(1, batchSize), remainingBudget));
    return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, request));
}
```

在 `ExtendedTimeAccelerationManager.LevelState.prepare` 中把服务器配置的工作量传给 `AcceleratorContext` 和 `enqueue`；保留单独的显示倍率，以免粒子效果把“倍率 × 秒”误显示成新的机器倍率。资源仍按每个加速器一次提交扣除一次，不按目标数量重复扣除。

在 `execute` 中把 `executedThisTick` 改为 `long`，使用 `globalBudget` 和 `requestedTicks`。并行关闭时输出与现有 `timeAcceleratorMaxExecutionsPerTick` 相同；并行开启时循环直到队列为空或目标失效，因此可以超过全局上限并完成当前目标。`timeAcceleratorExecutionBatchSize`、目标有效性检查、AE2 目标、随机刻扫描预算、`maxPendingTicks` 和 coalesced flush 保持不变。

- [ ] **步骤 4：运行执行策略和完整 Java 编译**

运行：`./gradlew test --tests com.jdte.common.blockentities.TimeAcceleratorExecutionPolicyTest --tests com.jdte.common.blockentities.TimeAcceleratorTimingTest; ./gradlew compileJava`

预期：预算策略测试通过；`compileJava` 退出码为 0；默认关闭路径仍使用有限预算。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicy.java src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java src/test/java/com/jdte/common/blockentities/TimeAcceleratorExecutionPolicyTest.java
git commit -m "feat: add optional unlimited time acceleration pass"
```

### 任务 5：实现主菜单可编辑、游戏中置灰的 JDTE 配置界面

**文件：**

- 创建：`src/main/java/com/jdte/client/TimeAcceleratorConfigScreenPolicy.java`
- 修改：`src/main/java/com/jdte/client/JDTEClientMod.java`
- 测试：`src/test/java/com/jdte/client/TimeAcceleratorConfigScreenPolicyTest.java`

- [ ] **步骤 1：编写失败的界面状态测试**

```java
package com.jdte.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorConfigScreenPolicyTest {

    @Test
    void serverFieldsAreEditableOnlyWithoutAnActiveWorld() {
        assertFalse(TimeAcceleratorConfigScreenPolicy.lockServerFields(false));
        assertTrue(TimeAcceleratorConfigScreenPolicy.lockServerFields(true));
    }
}
```

- [ ] **步骤 2：运行测试确认它因策略类缺失而失败**

运行：`./gradlew test --tests com.jdte.client.TimeAcceleratorConfigScreenPolicyTest`

预期：编译失败，指出 `TimeAcceleratorConfigScreenPolicy` 不存在。

- [ ] **步骤 3：实现状态策略和 ConfigurationScreen 过滤器**

策略类实现：

```java
public final class TimeAcceleratorConfigScreenPolicy {
    private TimeAcceleratorConfigScreenPolicy() {
    }

    public static boolean lockServerFields(boolean activeWorld) {
        return activeWorld;
    }
}
```

将 `JDTEClientMod` 的注册改为传入 `ConfigurationScreen` 过滤器：当 `context.modConfig().getType() == ModConfig.Type.SERVER` 且 `Minecraft.getInstance().level != null` 时，创建同名同提示但 `AbstractWidget.active = false` 的 `Element`；主菜单中返回原始 `Element`。由于独立 SERVER 配置只包含这两个字段，过滤整个 SERVER 配置的值集合不会影响现有 COMMON 配置。

过滤器不得调用配置值的 `set`、`ModConfigSpec.save()` 或发送网络包；进入世界后只能显示服务器同步值，不能写回配置文件。保留 JDTE 现有配置入口和其他配置的可编辑行为。

- [ ] **步骤 4：运行界面策略测试与编译**

运行：`./gradlew test --tests com.jdte.client.TimeAcceleratorConfigScreenPolicyTest; ./gradlew compileJava`

预期：策略测试通过；客户端配置界面相关代码在 NeoForge 21.1.233 下编译通过。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/client/TimeAcceleratorConfigScreenPolicy.java src/main/java/com/jdte/client/JDTEClientMod.java src/test/java/com/jdte/client/TimeAcceleratorConfigScreenPolicyTest.java
git commit -m "feat: lock time accelerator server settings in-world"
```

### 任务 6：补充双语配置说明并完成端到端验证

**文件：**

- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`
- 不修改：`src/main/resources/assets/jdte/guides/jdte/guide/time-accelerator.md`（本次需求只新增配置菜单描述，不改变指南结构）

- [ ] **步骤 1：列出待补充的语言键**

在语言验证脚本执行前，确认以下键在中英文文件中成对存在：

```text
config.jdte.jdte.serverTimeAccelerator
config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerateAllMachines
config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerateAllMachines.desc
config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerationDurationSeconds
config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerationDurationSeconds.desc
```

- [ ] **步骤 2：运行搜索确认新键尚未存在**

运行：`rg -n 'config\.jdte\.jdte\.serverTimeAccelerator' src/main/resources/assets/jdte/lang/zh_cn.json src/main/resources/assets/jdte/lang/en_us.json`

预期：命令无匹配并返回非零状态，证明新配置翻译尚未添加；`validateDocs` 仍只在翻译完成后作为最终资源一致性检查运行。

- [ ] **步骤 3：加入中英文名称、只读提示和风险警告**

中文描述必须明确包含：

```text
开启后会在一次调度中处理所有已发现机器，机器较多时可能造成服务器卡顿；进入游戏后该项只读。
```

英文描述必须表达同样含义。时长描述必须包含 `1-60` 秒范围、主菜单可编辑、进入游戏后只读和服务器配置文件用途。追加这些键后不改变现有翻译值。

- [ ] **步骤 4：运行完整验证命令**

依次运行：

```text
./gradlew test
./gradlew compileJava
./gradlew validateDocs
./gradlew jar
```

然后运行 `./gradlew runClient` 做客户端验收：

1. 在主菜单打开 Mods → JDTE → SERVER 配置，两个字段可编辑。
2. 创建并进入单人世界，再打开 JDTE 配置，两个字段仍显示但控件置灰。
3. 退出世界回到主菜单，两个字段恢复可编辑；修改时长后重新进入单人世界，服务器读取新值。
4. 连接多人服务器时两个字段置灰，客户端不能保存服务器值；服务端停机编辑 `serverconfig` 文件后重启，服务端使用文件值。
5. 开启并行配置并放置多个可加速机器，确认机器在同一调度轮中被处理；同时确认日志没有因大量目标产生额外异常。

- [ ] **步骤 5：检查差异并 Commit**

```text
git add src/main/resources/assets/jdte/lang/zh_cn.json src/main/resources/assets/jdte/lang/en_us.json
git commit -m "docs: describe time accelerator server settings"
```

---

## 计划自检

- 规格中的两个配置字段、默认值、范围、SERVER 权威性、主菜单可编辑、游戏中置灰、并行跳过全局预算、单目标批量保留、时长换算、时间流体/FE 线性成本、Creative 例外、双语警告和验证命令均有对应任务。
- 每个新纯逻辑 API 都先有失败测试，再实现，再运行通过测试；Minecraft 世界和客户端 UI 的行为通过策略测试、编译和 `runClient` 验收覆盖。
- 所有任务中使用的类名和方法名已统一：`TimeAcceleratorTiming.durationTicks`、`TimeAcceleratorTiming.workTicks`、`TimeAcceleratorCostMath.fluidCost`、`TimeAcceleratorCostMath.settleFluid`、`TimeAcceleratorExecutionPolicy.globalBudget`、`TimeAcceleratorExecutionPolicy.requestedTicks`、`TimeAcceleratorConfigScreenPolicy.lockServerFields`。
- 计划没有修改现有 COMMON 配置的字段归属，没有新增运行时命令/网络协议，也没有触及工作区中已有的无关改动。
- 计划文本不含未完成标记、模糊任务描述或未定义的 API 名称。
