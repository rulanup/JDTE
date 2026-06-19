---
navigation:
  title: 流体稳定器
  icon: "jdte:advanced_fluid_stabilizer"
  position: 13
item_ids:
  - jdte:basic_fluid_stabilizer
  - jdte:advanced_fluid_stabilizer
  - jdte:extended_fluid_stabilizer
  - jdte:time_fluid_catalyst
---

# 流体稳定器

流体稳定器会扫描配置范围内的源流体方块，并按催化剂槽内物品匹配 JDT 的 FluidDrop 配方。

- 每次运行最多转换 1 个源流体方块。
- 每次成功转换消耗 1 个催化剂。
- 创造升级可免除 FE 和催化剂消耗。
- 过滤槽使用流体桶作为过滤物品。
- 支持红石控制、范围升级、过滤升级、超频、降频和容量升级。

例如放入 JDT 的多态催化剂时，源水可转换为多态流体；放入 JDTE 的时间流体催化剂时，源水可转换为时间流体。

## 初级流体稳定器

<BlockImage id="jdte:basic_fluid_stabilizer" scale="2" />

无需 FE，仅消耗催化剂。运行间隔较长（40 tick）。

<RecipeFor id="jdte:basic_fluid_stabilizer" />

## 高级流体稳定器

<BlockImage id="jdte:advanced_fluid_stabilizer" scale="2" />

需要 FE 和催化剂。运行间隔 20 tick，FE 消耗随配置范围增加。

<RecipeFor id="jdte:advanced_fluid_stabilizer" />

## 扩展流体稳定器

<BlockImage id="jdte:extended_fluid_stabilizer" scale="2" />

高级流体稳定器的扩展版本，拥有 8 个升级槽。可通过扩展升级右键高级流体稳定器获得。

<RecipeFor id="jdte:extended_fluid_stabilizer" />
