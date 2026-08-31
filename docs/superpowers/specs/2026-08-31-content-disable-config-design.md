# 可配置内容禁用设计

日期：2026-08-31

## 目标

让整合包作者在不依赖 KubeJS 的情况下关闭 JDTE 内容，同时保持注册表 ID 和已有世界可加载：

- Greenhouse（含 Large Greenhouse）、Loot Fabricator、Bio Factory 各自提供动态配方生成开关。
- `jdte.content.disabledBlocks` 可按方块 ID 禁用任意 JDTE 方块。
- `jdte.content.disabledRecipes` 可按配方 ID 或目录前缀禁用任意 JDTE 配方。
- `timeAcceleratorEnabled` 与 `timeFreezerEnabled` 为两个常用家族提供直接总开关。
- 所有开关默认开启/列表默认为空，现有整合包行为不变。

## 禁用语义

方块仍注册，避免已有世界出现 missing registry entry。被禁用的方块：

- 不出现在 JDTE 创造栏或搜索结果中；
- 不能新放置或打开；
- 已存在方块保留且可破坏，但不执行服务器机器逻辑，也不暴露自动化能力；
- 同 ID 的合成配方自动被过滤。

配方禁用在服务器配方加载阶段执行。列表项既匹配精确 ID，也匹配其目录后代，例如 `jdte:greenhouse` 同时匹配 `jdte:greenhouse/wheat`。三个动态配方开关还必须阻止对应 JEI 枚举、类别催化剂、点击区域和客户端动态刷新。

## 组件

- `ContentConfig` 声明通用禁用列表与 Time Accelerator/Time Freezer 家族总开关，并聚合三个机器配置中的动态配方开关。
- `ContentSelection` 是不依赖游戏生命周期的纯 ID 归一化/匹配值对象，便于单元测试。
- `JDTEContentControl` 读取配置并提供方块、物品和配方判定；缓存以配置列表快照为键，配置改变后自动重建。
- `RecipeManagerMixin` 在配方 JSON 解码前删除被禁用的 ID，使服务端与其后同步到客户端的配方集合保持一致。
- `JDTERecipeFilter` 承载可独立测试的 Map 过滤逻辑，Mixin 只负责接入加载边界。
- NeoForge 放置/交互事件、基础机器 ticker 与能力 provider 共同落实方块禁用语义。

## 兼容性与失败行为

- 无效 ID 在配置校验阶段拒绝；省略 namespace 时按 `jdte` 处理。
- 禁用配置不删除世界方块、不清空库存、不注销 block/item/menu/block entity 类型。
- 关闭“动态配方生成”不会单独停机；整合包仍可通过数据包/KubeJS 提供自定义机器配方。只有 `disabledBlocks` 才禁用机器本身。
- Greenhouse/Bio Factory 的数据包配方仍可被 `disabledRecipes` 精确控制；不在 serializer 中制造逐配方解析错误。

## 测试与验收

- 单元测试覆盖简写 ID、完整 ID、目录前缀、方块同名配方和无效输入。
- 配置测试覆盖所有默认值保持兼容。
- 资源/JEI 策略测试确保三个动态生成器有独立门控，且配置关闭不会调用昂贵枚举。
- 运行定向 JUnit、完整 `test`、`compileJava`、`validateDocs`。
