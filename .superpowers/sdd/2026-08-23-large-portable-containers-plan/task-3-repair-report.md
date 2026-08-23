# Task 3 repair report — reviewer findings follow-up

日期：2026-08-23

状态：DONE

## 修复点

1. `LargePocketGeneratorScreen`
   - 按 JDT 原 screen 语义补回燃烧进度条与能量条渲染。
   - 每帧从真实来源重新刷新当前绑定栈，而不是只读初始绑定栈。
   - 恢复 JDT 原燃料 tooltip 的 `burnspeedmultiplier` 逻辑。

2. 手持打开绑定
   - 三个大型便携容器的手持打开统一改为绑定主手物品。
   - 不再依赖触发 `InteractionHand` 决定绑定来源。
   - Curios 快捷键打开仍保留服务端按槽位重新查找真实物品的流程。

3. 测试覆盖
   - 新增对按键请求生成、服务端 Curios 重新查找、Curios 缺失安全、主手绑定语义的真实断言。
   - 新增对 Pocket Generator 原 screen 关键公式和 tooltip 分支的断言。

## 本次变更文件

### 生产代码

- `src/main/java/com/jdte/client/LargePortableContainerClientEvents.java`
- `src/main/java/com/jdte/client/screens/LargePocketGeneratorScreen.java`
- `src/main/java/com/jdte/common/containers/LargePocketGeneratorContainer.java`
- `src/main/java/com/jdte/common/containers/LargePortableContainerBinding.java`
- `src/main/java/com/jdte/common/containers/LargePortableContainerMenus.java`
- `src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java`
- `src/main/java/com/jdte/common/items/LargePocketGeneratorItem.java`
- `src/main/java/com/jdte/common/items/LargePotionCanisterItem.java`

### 新增测试

- `src/test/java/com/jdte/common/containers/LargePortableContainerFlowTest.java`
- `src/test/java/com/jdte/client/screens/LargePocketGeneratorScreenTest.java`

## 验证证据

以下命令均在当前修复代码上重新串行执行：

1. 聚焦测试

   `.\gradlew test --tests com.jdte.common.containers.LargePortableContainerFlowTest --tests com.jdte.client.screens.LargePocketGeneratorScreenTest`

   结果：`BUILD SUCCESSFUL in 15s`

2. 完整测试

   `.\gradlew test`

   结果：`BUILD SUCCESSFUL in 16s`

3. Java 编译

   `.\gradlew compileJava`

   结果：`BUILD SUCCESSFUL in 1s`

## 对 reviewer 三条意见的对应说明

### (1) Pocket Generator screen

- 已按 JDT 原字节码行为恢复：
  - 燃烧条高度：`counter * 13 / maxBurn`
  - 能量条高度：`energy * 70 / maxEnergy`
  - 燃料 tooltip 中 `burnspeedmultiplier` 的判定顺序：
    - `Coal_T1`
    - `CoalBlock_T1`
    - `FuelCanister`
- screen 每帧调用当前来源解析，而不是只看初始绑定对象。

### (2) 手持打开主手绑定

- 三个 `use(...)` 都已统一改成调用“主手打开”入口。
- `LargePortableContainerMenus` 的手持解析只接受主手匹配项；即使由副手触发，也不会绑定副手的大容器。

### (3) 真实断言

- `LargePortableContainerFlowTest`
  - 断言副手触发时仍解析主手
  - 断言主手不匹配时不会错误回退到副手
  - 断言服务端 Curios 解析使用精确槽位名
  - 断言 Curios 缺失时不会访问槽位查询
  - 断言 client 热键只为有效场景生成对应 payload
- `LargePocketGeneratorScreenTest`
  - 断言燃烧条公式
  - 断言能量条公式
  - 断言 Fuel Canister tooltip 走 JDT 原 multiplier 逻辑

---

## Re-review 2 repair（Important findings follow-up）

状态：DONE

### 本轮修复点

1. 客户端 `LargePocketGenerator` live source 解析
   - 新增 `LargePortableContainerSource`，把菜单绑定来源显式编码为：
     - 主手
     - 精确 Curios 槽位
   - 菜单打开包现在同时下发“初始展示栈 + 绑定来源”。
   - 三个大型容器 client menu 构造都改为基于来源创建 client binding：
     - 主手每帧重新取 `player.getMainHandItem()`
     - Curios 每帧按精确槽位重新查找
   - Curios 在客户端缺失时安全回退到打开包里的解码栈副本，不崩溃、不改变服务端 source-of-truth。

2. Pocket Generator tooltip 的 Shift 语义
   - `LargePocketGeneratorScreen` 提取 `energyTooltipValues(...)`。
   - 非 Shift：当前值/最大值都保留 `MagicHelpers.withSuffix(...)`。
   - 按住 Shift：当前值/最大值都改为 `MagicHelpers.formatted(...)`，确保最大能量不再缩写。

### 本轮实际改动文件

#### 生产代码

- `src/main/java/com/jdte/client/screens/LargePocketGeneratorScreen.java`
- `src/main/java/com/jdte/common/containers/LargeFuelCanisterContainer.java`
- `src/main/java/com/jdte/common/containers/LargePocketGeneratorContainer.java`
- `src/main/java/com/jdte/common/containers/LargePortableContainerMenus.java`
- `src/main/java/com/jdte/common/containers/LargePortableContainerSource.java`
- `src/main/java/com/jdte/common/containers/LargePotionCanisterContainer.java`

#### 测试

- `src/test/java/com/jdte/client/screens/LargePocketGeneratorScreenTest.java`
- `src/test/java/com/jdte/common/containers/LargePortableContainerFlowTest.java`

### 回归断言

- `LargePortableContainerFlowTest`
  - `clientSourceResolverPrefersTheLiveMainHandStackOverTheDecodedCopy`
  - `clientSourceResolverUsesTheExactLiveCuriosSlotWhenPresent`
- `LargePocketGeneratorScreenTest`
  - `energyTooltipUsesFullFormattingForBothValuesWhenShiftIsHeld`
  - `energyTooltipUsesSuffixFormattingForBothValuesWhenShiftIsNotHeld`

### 验证证据

以下命令已在本轮修复完成后串行重跑：

1. 聚焦测试

   `.\gradlew test --tests com.jdte.common.containers.LargePortableContainerFlowTest --tests com.jdte.client.screens.LargePocketGeneratorScreenTest`

   结果：`BUILD SUCCESSFUL in 21s`

2. 完整测试

   `.\gradlew test`

   结果：`BUILD SUCCESSFUL in 16s`

3. Java 编译

   `.\gradlew compileJava`

   结果：`BUILD SUCCESSFUL in 1s`
