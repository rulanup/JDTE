# JDT Extras 开发文档

## 项目概述

JDT Extras (`jdte`) 是 Just Dire Things (JDT) 的 NeoForge 扩展模组，围绕 JDT 机器系统增加升级卡、时间加速器、扩展机器和额外自动化机器。

| 项目 | 当前值 |
|------|--------|
| 模组 ID | `jdte` |
| 模组名称 | `JDT Extras` |
| 当前版本 | `0.5.0` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.230+` |
| Just Dire Things | `1.5.7+` |
| Java | `21` |

当前主要功能：

- 11 种升级卡：容量、超频、降频、流体、存储流体、发电机、范围、过滤、创造、抢夺、锋利。
- 3 种时间加速器：初级、高级、扩展高级。
- 8 种 JDT T2 机器的扩展版本，每台拥有 8 个升级槽。
- 扩展高级时间加速器，可由高级时间加速器转换得到。
- 额外自动化机器：粘胶激活器、凝胶发生器、流体稳定器、物品/流体放置器、物品/流体接收器。
- 生物粉碎机：高级、扩展；杀死生物产生掉落物和经验流体，支持抢夺和锋利升级。
- BOSS 精华：凋灵精华、末影龙精华、远古守卫者精华。

## 构建与运行

本项目使用 Gradle 和 NeoForge ModDev 插件构建。JDT 作为本地 jar 依赖，不是 Gradle 子模块。

本地开发依赖：

```text
/home/guili/libs/justdirethings-1.5.7.jar
```

常用命令：

```bash
./gradlew compileJava
./gradlew jar
./gradlew runClient
./gradlew runServer
```

构建失败并提示找不到 JDT jar 时，需要先构建或复制 JDT jar 到上述路径。

## 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| NeoForge | `21.1.230` | 模组加载器和 API |
| Just Dire Things | `1.5.7` | 基础机器、接口、配置和时间流体 |
| GuideME | `21.1.16` | 游戏内指南文档 |
| Parchment | `2024.11.17` | Minecraft 映射 |

关键 JDT 包：

- `com.direwolf20.justdirethings`
- `com.direwolf20.justdirethings.setup.Registration`
- `com.direwolf20.justdirethings.setup.Config`

## 项目结构

```text
src/main/
├── java/com/jdte/
│   ├── JDTE.java                         # 模组入口、能力注册、网络注册监听
│   ├── client/                           # 客户端初始化、GUI、渲染器
│   ├── common/                           # 双端通用逻辑
│   │   ├── blockentities/                # 方块实体和机器逻辑
│   │   ├── blocks/                       # 方块定义
│   │   ├── containers/                   # 菜单、容器、槽位
│   │   ├── entities/                     # 时间加速视觉效果实体
│   │   ├── items/                        # 升级卡和扩展升级物品
│   │   ├── network/                      # 网络包和处理器
│   │   ├── upgrades/                     # 升级系统核心
│   │   └── utils/                        # 通用工具
│   ├── mixin/                            # Mixin 注入和 accessor
│   └── setup/                            # DeferredRegister 注册类
└── resources/
    ├── assets/jdte/
    │   ├── blockstates/                  # 方块状态
    │   ├── guideme_guides/               # GuideME 配置
    │   ├── guides/jdte/guide/            # GuideME 页面
    │   ├── lang/                         # 语言文件
    │   ├── models/                       # 方块/物品模型
    │   └── textures/                     # 贴图和 GUI 资源
    ├── data/jdte/                        # 配方、战利品表、标签
    └── META-INF/neoforge.mods.toml       # 模组元数据和依赖声明
```

## 注册流程

所有核心注册都在 `JDTE.java` 构造函数中完成。

当前注册顺序：

1. `JDTEBlocks`：方块注册。
2. `JDTEItems`：物品和 BlockItem 注册。
3. `JDTEBlockEntities`：方块实体类型注册。
4. `JDTEMenus`：菜单类型注册。
5. `JDTEAttachments`：数据附件注册。
6. `JDTECreativeTabs`：创造模式标签页注册。
7. `JDTEEntities`：实体类型注册。
8. `registerCapabilities()`：注册能量和流体能力。
9. `JDTEPacketHandler.registerNetworking()`：注册网络包。

新增机器时通常需要同步更新 `JDTEBlocks`、`JDTEItems`、`JDTEBlockEntities`、`JDTEMenus`、`JDTECreativeTabs`、`JDTEClientSetup` 和 `JDTE.registerCapabilities()`。

## 升级系统

### 升级类型

`UpgradeType` 定义序列化名和每台机器上限。

| 类型 | 序列化名 | 上限 | 作用 |
|------|----------|------|------|
| `CAPACITY` | `capacity` | 3 | FE 容量和流体容量按 2 的幂翻倍 |
| `OVERCLOCK` | `overclock` | 1 | 锁定 1 tick 运行，允许额外执行，能耗变为 3 倍 |
| `UNDERCLOCK` | `underclock` | 1 | 锁定 40 tick 运行，能耗降至 20% |
| `FLUID` | `fluid` | 3 | 仅流体容量按 2 的幂翻倍 |
| `FLUID_STORAGE` | `fluid_storage` | 1 | Clicker 专用，添加内部流体储罐 |
| `GENERATOR` | `generator` | 1 | JDT 发电机专用，2 倍燃料输入换 3 倍发电量 |
| `RANGE` | `range` | 2 | 范围机器专用，扩大可配置范围上限 |
| `FILTER` | `filter` | 2 | 可过滤机器专用，每张卡增加 9 个过滤槽 |
| `CREATIVE` | `creative` | 1 | 免 FE 消耗；时间加速器免时间流体消耗；包含超频效果 |

### 升级槽

- `UpgradeItemStackHandler`：普通机器升级处理器，固定 4 个槽位。
- `ExtendedUpgradeItemStackHandler`：扩展机器升级处理器，固定 8 个槽位。
- 每个升级槽 `getSlotLimit()` 为 1。
- `UpgradeItemStackHandler.isItemValid()` 负责检查升级卡类型、机器兼容性、数量上限和超频/降频互斥。
- `UpgradeHelper.getUpgradeHandler()` 通过 `ExtendedUpgradeMachine` marker 判断使用普通还是扩展升级附件。

### 容量与消耗

- `UpgradeHelper.adjustEnergyCapacity()` 只受 `CAPACITY` 影响。
- `UpgradeHelper.adjustFluidCapacity()` 同时受 `CAPACITY` 和 `FLUID` 影响。
- `UpgradeHelper.adjustEnergyCost()` 处理创造、降频、超频能耗规则。
- `UpgradeHelper.getEffectiveTickSpeed()` 处理普通机器的锁定 tick 速度。
- `UpgradeHelper.getMaxAreaRadius()` 和 `getMaxAreaOffset()` 处理范围升级。
- `UpgradeHelper.getActiveFilterSlots()` 根据过滤升级动态开放额外过滤槽。
- `UpgradeHelper.fillClickerItemFromTank()` 处理 Clicker 内部流体储罐向槽位物品填充。

## 时间加速器

`TimeAcceleratorBE` 是时间加速器基类，继承 JDT `BaseMachineBE`，并实现 `RedstoneControlledBE`、`AreaAffectingBE`、`FluidMachineBE`、`FilterableBE` 和 `TimeAcceleratorMachine`。

核心行为：

- 基础流体容量为 `1000 mB`，流体槽只接受 JDT `TimeFluid`。
- 每 tick 检查红石状态和 `canRun()`，满足条件时扫描配置范围。
- 跳过其他 `TimeAcceleratorMachine`，避免时间加速器互相加速。
- 只加速 `MiscTools.isValidTickAccelBlock()` 允许的方块或方块实体。
- 对方块实体重复调用 ticker；对随机刻方块重复调用 `randomTick()`。
- 加速成功后再消耗时间流体和 FE。
- 支持过滤槽，只加速过滤允许的目标。

| 方块实体 | 继承关系 | 倍率/消耗 |
|----------|----------|-----------|
| `BasicTimeAcceleratorBE` | `TimeAcceleratorBE` | 默认 4x；超频或创造升级后 16x；只消耗时间流体 |
| `AdvancedTimeAcceleratorBE` | `TimeAcceleratorBE`, `PoweredMachineBE` | 可调 1-128x；超频或创造升级后 256x；消耗时间流体和 FE |
| `ExtendedTimeAcceleratorBE` | `AdvancedTimeAcceleratorBE`, `ExtendedUpgradeMachine` | 高级时间加速器的 8 槽扩展版本 |

高级时间加速器常量：

- `BASE_ENERGY_CAPACITY = 200000`
- `MAX_MULTIPLIER = 128`
- `OVERCLOCK_MULTIPLIER = 256`
- 能耗基于 `Config.TIMEWAND_RF_COST`
- 流体消耗基于 `Config.TIMEWAND_FLUID_COST`

## 扩展机器

扩展机器通过 `ExtendedUpgradeMachine` marker 获得 8 个升级槽。JDT T2 机器使用 `ExtendedUpgradeItem` 右键转换，转换时会保存旧方块实体 NBT 并加载到新方块实体。

| 原机器 | 扩展方块 | 方块实体 | 基类 |
|--------|----------|----------|------|
| JDT Clicker T2 | `extended_clicker` | `ExtendedClickerBE` | `ClickerT1BE` |
| JDT Block Breaker T2 | `extended_block_breaker` | `ExtendedBlockBreakerBE` | `BlockBreakerT1BE` |
| JDT Block Placer T2 | `extended_block_placer` | `ExtendedBlockPlacerBE` | `BlockPlacerT1BE` |
| JDT Block Swapper T2 | `extended_block_swapper` | `ExtendedBlockSwapperBE` | `BlockSwapperT1BE` |
| JDT Dropper T2 | `extended_dropper` | `ExtendedDropperBE` | `DropperT1BE` |
| JDT Sensor T2 | `extended_sensor` | `ExtendedSensorBE` | `SensorT1BE` |
| JDT Fluid Collector T2 | `extended_fluid_collector` | `ExtendedFluidCollectorBE` | `FluidCollectorT1BE` |
| JDT Fluid Placer T2 | `extended_fluid_placer` | `ExtendedFluidPlacerBE` | `FluidPlacerT1BE` |
| JDTE Advanced Time Accelerator | `extended_time_accelerator` | `ExtendedTimeAcceleratorBE` | `AdvancedTimeAcceleratorBE` |

注意事项：

- 扩展方块实体构造函数必须传入自己的 `JDTEBlockEntities.*` 类型，不能传父类的类型。
- 容器的 `stillValid()` 必须检查当前扩展方块，而不是宽泛检查 `BaseMachineBlock`。
- 需要能力的扩展机器必须在 `JDTE.registerCapabilities()` 中显式注册。
- 如果新增可通过扩展升级转换的机器，需要更新 `ExtendedUpgradeItem.UPGRADE_MAP`。

## 额外自动化机器

这些机器是 JDTE 自己实现的机器族，通常分为初级、高级、扩展三个等级。高级和扩展版本通常实现 `PoweredMachineBE`，扩展版本实现 `ExtendedUpgradeMachine`。

| 机器族 | 已注册等级 | 基础方块实体 | 说明 |
|--------|------------|--------------|------|
| 粘胶激活器 | 初级、高级、扩展 | `GlueActivatorBE` | 处理 JDT 粘胶相关自动化 |
| 凝胶发生器 | 高级、扩展 | `GelGeneratorBE` | 消耗输入并产出凝胶相关结果，带流体和能量能力 |
| 流体稳定器 | 初级、高级、扩展 | `FluidStabilizerBE` | 使用催化剂槽匹配 JDT FluidDrop 配方，在配置范围内直接转换源流体 |
| 物品放置器 | 初级、高级、扩展 | `ItemSenderBE` | 将物品发送到配置范围内目标 |
| 流体放置器 | 初级、高级、扩展 | `FluidSenderBE` | 将内部流体发送到配置范围内目标 |
| 物品接收器 | 初级、高级、扩展 | `ItemReceiverBE` | 从配置范围内目标接收物品 |
| 流体接收器 | 初级、高级、扩展 | `FluidReceiverBE` | 从配置范围内目标接收流体 |
| 生物粉碎机 | 高级、扩展 | `BioCrusherBE` | 杀死生物产生掉落物和经验流体，支持抢夺和锋利升级，可放置在刷怪笼上方 |

能力注册现状：

- 时间加速器注册流体能力，高级和扩展时间加速器注册能量能力。
- 扩展 JDT 机器注册能量能力。
- 粘胶激活器的高级和扩展版本注册能量能力。
- 凝胶发生器注册能量和流体能力。
- 流体稳定器的高级和扩展版本注册能量能力，使用内部催化剂槽，不注册流体能力。
- 物品发送/接收器的高级和扩展版本注册能量能力。
- 流体发送/接收器注册流体能力，高级和扩展版本注册能量能力。
- Clicker 安装 `FLUID_STORAGE` 后通过 JDT Clicker 方块注册流体能力。

## Mixin 注入

Mixin 配置文件：`src/main/resources/mixins.jdte.json`。

服务端/通用 mixin：

| Mixin | 主要用途 |
|-------|----------|
| `BaseMachineBEMixin` | 挂载升级处理器、同步升级容量和持久化相关数据 |
| `BaseMachineBEPopupMixin` | 保存升级/过滤弹窗位置 |
| `BaseMachineBlockMixin` | 支持超频额外执行逻辑 |
| `BaseMachineContainerMixin` | 向 JDT 机器容器注入升级槽 |
| `BaseMachineContainerFilterMixin` | 动态扩展过滤槽 |
| `ClickerFluidMixin` | Clicker 流体储罐升级逻辑 |
| `AreaAffectingBEMixin` | 范围升级对区域配置上限的影响 |
| `EnergyCostMixin` | 普通机器能耗调整 |
| `ParadoxEnergyCostMixin` | Paradox 相关能耗调整 |
| `FluidCapacityMixin` | 流体容量调整 |
| `GeneratorT1UpgradeMixin` | 固体燃料发电机升级逻辑 |
| `GeneratorFluidUpgradeMixin` | 流体燃料发电机升级逻辑 |
| `PoweredMachineDefaultMaxEnergyMixin` | 默认能量容量调整入口 |
| `PoweredMachineOverrideMaxEnergyMixin` | 覆盖式能量容量调整入口 |
| `SpawnerMixin` | 生物粉碎机拦截刷怪笼逻辑 |
| `TickSpeedPacketMixin` | tick 速度包与升级逻辑兼容 |
| `EnergyStorageAccessor` | 修改 JDT/NeoForge 能量存储容量字段 |
| `FluidTankAccessor` | 修改流体槽容量字段 |
| `FilterBasicHandlerAccessor` | 修改过滤处理器内部槽位 |

客户端 mixin：

| Mixin | 主要用途 |
|-------|----------|
| `BaseMachineScreenMixin` | 升级弹窗、过滤弹窗、流体条、槽位重排 |
| `ScreenMixin` | 访问客户端 screen 字段 |
| `AbstractContainerScreenMixin` | 访问容器屏幕布局字段 |
| `SlotAccessor` | 动态移动槽位位置 |

Mixin 规范：

- Mixin 类不要显式继承目标类或其他父类。
- 自定义字段和方法使用 `@Unique`，命名使用 `jdte$` 前缀。
- 目标字段优先使用 `@Shadow`，字段名必须匹配当前映射。
- 当方法不在目标类类型上时，使用反射或接口判断，不要强行继承。
- 修改 GUI 槽位位置时只在状态变化和布局更新时处理，避免每帧写入。

## 资源文件规范

新增物品通常需要：

- `src/main/resources/assets/jdte/models/item/{name}.json`
- `src/main/resources/assets/jdte/textures/item/{name}.png`
- `src/main/resources/assets/jdte/lang/en_us.json`
- `src/main/resources/assets/jdte/lang/zh_cn.json`
- `src/main/resources/data/jdte/recipe/{name}.json`
- GuideME 页面或已有页面条目。

新增方块通常需要：

- `src/main/resources/assets/jdte/blockstates/{name}.json`
- `src/main/resources/assets/jdte/models/block/{name}.json`
- `src/main/resources/assets/jdte/models/item/{name}.json`
- `src/main/resources/assets/jdte/textures/block/{name}_*.png`
- `src/main/resources/data/jdte/loot_table/blocks/{name}.json`
- `src/main/resources/data/jdte/recipe/{name}.json`
- `src/main/resources/assets/jdte/lang/en_us.json`
- `src/main/resources/assets/jdte/lang/zh_cn.json`
- GuideME 页面或已有页面条目。

GuideME 入口：

- 配置：`src/main/resources/assets/jdte/guideme_guides/guide.json`
- 页面：`src/main/resources/assets/jdte/guides/jdte/guide/`
- 当前页面：`index.md`、`upgrades.md`、`time-accelerator.md`、`extended-machines.md`、`extended-upgrade.md`

## 常见开发流程

### 添加新升级卡

1. 在 `UpgradeType` 中添加类型、序列化名和上限。
2. 在 `JDTEItems` 中注册 `UpgradeCardItem`。
3. 如果升级有适用机器限制，在 `UpgradeItemStackHandler.isItemValid()` 中添加验证。
4. 在 `UpgradeHelper` 或对应 mixin/方块实体中实现效果。
5. 在 `JDTEItems.upgrades()` 中加入新升级卡，确保创造标签页自动展示。
6. 添加物品模型、贴图、配方、英文/中文翻译和 tooltip。
7. 更新 GuideME、README 和本开发文档。
8. 运行 `./gradlew compileJava` 验证。

### 添加新机器

1. 创建 `Block`、`BlockEntity`、`Container`，客户端需要时创建 `Screen` 和渲染器。
2. 在 `JDTEBlocks`、`JDTEItems`、`JDTEBlockEntities`、`JDTEMenus` 中注册。
3. 在 `JDTECreativeTabs` 中添加到创造标签页。
4. 在 `JDTEClientSetup` 中注册 screen 和 renderer。
5. 在 `JDTE.registerCapabilities()` 中注册能量、流体或其他能力。
6. 添加 blockstate、block model、item model、贴图、语言、战利品表和配方。
7. 如果是扩展机器，实现 `ExtendedUpgradeMachine` 并使用 `ExtendedUpgradeItemStackHandler`。
8. 如果需要由扩展升级转换，更新 `ExtendedUpgradeItem.UPGRADE_MAP`。
9. 运行 `./gradlew compileJava`，必要时运行 `./gradlew runClient` 做游戏内检查。

### 添加新 Mixin

1. 创建 mixin 类并标注 `@Mixin(TargetClass.class)`。
2. 用 `@Inject`、`@ModifyConstant` 或 accessor 实现最小注入。
3. 自定义成员添加 `@Unique` 并使用 `jdte$` 前缀。
4. 在 `mixins.jdte.json` 的 `mixins` 或 `client` 列表中注册。
5. 运行 `./gradlew compileJava` 检查编译和 refmap。
6. 运行客户端或服务器验证 mixin 是否成功 apply。

## 调试与验证

推荐验证顺序：

```bash
./gradlew compileJava
./gradlew jar
./gradlew runClient
```

常见问题：

| 问题 | 常见原因 | 处理方式 |
|------|----------|----------|
| `JDT jar not found` | 本地依赖 jar 缺失 | 检查 `/home/guili/libs/justdirethings-1.5.7.jar` |
| Mixin apply failed | 目标类、方法或字段名变化 | 用 `javap` 或 JDT 源码确认当前映射名 |
| `InvalidAccessor` | accessor 字段名或类型不匹配 | 对照目标类实际字段类型 |
| 打开 GUI 崩溃 | `stillValid()` 或槽位坐标注入错误 | 检查容器方块类型和 `SlotAccessor` 调整逻辑 |
| 能量/流体容量不同步 | 能力或容量 accessor 未更新 | 检查 `UpgradeHelper.syncCapacities()` 和 capability 注册 |
| 方块显示紫黑 | 模型、贴图或 blockstate 缺失 | 检查 assets 路径和资源 ID |
| 物品无合成 | 配方路径或 JSON 格式错误 | 检查 `data/jdte/recipe/{name}.json` |

## 版本历史

### v0.5.0（当前）

- 修复过滤槽 bug：一台机器的配置导致所有机器过滤槽全部消失，不加过滤升级也无法显示过滤槽。
- 修复 12 台机器的 blockstate JSON 缺失旋转变换，扳手旋转现在视觉生效。
- 升级弹窗和过滤弹窗增加原版 `container/slot` 槽位边框渲染。
- GuideME 指南增加英文翻译（14 页），中文页面移至 zh_cn/ 目录。
- 修复 fluid-stabilizer.md 导航位置冲突。

### v0.4.0

- 新增生物粉碎机：高级、扩展版本。
- 生物粉碎机支持抢夺升级和锋利升级。
- 生物粉碎机可放置在刷怪笼上方阻止刷怪并产生掉落物和经验流体。
- 新增 BOSS 精华物品：凋灵精华、末影龙精华、远古守卫者精华。
- 升级卡扩展到 11 种，新增抢夺升级和锋利升级。
- 新增 SpawnerMixin 拦截刷怪笼逻辑。
- 新增可配置系统，支持游戏内 Mods > Config 编辑。
- 新增生命提取器、灌注机。

### v0.3.0

### v0.2.0

- 升级卡扩展到 9 种，包含过滤升级和创造升级。
- 时间加速器扩展到初级、高级、扩展高级 3 种。
- 扩展高级时间加速器支持 8 个升级槽。
- 新增粘胶激活器、凝胶发生器、流体稳定器、物品/流体放置器、物品/流体接收器机器族。
- 升级弹窗和过滤弹窗支持独立显示、拖动和位置持久化。
- 可过滤机器支持根据过滤升级动态增加过滤槽。
- README 和开发文档同步到当前代码结构。

### v0.1.0

- 初始升级系统。
- 初级/高级时间加速器。
- 8 种 JDT T2 机器的扩展版本。
- GuideME 游戏内文档基础页面。

## 配置系统

JDTE 使用 NeoForge 的 `ModConfigSpec` 系统，配置可以通过游戏内 Mods > Config 编辑（COMMON 类型，重启后生效）。

配置类：`src/main/java/com/jdte/setup/JDTEConfig.java`

配置分类：

| 分类 | 路径 | 说明 |
|------|------|------|
| 升级系统 | `jdte.upgrades` | 过滤槽、能耗倍率、tick 速度、区域半径等 |
| 时间加速器 | `jdte.timeAccelerator` | 流体容量、各级倍率、能量容量等 |
| 生物粉碎机 | `jdte.bioCrusher` | 流体容量、能量消耗、伤害、半径等 |
| 发送器/接收器 | `jdte.senderReceiver` | 存储槽位、传输速率、延迟、能量等 |
| 凝胶发生器 | jdte.gelGenerator | 槽位数、容量、转换量、燃料次数等 |
| 发电机升级 | `jdte.generatorUpgrade` | 能量倍率、流体消耗等 |
| 升级物品 | `jdte.upgradeItems` | 最大数量、伤害值等 |

配置翻译键使用 `config.jdte.<category>.<key>` 格式，已在 `en_us.json` 和 `zh_cn.json` 中定义。

## 待办事项

- 完善生物粉碎机的 GuideME 页面。
- 添加 BOSS 精华的合成用途。
- 完善新增自动化机器的 GuideME 页面。
- 补齐新增机器的配方和战利品表检查。
- 为核心升级效果补充游戏内验证用例或测试清单。
- ~~添加可配置项，减少硬编码倍率、容量和消耗。~~ ✓ 已完成
- 继续优化时间加速器范围扫描性能。
