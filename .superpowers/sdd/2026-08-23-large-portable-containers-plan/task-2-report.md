# 任务 2 报告：大型物品、注册和能力

## 状态

已完成任务 2 的范围内实现，并在指定 worktree 内完成验证与提交准备。

## 实际修改文件

- `src/main/java/com/jdte/common/items/LargePocketGeneratorItem.java`
- `src/main/java/com/jdte/common/items/LargePotionCanisterItem.java`
- `src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java`
- `src/main/java/com/jdte/setup/JDTEItems.java`
- `src/main/java/com/jdte/setup/JDTECreativeTabs.java`
- `src/main/java/com/jdte/JDTE.java`
- `src/test/java/com/jdte/common/items/LargePortableContainerLogicTest.java`

未修改：

- 菜单
- 网络
- Curios 资源
- 客户端界面
- 主工作区 `D:\MCDev\idea-projects\JDTE`

## TDD 记录

### RED

先修改 `LargePortableContainerLogicTest`，新增以下断言：

- 三个新 holder：`large_pocket_generator`、`large_potion_canister`、`large_fuel_canister`
- 三个大型物品类型存在
- 默认 `stack size = 1`
- 大型发电机容量按 4 倍规则暴露公共容量方法

执行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

结果：`compileTestJava FAILED`

关键信息：

- `JDTEItems.LARGE_POCKET_GENERATOR` / `LARGE_POTION_CANISTER` / `LARGE_FUEL_CANISTER` 不存在
- `LargePocketGeneratorItem` / `LargePotionCanisterItem` / `LargeFuelCanisterItem` 不存在

这一步确认了测试先红，失败原因与任务 2 缺口一致。

### GREEN

随后补充最小实现：

- 新建三个大型物品类
- 在 `JDTEItems` 中注册 3 个 holder
- 在 `JDTECreativeTabs` 中加入创造标签展示
- 在 `JDTE#registerCapabilities` 中仅为 `large_pocket_generator` 注册 FE 能力，容量使用 `POCKET_GENERATOR_MAX_FE * 4`
- 将测试改为通过 `JDTEItems` 注册实例验证类型与堆叠，避免在冻结注册表阶段手工 `new Item`
- 将大型发电机的可单测容量规则提取为 `LargePocketGeneratorItem.getScaledMaxEnergy(int)`

再次执行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

结果：`BUILD SUCCESSFUL`

## 验证输出

### 目标测试

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
BUILD SUCCESSFUL in 21s
```

### 全量测试

```text
./gradlew test
BUILD SUCCESSFUL in 18s
```

### 编译

```text
./gradlew compileJava
BUILD SUCCESSFUL in 13s
```

说明：

- `compileJava` 过程中仍有基线已有的 deprecation/removal 警告
- 没有新增错误

## 自审

- 大型发电机保留原版 `PocketGenerator` 未改动，通过子类覆写 `getMaxEnergy()` 与 JDTE 侧 item FE capability 注册实现 4 倍容量。
- 原版 JDT 三个物品类未被修改；新增行为全部落在 JDTE 新类与注册层。
- 创造标签只追加新物品，不改变现有顺序结构之外的内容。
- `LargeFuelCanisterItem` 提供了 JDTE 侧放大后的最大燃料、最小消耗与燃料倍率辅助逻辑，但没有扩散到其它非本任务文件。
- 测试保持在 `LargePortableContainerLogicTest` 内，直接验证公共容量逻辑与注册结果，没有依赖 GUI mock。

## 疑虑

1. `LargePotionCanisterItem` 与 `LargeFuelCanisterItem` 当前只完成本任务要求的物品类与注册/能力边界，`use` 仍委托原版入口；后续任务若引入大型专属菜单，需要继续把菜单构造切到新的容器。
2. 这次没有在三个大型物品类上直接实现 `ICurioItem`。原因是当前 `compileTestJava` classpath 不带 Curios API，直接实现会让本任务的测试阶段失败；如果后续确实需要通过接口而不是标签/其它契约接入 Curios，需要在后续任务一并处理测试 classpath 或改为集成测试路径验证。

## 审查修复追加（2026-08-23）

### 修复范围

本轮只修复审查指出的两条真实接线问题，并更新测试：

- `LargePotionCanisterItem` 补充真实公共容量入口
- `LargeFuelCanisterItem` 补充可被现有调用复用的容量/最小消耗/倍率入口
- `GeneratorUpgradeHelper` 在大型燃料罐分支调用大型逻辑，原版 `FuelCanister` 分支保持不变
- `LargePortableContainerLogicTest` 直接断言大型药水与大型燃料的公共行为入口

未实现：

- 菜单
- 网络
- Curios 资源
- 客户端界面

### 第二轮 TDD

#### RED

先补测试，新增以下断言：

- `LargePotionCanisterItem.getPotionCapacityMb()` 返回 `4000`
- `JDTEItems.LARGE_POTION_CANISTER.get().getCapacityMb()` 返回 `4000`
- `LargeFuelCanisterItem.getMaxFuelLevel(int)` 走 4 倍容量
- `LargeFuelCanisterItem.getMinimumFuelConsumed(int)` 走 10 倍最小消耗
- `LargeFuelCanisterItem.getBurnSpeedMultiplier(ItemStack)` 返回原版倍率的 10 倍

执行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

结果：`compileTestJava FAILED`

关键信息：

- `LargePotionCanisterItem.getPotionCapacityMb()` / `getCapacityMb()` 不存在
- `LargeFuelCanisterItem.getMaxFuelLevel(int)` / `getMinimumFuelConsumed(int)` 不存在

这一步确认审查指出的“helper 未接线”问题在测试层可复现。

#### GREEN

最小实现如下：

- `LargePotionCanisterItem` 将原先无效的静态 `getMaxMB()` 改为真实公共入口：
  - `getPotionCapacityMb()`
  - `getCapacityMb()`
- `LargeFuelCanisterItem` 增加：
  - `getMaxFuelLevel(int)`
  - `getMinimumFuelConsumed(int)`
  - `getBurnSpeedMultiplier(int)`
  - `getBurnSpeedMultiplier(ItemStack)` 改为原版倍率再乘 10
- `GeneratorUpgradeHelper`：
  - `burnSpeedMultiplier` 先判断 `LargeFuelCanisterItem`
  - `consumeFuel` 在大型燃料罐剩余物分支调用 `LargeFuelCanisterItem.decrementFuel`

再次执行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

结果：`BUILD SUCCESSFUL in 19s`

### 审查修复后的验证

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
BUILD SUCCESSFUL in 19s
```

```text
./gradlew test
BUILD SUCCESSFUL in 27s
```

```text
./gradlew compileJava
BUILD SUCCESSFUL in 1s
```

### 审查修复后的自审

- 审查指出的两个“静态 helper 没有运行时接线”的问题都已修复。
- `LargePotionCanisterItem` 的 4000 mB 入口现在是显式公共 API，不再伪装成无效的静态覆写。
- `LargeFuelCanisterItem` 的 4 倍上限、10 倍最小消耗和 10 倍倍率入口都可直接单测。
- `GeneratorUpgradeHelper` 已改为在大型燃料罐分支调用大型逻辑，同时保留原版 `FuelCanister` 行为不变。
- 尝试为 `GeneratorUpgradeHelper` 写独立运行期测试时，测试类/反射路径都会被 Mixin 包保护拦截；因此本轮保留生产修复，但测试聚焦到用户明确要求的物品公共入口。
