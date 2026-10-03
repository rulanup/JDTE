# JDTE 0.6.0：KubeJS 与整合包内容控制

本文说明 JDTE 在 KubeJS 整合包中的配方替换、配方禁用和动态 JEI 配方开关。KubeJS 脚本放在实例的 `kubejs/server_scripts/`，配置文件是 `config/jdte/jdte.toml`。

## 1. 关闭动态配方生成

Greenhouse、Loot Fabricator 和 Bio Factory 的部分 JEI 内容是运行时扫描后生成的，不是 KubeJS 的 `ServerEvents.recipes` 配方。因此，KubeJS 的 `event.remove()` 无法关闭这类动态枚举；请使用 JDTE 配置：

```toml
[jdte.greenhouse]
# 普通温室、Large Greenhouse 的动态植物 JEI 配方
recipeGenerationEnabled = false

[jdte.lootFabricator]
# 根据服务端战利品表同步的动态 JEI 配方
recipeGenerationEnabled = false

[jdte.bioFactory]
# Productive Bees 等动态生物工厂 JEI 配方
recipeGenerationEnabled = false
```

这些开关只停止昂贵的动态 JEI 枚举，不会删除机器，也不会删除数据包或 KubeJS 提供的静态配方。配置修改后重启客户端/服务器；只修改脚本时可以使用 `/reload`。

## 2. 关闭方块、静态配方或 KubeJS 配方

`disabledBlocks` 和 `disabledRecipes` 支持完整 ID、短 ID 和目录前缀。省略命名空间时默认为 `jdte`；目录前缀只匹配自身及其子路径，不会误匹配相似名称。

```toml
[jdte.content]
# 方块仍保留注册 ID，避免旧存档坏档；但不会出现在创造栏，不能新放置、打开、运行或被自动化访问。
disabledBlocks = [
  "advanced_item_collector",
  "jdte:time_accelerator",
  "jdte:loot_fabricator"
]

# 同时过滤内置数据配方和 KubeJS 生成的数据配方。
disabledRecipes = [
  "greenhouse",                         # jdte:greenhouse/*
  "jdte:bio_factory/cow",               # 单个 Bio Factory 配方
  "jdte:jei/loot_fabricator/minecraft/zombie" # 隐藏该生物的全部分页 JEI 条目
]

# 这两个开关覆盖所有三档时间加速器/两档时间冻结器；Extended Clicker 不受影响。
timeAcceleratorEnabled = false
timeFreezerEnabled = false
```

`disabledRecipes` 的 ID 是资源配方 ID，而不是文件系统路径。例如 `data/jdte/recipe/greenhouse/wheat.json` 的 ID 是 `jdte:greenhouse/wheat`。Loot Fabricator 的动态条目使用 `jdte:jei/loot_fabricator/<namespace>/<path>/<page>`，如果要降低扫描开销，应优先关闭 `recipeGenerationEnabled`。

## 3. 替换 Greenhouse 配方

在 `kubejs/server_scripts/greenhouse.js` 中移除旧 ID，再用同一 ID 创建 `jdte:greenhouse` 自定义配方：

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'jdte:greenhouse/wheat' })

  event.custom({
    type: 'jdte:greenhouse',
    seed: { item: 'minecraft:wheat_seeds' },
    outputs: [
      { id: 'minecraft:wheat', count: 2 },
      { id: 'minecraft:wheat_seeds', count: 1 }
    ],
    display_block: 'minecraft:wheat',
    use_loot_table: false,
    growth_work: 4096,
    fluid: 'minecraft:water',
    time_fluid: 100
  }).id('jdte:greenhouse/wheat')
})
```

字段说明：

- `seed`：模板输入，使用 KubeJS ingredient，可写 `item` 或 `tag`。
- `outputs`：一个或多个物品堆；每项至少包含 `id` 和 `count`。
- `display_block`：JEI 和客户端预览显示的方块 ID。
- `harvest_block`：可选，指定实际使用的成熟方块；省略时使用 `display_block`。
- `use_loot_table`：可选，默认 `true`；设为 `false` 时只使用 `outputs`。
- `growth_work`：正整数，单次收获需要的生长工作量。
- `fluid`：可选的源流体 ID，必须是已注册的源流体；省略时使用 `justdirethings:time_fluid_source`。
- `time_fluid`：每次收获的流体用量，不是流体 ID。

普通 Greenhouse、Large Greenhouse 和 JDTE-Matrix 的成员温室共用这个配方类型。一个温室的单个储罐不能混装流体；替换配方后 `/reload`，旧流体仍可抽出，但只能驱动仍匹配的配方。

## 4. 添加 Bio Factory 配方

Bio Factory 使用一个可复用样本和最多三个无序材料输入。`count: 0` 表示材料只作为催化剂、不消耗；正数表示每次处理消耗对应数量。

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:bio_factory',
    specimen: { item: 'minecraft:cow_spawn_egg' },
    inputs: [
      { ingredient: { item: 'minecraft:wheat' }, count: 0 }
    ],
    outputs: [],
    output_fluid: 'minecraft:milk',
    output_fluid_amount: 1000,
    process_ticks: 600,
    energy: 1000
  }).id('my_pack:bio_factory/cow_milk')
})
```

物品产物示例：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:bio_factory',
    specimen: { item: 'minecraft:bee_spawn_egg' },
    inputs: [
      { ingredient: { tag: 'minecraft:bee_food' }, count: 0 }
    ],
    outputs: [
      { item: { id: 'minecraft:honeycomb', count: 1 }, chance: 1.0 }
    ],
    process_ticks: 600,
    energy: 1000
  }).id('my_pack:bio_factory/bee_honeycomb')
})
```

可选流体字段：`process_fluid`/`process_fluid_amount` 指定培养或授粉所需流体；`output_fluid`/`output_fluid_amount` 指定产物流体。`outputs` 和产物流体可以二选一，也可以同时存在。`chance` 范围为 `0.0-1.0`，省略时为 `1.0`。旧版的 `food`/`food_count` 写法仍可读取，但新脚本建议使用 `inputs`。

## 5. Loot Fabricator（战利品制造机）配方

Loot Fabricator 同时支持两种模式：
1. **动态战利品表模式**：默认根据服务端实际编译后的实体战利品表自动提取产物和掉落率。
2. **KubeJS 自定义配方（`jdte:loot_fabricator`）**：现在支持通过 `ServerEvents.recipes` 添加自定义战利品配方，指定任意输入物品（如刷怪蛋、特定核心）、产物列表、流体与能量消耗：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:loot_fabricator',
    input: { item: 'minecraft:zombie_spawn_egg' },
    outputs: [
      { item: { id: 'minecraft:rotten_flesh', count: 4 }, chance: 1.0 },
      { item: { id: 'minecraft:iron_ingot', count: 1 }, chance: 0.25 }
    ],
    life_fluid: 20,
    time_fluid: 1,
    energy: 5000,
    process_ticks: 20
  }).id('my_pack:loot_fabricator/custom_zombie')
})
```

字段说明：
- `input`：输入物品（支持 `{ item: '...' }` 或 `{ tag: '...' }`）。
- `outputs`：产物数组，每项包含 `item`（`id` 与 `count`）和 `chance`（0.0-1.0 掉落几率）。
- `life_fluid`：每次消耗生命流体 mB（默认 20）。
- `time_fluid`：每次消耗时间流体 mB（默认 1）。
- `energy`：每次消耗能量 FE（默认 5000）。
- `process_ticks`：单次合成基础 tick（默认 20）。

## 6. Bio Extractor（生物提取机）配方

高级/扩展生物提取机可在设定范围内扫描活体生物，提取流体（如牛奶）或物品（如羊毛、羽毛、蛋、墨囊等），并施加冷却与可选伤害。

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:bio_extractor',
    entity: 'minecraft:cow',
    output_fluid: {
      id: 'minecraft:milk',
      amount: 1000
    },
    output_items: [],
    energy: 5000,
    cooldown: 600,
    damage: 0.0
  }).id('my_pack:bio_extractor/cow_milk')

  // 提取羊毛示例（羊剪毛状态受保护，羊吃草后可再次提取）
  event.custom({
    type: 'jdte:bio_extractor',
    entity: 'minecraft:sheep',
    output_items: [
      { item: { id: 'minecraft:white_wool', count: 2 }, chance: 1.0 }
    ],
    energy: 3000,
    cooldown: 1200,
    damage: 0.0
  }).id('my_pack:bio_extractor/sheep_wool')
})
```

字段说明：
- `entity`：目标实体 ID（如 `'minecraft:cow'`）。
- `output_fluid`：可选输出流体，包含 `id` 和 `amount`。
- `output_items`：可选输出物品数组，包含 `item`（`id` 和 `count`）和 `chance`。
- `energy`：每次消耗 FE。
- `cooldown`：提取后的冷却时间（游戏 tick，600 = 30秒）。
- `damage`：每次提取对生物造成的伤害点数（浮点数，默认 0.0）。

## 7. Infusion Machine（灌注机）与 Life Extractor（生命提取机）配方

### Infusion Machine（`jdte:infusion`）
现已全面支持 KubeJS：移除了 JSON 结构中强制的 `"id"` 字段限制，材料全面兼容 Tag 和物品堆计数，正确返回合成产物并在 JEI 中展示：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:infusion',
    input: { tag: 'c:ingots/iron', count: 4 },
    fluid_type: 'jdte:life_fluid_source',
    fluid_amount: 500,
    result: { id: 'minecraft:echo_shard', count: 1 },
    energy: 10000,
    time: 100
  }).id('my_pack:infusion/iron_to_echo')
})
```

### Life Extractor（`jdte:life_extractor`）
现已提供自定义配方，可为指定实体定义专属提取流体与能量：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:life_extractor',
    entity: 'minecraft:warden',
    fluid: { id: 'justdirethings:time_fluid_source', amount: 2000 },
    energy: 50000,
    process_ticks: 200,
    damage: 10.0
  }).id('my_pack:life_extractor/warden')
})
```

## 8. Fluid Mixer（流体混合器）配方

流体混合器支持双流体 + 单物品输入合成产物流体：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:fluid_mixer',
    fluid_input_a: { id: 'jdte:life_fluid_source', amount: 500 },
    fluid_input_b: { id: 'minecraft:milk', amount: 500 },
    item_input: {
      ingredient: { item: 'minecraft:cactus' },
      count: 1
    },
    output: { id: 'justdirethings:time_fluid_source', amount: 250 },
    process_ticks: 100,
    energy: 5000
  }).id('my_pack:fluid_mixer/time_fluid')
})
```

## 9. 推荐的脚本组织方式

```text
kubejs/
└─ server_scripts/
   ├─ jdte_greenhouse.js
   ├─ jdte_bio_factory.js
   ├─ jdte_bio_extractor.js
   ├─ jdte_loot_fabricator.js
   ├─ jdte_infusion.js
   └─ jdte_recipe_cleanup.js
```

每个自定义配方使用独立命名空间（例如 `my_pack:`），替换内置配方时先 `event.remove` 再复用原 ID。改完脚本执行 `/reload`，查看日志中的 KubeJS recipe reload 结果；如果配方仍不显示，依次检查自定义配方类型、字段拼写、流体是否为源流体，以及是否被 `jdte.content.disabledRecipes` 匹配。
