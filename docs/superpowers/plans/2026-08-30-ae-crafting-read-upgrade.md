# AE 合成读取升级实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增可通过 AE2 无线接入点绑定的通用升级，使安装它的机器仅在绑定网络存在正在执行的合成任务时运行。

**架构：** 升级使用现有 `UpgradeType`/`UpgradeCardItem`/标准或扩展 handler。AE2 绑定和 CPU 状态读取隔离在 optional integration facade 中，使用 `WIRELESS_LINK_TARGET` 与 `IWirelessAccessPoint` 公共 API，并通过短周期服务端缓存复用结果。运行许可作为独立策略接入通用 JDT 工作边界和 JDTE 自有状态机副作用边界，不改变原有红石或 `canRun()` 语义。

**技术栈：** Java 21、NeoForge 21.1、AE2 19.2.17 public API、JUnit Jupiter、现有 Gradle ModDev 测试配置。

---

## 文件清单与职责

- 创建：`src/main/java/com/jdte/common/integrations/ae2/AE2CraftingReadNetwork.java`，AE2 可选 facade，负责注册 GridLinkables、解析绑定接入点、读取并缓存网络合成状态。
- 创建：`src/main/java/com/jdte/common/integrations/ae2/AE2CraftingReadNetworkIntegration.java`，只在 AE2 已加载时编译/加载的 API 实现；如现有 `AEOutputNetwork` 结构更适合，可合并到该 facade 对应实现，但不得让非 AE2 路径主动解析 AE2 类。
- 修改：`src/main/java/com/jdte/common/upgrades/UpgradeType.java`，增加 `AE_CRAFTING_READ`，序列化名 `ae_crafting_read`，上限 1。
- 修改：`src/main/java/com/jdte/setup/JDTEItems.java`，注册物品并加入普通升级列表。
- 修改：`src/main/java/com/jdte/common/upgrades/UpgradeHelper.java`，增加计数和运行许可查询入口。
- 修改：`src/main/java/com/jdte/mixin/BaseMachineBEMixin.java`、必要的通用 JDT mixin/目标方法，接入普通机器工作边界。
- 修改：相关 JDTE 自有机器 tick/处理类，至少覆盖 `GreenhouseBE`、`LifeSynthesisVatBE`、`MineralExtractorBE`、`TimeFreezerBE`，并按实际代码确认传输/生产机器的最小副作用入口。
- 修改：`src/main/java/com/jdte/JDTE.java`，在 common setup 中按现有 AE2 facade 模式注册绑定 handler。
- 创建：`src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java`，纯策略、绑定组件、缓存和失效行为测试。
- 创建或修改：代表性机器测试文件，验证无任务不推进/不扣费，恢复任务后继续。
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`、`zh_cn.json`，物品名称和 tooltip。
- 创建：`src/main/resources/assets/jdte/models/item/ae_crafting_read_upgrade.json` 和对应纹理 `src/main/resources/assets/jdte/textures/item/ae_crafting_read_upgrade.png`。
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/upgrades.md`、`_en_us/upgrades.md`，说明无线接入点绑定和运行条件；必要时更新 README/CHANGELOG。

### 任务 1：AE2 网络读取 facade 与缓存

**文件：**
- 创建：`src/main/java/com/jdte/common/integrations/ae2/AE2CraftingReadNetwork.java`
- 创建：`src/main/java/com/jdte/common/integrations/ae2/AE2CraftingReadNetworkIntegration.java`
- 修改：`src/main/java/com/jdte/JDTE.java`
- 测试：`src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java`

- [x] **步骤 1：编写失败测试**

测试 facade 的纯状态决策：未绑定、目标未加载、接入点不 active、网络 booting、CPU 非 busy/无 job 返回 false；至少一个 CPU `isBusy()` 且 `getJobStatus()` 非 null 返回 true；同一绑定位置在缓存窗口内复用结果，绑定位置或网格身份变化后重新读取。测试不得依赖 AE2 内部实现。

- [x] **步骤 2：运行测试确认失败**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest`
预期：FAIL，因为 facade 和状态快照尚不存在。

- [x] **步骤 3：实现最小 facade**

公开一个服务端查询入口，例如：

```java
public static boolean hasActiveCraftingTask(ServerLevel level, ItemStack upgrade);
```

实现只从 `AEComponents.WIRELESS_LINK_TARGET` 读取 `GlobalPos`，通过 `ServerLevel#getLevel` 和已加载方块实体取得 `IWirelessAccessPoint`，检查 `isActive()`、`getGrid()`、`grid.getPathingService().isNetworkBooting()` 和 `grid.getCraftingService().getCpus()`。使用 CPU 的 `isBusy()` 与非空 `getJobStatus()` 判断任务。缓存键至少包含目标 `GlobalPos` 与网格对象身份，缓存值包含结果和过期 tick；所有失败分支返回 false。将 `GridLinkables.register` handler 的 `canLink/link/unlink` 限定为新升级物品，并通过 `AEComponents.WIRELESS_LINK_TARGET` 写入/移除绑定。

- [x] **步骤 4：运行测试确认通过**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest`
预期：PASS；随后运行 `./gradlew compileJava` 验证 AE2 19.2.17 方法签名和 optional 类隔离。

- [x] **步骤 5：Commit**

```bash
git add src/main/java/com/jdte/common/integrations/ae2 src/main/java/com/jdte/JDTE.java src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java
git commit -m "feat: add AE crafting status integration"
```

### 任务 2：注册通用升级卡和资源

**文件：**
- 修改：`src/main/java/com/jdte/common/upgrades/UpgradeType.java`
- 修改：`src/main/java/com/jdte/setup/JDTEItems.java`
- 修改：`src/main/java/com/jdte/common/upgrades/UpgradeHelper.java`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`
- 创建：`src/main/resources/assets/jdte/models/item/ae_crafting_read_upgrade.json`
- 创建：`src/main/resources/assets/jdte/textures/item/ae_crafting_read_upgrade.png`
- 测试：`src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java`

- [x] **步骤 1：编写失败测试**

断言 `UpgradeType.AE_CRAFTING_READ` 的序列化名为 `ae_crafting_read`、上限为 1，注册物品使用 `UpgradeCardItem`，并且标准/扩展 handler 可接受该卡但第二张被拒绝。

- [x] **步骤 2：运行测试确认失败**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest`
预期：FAIL，因为枚举项、物品注册和资源键尚不存在。

- [x] **步骤 3：实现注册与资源**

按现有升级卡模式加入 enum、DeferredRegister、`upgrades()` 列表、英文/中文文本、模型和实际 PNG 纹理。兼容性判断只增加该通用卡，不改变现有互斥规则；用现有 `getUpgradeCount`/handler 上限逻辑拒绝重复安装。增加 `UpgradeHelper.hasAeCraftingReadUpgrade(BaseMachineBE)` 或等价纯查询方法，供后续运行入口使用。

- [x] **步骤 4：运行测试确认通过**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest && ./gradlew compileJava`
预期：PASS，资源路径和 Java 编译均成功。

- [x] **步骤 5：Commit**

```bash
git add src/main/java/com/jdte/common/upgrades src/main/java/com/jdte/setup/JDTEItems.java src/main/resources/assets/jdte/lang src/main/resources/assets/jdte/models/item/ae_crafting_read_upgrade.json src/main/resources/assets/jdte/textures/item/ae_crafting_read_upgrade.png src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java
git commit -m "feat: register AE crafting read upgrade"
```

### 任务 3：通用机器运行许可

**文件：**
- 修改：`src/main/java/com/jdte/common/upgrades/UpgradeHelper.java`
- 修改：`src/main/java/com/jdte/mixin/BaseMachineBEMixin.java`
- 修改：必要的现有 JDT 运行边界 mixin（以编译后的目标方法为准）
- 测试：`src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java` 和一个代表性普通机器测试

- [x] **步骤 1：编写失败测试**

增加一个可注入查询替身，验证：无升级不改变原逻辑；升级未绑定/无任务返回 deny；有任务返回 allow；deny 不会绕过原有红石关闭状态，也不清空机器已有库存/进度；同一 tick 的 overclock 二次调用不会重复消耗资源。

- [x] **步骤 2：运行测试确认失败**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest`
预期：FAIL，因为机器运行入口尚未组合 AE 许可。

- [x] **步骤 3：实现运行策略接入**

新增独立策略方法，例如：

```java
public static boolean mayRunWithUpgrades(BaseMachineBE machine) {
    return !hasAeCraftingReadUpgrade(machine)
        || AE2CraftingReadNetwork.hasActiveCraftingTask(machine.getLevel(), machine.getUpgradeStack());
}
```

实际签名必须遵循现有 handler/attachment 访问方式。不要在基类 HEAD 无条件跳过 `tickServer`；在能量消耗、生成/传输、或现有运行许可目标方法中合并策略，保留 off-state reset。Auto I/O 在机器被许可关闭时不得继续执行，且任何新 hook 都要排除非服务器和不存在的升级 attachment 情况。

- [x] **步骤 4：运行测试确认通过**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadUpgradeTest && ./gradlew compileJava`
预期：PASS；确认不改变无升级机器行为。

- [x] **步骤 5：Commit**

```bash
git add src/main/java/com/jdte/common/upgrades src/main/java/com/jdte/mixin src/test/java/com/jdte/common/upgrades/AECraftingReadUpgradeTest.java
git commit -m "feat: gate machine work on AE crafting"
```

### 任务 4：JDTE 自有状态机与管理器接入

**文件：**
- 修改：`src/main/java/com/jdte/common/blockentities/GreenhouseBE.java`
- 修改：`src/main/java/com/jdte/common/blockentities/LifeSynthesisVatBE.java`
- 修改：`src/main/java/com/jdte/common/blockentities/MineralExtractorBE.java`
- 修改：`src/main/java/com/jdte/common/blockentities/TimeFreezerBE.java`
- 修改：必要的生产/传输机器类（仅当其工作副作用不受任务 3 覆盖）
- 测试：对应现有测试或新增 `AECraftingReadMachineBehaviorTest.java`

- [x] **步骤 1：编写失败测试**

分别覆盖至少一个状态机入口：无任务时 Greenhouse/Life Synthesis 不 capture、advance、settle 或扣费；Mineral 不推进 transient/flush；Time Freezer 关闭其 active manager；任务恢复后再次允许处理。测试同时断言原有红石 off 的 reset/deactivate 语义仍然成立。

- [x] **步骤 2：运行测试确认失败**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadMachineBehaviorTest`
预期：FAIL，因为自有机器尚未调用 AE 运行策略。

- [x] **步骤 3：在副作用边界接入**

每台机器保留其原先的红石和资源条件，增加短路条件到实际副作用前：capture/advance/settle、transient/flush、资源扣除、队列提交、外部 manager activate/deactivate。禁止运行时保留进度和输入，不执行自动 I/O；从 deny 恢复后继续原有状态。避免把查询放在会破坏 reset 的最外层 ticker，也避免重复读取网络状态。

- [x] **步骤 4：运行测试确认通过**

运行：`./gradlew test --tests com.jdte.common.upgrades.AECraftingReadMachineBehaviorTest && ./gradlew compileJava`
预期：PASS，四类自有状态机关闭/恢复行为正确。

- [x] **步骤 5：Commit**

```bash
git add src/main/java/com/jdte/common/blockentities src/test/java/com/jdte/common/upgrades/AECraftingReadMachineBehaviorTest.java
git commit -m "feat: gate JDTE state machines on AE crafting"
```

### 任务 5：文档、资源校验与全量验证

**文件：**
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/upgrades.md`
- 修改：`src/main/resources/assets/jdte/guides/jdte/guide/_en_us/upgrades.md`
- 修改：必要时 `README.md`、`README_EN.md`、`CHANGELOG.md`、`AGENTS.md`

- [x] **步骤 1：更新文档**

说明升级名称、标准/扩展槽兼容性、将升级放入 AE2 Wireless Access Point linking 输入绑定、未绑定/无任务时机器停止、网络恢复后自动继续，以及 AE2 optional 依赖行为。中英文页面保持现有 GuideME 结构。

- [x] **步骤 2：运行资源与编译验证**

运行：`./gradlew compileJava test`
预期：编译成功，全部 JUnit 测试通过。

- [x] **步骤 3：检查工作树与资源引用**

运行：`git diff --check` 和资源路径检查，确认模型引用的纹理存在、语言键与物品注册名一致、未修改用户已有无关改动。

- [x] **步骤 4：Commit**

```bash
git add src/main/resources/assets/jdte/guides README.md README_EN.md CHANGELOG.md AGENTS.md
git commit -m "docs: document AE crafting read upgrade"
```

## 最终验证

- [x] `./gradlew compileJava`
- [x] `./gradlew test`
- [x] `git diff --check`
- [x] 检查 AE2 环境和非 AE2 环境的类加载边界
- [x] 检查无线接入点绑定/解绑、重启后组件持久化、目标未加载和网络 booting 时均安全返回 false
- [x] 检查无升级机器以及原有红石模式行为无回归
