# 大型便携容器实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在不改变 JDT 原版三个便携容器的前提下，新增 4 倍容量的大型便携发电机、药水罐和燃料罐，支持三个独立 Curios 槽位、无损升级配方以及饰品栏快捷键打开界面。

**架构：** 大型物品使用独立物品类和容量逻辑，不修改 JDT 原版静态容量方法；大型菜单直接绑定玩家对应 Curios 槽位中的实际 `ItemStack`，手持和饰品栏打开共用同一套数据处理。客户端快捷键只发送容器种类，服务端重新解析 Curios 槽位后创建菜单。纯容量/批量/倍率规则抽到可单测的 JDTE 工具类，避免测试依赖客户端界面。

**技术栈：** Java 21、NeoForge ModDev、Minecraft 1.21.1、Curios API（可选依赖）、NeoForge `MenuType`/payload、Minecraft Data Components、JUnit 5。

---

## 全局约束

- 工作目录是 `D:/MCDev/idea-projects/JDTE/.worktrees/large-portable-containers`，不要修改主工作区已有的 Gel Generator、Loot Fabricator、Just Dyna Things 或未跟踪文件。
- 不修改 JDT 原版 `pocket_generator`、`potion_canister`、`fuel_canister` 的注册、配方、容量和静态行为。
- 大型便携发电机默认容量为 JDT `POCKET_GENERATOR_MAX_FE` 的 4 倍；大型药水罐固定为 4,000 mB；大型燃料罐为 JDT `FUEL_CANISTER_MAXIMUM_FUEL` 的 4 倍。
- 大型药水罐每次必须消耗 4 瓶相同药水并增加 1,000 mB；不足 4 瓶、配方不一致或空间不足时不得消耗输入。
- 大型燃料罐的最小消耗刻数和向 JDT 发电机提供的燃烧倍率均为原版规则的 10 倍；默认最小消耗为 2,000 刻。
- Curios 未加载时，类加载和服务器启动不能失败；手持交互仍可用。
- 三个大型物品分别拥有 `large_pocket_generator`、`large_potion_canister`、`large_fuel_canister` 单槽位，并可同时装备。
- 快捷键请求必须由服务端按固定容器种类重新查找 Curios 槽位，客户端不得提交任意容器 `ItemStack`。
- 通过升级配方转换物品时保留相关 JDT Data Components；不把能量、燃料、药水效果、燃料速度丢掉。
- 每个任务先写能失败的测试并运行确认失败，再写最少生产代码；每个任务完成后提交，提交前运行该任务相关测试。

## 文件清单与职责

创建：

- `src/main/java/com/jdte/common/items/LargePortableContainerLogic.java`：容量、药水四瓶批量、燃料 10 倍规则等纯服务端规则。
- `src/main/java/com/jdte/common/items/LargePocketGeneratorItem.java`：大型发电机容量、燃料识别和 JDT 发电逻辑适配。
- `src/main/java/com/jdte/common/items/LargePotionCanisterItem.java`：大型药水容量、4 瓶批量处理、手持交互和 tooltip。
- `src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java`：大型燃料上限、10 倍燃料结算、手持交互和 tooltip。
- `src/main/java/com/jdte/common/containers/LargePocketGeneratorContainer.java`：大型发电机实际物品数据容器和 Curios 槽位有效性。
- `src/main/java/com/jdte/common/containers/LargePotionCanisterContainer.java`：大型药水罐实际物品数据容器和四瓶批处理输入槽。
- `src/main/java/com/jdte/common/containers/LargeFuelCanisterContainer.java`：大型燃料罐实际物品数据容器和燃料返还逻辑。
- `src/main/java/com/jdte/common/network/data/OpenLargePortableContainerPayload.java`：快捷键打开请求，仅编码容器枚举。
- `src/main/java/com/jdte/common/network/handler/OpenLargePortableContainerPacket.java`：服务端 Curios 槽位查找、权限校验和菜单打开。
- `src/main/java/com/jdte/common/recipes/LargePortableContainerRecipe.java`：对应 JDT 原版物品到大型物品的无损升级配方。
- `src/test/java/com/jdte/common/items/LargePortableContainerLogicTest.java`：规则级红绿测试。
- `src/test/java/com/jdte/common/recipes/LargePortableContainerRecipeTest.java`：无损组件转换和材料匹配测试。
- `src/main/resources/data/curios/curios/slots/large_pocket_generator.json`、`large_potion_canister.json`、`large_fuel_canister.json`：Curios 单槽位定义。
- `src/main/resources/data/jdte/curios/entities/large_pocket_generator.json`、`large_potion_canister.json`、`large_fuel_canister.json`：玩家槽位绑定。
- `src/main/resources/data/curios/tags/item/large_pocket_generator.json`、`large_potion_canister.json`、`large_fuel_canister.json`：物品标签 validator。
- `src/main/resources/data/jdte/recipe/large_pocket_generator.json`、`large_potion_canister.json`、`large_fuel_canister.json`：大型升级配方。
- `src/main/resources/assets/jdte/models/item/large_pocket_generator.json`、`large_potion_canister.json`、`large_fuel_canister.json`：大型物品模型和本 mod item properties。

修改：

- `src/main/java/com/jdte/setup/JDTEItems.java`：注册三个大型物品。
- `src/main/java/com/jdte/setup/JDTEMenus.java`：注册三个大型容器菜单类型。
- `src/main/java/com/jdte/setup/JDTERecipes.java`：注册大型升级配方类型/序列化器。
- `src/main/java/com/jdte/setup/JDTECreativeTabs.java`：加入三个大型物品。
- `src/main/java/com/jdte/JDTE.java`：注册大型发电机能力和快捷键打开服务端处理器。
- `src/main/java/com/jdte/client/JDTEKeyMappings.java`：增加三个可配置快捷键。
- `src/main/java/com/jdte/client/JDTEClientMod.java`、`JDTEClientSetup.java`：监听快捷键、注册按键、注册三种大型容器 screen 和 item properties。
- `src/main/java/com/jdte/common/network/JDTEPacketHandler.java`：注册打开容器 payload。
- `src/main/java/com/jdte/common/integrations/curios/BigFluidTankCuriosIntegration.java`：登录时补齐三个大型槽位。
- `src/main/resources/assets/jdte/lang/zh_cn.json`、`en_us.json`：物品名、tooltip、快捷键名、按键分类名。

### 任务 1：容量、批量和倍率规则

**文件：**
- 创建：`src/main/java/com/jdte/common/items/LargePortableContainerLogic.java`
- 创建：`src/test/java/com/jdte/common/items/LargePortableContainerLogicTest.java`

- [ ] **步骤 1：编写失败的规则测试**

测试必须覆盖：

```java
assertEquals(basePocketCapacity * 4, LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity));
assertEquals(4_000, LargePortableContainerLogic.potionCapacity());
assertEquals(1_000, LargePortableContainerLogic.POTION_BATCH_MB);
assertTrue(LargePortableContainerLogic.canFillPotionBatch(4, 3_000, 4_000));
assertFalse(LargePortableContainerLogic.canFillPotionBatch(3, 3_000, 4_000));
assertEquals(baseFuelCapacity * 4, LargePortableContainerLogic.fuelCapacity(baseFuelCapacity));
assertEquals(baseMinimum * 10, LargePortableContainerLogic.fuelMinimumConsumption(baseMinimum));
assertEquals(baseBurnMultiplier * 10, LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier));
```

- [ ] **步骤 2：运行规则测试确认正确失败**

运行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

预期：因 `LargePortableContainerLogic` 不存在而失败，而不是测试发现或断言错误。

- [ ] **步骤 3：编写最少规则实现**

实现无 Minecraft 客户端依赖的纯函数和常量：容量按 4 倍计算、药水批量固定为 1,000 mB 且要求 4 瓶、燃料最小消耗和发电机燃烧倍率按 10 倍计算。对 `int` 乘法使用长整型中间值并在越界时抛出清晰异常或裁剪到安全上限。

- [ ] **步骤 4：运行规则测试确认通过**

运行同一 `./gradlew test --tests ...` 命令，预期全部规则测试通过。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/items/LargePortableContainerLogic.java src/test/java/com/jdte/common/items/LargePortableContainerLogicTest.java
git commit -m "test(大型便携容器): 添加容量与倍率规则"
```

### 任务 2：大型物品、注册和能力

**文件：**
- 创建：`src/main/java/com/jdte/common/items/LargePocketGeneratorItem.java`
- 创建：`src/main/java/com/jdte/common/items/LargePotionCanisterItem.java`
- 创建：`src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java`
- 修改：`src/main/java/com/jdte/setup/JDTEItems.java`
- 修改：`src/main/java/com/jdte/JDTE.java`
- 修改：`src/main/java/com/jdte/setup/JDTECreativeTabs.java`

- [ ] **步骤 1：先写注册与行为测试**

在 `LargePortableContainerLogicTest` 或新的注册测试中增加对三个新注册 holder、默认 item 类型、stack size 为 1、发电机 4 倍 FE 容量的断言；测试直接调用大型物品公共容量方法，不通过 GUI mock。

- [ ] **步骤 2：运行测试确认失败**

运行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

预期：新物品 holder 和类不存在导致编译失败。

- [ ] **步骤 3：实现三个物品和注册**

大型发电机继承或适配 JDT `PocketGenerator`，覆盖 `getMaxEnergy()` 使用 JDT 配置值乘 4，并让 `LargeFuelCanisterItem` 的燃料倍率走 JDTE 适配逻辑；大型药水罐、燃料罐实现与 JDT 原版相同的手持 `use` 入口，但把菜单创建委托给后续大型菜单。三个物品都 `stacksTo(1)`，并实现 Curios 可装备接口/标签所需的物品契约。

在 `JDTEItems` 注册 `large_pocket_generator`、`large_potion_canister`、`large_fuel_canister`，加入创造标签。仅为大型发电机注册 FE 能力，容量使用 `POCKET_GENERATOR_MAX_FE * 4`；不要改动 JDT 原版能力注册。

- [ ] **步骤 4：运行测试确认通过**

运行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
./gradlew compileJava
```

预期：测试和编译通过，只有基线已有的弃用警告。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/items src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTECreativeTabs.java src/main/java/com/jdte/JDTE.java src/test/java/com/jdte/common/items
git commit -m "feat(大型便携容器): 注册大型物品与容量能力"
```

### 任务 3：容器菜单、饰品栏数据绑定和快捷键网络

**文件：**
- 创建：`src/main/java/com/jdte/common/containers/LargePocketGeneratorContainer.java`
- 创建：`src/main/java/com/jdte/common/containers/LargePotionCanisterContainer.java`
- 创建：`src/main/java/com/jdte/common/containers/LargeFuelCanisterContainer.java`
- 创建：`src/main/java/com/jdte/common/network/data/OpenLargePortableContainerPayload.java`
- 创建：`src/main/java/com/jdte/common/network/handler/OpenLargePortableContainerPacket.java`
- 修改：`src/main/java/com/jdte/setup/JDTEMenus.java`
- 修改：`src/main/java/com/jdte/common/network/JDTEPacketHandler.java`
- 修改：`src/main/java/com/jdte/client/JDTEKeyMappings.java`
- 修改：`src/main/java/com/jdte/client/JDTEClientMod.java`
- 修改：`src/main/java/com/jdte/client/JDTEClientSetup.java`

- [ ] **步骤 1：写菜单有效性与批量处理测试**

为容器逻辑增加最小可测断言：4 瓶同药水只产生一次 1,000 mB 变更；3 瓶、混合药水或容量不足时输入和容量均不变。为打开请求增加 payload 编解码测试，三个枚举值能往返，未知值无法构造为有效打开请求。

- [ ] **步骤 2：运行测试确认失败**

运行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
```

预期：新增 API 尚不存在，测试失败。

- [ ] **步骤 3：实现三个菜单和服务端打开路径**

菜单必须绑定实际来源：手持打开时绑定主手大型物品；快捷键打开时绑定对应 Curios handler 中的槽位。`stillValid` 检查来源槽位仍包含同一物品类型和同一数据实例，不能把 Curios 物品复制成菜单私有副本。大型药水菜单按 4 瓶一批调用规则类；大型燃料菜单按大型最小消耗和容量规则更新组件，关闭时返还输入槽内容。

注册三个 MenuType 和对应客户端 screen，screen 布局与 JDT 对应原版屏幕一致。快捷键分别绑定大型发电机、药水罐、燃料罐；默认键为 G/P/F，名称和分类走语言键。客户端按键触发时只发送 `OpenLargePortableContainerPayload`，服务端确认 Curios 槽位后调用 `player.openMenu`。

- [ ] **步骤 4：运行编译和网络/菜单测试**

运行：

```text
./gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest
./gradlew compileJava
```

预期：通过，且三种菜单、payload、客户端注册均可编译。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/containers src/main/java/com/jdte/common/network src/main/java/com/jdte/setup/JDTEMenus.java src/main/java/com/jdte/client
git commit -m "feat(大型便携容器): 支持饰品栏菜单与快捷键打开"
```

### 任务 4：Curios、无损配方和资源

**文件：**
- 创建：`src/main/java/com/jdte/common/recipes/LargePortableContainerRecipe.java`
- 创建：`src/test/java/com/jdte/common/recipes/LargePortableContainerRecipeTest.java`
- 修改：`src/main/java/com/jdte/common/integrations/curios/BigFluidTankCuriosIntegration.java`
- 修改：`src/main/java/com/jdte/setup/JDTERecipes.java`
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`
- 创建：Curios 槽位、实体绑定、物品 tag、三个配方、三个模型 JSON

- [ ] **步骤 1：写无损配方和槽位测试**

测试使用带有能量、药水内容物、燃料等级/燃烧速度组件的输入栈，断言配方输出对应大型物品并保留组件；错误原版物品、缺少材料和超过一个源物品均不匹配。资源测试断言三个 Curios tag 只包含对应大型物品，三个槽位都是 1 个槽且彼此独立。

- [ ] **步骤 2：运行测试确认失败**

运行：

```text
./gradlew test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest
```

预期：自定义配方类型/序列化器和新资源不存在，测试失败。

- [ ] **步骤 3：实现无损升级配方与 Curios 数据**

注册 `jdte:large_portable_container` 配方类型/序列化器，使用对应原版物品、4 个 `justdirethings:eclipsealloy_ingot` 和 1 个 `jdte:time_fluid_catalyst`。assemble 时复制输入物品相关 Data Components 后转换为目标大型物品；三类目标只能匹配自己的原版物品。

新增三个独立 Curios slot JSON、实体绑定 JSON 和 item tag JSON。扩展现有登录事件，为旧玩家分别补齐三个槽位；Curios API 调用仍在可选 mod 检查之后。添加资源模型，分别注册大型发电机启用属性以及药水/燃料容量分段属性；补齐中文和英文文本。

- [ ] **步骤 4：运行资源、配方和构建测试**

运行：

```text
./gradlew test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest
./gradlew compileJava
```

预期：测试、资源处理和编译通过。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/recipes src/main/java/com/jdte/common/integrations/curios src/main/java/com/jdte/setup/JDTERecipes.java src/main/resources
git commit -m "feat(大型便携容器): 添加 Curios 槽位与无损配方"
```

### 任务 5：整体验收与回归测试

**文件：**
- 修改：仅在发现真实缺口时修改对应生产代码或测试；不修改主工作区无关文件。

- [ ] **步骤 1：运行完整自动化测试**

运行：

```text
./gradlew test
```

预期：BUILD SUCCESSFUL，所有测试通过。

- [ ] **步骤 2：运行编译和文档检查**

运行：

```text
./gradlew compileJava
./gradlew validateDocs
```

预期：三个命令均成功；仅允许项目基线已有弃用/可选依赖警告，不允许新增编译错误。

- [ ] **步骤 3：逐项手动验收清单**

使用 JDT、Curios 和 JDTE 客户端验证：

1. 三个大型物品可以合成，原版三个物品仍可合成且行为未变。
2. 大型药水罐四瓶同药水增加 1,000 mB，三瓶和混合药水不消耗；容量为 4,000 mB。
3. 大型燃料罐达到 40,000,000 上限，单次消耗 2,000 刻，JDT 发电机识别大型燃料倍率 ×10。
4. 三个大型物品能同时放入各自 Curios 槽位，三个快捷键分别打开对应界面。
5. 快捷键打开界面时取下/替换饰品，菜单失效且输入物品安全返还。
6. 大型发电机在 Curios 中自动给背包和 Curios 中的可充能物品供能。

- [ ] **步骤 4：提交审查后修复**

```text
git add src/main/java/com/jdte/common src/main/java/com/jdte/setup src/main/java/com/jdte/client src/main/java/com/jdte/JDTE.java src/main/resources src/test/java/com/jdte/common
git commit -m "test(大型便携容器): 完成整体验收"
```

## 计划自检

- **规格覆盖度：** 容量/倍率在任务 1、物品能力在任务 2、手持与 Curios 菜单和快捷键在任务 3、Curios 槽位/资源/配方在任务 4、全量验收在任务 5；设计规格每个章节都有对应任务。
- **TDD：** 每个实现任务先列出会失败的测试、失败命令和预期原因，再列最少实现与绿灯命令。
- **类型一致性：** 三个物品注册名、三个槽位名、三个 payload 枚举值以及配方材料在全计划中保持一致。
- **占位符扫描：** 所有步骤都给出了实际文件、命令、预期结果和提交范围，没有流程占位符。
