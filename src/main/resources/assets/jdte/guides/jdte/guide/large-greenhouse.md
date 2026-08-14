---
navigation:
  title: 大型温室大棚
  icon: "jdte:large_greenhouse"
  position: 19.5
item_ids:
  - jdte:large_greenhouse
---

# 大型温室大棚

<ItemImage id="jdte:large_greenhouse" scale="2" />

大型温室大棚是一次放置成型的 **3×3×2** 机器。它提供 9 条独立生产线，植物模板可重复使用，只消耗时间流体和 FE。

## 快速使用

1. 预留完整的 3×3×2 空间并放置温室。
2. 放入最多 9 种种子、花朵或树苗；模板数量代表并行种植数量。
3. 接入 FE 和时间流体，选择 1-32x 速度。
4. 从底层基座任意面接管道，或在界面中配置自动输出。

## 核心参数

| 项目 | 数值 |
|------|------|
| 速度 | 1-32x；超频或创造升级固定为 64x |
| 基础消耗 | 每次收获 10 FE；时间流体按植物配方计算 |
| 大型结构加成 | 每条生产线 9 倍生产速度；批量时间流体成本再除以 9 |
| 输出库存 | 基础 16 格；每张容量升级增加 16 格，最多 64 格 |
| 单槽上限 | 无升级 64；1/2/3 张容量升级为 2048/4096/8192 |
| 时运 | 最多 3 张，每张使长期平均产出增加 10% |

小批次时间流体成本最低为 1 mB，因此低速运行时可能无法获得完整的 9 倍流体效率。拆除容量升级不会删除已有的超量堆叠，但数量降到当前上限前不能继续填入。

## 升级

- 支持：容量、流体、超频、创造、时运。
- 不支持：范围、过滤、降频、精准。

## 植物与配方

- 内置常用原版作物、树苗、花朵和同类植物。
- 自动发现 Mystical Agriculture、Mystical Agradditions 和 Botany Pots 作物。
- 通用识别带成熟年龄的模组作物，并保留成熟方块掉落表中的副产物。
- 整合包可添加共享的 `jdte:greenhouse` 数据配方。

JEI 会显示模板、基础 FE、时间流体和预览产物；点击界面的苗床进度区域也可打开对应分类。

## KubeJS：可配置流体

大型温室使用共享的 `jdte:greenhouse` 配方格式。以下示例让胡萝卜消耗水而不是默认时间流体：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'jdte:greenhouse',
    seed: { item: 'minecraft:carrot' },
    outputs: [{ id: 'minecraft:carrot', count: 2 }],
    display_block: 'minecraft:carrots',
    growth_work: 20,
    fluid: 'minecraft:water',
    time_fluid: 100
  }).id('kubejs:watered_carrot')
})
```

省略 `fluid` 时使用 `justdirethings:time_fluid_source`；`time_fluid` 仍是每次收获的数量。机器的单个储罐不会混装流体；`/reload` 后旧内容仍可抽出，但只会驱动所需流体仍匹配的配方。

## 自动化与加速

产物先进入内部库存，再于真实服务器 Tick 末合批输出；容器堵塞时生产会受到背压。时间加速器产生的虚拟 Tick 会合并结算，不会重复冲击库存。Botany Pots 等动态收获默认每台温室每个真实 Tick 最多执行 128 次，剩余工作会保留到后续 Tick。

## 放置与拆除

控制器位于正面中央底层，其余 17 格为结构部件。底层基座任意面都可输入种子、时间流体和 FE，也可抽取产物。右击任意部件会打开控制器；破坏任意部件会拆除整座结构，并只返还一个大型温室物品。

## 合成

<RecipeFor id="jdte:large_greenhouse" />
