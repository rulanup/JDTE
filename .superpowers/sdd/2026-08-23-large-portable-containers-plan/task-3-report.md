# Task 3 report — 容器菜单、饰品栏数据绑定和快捷键网络

状态：DONE

## 实际改动

- 新增大型便携容器菜单链路：
  - `LargePocketGeneratorContainer`
  - `LargePotionCanisterContainer`
  - `LargeFuelCanisterContainer`
  - `LargePortableContainerBinding`
  - `LargePortableContainerMenus`
- 新增大型容器专用 handler：
  - `LargePotionCanisterHandler`：菜单输入改走 `LargePotionCanisterItem.tryFillBatch`
  - `LargeFuelCanisterHandler`：菜单输入改走大型燃料逻辑，并拒绝燃料罐递归填充
- 新增网络：
  - `OpenLargePortableContainerPayload`
  - `OpenLargePortableContainerPacket`
  - `JDTEPacketHandler` 注册服务端处理
- 新增客户端快捷键与事件：
  - `JDTEKeyMappings` 增加默认键 `G / P / F`
  - `LargePortableContainerClientEvents` 仅发送“容器种类”
- 新增客户端 screen：
  - `LargePocketGeneratorScreen`
  - `LargePotionCanisterScreen`
  - `LargeFuelCanisterScreen`
  - 布局沿用 JDT 对应原版纹理/槽位布局
- 更新菜单注册与 screen 注册：
  - `JDTEMenus`
  - `JDTEClientSetup`
- 更新三种大型物品的手持打开逻辑：
  - 手持打开不再走 JDT 原静态菜单处理器
  - 直接切到 JDTE 自己的菜单/provider
- 服务端 Curios 重查逻辑：
  - 客户端快捷键不上传 `ItemStack`
  - 服务端按精确槽位名重新查找真实物品：
    - `large_pocket_generator`
    - `large_potion_canister`
    - `large_fuel_canister`
  - `stillValid` 基于真实来源栈对象身份校验，避免菜单持有私有复制品

## TDD：RED → GREEN

### RED

先补了任务 3 的行为/边界测试到 `LargePortableContainerLogicTest`，覆盖：

- 大型药水菜单 handler 通过 `tryFillBatch` 完成 4 瓶批处理并返还 4 个玻璃瓶
- 批处理失败时不吞输入
- 大型燃料菜单 handler 拒绝燃料罐作为输入燃料
- `LargePortableContainerBinding` 必须绑定同一实际 `ItemStack` 实例
- `OpenLargePortableContainerPayload` 三种 kind 往返编码
- `OpenLargePortableContainerPayload` 拒绝未知网络值

RED 命令：

`.\gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest`

RED 结果：

- 失败（缺少 `LargePortableContainerBinding` / `LargePotionCanisterHandler` / `LargeFuelCanisterHandler` / `OpenLargePortableContainerPayload` 等实现）

### GREEN

按最小实现补齐测试所需类，再继续完成菜单/网络/client 打开链路。

GREEN 聚焦命令：

`.\gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest`

GREEN 结果：

- `BUILD SUCCESSFUL`

## 验证命令与结果

1. 聚焦测试

   `.\gradlew test --tests com.jdte.common.items.LargePortableContainerLogicTest`

   结果：`BUILD SUCCESSFUL in 32s`

2. 完整测试

   `.\gradlew test`

   结果：`BUILD SUCCESSFUL in 22s`

3. Java 编译

   `.\gradlew compileJava`

   结果：`BUILD SUCCESSFUL in 11s`

补充说明：

- 中途有一次我并行触发了两个 Gradle 测试任务，导致 `build/test-results/test/binary/output.bin` 文件锁删除失败；这是执行方式问题，不是代码失败。之后已串行重跑并通过。

## 自审

- 手持打开已切到 JDTE 自己的菜单/provider，没有再复用 JDT 固定 `1000 mB` / 原最小燃料消耗的原菜单处理器。
- 快捷键客户端只发送 `ContainerKind`，没有上传 `ItemStack`。
- 服务端打开 Curios 菜单时按槽位名重新找真实栈，并把真实栈对象交给菜单绑定。
- `stillValid` 使用来源 resolver + 对象身份判断，要求“来源仍在、类型一致、栈未被替换”。
- 大型燃料菜单关闭时会安全返还输入槽剩余物。
- Curios 未加载时，快捷键链路会安全返回；手持菜单不依赖 Curios。
- screen 采用 JDTE 自建适配，而不是强继承 JDT 原 screen，从而规避了：
  - JDT 原 screen 对主手来源的硬编码
  - JDT 原 potion/fuel 容量显示对原值的硬编码

## 疑虑 / 后续关注

1. 大型药水菜单当前已把输入槽上限放到 4，并正确调用 `tryFillBatch`；但仍建议在客户端/实机里确认 1.21.1 原版药水的堆叠交互是否能自然聚成 4。若原版点击行为仍受单瓶限制，后续可能需要进一步定制 slot 点击聚合体验。
2. 本任务未创建资源与本地化；快捷键翻译 key/category 已接到 JDTE 命名空间，但展示文本仍依赖后续资源任务补齐。

