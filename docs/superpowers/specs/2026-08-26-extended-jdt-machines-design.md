# JDT 初级机器扩展版设计规格

## 目标

为 JDT 的四个普通机器提供 JDTE 扩展版：煤炭发电器、燃液发电器、经验存储器和能量传输器。扩展版使用 JDTE 的八槽升级系统，同时保持对应普通机器的操作和存档语义。

## 范围

### 新增内容

新增以下独立注册对象：

| 功能 | 方块 ID | 机器类型 |
| --- | --- | --- |
| 扩展煤炭发电器 | `jdte:extended_generator` | `ExtendedGeneratorBE` |
| 扩展燃液发电器 | `jdte:extended_fluid_generator` | `ExtendedFluidGeneratorBE` |
| 扩展经验存储器 | `jdte:extended_experience_holder` | `ExtendedExperienceHolderBE` |
| 扩展能量传输器 | `jdte:extended_energy_transmitter` | `ExtendedEnergyTransmitterBE` |

每个方块都有对应的 BlockItem、BlockEntityType、MenuType、客户端屏幕、创造标签入口、配方、方块状态、模型和中英文语言条目。

### 明确不做

- 不新增这四类机器的高级版。
- 不删除或重命名现有 `jdte:advanced_energy_transmitter`。
- 不把高级能量传输器的 AE2 网络抽取、玩家装备充能或其他 JDTE 专属行为复制到扩展能量传输器。
- 不改变 JDT 普通机器的注册对象、基础配方、容量、耗能、燃烧速度或传输损耗。
- 不新增专用升级卡；继续使用现有 `jdte:extended_upgrade` 和标准升级卡。

## 行为设计

### 共同规则

四个扩展 BE 都实现 `ExtendedUpgradeMachine`，由 `UpgradeHelper` 自动选择八槽升级附件。标准升级的兼容性继续由机器实际实现的 JDT 接口决定，已有的容量、范围、过滤、发电和红石规则不另起一套。

用 `jdte:extended_upgrade` 对应普通机器右键时：

1. 仅服务端执行转换，客户端只返回成功交互结果。
2. 保存旧 BE 的完整元数据 NBT。
3. 保留方块朝向和普通机器的全部运行状态、设置、过滤数据、燃烧/流体/经验数据。
4. 无掉落地替换成扩展方块并恢复 NBT。
5. 成功后消耗一个扩展升级物品并播放现有升级音效。

升级映射为：

- JDT `Registration.GeneratorT1` → `JDTEBlocks.EXTENDED_GENERATOR`
- JDT `Registration.GeneratorFluidT1` → `JDTEBlocks.EXTENDED_FLUID_GENERATOR`
- JDT `Registration.ExperienceHolder` → `JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER`
- JDT `Registration.EnergyTransmitter` → `JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER`

每个扩展机器也提供“普通机器 + 扩展升级”的合成配方，用于获得可直接放置的扩展方块；配方不承诺保留输入方块的运行 NBT。

### 扩展煤炭发电器

`ExtendedGeneratorBE` 复用 JDT `GeneratorT1BE` 的燃料槽、燃烧计时、FE 生成、红石控制和能量输出逻辑，只通过带有 JDTE 注册类型的构造函数创建。现有 `GeneratorT1BE` 目标 mixin 应自然作用于子类，使发电升级、燃料消耗和能量容量调整规则保持一致。

扩展菜单复用 JDT 发电器的燃料槽布局和数据同步，但使用 JDTE 自己的 MenuType 和 `stillValid` 方块校验。

### 扩展燃液发电器

`ExtendedFluidGeneratorBE` 复用 JDT `GeneratorFluidT1BE` 的燃料流体槽、燃烧逻辑、FE 生成、红石控制和能量输出逻辑。现有燃液发电器升级 mixin、流体处理和能量容量调整规则继续作用于该子类。

扩展菜单复用普通燃液发电器的槽位和数据同步，但使用 JDTE 自己的 MenuType 和 `stillValid` 方块校验。

### 扩展经验存储器

`ExtendedExperienceHolderBE` 使用 JDTE 自己的 BE 类型，并复现 JDT 经验存储器的必要行为：经验存入/取出、经验球收集、目标经验同步、所有者限制、粒子开关、红石控制、范围设置和 NBT 保存/加载。JDT 原 BE 构造函数硬编码了 JDT 的 BlockEntityType，因此不直接继承它来避免扩展方块挂载错误的 BE 类型。

扩展 BE 的 NBT 键与 JDT 原实现保持兼容，以便 `ExtendedUpgradeItem` 转换时能恢复已有机器状态。菜单沿用原版经验存储器的交互控件和数据布局，使用 JDTE 自己的 MenuType。

### 扩展能量传输器

`ExtendedEnergyTransmitterBE` 严格复用 JDT 普通 `EnergyTransmitterBE` 的能量平衡、目标发现、过滤、范围、面向、红石和粒子行为，只替换 BE 类型并实现八槽升级接口。它不继承 `AdvancedEnergyTransmitterBE`，不接入高级能量传输器的 AE2 或玩家装备充能逻辑。

扩展能量传输器的菜单和客户端屏幕沿用 JDT 普通能量传输器的布局与设置操作，但注册为 JDTE 自己的 MenuType。现有高级能量传输器及其网络包、Jade 状态和专用渲染器保持独立。

## 注册与资源

实现需要更新以下注册入口：

- `JDTEBlocks`：四个扩展方块。
- `JDTEItems`：四个扩展 BlockItem。
- `JDTEBlockEntities`：四个扩展 BlockEntityType。
- `JDTEMenus`：四个扩展 MenuType。
- `JDTEClientSetup`：四个屏幕注册。
- `JDTECreativeTabs`：四个扩展方块入口。
- `MachineCapabilities`：按普通机器的实际能力注册扩展方块；发电器提供能量和燃料物品输入，燃液发电器额外提供流体输入，经验存储器不凭空增加新的外部能力，能量传输器提供普通传输器的能量/物品能力。
- `ExtendedUpgradeItem`：加入四个普通到扩展的转换映射。

资源使用 JDTE 的扩展 ID，方块状态保留普通机器的朝向属性。模型优先复用 JDT 的原有纹理和朝向模型，避免引入未要求的视觉变更；扩展版仍必须拥有独立的 JDTE 模型入口，确保资源存在且可被注册物品渲染。

## 错误处理和兼容性

- 不是四个映射源方块时，扩展升级物品继续返回 `PASS`，不消费物品。
- 转换找不到旧或新 BE 时不得消费升级物品；应保持方块和旧数据不变。
- 扩展机器的 `stillValid` 必须校验扩展方块，不能继续接受 JDT 普通方块。
- 所有扩展 BlockEntityType 必须只绑定自己的扩展方块，构造函数不得传入父类或 JDT 的注册类型。
- 普通机器的旧存档不做全局迁移；只有玩家实际执行扩展转换时才变更方块类型。

## 测试策略

先写失败测试，再实现：

1. 注册/转换测试：四个普通方块都映射到正确扩展方块；未知方块不转换且不消耗物品。
2. 升级槽测试：四个扩展 BE 都通过 `UpgradeHelper` 取得八槽 handler，普通版行为不受影响。
3. 兼容性测试：发电器和燃液发电器仍能识别原燃料/流体；经验存储器的经验与设置 NBT 键保持兼容；扩展传输器不调用高级传输器专属功能。
4. `compileJava`：确认所有注册、泛型、客户端屏幕和 mixin 目标可编译。
5. `test`：运行全部单元测试和新增回归测试。
6. `runClient`：使用已设置的低内存开发客户端参数验证 Mod 能进入标题界面，避免再次因开发包原生内存峰值在资源加载阶段崩溃。
