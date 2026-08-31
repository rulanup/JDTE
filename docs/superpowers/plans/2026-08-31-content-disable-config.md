# 可配置内容禁用实现计划

> **面向 AI 代理的工作者：** 必须使用 `test-driven-development` 与 `verification-before-completion`，按复选框记录红—绿证据。

**目标：** 提供通用方块/配方禁用列表，并为 Greenhouse、Loot Fabricator、Bio Factory 提供独立动态配方生成开关。

**架构：** 注册表保持稳定；纯 `ContentSelection` 负责 ID 归一化和匹配，`JDTEContentControl` 负责读取配置。配方在 `RecipeManager` 解码入口过滤，方块在创造栏、放置/交互、ticker 和 capability 边界统一门控。

**技术栈：** Java 21、NeoForge 21.1、Mixin、JEI 19、JUnit 5。

---

### 任务 1：内容选择与配置契约

**文件：**

- 创建 `src/main/java/com/jdte/setup/config/ContentConfig.java`
- 创建 `src/main/java/com/jdte/common/content/ContentSelection.java`
- 创建 `src/main/java/com/jdte/common/content/JDTEContentControl.java`
- 修改 `src/main/java/com/jdte/setup/JDTEConfig.java`
- 测试 `src/test/java/com/jdte/common/content/ContentSelectionTest.java`
- 测试 `src/test/java/com/jdte/setup/ContentConfigTest.java`

- [x] 先写测试，断言 `greenhouse` 归一化为 `jdte:greenhouse`、完整外部 ID 保留、目录后代被禁用、无关 ID 保持启用、默认列表为空且三个动态生成开关默认为开启。
- [x] 运行定向测试并确认因类型/接口尚不存在而失败。
- [x] 实现最小纯匹配对象、配置类和读取门面。
- [x] 重跑定向测试并确认通过。

### 任务 2：配方与 JEI 门控

**文件：**

- 创建 `src/main/java/com/jdte/mixin/RecipeManagerMixin.java`
- 修改 `src/main/resources/mixins.jdte.json`
- 修改 `src/main/java/com/jdte/common/jei/JDTEJeiPlugin.java`
- 修改三个 JEI recipe provider 与 Loot Fabricator 同步路径
- 测试 `src/test/java/com/jdte/common/content/ContentRecipePolicyTest.java`

- [x] 先写纯策略测试，断言独立关闭三个生成器只影响各自家族，并断言禁用方块同时禁用同名合成配方。
- [x] 运行测试并确认缺失策略失败。
- [x] 在 RecipeManager 解码前过滤配置命中的配方；JEI 注册、催化剂、点击区域和动态刷新复用同一策略。
- [x] 删除 serializer 级配置错误门控，避免关闭功能时刷解析错误。
- [x] 重跑策略与现有 Greenhouse/Bio Factory/Loot Fabricator 测试。

### 任务 3：方块禁用边界

**文件：**

- 创建 `src/main/java/com/jdte/common/content/JDTEContentEvents.java`
- 修改 `src/main/java/com/jdte/JDTE.java`
- 修改 `src/main/java/com/jdte/mixin/BaseMachineBlockMixin.java`
- 修改 `src/main/java/com/jdte/common/capabilities/MachineCapabilities.java`
- 修改 `src/main/java/com/jdte/setup/JDTECreativeTabs.java`

- [x] 先扩展策略测试，覆盖 BlockItem 判定和创造栏过滤所需的纯入口。
- [x] 运行并确认失败。
- [x] 取消禁用方块的新放置/右键交互，跳过其服务器 tick，能力 provider 返回空，并从创造栏父级/搜索条目移除。
- [x] 运行定向测试和 `compileJava`。

### 任务 4：文案、文档与完整验证

**文件：**

- 修改 `src/main/resources/assets/jdte/lang/en_us.json`
- 修改 `src/main/resources/assets/jdte/lang/zh_cn.json`
- 修改 `README.md`、`README_EN.md`、`CHANGELOG.md`

- [x] 补齐配置标题、说明、重启/旧存档语义与示例 ID。
- [x] 运行 `./gradlew test`、`./gradlew compileJava`；`validateDocs` 运行到基线已有的扩展 JDT 机器指南/旧 Patchouli 陈旧项时失败，未改动这些无关资源。
- [x] 检查 `git diff --check` 与工作区；代码提交干净。原先未跟踪的 `CON` 文件在构建期间被环境删除，无法从 Git 恢复。
- [x] 独立审查完整 diff，修复所有重要发现后重新验证。
