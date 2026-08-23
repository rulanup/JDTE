# 高级机器设置复制器实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `subagent-driven-development` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增一个沿用 JDT 原版交互方式、可复制 JDTE/JDT/Dyna 设置并额外复制 JDTE 六面自动输入输出配置的高级机器设置复制器。

**架构：** 继承 JDT 的 `MachineSettingsCopier`，复用其 GUI、四类设置选择、网络包和基础兼容性。在同一个 `COPIED_MACHINE_DATA` 数据组件中增加 `jdteAutoIoConfig` 子标签，保存 JDTE 自动输入输出的输入/输出位掩码；不序列化机器运行时状态。

**技术栈：** Java 21、NeoForge 21.1、JDT `MachineSettingsCopier` 公共 API、Minecraft `CompoundTag`、JUnit 5、Gradle。

---

## 文件清单

- 创建：`src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java`，负责 JDTE 面配置掩码的编码、解码和裁剪。
- 创建：`src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java`，继承 JDT 复制器并扩展面配置的保存与加载。
- 创建：`src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierDataTest.java`，验证面配置数据契约。
- 修改：`src/main/java/com/jdte/setup/JDTEItems.java`，注册高级复制器。
- 修改：`src/main/java/com/jdte/setup/JDTECreativeTabs.java`，将高级复制器加入 JDTE 创造模式物品栏。
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`，添加中文名称。
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`，添加英文名称。
- 创建：`src/main/resources/assets/jdte/models/item/advanced_machine_settings_copier.json`，复用 JDT 复制器纹理。
- 创建：`src/main/resources/data/jdte/recipe/advanced_machine_settings_copier.json`，添加高级复制器配方。

## 全局约束

- 交互必须保持 JDT 原版复制器语义：Shift 右键复制、普通右键粘贴、右键空气打开原版四项设置界面。
- 复制范围、偏移、过滤器、红石设置继续由父类处理；高级复制器只扩展 JDTE 自动输入输出面配置。
- 面配置保存为绝对方向的 `inputMask` 和 `outputMask`，每个掩码只允许 `AutoIoConfigData.ALL_SIDES_MASK` 的 6 个方向位。
- 面配置只对 `AutoIoConfigHelper.hasConfigurableIo` 支持的 `BaseMachineBE` 生效；不直接引用 Just Dyna Things 的可选类。
- 不复制或恢复机器库存、流体、能量、工作进度、冷却状态及其他运行时数据。
- 复制不支持面配置的机器后，必须清除复制器中的旧 `jdteAutoIoConfig`，防止粘贴旧数据。
- 保留工作区现有未提交改动，不修改与本功能无关的文件。

### 任务 1：面配置数据契约

**文件：**
- 创建：`src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java`
- 测试：`src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierDataTest.java`

- [ ] **步骤 1：编写失败的测试**

测试以下 API 和行为：

```java
CompoundTag copiedData = new CompoundTag();
AdvancedMachineSettingsCopierData.write(copiedData, 0b11_1111, 0b10_1010);

assertEquals(Optional.of(new AdvancedMachineSettingsCopierData.Masks(0b11_1111, 0b10_1010)),
        AdvancedMachineSettingsCopierData.read(copiedData));
assertEquals(Set.of("jdteAutoIoConfig"), copiedData.getAllKeys());
```

另外覆盖：高位掩码被裁剪；缺少根标签、缺少 `inputMask` 或缺少 `outputMask` 时返回 `Optional.empty()`；`remove` 会删除根标签。

- [ ] **步骤 2：运行测试验证失败**

运行：

```text
./gradlew test --tests com.jdte.common.items.AdvancedMachineSettingsCopierDataTest
```

预期：因 `AdvancedMachineSettingsCopierData` 尚未定义而失败。

- [ ] **步骤 3：编写最少实现代码**

实现以下固定契约：

```java
public final class AdvancedMachineSettingsCopierData {
    public static final String ROOT_KEY = "jdteAutoIoConfig";

    public record Masks(int inputMask, int outputMask) {}

    public static void write(CompoundTag copiedData, int inputMask, int outputMask);
    public static Optional<Masks> read(CompoundTag copiedData);
    public static void remove(CompoundTag copiedData);
}
```

`write` 和 `read` 均使用 `AutoIoConfigData.ALL_SIDES_MASK` 裁剪掩码；`read` 只接受同时存在两个整型字段的子标签。

- [ ] **步骤 4：运行测试验证通过**

运行同一条测试命令，预期全部通过。

- [ ] **步骤 5：Commit**

```text
git add src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierDataTest.java
git commit -m "feat(机器设置复制器): 添加面配置数据契约"
```

### 任务 2：高级复制器行为

**文件：**
- 创建：`src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java`

- [ ] **步骤 1：确认任务 1 的测试和提交已完成**

运行：

```text
./gradlew test --tests com.jdte.common.items.AdvancedMachineSettingsCopierDataTest
```

预期：任务 1 测试通过。

- [ ] **步骤 2：实现继承和复制行为**

定义 `AdvancedMachineSettingsCopierItem extends MachineSettingsCopier`，构造器只设置 `stacksTo(1)` 的父类属性。覆盖：

```java
@Override
public void saveSettings(Level level, BlockEntity blockEntity, ItemStack stack);

@Override
public void loadSettings(Level level, BlockEntity blockEntity, ItemStack stack);
```

`saveSettings` 先调用 `super.saveSettings`，再从支持自动输入输出的 `BaseMachineBE` 读取 `AutoIoConfigHelper.getInputMask` 和 `getOutputMask`，写入 JDT `JustDireDataComponents.COPIED_MACHINE_DATA` 的 `jdteAutoIoConfig`。不支持时删除旧子标签；如果整个复制数据为空，删除该数据组件。

`loadSettings` 先调用 `super.loadSettings`，再读取同一数据组件中的面配置；目标为支持自动输入输出的 `BaseMachineBE` 时调用 `AutoIoConfigHelper.setMasks`，否则跳过。不得调用完整方块实体 NBT 的保存或加载方法。

- [ ] **步骤 3：运行编译和相关测试**

运行：

```text
./gradlew test --tests com.jdte.common.items.AdvancedMachineSettingsCopierDataTest
./gradlew compileJava
```

预期：命令成功，且没有新增编译错误。

- [ ] **步骤 4：自审并 Commit**

检查高级物品仍然是 `MachineSettingsCopier` 的子类，JDT 原版网络包的 `instanceof MachineSettingsCopier` 能识别它，且没有新增客户端类在服务器专用路径上的直接初始化。

```text
git add src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java
git commit -m "feat(机器设置复制器): 支持复制 JDTE 面配置"
```

### 任务 3：注册、资源和配方

**文件：**
- 修改：`src/main/java/com/jdte/setup/JDTEItems.java`
- 修改：`src/main/java/com/jdte/setup/JDTECreativeTabs.java`
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`
- 创建：`src/main/resources/assets/jdte/models/item/advanced_machine_settings_copier.json`
- 创建：`src/main/resources/data/jdte/recipe/advanced_machine_settings_copier.json`

- [ ] **步骤 1：注册物品**

在 `JDTEItems` 中注册：

```java
public static final DeferredHolder<Item, AdvancedMachineSettingsCopierItem> ADVANCED_MACHINE_SETTINGS_COPIER =
        ITEMS.register("advanced_machine_settings_copier", AdvancedMachineSettingsCopierItem::new);
```

在 JDTE 创造模式物品栏中紧邻现有工具物品加入该注册项。

- [ ] **步骤 2：添加名称、模型和配方**

添加语言键：

```text
item.jdte.advanced_machine_settings_copier = 高级机器设置复制器
item.jdte.advanced_machine_settings_copier = Advanced Machine Settings Copier
```

物品模型使用 `minecraft:item/generated`，纹理层复用 `justdirethings:item/machine_settings_copier`。

配方使用 `justdirethings:machinesettingscopier` 和 `justdirethings:eclipsealloy_ingot`，形状为：

```text
 e 
ece
 e 
```

结果为 1 个 `jdte:advanced_machine_settings_copier`。

- [ ] **步骤 3：运行资源和文档校验**

运行：

```text
./gradlew validateDocs
./gradlew compileJava
```

预期：资源注册、语言键和 Java 编译全部通过。

- [ ] **步骤 4：Commit**

```text
git add src/main/java/com/jdte/setup/JDTEItems.java src/main/java/com/jdte/setup/JDTECreativeTabs.java src/main/resources/assets/jdte/lang/zh_cn.json src/main/resources/assets/jdte/lang/en_us.json src/main/resources/assets/jdte/models/item/advanced_machine_settings_copier.json src/main/resources/data/jdte/recipe/advanced_machine_settings_copier.json
git commit -m "feat(机器设置复制器): 注册高级复制器物品"
```

### 任务 4：整体验证与最终审查

**文件：**
- 验证：任务 1 至任务 3 的全部变更。

- [ ] **步骤 1：运行完整测试**

```text
./gradlew test
```

预期：全部测试通过。

- [ ] **步骤 2：运行编译和资源校验**

```text
./gradlew compileJava
./gradlew validateDocs
```

预期：退出码为 0。

- [ ] **步骤 3：检查需求覆盖**

逐项确认：新物品注册、创造模式物品栏、中文/英文名称、模型、配方、JDT 原版交互、JDTE 面配置复制、JDT/Dyna 通用设置兼容、运行时数据排除、无关改动未被纳入提交。

- [ ] **步骤 4：查看最终 diff**

```text
git diff --check dbe3e24..HEAD
git status --short
```

预期：功能提交没有空白错误；工作区中只保留原有无关改动和本计划允许的产物。
