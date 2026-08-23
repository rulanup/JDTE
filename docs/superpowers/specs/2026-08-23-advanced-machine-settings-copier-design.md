# 高级机器设置复制器设计规格

## 目标

新增 `jdte:advanced_machine_settings_copier`，沿用 JDT 原版机器设置复制器的操作方式，支持复制和粘贴 JDTE、JDT、Just Dyna Things 机器的设置，并额外复制 JDTE 自动输入输出的六个绝对方向面配置。

复制内容仅限配置数据，不包含机器库存、流体、能量、工作进度、冷却状态或其他运行时状态。

## 用户交互

- 普通右键点击机器：将复制器中的设置粘贴到目标机器。
- Shift 右键点击机器：读取目标机器设置并写入复制器。
- 右键点击空气：打开 JDT 原版设置选择界面。
- 原版界面中的范围、偏移、过滤器和红石四类开关保持原样。
- JDTE 自动输入输出面配置始终随高级复制器复制，不额外增加界面开关。

目标机器不支持某类设置时，忽略该类设置；不因粘贴操作修改库存或运行时数据。

## 兼容范围

高级复制器继承 JDT 的 `MachineSettingsCopier`，因此继续使用 JDT 的 `BaseMachineBE`、`AreaAffectingBE`、`FilterableBE` 和 `RedstoneControlledBE` 公共接口。

- JDTE 机器沿用现有的 JDT 基类接口，并额外支持 JDTE 自动输入输出配置。
- JDT 机器保持原版复制器行为。
- Just Dyna Things 机器只依赖其已经实现的 JDT 公共接口，不直接引用 Dyna 的可选类，未安装 Dyna 时不会影响加载。
- 自动输入输出配置仅对 `AutoIoConfigHelper.hasConfigurableIo` 判定支持的机器生效。

## 数据流与存储

### 复制

1. 高级复制器调用父类 `saveSettings`，保存原版四类设置。
2. 如果源机器支持 JDTE 自动输入输出，则读取 `AutoIoConfigHelper` 提供的输入掩码和输出掩码。
3. 将两个六位掩码写入 JDT `COPIED_MACHINE_DATA` 数据组件中的 `jdteAutoIoConfig` 子标签：
   - `inputMask`：各绝对方向的输入开关。
   - `outputMask`：各绝对方向的输出开关。
4. 如果源机器不支持自动输入输出，则删除复制器中已有的 `jdteAutoIoConfig`，避免旧数据污染下一次复制。

### 粘贴

1. 高级复制器调用父类 `loadSettings`，按 JDT 原版逻辑恢复四类设置。
2. 目标机器支持自动输入输出且复制数据存在 `jdteAutoIoConfig` 时，读取两个掩码。
3. 通过 `AutoIoConfigHelper.setMasks` 写入目标机器；该方法负责按目标机器实际支持的输入输出能力裁剪掩码、重置传输状态并同步客户端。
4. 不读取或写回机器完整方块实体 NBT，因此不会覆盖库存、流体、能量和进度。

掩码只保留 `AutoIoConfigData.ALL_SIDES_MASK` 范围内的 6 个方向位。输入和输出同时开启时仍保持「输入 + 输出」模式。

## 代码结构

- 创建 `src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java`：继承 JDT 复制器，复用父类交互和设置逻辑，扩展自动输入输出数据的保存与加载。
- 创建 `src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java`：只负责掩码编码/解码，避免测试依赖完整客户端界面。
- 修改 `src/main/java/com/jdte/setup/JDTEItems.java`：注册新物品。
- 修改 `src/main/java/com/jdte/setup/JDTECreativeTabs.java`：将新物品加入 JDTE 创造模式物品栏。
- 修改 `src/main/resources/assets/jdte/lang/zh_cn.json` 和 `en_us.json`：添加物品名称及必要提示文本。
- 创建 `src/main/resources/assets/jdte/models/item/advanced_machine_settings_copier.json`：复用 JDT 机器设置复制器的物品纹理。
- 创建 `src/main/resources/data/jdte/recipe/advanced_machine_settings_copier.json`：使用原版机器设置复制器和蚀空合金锭制作高级版本。

不新增网络包、不修改 JDT 原版客户端界面、不复制任何机器运行数据。

## 错误处理

- 复制器没有复制数据时，粘贴操作保持 JDT 原版的无操作行为。
- 复制数据缺少或不完整的 JDTE 面子标签时，跳过面配置恢复，不影响四类原版设置。
- 目标机器不支持自动输入输出时，跳过面配置恢复。
- 所有掩码写入均经过六位范围裁剪，不能通过物品 NBT 打开不存在的方向。

## 测试与验收

自动化测试至少覆盖：

1. 六个绝对方向的输入、输出及输入输出同时开启状态可以编码并解码保持一致。
2. 掩码之外的高位会被裁剪，缺少面配置标签时返回无配置。
3. 面配置数据只包含两个掩码，不包含库存、流体、能量或进度字段。
4. 原版设置选择开关仍由 JDT 复制器提供，且高级物品可被 JDT 网络包识别为复制器。

构建验收运行：

```text
./gradlew test
./gradlew compileJava
./gradlew validateDocs
```

最终手动验收使用 JDTE、JDT 和 Just Dyna Things 机器分别测试 Shift 右键复制、普通右键粘贴、右键空气打开菜单，以及 JDTE 每个面的四种模式恢复结果。
