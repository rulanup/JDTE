---
navigation:
  title: 流体发送器
  icon: "jdte:advanced_fluid_sender"
  position: 10
item_ids:
  - jdte:basic_fluid_sender
  - jdte:advanced_fluid_sender
  - jdte:extended_fluid_sender
---

# 流体发送器

流体发送器可以将内部流体发送到配置范围内的目标容器，不会在世界中放置流体源方块。

## 变体

| 机器 | 升级槽 | 每次发送量 | 需要 FE |
|------|--------|-----------|---------|
| 初级流体发送器 | 4 | 全部可用流体 | 否 |
| 高级流体发送器 | 4 | 全部可用流体 | 是 |
| 扩展流体发送器 | 8 | 全部可用流体 | 是 |

## 功能

- 将内部储罐中的流体发送到范围内的流体容器
- 在同一范围内按轮询顺序分配流体，避免靠近机器或位于正面的容器长期独占传输
- 支持红石控制、范围升级、过滤升级
- 默认启用无限批量，每次操作发送储罐中全部可用流体；可在配置中关闭并改用普通/超频批量限制
- 安装超频或创造升级并开启自动输入后，可将自动输入侧容器中的流体直接发送到范围目标，不受内部储罐容量限制
- 速度按钮和降频升级控制执行间隔，容量升级提高内部储罐容量

## 合成

### 初级流体发送器

<BlockImage id="jdte:basic_fluid_sender" scale="2" />

<RecipeFor id="jdte:basic_fluid_sender" />

### 高级流体发送器

<BlockImage id="jdte:advanced_fluid_sender" scale="2" />

<RecipeFor id="jdte:advanced_fluid_sender" />

### 扩展流体发送器

<BlockImage id="jdte:extended_fluid_sender" scale="2" />

高级流体发送器的扩展版本，拥有 8 个升级槽。可通过扩展升级右键高级流体发送器获得。

<RecipeFor id="jdte:extended_fluid_sender" />
