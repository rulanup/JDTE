---
navigation:
  title: 分级太阳能板
  icon: "jdte:concentrated_solar_panel"
  position: 2.8
item_ids:
  - jdte:concentrated_solar_panel
  - jdte:singularity_solar_panel
  - jdte:stellar_fusion_solar_panel
  - jdte:dimensional_collapse_solar_panel
  - jdte:creative_solar_panel
---

# 分级太阳能板

<ItemGrid>
  <ItemIcon id="jdte:concentrated_solar_panel" />
  <ItemIcon id="jdte:singularity_solar_panel" />
  <ItemIcon id="jdte:stellar_fusion_solar_panel" />
  <ItemIcon id="jdte:dimensional_collapse_solar_panel" />
  <ItemIcon id="jdte:creative_solar_panel" />
</ItemGrid>

JDT Extras 的太阳能板比 JDT 基础太阳能板造价更高，并按等级提供更大的发电量和内部缓冲。生存等级的默认数值可以由服务端配置修改。

## 等级比较

| 等级 | 默认基础发电 | 内部容量 |
|---|---:|---:|
| 聚能太阳能板 | 46,080 FE/t | 4,608,000 FE |
| 奇点太阳能板 | 184,320 FE/t | 18,432,000 FE |
| 恒星聚变太阳能板 | 737,280 FE/t | 73,728,000 FE |
| 维度坍缩太阳能板 | 2,949,120 FE/t | 294,912,000 FE |
| 创造太阳能板 | 无限能源源 | `Integer.MAX_VALUE` FE 能力视图 |

创造太阳能板不会耗尽。由于 NeoForge FE 能力的单次请求量使用 32 位整数，它可以在每次能力请求中提供最多 **2,147,483,647 FE**，并在之后的请求中继续提供能源。

## 发电条件与阵列

生存等级只在白天、面板正上方为空气且能够看见天空时发电。实际发电量还受高度影响：越远离世界垂直中心，倍率越接近完整值；靠近垂直中心时倍率最低。

同一高度的 3×3 范围内若有至少七个同级太阳能板，邻接倍率变为 2x。不同等级不会互相提供邻接加成。

所有太阳能板都会主动向六个方向轮转输出，优先把能源送入相邻且能够接收 FE 的方块。创造太阳能板不受昼夜、天空或高度限制。

## 合成

<RecipeFor id="jdte:concentrated_solar_panel" />
<RecipeFor id="jdte:singularity_solar_panel" />
<RecipeFor id="jdte:stellar_fusion_solar_panel" />
<RecipeFor id="jdte:dimensional_collapse_solar_panel" />

创造太阳能板只能从创造模式物品栏或管理命令获取，没有生存合成配方。
