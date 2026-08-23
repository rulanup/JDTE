# Task 4 report — Curios、无损配方和资源

状态：DONE

## 实际改动

- 新增自定义无损升级配方：
  - `src/main/java/com/jdte/common/recipes/LargePortableContainerRecipe.java`
  - 注册 `jdte:large_portable_container` 自定义 serializer，供 JSON `type` 反序列化使用
  - 仅允许以下一对一升级：
    - `justdirethings:pocket_generator -> jdte:large_pocket_generator`
    - `justdirethings:potion_canister -> jdte:large_potion_canister`
    - `justdirethings:fuel_canister -> jdte:large_fuel_canister`
  - 匹配材料固定为：
    - 1 个对应原版便携容器
    - 4 个 `justdirethings:eclipsealloy_ingot`
    - 1 个 `jdte:time_fluid_catalyst`
  - `assemble` 使用 `ItemStack.transmuteCopy(...)`，保持输入栈相关 data components 不丢失

- 新增 Task 4 聚焦测试：
  - `src/test/java/com/jdte/common/recipes/LargePortableContainerRecipeTest.java`
  - 覆盖：
    - pocket generator / fuel canister 的组件无损升级
    - potion canister 的合法原版状态无损升级
    - 错误源物品 / 缺材料 / 多源物品拒绝匹配
    - serializer 注册 ID 与 vanilla crafting runtime type
    - 通过 `RecipeManager` + `RecipeType.CRAFTING` 验证 JSON 配方可被工作台路径发现
    - Curios item tag / slot / entity 资源契约
    - 三个 recipe JSON、三个基础 item model、英文/中文 lang key

- 更新 Curios 登录补槽逻辑：
  - `src/main/java/com/jdte/common/integrations/curios/BigFluidTankCuriosIntegration.java`
  - 在原 `big_fluid_tank` 之外，为旧玩家登录时补齐：
    - `large_pocket_generator`
    - `large_potion_canister`
    - `large_fuel_canister`
  - Curios API 调用仍全部保留在 `ModList.get().isLoaded("curios")` 可选依赖边界后

- 更新客户端 item property 注册：
  - `src/main/java/com/jdte/client/JDTEClientSetup.java`
  - 注册：
    - `justdirethings:enabled` -> `LARGE_POCKET_GENERATOR`
    - `justdirethings:potion_fullness` -> `LARGE_POTION_CANISTER`
    - `jdte:fuel_fullness` -> `LARGE_FUEL_CANISTER`

- 为大型物品补充模型属性辅助方法：
  - `src/main/java/com/jdte/common/items/LargePocketGeneratorItem.java`
    - 新增 `getEnabledProperty`
  - `src/main/java/com/jdte/common/items/LargePotionCanisterItem.java`
    - 新增 `getFullness`
  - `src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java`
    - 新增 `getFullness`

- 新增资源：
  - Curios item tags：
    - `data/curios/tags/item/large_pocket_generator.json`
    - `data/curios/tags/item/large_potion_canister.json`
    - `data/curios/tags/item/large_fuel_canister.json`
  - Curios slots：
    - `data/curios/curios/slots/large_pocket_generator.json`
    - `data/curios/curios/slots/large_potion_canister.json`
    - `data/curios/curios/slots/large_fuel_canister.json`
  - Curios entity bindings：
    - `data/jdte/curios/entities/large_pocket_generator.json`
    - `data/jdte/curios/entities/large_potion_canister.json`
    - `data/jdte/curios/entities/large_fuel_canister.json`
  - 配方：
    - `data/jdte/recipe/large_pocket_generator.json`
    - `data/jdte/recipe/large_potion_canister.json`
    - `data/jdte/recipe/large_fuel_canister.json`
  - 模型：
    - `assets/jdte/models/item/large_pocket_generator*.json`
    - `assets/jdte/models/item/large_potion_canister*.json`
    - `assets/jdte/models/item/large_fuel_canister*.json`
  - 文本：
    - `assets/jdte/lang/en_us.json`
    - `assets/jdte/lang/zh_cn.json`

## TDD：RED → GREEN

### RED

先新增 `LargePortableContainerRecipeTest`，然后运行聚焦测试：

`./gradlew test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest --rerun-tasks`

RED 结果：

- 失败（缺少 `LargePortableContainerRecipe` 以及 `JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_TYPE / SERIALIZER`）

### GREEN

补齐 recipe / serializer、Curios 资源、模型属性和 lang 后重跑。

中途发现一个测试预期需要修正：

- 原版 `PotionCanister.setPotionAmount(...)` 会把数值钳制到 `1000 mB`
- 因此“原版小药水罐升级后保留 `3000 mB`”不是合法原版状态
- 已将该断言修正为验证合法原版容量下的无损复制（`1000 mB`）

GREEN 聚焦命令：

`./gradlew test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest`

GREEN 结果：

- `BUILD SUCCESSFUL in 19s`

### 2026-08-23 复审修复：接入原版工作台

复审发现关键集成缺陷：

- `LargePortableContainerRecipe.getType()` 返回了自定义 `jdte:large_portable_container`
- 但工作台查询的是 `RecipeType.CRAFTING`
- 结果是三个升级 JSON 虽然能被自定义 serializer 反序列化，但不会出现在原版 crafting recipe 集合中

修复内容：

- `LargePortableContainerRecipe` 改为实现 `CraftingRecipe`
- `getType()` 改为返回 `RecipeType.CRAFTING`
- 增加 `category()`，归类为 `CraftingBookCategory.MISC`
- 删除误导性的 `LARGE_PORTABLE_CONTAINER_RECIPE_TYPE` 运行时注册，仅保留自定义 serializer 注册
- 新增 `RecipeManager` 回归测试：从真实 Task 4 JSON 走 `RecipeManager.fromJson(...)` + `replaceRecipes(...)` 装入后，必须能被 `getRecipeFor(RecipeType.CRAFTING, ...)` 找到

修复后聚焦命令：

`./gradlew test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest`

修复后结果：

- `BUILD SUCCESSFUL in 19s`

## 验证命令与结果

1. 聚焦测试

   `./gradlew test --tests com.jdte.common.recipes.LargePortableContainerRecipeTest`

   结果：`BUILD SUCCESSFUL in 19s`

2. 完整测试

   `./gradlew test`

   结果：`BUILD SUCCESSFUL in 16s`

3. Java 编译

   `./gradlew compileJava`

   结果：`BUILD SUCCESSFUL in 1s`

## 自审

- 自定义升级配方只接受三种明确映射，没有开放成泛化“任意便携容器升级”。
- `assemble` 走 `transmuteCopy`，没有手写“挑几个字段复制”的易漏方案。
- Curios 登录补槽仍然是可选集成；未安装 Curios 时不会触发类加载边界外的 API 调用。
- 三个 Curios 槽位独立、大小均为 1，且 item tag 只接受各自对应的大型物品。
- 升级配方现在属于原版 `RecipeType.CRAFTING`，工作台与 `RecipeManager.getRecipeFor(RecipeType.CRAFTING, ...)` 都能发现它。
- 模型属性 ID 与资源 ID 按任务要求保持精确：
  - `justdirethings:enabled`
  - `justdirethings:potion_fullness`
  - `jdte:fuel_fullness`
  - `jdte:large_portable_container`
- 未修改与 Task 4 无关的源码文件。

## 疑虑 / 后续关注

1. `BigFluidTankCuriosIntegration` 仍使用 Curios 当前已弃用但项目现存也在使用的 `ISlotHelper` API；本任务保持与现有代码风格一致，没有顺手扩大为 Curios API 升级重构。
2. 本任务的资源契约和核心复制语义已由测试覆盖，但“三个独立 Curios 槽位可同时实机装备并分别快捷键打开”的端到端交互仍主要依赖 Task 3 已有链路，建议后续联调时顺手做一次手动验收。
