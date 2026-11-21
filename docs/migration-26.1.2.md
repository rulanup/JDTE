# JDTE 迁移到 Minecraft 26.1.2 / NeoForge 的执行计划

本计划从当前目录原地迁移，涵盖主模组 `jdte` 和独立附属 `jdte_ae`。清单中的版本来自本地项目快照。只有解析目标制品、运行对应场景并记录结果后，才能勾选相应验收项。

## 已核对的起点

| 项目 | 快照 | 迁移用途 |
|---|---|---|
| JDTE | `0d56f030`，0.6.5.4，主模块 790 个 Java 源文件，主模块和附属分别有 119、8 个 Java 测试源文件 | 以现有注册内容、测试、AGENTS.md 的维护约定和旧版运行行为作为对照 |
| IUCE | `561bad6`，Minecraft 26.1.2，NeoForge 26.1.2.126，Java 25，ModDevGradle 2.0.148，Gradle 9.2.1 | 对齐工具链、依赖坐标和运行配置。客户端与专服分别解析 |
| JustDireThings | 隔壁 `../JustDireThings` 的 `c2d73020`，Minecraft 26.1.2，JDT 1.6.11，其开发 NeoForge 为 26.1.2.21-beta | 查实际接口和 Mixin 目标。需另行锁定与源码相符的可复现二进制，并在 .126 上跑通 |

## IUCE 运行时依赖清单

JDTE 工厂打包机的裂变反应堆联动另需 Mekanism Generators。Gradle 已解析目标制品 `curse.maven:mekanism-generators-version-locked-1713978:9169604`（SHA-256 `B97BBC4EBDB334E287AB58A32B9FCA1AB7823DCCEA1A755549A5F2037625A065`）。该制品声明 `mekanismgenerators` 并要求 Mekanism 核心 `[10.8.0]`，与文件 `9267959` 的声明一致。主模块通过 `compileOnly` 编译联动，开发环境通过 `-PcompatibilityRuntime` 加载。裂变停机、传输器内容和仅安装核心的场景待游戏内验收。

Just Dyna Things 的 26.1.2 制品 `9649865` 未提供旧版 Echoing Buddings 方块和方块实体。制品中的 `time_buddings` 物品标签只引用 JDT 的时间水晶方块和芽块。孵化器通过 JDTE 方块标签识别 JDT 的时间水晶芽块和成熟晶簇，由 JDT 自己的随机刻处理生长阶段和维度规则。旧版 Dyna 专属芽块的供能、生长和采收需等上游恢复对应内容后再验收。

以 IUCE 的 `build.gradle` 中有效声明为准。坐标相同的依赖在 JDTE 根模块、测试和附属之间共用版本。编译直接引用某 API 的模块另配编译依赖；开发运行依赖不自动进入发布元数据。

| IUCE 配置 | 需要移入 JDTE 的坐标 |
|---|---|
| 默认通用，界面与指南 | Jade 本地制品 `Jade-mc26.1-NeoForge-26.1.1.jar`，`curse.maven:jei-238222:9193052`，`curse.maven:mezzconfig-1689768:9527941`，`maven.modrinth:guideme:26.1.17-beta` |
| 默认通用，AE 生态 | `maven.modrinth:ae2:26.1.13-beta`，`curse.maven:extendedae-plus-1337639:9006822`，`curse.maven:ex-pattern-provider-892005:9453241`，`maven.modrinth:glodium:26.1-1.2-neoforge`，`curse.maven:ae2-crystal-science-1435174:8133403` |
| 仅客户端 | `curse.maven:mouse-tweaks-60089:7840778`，`curse.maven:just-enough-characters-250702:9163773`，`curse.maven:modern-ui-352491:8248720`，`curse.maven:sodium-394468:8859590` |
| `-PcompatibilityRuntime` 公共部分 | `maven.modrinth:advancedae:26.1.7`，`curse.maven:geckolib-388172:8327201`，`curse.maven:sophisticated-core-618298:9524637`，`maven.modrinth:pf-emi:RHajWT5D`，`curse.maven:mekanism-version-locked-1713956:9267959`，`io.github.victor0hxz:Applied-Mekanistics-Version-Locked:1.6.0-port1` |
| 兼容组，未启用 eco | `curse.maven:ae2-lightning-tech-1527395:8121639` |
| 同时使用 `-PcompatibilityRuntime -PecoRuntime` | `maven.modrinth:neoecoae:26.2.15-beta`，`maven.modrinth:B1CBVXHX:mc26.1.x-26.1.2.46-neoforge` |

复用 IUCE 对 Modrinth、CurseMaven 的仓库过滤和 Applied Mekanistics 的 GitHub Release Ivy 文件名规则。Jade 优先锁到与本地 jar 一致的公开制品，记录 SHA-256；若需要本地供应，给出可重复的获取与校验步骤。IUCE 的 `libs` 还有一个 JEI jar，实际声明使用 CurseMaven，只装入一份。逐 jar 检查模组 ID、Minecraft 范围、NeoForge 范围和传递依赖，特别核实名称含 26.2 的 eco 制品。实际加载失败须记录具体配置、冲突和验证过的替代版本。

## 执行顺序

Botany Pots（CurseForge 项目 `353928`）和 Draconic Evolution（`223565`）的 CFWidget 文件索引未列出 26.1.2。Modrinth 按 26.1.2 / NeoForge 搜索也未命中，两者上游 Git 分支均未找到对应版本。当前构建缺少盆栽配方、混沌守卫、稳定刷怪笼及聚合注入器的专属联动。适配代码和相关测试保存在 `compat-pending/`，待目标版本可用后恢复注册、调用并复验。

### 2026-11-21 验收记录

- 当前主 jar SHA-256 为 `38A7A701EF5C8D22031C2600F8AD75E56AB590DD1220CBBAB1A4ED1CB6387F28`。独立客户端、专服加载的主 jar 与构建产物一致；正式双模块构建、629 项主模组测试、33 项附属测试和文档校验通过。移出不兼容的 ExtendedAE Plus 后，默认组合中的玩家成功登录、JEI 启动并在线重载，前后各 8,638 个配方，重复创意页签异常已修复。依据为 `build/port-final-tab-build.log`、`build/port-default-verified-server.log` 和 `build/port-default-verified-client.log`。
- 流体处理迁至原生 transfer API，生产源码与测试已移除 `IFluidHandler` 和 `FluidAction`。普通及大型温室在处理器限流、整批扣费失败时回滚全部已扣流体，测试覆盖不同组件和跨多个成员的支付。升级附件、便携容器和复制器等 16 个生产源码文件仍引用旧物品 API。实际 AE 装配产量、生态组合与其余功能矩阵待核验。

- 在旧世界副本中通过打包机正常启动入口实际展开 UUID 为 `baf4a54e-9065-4f1a-b62d-dc193881098d` 的原始工厂包。验收额外加载了 Sophisticated Storage 1.5.130.2225 和 Hostile Neural Networks 7.0.0，沿用 IUCE 的 Sophisticated Core 1.5.33。专服保存后读回全部 7 个方块的注册 ID、坐标与方块实体 ID，周边共 5 个物流节点物品和 5 个下界合金升级，与原包相符。打包机回到空闲、错误码为零，包槽和已认领文件均已清除。此次验收覆盖一次正常展开。依赖缺失时，读取包内未注册方块会报告其 ID；回归测试覆盖未知方块的读取失败。`build/port-factory-live-retest.log` 与 `build/port-factory-verify.py` 保留运行和落盘检查。

- 原始世界的两份独立副本中共有 176 台 JDTE 方块实体。冻结游戏刻后，在第二份副本强制加载主世界和末地各 42 个区块，正常停服保存。165 台所在区块从 DataVersion 3955 升级为 4790，11 台仍在旧格式区块。坐标和方块实体 ID 全部匹配。修复后的 JDT 能量附件、分页过滤槽和机器液体附件保持原内容，包括 512,000 mB 水；两台扩展方块交换器双向绑定回读正常。旧工厂包 UUID 文件的 SHA-256 在源世界和首份副本一致。冻结后的逐槽差异限于 28 个未注册物品槽：Create 锌矿石 6,762、深层锌矿石 6,793；Draconic Evolution 龙心 5、龙尘 327、创造电容 1,088。这些缺失物品在这次保存中丢失。原存档未改动。原始差异及专服日志在 `build/port-oldsave-retest/diff.json`、`build/port-matrix-native.log`。

- IUCE 原组合在加载真实旧世界的 ExtendedAE 装配矩阵时崩溃。Plus 1.6.3（9006822）要求两个装配器字段的描述符为 `ReferenceSet`。26.1.2 官方 ExtendedAE 1.0.1 至 1.1.0 声明为 `Set`，1.0.0 的字段匹配但因 `TileAssemblerMatrixPattern.patternInventory` 类型不匹配而启动失败。默认开发运行配置保留 IUCE 的 ExtendedAE 1.0.6，移出 Plus；工厂打包机的 Plus 无线收发器可选适配仍参与编译。上表记载 IUCE 的初始清单。两次故障分别记录于 `build/port-oldsave-retest.log` 和 `build/port-matrix-retest.log`。

- `:build :jdte-ae:build validateDocs` 在 Java 25 和 NeoForge 26.1.2.126 下通过。主模块 629 项、附属 33 项，零失败和跳过。文档校验覆盖 1,062 个翻译键、71 个方块和 46 页指南。
- 独立测试目录使用 NeoForge ModDev 启动器，清空开发源码模组列表，把两个构建 jar 放到客户端和专服各自的 `mods/`。默认 IUCE 运行组合上，客户端登录，专服确认玩家在线，在线重载仍解析 8,697 个配方。
- 放置矿物提取器、充能避雷针和高级时间加速器，发放高级升级箱。重开同一存档后读回三个方块实体 ID，再次在线重载。改正区块保存屏障后，最终停服在一秒内保存全部维度并退出。日志在 `build/port-persistence-server.log` 和 `build/port-persistence-client.log`。
- 此项覆盖 26.1.2 创建的存档。旧版存档升级、其余依赖组合、工厂打包完整恢复及实际 AE 网络装配仍需单独验证。在线时发现的 JDT 扩展手册分类问题已修改中英文条目，待下一次客户端读书复验。

每阶段保留所用 commit、坐标、命令和失败样例，便于从最后通过的阶段继续。

1. **固定旧版行为和依赖台账。** 在旧提交的独立工作树用原工具链运行两个模块的测试与文档校验，记录既有失败。导出注册 ID、配方类型、数据组件、附件、菜单、网络消息、配置默认值和 Mixin 清单。从旧版创建可复制的带状态样本，包括机器升级、物品箱、模式卡、传送坐标、矿物调查、多方块及已打包工厂。将每项功能关联到现有测试或游戏内场景。依赖台账逐项读取根构建、`dependencies.gradle`、附属构建和两个 TOML，记录旧坐标、调用者、目标坐标、元数据、验证环境和状态。
2. **先解决 JDT 与其他上游制品。** 核实 JDT 1.6.11 的发布文件及强制依赖；若未找到可用发布物，从上述固定提交构建并提供 CI 可重现的制品来源和哈希。用 IUCE 的 Java/NeoForge 组合在空实例单独启动 JDT。对 JDTE 特有依赖逐一核实，重点包括 Curios、Applied Flux、Create、Mekanism Generators、PneumaticCraft、Productive Bees/ProductiveLib、Botany Pots、神秘农业及附属、Just Dyna Things、Draconic Evolution/BrandonsCore、Apothic Spawners、FTB Ultimine、Logistics Networks、Polymorph、IPN 和旧版 JEI/EMI 桥接。为每项确认目标分支的 API 和真实运行载体；无可用移植时登记受影响功能、所需上游或替代实现、复测场景，继续处理独立模块，该项保持未完成。
3. **切换两个模块的构建。** 更新 wrapper 及对应发行包校验和、根与附属的插件和 Java toolchain、CI Java、两个 TOML 和资源展开属性。Minecraft 范围定为 `[26.1.2]`，以 .126 为开发与首轮验收版本；发布范围按实测填写。移除旧 Parchment 配置，统一 ModDev 使用的 NeoForge 与显式依赖版本。检查两个 Mixin JSON 的 Java 兼容级别及实际 Mixin 支持。同步 `compileOnly`、测试和附属本地运行中的重复 JDT/AE2 坐标，按上表重建默认、客户端、compat、eco 配置，另建可排除可选模组的最小测试配置。核对 ProductiveLib 嵌套包路径与公共输出类型，确保生成物只进编译路径。验收为干净缓存可解析各配置，两个发布 jar 的依赖声明保持主模组可选 AE2、附属强制匹配 JDTE 与 AE2。
4. **迁移公共 API 和一条完整机器路径。** 从 `JDTE.java`、`setup/`、升级附件、容器、自动 I/O 和网络包入手。对照目标 JDT 的 `JDTRegistration`、`MiscHelpers` 和 `BaseMachineBE`；后者的存取签名已改为 `ValueInput/ValueOutput`，机器物品处理器返回 `ItemStacksResourceHandler`。核对 Identifier、注册键、组件 Codec/StreamCodec、配方输入、能力注册、面向与缓存失效。按目标 transfer API 实现物品、流体和 FE 转移，逐项映射模拟、部分接受、提交、撤销和通知时机。先贯通 Fluid Mixer 从放置、菜单、双输入、支付、输出到破坏掉落、重放置和重启；输出不足时输入、FE、产物均保持原值。再复用已验证的公共接口迁移其他机器。
5. **迁移服务端功能并逐项对照。** 按下方场景分组处理 block、BE、容器、物品、升级、调度、管理器和相应 Mixin。对每个 `@Inject`、访问器、调用器和重定向记录新 owner、方法描述符、注入点、实际调用次数和分侧条件；逐项检查可选模组缺失时的类加载。特别核对 JDT 父类的 ticker、保存和掉落顺序，防止双执行。专服执行相应游戏场景，并观察变更落盘和重载。
6. **迁移客户端和资源。** 迁移菜单槽位、输入事件、HUD、粒子、方块实体及实体渲染；对照 IUCE 的 26.1.2 渲染器和 JEI/Jade 插件验证实际签名。逐项核对物品客户端定义、模型、方块状态、纹理、配方、战利品、标签、附属数据条件、GuideME/Patchouli、双语文本、生成器与 pack 元数据。通过资源重载、服务端 `/reload` 和玩家重新连接检查动态数据与缓存。屏幕实际测试分页、Shift 点击、拖放、坐标编辑、按住挖掘及 `2i` 的槽位范围。
7. **迁移 AE2 与附属。** 主模组先测无 AE2，再测仅 AE2 的单设备回退；离线或启动中的节点应判为不活跃。附属依次迁移 `AEGridResolver`、`AEGridVirtualTickExecutor`、`AEVirtualTickContext` 和 `TickHandlerMixin`，从目标 AE2 源码/字节码定位四阶段生命周期和时钟读取点。用原版 Molecular Assembler 及 ExtendedAE 装配器实际合成，验证同网去重、相邻帧时钟单调、异常清理、卸载和停服重启。同步验证 AE 输出/补给、网络绑定、在线任务门控和 Applied Flux 分支。
8. **执行发布审计。** 用迁移后的正式 jar 在独立客户端和专服测试，保存依赖树、Mixin 应用日志、回归结果、失败记录和实际版本。更新 README、中英文指南、AGENTS、当前发布说明和 CI。历史发行说明和旧存档 fixture 可保留版本出处。全部功能有通过证据后再标记迁移完成。

## 功能验收矩阵

| 范围 | 必须复现的行为 |
|---|---|
| 升级与扩展机器 | 八种 JDT T2 转换前后设置、附件和库存守恒；四/八槽限制、冲突和各档上限；设置复制器复用、补缺、退还，高级升级箱含 Curios 和侧栏双向 Shift 转移 |
| 合成与输出 | 幽灵配方服务端验证、铁砧 1 XP、三档卡容量、精确组件匹配、共享原料轮转、容器余物及网络部分接收；待处理原料跨重启保留 |
| 方块/实体加速与冻结 | 两台 16x 重叠为 31x；1024x 为 1023 次额外生命周期；费用不足整批拒绝；常规队列预算、实体 AI/碰撞抑制、维度天气冻结及管理员命令 |
| 能源、收集与区域设备 | 高级/扩展发射器、自目标与已移除目标、跨维度 UUID 充电、缓存失效、超载限额；收集器生成前拦截和容量触发 AE 插入；区域阻挡、实体抑制、声画开关和六种目标 |
| 生产与多方块 | Greenhouse/Large Greenhouse、Bio Factory、Life Breeder、Life Synthesis Vat、矿物提取器双尺寸：模板复用、17 部件定位、允许的外侧 I/O、产物分页、队列背压；矿物 1x 只需 FE，可选流体及注册表安全遍历；验证升级后 6,400,000 堆叠和容量缩小时的显示 |
| 配方与生物设备 | 胶激活、凝胶生成、流体稳定与收发、晶体孵化、混合并行、生物粉碎/提取、生命提取、灌注、药水六步和两种燃料、掉落模拟；检查产物数量、实体冷却、专用 FakePlayer 清理及刷新后的 JEI 嵌套战利品 |
| 工厂打包 | 含物品/流体、实体树、计划 tick 与内部链接的工厂旋转还原；中断、重试、重启恢复不丢不重；分别验证 AE 移动、Logistics 节点、Mekanism 裂变停机与放射性传输器 |
| 物品与世界交互 | 传送枪分页、同维 25 mB/格上限 25,000 与跨维 500 mB；储罐四模式、时间多用工具延迟重置、法杖倍率、扳手批量、护符修理 FE 与永久生命苹果重登；避雷针天气门控、三档频率及独立渲染音效 |
| 数据与可选生态 | 对注册清单逐 ID 验证加载、可获得性、菜单/模型及序列化；每个集成各测缺失和存在，用真实作物、蜂笼、网络、刷怪笼或配方验证结果；测试 KubeJS 内容开关对注册内容及浏览器显示的影响 |

## 最终执行与完成条件

### 便携物品与默认组合复测

普通和高级升级箱的菜单及物品能力共用目标版组件处理器。测试覆盖单张上限为 1 的卡在箱内堆叠至 64、组件保存回读、机器插卡上限、大型口袋发电机耗料，以及大型药水罐将四瓶药转为 1,000 mB 并返还四个空瓶。药水罐的未提交事务回滚后保留原有三瓶药和零储液，随后提交同一次补料即可完成整批转换。

本次执行 `:build :jdte-ae:build validateDocs`，主模组 630 项、附属 33 项测试全部通过，无跳过。两个实际 JAR 在独立默认客户端与专服完成登录、JEI 启动、在线重载和退出，重载前后均有 8,638 个配方。构建记录在 `build/port-component-verification.log`，双端日志在 `build/port-default-verified-{server,client}.log`。

| 默认组合验收制品 | SHA-256 |
|---|---|
| `jdte-0.6.5.4.jar` | `0D726DE03F7F598B610184012C5B76C83D8A9FB7A2719070C18F08946DBC3E50` |
| `jdte-ae-0.6.5.4.jar` | `3E180158A11C84380ED0588D457358E3B82E87BD2A432AAFB517E0AA22338896` |

仍须迁移 9 个生产文件中的旧物品接口，复测其余运行组合、AE 装配产出和加速倍率。旧存档副本仍有 28 个机器库存槽因缺少 Create 或 Draconic Evolution 的物品注册而丢失内容，另有 11 台机器所在区块尚未完成升级。

以下 PowerShell 命令作为目标工程的验收入口；专服每次使用独立测试存档，等启动完成并执行场景后正常停服。最小配置的具体 Gradle 开关在阶段 3 确定。

```powershell
.\gradlew.bat --version
.\gradlew.bat :dependencies --configuration runtimeClasspath
.\gradlew.bat :dependencies --configuration clientRuntimeRuntimeClasspath
.\gradlew.bat :jdte-ae:dependencies --configuration testRuntimeClasspath
.\gradlew.bat clean :build :jdte-ae:build validateDocs
.\gradlew.bat runServer
.\gradlew.bat runClient
.\gradlew.bat :jdte-ae:runServer
.\gradlew.bat runClient -PcompatibilityRuntime
.\gradlew.bat runClient -PcompatibilityRuntime -PecoRuntime
```

- [ ] 在最小主模组、主模组+AE2、主模组+附属+AE2、IUCE 默认、compat、compat+eco 各组合验证适用的客户端/专服。专服不装四个客户端专用依赖；实测缺失可选模组时机器、菜单及资源加载。
- [ ] 对主模块、附属、测试、datagen、Mixin 描述符、Gradle、CI、两个生成的 TOML 和最终 jar 扫描旧坐标与 API。初筛词包括 `1\.21\.1|21\.1\.216|21\.1\.233|7463040|JAVA_21|ResourceLocation|IItemHandler|IFluidHandler|IEnergyStorage`。补入阶段 4、5 发现的删除/改名符号，核实命中归属。以目标 classpath 编译及反射/Mixin 实际执行覆盖字符串引用。检查最终依赖树和 jar 内容，排除旧 jar、意外嵌入依赖和本机绝对路径。
- [ ] 比对旧、新注册 ID 和配置键，逐项回读旧 fixture，再做新格式的保存、卸载、重载和第二次保存。对真实旧世界只用副本测试，分别记录 Minecraft/JDT 上游能否升级和 JDTE 自有数据是否守恒。
- [ ] 汇总两个模块实际运行的测试数、失败与跳过原因；每项功能绑定证据。附属专服之外，使用客户端装载两个发布 jar 加入专服，验证协议和界面。存在未供应的上游、未覆盖的 Mixin 或功能退化时，列出对应功能与复测入口，继续保留未完成状态。
