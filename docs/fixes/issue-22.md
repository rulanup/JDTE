# Issue #22：与永恒星光同装时登录世界栈溢出

## 日志与定位

- 用户提供的完整 `latest.log` 显示 JDTE **0.6.5.0**、永恒星光（Eternal Starlight）**0.9.1+1.21.1+neoforge**。
- 日志第 **25109** 行开始记录数据同步事件失败，监听器索引为 **36**，对应 `com.jdte.JDTE`；第 **25165** 行开始的 `StackOverflowError` 重复 `RegistryFileCodec`、`RecursiveCodec`、`RecordCodecBuilder` 的编码调用。
- 这是 [#22](https://github.com/rulanup/JDTE/issues/22) 报告的递归编码，与 [#21](https://github.com/rulanup/JDTE/issues/21) 中 Infinity 放置条件的类型转换异常不同。

检查[官方永恒星光 0.9.1 发布包](https://modrinth.com/mod/eternal-starlight/version/IyTc0Vvj)的字节码和数据资源后，确认以下引用路径：

1. `MineralFeatureAnalyzer` 原先在判断特征是否产出矿物前，就对所有放置条件使用普通 `JsonOps` 编码。
2. 永恒星光的 `eternal_starlight:avoid_structure` 使用 `RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC)` 编码结构集合。
3. 其 `#eternal_starlight:boss_landmarks` 包含 `eternal_starlight:golem_forge`，这是一个引用 `eternal_starlight:golem_forge/boss` 模板池的 Jigsaw 结构。
4. 该模板池的 `fallback` 指向 `minecraft:empty`。Minecraft 在初始化时让空模板池的 `fallback` 引用它自身，这是合法的注册表引用。
5. 普通 `JsonOps` 没有注册表上下文，`RegistryFileCodec` 因而把引用对象内联展开，重复编码空模板池的 `fallback`，直到耗尽线程栈。

官方发布包 SHA-512：

```text
2bd7d2fbc478b3cc2b6426ff9cffddf8800b46aa2ca5a4570f9d9ed2f50d4aeadc83bea0c5c410bf26c2e9e0c6cc3296cb8adff233787897265aa2d7a1ad9a71
```

## 修复方式

- `MineralSurveyIndex` 将当前服务器的 `registryAccess()` 传入分析器，登录时的惰性构建和 `/reload` 重建均使用当前注册表。
- 配置特征与放置条件的三个 Codec 编码入口统一使用 `MineralRegistryOps`，让命名注册表引用编码为 ID 或标签，而不展开其完整对象图。
- 如果某个 Codec 需要的注册表不可用，立即拒绝该条可选元数据的编码，避免退回内联展开。无上下文的旧版分析接口仍支持不依赖注册表的普通矿物配置。
- 每次编码最多经过 **64 次 RegistryFileCodec Holder 查找**，防止不支持的直接 Holder 环或过深内联引用耗尽栈；限制在每次编码开始时重置。配置特征的直接 Holder 解包也检测重复对象并限制深度。
- 先确认矿物目标，再读取放置条件，避免为非矿物植被等特征编码结构过滤器。无法读取的可选条件只跳过该条估算，继续保留矿物、其它数量/高度条件及后续特征；原版与 Mekanism 结构的矿物读取方式保留。
- 未修改永恒星光的世界生成，也未禁用该模组的矿物或整个矿物清单功能。

## 构建与适用范围

正式修复包含在 **[0.6.5.1](../releases/0.6.5.1.md)** 中。客户端和服务端均应升级主模组；使用可选 JDTE-AE 时，也需更新到配套的 **0.6.5.1**。

初次定位时，主模组和 JDTE-AE 编译、打包成功，并提供过保留 0.6.5.0 版本号的开发修复包。该阶段只进行源码检查、编译和打包，未运行自动化测试；尚未在用户的完整 ATM10 存档中验证登录。
