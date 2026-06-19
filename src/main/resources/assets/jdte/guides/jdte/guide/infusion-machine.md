---
navigation:
  title: 灌注机
  icon: "jdte:advanced_infusion_machine"
  position: 15
item_ids:
  - jdte:advanced_infusion_machine
  - jdte:extended_infusion_machine
  - jdte:life_apple
---

# 灌注机

灌注机消耗物品和流体，根据灌注配方输出新的物品。它是生命流体的主要消耗途径，可将普通物品与生命流体结合转化为特殊产物。

## 变体

| 机器 | 升级槽 | 能量容量 | 流体容量 |
|------|--------|----------|----------|
| 高级灌注机 | 4 | 50,000 FE | 8,000 mB |
| 扩展灌注机 | 8 | 100,000 FE | 8,000 mB |

## 功能

### 工作流程

1. 将输入物品放入左侧输入槽
2. 将流体（如生命流体）注入内部储罐
3. 机器自动匹配灌注配方并开始处理
4. 处理完成后产物出现在右侧输出槽

### 槽位

- 输入槽：1 个，放置待灌注的物品
- 输出槽：1 个，收集灌注产物

### 处理参数

- 处理时间：20 tick（1 秒）
- 基础能耗：500 FE/次
- 流体接受任意类型，配方决定所需流体

### 灌注配方示例

| 输入 | 流体 | 产物 | 能耗 |
|------|------|------|------|
| 苹果 | 生命流体 1000 mB | 生命苹果 | 500 FE |

### 升级效果

| 升级 | 效果 |
|------|------|
| 超频 | 锁定 1 tick 运行，能耗 3 倍 |
| 降频 | 锁定 40 tick 运行，能耗降至 20% |
| 创造 | 免能耗，含超频效果 |
| 容量 | 提升 FE 和流体容量 |
| 流体 | 仅提升流体容量 |

## 合成

### 高级灌注机

<BlockImage id="jdte:advanced_infusion_machine" scale="2" />

<RecipeFor id="jdte:advanced_infusion_machine" />

### 扩展灌注机

<BlockImage id="jdte:extended_infusion_machine" scale="2" />

高级灌注机的扩展版本，拥有 8 个升级槽。可通过扩展升级右键高级灌注机获得。

<RecipeFor id="jdte:extended_infusion_machine" />
