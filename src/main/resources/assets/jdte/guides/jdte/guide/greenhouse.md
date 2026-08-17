---
navigation:
  title: 温室大棚
  icon: "jdte:greenhouse"
  position: 19
item_ids:
  - jdte:greenhouse
---

# 温室大棚

<BlockImage id="jdte:greenhouse" scale="2" />

温室大棚以作物、花朵或树苗作为可重复使用的模板，消耗 FE 和配方所需流体（默认时间流体）直接生成产物。显示的植物仅为客户端模型，不会在世界中放置或破坏真实方块。

## 快速使用

1. 在左侧 4 个种植槽放入植物模板；模板不会消耗，堆叠数量代表并行种植数量。
2. 输入 FE 和配方所需流体（默认时间流体），然后用速度按钮选择 `1-32x`。
3. 产物进入分页输出槽，可由自动 I/O 或相邻容器直接接收。

## 生产与容量

| 项目 | 说明 |
|------|------|
| 基础速度 | 1x 每 Tick 累计 512 生长工作量；默认每 20 Tick 合并结算 |
| 单次消耗 | 10 FE；配方流体成本除以 100 后向上取整，最低 1 mB |
| 模板堆叠 | 不超过物品堆叠上限一半时为 1 倍流体，超过一半为 2 倍 |
| 输出 | 基础 16 格；每张容量升级增加 16 格，最多 64 格 |
| 单槽上限 | 基础 64；安装 1/2/3 张容量升级后为 2048/4096/8192 |

输出堵塞时生产会自动暂停，不会将物品丢到世界中。横向相邻的温室只会连接外观，库存和升级仍各自独立。

## 植物兼容

解析顺序为：JDTE 数据配方 → 神秘农业专用集成 → Botany Pots → 通用植物。

- 内置常见原版农作物、下界疣、可可豆和树苗配方。
- Mystical Agriculture 与 Mystical Agradditions 通过公开 Crop Registry 自动兼容，并使用对应成熟作物掉落。
- 直接读取当前加载的全部 Botany Pots 作物配方，包括孢子花以及模组或数据包添加的盆栽植物。
- 其他带 `age` 属性的作物，以及常见花朵、树苗和蘑菇会尝试通用识别。
- 整合包可添加 `jdte:greenhouse` 数据配方来明确指定模板、产物和流体成本。

JEI 会显示模板、基础消耗和预览产物。点击界面的苗床进度区域可直接打开该分类。

## KubeJS：可配置流体

使用 `jdte:greenhouse` 配方替换植物配方；普通温室、大型温室和温室矩阵（温室矩阵位于独立的 JDTE-Matrix 模组中）都使用这一格式。

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'jdte:greenhouse/wheat' })
  event.custom({
    type: 'jdte:greenhouse',
    seed: { item: 'minecraft:wheat_seeds' },
    outputs: [{ id: 'minecraft:wheat', count: 2 }],
    display_block: 'minecraft:wheat',
    growth_work: 20,
    fluid: 'minecraft:water',
    time_fluid: 100
  }).id('jdte:greenhouse/wheat')
})
```

`fluid` 可省略，默认值为 `justdirethings:time_fluid_source`。`time_fluid` 仍表示每次收获消耗的数量，而不是流体 ID。普通温室只有一个储罐，因此不会混装流体；`/reload` 后旧流体仍可抽出，但只能驱动仍要求该流体的配方。

## 升级与自动化

- 支持：容量、流体、时运、超频、创造升级。
- 每张时运升级使最终平均产量增加 10%，最多 3 张。
- 超频或创造升级将倍率固定为 64x；创造升级免除资源消耗。
- 自动 I/O 支持模板与配方所需流体（默认时间流体）输入、产物输出；高倍率产物会在 Tick 末合并后直推相邻库存。

## 合成

<RecipeFor id="jdte:greenhouse" />
