# JEI、Bio Factory 与温室兼容性修复规格

日期：2026-08-26

## 目标

修复以下三个问题，并保持现有机器存档、配方 ID 和可选模组兼容性：

1. JEI 注册 `jdte:jei_plugin` 时不因 Botany Pots 或 Productive Bees 的动态数据解析而长时间阻塞主线程。
2. Bio Factory 对带有实体数据的 Productive Bees 蜂 spawn egg 使用与实际实体一致的蜂种数据，从而得到正确贴图。
3. Forbidden & Arcanus 的 Edelwood sapling 在温室中产出 Edelwood log。

## 非目标

- 不修改 JEI、Botany Pots、Productive Bees 或 Forbidden & Arcanus 的外部代码。
- 不改变 Bio Factory 的服务器生产逻辑、能量/流体消耗或存档 NBT 格式。
- 不把可选模组依赖改为必需依赖。
- 不通过关闭整个 JEI 插件来规避问题。

## 根因与设计

### 1. JEI 动态配方解析

`GreenhouseJeiRecipe` 在枚举普通植物时会再次调用完整的 `GreenhouseCropResolver.find(level, seed)`。完整解析器包含 Botany Pots 专用扫描；而 Botany Pots 的每个作物定义还会扫描土壤配方和物品注册表。这样会把“一次 Botany Pots 枚举”放大为“每个普通植物重复 Botany Pots 枚举”，并把标签匹配开销集中在 JEI 主线程。

改为两条互不重入的路径：

- Botany Pots 只在 `BotanyPotsGreenhouseIntegration.getCrops(level)` 的专用枚举阶段执行一次。
- 普通植物的兜底阶段只调用不访问任何可选集成的 `GreenhouseCropResolver.findGeneric(seed)`。

Productive Bees 的 JEI 蜂配方改成逐配方隔离：

- 每个 Advanced Beehive 配方单独生成显示数据；生成失败只跳过该配方并记录一次 debug/warn 日志，继续处理其他配方。
- 花标签展开限制为固定最大条目数；超过上限时停止展开该标签，不继续递归或扫描整个注册表。
- 标签解析使用线程本地的标签 ID 访问栈，重复进入同一标签时立即停止当前标签分支。
- 显式花方块、花物品和花流体仍按当前语义生成 JEI 输入；异常的标签分支不影响配方主体。

### 2. Bio Factory 蜂实体数据

Productive Bees 的可配置蜂 spawn egg 将蜂种放在 `DataComponents.ENTITY_DATA` 中。当前 Bio Factory 直接调用 `SpawnEggItem.getType(stack).create(level)`，没有把该组件加载回实体；自然世界实体或 Bee Cage 则会保留这份数据，因此出现“实体在空气中正常、工厂内丢贴图”的差异。

新增一个不依赖可选模组的 spawn-egg 数据应用辅助方法：

- 从 `DataComponents.ENTITY_DATA` 复制 `CompoundTag`。
- 实体创建后调用 `Entity.load` 应用数据。
- 无实体数据时保持现有行为。

Productive Bees 集成和 Bio Factory BER 的 spawn egg 渲染路径统一调用这条逻辑；Bee Cage 路径继续使用 Productive Bees 的公开 API。

### 3. Edelwood 温室配方

新增带 `neoforge:mod_loaded` 条件的兼容配方：

- 种子：`forbidden_arcanus:growing_edelwood`
- 展示方块：`forbidden_arcanus:growing_edelwood`
- 产物：`4x forbidden_arcanus:edelwood_log`、`1x forbidden_arcanus:edelwood_sapling`
- 使用固定成长工作量和 Time Fluid 成本，与现有 sapling 温室配方一致。
- `use_loot_table: false`，确保产物明确为原木而不是依赖外部树叶/掉落表。

目标版本依赖 jar 已确认使用 `forbidden_arcanus:growing_edelwood`，不得改用不存在的 `edelwood_sapling` ID。

## 数据流与失败行为

```text
JEI greenhouse registration
  ├─ direct JDTE recipes
  ├─ Mystical Agriculture recipes
  ├─ Botany Pots recipes (one dedicated pass)
  └─ generic plant fallback (integration-free)

JEI Bio Factory registration
  └─ for each bee recipe
       ├─ resolve cage/entity/flowering data
       ├─ bounded tag expansion
       └─ on failure: log and skip only this recipe

Bio Factory specimen render
  └─ create entity → apply spawn-egg ENTITY_DATA → cache/render entity
```

## 回归测试

测试先于生产代码添加：

1. 温室解析器测试：普通植物的 generic fallback 返回定义，不触发 Botany Pots 路径；现有 greenhouse recipe codec 测试验证 Edelwood 配方的条件、输入和原木输出。
2. Productive Bees/Bio Factory 数据测试：带 `DataComponents.ENTITY_DATA` 的物品栈能提取独立的实体数据副本；无数据时返回空结果。测试辅助类不加载可选 Productive Bees 类。
3. 运行相关 JUnit 测试，先确认新增测试在缺少实现时失败，再完成实现并确认通过。
4. 运行 `compileJava` 和 `test`；若可选模组运行环境可用，再检查 JEI 注册日志不再重复进入动态扫描，并确认完整构建。

## 验收标准

- `GreenhouseJeiRecipe` 的普通植物循环不再调用包含 Botany Pots 的完整解析器。
- 单个 Productive Bees 异常配方不能阻塞或中断其他 JEI 配方注册；标签展开有固定上限。
- Productive Bees 带蜂种实体数据的 spawn egg 在 Bio Factory 缓存实体上保留该数据，渲染使用正确蜂种贴图。
- Forbidden & Arcanus 加载时 Edelwood sapling 的温室配方可见且产出 Edelwood log；未加载时不会产生无效配方报错。
- 现有测试、编译和未相关工作区改动不受影响。
