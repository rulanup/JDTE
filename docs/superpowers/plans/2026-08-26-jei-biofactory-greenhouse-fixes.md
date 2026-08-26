# JEI、Bio Factory 与温室兼容性修复实现计划

> **面向 AI 代理的工作者：** 必须使用 `test-driven-development` 与 `verification-before-completion`；每个生产代码改动都要先有能证明根因的回归测试。

**目标：** 消除 JEI 动态配方注册中的重复 Botany Pots 扫描，隔离 Productive Bees 异常蜂配方，恢复 Bio Factory 蜂 spawn egg 的实体数据贴图，并添加 Forbidden & Arcanus Edelwood 温室产木配方。

**架构：** 保留现有 JEI 分类与机器生产 API；只新增 integration-free greenhouse generic seam、可选模组 JEI 单配方保护、通用 spawn-egg `ENTITY_DATA` 复制辅助和条件化数据配方。

**技术栈：** Java 21、NeoForge 21.1.215、Minecraft 1.21.1、JEI 19.27、JUnit 5、Codec/JSON recipe tests。

---

## 任务 1：建立失败回归测试

**文件：**

- 新增 `src/test/java/com/jdte/common/recipes/GreenhouseCropResolverTest.java`
- 新增 `src/test/java/com/jdte/common/utils/SpawnEggEntityDataTest.java`
- 新增 `src/test/java/com/jdte/common/recipes/ForbiddenArcanusGreenhouseRecipeTest.java`

**步骤：**

1. 先写 `GreenhouseCropResolver.findGeneric` 的普通作物测试，断言它能返回小麦定义；测试在 helper 尚不存在时必须编译失败。
2. 写 spawn egg `DataComponents.ENTITY_DATA` 复制测试，断言返回的是独立 tag，修改返回值不污染原栈；无组件时断言为空。
3. 写配方 JSON codec 测试，读取 `data/jdte/recipe/greenhouse/forbidden_arcanus_edelwood.json`，断言条件存在、seed 是 Edelwood sapling、输出包含 Edelwood log。
4. 执行：`./gradlew test --tests "com.jdte.common.recipes.GreenhouseCropResolverTest" --tests "com.jdte.common.utils.SpawnEggEntityDataTest" --tests "com.jdte.common.recipes.ForbiddenArcanusGreenhouseRecipeTest"`。
5. 记录预期失败原因：生产 helper/资源尚未实现，不把失败误判为环境问题。

## 任务 2：修复温室 JEI 重复解析

**文件：**

- 修改 `src/main/java/com/jdte/common/recipes/GreenhouseCropResolver.java`
- 修改 `src/main/java/com/jdte/common/jei/greenhouse/GreenhouseJeiRecipe.java`

**步骤：**

1. 把 generic BlockItem/CropBlock/BushBlock fallback 提取为不访问 Level、RecipeManager 或可选集成的 `findGeneric(ItemStack)`。
2. 让完整运行时解析器继续按现有优先级调用 Botany Pots 等集成，最后复用 generic helper。
3. 修改 JEI 普通物品枚举只调用 `findGeneric`，保持前置直接配方、Mystical Agriculture 和 Botany Pots 专用枚举不变。
4. 运行任务 1 的 resolver 测试，确认通过；用 `rg` 检查普通 JEI 循环不再调用 `GreenhouseCropResolver.find(minecraft.level, ...)`。

## 任务 3：隔离 Productive Bees JEI 配方与恢复蜂贴图

**文件：**

- 新增 `src/main/java/com/jdte/common/utils/SpawnEggEntityData.java`
- 修改 `src/main/java/com/jdte/common/integrations/ProductiveBeesBioFactoryIntegration.java`
- 修改 `src/main/java/com/jdte/client/renderers/BioFactoryBER.java`

**步骤：**

1. 实现纯 vanilla 的 spawn egg entity-data 复制 helper，并让 Productive Bees spawn egg 创建路径在实体创建后加载复制的 tag。
2. 让 Bio Factory BER 的 spawn egg 路径复用同一 helper/集成入口，避免渲染器绕过数据恢复。
3. 把 Productive Bees JEI 单配方构建提取为可捕获失败的边界；单配方失败只记录并跳过。
4. 为 flowering tag 解析增加线程本地重复标签保护和固定最大输入数；超限或异常只放弃该 flowering 分支，不中断整个注册。
5. 运行 spawn egg 测试与现有 Bio Factory 测试，随后 `./gradlew compileJava`。

## 任务 4：添加 Edelwood 温室兼容配方

**文件：**

- 新增 `src/main/resources/data/jdte/recipe/greenhouse/forbidden_arcanus_edelwood.json`

**步骤：**

1. 使用 `neoforge:mod_loaded` 条件保护配方。
2. 使用已确认的 `forbidden_arcanus:growing_edelwood` 输入/展示方块和 `forbidden_arcanus:edelwood_log` 原木输出；不要使用不存在的 `edelwood_sapling` ID。
3. 保持 `use_loot_table:false`，并沿用现有 sapling 配方的成长工作量、Time Fluid 与返还 sapling 语义。
4. 运行 recipe codec 测试及所有 greenhouse tests。

## 任务 5：集成验证与交付审查

**步骤：**

1. 执行定向测试：`./gradlew test --tests "com.jdte.common.recipes.*Greenhouse*" --tests "com.jdte.common.utils.SpawnEggEntityDataTest"`。
2. 执行完整 `./gradlew test` 与 `./gradlew compileJava`；失败时区分环境依赖下载失败和代码失败。
3. 执行 `git diff --check`、`git status --short`、`git diff --stat`，确认只包含规格、计划、三项修复与回归测试。
4. 检查 JEI 代码日志与调用链，确认没有为每个普通植物再次进入 Botany Pots 专用解析。
5. 按 `requesting-code-review` 和 `finishing-a-development-branch` 做最终审查，向用户报告测试证据和是否需要合并/保留分支。
