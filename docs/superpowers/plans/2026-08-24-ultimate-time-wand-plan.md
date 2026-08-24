# 顶级时间手杖实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增独立实现的 `jdte:ultimate_time_wand`，复刻 Dyna 高级时间手杖的模式切换、目标叠加和资源扣除逻辑，最高支持 1024×，并直接加速 AE2 `IGridTickable`。

**架构：** 用 `UltimateTimeWandData` 承载不依赖 Minecraft 世界的模式、倍率、成本、饱和计算和叠加状态；物品只在服务端完成校验、资源模拟、实体创建/修改和扣费。用 `UltimateTimeWandEntity` 保存目标和剩余时间，用 `UltimateTimeWandTargetRuntime` 将普通 JDT 目标与 AE2 目标路由到 JDTE 共享的有界执行预算，AE2 路径优先且不重复执行普通方块实体 tick。

**技术栈：** Java 21、NeoForge 21.1、Minecraft 1.21.1、JUnit 5、JDT `FluidContainingItem`/时间流体成本 API、AE2 公共 `IGridTickable` API、NeoForge 数据组件和同步实体数据。

---

## 文件清单

**创建：**

- `src/main/java/com/jdte/common/items/UltimateTimeWandData.java`：模式、指数、倍率、容量、成本和实体叠加的纯逻辑。
- `src/main/java/com/jdte/common/items/UltimateTimeWandItem.java`：时间流体容器能力、潜行模式切换和目标右键操作。
- `src/main/java/com/jdte/common/entities/UltimateTimeWandEntity.java`：目标坐标、指数、总时间/剩余时间、NBT 和服务端生命周期。
- `src/main/java/com/jdte/common/blockentities/UltimateTimeWandTargetRuntime.java`：普通目标/AE2 目标统一路由和一次有界执行。
- `src/main/java/com/jdte/client/entityrenders/UltimateTimeWandRenderer.java`：倍率、剩余时间和进度条渲染。
- `src/test/java/com/jdte/common/items/UltimateTimeWandDataTest.java`：纯逻辑红绿测试。
- `src/test/java/com/jdte/common/blockentities/UltimateTimeWandTargetRuntimeTest.java`：执行预算、AE2 路由和重复执行防护测试。
- `src/test/java/com/jdte/common/entities/UltimateTimeWandEntityPersistenceTest.java`：NBT 字段和叠加状态契约测试。
- `src/test/java/com/jdte/setup/UltimateTimeWandConfigLanguageContractTest.java`：配置默认值和语言键契约测试。
- `src/main/resources/assets/jdte/models/item/ultimate_time_wand.json`：手杖模型。
- `src/main/resources/data/jdte/recipe/ultimate_time_wand.json`：只使用 JDTE/JDT/原版材料的配方。
- `src/main/resources/assets/jdte/guides/jdte/guide/ultimate-time-wand.md`：中文 GuideME 页面。
- `src/main/resources/assets/jdte/guides/jdte/guide/_en_us/ultimate-time-wand.md`：英文 GuideME 页面。

**修改：**

- `src/main/java/com/jdte/setup/config/TimeAcceleratorConfig.java`：注册顶级手杖的两种容量、最大指数、模式步进、持续时间和成本乘数配置。
- `src/main/java/com/jdte/setup/JDTEConfig.java`：暴露 `TimeAcceleratorConfig` 新字段给 COMMON 配置对象。
- `src/main/java/com/jdte/setup/JDTEDataComponents.java`：注册 `ultimate_time_wand_mode` 的持久化/网络同步组件。
- `src/main/java/com/jdte/setup/JDTEItems.java`：注册 `ULTIMATE_TIME_WAND`。
- `src/main/java/com/jdte/setup/JDTEEntities.java`：注册可保存、可追踪的 `ULTIMATE_TIME_WAND` 实体类型。
- `src/main/java/com/jdte/setup/JDTECreativeTabs.java`：把手杖加入 JDTE 创造标签，并给时间流体能力提供空容器物品。
- `src/main/java/com/jdte/client/JDTEClientSetup.java`：注册实体渲染器和模型属性/客户端事件入口。
- `src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`：抽取可复用的单目标有界执行入口，保持机器升级卡、资源扣除和区域筛选不变。
- `src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`：让现有机器和手杖共享目标执行预算、待执行工作和失效清理。
- `src/main/java/com/jdte/common/integrations/ae2/ExtendedTimeAcceleratorAE2Integration.java`：补充无升级卡门槛的直接 AE2 请求入口，继续只使用 AE2 公共 API。
- `src/main/resources/assets/jdte/lang/zh_cn.json`、`src/main/resources/assets/jdte/lang/en_us.json`：物品名、模式提示、错误提示、工具提示和配置翻译。
- `src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/zh_cn/entries/jdte/ultimate-time-wand.json`、`src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/en_us/entries/jdte/ultimate-time-wand.json`：GuideME 索引条目（若现有书籍索引需要显式列出条目，同步修改对应 index 文件）。
- `src/main/resources/assets/jdte/guides/jdte/guide/ultimate-time-wand.md`、`src/main/resources/assets/jdte/guides/jdte/guide/_en_us/ultimate-time-wand.md`：与实际操作一致的文档内容。

---

### 任务 1：先锁定纯逻辑契约和配置默认值

**文件：**

- 创建：`src/main/java/com/jdte/common/items/UltimateTimeWandData.java`
- 修改：`src/main/java/com/jdte/setup/config/TimeAcceleratorConfig.java`、`src/main/java/com/jdte/setup/JDTEConfig.java`
- 测试：`src/test/java/com/jdte/common/items/UltimateTimeWandDataTest.java`

- [x] **步骤 1：编写失败测试，覆盖模式和指数契约**

```java
@Test
void cyclesNormalX2X4MaxAndBack() {
    assertEquals(Mode.X2, Mode.NORMAL.next());
    assertEquals(Mode.X4, Mode.X2.next());
    assertEquals(Mode.MAX.next(), Mode.NORMAL);
}

@Test
void modesUseOneTwoFourAndTenExponentSteps() {
    assertEquals(1, Mode.NORMAL.step());
    assertEquals(2, Mode.X2.step());
    assertEquals(4, Mode.X4.step());
    assertEquals(10, Mode.MAX.step());
    assertEquals(1024, multiplierForExponent(10));
}
```

- [x] **步骤 2：运行目标测试确认当前缺少类型**

运行：`.\gradlew.bat test --tests com.jdte.common.items.UltimateTimeWandDataTest`

预期：FAIL，编译错误指向尚未创建的 `UltimateTimeWandData`/`Mode`。

- [x] **步骤 3：实现最小纯逻辑和 COMMON 配置**

在 `UltimateTimeWandData` 中固定四个模式名 `normal`、`x2`、`x4`、`max`，非法序号/名称统一回退 `NORMAL`；`multiplierForExponent` 使用 `1 << clamp(exponent, 0, 10)`；`addStep` 饱和到 10。配置默认值明确为：流体 `800000` mB、FE `10000000`、持续时间 `600` tick、最大指数 `10`、模式步进 `1/2/4/10`，并把倍率基础成本乘数、FE 基础成本乘数和流体小数结算开关放入 JDTE COMMON 配置。对 FE 使用 `long` 乘法后饱和到 `Integer.MAX_VALUE`；流体用 `double` 累积，结算时只取整数 mB 并保留小数余量。

- [x] **步骤 4：运行纯逻辑测试确认通过**

运行：`.\gradlew.bat test --tests com.jdte.common.items.UltimateTimeWandDataTest`

预期：PASS；模式循环、非法模式恢复、1024× 上限、FE 饱和和流体小数结算全部通过。

- [x] **步骤 5：提交纯逻辑和配置契约**

```bash
git add src/main/java/com/jdte/common/items/UltimateTimeWandData.java src/main/java/com/jdte/setup/config/TimeAcceleratorConfig.java src/main/java/com/jdte/setup/JDTEConfig.java src/test/java/com/jdte/common/items/UltimateTimeWandDataTest.java
git commit -m "feat(顶级时间手杖): 添加模式与成本逻辑"
```

### 任务 2：抽取共享目标执行器并接入 AE2 公共入口

**文件：**

- 创建：`src/main/java/com/jdte/common/blockentities/UltimateTimeWandTargetRuntime.java`
- 创建：`src/test/java/com/jdte/common/blockentities/UltimateTimeWandTargetRuntimeTest.java`
- 修改：`src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java`、`src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java`、`src/main/java/com/jdte/common/integrations/ae2/ExtendedTimeAcceleratorAE2Integration.java`

- [x] **步骤 1：先写路由和预算测试**

```java
@Test
void requestedTicksAreCappedBySharedBatchAndBudget() {
    assertEquals(64, UltimateTimeWandTargetRuntime.admit(1024, 64, 64));
    assertEquals(0, UltimateTimeWandTargetRuntime.admit(1024, 64, 0));
}

@Test
void ae2RouteWinsWhenTickableExists() {
    assertEquals(Route.AE2, UltimateTimeWandTargetRuntime.route(true, true, true));
    assertEquals(Route.ORDINARY, UltimateTimeWandTargetRuntime.route(true, false, true));
    assertEquals(Route.ORDINARY, UltimateTimeWandTargetRuntime.route(false, false, true));
}
```

- [x] **步骤 2：运行目标测试确认失败**

运行：`.\gradlew.bat test --tests com.jdte.common.blockentities.UltimateTimeWandTargetRuntimeTest`

预期：FAIL，尚未存在路由和预算入口。

- [x] **步骤 3：把普通目标执行提取为共享、有界入口**

将 `TimeAcceleratorBE.accelerateTarget` 中的方块实体 ticker、`CoalescedAcceleratedMachine`、随机刻和 `MiscTools.isValidTickAccelBlock` 相关执行提取到可被机器管理器和手杖调用的包级入口；每次调用只执行 `min(requestedTicks, JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize)`，不在 1024× 请求中无界循环。现有管理器继续负责机器贡献、全局预算、轮转和待执行虚拟 tick，改为调用该入口，确保既有时间加速器行为和资源结算不变。

- [x] **步骤 4：实现 AE2 优先路由**

在 `UltimateTimeWandTargetRuntime.execute(ServerLevel, BlockPos, int)` 中先调用 `ExtendedTimeAcceleratorAE2Integration.hasTickable`；存在服务时调用 `accelerate`，返回实际执行数并把 `SLEEP` 端点视为无效/空闲；只有没有 AE2 服务时才执行普通 JDT 目标。AE2 未加载、无节点、无 `IGridTickable` 或服务休眠时均不抛异常；同一次请求绝不再调用普通 ticker。把 AE2 直接入口与现有升级卡入口分开，现有机器仍由 `AE_ACCELERATION_UPGRADE` 控制。

- [x] **步骤 5：运行测试和现有时间加速回归测试**

运行：`.\gradlew.bat test --tests com.jdte.common.blockentities.UltimateTimeWandTargetRuntimeTest --tests com.jdte.common.blockentities.ExtendedTimeAccelerationManagerTest --tests com.jdte.common.blockentities.TimeAcceleratorExecutionPolicyTest`

预期：全部 PASS；普通路径受预算限制、AE2 路径不双算、现有机器管理器测试保持通过。

- [x] **步骤 6：提交共享执行器**

```bash
git add src/main/java/com/jdte/common/blockentities/UltimateTimeWandTargetRuntime.java src/main/java/com/jdte/common/blockentities/TimeAcceleratorBE.java src/main/java/com/jdte/common/blockentities/ExtendedTimeAccelerationManager.java src/main/java/com/jdte/common/integrations/ae2/ExtendedTimeAcceleratorAE2Integration.java src/test/java/com/jdte/common/blockentities/UltimateTimeWandTargetRuntimeTest.java
git commit -m "feat(顶级时间手杖): 共享有界目标执行与AE路由"
```

### 任务 3：实现手杖实体的生命周期、叠加和持久化

**文件：**

- 创建：`src/main/java/com/jdte/common/entities/UltimateTimeWandEntity.java`
- 创建：`src/test/java/com/jdte/common/entities/UltimateTimeWandEntityPersistenceTest.java`
- 修改：`src/main/java/com/jdte/setup/JDTEEntities.java`

- [x] **步骤 1：编写状态/NBT 失败测试**

```java
@Test
void persistedStateKeepsTargetExponentAndTimers() {
    WandState original = new WandState(new BlockPos(4, 5, 6), 10, 600, 477);
    CompoundTag tag = UltimateTimeWandEntity.saveState(original);
    assertEquals(original, UltimateTimeWandEntity.loadState(tag));
}

@Test
void mergeAddsStepAndHalfElapsedTimeWithoutPassingMaxExponent() {
    WandState merged = UltimateTimeWandEntity.merge(
            new WandState(BlockPos.ZERO, 4, 600, 400), 10, 10);
    assertEquals(10, merged.exponent());
    assertEquals(500, merged.remainingTime());
}
```

- [x] **步骤 2：运行测试确认失败**

运行：`.\gradlew.bat test --tests com.jdte.common.entities.UltimateTimeWandEntityPersistenceTest`

预期：FAIL，尚未有状态记录、稳定 NBT 键和合并方法。

- [x] **步骤 3：实现实体状态和注册类型**

定义 `WandState(BlockPos target, int exponent, int totalTime, int remainingTime)`，NBT 键固定为 `target`、`exponent`、`totalTime`、`remainingTime`；实体同步 `exponent`、`totalTime`、`remainingTime`，构造时把位置放在目标方块中心。实体默认持续 600 tick；`merge` 将指数加上模式步进并限制为 10，将已消耗时间 `(totalTime - remainingTime) / 2` 加回剩余时间并限制到 `totalTime`。在服务端每 tick 检查同维度目标、目标方块有效性、AE2 端点/普通 ticker有效性和剩余时间，失效或耗尽就移除；有效时把一个真实 tick 的有界工作提交到 `UltimateTimeWandTargetRuntime`，然后扣减一真实 tick。客户端只消费同步字段。

- [x] **步骤 4：运行持久化测试和编译**

运行：`.\gradlew.bat test --tests com.jdte.common.entities.UltimateTimeWandEntityPersistenceTest; .\gradlew.bat compileJava`

预期：状态往返、指数上限、半消耗时间补充和实体注册编译通过。

- [x] **步骤 5：提交实体生命周期**

```bash
git add src/main/java/com/jdte/common/entities/UltimateTimeWandEntity.java src/main/java/com/jdte/setup/JDTEEntities.java src/test/java/com/jdte/common/entities/UltimateTimeWandEntityPersistenceTest.java
git commit -m "feat(顶级时间手杖): 添加可持久化加速实体"
```

### 任务 4：实现物品操作、资源原子性和容器能力

**文件：**

- 创建：`src/main/java/com/jdte/common/items/UltimateTimeWandItem.java`
- 修改：`src/main/java/com/jdte/setup/JDTEDataComponents.java`、`src/main/java/com/jdte/setup/JDTEItems.java`、`src/main/java/com/jdte/setup/JDTECreativeTabs.java`
- 测试：`src/test/java/com/jdte/common/items/UltimateTimeWandDataTest.java`（补充资源/叠加纯逻辑测试）

- [x] **步骤 1：补充失败测试，先锁定原子资源流程**

```java
@Test
void insufficientResourcesDoNotChangeExistingState() {
    WandState before = new WandState(BlockPos.ZERO, 4, 600, 400);
    assertFalse(UltimateTimeWandData.canApply(before, Mode.MAX, 0, 0, 1, 1));
    assertEquals(before, UltimateTimeWandData.applyIfAffordable(before, Mode.MAX, 0, 0, 1, 1).state());
}

@Test
void existingMaxExponentIsNotChargedAgain() {
    OperationResult result = UltimateTimeWandData.planOperation(
            new WandState(BlockPos.ZERO, 10, 600, 300), Mode.MAX, 100000, 100000);
    assertFalse(result.success());
    assertEquals(0, result.fluidCost());
    assertEquals(0, result.energyCost());
}
```

- [x] **步骤 2：运行目标测试确认失败**

运行：`.\gradlew.bat test --tests com.jdte.common.items.UltimateTimeWandDataTest`

预期：FAIL，尚未存在原子操作计划类型。

- [x] **步骤 3：实现 `UltimateTimeWandItem` 的服务端操作顺序**

让物品实现 JDT `FluidContainingItem`，`getMaxMB()` 从 COMMON 配置返回 800000，FE 容量通过 JDT `PoweredItem` 能力返回 10000000。潜行右键只循环数据组件模式、显示当前模式并返回成功；普通右键先用 `MiscTools.isValidTickAccelBlock` 或 AE2 `hasTickable` 校验，再查找同一 `ServerLevel`/`BlockPos` 的 `UltimateTimeWandEntity`。新实体使用 600 tick；已有实体只允许指数真正增加且不超过 10。先用最终指数计算 JDT `Config.TIMEWAND_FLUID_COST`/`Config.TIMEWAND_RF_COST` 对应成本，流体保留小数结算，FE 用饱和乘法；创造模式跳过消耗但不跳过目标和实体状态校验。资源模拟成功后才修改实体并扣除资源，失败路径不生成、不延长、不扣费。右键方块不打开容器界面；填充继续走标准 `FluidContainingItem` capability。成功后播放 JDT 风格音效并同步实体数据。

- [x] **步骤 4：注册物品和数据组件，加入创造标签**

在 `JDTEDataComponents` 注册 `ultimate_time_wand_mode`（持久化字符串或枚举 codec，网络同步使用对应 stream codec）；在 `JDTEItems` 注册 `ULTIMATE_TIME_WAND`；在创造标签中展示空手杖。注册过程不引用 Dyna 包，也不修改任何 `JustDynaThings*` 集成类。

- [x] **步骤 5：运行物品逻辑和编译测试**

运行：`.\gradlew.bat test --tests com.jdte.common.items.UltimateTimeWandDataTest; .\gradlew.bat compileJava`

预期：原子资源测试通过，物品、组件、能力和注册均编译通过。

- [x] **步骤 6：提交物品操作**

```bash
git add src/main/java/com/jdte/common/items/UltimateTimeWandItem.java src/main/java/com/jdte/setup/JDTEDataComponents.java src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTECreativeTabs.java src/test/java/com/jdte/common/items/UltimateTimeWandDataTest.java
git commit -m "feat(顶级时间手杖): 添加物品操作与资源校验"
```

### 任务 5：添加客户端渲染、模型和语言资源

**文件：**

- 创建：`src/main/java/com/jdte/client/entityrenders/UltimateTimeWandRenderer.java`
- 修改：`src/main/java/com/jdte/client/JDTEClientSetup.java`
- 创建：`src/main/resources/assets/jdte/models/item/ultimate_time_wand.json`
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`、`src/main/resources/assets/jdte/lang/en_us.json`

- [ ] **步骤 1：实现客户端实体渲染器**

复制现有 `TimeAcceleratorEffectRenderer` 的无纹理进度条和六面文字布局，但让 renderer 类型改为 `UltimateTimeWandEntity`，倍率文字使用 `UltimateTimeWandData.multiplierForExponent`；对 `totalTime <= 0`、方块为空和剩余时间越界做安全裁剪，避免客户端同步半帧产生除零或负进度。

- [ ] **步骤 2：注册 renderer 和模型属性**

在 `JDTEClientSetup.registerRenderers` 注册 `JDTEEntities.ULTIMATE_TIME_WAND`，在客户端设置注册可选的模式/满度属性。不要让服务端类路径加载 `net.minecraft.client` 或 renderer 类。

- [ ] **步骤 3：添加模型和翻译**

模型优先复用 JDT 的时间手杖父模型；如果 JDT 父模型不可直接作为跨命名空间 parent，则复制其公开模型结构到 JDTE 资源并只替换纹理。中文显示名为“顶级时间手杖”，英文为“Ultimate Time Wand”；补充 Normal、2×、4×、Max、切换提示、资源不足、目标无效和最高倍率提示键，以及新配置键的中英文翻译。

- [ ] **步骤 4：运行资源/编译测试**

运行：`.\gradlew.bat compileJava; .\gradlew.bat test --tests com.jdte.setup.UltimateTimeWandConfigLanguageContractTest`

预期：客户端注册编译通过，语言契约测试确认所有新增 key 在中英文文件中均存在。

- [ ] **步骤 5：提交客户端资源**

```bash
git add src/main/java/com/jdte/client/entityrenders/UltimateTimeWandRenderer.java src/main/java/com/jdte/client/JDTEClientSetup.java src/main/resources/assets/jdte/models/item/ultimate_time_wand.json src/main/resources/assets/jdte/lang/zh_cn.json src/main/resources/assets/jdte/lang/en_us.json
git commit -m "feat(顶级时间手杖): 添加客户端显示资源"
```

### 任务 6：添加配方、GuideME 文档和注册契约

**文件：**

- 创建：`src/main/resources/data/jdte/recipe/ultimate_time_wand.json`
- 创建：`src/main/resources/assets/jdte/guides/jdte/guide/ultimate-time-wand.md`
- 创建：`src/main/resources/assets/jdte/guides/jdte/guide/_en_us/ultimate-time-wand.md`
- 创建/修改：`src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/zh_cn/entries/jdte/ultimate-time-wand.json`、`src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/en_us/entries/jdte/ultimate-time-wand.json`及对应索引
- 创建：`src/test/java/com/jdte/setup/UltimateTimeWandConfigLanguageContractTest.java`

- [ ] **步骤 1：编写注册契约失败测试**

```java
@Test
void itemAndRecipeUseJdteNamespaceWithoutDynaReferences() throws IOException {
    assertTrue(Files.exists(Path.of("src/main/resources/data/jdte/recipe/ultimate_time_wand.json")));
    String source = Files.readString(Path.of("src/main/java/com/jdte/common/items/UltimateTimeWandItem.java"));
    assertFalse(source.contains("justdynthings"));
    assertFalse(source.contains("com.direwolf20.justdynathings"));
}
```

- [ ] **步骤 2：创建配方和文档**

配方使用 JDTE 现有 `time_multitool`/`ultimate_portal_gun` 已采用的高阶材料体系：JDT 蚀空合金 Paxel、`jdte:big_fluid_tank`、`jdte:time_fluid_catalyst` 和 `jdte:ae_acceleration_upgrade`，结果为 `jdte:ultimate_time_wand`；不得写入任何 Dyna 物品 ID。文档明确列出 800000 mB、10000000 FE、四种模式、潜行切换、普通右键、实体叠加、1024×上限、AE2 直接加速、资源不足不扣除和服务端安全规则。

- [ ] **步骤 3：运行资源契约和文档检查**

运行：`.\gradlew.bat test --tests com.jdte.setup.UltimateTimeWandConfigLanguageContractTest; python scripts/test_validate_docs.py`

预期：物品/配方命名空间、翻译、GuideME 页面和既有文档校验通过；若既有 Patchouli 资源漂移导致基线校验失败，只记录具体基线失败文件，不修改与本功能无关的文档。

- [ ] **步骤 4：提交配方和文档**

```bash
git add src/main/resources/data/jdte/recipe/ultimate_time_wand.json src/main/resources/assets/jdte/guides src/main/resources/assets/justdirethings/patchouli_books src/test/java/com/jdte/setup/UltimateTimeWandConfigLanguageContractTest.java
git commit -m "feat(顶级时间手杖): 添加配方与指南"
```

### 任务 7：全量验证、源码依赖审计和交付前审查

**文件：**

- 修改：仅在测试揭示本功能回归时修改对应实现/测试文件。
- 审计：所有新增手杖文件、注册文件、资源文件以及 `JustDynaThings*` 集成未改动状态。

- [ ] **步骤 1：运行新增测试和完整测试**

运行：`.\gradlew.bat test --tests com.jdte.common.items.UltimateTimeWandDataTest --tests com.jdte.common.blockentities.UltimateTimeWandTargetRuntimeTest --tests com.jdte.common.entities.UltimateTimeWandEntityPersistenceTest --tests com.jdte.setup.UltimateTimeWandConfigLanguageContractTest`

预期：新增测试全部 PASS。

运行：`.\gradlew.bat test`

预期：完整 JUnit 套件 PASS，或若失败只出现已在基线记录中的失败。

- [ ] **步骤 2：运行编译和静态依赖审计**

运行：`.\gradlew.bat compileJava; rg -n "Dyna|dyna|justdynathings|com\.direwolf20\.justdynathings" src/main/java/com/jdte/common/items/UltimateTimeWand* src/main/java/com/jdte/common/entities/UltimateTimeWand* src/main/java/com/jdte/common/blockentities/UltimateTimeWand* src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTEEntities.java`

预期：编译成功，新增手杖实现和注册代码无 Dyna 导入、继承或调用；JDT 和 AE2 引用均为已声明依赖/API。

- [ ] **步骤 3：检查差异范围和资源完整性**

运行：`git diff --check; git status --short; git diff main...HEAD --stat`

确认只包含本计划定义的实现、测试、配置、注册和资源；不覆盖主工作区的用户改动，不修改 Dyna 集成，不替换 JDT 原版 `time_wand`，不添加 Curios 槽位。

- [ ] **步骤 4：提交最终验证修复并请求代码审查**

```bash
git add -u
git add src/main/java/com/jdte/common/items src/main/java/com/jdte/common/entities src/main/java/com/jdte/common/blockentities src/main/java/com/jdte/setup src/main/java/com/jdte/client src/main/resources src/test/java/com/jdte
git commit -m "test(顶级时间手杖): 完成集成验证"
```

然后使用 requesting-code-review 技能检查：模式/成本/实体叠加、AE2 不双算、失败原子性、NBT 重启恢复、无 Dyna 运行时依赖和 1024× 有界执行。

---

## 规格覆盖自检

- 操作逻辑：任务 1、3、4 覆盖模式、右键新建/叠加、最高指数和失败不扣费。
- 实体生命周期：任务 3 覆盖目标、计时、维度/目标失效、同步和 NBT。
- 普通目标执行：任务 2、3 覆盖 JDT 有效性、ticker、随机刻、共享预算、待执行工作和 1024× 有界执行。
- AE2：任务 2、3、4 覆盖公开 `IGridTickable`、SLEEP、未安装回退、直接启用和避免普通路径重复执行。
- 资源与安全：任务 1、4 覆盖配置、饱和 FE、小数流体、创造模式和服务端原子性。
- 注册与资源：任务 3、4、5、6 覆盖物品、实体、数据组件、创造标签、渲染器、模型、语言、配方和 GuideME。
- 非目标：任务 4、6、7 明确不替换 JDT 手杖、不改 Dyna、不扩展普通手杖 AE、不增加 Curios。

## 计划自检

- 每个步骤都给出了具体文件、命令、预期结果或可直接复制的代码契约。
- 测试类型、方法名和字段名在前后任务中保持一致：`Mode`、`WandState`、`OperationResult`、`UltimateTimeWandTargetRuntime.admit/route/execute`。
- 计划按依赖顺序拆分：纯逻辑 → 共享执行 → 实体 → 物品 → 客户端 → 资源文档 → 全量验证。
