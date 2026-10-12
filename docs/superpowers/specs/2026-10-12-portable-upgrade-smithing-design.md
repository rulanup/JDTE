# 便携物品升级统一使用锻造台

## 目标

统一 JDTE 便携物品上可安装升级的操作方式，使升级只通过锻造台应用，并保留升级目标已有的数据组件与资源状态。

## 已确认范围

- Ultimate Time Wand 的实体加速升级和 Ultimate Overclock 升级均通过锻造台安装。
- Entity Acceleration Upgrade 继续以升级物品为模板、Ultimate Time Wand 为底材，并保留当前配方要求的 Time Fluid Catalyst 添加物。
- Ultimate Overclock Upgrade 以升级物品为模板、Ultimate Time Wand 为底材；其安装成本保持现状，不增加新的添加物。
- AE Extraction Upgrade 已使用锻造台应用，本次保持其行为和绑定流程：先在 AE2 Wireless Access Point 绑定，再作为模板应用到合格的 JDT/JDTE 能量或流体物品。
- 结果复制底材的全部现有数据组件，只修改对应升级标记。已安装相同升级的目标、错误底材和错误材料组合均不匹配。
- 移除 Entity Acceleration Upgrade 与 Ultimate Overclock Upgrade 在物品栏/快捷栏中的副手点击安装路径。
- 机器升级卡继续保留机器 GUI 安装和潜行右键快捷安装；Extended Upgrade 继续执行现有的世界方块类型转换。

## 不在范围内

- 不改变机器升级卡的种类、容量、互斥规则、配方成本或机器安装逻辑。
- 不改变 Extended Upgrade 的方块替换、方块实体状态迁移或物品消耗逻辑。
- 不改变 Ultimate Time Wand 的模式切换、加速目标和资源消耗行为。
- 不改变 AE Extraction Upgrade 的绑定、AE 网络校验、适用物品判定或条件加载。

## 方案

沿用项目现有的自定义 SmithingRecipe 实现，为 Ultimate Time Wand 的两种升级提供明确的锻造台配方。Entity Acceleration 配方沿用当前自定义 smithing 配方和催化剂条件；Ultimate Overclock 增加专用配方实现，复制输入手杖后只设置 Ultimate Overclock 数据组件。保留 AE Extraction 的独立配方，因为它还需验证链接状态、物品能力和 AE2 可选依赖。

不建立通用升级配方框架：当前便携升级的目标、数据组件和材料条件不同，专用配方可直接表达各自规则，也能避免影响机器升级系统。

## 可观察行为

1. 在锻造台放入 Ultimate Time Wand 与 Entity Acceleration Upgrade，并提供 Time Fluid Catalyst 后，产出带实体加速标记的 Ultimate Time Wand；手杖的能量、流体、模式及其他已有组件均保留。
2. 在锻造台放入 Ultimate Time Wand 与 Ultimate Overclock Upgrade 后，产出带 Ultimate Overclock 标记的 Ultimate Time Wand，且保留手杖已有组件。
3. 已安装对应升级的手杖、非 Ultimate Time Wand 底材、缺失或错误的 Entity Acceleration 添加物均不能产出结果。
4. 两种升级都不再通过物品栏副手点击安装。
5. AE Extraction 的绑定与 smithing 应用、机器升级入口和 Extended Upgrade 方块转换维持原状。

## 验证

- 单元测试覆盖配方匹配、无效/重复安装拒绝、数据组件保留及正确升级标记。
- 配方资源和中英文指南覆盖检查确认安装步骤已说明为锻造台流程。
- 使用 JDTE 项目 Gradle Wrapper、Java 21 运行相关测试及 ./gradlew build。
- 构建和测试不替代游戏内验证；完成报告将区分已验证的代码路径与未进行的游戏内操作。