# 高级机器设置复制器设计

## 背景

`AdvancedMachineSettingsCopierItem` 当前只扩展了 JDT 原生复制器的区域、偏移、过滤器、红石、升级卡和自动 I/O 面配置。JDT 与 JDTE 的多个机器界面还提供了机器专用设置，因此复制后目标机器仍会保留自己的倍率、模式、开关或专用过滤条件。

本次改动让复制器覆盖当前项目中所有可调节的机器配置，同时保留存储、运行时和世界拓扑状态的隔离。

## 目标与非目标

### 目标

- 同一方块实体类型之间复制所有可调节机器设置。
- 兼容 JDT 原生机器和 JDTE 机器。
- 兼容已有复制器物品中保存的旧格式数据。
- 在校验失败、数据损坏或升级材料不足时不产生部分修改。
- 保留复制器界面现有的区域、偏移、过滤器和红石开关语义。

### 非目标

- 不复制机器库存中的实际物品、流体或 FE。
- 不复制生产进度、燃料、运行状态、计数器、缓存、网络发现结果或分页位置。
- 不复制 `placedBy` 所有者。
- 不复制 Block Swapper 的世界坐标伙伴连接。
- 不复制 Paradox Machine 的快照内容；只复制快照显示和目标类型设置。

## 复制字段

### 通用字段

以下字段属于所有机器可用的通用设置：

- JDT 复制器已有的区域半径、区域偏移、区域显示、过滤器和红石设置。
- 基础机器 tick 速度和内部方向值。方向只改变机器内部工作方向，不改变目标方块的结构朝向。
- JDTE 自动 I/O 支持的六个方向输入、输出面掩码。
- 所有实际的升级槽，包括标准升级槽和机器专用升级槽。

区域、偏移、过滤器和红石字段继续分别受复制器上的四个选项控制。tick 速度、内部方向、自动 I/O 和升级槽属于高级复制器的机器设置，始终复制。传感器的方块状态过滤条件和 Inventory Holder 的独立过滤槽跟随过滤器选项。

### JDTE 机器专用字段

| 机器 | 复制的设置 |
| --- | --- |
| Advanced/Extended Time Accelerator | 倍率 |
| Crystal Incubator | 倍率 |
| Greenhouse/Large Greenhouse | 倍率 |
| Bio Factory | 倍率 |
| Life Breeder | 倍率、工作模式 |
| Life Extractor | 工作模式 |
| Mineral Extractor/Large Mineral Extractor | 倍率 |
| Life Synthesis Vat | 倍率 |
| Bio Crusher | 工作模式、Looting 和 Sharpness 专用升级槽 |
| Entity Suppressor | 模式、目标类型、黑白名单 |
| Range Blocker | 模式、目标类型、黑白名单 |
| Time Freezer/Extended Time Freezer | 时间冻结、天气冻结开关 |
| Gel Generator | 输入自动平衡开关 |
| Advanced Potion Brewer | 配方锁定、锁定配方模板、燃料输入开关 |
| Energy Transmitter/Extended Energy Transmitter | 粒子显示开关 |
| Advanced Energy Transmitter | 粒子显示开关、玩家绑定 UUID 和名称 |
| Extended Experience Holder | 目标经验值、仅所有者、收集经验、粒子显示 |

Bio Factory、Loot Fabricator、Advanced Potion Brewer 等机器的实际专用升级槽统一纳入升级槽快照，并参与粘贴时的材料校验和原子扣除。

### JDT 原生机器专用字段

| 机器 | 复制的设置 |
| --- | --- |
| Clicker T1/T2 | 点击类型、目标类型、潜行、模拟玩家、长按 tick 数 |
| Dropper T1/T2 | 丢弃数量、拾取延迟 |
| Block Breaker T1/T2 | 潜行模式 |
| Block Swapper T1/T2 | 方块交换开关、实体交换类型 |
| Sensor T1/T2 及 JDTE Extended Sensor | 感知目标、强/弱红石、感知数量、比较方式、每个过滤槽的方块状态属性 |
| Item Collector | 尊重拾取延迟、粒子显示 |
| Experience Holder | 目标经验值、仅所有者、收集经验、粒子显示 |
| Energy Transmitter | 粒子显示 |
| Inventory Holder | NBT 比较、仅过滤器、仅自动过滤器、数量比较、自动数量比较、显示槽位、显示玩家，以及独立过滤槽内容 |
| Player Accessor | 六个方向的玩家库存类型 |
| Paradox Machine | Paradox 显示、目标类型 |

Generator、Block Placer、Fluid Placer、Fluid Collector 和没有额外界面设置的机器只复制通用字段及已有的 JDT 设置。

## 不复制状态的边界

机器的输入/输出物品、内部流体、能量、经验存量、燃料存量、生产进度、序列状态、运行计时器、输出分页、临时缓存、目标列表、自动 I/O 运行退避和管理器注册状态均属于目标机器自己的运行状态，不进入配置快照。

Advanced Energy Transmitter 的玩家绑定是界面可调节配置，因此复制绑定 UUID 和显示名称；目标机器的 `placedBy` 不改变。Block Swapper 的 `GlobalPos` 伙伴连接依赖世界拓扑，复制后会指向错误位置，因此保留目标原连接。

## 数据格式

### 现有数据

保留 `jdteAutoIoConfig` 根节点中的 `machineType`、自动 I/O 面掩码，以及现有顶层 `upgrades` 数据。旧复制器物品仍可复制区域、偏移、过滤器、红石、升级和自动 I/O；缺少新设置节点时，目标专用设置保持不变。

### 新数据

新增版本化的 `jdteMachineSettings` 节点：

```text
jdteMachineSettings {
  schemaVersion: 1
  common {
    tickSpeed: int
    direction: int
  }
  custom { ... }
}
```

`custom` 由机器类型对应的显式 Codec 写入。枚举使用稳定名称或经过范围校验的值；物品模板使用带注册表访问器的标准 ItemStack NBT。保存时每次从源机器生成新快照，避免旧复制数据残留。

## Codec 架构

新增机器设置快照模型和 Codec 注册表：

- 通用 Codec 负责 tick 速度和方向。
- JDT/JDTE 专用 Codec 按精确 `BlockEntityType` 注册，并只访问公开 API 或为 JDTE 自有类添加明确的复制辅助方法。
- Codec 分成 `读/校验` 和 `应用` 两阶段，先生成不可变快照，再对目标机器应用。
- 不使用全量 NBT 排除字段，也不使用反射扫描字段，避免把库存、缓存和运行状态意外带过去。
- 未知或损坏的 schema、枚举、方向、槽位和 ItemStack 数据会使本次粘贴失败，并保留目标和玩家背包不变。

## 保存流程

1. 调用 JDT 父复制器保存用户勾选的区域、偏移、过滤器和红石数据。
2. 记录精确的方块实体类型 ID。
3. 读取通用设置、机器专用设置、自动 I/O 面掩码和全部升级槽。
4. 对专用设置执行范围校验，并写入版本化节点。
5. 将完整快照写入复制器的 `CustomData`。

## 粘贴流程

1. 验证复制数据存在，且精确匹配目标方块实体类型。
2. 在修改目标或消耗材料前，完整解析通用设置、专用设置、自动 I/O 和全部升级槽。
3. 统计标准及专用升级槽所需材料；玩家背包不足时整体拒绝。
4. 原子扣除升级材料。
5. 应用父复制器设置、升级槽、通用设置、专用设置和自动 I/O。
6. 调用机器已有的 setter、容量同步、过滤缓存刷新和管理器刷新逻辑。
7. 标记目标机器和客户端同步状态为已修改。

直接调用 `loadSettings` 时也执行相同的类型和数据校验，但不执行玩家材料扣除。

## 测试策略

先为新行为编写失败测试，再实现 Codec 和复制流程。测试覆盖：

- 通用 tick 速度、方向、区域、过滤器、红石和六面自动 I/O。
- 每个 JDTE 专用设置及 JDT 原生机器设置。
- 传感器方块状态属性、Inventory Holder 独立过滤槽和所有专用升级槽。
- 同类型复制、跨类型拒绝、旧格式兼容。
- 损坏数据、非法枚举、非法方向、非法槽位和未知 schema。
- 升级材料完整检查、重复升级材料和失败时背包不变。
- 失败粘贴时目标机器不被部分修改。
- 运行库存、流体、能量、进度和缓存未被复制。

## 验收标准

- 当前仓库登记的 JDT/JDTE BaseMachineBE 机器，其界面或设置 payload 可调字段均有对应 Codec 测试。
- 同类型目标粘贴后，所有可调设置与源机器一致。
- 非配置运行状态和世界拓扑状态不被复制。
- 旧复制器数据仍可正常使用。
- `compileJava`、相关单元测试和最终构建验证通过。
