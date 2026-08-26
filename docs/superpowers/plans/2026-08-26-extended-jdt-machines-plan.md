# JDT 初级机器扩展版实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为 JDT 的煤炭发电器、燃液发电器、经验存储器和普通能量传输器增加 JDTE 扩展版，保留原版行为与存档语义，并接入现有八槽升级系统。

**架构：** 每个扩展机器拥有独立的 JDTE 方块、方块实体、菜单和屏幕。煤炭/燃液发电器与普通能量传输器继承对应 JDT BE，通过带 JDTE `BlockEntityType` 的构造函数复用原逻辑；经验存储器因为 JDT 构造函数硬编码了原版类型，使用独立 BE 复现原版状态、经验和红石/范围逻辑。四个 BE 都实现 `ExtendedUpgradeMachine`，由现有 `UpgradeHelper` 自动提供八个升级槽。保留现有 `AdvancedEnergyTransmitterBE` 原样，不把 AE2 或玩家装备充能功能带入新的普通扩展传输器。

**技术栈：** Java 21、NeoForge 21.1.215+、Minecraft 1.21.1、JDT 1.5.7+、Gradle、JUnit 5、现有 `BaseMachineBE`/`BaseMachineContainer`/`BaseMachineScreen` 和 JDTE 注册/能力框架。

---

## 文件变更清单

### 新建 Java 文件

- `src/main/java/com/jdte/common/blockentities/ExtendedGeneratorBE.java`：继承 JDT `GeneratorT1BE`，实现 `ExtendedUpgradeMachine`，只替换为 `JDTEBlockEntities.EXTENDED_GENERATOR` 类型。
- `src/main/java/com/jdte/common/blocks/ExtendedGeneratorBlock.java`：扩展煤炭发电器方块、菜单入口和 BE 校验。
- `src/main/java/com/jdte/common/containers/ExtendedGeneratorContainer.java`：复用 JDT 发电器燃料槽/同步数据，使用 JDTE 菜单类型和扩展方块 `stillValid`。
- `src/main/java/com/jdte/client/screens/ExtendedGeneratorScreen.java`：扩展煤炭发电器客户端界面。
- `src/main/java/com/jdte/common/blockentities/ExtendedFluidGeneratorBE.java`：继承 JDT `GeneratorFluidT1BE`，实现 `ExtendedUpgradeMachine`，只替换 BE 类型。
- `src/main/java/com/jdte/common/blocks/ExtendedFluidGeneratorBlock.java`：扩展燃液发电器方块和菜单入口。
- `src/main/java/com/jdte/common/containers/ExtendedFluidGeneratorContainer.java`：复用燃液发电器流体槽/同步数据并校验扩展方块。
- `src/main/java/com/jdte/client/screens/ExtendedFluidGeneratorScreen.java`：扩展燃液发电器客户端界面。
- `src/main/java/com/jdte/common/blockentities/ExtendedExperienceHolderBE.java`：独立实现经验存储、经验球收集、目标经验、所有者限制、粒子开关、红石、范围、过滤和原版 NBT 兼容。
- `src/main/java/com/jdte/common/blocks/ExtendedExperienceHolderBlock.java`：扩展经验存储器方块和菜单入口。
- `src/main/java/com/jdte/common/containers/ExtendedExperienceHolderContainer.java`：复用原版经验存储器控件/数据布局并校验扩展方块。
- `src/main/java/com/jdte/client/screens/ExtendedExperienceHolderScreen.java`：扩展经验存储器客户端界面。
- `src/main/java/com/jdte/common/blockentities/ExtendedEnergyTransmitterBE.java`：直接继承 JDT 普通 `EnergyTransmitterBE`，实现 `ExtendedUpgradeMachine`，不继承或引用 `AdvancedEnergyTransmitterBE` 的专属逻辑。
- `src/main/java/com/jdte/common/blocks/ExtendedEnergyTransmitterBlock.java`：普通能量传输器扩展方块、设置交互和菜单入口。
- `src/main/java/com/jdte/common/containers/ExtendedEnergyTransmitterContainer.java`：复用普通传输器设置控件/同步数据并校验扩展方块。
- `src/main/java/com/jdte/client/screens/ExtendedEnergyTransmitterScreen.java`：普通能量传输器扩展客户端界面。

### 修改 Java 文件

- `src/main/java/com/jdte/setup/JDTEBlocks.java`：注册 `extended_generator`、`extended_fluid_generator`、`extended_experience_holder`、`extended_energy_transmitter`。
- `src/main/java/com/jdte/setup/JDTEItems.java`：为四个扩展方块注册 `BlockItem`。
- `src/main/java/com/jdte/setup/JDTEBlockEntities.java`：为四个扩展方块注册只绑定自身方块的 `BlockEntityType`。
- `src/main/java/com/jdte/setup/JDTEMenus.java`：注册四个扩展菜单类型。
- `src/main/java/com/jdte/setup/JDTEClientSetup.java`：注册四个屏幕；若普通传输器有区域渲染，则为扩展类型注册同一普通区域渲染器，不注册高级传输器专用渲染器。
- `src/main/java/com/jdte/setup/JDTECreativeTabs.java`：在 Extended Machines 区域加入四个 BlockItem。
- `src/main/java/com/jdte/common/items/ExtendedUpgradeItem.java`：加入四个 JDT 普通方块到 JDTE 扩展方块的转换映射，并保持完整 NBT、朝向、服务端消费和失败不消费语义。
- `src/main/java/com/jdte/common/capabilities/MachineCapabilities.java`：把四个扩展方块加入与普通机器一致的能力表；煤炭发电器提供能量/燃料物品输入，燃液发电器提供能量/燃液输入，普通扩展传输器提供普通传输器的能量/物品能力，经验存储器不新增错误的外部能力。

### 新建资源文件

- `src/main/resources/data/jdte/recipe/extended_generator.json`
- `src/main/resources/data/jdte/recipe/extended_fluid_generator.json`
- `src/main/resources/data/jdte/recipe/extended_experience_holder.json`
- `src/main/resources/data/jdte/recipe/extended_energy_transmitter.json`
- `src/main/resources/assets/jdte/blockstates/extended_generator.json`
- `src/main/resources/assets/jdte/blockstates/extended_fluid_generator.json`
- `src/main/resources/assets/jdte/blockstates/extended_experience_holder.json`
- `src/main/resources/assets/jdte/blockstates/extended_energy_transmitter.json`
- `src/main/resources/assets/jdte/models/block/extended_generator.json`
- `src/main/resources/assets/jdte/models/block/extended_fluid_generator.json`
- `src/main/resources/assets/jdte/models/block/extended_experience_holder.json`
- `src/main/resources/assets/jdte/models/block/extended_energy_transmitter.json`
- `src/main/resources/assets/jdte/models/item/extended_generator.json`
- `src/main/resources/assets/jdte/models/item/extended_fluid_generator.json`
- `src/main/resources/assets/jdte/models/item/extended_experience_holder.json`
- `src/main/resources/assets/jdte/models/item/extended_energy_transmitter.json`

### 修改资源和测试文件

- `src/main/resources/assets/jdte/lang/en_us.json`、`src/main/resources/assets/jdte/lang/zh_cn.json`：加入四个方块名称以及需要显示的菜单标题。
- `src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`：验证四个 BE 的类型、八槽标记、普通行为入口及经验 NBT 兼容。
- `src/test/java/com/jdte/common/items/ExtendedUpgradeItemTest.java`：验证四个转换映射、未知方块拒绝、扩展/高级传输器隔离。
- `src/test/java/com/jdte/common/recipes/ExtendedJdtMachineResourceTest.java`：验证四个注册 ID、配方、模型、方块状态和双语文本资源。

现有 `AdvancedEnergyTransmitterBE`、其菜单、屏幕、网络包、Jade 状态和 AE2 集成不修改。现有 `build.gradle` 中为开发客户端设置的低内存参数保留，供最终 `runClient` 验证使用。

### 任务 1：扩展煤炭发电器垂直切片

**文件：**
- 创建：`src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`
- 创建：`src/main/java/com/jdte/common/blockentities/ExtendedGeneratorBE.java`
- 创建：`src/main/java/com/jdte/common/blocks/ExtendedGeneratorBlock.java`
- 创建：`src/main/java/com/jdte/common/containers/ExtendedGeneratorContainer.java`
- 创建：`src/main/java/com/jdte/client/screens/ExtendedGeneratorScreen.java`
- 修改：`src/main/java/com/jdte/setup/JDTEBlocks.java`、`JDTEItems.java`、`JDTEBlockEntities.java`、`JDTEMenus.java`

- [ ] **步骤 1：编写失败的煤炭发电器测试**

在测试中实例化 `ExtendedGeneratorBE`，断言它同时是 `GeneratorT1BE` 和 `ExtendedUpgradeMachine`，`getType()` 等于 `JDTEBlockEntities.EXTENDED_GENERATOR.get()`，并通过 `UpgradeHelper.getUpgradeHandler(be).getSlots()` 断言槽数为 `ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT`。再从 `BuiltInRegistries.BLOCK` 和 `BuiltInRegistries.ITEM` 验证 `jdte:extended_generator` 已有独立注册入口。

```java
@Test
void extendedGeneratorUsesItsOwnTypeAndEightUpgradeSlots() {
    ExtendedGeneratorBE machine = new ExtendedGeneratorBE(
            BlockPos.ZERO, JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState());

    assertInstanceOf(GeneratorT1BE.class, machine);
    assertInstanceOf(ExtendedUpgradeMachine.class, machine);
    assertEquals(JDTEBlockEntities.EXTENDED_GENERATOR.get(), machine.getType());
    assertEquals(ExtendedUpgradeItemStackHandler.EXTENDED_SLOT_COUNT,
            UpgradeHelper.getUpgradeHandler(machine).getSlots());
}
```

- [ ] **步骤 2：运行测试确认当前实现失败**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest`

预期：因 `ExtendedGeneratorBE`、扩展注册对象尚不存在而失败；记录失败点，不修改测试断言来绕过缺失实现。

- [ ] **步骤 3：实现煤炭发电器扩展 BE 和注册**

按 JDT `GeneratorT1BE` 的 typed constructor 形式实现：

```java
public ExtendedGeneratorBE(BlockPos pos, BlockState state) {
    super(JDTEBlockEntities.EXTENDED_GENERATOR.get(), pos, state);
}
```

让扩展方块创建该 BE；在四个 JDTE 注册表中先加入煤炭发电器对应对象。`BlockEntityType.Builder` 只能绑定 `JDTEBlocks.EXTENDED_GENERATOR.get()`，不得绑定 JDT 原方块或父类注册类型。保留 JDT 发电器 mixin 的目标继承关系，使 Generator Upgrade、燃料计时、红石和 FE 能量逻辑继续作用于子类。

- [ ] **步骤 4：实现煤炭发电器菜单、屏幕和槽布局**

以 JDT `GeneratorT1Container`/`GeneratorT1Screen` 的燃料槽、数据同步和绘制坐标为基准，使用 `JDTEMenus.EXTENDED_GENERATOR`，并在 `stillValid` 中只接受 `JDTEBlocks.EXTENDED_GENERATOR`。不要直接复用会硬编码 JDT MenuType 或 JDT 方块的构造函数。确认 `BaseMachineContainer` 的构造阶段仍能为 marker BE 添加八个升级槽。

- [ ] **步骤 5：运行煤炭发电器测试确认通过**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest`

预期：新增煤炭发电器测试通过，且同一测试类中现有机器测试不受影响。

- [ ] **步骤 6：提交煤炭发电器切片**

```bash
git add src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java src/main/java/com/jdte/common/blockentities/ExtendedGeneratorBE.java src/main/java/com/jdte/common/blocks/ExtendedGeneratorBlock.java src/main/java/com/jdte/common/containers/ExtendedGeneratorContainer.java src/main/java/com/jdte/client/screens/ExtendedGeneratorScreen.java src/main/java/com/jdte/setup/JDTEBlocks.java src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTEBlockEntities.java src/main/java/com/jdte/setup/JDTEMenus.java
git commit -m "feat: add extended coal generator"
```

### 任务 2：扩展燃液发电器垂直切片

**文件：**
- 修改：`src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`
- 创建：`src/main/java/com/jdte/common/blockentities/ExtendedFluidGeneratorBE.java`
- 创建：`src/main/java/com/jdte/common/blocks/ExtendedFluidGeneratorBlock.java`
- 创建：`src/main/java/com/jdte/common/containers/ExtendedFluidGeneratorContainer.java`
- 创建：`src/main/java/com/jdte/client/screens/ExtendedFluidGeneratorScreen.java`
- 修改：`src/main/java/com/jdte/setup/JDTEBlocks.java`、`JDTEItems.java`、`JDTEBlockEntities.java`、`JDTEMenus.java`

- [ ] **步骤 1：先加入燃液发电器失败测试**

在同一测试类增加 `ExtendedFluidGeneratorBE` 的 `GeneratorFluidT1BE`、`ExtendedUpgradeMachine`、自有 `BlockEntityType` 和八槽断言，并验证 `jdte:extended_fluid_generator` 的注册对象存在。

```java
@Test
void extendedFluidGeneratorKeepsJdtFluidGeneratorContract() {
    ExtendedFluidGeneratorBE machine = new ExtendedFluidGeneratorBE(
            BlockPos.ZERO, JDTEBlocks.EXTENDED_FLUID_GENERATOR.get().defaultBlockState());
    assertInstanceOf(GeneratorFluidT1BE.class, machine);
    assertInstanceOf(ExtendedUpgradeMachine.class, machine);
    assertEquals(JDTEBlockEntities.EXTENDED_FLUID_GENERATOR.get(), machine.getType());
    assertEquals(8, UpgradeHelper.getUpgradeHandler(machine).getSlots());
}
```

- [ ] **步骤 2：运行针对性测试确认失败**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest.extendedFluidGeneratorKeepsJdtFluidGeneratorContract`

预期：因扩展燃液发电器类和注册对象缺失而失败。

- [ ] **步骤 3：实现 typed BE、方块和注册对象**

调用 JDT `GeneratorFluidT1BE` 的 typed constructor，注册类型只绑定扩展燃液发电器方块。复用现有 `GeneratorFluidUpgradeMixin`、流体处理、燃烧、FE 生成、红石和容量调整逻辑；不复制一套会偏离 JDT 的燃料规则。

- [ ] **步骤 4：实现燃液菜单和屏幕**

复制 JDT `GeneratorFluidT1Container` 的燃液槽与同步数据，改用 JDTE MenuType 和扩展方块校验；复制 `GeneratorFluidT1Screen` 的界面布局，构造函数接收 `ExtendedFluidGeneratorContainer`。确认流体能力和物品/升级槽的方向与普通燃液发电器一致。

- [ ] **步骤 5：运行测试确认通过并提交**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest`

预期：煤炭与燃液两个扩展 BE 测试均通过。

```bash
git add src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java src/main/java/com/jdte/common/blockentities/ExtendedFluidGeneratorBE.java src/main/java/com/jdte/common/blocks/ExtendedFluidGeneratorBlock.java src/main/java/com/jdte/common/containers/ExtendedFluidGeneratorContainer.java src/main/java/com/jdte/client/screens/ExtendedFluidGeneratorScreen.java src/main/java/com/jdte/setup/JDTEBlocks.java src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTEBlockEntities.java src/main/java/com/jdte/setup/JDTEMenus.java
git commit -m "feat: add extended fluid generator"
```

### 任务 3：扩展经验存储器垂直切片

**文件：**
- 修改：`src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`
- 创建：`src/main/java/com/jdte/common/blockentities/ExtendedExperienceHolderBE.java`
- 创建：`src/main/java/com/jdte/common/blocks/ExtendedExperienceHolderBlock.java`
- 创建：`src/main/java/com/jdte/common/containers/ExtendedExperienceHolderContainer.java`
- 创建：`src/main/java/com/jdte/client/screens/ExtendedExperienceHolderScreen.java`
- 修改：`src/main/java/com/jdte/setup/JDTEBlocks.java`、`JDTEItems.java`、`JDTEBlockEntities.java`、`JDTEMenus.java`

- [ ] **步骤 1：编写经验状态/NBT 失败测试**

测试用 JDT `ExperienceHolderBE` 的保存/加载实现和反编译结果确认键名，然后为 `ExtendedExperienceHolderBE` 写等价断言：`exp`、`targetExp`、`collectExp`、`ownerOnly`、`showParticles`、过滤数据、范围数据和红石设置在保存后可加载；经验存取公开入口的结果与普通 BE 一致。测试同时断言扩展 BE 的类型是 `JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER`，而不是 JDT `Registration.ExperienceHolderBE`。

```java
@Test
void extendedExperienceHolderRoundTripsJdtStateKeys() {
    CompoundTag saved = new CompoundTag();
    ExtendedExperienceHolderBE machine = new ExtendedExperienceHolderBE(
            BlockPos.ZERO, JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState());
    machine.loadAdditional(jdtExperienceState(), null);
    machine.saveAdditional(saved);

    assertEquals(12_345, saved.getInt("exp"));
    assertEquals(20_000, saved.getInt("targetExp"));
    assertEquals(jdtExperienceState().getBoolean("collectExp"), saved.getBoolean("collectExp"));
    assertEquals(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(), machine.getType());
}
```

`jdtExperienceState()` 必须包含从 JDT 实现确认的原键名和有效范围/过滤数据，不使用 JDTE 自造的新键名作为替代。

- [ ] **步骤 2：运行测试确认失败**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest.extendedExperienceHolderRoundTripsJdtStateKeys`

预期：因扩展经验存储器不存在或尚未实现 JDT NBT 键而失败。

- [ ] **步骤 3：实现独立经验存储 BE**

参考 JDT `ExperienceHolderBE` 的 tick、经验存入/取出、经验球收集、目标经验同步、所有者限制、粒子开关、红石控制、范围和过滤逻辑，复制必要实现并将字段/方法接到 JDTE 自己的 BE 类型。不要继承 JDT `ExperienceHolderBE`，因为其构造函数内部固定使用 `Registration.ExperienceHolderBE`。保存和加载时逐一保留原版键名与数值语义，只有成功的实际状态变化才标记 dirty/同步。

- [ ] **步骤 4：实现经验存储器方块、菜单、屏幕和注册**

方块交互沿用普通经验存储器；菜单复制原版控件和数据布局但使用 `JDTEMenus.EXTENDED_EXPERIENCE_HOLDER`，`stillValid` 只接受扩展方块。注册的 `BlockEntityType` 只绑定扩展方块；经验存储器不加入伪造的能量或流体 capability。

- [ ] **步骤 5：运行状态测试并提交**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest`

预期：三个扩展 BE 测试通过，经验状态在保存/加载测试中保持兼容。

```bash
git add src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java src/main/java/com/jdte/common/blockentities/ExtendedExperienceHolderBE.java src/main/java/com/jdte/common/blocks/ExtendedExperienceHolderBlock.java src/main/java/com/jdte/common/containers/ExtendedExperienceHolderContainer.java src/main/java/com/jdte/client/screens/ExtendedExperienceHolderScreen.java src/main/java/com/jdte/setup/JDTEBlocks.java src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTEBlockEntities.java src/main/java/com/jdte/setup/JDTEMenus.java
git commit -m "feat: add extended experience holder"
```

### 任务 4：扩展普通能量传输器垂直切片

**文件：**
- 修改：`src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java`
- 创建：`src/main/java/com/jdte/common/blockentities/ExtendedEnergyTransmitterBE.java`
- 创建：`src/main/java/com/jdte/common/blocks/ExtendedEnergyTransmitterBlock.java`
- 创建：`src/main/java/com/jdte/common/containers/ExtendedEnergyTransmitterContainer.java`
- 创建：`src/main/java/com/jdte/client/screens/ExtendedEnergyTransmitterScreen.java`
- 修改：`src/main/java/com/jdte/setup/JDTEBlocks.java`、`JDTEItems.java`、`JDTEBlockEntities.java`、`JDTEMenus.java`

- [ ] **步骤 1：编写普通/高级隔离的失败测试**

测试必须锁定继承关系和注册类型：扩展 BE 的直接父类是 JDT `EnergyTransmitterBE`，不是 `AdvancedEnergyTransmitterBE`；扩展 BE 不是高级 BE 的实例；它拥有八个升级槽；现有高级方块仍创建 `AdvancedEnergyTransmitterBE`。

```java
@Test
void extendedTransmitterIsTheOrdinaryJdtTransmitterOnly() {
    ExtendedEnergyTransmitterBE machine = new ExtendedEnergyTransmitterBE(
            BlockPos.ZERO, JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get().defaultBlockState());
    assertEquals(EnergyTransmitterBE.class, ExtendedEnergyTransmitterBE.class.getSuperclass());
    assertFalse(machine instanceof AdvancedEnergyTransmitterBE);
    assertEquals(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), machine.getType());
    assertEquals(8, UpgradeHelper.getUpgradeHandler(machine).getSlots());
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest.extendedTransmitterIsTheOrdinaryJdtTransmitterOnly`

预期：因扩展传输器类、注册对象和菜单不存在而失败。

- [ ] **步骤 3：实现 typed BE、方块和注册**

使用 JDT `EnergyTransmitterBE(BlockEntityType<?>, BlockPos, BlockState)` 构造函数，并只传入 `JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get()`。复用普通传输器的目标发现、能量平衡、过滤、范围、面向、红石和粒子行为；不要调用 `AdvancedEnergyTransmitterBE`、AE2 能量源、玩家充能器、其网络包或高级配置。

- [ ] **步骤 4：实现普通传输器菜单和屏幕**

复制 JDT `EnergyTransmitterContainer`/`EnergyTransmitterScreen` 的设置控件、数据同步和普通传输器布局，改用 JDTE MenuType、扩展方块的 `stillValid` 和扩展 BE 的字段。保留六面普通配置和过滤数据的读写路径，不将高级传输器独有的设置控件带入界面。

- [ ] **步骤 5：运行测试并提交**

运行：`./gradlew test --tests com.jdte.common.blockentities.ExtendedJdtMachineBehaviorTest`

预期：四个扩展 BE 的类型/继承/八槽/NBT 测试通过，已有高级传输器测试仍通过。

```bash
git add src/test/java/com/jdte/common/blockentities/ExtendedJdtMachineBehaviorTest.java src/main/java/com/jdte/common/blockentities/ExtendedEnergyTransmitterBE.java src/main/java/com/jdte/common/blocks/ExtendedEnergyTransmitterBlock.java src/main/java/com/jdte/common/containers/ExtendedEnergyTransmitterContainer.java src/main/java/com/jdte/client/screens/ExtendedEnergyTransmitterScreen.java src/main/java/com/jdte/setup/JDTEBlocks.java src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTEBlockEntities.java src/main/java/com/jdte/setup/JDTEMenus.java
git commit -m "feat: add extended energy transmitter"
```

### 任务 5：转换映射与完整状态保留

**文件：**
- 修改：`src/test/java/com/jdte/common/items/ExtendedUpgradeItemTest.java`
- 修改：`src/main/java/com/jdte/common/items/ExtendedUpgradeItem.java`

- [ ] **步骤 1：编写转换映射失败测试**

通过包可见的 `ExtendedUpgradeItem.targetFor(Block source)` 断言四个 JDT 源方块精确映射到四个 JDTE 扩展方块，并断言 JDT 点击器、放置器和 JDTE 高级传输器返回空结果。测试还要断言扩展目标的 `BlockEntityType` 均绑定自身方块。

```java
@Test
void mapsOnlyTheFourOrdinaryJdtSources() {
    assertEquals(JDTEBlocks.EXTENDED_GENERATOR.get(),
            ExtendedUpgradeItem.targetFor(Registration.GeneratorT1.get()));
    assertEquals(JDTEBlocks.EXTENDED_FLUID_GENERATOR.get(),
            ExtendedUpgradeItem.targetFor(Registration.GeneratorFluidT1.get()));
    assertEquals(JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get(),
            ExtendedUpgradeItem.targetFor(Registration.ExperienceHolder.get()));
    assertEquals(JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get(),
            ExtendedUpgradeItem.targetFor(Registration.EnergyTransmitter.get()));
    assertNull(ExtendedUpgradeItem.targetFor(Registration.ClickerT1.get()));
    assertNull(ExtendedUpgradeItem.targetFor(JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get()));
}
```

- [ ] **步骤 2：运行映射测试确认失败**

运行：`./gradlew test --tests com.jdte.common.items.ExtendedUpgradeItemTest`

预期：四个新映射尚未存在，测试失败。

- [ ] **步骤 3：加入映射并复用安全转换流程**

将映射加入现有 `UPGRADE_MAP`，使用 JDT `Registration` 的普通源方块和 JDTE 四个目标方块。保持现有流程：客户端只返回成功交互结果；服务端先检查旧 BE、新 BE 类型和替换位置，再用 `saveWithFullMetadata` 保存旧 BE，保留 `BlockStateProperties.FACING`，无掉落替换，`loadCustomOnly` 恢复完整 NBT，全部成功后才 shrink 一个扩展升级物品并播放音效。任何源方块不匹配、旧/新 BE 缺失或恢复失败都必须返回不消费且保持原方块/数据的结果。

- [ ] **步骤 4：编写并运行状态保留测试**

为煤炭/燃液/传输器的可复制字段建立最小服务器交互测试，覆盖燃烧计时、能量/流体槽、红石/范围/过滤、传输器六面设置；经验存储器使用任务 3 的 NBT round-trip 断言。测试必须确认转换目标是扩展 BE 而不是仍挂载 JDT BE。

运行：`./gradlew test --tests com.jdte.common.items.ExtendedUpgradeItemTest`

预期：映射、未知方块拒绝、状态保留和失败不消费测试全部通过。

- [ ] **步骤 5：提交转换功能**

```bash
git add src/test/java/com/jdte/common/items/ExtendedUpgradeItemTest.java src/main/java/com/jdte/common/items/ExtendedUpgradeItem.java
git commit -m "feat: upgrade ordinary JDT machines to extended variants"
```

### 任务 6：能力、客户端注册、创造标签与资源

**文件：**
- 修改：`src/main/java/com/jdte/common/capabilities/MachineCapabilities.java`
- 修改：`src/main/java/com/jdte/client/JDTEClientSetup.java`
- 修改：`src/main/java/com/jdte/setup/JDTECreativeTabs.java`
- 创建：四个 `src/main/resources/data/jdte/recipe/*.json`
- 创建：四个 `src/main/resources/assets/jdte/blockstates/*.json`
- 创建：八个 `src/main/resources/assets/jdte/models/{block,item}/*.json`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`、`src/main/resources/assets/jdte/lang/zh_cn.json`
- 创建：`src/test/java/com/jdte/common/recipes/ExtendedJdtMachineResourceTest.java`

- [ ] **步骤 1：先编写资源/能力失败测试**

测试从 classpath 读取四个配方、方块状态、方块模型、物品模型和中英文语言 JSON，断言资源存在且 ID 一致；同时断言四个扩展方块出现在注册表。配方使用现有扩展机器的 shaped 模式，输入是对应 JDT 普通方块加一个 `jdte:extended_upgrade`，结果是对应 JDTE 扩展方块；不承诺通过配方保留机器运行 NBT。

```java
@Test
void allExtendedJdtMachineResourcesExist() {
    for (String id : List.of("generator", "fluid_generator", "experience_holder", "energy_transmitter")) {
        assertResource("data/jdte/recipe/extended_" + id + ".json");
        assertResource("assets/jdte/blockstates/extended_" + id + ".json");
        assertResource("assets/jdte/models/block/extended_" + id + ".json");
        assertResource("assets/jdte/models/item/extended_" + id + ".json");
    }
}
```

- [ ] **步骤 2：运行资源测试确认失败**

运行：`./gradlew test --tests com.jdte.common.recipes.ExtendedJdtMachineResourceTest`

预期：四组资源尚不存在，测试失败。

- [ ] **步骤 3：注册普通能力和屏幕**

在 `MachineCapabilities.MACHINES` 中加入四个条目，并检查 provider 通过实际 BE 类型取 handler；煤炭发电器物品槽使用与 JDT 普通发电器相同的燃料验证，燃液发电器流体 provider 使用与 JDT 普通燃液发电器相同的 tank。为四个菜单注册对应屏幕，避免把 `AdvancedEnergyTransmitterScreen` 或 `AdvancedEnergyTransmitterBER` 用于普通扩展传输器。

- [ ] **步骤 4：加入创造标签和资源**

在 Extended Machines 分组加入四个 BlockItem。方块状态沿用普通机器的 `FACING` 属性，模型入口独立使用 `jdte` ID，模型 parent/纹理优先引用 JDT 原机器资源，不新造纹理。四个配方分别使用普通 JDT 方块和 `jdte:extended_upgrade`；语言文件加入清晰的中英文名称，例如“扩展煤炭发电器 / Extended Coal Generator”。

- [ ] **步骤 5：运行资源和编译测试确认通过**

运行：`./gradlew test --tests com.jdte.common.recipes.ExtendedJdtMachineResourceTest`

预期：四组资源、注册、能力和双语文本断言通过。

- [ ] **步骤 6：提交整合资源**

```bash
git add src/main/java/com/jdte/common/capabilities/MachineCapabilities.java src/main/java/com/jdte/client/JDTEClientSetup.java src/main/java/com/jdte/setup/JDTECreativeTabs.java src/main/resources/data/jdte/recipe src/main/resources/assets/jdte/blockstates src/main/resources/assets/jdte/models src/main/resources/assets/jdte/lang/en_us.json src/main/resources/assets/jdte/lang/zh_cn.json src/test/java/com/jdte/common/recipes/ExtendedJdtMachineResourceTest.java
git commit -m "feat: register extended JDT machine resources"
```

### 任务 7：全量验证、启动验证和回归检查

**文件：** 不预期新增文件；如测试发现实现错误，只修改任务 1–6 已列出的对应文件。不得修改现有高级能量传输器专属实现来“复用”功能。

- [ ] **步骤 1：运行编译验证**

运行：`./gradlew compileJava`

预期：四个 BE 的泛型构造函数、注册引用、客户端屏幕构造函数和 capability provider 全部编译通过。

- [ ] **步骤 2：运行完整单元测试**

运行：`./gradlew test`

预期：所有既有测试和新增的 `ExtendedJdtMachineBehaviorTest`、`ExtendedUpgradeItemTest`、`ExtendedJdtMachineResourceTest` 通过，无新增失败测试。

- [ ] **步骤 3：运行打包验证**

运行：`./gradlew jar`

预期：生成 JDTE jar，四个方块/物品/菜单/BE 注册和资源进入产物，且没有资源重复键或缺失模型警告导致构建失败。

- [ ] **步骤 4：运行低内存开发客户端验证**

运行：`./gradlew runClient`

使用当前 `build.gradle` 中 `-Xms512m`/`-Xmx2G` 的开发客户端参数；在客户端进入标题界面后退出。检查 `runs/client/logs/latest.log`，确认没有 `hs_err_pid*.log`、Mixin 注入失败、菜单屏幕注册失败、BlockEntityType 不匹配或 missing model 错误。

- [ ] **步骤 5：执行隔离性静态检查**

运行：`rg -n "AdvancedEnergyTransmitterBE|AdvancedEnergyTransmitter|AE2|PlayerCharger" src/main/java/com/jdte/common/blockentities/ExtendedEnergyTransmitterBE.java src/main/java/com/jdte/common/blocks/ExtendedEnergyTransmitterBlock.java src/main/java/com/jdte/common/containers/ExtendedEnergyTransmitterContainer.java src/main/java/com/jdte/client/screens/ExtendedEnergyTransmitterScreen.java`

预期：扩展普通传输器实现不出现高级传输器专属类、AE2 或玩家充能依赖；`AdvancedEnergyTransmitterBE` 本身的源代码和注册对象保持未修改。

- [ ] **步骤 6：提交最终验证修正**

若前述命令暴露真实实现问题，先补充对应回归测试，再修复并重新运行失败命令、`./gradlew test` 和 `./gradlew compileJava`；验证通过后提交：

```bash
git add src/main/java src/main/resources src/test/java
git commit -m "test: verify extended JDT machine family"
```
