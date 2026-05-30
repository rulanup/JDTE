---
navigation:
  title: 流体稳定器
  icon: "jdte:fluid_stabilizer"
  position: 5
item_ids:
  - jdte:fluid_stabilizer
  - jdte:time_fluid_catalyst
---

# 流体稳定器

<BlockImage id="jdte:fluid_stabilizer" scale="2" />

流体稳定器会扫描配置范围内的源流体方块，并按催化剂槽内物品匹配 JDT 的 FluidDrop 配方。

- 每次运行最多转换 1 个源流体方块。
- 每次成功转换消耗 FE 和 1 个催化剂。
- 创造升级可免除 FE 和催化剂消耗。
- FE 消耗会随配置范围增加。
- 过滤槽使用流体桶作为过滤物品。
- 支持红石控制、范围升级、过滤升级、超频、降频和容量升级。

例如放入 JDT 的多态催化剂时，源水可转换为多态流体；放入 JDTE 的时间流体催化剂时，源水可转换为时间流体。

<RecipeFor id="jdte:fluid_stabilizer" />
