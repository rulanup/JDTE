# AE2 提取升级实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增可绑定 AE2 无线访问点的锻造升级，使玩家携带或装备的 JDT/JDTE FE/流体物品从绑定 ME 网络自动补满。

**架构：** 自定义锻造配方把无线链接和 JDTE 启用组件写入目标物品；无 AE2 类型的玩家 Tick 服务收集增强物品，再把整批物品交给 AE2 隔离桥。桥按无线访问点分组，使用可单元测试的事务助手完成“模拟、提取、写入、差额退回”；Applied Flux 与 Curios 保持独立可选类加载边界。

**技术栈：** Java 21、Minecraft 1.21.1、NeoForge 21.1、AE2 19.2 公共 API、Applied Flux 公共 FE Key、Curios 9.4、JUnit 5、Gradle。

---

## 文件结构

**新建生产文件：**

- `common/items/AEExtractionUpgradeItem.java`：绑定状态 tooltip。
- `common/items/AEExtractionPlayerService.java`：玩家 Tick、库存收集、增强物品 tooltip。
- `common/recipes/AEExtractionSmithingRecipe.java`：两输入锻造配方和序列化器。
- `common/integrations/ae2/AEExtractionNetwork.java`：不暴露 AE2 类型的门面。
- `common/integrations/ae2/AEExtractionNetworkIntegration.java`：GridLinkables、访问点解析、流体 ME 操作。
- `common/integrations/ae2/AEExtractionEnergyIntegration.java`：Applied Flux FE 操作。
- `common/integrations/ae2/AEExtractionTransfer.java`：通用事务转移。
- `common/integrations/ae2/AEExtractionFluidPolicy.java`：流体选择和缓存。

**修改生产文件：**

- `setup/JDTEDataComponents.java`、`JDTEItems.java`、`JDTERecipes.java`、`JDTECreativeTabs.java`。
- `JDTE.java`：事件监听和 common setup 注册。

`JDTE.java`、`JDTEItems.java`、`JDTECreativeTabs.java` 当前已有用户未提交改动。内联执行必须使用窄补丁核对 diff，不得回退或提交既有 Repair Talisman、Energy Brewing Upgrade 等修改。

**测试：**

- `AEExtractionTransferTest.java`
- `AEExtractionFluidPolicyTest.java`
- `AEExtractionSmithingRecipeTest.java`
- `AEExtractionPlayerServiceTest.java`
- `AEExtractionUpgradeResourceTest.java`

**资源和文档：**

- 两个配方 JSON、物品模型、独立纹理、中英文语言文件和 GuideME 升级页。
- 运行现有生成脚本同步 Patchouli。
- 更新 `README.md`、`README_EN.md`、`CHANGELOG.md`、`AGENTS.md` 功能清单。

---

### 任务 1：事务式资源转移核心

**文件：**
- 创建：`src/main/java/com/jdte/common/integrations/ae2/AEExtractionTransfer.java`
- 测试：`src/test/java/com/jdte/common/integrations/ae2/AEExtractionTransferTest.java`

- [ ] **步骤 1：编写失败的转移测试**

```java
@Test
void movesOnlyTheAmountAcceptedByBothSides() {
    FakeSource source = new FakeSource(100);
    FakeSink sink = new FakeSink(80, 80);
    AEExtractionTransfer.Result result = AEExtractionTransfer.move(100, source, sink);
    assertEquals(80, result.moved());
    assertEquals(20, source.amount());
    assertEquals(0, result.unrestored());
}
```

同文件增加：零需求不调用端点、源模拟不足、源实际短缺、目标实际短缺会退款、退款短缺报告 `unrestored`。

- [ ] **步骤 2：运行并确认因类型缺失而失败**

```powershell
.\gradlew.bat test --tests "com.jdte.common.integrations.ae2.AEExtractionTransferTest"
```

- [ ] **步骤 3：实现最小通用事务**

```java
interface Source {
    long extract(long amount, boolean simulate);
    long restore(long amount);
}
interface Sink { long insert(long amount, boolean simulate); }
record Result(long moved, long unrestored) {}
```

`move` 顺序固定为：sink 模拟、source 模拟、source 实际、sink 实际、source 退款；所有端点返回值夹在 `0..requested`。

- [ ] **步骤 4：重跑定向测试，预期全部通过**

- [ ] **步骤 5：仅在干净功能分支中提交**

```powershell
git add src/main/java/com/jdte/common/integrations/ae2/AEExtractionTransfer.java src/test/java/com/jdte/common/integrations/ae2/AEExtractionTransferTest.java
git commit -m "feat: add transactional AE extraction transfer"
```

当前脏工作区内联执行时跳过提交，只保留已验证 diff。

---

### 任务 2：数据组件、绑定升级与锻造配方

**文件：**
- 创建：`AEExtractionUpgradeItem.java`、`AEExtractionSmithingRecipe.java`、`AEExtractionNetwork.java`、`AEExtractionNetworkIntegration.java`
- 修改：`JDTEDataComponents.java`、`JDTEItems.java`、`JDTERecipes.java`、`JDTE.java`
- 测试：`src/test/java/com/jdte/common/recipes/AEExtractionSmithingRecipeTest.java`

- [ ] **步骤 1：编写失败的锻造与组件保留测试**

```java
@Test
void boundUpgradePreservesResourcesAndWritesLink() {
    GlobalPos link = GlobalPos.of(Level.OVERWORLD, new BlockPos(3, 64, 5));
    ItemStack template = new ItemStack(JDTEItems.AE_EXTRACTION_UPGRADE.get());
    template.set(AEComponents.WIRELESS_LINK_TARGET, link);
    ItemStack base = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
    base.set(DataComponents.CUSTOM_NAME, Component.literal("Linked Wand"));
    base.set(JustDireDataComponents.FORGE_ENERGY, 12_345);
    SmithingRecipeInput input = new SmithingRecipeInput(template, base, ItemStack.EMPTY);
    ItemStack result = new AEExtractionSmithingRecipe().assemble(input, REGISTRY_ACCESS);
    assertEquals(link, result.get(AEComponents.WIRELESS_LINK_TARGET));
    assertEquals(12_345, result.get(JustDireDataComponents.FORGE_ENERGY));
    assertTrue(result.getOrDefault(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), false));
}
```

增加拒绝用例：未绑定、附加槽非空、非 JDT/JDTE 命名空间、无 capability、已增强。测试 `isTemplateIngredient/isBaseIngredient/isAdditionIngredient`。

- [ ] **步骤 2：运行测试，预期编译失败且只因新类型缺失**

```powershell
.\gradlew.bat test --tests "com.jdte.common.recipes.AEExtractionSmithingRecipeTest"
```

- [ ] **步骤 3：注册启用组件与非机器升级物品**

```java
public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AE_EXTRACTION_ENABLED =
        DATA_COMPONENTS.register("ae_extraction_enabled", () -> DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL)
                .cacheEncoding().build());
```

`AE_EXTRACTION_UPGRADE` 使用 `new Item.Properties().stacksTo(1)`，不得加入 `JDTEItems.upgrades()`。

- [ ] **步骤 4：实现链接门面和隔离类**

门面固定提供 `registerLinkable()`、`isLinked(ItemStack)`、`copyLink(source,target)`、`refill(ServerPlayer,List<ItemStack>)`。前三者复用 `GridLinkables` 和 `AEComponents.WIRELESS_LINK_TARGET`；只有 AE2 已加载才进入实现类。`refill` 此任务先安全空操作。

- [ ] **步骤 5：实现无字段 SmithingRecipe**

`matches` 执行全部批准条件；`assemble` 使用 `base.copyWithCount(1)`，复制链接后写启用组件。序列化器使用无字段 `MapCodec` 和不读写字节的 `StreamCodec`；`getResultItem` 返回空栈；`isAdditionIngredient` 返回 false。

- [ ] **步骤 6：注册 `ae_extraction_smithing` 序列化器，并在 common setup 注册 GridLinkable**

- [ ] **步骤 7：运行锻造测试和 `compileJava`，预期退出码 0**

- [ ] **步骤 8：干净分支提交 `feat: add bound AE extraction smithing upgrade`；当前脏工作区跳过提交**

---

### 任务 3：流体选择与玩家物品发现

**文件：**
- 创建：`AEExtractionFluidPolicy.java`、`AEExtractionPlayerService.java`
- 测试：`AEExtractionFluidPolicyTest.java`、`AEExtractionPlayerServiceTest.java`

- [ ] **步骤 1：编写失败的流体选择测试**

```java
@Test
void existingContentsWinAndEmptyUniversalTankStaysUnselected() {
    assertEquals(Fluids.WATER, AEExtractionFluidPolicy.select(
            FakeHandler.withWater(250, 1000), 0, List.of(Fluids.WATER, Fluids.LAVA)).getFluid());
    assertNull(AEExtractionFluidPolicy.select(
            FakeHandler.emptyUniversal(1000), 0, List.of(Fluids.WATER, Fluids.LAVA)));
}
```

增加空专用罐唯一候选、多储罐、满罐和零候选测试。

- [ ] **步骤 2：运行定向测试，确认 `AEExtractionFluidPolicy` 缺失失败**

- [ ] **步骤 3：实现策略**

策略返回保留数据组件的 `FluidStack` 原型：非空罐返回当前流体的 1 mB 副本；空罐只在 `isFluidValid` 接受的源流体恰好一个时返回该流体的 1 mB 原型。生产缓存键为 `Item` 身份与 tank 索引，实际填充前仍模拟复验。

- [ ] **步骤 4：编写失败的身份去重测试**

```java
@Test
void markedStackIsCollectedOnceByIdentity() {
    ItemStack marked = marked(new ItemStack(Items.STICK));
    assertEquals(List.of(marked), AEExtractionPlayerService.collectDistinct(
            List.of(marked, ItemStack.EMPTY, marked), List.of(marked)));
}
```

增加未标记忽略、内容相同但实例不同的两个栈都保留。

- [ ] **步骤 5：实现玩家服务**

服务端 `PlayerTickEvent.Post` 使用 `Inventory.getContainerSize()` 覆盖主库存、护甲、副手；调用现有 `AdvancedEnergyTransmitterPlayerEquipmentSources.collectCurios` 收集 Curios。去重后一次调用门面 `refill`。`ItemTooltipEvent` 只追加 `tooltip.jdte.ae_extraction.enabled`。

- [ ] **步骤 6：运行两个定向测试，预期全部通过**

```powershell
.\gradlew.bat test --tests "com.jdte.common.integrations.ae2.AEExtractionFluidPolicyTest" --tests "com.jdte.common.items.AEExtractionPlayerServiceTest"
```

- [ ] **步骤 7：干净分支提交 `feat: discover AE extraction items and fluid targets`**

---

### 任务 4：AE2、Applied Flux 与运行时事件

**文件：**
- 修改：`AEExtractionNetwork.java`、`AEExtractionNetworkIntegration.java`、`JDTE.java`
- 创建：`AEExtractionEnergyIntegration.java`
- 扩展：`AEExtractionTransferTest.java`、`AEExtractionFluidPolicyTest.java`

- [ ] **步骤 1：先增加 `Integer.MAX_VALUE` 单次上限和缓存候选被实际 fill 拒绝的失败测试**

- [ ] **步骤 2：运行测试，确认新增断言失败而不是测试配置错误**

- [ ] **步骤 3：按 `GlobalPos` 分组并解析访问点**

```java
ServerLevel linkedLevel = player.server.getLevel(link.dimension());
if (linkedLevel == null || !linkedLevel.isLoaded(link.pos())) return;
BlockEntity be = linkedLevel.getBlockEntity(link.pos());
if (!(be instanceof IWirelessAccessPoint accessPoint) || !accessPoint.isActive()) return;
IGrid grid = accessPoint.getGrid();
if (grid == null) return;
MEStorage storage = grid.getStorageService().getInventory();
IActionSource action = IActionSource.ofPlayer(player, accessPoint);
```

同一玩家、同一链接每 Tick 只解析一次；离线为正常空操作。
不增加距离或无线终端能耗检查；访问点已加载、active 且有网格时保持与 AE 输出升级一致的跨维度远程语义。

- [ ] **步骤 4：实现 Applied Flux FE 桥**

仅 `appflux` 已加载时引用 `FluxKey.of(EnergyType.FE)`。物品 sink 包装 `receiveEnergy(clampToInt(amount), simulate)`；ME source 包装同一 Key 的 extract 和退款 insert；每个物品单 Tick最多处理 `Integer.MAX_VALUE` FE。

- [ ] **步骤 5：实现 AE2 流体桥**

按储罐读取候选，创建保留组件的 `FluidStack`/`AEFluidKey`。物品 sink 使用 `fill`，ME source 使用同 Key 的 extract/insert。发生移动后重新读取 handler，再处理下一罐。

- [ ] **步骤 6：差额退款失败按“链接 + 资源 Key”每 200 Tick 最多记录一次 error**

- [ ] **步骤 7：在 `JDTE` 注册玩家 Tick 与 tooltip 监听器，保持现有监听器不变**

- [ ] **步骤 8：运行定向测试和 `compileJava`，预期退出码 0**

```powershell
.\gradlew.bat test --tests "com.jdte.common.integrations.ae2.AEExtractionTransferTest" --tests "com.jdte.common.integrations.ae2.AEExtractionFluidPolicyTest" --tests "com.jdte.common.items.AEExtractionPlayerServiceTest"
.\gradlew.bat compileJava
```

- [ ] **步骤 9：干净分支提交 `feat: refill linked items from AE storage`；当前脏工作区跳过提交**

---

### 任务 5：配方、资源、指南和契约测试

**文件：**
- 创建：`data/jdte/recipe/ae_extraction_upgrade.json`、`ae_extraction_upgrade_apply.json`
- 创建：`assets/jdte/models/item/ae_extraction_upgrade.json`、专用 PNG
- 修改：中英文语言、GuideME、创意页、README、CHANGELOG、AGENTS
- 测试：`src/test/java/com/jdte/common/items/AEExtractionUpgradeResourceTest.java`

- [ ] **步骤 1：编写失败的资源契约测试**

```java
@Test
void registersRecipesModelTranslationsAndGuideText() throws IOException {
    JsonObject craft = readJson("data/jdte/recipe/ae_extraction_upgrade.json");
    assertEquals("minecraft:crafting_shapeless", craft.get("type").getAsString());
    assertTrue(ingredients(craft).contains("ae2:wireless_receiver"));
    assertTrue(ingredients(craft).contains("ae2:export_bus"));
    JsonObject apply = readJson("data/jdte/recipe/ae_extraction_upgrade_apply.json");
    assertEquals("jdte:ae_extraction_smithing", apply.get("type").getAsString());
}
```

测试还断言：两个配方都有 AE2 `mod_loaded` 条件；物品注册、模型、专用纹理、四个语言键存在；GuideME 中英文包含 Applied Flux 与空通用容器说明。

- [ ] **步骤 2：运行资源测试，确认因资源缺失失败**

- [ ] **步骤 3：创建配方**

制作配方为 `ae2:wireless_receiver + ae2:export_bus + jdte:capacity_upgrade`，输出 1 个升级；应用配方 JSON 只有自定义 type。两者均带 AE2 条件。

- [ ] **步骤 4：创建模型与 16×16 独立纹理**

模型使用 `minecraft:item/generated`；纹理保持现有升级卡轮廓，以向内箭头区别输出升级，不覆盖现有 PNG。

- [ ] **步骤 5：添加创意页和语言键**

固定键：`item.jdte.ae_extraction_upgrade`、`tooltip.jdte.ae_extraction.linked`、`.unlinked`、`.enabled`。

- [ ] **步骤 6：更新 GuideME 后运行生成器同步 Patchouli**

```powershell
py -3 scripts/generate_patchouli_book.py
```

核对未删除既有 AE 输出、能量酿造和修复护符文档。

- [ ] **步骤 7：更新 README、CHANGELOG、AGENTS 功能清单，保留当前版本与既有修改**

- [ ] **步骤 8：运行资源测试和文档验证**

```powershell
.\gradlew.bat test --tests "com.jdte.common.items.AEExtractionUpgradeResourceTest"
.\gradlew.bat validateDocs
```

- [ ] **步骤 9：干净分支提交 `docs: add AE2 extraction upgrade resources`；当前脏工作区跳过提交**

---

### 任务 6：完整验证与需求核对

- [ ] **步骤 1：运行全部测试**

```powershell
.\gradlew.bat test
```

- [ ] **步骤 2：运行独立生产编译**

```powershell
.\gradlew.bat compileJava
```

- [ ] **步骤 3：运行文档验证**

```powershell
.\gradlew.bat validateDocs
```

- [ ] **步骤 4：构建发布 Jar**

```powershell
.\gradlew.bat jar
```

预期生成 `build/libs/jdte-0.6.0-pre1.jar`，全部命令退出码 0。

- [ ] **步骤 5：检查差异和需求清单**

```powershell
git diff --check
git status --short
```

逐项确认：绑定后锻造、组件保留、命名空间/capability 限制、玩家库存与 Curios、FE/流体双补充、通用空容器不乱选、离线安全、Applied Flux 可选、tooltip/指南/配方/模型均存在。明确区分本功能文件与任务开始前已有用户改动。

- [ ] **步骤 6：只在干净功能分支中提交最终修正；当前脏工作区只报告验证证据与未提交文件**
