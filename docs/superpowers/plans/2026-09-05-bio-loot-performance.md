# 生物工厂与战利品制造机性能优化实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。

**目标：** 降低单台生物工厂和战利品制造机稳定运行时的服务端 tick 开销，同时保持产出和存档兼容。

**架构：** 使用输入/升级变更驱动的脏标记重建派生缓存；服务端 tick 只读取缓存并推进进度。Productive Bees 环境条件按 20 tick 快照，战利品制造机维护有效模板槽数组。

**技术栈：** Java 21、NeoForge 1.21.1、JUnit 5、现有 BlockEntity/ItemStackHandler API。

---

### 任务 1：战利品制造机有效模板缓存

**文件：**
- 修改：`src/main/java/com/jdte/common/blockentities/LootFabricatorBE.java`
- 测试：`src/test/java/com/jdte/common/blockentities/LootFabricatorCostTest.java`（必要时新增独立缓存测试）

- [ ] 在输入处理器和升级变化处设置缓存脏标记，新增有效槽数组与缓存重建方法。
- [ ] 将 `processLoot()` 的模板遍历改为读取有效槽数组；保持轮询顺序、批处理数量、成本结算和输出顺序不变。
- [ ] 删除每 tick 对输入堆的 `copy`/组件比较，仅在脏标记时复制快照并计算成本。
- [ ] 增加缓存失效测试并运行相关 JUnit。

### 任务 2：生物工厂配方与环境缓存

**文件：**
- 修改：`src/main/java/com/jdte/common/blockentities/BioFactoryBE.java`
- 修改：`src/main/java/com/jdte/common/integrations/ProductiveBeesBioFactoryIntegration.java`（仅在需要暴露轻量环境键时）
- 测试：`src/test/java/com/jdte/common/blockentities/` 下新增缓存行为测试

- [ ] 让流体槽内容变化通过监听/脏标记触发配方重建；流体数量变化只参与资源检查。
- [ ] 为 Productive Bees 的昼夜、降雨、雷暴条件增加 20 tick 环境快照，避免每 tick 重复计算。
- [ ] 保持实体创建失败、可选模组缺失和空产出行为不变。
- [ ] 增加同窗口单次计算及跨窗口刷新测试。

### 任务 3：验证与清理

**文件：**
- 修改：无额外文件

- [ ] 运行生物工厂、战利品制造机及机器设置相关测试。
- [ ] 运行 `./gradlew compileJava`。
- [ ] 运行 `git diff --check`，确认无调试日志、临时文件或 NBT/容器布局变化。
